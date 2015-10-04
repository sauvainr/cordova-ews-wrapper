package org.apache.cordova.plugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.text.*;
import java.util.Date;
import java.util.Locale;

import microsoft.exchange.webservices.data.*;
import microsoft.exchange.webservices.data.core.*;
import microsoft.exchange.webservices.data.core.service.item.*;
import microsoft.exchange.webservices.data.core.enumeration.property.*;
import microsoft.exchange.webservices.data.property.complex.*;

import org.apache.commons.lang3.ArrayUtils;

public class AppointmentWrapper extends Appointment {

  public AppointmentWrapper(ExchangeService service) throws Exception{
    super(service);
  }

  public void setJsData(String jsString) throws Exception{
    this.setJsData(new JSONObject(jsString));
  }

  public void setJsData(JSONObject jsObject) throws Exception{
    String subject = jsObject.optString("title");
    if(subject != null)
      this.setSubject(subject);

    String body = jsObject.optString("description");
    if(body != null)
      this.setBody(MessageBody.getMessageBodyFromText(body));


    String startDateISO = jsObject.optString("start");
    if(startDateISO != null){
      DateTime endDate = new DateTime(endDateISO);
      this.setStart(startDate.toDate());
    }

    String endDateISO = jsObject.optString("end");
    if(endDateISO != null){
      DateTime endDate = new DateTime(endDateISO);
      this.setEnd(endDate.toDate());
    }

    String location = jsObject.optString("location");
    if(location != null)
      this.setLocation(location);

    JSONArray attendees = jsObject.getJSONArray("attendees");
    if(attendees != null){

      this.getRequiredAttendees().clear();

      for (int i = 0, len = attendees.length(); i < len; i++) {
          this.getRequiredAttendees().add(attendees.getString(i));
      }

    }//attendees

  } // setJsData

  public String getJsString() throws Exception{
    return this.getJsData().toString();
  }

  public JSONObject getJsData() throws Exception{
    JSONObject jsData = new JSONObject();

    jsData.put("title", this.getSubject());
    jsData.put("description", this.getBody());
    DateFormat df = new SimpleDateFormat("MM/dd/yyyyTHH:mm:ss.000Z");
    jsData.put("start", df.format(this.getStart()));
    jsData.put("end", df.format(this.getEnd()));
    jsData.put("location", this.getLocation());
    jsData.put("attendees", this.getRequiredAttendees());

    return jsData;

  }//getJsData
}
