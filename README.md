# NetMusic — Minecraft 26.1.2 Fabric

Fork of [TartaricAcid/NetMusic](https://github.com/TartaricAcid/NetMusic), based on `26.1-fabric`.

Download the playable `netmusic-*-all.jar` from [Releases](https://github.com/TirededMaxer/NetMusic/releases).
Requires Java 25, Fabric Loader 0.19.2+, Fabric API for 26.1.2, Forge Config API Port and Cloth Config. Mod Menu is optional.
Install the same version on the client and server.

## Commands

- `/netmusic config`: client configuration screen, no operator permission or Mod Menu required.
- `/netmusic music_player on|off`: enable/disable the world's music player. On selects the nearest loaded player with a disc.
- `/netmusic big_megaphone on|off`: switch every megaphone in this world.
- Aliases: `jukebox`, `megaphone`, `唱片机`, `大喇叭`.
- Playback commands follow upstream operator permission requirements; configuration is client-only.

## Playback

- The computer, local-file playback, its recipes/assets/menu, and the old preset system are removed.
- Both players have a fixed 1024-block radius. Music player volume is fixed; each megaphone has a volume slider.
- Starting another music player stops the previous one, including any pending URL resolution.
- Megaphones share one selected station and one global switch, including across dimensions of a world.
- A client opens only one radio stream and keeps it while moving between overlapping relay coverage.
- Broadcast source and relay positions are saved in `netmusic-world.json` in the world directory.
- Registered relays remain coverage points when their chunks unload; removal updates the saved index. No chunks are force-loaded.

## Station directory

The selection screen provides 总台 → stations, 国内地区广播 → province → province/city → stations, and 国外 → stations.
Search works across names and regions.
GitHub Actions enumerates every entry in the [official 云听 directory](https://www.radio.cn/pc-portal/erji/radioStation.html).
City classification uses station titles and [published city names](https://github.com/modood/Administrative-divisions-of-China).
Stations missing from that public directory or lacking a public internet stream cannot be guaranteed.
Time-limited Chinese stream addresses are refreshed from the official directory at playback time rather than stored in the JAR.
The release includes a directory coverage report.

Foreign stations include BBC World Service, NPR, RFI, Franceinfo, Deutschlandfunk/Kultur/Nova and ABC Radio National.
Network and broadcaster regional restrictions can affect availability.

## GitHub build

All changes are committed through GitHub and all compilation/tests run in GitHub Actions; no local build is required.
The workflow generates the directory, runs playback policy tests, builds the shaded playable JAR, and uploads checksums and reports.
