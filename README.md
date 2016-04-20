# BasePlusubLib
=======

A base frame library for Android

![](website/main.png)

For more information please see [the website][1]
using this frame you can see [说明文档](website/using_info.pdf)


Download
--------

Download [the latest JAR][2] or grab via Gradle:
```groovy
compile 'com.plusub.lib:PlusubBaseViewLib:1.0.2'
```
or Maven:
```xml
<dependency>
  <groupId>com.plusub.lib</groupId>
  <artifactId>PlusubBaseViewLib</artifactId>
  <version>1.0.2</version>
  <type>pom</type>
</dependency>
```

if you want use base lib(not content view, you can via Gradle):
```groovy
compile 'com.plusub.lib:PlusubBaseLib:1.0.2'
```
or Maven:
```xml
<dependency>
  <groupId>com.plusub.lib</groupId>
  <artifactId>PlusubBaseLib</artifactId>
  <version>1.0.2</version>
  <type>pom</type>
</dependency>
```


ProGuard
--------

If you are using ProGuard you might need to add the following option:
```
-keepnames class * implements java.io.Serializable
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keepnames class * implements java.io.Serializable
-keep public class * extends com.plusub.lib.activity.BaseActivity{
    <fields>;
}
-keep public class * extends com.plusub.lib.activity.BaseFragment{
     <fields>;
 }
-keep public class * extends com.plusub.lib.activity.BaseFragmentActivity{
    <fields>;
}

-keep class com.nostra13.universalimageloader.** { *; }
-keep class de.greenrobot.event.** { *; }
```



License
--------

    Copyright 2013 Square, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


 [1]: http://www.blakequ.com