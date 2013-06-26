JasperMobile for Android version 1.5 Readme

JasperMobile for Android is a native application which allows you to browse your JasperReports Server repository and view reports in several formats.

It is built using the Jaspersoft Mobile SDK for Android, and is compatible with JasperReports Server 4.5.1 or higher.  It can be used with JRS CE version 4.5.0, after installing the compatibility hotfix - hotfix_JRS_CE_4.5.0_Android_compat.zip (available from http://community.jaspersoft.com/project/mobile-sdk-android/releases).

The source code of JasperMobile for Android is freely available and can be used as a good example of how to integrate reporting and analysis services of JasperReports Server with their native applications.

General Information
--------------------

Please see the JasperMobile for Android project page:
http://community.jaspersoft.com/project/jaspermobile-android

What's New
--------------------

*	Improved Action Bar
*	Translations for:  Chinese, French, German, Italian, Japanese, Spanish,
*	Application Settings
*	JRS CE 5.0.1 bug fix
* Improved stability
*	bug fixes


Source Build
--------------------

We use Apache Maven and the Android Maven Plugin to build JasperMobile application for Android.

- Install Apache Maven from http://maven.apache.org/

- Install the Android SDK and Android 4.0 (API 14) SDK Platform
  (the minimum API Level required for the application to run is API 7, but API 14 is required for build the application)

- Install the Jaspersoft Android SDK for Android from http://community.jaspersoft.com/project/mobile-sdk-android
  Please see "Getting Started.html" included with the SDK for details.

- To build JasperMobile application for Android, from the command line, run:
  mvn clean install 

- Deploy application on a connected device/emulator

Documentation about using the Android Maven Plugin including archetypes for project creation,
issues tracker, mailing list of oon thers users  and more can be found at http://code.google.com/p/maven-android-plugin/
