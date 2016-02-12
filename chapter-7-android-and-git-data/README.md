To build a debug app, use this command:

```
JAVA_HOME=~/bin/jdk1.7.0_79 ANDROID_HOME=~/Android/Sdk ./gradlew assembleDebug
```

To run the unit tests, use a GitHub account which does not have 2-factor auth enabled.

```
GITHUB_HELPER_USERNAME=BurningOnUp GITHUB_HELPER_PASSWORD=somethingOrOther JAVA_HOME=~/bin/jdk1.7.0_79 ANDROID_HOME=~/Android/Sdk ./gradlew test
```
