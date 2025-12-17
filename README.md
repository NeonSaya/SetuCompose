## 💖 SetuCompose ✨

> 📢 **偷偷告诉你：这是我和我身边的 Gemini v3 小伙伴一起捣鼓出来的！**
> 
> 
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-green.svg)
![API](https://img.shields.io/badge/API-Lolicon.app-pink.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

-----

**SetuCompose** 呀，它就是我用 **Jetpack Compose** 框架，一点点堆出来的 **Android 小应用**。

我调用了 **Lolicon API**，就是想给你一个 **超流畅、超好看、超舒服** 的方式，来浏览、搜索、保存和分享那些精美的二次元插画！

✨ **这个项目对我来说**：它不仅是个App，还是我学习和实践 **现代 Android 开发技术** 的**小成果展示**。如果你也对新技术感兴趣，欢迎来看看我的代码是怎么搭积木的！

-----

## 🌟 宝藏功能清单 (Features List)

### ❤️ 收藏夹管理大师 (新功能！)

  * **多选模式**：在收藏页面，**轻轻长按** 任意一张图片，就能进入多选模式，对您的宝贝收藏进行批量操作！
  * **批量取消收藏**：手滑点错了？或者只是想整理一下？现在可以一次性选择多张图片，将它们批量移出收藏。
  * **批量保存**：看到一堆神仙图想立刻保存到本地？没问题！在多选模式下，一键即可将所有选中的图片保存到您的相册。
  * **删除二次确认**：为了防止您心爱的小图片被误删，我们特地增加了一个**二次确认**对话框。给您一个反悔的机会，操作更安心！

### 🔍 帮你找到最爱的那张图

  * **安全模式随心换**：想看 R18？想看纯净的？还是想看混合的？**一键切换**，超级方便！
  * **不看 AI 画的？**：可以！我们支持 **排除 AI 生成的作品**，让你专注于画师们的心血。
  * **精准 Tag 搜索**：支持关键词，甚至连 **正则匹配** 这种有点酷的功能都有哦（比如输入 `$白丝|黑丝$` 就能同时搜到白丝和黑丝！）
  * **一次拿几张**：想要多一点还是少一点？你可以自己定，**1 到 20 张** 随你挑。

### 🖼️ 就像看画展一样舒服

  * **加载飞快**：列表页会先加载**超小的缩略图**，这样**流量省了**，**速度也快了**，体验超棒！
  * **大图细节不放过**：点进去后，App 会自动加载 **高清的原图**。加载和失败时，都会有贴心的小提示。
  * **沉浸感拉满**：界面上**没有多余的标题栏**，整张图都在眼前！你可以自由地 **双指缩放**、**拖拽**，把图上的每一个细节都看清楚。

### 🪄 交互的小心机

  * **信息面板优雅弹出**：作品信息（PID、UID、Tag 等）会从底部**轻轻滑上来**，**不会遮挡画面**。
  * **手势超智能**：当你没有缩放图片时，**上滑** 是展开详情；当你放大图片后，**拖拽** 就是移动图片。它知道你在想什么！

### 🛠️ 实用小工具

  * **无感保存**：支持 **新版安卓的存储机制**，**不用申请权限** 就能把图存到相册！（老版本？它会自己去申请权限的，不用你操心。）
  * **分享超轻松**：你可以直接把图片文件**分享**给你的微信、QQ 好友。

-----

## 💻 我用了哪些“积木” (Tech Stack)

  * **编程语言**：[Kotlin](https://kotlinlang.org/)（现代 Android 必备！）
  * **界面框架**：[Jetpack Compose](https://developer.android.com/jetbrains/compose) (Material3)（用它写 UI，快乐加倍！）
  * **核心架构**：MVVM（让我的代码井井有条！）
  * **网络通讯**：[Retrofit2](https://square.github.io/retrofit/) + Gson
  * **图片加载**：[Coil](https://coil-kt.github.io/coil/)（专为 Kotlin 和 Compose 设计，好用！）
  * **异步操作**：Kotlin Coroutines + Flow（让复杂的任务变得简单可靠！）

-----

## 🚀 想要自己跑起来？ (Getting Started)

### 🏡 需要准备这些

  * **软件**：Android Studio Hedgehog | 2023.1.1 或更新的版本。
  * **环境**：JDK 17+
  * **系统要求**：Android SDK API 34 (compileSdk) / API 21 (minSdk)

### 🏃‍♀️ 跑起来很简单

```bash
# 1. 把我的小项目克隆到你的电脑上
git clone https://github.com/your_username/SetuCompose.git

# 2. 打开 Android Studio，然后导入这个项目

# 3. 等待 Gradle 同步（很快的！）

# 4. 连接你的手机或者打开模拟器，点击运行 (Run) 按钮！
```

-----

## 📦 项目结构是这样的

```text
com.example.setucompose
├── api             # 💌 处理网络请求和数据格式
├── model           # 💖 各种数据模型
├── ui              
│   ├── screens     # 🖥️ 所有的 App 页面都在这里
│   ├── theme       # 🎨 App 的主题颜色和样式
│   ├── MainActivity.kt # 🏠 App 的入口
│   └── SetuViewModel.kt # 🧠 App 的“大脑”，负责处理业务逻辑
└── util            # 🛠️ 各种工具，比如图片保存
```

-----

## 🤝 Lolicon API

这个 App 的图片调用都来自于 **Lolicon API (Setu API v2)**。

  * **API 文档在这里**：[https://docs.api.lolicon.app/\#/](https://docs.api.lolicon.app/#/)
  * **API 地址**：[https://api.lolicon.app/setu/v2/](https://api.lolicon.app/setu/v2)

>  **声明：** 这个 API 是第三方提供的，我只是个把它展示给大家看的**小工具**哦！

-----

## ⚠️ 一些小提醒 (Disclaimer)

1.  请记住，我做这个 App **只是为了学习和交流编程技术**！
2.  App 里的所有图片都来源于Pixiv，我**不为图片的内容负责**。
3.  请一定要 **遵守你当地的法律法规**，合理地使用它！
4.  如果你未满 18 岁，请不要使用涉及 R18 的相关功能哦。
