Sume Launcher is a launcher app designed for E-ink Readers running Android OS. Its name comes from sumere, which means "take". Reading is a good way to acquire knowledge, and I hope this app will guide you to apps through which you can accomplish this goal.

Nowadays, people use E-ink Readers to obtain information in a more eyes-friendly way. However, productors of this kind of device may tighten restrictions of installing apps or managing settings due to the aim to promote their own online book vending business and reducing cost of repairing to the lowest as they can. Furthermore, their ability of developing may be quite poor so that the systems, especially the build-in launcher apps of their products are very hard to use. This app was developed for the purpose of breaking down limitations set by these productors and managing installed apps and settings easily.

Any feedbacks and suggestions are welcomed and appreciated.

Function
1. Displays all launchable applications on your device and provides entrances to manage them.
2. Immitates status bar to show time, date and battery information.
3. Multiple ways to switch page.


秀墨启动器是一个为运行Android操作系统的电纸书阅读器设计的桌面应用程序。它的名字来源于（拉丁语）sumere，意为“拿”，引申为“获取”，对应“通过阅读获取知识”；中文名“秀墨”在音译的同时，还有“秀丽的墨迹”的含义，十分贴合“电纸书阅读器”这一主题。（这个名字是LLM取的，哈哈。）

近年来，国内的许多电纸书厂商都转向使用“开放的Android系统”。然而，这些厂商为了“让你专心读书”，不择手段地限制用户的权力，包括但不限于隐藏原生设置（com.android.settings）、魔改软件包安装程序（com.android.packageinstaller）和禁用ADB等。诚然，很多人只用电纸书来阅读图书和文档，但主观且武断地认为所有读者都是如此，以此为理由去教用户做事，是不能接受的。我相信许多人有使用第三方应用的需求，也早已厌倦了厂商内置的<样式=“划掉” 文本=“功能繁杂且优化很差”>的阅读-桌面一体化默认桌面。同时，由于上述桌面大多共享系统签名，很多设备在被crack后也没有桌面可用。此应用正是为解决这一问题而生。

此应用力图通过现代的方式实现一个尽量简单的桌面，在满足需求的同时尽可能提升流畅度。针对电纸书普遍存在的性能问题，此应用使用ViewPager2而非传统GridLayout（Launcher3的实现）实现应用程序页面。它具有垃圾回收功能，对于安装了较多应用、需要多个页面显示应用程序的设备，能够自动回收暂时不需要的页面，保证流畅性和较低的内存占用。部分电纸书具有翻页按键，此应用已适配通过这些按键（在系统中被“映射”为音量键）翻页的功能。对于部分设备将status_bar_height设为0、导致状态栏被隐藏的问题，此应用实现了一个简单的状态栏。若存在实现类iOS样式控制中心的需求，日后的更新中将会提供。

希望此应用能带给您良好的使用体验。欢迎在Issue中反馈问题、提出意见和建议。在此不胜感激。
