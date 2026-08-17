# Add project specific ProGuard rules here.

# TinyPinyin 及 Aho-Corasick（纯 Java 库，防止被 R8 过度裁剪）
-keep class com.github.promeg.pinyinhelper.** { *; }
-dontwarn com.github.promeg.pinyinhelper.**
-keep class org.ahocorasick.** { *; }
-dontwarn org.ahocorasick.**

# Room 实体与 DataStore 设置（字段名需保留，供 Room/DataStore 生成代码访问）
-keep class com.t9launcher.data.** { *; }
-keep class com.t9launcher.settings.** { *; }
