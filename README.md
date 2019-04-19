# Rewards Module SDK for Android

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
  implementation 'io.cere.rewards_module:2.0.*'
}
```

Then rebuild your project.

## Usage

Firstly you need init it:
```java
  RewardsModule rewardsModule = new RewardsModule(context);
  rewardsModule.init("YOUR_APP_ID");
```

Then you can simply call somewhere in your code:
```java
  rewardsModule.show("YOUR_PLACEMENT");
```

For more info read our [JavaDoc](https://funler.github.io/widget-android/)
