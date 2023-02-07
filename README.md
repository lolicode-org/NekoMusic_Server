# AllMusic Server

适用于Fabric服务端的AllMusic Mod，为Fabric服务器提供全服点歌功能。

本项目使用了 [Coloryr](https://github.com/Coloryr) 的 [AllMusic_M](https://github.com/Coloryr/AllMusic_M) 作为配套客户端，但**并不是**其服务端的Fabric移植。

## 使用

### 服务端

1. 请确保已安装 [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) 和 [Fabric Language Kotlin](https://www.curseforge.com/minecraft/mc-mods/fabric-language-kotlin)。
2. 从 [Releases](https://github.com/lolicode-org/AllMusic_Server/releases) 下载最新版本的服务端Mod，放入服务端的 `mods` 目录中。
3. 启动服务端，服务端会在 `config` 目录下生成 `allmusic.json` 配置文件，根据需要修改配置文件。
```json5
{
  "cookie": "",  // 网易云音乐的cookie，用于获取音乐信息
  "idle_list": 0,  // 空闲列表的歌单ID
  "api_address": "http://127.0.0.1:3000",  // 网易云Nodejs api的地址
  "vote_threshold": 0.5,  // 投票切歌所需的人数百分比
  "max_quality": 320000  // 最大音质，默认为320k，如需无损或Hi-res音质，请修改为999000
}
```
4. 重启服务端即可正常使用。

### 客户端

下载对应版本的[AllMusic_M](https://github.com/Colyyr/AllMusic_M/releases)，按照正常的Mod安装方法安装即可。

**注意：** 高版本（大概是1.19+）的客户端Mod似乎存在严重的Bug [1](https://github.com/Coloryr/AllMusic_M/issues/16) [2](https://github.com/Coloryr/AllMusic_M/issues/17)，如果你使用原版客户端发现了卡死的情况，可以尝试使用我的[修复版](https://github.com/lolicode-org/AllMusic_M/releases)。

### 命令

| 命令                    | 权限                      | 用途                                 |
|:----------------------|:------------------------|:-----------------------------------|
| `/music`              | `allmusic`              | 命令根节点。后面可以直接加歌曲ID或者链接来快速点歌（需要点歌权限） |
| `/music add`          | `allmusic.add`          | 点歌，后面可以加歌曲ID或者链接                   |
| `/music list`         | `allmusic.list`         | 查看当前播放列表                           |
| `/music vote`         | `allmusic.vote`         | 投票切歌                               |
| `/music next`         | `allmusic.next`         | 强制切歌                               |
| `/music del`          | `allmusic.del`          | 删除当前播放列表中的歌曲                       |
| `/music del`          | `allmusic.del.others`   | 删除别人点的歌曲                           |
| `/music reload`       | `allmusic.reload`       | 重新加载配置文件                           |
| `/music login`        | `allmusic.login`        | 登陆命令根节点                            |
| `/music login start`  | `allmusic.login.start`  | 开始登陆，会在**控制台**打印登陆二维码，**在聊天栏无法执行** |
| `/music login check`  | `allmusic.login.check`  | 检查登陆状态，如果登陆成功，会将cookie写入配置文件       |
| `/music login status` | `allmusic.login.status` | 显示当前登陆的用户信息                        |

### API

本项目并不直接与网易的API交互，而是通过 [NeteaseCloudMusicApi](https://github.com/Binaryify/NeteaseCloudMusicApi) 来获取音乐信息。此举是为了降低网易后续可能的限制带来的影响。

关于本API的使用方法，请参考其[文档](https://binaryify.github.io/NeteaseCloudMusicApi/#/)。

如果API因异常而频繁退出，可以使用 [PM2](https://pm2.keymetrics.io/) 来保证持续运行，具体请参考官方文档。

此外在Linux上可以使用 `systemd` 来实现自动重启。参考以下配置文件：
```ini
[Unit]
Description=NeteaseCloudMusicApi
After=network.target
Requires=network.target

[Service]
Type=simple
# 改成你的服务端运行的用户
User=minecraft
# 改成你的API所在的目录
WorkingDirectory=/home/minecraft/NeteaseCloudMusicApi
ExecStart=/usr/bin/node app.js
Restart=always
RestartSec=5s
PrivateTmp=true
ProtectSystem=full
NoNewPrivileges=true
PrivateDevices=true
ProtectKernelTunables=true
ProtectKernelModules=true
ProtectControlGroups=true
RestrictSUIDSGID=true

[Install]
WantedBy=multi-user.target
```
将以上内容保存为 `/etc/systemd/system/NeteaseCloudMusicApi.service` ，然后以root用户执行 `systemctl enable --now NeteaseCloudMusicApi` 即可。

对于Windows Server，可以使用系统的 [计划任务](https://docs.microsoft.com/zh-cn/windows/win32/taskschd/task-scheduler-start-page) 来实现开机自启和自动重启。

对于没有ssh权限的面板服，可以参考官方文档的Serverless部署方式在公网部署API，此处不再赘述。

### Cookie

本Mod并不要求填写您的帐号密码，这一方面是为了保护您的隐私，另一方面是因为网易的登录接口已经添加了验证码，大部分情况下使用帐号密码会登陆失败。

Cookie是可选的，如果不填写，会导致无法获取更高音质或者会员歌曲，同时对歌单的访问可能会受到限制。

**注意：** 
1. 请不要将您的Cookie泄露给他人，否则会导致您的帐号被盗。**不建议**在没有实际控制权的服务器（如面板服）上登陆账号。
2. 后续网易**可能**采取针对共享VIP帐号的限制措施，开发者不对您因使用本Mod而导致的任何损失负责，使用时请确保符合当地法律法规。

获取Cookie:
1. 登陆服务器控制台
2. 输入`/music login start`，扫描生成的二维码
> 注意：如果生成的二维码长宽比不一致导致无法被网易云客户端扫描，可以换个终端模拟器再试。您也可以拍照/截图后使用图片编辑工具拉伸后再扫描。
3. 扫描并授权后，输入`/music login check`，控制台会输出结果。如果成功，Cookie会被自动保存在配置文件中

## 问题反馈

请使用本项目的Issue页面进行反馈，如果您有更好的建议或者想法，也欢迎在Issue中提出。由于时间以及水平所限，我可能并不会实现很多功能，如有需要，可以自行Fork后修改。

## 致谢

[NeteaseCloudMusicApi](https://github.com/Binaryify/NeteaseCloudMusicApi)：网易云音乐API

[AllMusic_M](https://github.com/Coloryr/AllMusic_M)：客户端以及部分参考

## 许可证
```text
Copyright (c) 2023 KoishiMoe

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.
```
