# Quiz-App

A single-screen Quiz App built with Jetpack Compose and Material3.

## Description

This app demonstrates a clean MVVM architecture with state management via ViewModel. It displays one quiz question at a time (True/False, Single Choice, Multi Choice, Text Entry) and handles configuration changes (rotation, backgrounding) safely. The "Next" button is enabled only after an answer is selected, and on the final question, it changes to "Submit" which leads to a thank-you screen with a retry option. The UI is padded to avoid camera cutouts/status bars.

## Features

- **Multiple Question Types:**  
  - True/False (with light colors that change to dark when selected)  
  - Single Choice (radio buttons)  
  - Multi Choice (checkboxes)  
  - Text Entry (freeform input)

- **State Management:**  
  - Answers are stored in a ViewModel for persistence across configuration changes.
  
- **User Flow:**  
  - "Next" button remains disabled until an answer is selected.
  - On the final question, "Submit" is shown; after submission, a thank-you screen with a "Retry" option is displayed.

- **Safe Display:**  
  - The layout is padded using window insets so that content does not cover the camera cutout/status bar.

## Getting Started

1. **Clone the Repository:**  
   `git clone https://github.com/RamNikhilRN/QuizApp_Cricut

2. **Open in Android Studio:**  
   Open the project using Android Studio.

3. **Sync Gradle:**  
   Make sure all dependencies are resolved.

4. **Run the App:**  
   Build and run the app on an emulator or a physical device.

## Dependencies

- Jetpack Compose
- Material3
- Android Lifecycle & ViewModel Compose

---

This project is provided as part of a technical assessment. 
