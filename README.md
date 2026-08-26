# T9Flow

> 一个极简、快速、**完全离线**的 Android T9 应用启动器。

通过**中文首字母、完整拼音、英文名、T9 数字键**快速搜索并一键启动手机里已安装的应用。不需要网络、不需要登录、不收集任何数据。

## ✨ 特性

- 🔍 多方式搜索：中文名、拼音首字母、完整拼音、英文名、T9 数字
- 🈶 词语级多音字识别：`招商银行 → zhao shang yin hang`（不会误判成 `yin xing`）
- 🧩 首字母 / 拼音 / T9 均支持**子串模糊匹配**：输入 `jsyh` 或 `5794` 也能找到「中国建设银行」
- ⌨️ T9 数字键盘：输入 `247663` 直接找到 Chrome
- 📌 长按固定 / 取消固定常用应用（独立固定区）
- 🕒 最近打开 + 使用次数统计（本地持久化）
- ⚡ 打开应用即实时扫描，**新安装的 App 自动加载**，无需手动刷新
- 🌙 深色模式（跟随系统 / 浅色 / 深色）
- 📴 完全离线，无任何网络权限，数据全部本地存储

## 📱 使用

打开应用即见搜索框和 T9 键盘，直接输入即可实时搜索：

| 输入 | 示例 | 匹配结果 |
|---|---|---|
| 中文名 | `招商银行` | 招商银行 |
| 首字母 | `zsyh` | 招商银行 |
| 首字母子串 | `jsyh` | 中国建设银行 |
| 完整拼音 | `zhaoshangyinhang` | 招商银行 |
| 英文名 | `chrome` | Chrome |
| T9 数字 | `247663` | Chrome |
| T9 首字母数字 | `5794` | 建设银行 / 中国建设银行 |

- **点击**应用卡片 → 启动应用
- **长按**应用卡片 → 固定 / 取消固定
- **点击设置齿轮** → 最近打开数量、深色模式、清空记录、重新扫描

## 🛠 技术栈

- Kotlin + Jetpack Compose + Material 3
- Room（本地数据库）
- Kotlin Coroutines + Flow
- DataStore Preferences
- [TinyPinyin](https://github.com/promeG/TinyPinyin)（拼音 + 自定义多音字词典）
- Gradle Kotlin DSL + Version Catalog

## 📁 项目结构

```
app/src/main/java/com/t9launcher/
├── data/        # Room 实体 / DAO / 数据库 / 仓库
├── pinyin/      # 拼音转换 + 词语级多音字词典
├── search/      # T9 映射 + 搜索引擎 + 排序
├── launcher/    # 应用扫描 + 启动
├── settings/    # 设置持久化（DataStore）
└── ui/          # Compose 界面
```

## 🔨 构建

- `minSdk 26` / `targetSdk 36`
- 用 Android Studio 直接打开运行，或命令行：

```bash
./gradlew assembleDebug      # 调试版
./gradlew assembleRelease    # 正式版（R8 裁剪 + 签名）
```

> 正式版构建需要 `keystore/release.jks`，请自行生成并配置到 `app/build.gradle.kts` 的 `signingConfigs`。

## 📦 下载

见 [Releases](../../releases) 页面，下载 `app-release.apk` 直接安装。

## 📄 许可证

本项目采用 [MIT License](LICENSE) 开源，可自由使用、修改、商用，但需保留版权声明。

## ⚖️ 第三方开源库

本项目使用以下第三方库，各自遵循其许可证：

| 库 | 用途 | 许可证 |
|---|---|---|
| [TinyPinyin](https://github.com/promeG/TinyPinyin) | 汉字转拼音 | [Apache-2.0](https://github.com/promeG/TinyPinyin/blob/master/LICENSE) |
| 其他（AndroidX / Jetpack Compose / Room 等） | JetBrains / Google 官方库 | 各自许可证 |

> 上述库的版权归各自原作者所有。

## 🔒 隐私与数据

- 本应用**不需要任何网络权限**（`INTERNET` 等均未声明），完全离线运行。
- 所有数据（应用索引、最近打开记录、固定状态、设置）仅保存在设备本地，**不会上传或共享**。
- 应用扫描仅通过 Android 系统 `PackageManager` 读取本地已安装应用列表，用于搜索与启动，不收集任何个人信息。

## ⚠️ 免责声明

本软件按「现状」提供，作者不对其适用性、可靠性或准确性作任何明示或暗示的保证。使用本软件产生的任何直接或间接损失，作者不承担责任。

各可启动应用及其图标、名称的商标与版权归其各自权利人所有。
