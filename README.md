TIBCO® JasperMobile? for Android
========================

TIBCO® JasperMobile? for Android is a native application which allows you to browse your JasperReports® Server repository and view reports in several formats.

It is built using the TIBCO® JasperMobile? SDK for Android, and is compatible with JasperReports® Server 5.5 or higher.

The source code of TIBCO® JasperMobile? for Android is freely available and can be used as a good example of how to integrate reporting and analysis services of JasperReports® Server with their native applications.

General Information
--------------------

Please see the TIBCO® JasperMobile? for Android project page:
http://community.jaspersoft.com/project/jaspermobile-android

Source Build
--------------------

We use gradle to build TIBCO® JasperMobile? application for Android.

- Install the Android SDK and Android 5.0 (API 21) SDK Platform
  (the minimum API Level required for the application to run is API 14, but API 21 is required for build the application)

- Install the TIBCO® JasperMobile? SDK for Android from http://community.jaspersoft.com/project/mobile-sdk-android
  Please see the Documentation on TIBCO® Jaspersoft® Community site for details:
  http://community.jaspersoft.com/documentation

- To build JasperMobile application for Android, from the command line, run:
  ./gradlew assemble

- Deploy application on a connected device/emulator, from the command line, run:
  ./gradlew install