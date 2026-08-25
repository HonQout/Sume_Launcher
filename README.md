# Sume Launcher

**English** | [Chinese-Simplified](./README.zh.md)

## Description
Sume Launcher is a launcher app designed for E-ink Readers running Android OS. Its name comes from latin sumere, which means "take", and can be extended to "acquire", corresponding to the aim of reading: acquiring knowledge. 

## Features
This app is dedicated to satisfy fundamental requirements while being as smooth as possible. 
1. In regard to the common problem of performance existing among E-ink Readers, this app used ViewPager2 instead of traditional GridLayout to realize the page of apps. It is capable to collect garbage. For those devices which have a great amount of applications installed, it is able to collect the pages which are not used at the moment to ensure smoothness and low memory usage. 
2. Some E-ink Readers provide PageUp and PageDown keys to switch pages. This app is optimized for these devices, which means that you can switch pages by using these keys.
3. For the devices which have status_bar_height set to 0 and leading to the absence of status bar, this app provides a simple simulated status bar. You can view the status of time, date, ringer mode, airplane mode, WiFi, Bluetooth and battery.
4. For the devices which have hide system Settings application, this app provides a simple control center. You can open Settings page of WiFi, Bluetooth, sound and display, or lock the screen by just pressing a button after Device Admin activated.

## Quick start

### System requirements
Recommended Version: Android 11 (API 30) or above, armeabi-v7a or arm64-v8a

Minimum version: Android 6.0 (API 23) or above, armeavi-v7a or arm64-v8a

### Installation guide
1. Click the latest version in Releases block on the right side of the page and open the page of version details;
2. Scroll down, find Sume_Launcher_<Version Name>_all.apk in Assets block and click it to download;
3. Enable "Install Apps From Unknown Source" in Settings;
4. Import the package into your device and click to install.

## Others
Hope this app brings you good using experience. Any feedback and suggestions in Issue are welcomed and appreciated.
