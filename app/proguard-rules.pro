# ZukuLock specific rules
# ShizukuHelper invokes private Shizuku APIs via reflection, so the
# targets and their members must survive R8.
-keep class rikka.shizuku.Shizuku { *; }
-keep class rikka.shizuku.ShizukuRemoteProcess { *; }
-keep class rikka.shizuku.Shizuku$OnRequestPermissionResultListener { *; }
-dontwarn rikka.shizuku.**

# Keep line numbers in stack traces while still obfuscating class/method names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile