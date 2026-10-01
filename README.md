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

## 指令与配置

仅保留 /netmusic，输入后打开三个按钮的客户端菜单：配置、唱片机开关、大喇叭开关。旧子指令全部移除。
开关通过服务器消息执行，不要求 OP。单人和多人世界都需要安装本版本；服务器不匹配时仍可打开客户端配置，但开关会禁用。
唱片机开启时选择执行者所在维度内最近的、已加载且有唱片的唱片机。
配置仅包含唱片机上方歌词、ActionBar 歌词和原/译歌词颜色；女仆、代理和立体声相关代码、配置、文字与资源已移除。
ActionBar 和方块上方歌词可独立开启；ActionBar 使用配置中的原/译歌词颜色。

## 播放规则

- 移除本地音频电脑播放器及其代码、菜单、材质、模型、合成与掉落配置。
- 唱片机和大喇叭的播放半径均固定为 1024 格。两种播放器都固定音量，按距离线性衰减：增益 = max(0, 1 − 距离/1024)。所有输出转成单声道。
- 同一世界最多一台模组唱片机播放；开启另一台会停止上一台及其待完成播放请求。
- 全部大喇叭共享电台和开关，各维度都受同一开关控制，每台大喇叭作为所在维度的覆盖点。
- 在重叠大喇叭范围内取最近中继的最大增益，不累加音量。每个客户端只打开一条广播流，换中继时保留同一个声音实例；不同客户端的网络直播缓冲延迟可能不同。
- 大喇叭右键界面仅保留开始、停止、选择电台以及广播地址/名称；不再有保存或音量控件。点击开始时应用所选电台。
- 两种播放器都保留红石上升沿切换。大喇叭的红石脉冲切换世界中的全部大喇叭；信号下降沿不重复切换。唱片机保持比较器输出：无唱片为 0、有唱片停止为 7、播放为 15。
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
官方目录未公开的电台，以及没有公开网络直播源的电台，不能保证完整收录。目录中的短期签名播放地址会在播放时重新解析；打开失败时依次尝试该台其他官方候选地址。
Release 附带 radio-directory-report.json，记录目录数量、补充来源与核实日期。

支持 MP3、ADTS AAC、MPEG-TS 音频 HLS 以及多级 HLS 播放列表。广播机构的网络限制、停播与临时断流会影响可用性。

## 构建与验证

所有源码提交、编译与测试均通过 GitHub 完成，本地不构建。
GitHub Actions 生成电台目录，运行播放策略、歌词时间点、HLS 解析及界面/语言检查，检查全部总台广播的单声道解码，并用正式 JAR 的 HLS 处理器实际连接中国之声及音乐之声。
音频报告中的 network_unavailable 表示该直播源在 GitHub runner 所在网络无法访问，和解码错误分别记录。
这些检查不替代 Minecraft 客户端内的界面、联机和移动覆盖实测。
