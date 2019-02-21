# Rewards Widget SDK for Android

## Including into project

In `build.gradle` file for your **project** add **jitpack** repo inside `allprojects.repositories` block:
```gradle
allprojects {
  repositories {
    ...your repos
    
    // add this
    maven {
      url 'https://jitpack.io'
    }
  }
}
```        

In `build.gradle` file for your **module** add new dependency into `dependencies` block:

```gradle
dependencies {
  ...your dependencies

  // add this
  implementation 'com.github.funler:widget-android:1.0.*'
}
```

Then rebuild your project.

## Usage

Firstly you need init it:
```java
  List<String> sections = new ArrayList<>();
  sections.add("secion_1");
  sections.add("secion_2");
  sections.add("secion_3");

  WidgetView widgetView = new WidgetView(context);
  widgetView.init("YOUR_APP_ID", "YOUR_USER_ID", sections);
```

Then you can simply call somewhere in your code:
```java
  widgetView.show();
```
