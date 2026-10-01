"""Verify removed features, command boundary and language coverage on GitHub."""
import json,re
from pathlib import Path

source = Path("src/main/java/com/github/tartaricacid/netmusic")
files = list(source.rglob("*.java"))
text = "\n".join(path.read_text() for path in files)
assert not re.search(r"(?i)maid|backpack|ENABLE_STEREO|PROXY_TYPE|PROXY_ADDRESS|ConfigProxySelector", text)
assert not (source / "command/NetMusicCommand.java").exists()
assert not (source / "init/CommandRegistry.java").exists()
command = (source / "client/command/ClientCommands.java").read_text()
assert command.count('literal("netmusic")') == 1 and ".then(" not in command
editor = (source / "client/gui/BigMegaphoneScreen.java").read_text()
assert "VolumeSlider" not in editor and "Action.SAVE" not in editor
packet = (source / "network/message/BigMegaphoneControlMessage.java").read_text()
assert not re.search(r"\bSAVE\b|\brange\b|\bvolume\b", packet)
assert "onRedstoneSignalChanged" in (source / "tileentity/TileEntityBigMegaphone.java").read_text()
assert "manager.setRadioEnabled(!manager.radioEnabled())" in (source / "tileentity/TileEntityBigMegaphone.java").read_text()
assert "playerMusic(" in (source / "block/BlockMusicPlayer.java").read_text()
assert (source / "client/gui/NetMusicControlScreen.java").read_text().count("Button.builder(") == 3
keys = set(re.findall(r'Component\.translatable\("([^"]+)"', text))
langs = {}
for lang in ("zh_cn", "en_us"):
    data = json.loads(Path("src/main/resources/assets/netmusic/lang/" + lang + ".json").read_text())
    assert not any(re.search(r"maid|backpack|stereo|proxy|big_megaphone\.(save|volume)|^command\.netmusic", key, re.I) for key in data)
    missing = keys - data.keys()
    assert not missing, (lang, missing)
    langs[lang] = len(data)
assert set(json.loads(Path("src/main/resources/assets/netmusic/lang/zh_cn.json").read_text())) == set(json.loads(Path("src/main/resources/assets/netmusic/lang/en_us.json").read_text()))
print(json.dumps({"only_command": "/netmusic", "control_buttons": 3, "language_keys": langs, "feature_removals": "passed"}, ensure_ascii=False))
