# Shadow 👤

**Intentional Android Launcher & Friction Engine**

Shadow is a minimalist launcher designed for digital minimalists and productivity enthusiasts. Unlike standard launchers that make it easy to mindlessly scroll, Shadow adds **deliberate friction** to distracting apps, helping you reclaim your focus and stay intentional.

## 🚀 Version 1.0 (The Behavioral Engine)

Shadow v1 introduces the concept of **Incremental Friction**. It doesn't just block apps; it makes them harder to open as your usage increases.

### Key Features

*   **🌑 Minimalist UI:** A clean, typography-focused home screen with a global search bar. No icons, no distractions.
*   **⚖️ Dynamic Friction Engine:**
    *   **70% Rule:** Friction begins once you've used 30% of your daily limit.
    *   **Incremental Delay:** The wait time grows from 0s to 10s linearly as you approach your limit.
    *   **Launch Heat:** Rapidly attempting to open a distracting app adds a "Heat Penalty," making the delay even longer.
*   **🛡️ Active Enforcement:** A background monitor that closes apps the moment their daily limit + extension time is consumed.
*   **💎 Glass & Shadow Themes:** Beautiful, modern themes including a "Real Glass" transparent mode and a deep "Shadow" dark mode.
*   **📊 Productivity Insights:** Track how many times Shadow "intervened" to save your focus.
*   **🛠️ Extension Pool:** Out of time? Get a 5-10 minute extension, but be warned—it comes with high friction.

## 🛠 Installation & Setup

1.  **Clone the Repo:** `git clone https://github.com/smthbig/shadow.git`
2.  **Build:** Open in Android Studio and build the APK.
3.  **Permissions:**
    *   **Usage Access:** Required to track app usage and enforce limits.
    *   **Default Launcher:** Set Shadow as your default home app for the full experience.
    *   **Restricted Settings:** If on Android 13+, you may need to "Allow Restricted Settings" in App Info for Usage Access.

## 🛡 Privacy

*   **Local Only:** No data ever leaves your device. Usage tracking and limits are stored in local `SharedPreferences`.
*   **Zero Analytics:** No Firebase, no tracking, no ads. Just you and your focus.

## 🗺 Roadmap

*   [ ] **Category Limits:** Group apps (e.g., "Social Media") and set a shared limit.
*   [ ] **Focus Schedules:** Automatically increase friction during work hours.
*   [ ] **Whitelist Mode:** Completely hide distracting apps during focus sessions.
*   [ ] **App Icon Support:** Optional subtle icon support for those who need it.

---

*Made with 🖤 for a more intentional digital world.*
