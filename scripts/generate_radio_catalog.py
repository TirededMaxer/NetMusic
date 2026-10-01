"""Build the station directory on GitHub; stream signatures are resolved at playback time."""
import hashlib, json, re, time, urllib.parse, urllib.request
from pathlib import Path
from concurrent.futures import ThreadPoolExecutor

API = "https://ytmsout.radio.cn"
KEY = "f0fc4c668392f9f9a447e48584c214ee"

def request(path, params):
    query = urllib.parse.urlencode(sorted(params.items()))
    for attempt in range(4):
        try:
            stamp = str(int(time.time() * 1000))
            text = (query + "&" if query else "") + "timestamp=" + stamp + "&key=" + KEY
            sign = hashlib.md5(text.encode()).hexdigest().upper()
            req = urllib.request.Request(API + path + ("?" + query if query else ""), headers={
                "timestamp": stamp, "sign": sign, "equipmentId": "0000", "platformCode": "WEB",
                "Content-Type": "application/json", "User-Agent": "Mozilla/5.0"})
            data = json.load(urllib.request.urlopen(req, timeout=30))
            if data["code"] != 0 or not isinstance(data["data"], list):
                raise ValueError(data.get("message"))
            return data["data"]
        except Exception:
            if attempt == 3:
                raise
            time.sleep(2 ** attempt)

def get_json(url):
    req = urllib.request.Request(url, headers={"User-Agent": "NetMusic-directory-build"})
    return json.load(urllib.request.urlopen(req, timeout=30))

def main():
    provinces = request("/web/appProvince/list/all", {})
    cities = get_json("https://raw.githubusercontent.com/modood/Administrative-divisions-of-China/master/dist/cities.json")
    names = {}
    for city in cities:
        name = city["name"]
        if name in ("市辖区", "县", "省直辖县级行政区划", "自治区直辖县级行政区划"):
            continue
        stem = re.sub(r"(市|地区|自治州|盟)$", "", name)
        names.setdefault(city["provinceCode"], []).append((stem, name))
    def collect(province):
        code = str(province["provinceCode"])
        name = province["provinceName"]
        rows = request("/web/appBroadcast/list", {"categoryId": "0", "provinceCode": code})
        stations = []
        for row in rows:
            title = row["title"]
            city = "省级"
            matches = [(stem, ct) for stem, ct in names.get(code[:2], []) if stem and stem in title]
            if matches:
                city = max(matches, key=lambda pair: len(pair[0]))[1]
            stations.append({
                "name": title, "url": "https://www.radio.cn/pc-portal/erji/radioStation.html?" +
                    urllib.parse.urlencode({"provinceCode": code, "id": row["contentId"]}),
                "group": "总台" if code == "0" else "国内", "province": "" if code == "0" else name,
                "city": "" if code == "0" else city
            })
        return name, stations
    results = list(ThreadPoolExecutor(max_workers=4).map(collect, provinces))
    stations = [station for _, rows in results for station in rows]
    supplements = json.loads(Path("scripts/regional_stations.json").read_text(encoding="utf-8"))
    seen_urls = {station["url"] for station in stations}
    additions = []
    for station in supplements:
        if station["url"] not in seen_urls:
            stations.append({key: station[key] for key in ("name", "url", "group", "province", "city")})
            additions.append(station)
            seen_urls.add(station["url"])
    foreign = [
        ("BBC World Service", "英国", "https://stream.live.vc.bbcmedia.co.uk/bbc_world_service"),
        ("NPR", "美国", "https://npr-ice.streamguys1.com/live.mp3"),
        ("RFI Monde", "法国", "https://rfimonde64k.ice.infomaniak.ch/rfimonde-64.mp3"),
        ("Franceinfo", "法国", "https://icecast.radiofrance.fr/franceinfo-midfi.mp3"),
        ("Deutschlandfunk", "德国", "https://st01.sslstream.dlf.de/dlf/01/128/mp3/stream.mp3"),
        ("Deutschlandfunk Kultur", "德国", "https://st02.sslstream.dlf.de/dlf/02/128/mp3/stream.mp3"),
        ("Deutschlandfunk Nova", "德国", "https://st03.sslstream.dlf.de/dlf/03/128/mp3/stream.mp3"),
        ("ABC Radio National", "澳大利亚", "https://abc.streamguys1.com/live/rnnsw/icecast.audio"),
    ]
    for name, country, url in foreign:
        stations.append({"name": name, "url": url, "group": "国外", "province": country, "city": ""})
    counts = {name: len(rows) for name, rows in results}
    assert counts.get("国家", 0) >= 19, counts
    assert len(stations) > 500, counts
    assert len(provinces) >= 32, counts
    ids = [(s["group"], s["province"], s["url"]) for s in stations]
    assert len(ids) == len(set(ids)), "Duplicate station IDs"
    output = Path("src/main/resources/assets/netmusic/radio_stations.json")
    output.write_text(json.dumps(stations, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    report = {"source": "https://www.radio.cn/pc-portal/erji/radioStation.html",
              "generated_at": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
              "total": len(stations), "regions": counts, "foreign": len(foreign),
              "verified_regional_additions": additions,
              "city_classification": "Official station title matched against published administrative city names",
              "coverage": "All entries returned by the official public directory, not unavailable or unlisted stations."}
    Path("build").mkdir(exist_ok=True)
    Path("build/radio-directory-report.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False, indent=2))

if __name__ == "__main__":
    main()
