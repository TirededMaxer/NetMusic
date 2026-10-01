"""Fetch bounded public samples on GitHub and verify the codecs in the actual shaded JAR."""
import json, subprocess, urllib.request, urllib.parse, zipfile
from pathlib import Path

def fetch(url, limit=262144):
    request = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    with urllib.request.urlopen(request, timeout=25) as response:
        return response.read(limit), response.geturl()

def hls_sample(url):
    for _ in range(4):
        body, effective = fetch(url)
        lines = body.decode().splitlines()
        if any(line.startswith("#EXT-X-STREAM-INF:") for line in lines):
            index = next(i for i, line in enumerate(lines) if line.startswith("#EXT-X-STREAM-INF:"))
            variant = next(line for line in lines[index+1:] if line and not line.startswith("#"))
            url = urllib.parse.urljoin(effective, variant)
            continue
        segments = [line for line in lines if line and not line.startswith("#")]
        assert len(segments) >= 2, "No live audio segments"
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
    output = Path("build/radio-audio-samples")
    output.mkdir(parents=True, exist_ok=True)
    stations = json.loads(Path("src/main/resources/assets/netmusic/radio_stations.json").read_text())
    national = next(row for row in stations if row["group"] == "总台")
    # Resolve a signed official stream with the same request signing as directory generation.
    import generate_radio_catalog as directory
    info = next(row for row in directory.request("/web/appBroadcast/list", {"categoryId": "0", "provinceCode": "0"})
                if str(row["contentId"]) == urllib.parse.parse_qs(urllib.parse.urlparse(national["url"]).query)["id"][0])
    official_url = next(info[key] for key in ("playUrlLow", "playUrlMulti", "mp3PlayUrlHigh", "mp3PlayUrlLow") if info.get(key))
    probes = [
        ("cnr", official_url),
        ("bbc", "https://stream.live.vc.bbcmedia.co.uk/bbc_world_service"),
        ("abc", "https://abc.streamguys1.com/live/rnnsw/icecast.audio"),
    ] + [(row["name"], row["url"]) for row in json.loads(Path("scripts/regional_stations.json").read_text())]
    jar = next(Path("build/libs").glob("*-all.jar"))
    with zipfile.ZipFile(jar) as archive:
        reader = next(name[:-6].replace("/", ".") for name in archive.namelist() if name.endswith("/TSAudioFileReader.class"))
    logging_jars = list((Path.home() / '.gradle/caches/modules-2/files-2.1/org.slf4j/slf4j-api').glob('**/*.jar'))
    classpath = ':'.join([str(jar)] + [str(path) for path in logging_jars[:1]])
    records = []
    failures = []
    for index, (name, url) in enumerate(probes):
        data = hls_sample(url) if ".m3u8" in url else fetch(url)[0]
        path = output / (str(index) + ".audio")
        path.write_bytes(data)
        result = subprocess.run(["java", "--class-path", classpath, "scripts/AudioDecodeProbe.java", str(path), reader],
                                check=False, capture_output=True, text=True, timeout=60)
        if result.returncode:
            print(name, result.stdout, result.stderr, flush=True)
            failures.append(name)
            records.append({'name': name, 'error': result.stderr})
            continue
        record = {"name": name, "encoded_bytes": len(data), "decoder": result.stdout.strip()}
        records.append(record)
        print(json.dumps(record, ensure_ascii=False), flush=True)
    Path("build/radio-audio-report.json").write_text(json.dumps(records, ensure_ascii=False, indent=2), encoding="utf-8")
    assert not failures, "Packaged audio decoding failures: " + str(failures)
if __name__ == "__main__":
    main()
