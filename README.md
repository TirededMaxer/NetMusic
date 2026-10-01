# NetMusic — Minecraft 26.1.2 Fabric

基于 [TartaricAcid/NetMusic](https://github.com/TartaricAcid/NetMusic) 的 26.1-fabric 分支修改。

## 下载与安装

从 [GitHub Releases](https://github.com/TirededMaxer/NetMusic/releases) 下载 **netmusic-*-all.jar** 放入 mods 文件夹。sources.jar 是源码包。
使用 Java 25、Minecraft 26.1.2 和 Fabric Loader 0.19.2 或更新版本。客户端和服务器安装相同版本。

同时安装以下 Fabric 依赖，所列版本用于 GitHub 构建：

- [Fabric API 0.146.0+26.1.2](https://modrinth.com/mod/fabric-api/version/Jj2SOUMp)
- [Cloth Config 26.1.154](https://modrinth.com/mod/cloth-config/version/GFM8zh9J)
- [Forge Config API Port 26.1.3](https://modrinth.com/mod/forge-config-api-port/version/RXbL43Lt)

Mod Menu 可选；配置入口不依赖它。

## 指令

- /netmusic config：在客户端直接打开配置界面，无需 OP。
- /netmusic music_player on 或 off：开启或关闭唱片机。开启时选择执行者所在维度内最近的、已加载且有唱片的模组唱片机。
- /netmusic big_megaphone on 或 off：开启或关闭当前世界的全部大喇叭。
- 类型别名：jukebox、megaphone、唱片机、大喇叭。
- 播放控制指令沿用上游 OP 权限要求；单人世界需要允许指令。

## 播放规则

- 移除本地音频电脑播放器及其代码、菜单、材质、模型、合成与掉落配置。
- 唱片机和大喇叭的播放半径均固定为 1024 格。唱片机音量固定，大喇叭可调节音量。
- 同一世界最多一台模组唱片机播放；开启另一台会停止上一台及其待完成播放请求。
- 全部大喇叭共享电台和开关，各维度都受同一开关控制，每台大喇叭作为所在维度的覆盖点。
- 在重叠大喇叭范围内客户端只打开一个直播连接，在覆盖点间移动时继续播放。
- 电台与中继位置保存到世界目录的 netmusic-world.json。已登记的中继在区块卸载后仍提供覆盖，不强制加载区块；拆除中继会更新登记。
- 离开再进入唱片机覆盖范围时，客户端会跳到该歌曲当前进度。

## 选择电台

大喇叭右键界面中的“选择电台”替换了旧预设电台功能。

- 总台：选择总台广播。
- 国内地区广播：省 → 省级或省－市 → 电台。
- 国外：BBC World Service、NPR、RFI、Franceinfo、Deutschlandfunk/Kultur/Nova、ABC Radio National。
- 支持按电台名称、地区搜索。

GitHub Actions 收录[云听官方公开目录](https://www.radio.cn/pc-portal/erji/radioStation.html)返回的全部条目，并补充已核实的天津滨海之声、滨海音乐及滨海文艺。
市级分类依据电台名称匹配[行政区划城市名称](https://github.com/modood/Administrative-divisions-of-China)；未识别城市的条目归入省级分组。
官方目录未公开的电台，以及没有公开网络直播源的电台，不能保证完整收录。目录中的短期签名播放地址会在播放时重新解析。
Release 附带 radio-directory-report.json，记录目录数量、补充来源与核实日期。

支持 MP3、ADTS AAC、MPEG-TS 音频 HLS 以及多级 HLS 播放列表。广播机构的网络限制、停播与临时断流会影响可用性。

## 构建与验证

所有源码提交、编译与测试均通过 GitHub 完成，本地不构建。
GitHub Actions 生成电台目录，运行播放策略与 HLS 解析测试，检查正式 JAR 的实际广播音频解码，并上传模组、SHA-256 校验值和报告。
音频报告中的 network_unavailable 表示该直播源在 GitHub runner 所在网络无法访问，和解码错误分别记录。
这些检查不替代 Minecraft 客户端内的界面、联机和移动覆盖实测。
