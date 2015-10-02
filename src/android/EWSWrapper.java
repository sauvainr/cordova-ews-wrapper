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

/**
* This class check if google play is installed
*/
public class EWSWrapper extends CordovaPlugin {

  private EWSProxy ewsProxy = null

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

    switch (action.toLowerCase()) {

      case "init":
      String serverUrl = args.getString(0);
      String email = args.getString(1);
      String password = args.getString(2);
      this.ewsProxy = new EWSProxy(serverUrl, email, password);
      Boolean success = this.ewsProxy != null && this.ewsProxy.defaultCalendar != null;
      callbackContext.success(success);
      return success;

      case "connect":
      String email = args.getString(0);
      String password = args.getString(1);
      Boolean success = this.ewsProxy.connect(email, password);
      callbackContext.success(java.lang.Boolean.toString(success));
      return success;

      case "getCalendars":
      JSONArray calendars = this.ewsProxy.getJSONCalendars();
      if(calendars != null) {
        callbackContext.success(calendars);
        return true;
      }
      break;

      case "createCalendar":
      String title = args.getString(0);
      CalendarFolder calendar = this.ewsProxy.createCalendar(title);
      if(calendar != null){
        String calId = calendar.getId().toString();
        callbackContext.success(calId);
        return true;
      }
      break;

      case "selectCalendar":
      String calId = args.getString(0);
      CalendarFolder calendar = this.ewsProxy.selectCalendard(calId);
      Boolean success = calendar != null;
      callbackContext.success(success);
      return success;

      case "findMeetings":
      String startISO = args.getString(0);
      String endISO = args.getString(1);
      JSONArray meetings = this.ewsProxy.findMeetings(startISO, endISO);
      Boolean success = meetings != null;
      callbackContext.success(meetings);
      return success;

      case "createMeeting":
      JSONObject jsMeeting = args.getJSONObject(0);
      String meetingId = this.ewsProxy.createMeeting(jsMeeting);
      Boolean success = meetingId != null;
      callbackContext.success(meetingId);
      return success;

      case "updateMeeting":
      JSONObject jsMeeting = args.getJSONObject(0);
      String meetingId = this.ewsProxy.updateMeeting(jsMeeting);
      Boolean success = meetingId != null;
      callbackContext.success(meetingId);
      return success;

      default:
      callbackContext.error("Unknown action.");
      return false;
    }

    callbackContext.error("Action failed..");
    return false;
  }

  public void connect(String email, String password) {
    ExchangeCredentials credentials = new WebCredentials(email, password);
    this.service.setCredentials(credentials);
  }

}
