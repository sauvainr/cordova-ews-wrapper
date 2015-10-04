package org.apache.cordova.plugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import Microsoft.Exchange.WebServices.Data;
import Microsoft.Exchange.WebServices.Data.*;

import microsoft.exchange.webservices.data;
import microsoft.exchange.webservices.data.*;
import microsoft.exchange.webservices.data.core.*;
import microsoft.exchange.webservices.data.core.service.item.*;
import microsoft.exchange.webservices.data.core.enumeration.property.*;

import javax.xml.bind.DatatypeConverter;
import org.apache.commons.lang3.ArrayUtils;

public class AppointmentWrapper extends Appointment {
  public void setJsData(String jsString){
    this.setJsData(new JSONObject(jsString));
  }

  public void setJsData(JSONObject jsObject){
    String subject = jsObject.optString("title");
    if(subject != null)
      this.setSubject(subject);

    String body = jsObject.optString("description");
    if(body != null)
      this.setBody(MessageBody.getMessageBodyFromText(body));

    String startDateISO = jsObject.optString("start");
    if(startDateISO != null){
      Date startDate = javax.xml.bind.DatatypeConverter.parseDateTime(startDateISO);
      this.setStart(startDate);
    }

    String endDateISO = jsObject.optString("end");
    if(endDateISO != null){
      Date endDate = javax.xml.bind.DatatypeConverter.parseDateTime(endDateISO);
      this.setEnd(endDate);
    }

    String location = jsObject.optString("location");
    if(location != null)
      this.setLocation(location);

    JSONArray attendees = jsObject.getJSONArray("attendees");
    if(attendees != null){

      this.getRequiredAttendees().Clear();

      for (int i = 0, len = attendees.length(); i < len; i++) {
          this.getRequiredAttendees().add(attendees[i]);
      }

    }//attendees

  } // setJsData

  public String getJsString(){
    return this.getJsData().toString();
  }

  public JSONObject getJsData(){
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
