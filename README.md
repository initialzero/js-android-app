JasperMobile for Android
========================

JasperMobile for Android is a native application which allows you to browse your JasperReports Server repository and view reports in several formats.

It is built using the Jaspersoft Mobile SDK for Android, and is compatible with JasperReports Server 5.0 or higher.

The source code of JasperMobile for Android is freely available and can be used as a good example of how to integrate reporting and analysis services of JasperReports Server with their native applications.

General Information
--------------------

Please see the JasperMobile for Android project page:
http://community.jaspersoft.com/project/jaspermobile-android

Source Build
--------------------

We use Apache Maven and the Android Maven Plugin to build JasperMobile application for Android.

- Install Apache Maven from http://maven.apache.org/

- Install the Android SDK and Android 4.1 (API 16) SDK Platform
  (the minimum API Level required for the application to run is API 9, but API 16 is required for build the application)

- Install the Jaspersoft Android SDK for Android from http://community.jaspersoft.com/project/mobile-sdk-android
  Please see the Documentation on Jaspersoft Community site for details:
  http://community.jaspersoft.com/documentation

- To build JasperMobile application for Android, from the command line, run:
  mvn clean install 

- Deploy application on a connected device/emulator

Documentation about using the Android Maven Plugin including archetypes for project creation,
issues tracker, mailing list and more can be found at http://code.google.com/p/maven-android-plugin/
