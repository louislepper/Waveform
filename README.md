# Waveform #

This is an Android app that detects lines through your phone's camera, and then interprets those lines as sound waves. Those sound waves can be played directly, or they can be mapped to keyboard tones, and played on the onscreen keyboard.

The best way to try out this app is to download it from the play store: [Waveform @ Google Play](https://play.google.com/store/apps/details?id=com.louislepper.waveform&hl=en)

For getting the most interesting sounds I recommend putting your phone on a stand, facing it towards a whiteboard, and drawing soundwaves, like sinewaves, squarewaves, and shapes on the board. You'll need to make sure the board is clean, and doesn't have strong light reflections on it, otherwise patterns other than your hand drawn lines will be detected. (you'll be able to tell if this is happening from the onscreen feedback)

That being said, you can also just point your phone at anything, and hear what it sounds like :-)


For development on this project, you'll need to [setup and get familiar with openCV](http://opencv.org/platforms/android.html). If you have any questions just ask around here or send me a message.

## Setup

| Tool      | Version |
| ---       |  ---    |
| [OpenCV](https://opencv.org) | 4.13.0
| [Android Studio](https://developer.android.com/studio) | any recent
| [Android Build Tool](https://developer.android.com/about) | 36.0.0
| [Android NDK](https://developer.android.com/ndk/guides) | 28.2.13676358
| [CMake](https://cmake.org) | 3.22.1
| [Kotlin](https://kotlinlang.org/docs/reference/) | 2.4.0
| [Gradle](https://gradle.org) | 8.14.3
| [Android Gradle Plugin](https://developer.android.com/build) | 8.13.0
| JDK | 17
| Compile / target SDK | 36 (min 21)

NDK r28 is what makes the app's native libraries 16 KB page aligned, which
Android 15+ devices require. OpenCV 4.13.0 is used rather than 4.14.0 because
4.14's arm64 build probes the SVE vector length whenever `HWCAP2_SVE2` is set
without first checking `HWCAP_SVE`, and so crashes with `SIGILL` while loading
on hosts that report the former without the latter - including the Android
emulator on Apple Silicon. See the comment in [gradle.properties](gradle.properties).

## How to use this repository

1. [Download and Install Android Studio](https://developer.android.com/studio)

2. [Install NDK and CMake](https://developer.android.com/studio/projects/install-ndk.md)

3. Install *OpenCV Android release* :
    * Download the [OpenCV 4.13.0 Android release](https://github.com/opencv/opencv/releases/download/4.13.0/opencv-4.13.0-android-sdk.zip). Newer releases are listed on the [OpenCV website](https://opencv.org/releases/), but see the note above before moving off 4.13.0.
    * Unzip the downloaded file. It expands to an **OpenCV-android-sdk** directory; rename it to include the version, e.g. **OpenCV-4.13.0-android-sdk**, and put it on a path of your choice. Naming it by version lets several releases sit side by side, which makes switching between them a one line change.

4. Link your *Android Studio* project to the *OpenCV Android SDK* you just downloaded :
    * Open [gradle.properties](gradle.properties) file and edit following line with your own *OpenCV Android SDK* directory path :

          opencvsdk=/Users/Example/Downloads/OpenCV-4.13.0-android-sdk

5. Sync Gradle and run the application on your Android Device!

The `:opencv` Gradle module is the `sdk` directory of that SDK, included from
[settings.gradle](settings.gradle). It is not vendored into this repository, so the root
[build.gradle](build.gradle) injects this project's compileSdk, NDK version and Java level into it
instead of the SDK's own build file being edited in place.

## Bootstrap a new Android project with Native OpenCV support

Here are the steps to follow to create a new Android Studio project with native OpenCV support :

1. [Download and Install Android Studio](https://developer.android.com/studio)

2. [Install NDK and CMake](https://developer.android.com/studio/projects/install-ndk.md)

3. Create a new *Native Android Studio project* :
    * Select `File -> New -> New Project...` from the main menu.
    * Click `Phone and Tablet tab`, select `Native C++` and click next.
    * Choose an `Application Name`, select your favorite `language` (Kotlin or Java), choose `Minimum API level` (28 here) and select next.
    * Choose `Toolchain default` as *C++ standard* and click Finish.

4. Install *OpenCV Android release* :
    * Download the [OpenCV 4.13.0 Android release](https://github.com/opencv/opencv/releases/download/4.13.0/opencv-4.13.0-android-sdk.zip) or the latest available Android release on the [OpenCV website](https://opencv.org/releases/).
    * Unzip downloaded file and put the **OpenCV-android-sdk** directory on a path of your choice.
    
5. Add *OpenCV Android SDK* as a module into your project :
    * Open [setting.gradle](settings.gradle) file and append these two lines.
    
          include ':opencv'
          project(':opencv').projectDir = new File(opencvsdk + '/sdk')
        
    * Open [gradle.properties](gradle.properties) file and append following line. Do not forget to use correct *OpenCV Android SDK* path for your machine. 
    
          opencvsdk=/Users/Example/Downloads/OpenCV-4.13.0-android-sdk
          
    * Open [build.gradle](app/build.gradle) file and add `implementation project(path: ':opencv')` to dependencies section :
    
          dependencies {
              ...
              implementation project(path: ':opencv')
          }
    
    * Click on `File -> Sync Project with Gradle Files`.
    
6. Add following config to app [build.gradle](app/build.gradle) file :
    * In `android -> defaultConfig -> externalNativeBuild -> cmake` section, put these three lines :
    
          cppFlags "-frtti -fexceptions"
          abiFilters 'x86', 'x86_64', 'armeabi-v7a', 'arm64-v8a'
          arguments "-DOpenCV_DIR=" + opencvsdk + "/sdk/native"
        
7. Add following config to [CMakeLists.txt](app/src/main/cpp/CMakeLists.txt) file :
    * Before `add_library` instruction, add three following lines :
    
          include_directories(${OpenCV_DIR}/jni/include)
          add_library( lib_opencv SHARED IMPORTED )
          set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION ${OpenCV_DIR}/libs/${ANDROID_ABI}/libopencv_java4.so)
        
    * In `target_link_libraries` instruction arguments, add following line :
    
          lib_opencv
        
8. Add following *permissions* to your [AndroidManifest.xml](app/src/main/AndroidManifest.xml) file :

       <uses-permission android:name="android.permission.CAMERA"/>
       <uses-feature android:name="android.hardware.camera"/>
       <uses-feature android:name="android.hardware.camera.autofocus"/>
       <uses-feature android:name="android.hardware.camera.front"/>
       <uses-feature android:name="android.hardware.camera.front.autofocus"/>
    
9. Create your *MainActivity* :
    * You can copy paste MainActivity [Kotlin](/app/src/main/kotlin/com/example/waveform/MainActivity.kt) or [Java](/app/src/main/java/com/example/nativeopencvandroidtemplate/MainActivity.java) file. Do not forget to adapt package name.
    
10. Create your *activity_main.xml* :
    * You can copy paste [activity_main.xml](/app/src/main/res/layout/activity_main.xml) file.
    
11. Add native code in *native-lib.cpp* :
    * You can copy paste [native-lib.cpp](app/src/main/cpp/native-lib.cpp) file. Do not forget to adapt the method name : 
    `Java_com_example_nativeopencvtemplate_MainActivity_adaptiveThresholdFromJNI`
    should be replaced with 
    `Java_<main-activity-package-name-with-underscores>_MainActivity_adaptiveThresholdFromJNI`.
    
12. Sync Gradle and run the application on your Android Device!


This was partly created using this OpenCV template:
https://github.com/VlSomers/native-opencv-android-template
