First, download the Android SDK and make sure you have Java installed.

To build a debug app, use this command:

```
JAVA_HOME=~/bin/jdk1.7.0_79 \
ANDROID_HOME=~/Android/Sdk \
./gradlew assembleDebug
```

To run the unit tests, use a GitHub account which:

* does not have 2-factor auth enabled.
* has a Jekyll repository named `username.github.io` created for the user.

Then, run this:

```
GITHUB_HELPER_USERNAME=BurningOnUp \
GITHUB_HELPER_PASSWORD=somethingOrOther \
JAVA_HOME=~/bin/jdk1.7.0_79 \
ANDROID_HOME=~/Android/Sdk \
./gradlew test
```

This single test proves that our GitHub client Java code makes a new file inside the Jekyll repository
and then views the published post.
