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

To run the espresso test, you will need to create a `secrets.xml` file inside `app/src/main/res/values`.
This file should contain the same username and password and look like this:

```
<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2">
    <string name="github_helper_username">BurningOnUp</string>
    <string name="github_helper_password">somethingOrOther</string>
</resources>
```

Then, to run the tests, use this command:

```
GITHUB_HELPER_USERNAME=BurningOnUp \
GITHUB_HELPER_PASSWORD=somethingOrOther \
JAVA_HOME=~/bin/jdk1.7.0_79 \
ANDROID_HOME=~/Android/Sdk \
./gradlew connectedTest
```
