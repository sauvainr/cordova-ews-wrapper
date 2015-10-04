//see https://github.com/OfficeDev/ews-java-api/wiki/Getting-Started-Guide#using-https

package org.apache.cordova.plugin;

import java.util.*;
import org.apache.cordova.*;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import microsoft.exchange.webservices.data.core.service.folder.*;

/**
* This class check if google play is installed
*/
public class EWSWrapper extends CordovaPlugin {

  private EWSProxy ewsProxy = null;

  /**
  * Executes the request and returns PluginResult.
  *
  * @param action            The action to execute.
  * @param args              JSONArry of arguments for the plugin.
  * @param callbackContext   The callback id used when calling back into JavaScript.
  * @return                  True if the action was valid, false otherwise.
  */
  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if(this.ewsProxy == null && action.toLowerCase() != "init"){
      callbackContext.error("Not initialized yet.");
      return false;
    }

    String serverUrl = null;
    String email = null;
    String password = null;
    Boolean success = null;

    String calId = null;
    String meetingId = null;
    String title = null;
    String startISO = null;
    String endISO = null;

    CalendarFolder calendar = null;
    JSONArray calendars = null;
    JSONArray meetings = null;
    JSONObject jsMeeting = null;

    try {

    switch (action.toLowerCase()) {

      case "init":
      serverUrl = args.getString(0);
      email = args.getString(1);
      password = args.getString(2);
      this.ewsProxy = new EWSProxy(serverUrl, email, password);
      success = this.ewsProxy != null && this.ewsProxy.defaultCalendar != null;
      callbackContext.success(java.lang.Boolean.toString(success));
      return success;

      case "connect":
      email = args.getString(0);
      password = args.getString(1);
      success = this.ewsProxy.connect(email, password);
      callbackContext.success(java.lang.Boolean.toString(success));
      return success;

      case "getCalendars":
      calendars = this.ewsProxy.getJSONCalendars();
      if(calendars != null) {
        callbackContext.success(calendars);
        return true;
      }
      break;

      case "createCalendar":
      title = args.getString(0);
      calendar = this.ewsProxy.createCalendar(title);
      if(calendar != null){
        calId = calendar.getId().toString();
        callbackContext.success(calId);
        return true;
      }
      break;

      case "selectCalendar":
      calId = args.getString(0);
      calendar = this.ewsProxy.selectCalendard(calId);
      success = calendar != null;
      callbackContext.success(java.lang.Boolean.toString(success));
      return success;

      case "findMeetings":
      startISO = args.getString(0);
      endISO = args.getString(1);
      meetings = this.ewsProxy.findMeetings(startISO, endISO);
      success = meetings != null;
      callbackContext.success(meetings);
      return success;

      case "createMeeting":
      jsMeeting = args.getJSONObject(0);
      meetingId = this.ewsProxy.createMeeting(jsMeeting);
      success = meetingId != null;
      callbackContext.success(meetingId);
      return success;

      case "updateMeeting":
      jsMeeting = args.getJSONObject(0);
      meetingId = this.ewsProxy.updateMeeting(jsMeeting);
      success = meetingId != null;
      callbackContext.success(meetingId);
      return success;

      default:
      callbackContext.error("Unknown action.");
      return false;
    }

  }catch(Exception e){
    System.err.println("Exception: " + e.getMessage());
    callbackContext.error(e.getMessage());
    return false;
  }
    callbackContext.error("Action failed..");
    return false;
  }
}
