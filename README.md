<img width="1280" height="640" alt="ThondiEngotta Banner" src="https://github.com/user-attachments/assets/8920b256-2ba8-4988-b824-5351134eb4bd" />

# ThondiEngotta 📱💀

> **Thondi thondi irikkathe vello panikkum pokkude?**

A completely unnecessary Android app that measures **how far your thumb travels while scrolling through your phone** and then makes fun of you for it. 😂

---

## 🎯 Basic Details

### Team Name:
Leo

### Team Members

- Joel Joy - St. Joseph's College of Engineering & Technology, Palai
- Karthik Krishnan - St. Joseph's College of Engineering & Technology, Palai

---

## 📱 Project Description

**ThondiEngotta** is a gamified Android app that tracks the physical distance travelled by your thumb while scrolling through different apps.

Instead of showing boring screen-time statistics like:

> "You spent 6 hours on your phone."

ThondiEngotta tells you:

> **"Bro... your thumb travelled 8.7 KM today. 💀"**

The more you scroll, the more achievements you unlock — and the achievements are basically Malayalam/Manglish roasts judging your life choices. 😂

---

## 🤡 The Problem (that doesn't exist)

We already have:

- Step counters 🚶
- Screen-time trackers 📱
- Fitness trackers 🏃
- Calorie trackers 🍔

But there is one extremely important metric nobody tracks:

> **How many kilometres does your thumb travel while scrolling?**

Imagine scrolling Instagram, YouTube, Reddit, WhatsApp and other apps all day.

Your thumb could travel kilometres.

Nobody knows.

Nobody asked.

So we decided to solve this completely unnecessary problem.

---

## 💀 The Solution (that nobody asked for)

**ThondiEngotta** turns your scrolling into a virtual journey.

The app:

1. Detects scrolling activity across apps.
2. Estimates the physical distance travelled by your thumb.
3. Converts the distance into kilometres.
4. Tracks your scrolling statistics.
5. Gives you XP and levels.
6. Unlocks achievements at different distance milestones.
7. And most importantly...

### ROASTS YOU. 😂

For example:

At **0.2 KM**:

> 🏆 **FIRST ROAST**
>
> "Thondi thondi irikkathe vello panikkum pokkude? 😂"

At **1 KM**:

> 🏆 **THUMB WALKER**
>
> "1 KM aayi... nee nadannirunnenkil fitness aayene! 💀"

At **10 KM**:

> 🏆 **SCROLL MONSTER**
>
> "10 KM! Purathottu nokkeda mone, lokam avideyum undu. 💀"

The further you scroll...

**The worse the roast gets. 💀**

---

# 🛠️ Technical Details

## Technologies / Components Used

### For Software:

- **Kotlin**
- **Jetpack Compose**
- **Android AccessibilityService**
- **Android SDK**
- **Material 3**
- **Kotlin Coroutines**
- **ViewModel**
- **StateFlow**
- **Git & GitHub**
- **Android Studio**

### Core Technologies:

- AccessibilityService
- Scroll Event Processing
- Hybrid Distance Calculation
- Gamification Engine
- Achievement System
- In-memory Data Repository
- Neo-Brutalist UI

---

## 🧠 How It Works

ThondiEngotta uses Android's `AccessibilityService` to detect scroll events from different applications.

The app does **NOT read the content displayed on the screen**.

Instead, it processes numerical scroll information such as:

- Scroll deltas
- Scroll positions
- Scroll indices
- Event timestamps
- Source/application information where applicable

These events are processed and converted into an estimated physical scrolling distance.

### Basic Pipeline

```text
        User Scrolls
             │
             ▼
    Android Accessibility
         Service
             │
             ▼
    Scroll Event Processor
             │
             ▼
   Hybrid Distance Calculator
             │
             ▼
      Distance in KM
             │
       ┌─────┴─────┐
       ▼           ▼
    Statistics   Gamification
                   │
              ┌────┴─────┐
              ▼          ▼
             XP      Achievements
                         │
                         ▼
                   Malayalam Roast
