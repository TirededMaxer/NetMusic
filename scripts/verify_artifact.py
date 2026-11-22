"""Inspect the version-specific Fabric distribution produced by Actions."""
import json
import sys
import zipfile
from pathlib import Path

target = sys.argv[1]
assert target in json.loads(Path('versions.json').read_text())
jars = list((Path('build') / target / 'libs').glob('*-all.jar'))
assert len(jars) == 1, jars
with zipfile.ZipFile(jars[0]) as jar:
    names = jar.namelist()
    for cls in ('playback/WorldPlaybackManager', 'client/command/ClientCommands',
                'client/gui/NetMusicControlScreen', 'client/audio/LyricsActionBar',
                'client/ClientPlatform', 'api/AudioHttpTransport'):
        assert f'com/github/tartaricacid/netmusic/{cls}.class' in names, cls
    assert not any(any(s in name.lower() for s in ('maid', 'backpack', 'localfilehandler', 'preset')) for name in names)
    assert not any('Computer' in name or 'NetMusicCommand.class' in name for name in names)
    stations = json.loads(jar.read('assets/netmusic/radio_stations.json'))
    assert len(stations) > 500
    metadata = json.loads(jar.read('fabric.mod.json'))
    assert metadata['depends']['minecraft'] == '=' + target, metadata
    assert metadata['depends']['java'] == '>=25'
    assert 'fabricloader' in metadata['depends']
    assert f'+mc{target}' in metadata['version']
    zh = json.loads(jar.read('assets/netmusic/lang/zh_cn.json'))
    en = json.loads(jar.read('assets/netmusic/lang/en_us.json'))
    assert zh.keys() == en.keys()
    print(jars[0], metadata['version'], len(stations), 'stations', len(zh), 'language keys')
