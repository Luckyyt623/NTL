# 🐍 SlitherMixMod
**Slither.io × NTL Mod v9.18 × Mobile Controls**

A custom Android app that combines the full NTL Chrome Extension mod with a native mobile joystick/arrow system — inspired by the original mobile slither SWF.

---

## ✨ Features

### NTL Mod Features (from NTL Chrome Extension v9.18)
- 🤖 Bot autopilot system
- 💀 Kill counter & score overlay
- 🎵 Sound effects (beep, SOS, chat)
- 📸 Auto screenshot on kills/death
- 🎨 60 skin patterns + 34 accessories
- 🔧 Advanced settings panel
- 🌐 Server selector
- 📊 Stats tracking

### Mobile-Exclusive Features (new in this mod)
- 🕹️ **Native joystick** — drag to steer (matches SWF arrow system)
- 4 directional arrow indicators on joystick base
- ⚡ **BOOST button** — one-thumb boost
- 🎨 **Skin dialog** — hue sliders + pattern grid + accessory picker
- 🔲 **Fullscreen landscape** — no browser chrome, immersive play
- 🔁 Random skin generator

---

## 📦 Build Instructions

### Requirements
- **Android Studio** (Hedgehog 2023.1.1 or newer) — free at developer.android.com
- **JDK 17+** (bundled with Android Studio)
- Internet connection (to download Gradle and dependencies on first build)

### Steps

1. **Open the project**
   - Launch Android Studio → `File → Open` → select this folder

2. **Let Gradle sync** (automatic, ~2-5 min on first run)
   - Android Studio downloads Gradle 8.4 and the Android SDK automatically

3. **Build the APK**
   - **Debug APK** (easiest — for sideloading):
     - `Build → Build Bundle(s)/APK(s) → Build APK(s)`
     - APK saved to: `app/build/outputs/apk/debug/app-debug.apk`
   - **Release APK** (for sharing):
     - `Build → Generate Signed Bundle/APK → APK`
     - Create or use an existing keystore

4. **Install on your phone**
   - Enable "Install from unknown sources" in phone settings
   - Transfer the APK and tap to install
   - **OR** connect phone via USB → `Run → Run 'app'` in Android Studio

---

## 🎮 How to Play

| Control | Action |
|---------|--------|
| Drag **joystick** (left) | Steer your snake |
| Hold **⚡ BOOST** (right) | Speed boost |
| Tap **🎨** (top right) | Open skin customizer |
| Tap **⚙️** (top right) | Settings / reload |
| NTL mod panel | Accessible via ⚙️ → NTL Mod Panel |

---

## 📁 Project Structure

```
SlitherMixMod/
├── app/src/main/
│   ├── java/com/slithermix/ntl/
│   │   ├── MainActivity.java      ← WebView setup, control wiring
│   │   ├── JoystickView.java      ← Custom joystick (arrow system)
│   │   ├── BoostButton.java       ← Custom boost button
│   │   └── SkinDialog.java        ← Skin customizer UI
│   ├── assets/
│   │   ├── mobile_inject.js       ← Chrome API bridge + JS controls
│   │   ├── main-mt.js             ← NTL Mod core (8.6MB)
│   │   ├── jquery-2.2.4.min.js   ← jQuery (required by NTL)
│   │   ├── bootstrap.css          ← Bootstrap (required by NTL)
│   │   ├── s/                     ← Skin pattern images (60 skins)
│   │   └── pr/                    ← Profile images
│   └── res/
│       ├── layout/activity_main.xml
│       └── layout/dialog_skin.xml
```

---

## 🔧 Customization

### Add your own skins
Put `.webp` images in `app/src/main/assets/s/` named `t_60.webp`, `t_61.webp`, etc.  
Update `PATTERN_COUNT` in `SkinDialog.java` to match.

### Change default server
In `MainActivity.java`, change:
```java
webView.loadUrl("https://slither.io");
```

### Adjust joystick sensitivity
In `mobile_inject.js`, change the `SCALE` value:
```javascript
var SCALE = 4.5; // higher = more sensitive
```

### Adjust joystick size
In `activity_main.xml`, change `layout_width/height` of `JoystickView`.

---

## ⚠️ Notes

- Requires internet connection to play (slither.io is an online game)
- NTL mod's screenshot feature is disabled on mobile (Chrome extension API)  
- The SWF arrow design is reimplemented natively in `JoystickView.java`
- Flash (SWF) cannot run on modern Android; controls are rebuilt from scratch

---

## Credits
- **NTL Mod** © [NTL] Nothing To Lose — ntl-slither.com
- **slither.io** © Lowtech Studios
- **Mobile integration & joystick** — SlitherMixMod project
