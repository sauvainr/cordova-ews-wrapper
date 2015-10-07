# cordova-ews-wrapper


This plugin tryed to wrap for cordova the EWS java project: https://github.com/OfficeDev/ews-java-api

Unfortunatly this project is based on deprecated httpClient class which is not supported on recent version of android (>21).
Despit my try I could make the api work. Either by incorporating the missing httpClient or forcing using a legacy SDK.
The most-likely solution would be to update the original project with the alternative of httpClient.

Import httpClient: problem duplicate library wont compile
> http://stackoverflow.com/questions/20989317/multiple-dex-files-define-landroid-support-v4-accessibilityservice-accessibility/21100040#21100040 
> http://stackoverflow.com/questions/32153318/httpclient-wont-import-in-android-studio
> http://stackoverflow.com/questions/30856785/how-to-add-apache-http-api-legacy-as-compile-time-dependency-to-build-grade

Downgrade SDK: no difference more incompatibility issues
> http://stackoverflow.com/questions/28527902/cordova-phonegap-how-to-use-android-api-level-21
> http://stackoverflow.com/questions/20010969/phonegap-with-android-sdk-4-4

Compile & Include a newer forked version of the api
> https://github.com/NetCitadel/ews-java-api

Not tested potential solutions:
> Compile & Include httpClient missing class: may have compatibility issue
> Fork & Update the original project with the alternative of httpClient


EWS-Wrapper
======

> This plugin let your cordova javascript app to know if the Android device has google play installed.


## Installation

    cordova plugin add https://github.com/sauvainr/cordova-ews-wrapper


## Usage

The test is runned automatically at startup, so you can just just:

    var EWS = new EWSWrapper(server, email, password, function isReady(){

      EWS.getCalendars(function(calendars){

      });

    });
