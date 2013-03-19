base-android-library
====================

Core code to be reused in other Android apps. [at GitHub](https://github.com/imminent/base-android-library)

## Building

The build requires [Maven](http://maven.apache.org/download.html)
v3.0.3+ and the [Android SDK](http://developer.android.com/sdk/index.html)
to be installed in your development environment. In addition you'll need to set
the `ANDROID_HOME` environment variable to the location of your SDK:

    export ANDROID_HOME=/home/roberto/tools/android-sdk

After satisfying those requirements, the build is pretty simple:

* Run `mvn clean cobertura:cobertura -P default -P cobertura; mvn sonar:sonar -P cobertura` to run the tests and create the code analysis for viewing in Sonar
* Run `mvn clean install` to run the instrumented tests on the connected devices

## Acknowledgements

Uses many great open-source libraries from the Android dev community:

* [ActionBarSherlock](https://github.com/JakeWharton/ActionBarSherlock) for back-ported ActionBar,
  [Butterknife](https://github.com/JakeWharton/Butterknife) for View injections and
  [NineOldAndroids](https://github.com/JakeWharton/NineOldAndroids) for the
  back-ported Animation API
  [Jake Wharton](http://jakewharton.com/).
* [Dagger](https://github.com/square/dagger) for dependency-injection,
  [Spoon](https://github.com/square/spoon) for distributed integration tests,
  [Retrofit](https://github.com/square/retrofit) for REST client,
  [Otto](https://github.com/square/otto) for efficient EventBus and
  [Fest-Android](https://github.com/square/fest-android) for Android-focused assertions.
  [Square, Inc.](http://squareup.com)
* [Robolectric](http://pivotal.github.com/robolectric/)
  for Android JUnit testing.
* [android-maven-plugin](https://github.com/jayway/maven-android-plugin)
  for automating our build and producing release-ready APKs.
* WizardPager for data entry wizard pager,
  QuickReturnBar for maximizing screen real estate,
  Done/Discard for editing/creating a resource, and
  UndoBar for canceling an action.
  [Roman Nurik](http://code.google.com/p/romannurik-code)
* Project Structure thanks to [Quality Tools for Android](https://github.com/stephanenicolas/Quality-Tools-for-Android)