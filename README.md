# cordova-ews-wrapper



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
