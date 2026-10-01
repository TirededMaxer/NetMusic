"""Verify real public streams with codecs and HLS handlers from the shipped JAR on GitHub."""
import json, subprocess, urllib.request, urllib.parse, zipfile, time
from pathlib import Path
from concurrent.futures import ThreadPoolExecutor
import generate_radio_catalog as directory

def fetch(url, limit=65536):
    for attempt in range(4):
        try:
            request = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0 (NetMusic)"})
            with urllib.request.urlopen(request, timeout=25) as response:
                return response.read(limit), response.geturl()
        except Exception:
            if attempt == 3: raise
            time.sleep(2 ** attempt)

def hls_sample(url):
    for _ in range(4):
        body, effective = fetch(url)
        lines = body.decode().splitlines()
        if any(line.startswith("#EXT-X-STREAM-INF:") for line in lines):
            index = next(i for i, line in enumerate(lines) if line.startswith("#EXT-X-STREAM-INF:"))
            url = urllib.parse.urljoin(effective, next(line for line in lines[index+1:] if line and not line.startswith("#")))
            continue
        segments = [line for line in lines if line and not line.startswith("#")]
        if not segments: raise ValueError("Empty live playlist")
        samples = []
        for segment in segments[-2:]:
            data, _ = fetch(urllib.parse.urljoin(effective, segment), 1048576)
            if data.startswith(b"ID3"):
                size = sum((data[6+i] & 127) << (21-7*i) for i in range(4))
                data = data[10+size:]
            samples.append(data)
        return b"".join(samples)
    raise ValueError("Too many HLS levels")

def main():
    output = Path("build/radio-audio-samples"); output.mkdir(parents=True, exist_ok=True)
    nationals = directory.request("/web/appBroadcast/list", {"categoryId": "0", "provinceCode": "0"})
    probes = []
    for row in nationals:
        urls = list(dict.fromkeys(row[key] for key in ("playUrlLow", "playUrlMulti", "mp3PlayUrlHigh", "mp3PlayUrlLow") if row.get(key)))
        probes.append((row["title"], urls, row["title"] in ("音乐之声", "中国之声")))
    probes += [
        ("BBC", ["https://stream.live.vc.bbcmedia.co.uk/bbc_world_service"], False),
        ("ABC", ["https://abc.streamguys1.com/live/rnnsw/icecast.audio"], False),
    ] + [(row["name"], [row["url"]], False) for row in json.loads(Path("scripts/regional_stations.json").read_text())]
    jar = next(Path("build/libs").glob("*-all.jar"))
    with zipfile.ZipFile(jar) as archive:
        reader = next(name[:-6].replace("/", ".") for name in archive.namelist() if name.endswith("/TSAudioFileReader.class"))
    logging_jars = list((Path.home() / ".gradle/caches/modules-2/files-2.1/org.slf4j/slf4j-api").glob("**/*.jar"))
    classpath = ":".join([str(jar)] + [str(path) for path in logging_jars[:1]])
    def probe(item):
        index, (name, urls, live_test) = item
        errors, network = [], []
        for url in urls:
            try: data = hls_sample(url) if ".m3u8" in url else fetch(url)[0]
            except Exception as error:
                network.append(str(error)); continue
            path = output / (str(index) + ".audio"); path.write_bytes(data)
            args = ["java", "--class-path", classpath, "scripts/AudioDecodeProbe.java", str(path), reader]
            result = subprocess.run(args, capture_output=True, text=True, timeout=60)
            if result.returncode:
                errors.append(result.stderr); continue
            record = {"name": name, "encoded_bytes": len(data), "mono_decoder": result.stdout.strip()}
            if live_test:
                live = subprocess.run(args + [url], capture_output=True, text=True, timeout=90)
                if live.returncode:
                    errors.append(live.stderr); continue
                record["shipped_hls_handler"] = live.stdout.strip()
            if errors or network: record["fallback_attempts"] = errors + network
            print(json.dumps(record, ensure_ascii=False), flush=True)
            return record
        record = {"name": name}
        if errors: record["decoder_errors"] = errors
        else: record["network_unavailable"] = network
        print(json.dumps(record, ensure_ascii=False), flush=True)
        return record
    records = list(ThreadPoolExecutor(max_workers=4).map(probe, enumerate(probes)))
    Path("build/radio-audio-report.json").write_text(json.dumps(records, ensure_ascii=False, indent=2), encoding="utf-8")
    failures = [record for record in records if record.get("decoder_errors") or
                (record["name"] in ("音乐之声", "中国之声") and "shipped_hls_handler" not in record)]
    assert not failures, failures
if __name__ == "__main__":
    main()
