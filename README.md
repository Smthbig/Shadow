# Shadow

> A minimal Android launcher designed to boost focus and productivity by eliminating distractions through smart search and intelligent app blocking.

[![Android](https://img.shields.io/badge/Platform-Android-green?style=flat-square)](https://www.android.com/)
[![Java](https://img.shields.io/badge/Language-Java-orange?style=flat-square)](https://www.java.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/Smthbig/Shadow?style=flat-square)](https://github.com/Smthbig/Shadow/stargazers)

## Overview

Shadow is a next-generation Android launcher that reimagines how you interact with your device. By combining powerful search capabilities with intelligent distraction blocking, Shadow helps you stay focused on what matters most.

Whether you're looking to minimize digital distractions, reclaim your attention, or simply enjoy a cleaner mobile experience, Shadow is your answer.

## ✨ Features

- **🔍 Intelligent Search** - Find apps and content instantly without endless scrolling
- **🚫 Smart Blocking** - Block distracting apps and delay access to reduce digital procrastination
- **📱 Minimal Design** - Clean, distraction-free interface that puts focus first
- **⚡ Lightweight** - Optimized performance with minimal resource usage
- **🎯 Focus Mode** - Concentrate on your goals with built-in app restrictions
- **🎨 Customizable** - Personalize your launcher experience to match your workflow

## 🚀 Getting Started

### Prerequisites

- Android 6.0 (API 23) or higher
- 50 MB free storage space

### Installation

1. Clone the repository
```bash
git clone https://github.com/Smthbig/Shadow.git
cd Shadow
```

2. Open in Android Studio and build
```bash
./gradlew build
```

3. Run the app
```bash
./gradlew installDebug
```

## 💡 How It Works

Shadow operates on a simple philosophy: **less is more**. 

- **Search-First Approach** - Begin with a powerful search bar to find exactly what you need
- **Distraction Detection** - Identify and block time-wasting apps automatically
- **Smart Delays** - Set cooling-off periods before accessing blocked applications
- **Focus Sessions** - Create dedicated time blocks for deep work

## 📚 Usage Examples

### Enable Focus Mode
```java
// Activate focus mode with selected apps blocked
launcher.enableFocusMode(blockedApps, durationMinutes);
```

### Search Apps
```java
// Quick search across all installed applications
List<App> results = launcher.search("keyword");
```

### Set App Delay
```java
// Delay access to distracting apps
launcher.setAppDelay(appName, delaySeconds);
```

## 🏗️ Project Structure

```
Shadow/
├── src/
│   ├── main/
│   │   ├── java/          # Java source code
│   │   ├── res/           # Resources (layouts, strings, drawables)
│   │   └── AndroidManifest.xml
│   └── test/              # Unit tests
├── gradle/                # Gradle wrapper
├── build.gradle           # Build configuration
└── README.md              # This file
```

## 🤝 Contributing

We welcome contributions! Whether it's bug reports, feature requests, or code improvements, your help makes Shadow better.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 🐛 Issue Reporting

Found a bug? Please open an [issue](https://github.com/Smthbig/Shadow/issues) with:
- Clear description of the problem
- Steps to reproduce
- Expected vs actual behavior
- Device info and Android version

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Android community for inspiration and guidance
- Contributors who help improve Shadow
- Users providing valuable feedback

## 📞 Contact & Support

- 📧 Create an issue on [GitHub](https://github.com/Smthbig/Shadow/issues)
- 💬 Discussions available in [GitHub Discussions](https://github.com/Smthbig/Shadow/discussions)

---

**Made with ❤️ by [Smthbig](https://github.com/Smthbig)**

*Shadow: Focus on what matters.*
