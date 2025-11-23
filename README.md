# SetuCompose - 现代化的随机涩图 App

由Gemini v3辅助开发，DEBUG

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-green.svg)
![API](https://img.shields.io/badge/API-Lolicon.app-pink.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

**SetuCompose** 是一个基于 Google 最新 **Jetpack Compose** 框架开发的 Android 应用程序。它调用 Lolicon API，提供流畅、美观的二次元插画浏览、搜索、保存及分享体验。

本项目旨在演示现代 Android 开发架构（MVVM + Compose + Coroutines + Retrofit）。

## ✨ 功能特性 (Features)

*   **高度可配置的搜索**
    *   支持 R18 / 非 R18 / 混合模式切换。
    *   支持排除 AI 生成的作品。
    *   支持 Tag 关键词搜索（支持正则匹配，如 `白丝|黑丝`）。
    *   自定义单次获取数量（1-20张）。

*   **极致的浏览体验**
    *   **列表页**：优先加载极小缩略图（Thumb），极大节省流量并提升加载速度。
    *   **详情页**：自动加载高清原图（Original），并带有加载中/失败的状态提示。
    *   **沉浸式查看**：无标题栏设计，支持**双指缩放**、**拖拽查看**细节。

*   **交互与动画**
    *   **上滑面板**：使用 `BottomSheetScaffold`，上滑查看完整的作品信息（PID、UID、分辨率、Tags等），不遮挡画面。
    *   **手势协同**：未缩放时上滑展开详情，缩放时拖拽图片，交互逻辑自然流畅。

*   **实用工具**
    *   **本地保存**：支持 Android 10+ Scoped Storage，无需权限直接保存至相册（旧版本自动申请权限）。
    *   **一键分享**：通过 `FileProvider` 直接分享图片文件到微信、QQ 等应用。

## 🛠 技术栈 (Tech Stack)

*   **语言**: [Kotlin](https://kotlinlang.org/)
*   **UI 框架**: [Jetpack Compose](https://developer.android.com/jetbrains/compose) (Material3)
*   **架构模式**: MVVM (Model-View-ViewModel)
*   **网络请求**: [Retrofit2](https://square.github.io/retrofit/) + [Gson](https://github.com/google/gson)
*   **图片加载**: [Coil](https://coil-kt.github.io/coil/) (支持 SubcomposeAsyncImage 加载状态监听)
*   **异步处理**: Kotlin Coroutines + Flow
*   **导航**: Navigation Compose
*   **构建工具**: Gradle Kotlin DSL (KTS)

## 📸 截图展示 (Screenshots)

| 配置页面 | 结果列表 | 沉浸详情 | 上滑信息 |
|:---:|:---:|:---:|:---:|
| <img src="screenshots/config.jpg" width="200"/> | <img src="screenshots/list.jpg" width="200"/> | <img src="screenshots/detail.jpg" width="200"/> | <img src="screenshots/sheet.jpg" width="200"/> |

*(注：请将您的应用截图放入 `screenshots` 文件夹并替换上述文件名)*

## 🚀 快速开始 (Getting Started)

### 环境要求
*   Android Studio Hedgehog | 2023.1.1 或更高版本
*   JDK 17+
*   Android SDK API 34 (compileSdk) / API 21 (minSdk)

### 克隆与构建
```bash
# 1. 克隆仓库
git clone https://github.com/your_username/SetuCompose.git

# 2. 打开 Android Studio，导入项目

# 3. 等待 Gradle Sync 完成

# 4. 运行 App (连接真机或模拟器)
```

## 📦 项目结构

```text
com.example.setucompose
├── api             # 网络层 (Retrofit Interface, Data Models)
├── model           # 应用状态模型
├── ui              
│   ├── screens     # Compose 页面 (Config, Result, Detail, About)
│   ├── theme       # Material3 主题配置
│   ├── MainActivity.kt
│   └── SetuViewModel.kt # 核心业务逻辑
└── util            # 工具类 (ImageSaver)
```

## 🤝 API 说明

本应用使用了 **Lolicon API (Setu API v2)**。
*   **API 文档**: [https://docs.api.lolicon.app/#/](https://docs.api.lolicon.app/#/)
*   **API 地址**: `https://api.lolicon.app/setu/v2`

*请注意：该 API 由第三方提供，本应用仅作为客户端进行数据展示。*

## ⚠️ 免责声明 (Disclaimer)

1.  本应用仅供编程学习和技术交流使用。
2.  应用内展示的所有图片资源均来源于第三方 API，开发者不对图片内容负责。
3.  请遵守当地法律法规，合理使用本应用。
4.  部分内容可能涉及 R18，请确保您已年满 18 岁。

## 📄 License

```
MIT License

Copyright (c) 2024 [Your Name]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction...
```
```

### 使用建议：
1.  **截图文件夹**：在你的项目根目录下建一个 `screenshots` 文件夹，用模拟器或真机截几张图放进去，重命名为 `config.jpg`, `list.jpg` 等，这样 GitHub 页面会非常好看。
2.  **上传 GitHub**：把整个项目上传到 GitHub，这份 README 会自动显示在主页。
