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

You can embed **WidgetView** component into your activity markup. Something like:
```xml
<RelativeLayout>
 <com.github.funler.widget_android.WidgetView
        android:id="@+id/widget_view"
        android:layout_width="300dp"
        android:layout_height="match_parent"
        android:layout_marginEnd="0dp"
        android:layout_marginBottom="0dp"
        android:background="@android:color/transparent"/>
</RelativeLayout>
```
After that in your java code you need to initialize widget and provide some required params. 
Declare **WidgetView** in you activity:
```java
public class SomeActivity {
  private WidgetView widgetView;
}
```
Then init it:
```java
  widgetView = (WidgetView) findViewById(R.id.widget_view);
  widgetView
    .setAppId("YOUR_APP_ID")
    .setUserId("YOUR_USER_ID")
    .setSections(SECTIONS_ARRAY)
    .load();
```
