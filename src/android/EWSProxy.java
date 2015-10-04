package org.apache.cordova.plugin;

import java.util.*;
import java.net.URI;
import java.net.URISyntaxException;
import microsoft.exchange.webservices.data.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.lang.ArrayUtils;


static class RedirectionUrlCallback implements IAutodiscoverRedirectionUrl {
  public boolean autodiscoverRedirectionUrlValidationCallback(String redirectionUrl) {
    return redirectionUrl.toLowerCase().startsWith("https://");
  }
}

/**
* This class check if google play is installed
*/
public class EWSProxy {

  private ExchangeService service = null;
  public CalendarFolder defaultCalendar = null;
  public List<CalendarFolder> calendars = null;

  public EWSProxy(String serverUrl, String email, String password){

    this.service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);

    if (server != null) {
      try {
        service.setUrl(new URI(serverUrl));//"https://domain/EWS/Exchange.asmx"
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    } else if (email != null) {
      service.autodiscoverUrl(email, new RedirectionUrlCallback());
    } else {
      // should raise error here
    }
    service.setTraceEnabled(true);
    this.connect(email, password);
  }

  public Boolean connect(String email, String password) {
    ExchangeCredentials credentials = new WebCredentials(email, password);
    this.service.setCredentials(credentials);

    this.defaultCalendar = null;
    this.defaultCalendar = CalendarFolder.bind(this.service, WellKnownFolderName.Calendar);
    return this.defaultCalendar != null;
  }


  /***
  *
  * MEETING FUNCTIONS
  *
  **/
  public String createMeeting(FolderId calId, JSONObject jsMeeting){
    AppointmentWrapper appointment = new AppointmentWrapper(this.service);
    appointment.setJsData(jsMeeting);
    appointment.save(calId);
    return appointment.getId().toString();
  }

  public String createMeeting(JSONObject jsMeeting){
    return this.createMeeting(this.defaultCalendar.getId(), new JSONObject(jsString));
  }

  public String createMeeting(String jsString){
    return this.createMeeting(new JSONObject(jsString));
  }

  public String createMeeting(CalendarFolder calendar, JSONObject jsMeeting){
    return this.createMeeting(calendar.getId(), jsMeeting);
  }

  public String updateMeeting(String jsString){
    this.updateMeeting(new JSONObject(jsString));
  }

  public String updateMeeting(JSONObject jsMeeting){
    String meetingId = jsMeeting.getString("id");
    if(meetingId == null) {
      throw new Exceptions("UpdateMeeting : missing meeting Id");
    }

    AppointmentWrapper appointment = AppointmentWrapper.bind(this.service, new ItemId(meetingId));
    appointment.setJsData(jsMeeting);
    appointment.update(ConflictResolutionMode.AutoResolve, SendInvitationsOrCancellationsMode.SendOnlyToChanged);
    return appointment.getId();
  }

  public void cancelMeeting(String uniqueId){
    Appointment appointment = Appointment.bind(this.service, new ItemId(uniqueId));
    appointment.cancelMeeting();
  }

  public List<AppointmentWrapper> findMeetings(CalendarFolder folder, Date startDate, Date endDate) {
    FindItemsResults<AppointmentWrapper> findResults = (FindItemsResults<AppointmentWrapper>)(Object)folder.findAppointments(new CalendarView(startDate, endDate));
    return findResults.getItems();
  }

  public List<AppointmentWrapper> findMeetings(Date startDate, Date endDate) {
    return this.findMeetings(this.defaultCalendar, startDate, endDate);
  }

  public JSONArray findMeetings(String start, String end) {
    return this.findMeetings(this.defaultCalendar, start, end);
  }

  public JSONArray findMeetings(CalendarFolder folder, String startDateISO, String endDateISO) {
    Date startDate = javax.xml.bind.DatatypeConverter.parseDateTime(startDateISO);
    Date endDate = javax.xml.bind.DatatypeConverter.parseDateTime(endDateISO);
    List<AppointmentWrapper> meetings = this.findMeetings(folder, startDate, endDate);

    JSONArray jsonMeetings = new JSONArray();

    for (int i = 0, len = meetings.length(); i < len; i++) {
      jsonMeetings.add(meetings[i].getJsData());
    }

    return jsonMeetings;
  }


  /***
  *
  * CALENDAR FUNCTIONS
  *
  **/

  public CalendarFolder createCalendar(String name) {
    CalendarFolder calendar = new CalendarFolder(this.service);
    calendar.setDisplayName(name);
    calendar.save(WellKnownFolderName.PublicFoldersRoot);

    if(this.calendars != null)
    this.calendars.add(calendar);

    return calendar;
  }

  public void updateCalendar(String calId, String name) {
    CalendarFolder calendar = this.getCalendar(calId);
    calendar.setDisplayName(name);
    calendar.update(ConflictResolutionMode.AutoResolve);
  }

  public CalendarFolder getRootCalendar() {
    return this.getCalendar(WellKnownFolderName.Calendar);
  }

  public CalendarFolder getCalendar(String calId) {
    return this.getCalendar(new FolderId(calId));
  }

  public CalendarFolder getCalendar(FolderId calId) {
    return CalendarFolder.bind(this.service, calId);
  }

  public JSONArray getJSONCalendars() {
    List<CalendarFolder> calendars = this.getCalendars();
    JSONArray jsonCalendars = new JSONArray();

    for (int i = 0, len = calendars.length(); i < len; i++) {
      JSONArray jsonCalendar = new JSONArray();
      jsonCalendar.put("id",calendars[i].getId());
      jsonCalendar.put("title",calendars[i].getDisplayName());
      jsonCalendars.add(jsonCalendar);
    }

    return jsonCalendars;
  }

  public List<CalendarFolder> getCalendars() {
    return this.getCalendars(WellKnownFolderName.PublicFoldersRoot);
  }

  public List<CalendarFolder> getCalendars(FolderId calId) {
    if(this.calendars == null) {
      FindFoldersResults findResults = this.service.FindFolders(calId);
      this.calendars = findResults.Folders;
    }
    return this.calendars;
  }

  public String getCalendarId(String name) {
    CalendarFolder calendar = this.getCalendarByName(name);

    if(calendar == null)
    return null;

    return calendar.getId().toString();
  }

  public String getCalendarName(String calId) {
    return this.getCalendar(calId).getDisplayName();
  }

  public CalendarFolder getCalendarByName(String name) {
    if(this.calendars == null)
    this.getCalendars();

    for (int i = 0, len = this.calendars.length(); i < len; i++) {
      if(this.calendars[i].getDisplayName() == name) {
        return this.calendars[i];
      }
    }

    return this.getCalendar(WellKnownFolderName.Calendar);
  }

  public CalendarFolder selectCalendarByName(String name) {
    this.defaultCalendar = this.getCalendarByName(name);

    if(this.defaultCalendar == null)
    this.defaultCalendar = this.createCalendar(name);

    return this.defaultCalendar;
  }

  public CalendarFolder selectCalendard(String calId) {
    this.defaultCalendar = this.getCalendar(calId);
    return this.defaultCalendar;
  }

}
