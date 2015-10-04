package org.apache.cordova.plugin;

import java.util.*;
import java.text.*;
import java.net.URI;
import java.net.URISyntaxException;

import microsoft.exchange.webservices.data.*;
import microsoft.exchange.webservices.data.credential.*;
import microsoft.exchange.webservices.data.search.*;
import microsoft.exchange.webservices.data.core.*;
import microsoft.exchange.webservices.data.core.service.*;
import microsoft.exchange.webservices.data.core.service.item.*;
import microsoft.exchange.webservices.data.core.service.folder.*;
import microsoft.exchange.webservices.data.core.enumeration.property.*;
import microsoft.exchange.webservices.data.core.enumeration.misc.*;
import microsoft.exchange.webservices.data.core.enumeration.service.*;
import microsoft.exchange.webservices.data.autodiscover.*;
import microsoft.exchange.webservices.data.property.complex.*;
import microsoft.exchange.webservices.data.misc.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.bind.DatatypeConverter;
import org.apache.commons.lang3.ArrayUtils;


class RedirectionUrlCallback implements IAutodiscoverRedirectionUrl {
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

  public EWSProxy(String serverUrl, String email, String password) throws Exception{

    this.service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);

    if (serverUrl != null) {
      try {
        this.service.setUrl(new URI(serverUrl));//"https://domain/EWS/Exchange.asmx"
      } catch (URISyntaxException e) {
        e.printStackTrace();
        return;
      }
    } else if (email != null) {
      this.service.autodiscoverUrl(email, new RedirectionUrlCallback());
    } else {
      // should raise error here
    }
    this.service.setTraceEnabled(true);
    this.connect(email, password);
  }

  public Boolean connect(String email, String password)  throws Exception {
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
  public String createMeeting(FolderId calId, JSONObject jsMeeting) throws Exception{
    AppointmentWrapper appointment = new AppointmentWrapper(this.service);
    appointment.setJsData(jsMeeting);
    appointment.save(calId);
    return appointment.getId().toString();
  }

  public String createMeeting(JSONObject jsMeeting) throws Exception{
    return this.createMeeting(this.defaultCalendar.getId(), jsMeeting);
  }

  public String createMeeting(String jsString) throws Exception{
    return this.createMeeting(new JSONObject(jsString));
  }

  public String createMeeting(CalendarFolder calendar, JSONObject jsMeeting){
    return this.createMeeting(calendar.getId(), jsMeeting);
  }

  public String updateMeeting(String jsString) throws Exception{
    return this.updateMeeting(new JSONObject(jsString));
  }

  public String updateMeeting(JSONObject jsMeeting) throws Exception{
    String meetingId = jsMeeting.getString("id");
    if(meetingId == null) {
      throw new Exception("UpdateMeeting : missing meeting Id");
    }

    AppointmentWrapper appointment = (AppointmentWrapper)AppointmentWrapper.bind(this.service, new ItemId(meetingId));
    appointment.setJsData(jsMeeting);
    appointment.update(ConflictResolutionMode.AutoResolve, SendInvitationsOrCancellationsMode.SendOnlyToChanged);
    return appointment.getId().toString();
  }

  public void cancelMeeting(String uniqueId) throws Exception{
    Appointment appointment = (AppointmentWrapper)Appointment.bind(this.service, new ItemId(uniqueId));
    appointment.cancelMeeting();
  }

  public List<AppointmentWrapper> findMeetings(CalendarFolder folder, Date startDate, Date endDate) throws Exception{
    FindItemsResults<AppointmentWrapper> findResults = (FindItemsResults<AppointmentWrapper>)(Object)folder.findAppointments(new CalendarView(startDate, endDate));
    return findResults.getItems();
  }

  public List<AppointmentWrapper> findMeetings(Date startDate, Date endDate) throws Exception {
    return this.findMeetings(this.defaultCalendar, startDate, endDate);
  }

  public JSONArray findMeetings(String start, String end) throws Exception {
    return this.findMeetings(this.defaultCalendar, start, end);
  }

  public JSONArray findMeetings(CalendarFolder folder, String startDateISO, String endDateISO) throws Exception {
    Date startDate = javax.xml.bind.DatatypeConverter.parseDateTime(startDateISO).getTime();
    Date endDate = javax.xml.bind.DatatypeConverter.parseDateTime(endDateISO).getTime();
    List<AppointmentWrapper> meetings = this.findMeetings(folder, startDate, endDate);

    JSONArray jsonMeetings = new JSONArray();

    for (AppointmentWrapper meeting : meetings) {
      jsonMeetings.put(meeting.getJsData());
    }

    return jsonMeetings;
  }


  /***
  *
  * CALENDAR FUNCTIONS
  *
  **/

  public CalendarFolder createCalendar(String name) throws Exception{
    CalendarFolder calendar = new CalendarFolder(this.service);
    calendar.setDisplayName(name);
    calendar.save(WellKnownFolderName.PublicFoldersRoot);

    if(this.calendars != null)
    this.calendars.add(calendar);

    return calendar;
  }

  public void updateCalendar(String calId, String name) throws Exception{
    CalendarFolder calendar = this.getCalendar(calId);
    calendar.setDisplayName(name);
    calendar.update();
  }

  public CalendarFolder getRootCalendar() throws Exception {
    return this.getCalendar(new FolderId(WellKnownFolderName.Calendar));
  }

  public CalendarFolder getCalendar(String calId) throws Exception {
    return this.getCalendar(new FolderId(calId));
  }

  public CalendarFolder getCalendar(FolderId calId) throws Exception {
    return CalendarFolder.bind(this.service, calId);
  }

  public JSONArray getJSONCalendars() throws Exception {
    List<CalendarFolder> calendars = this.getCalendars();
    JSONArray jsonCalendars = new JSONArray();

    // for (int i = 0, len = calendars.length(); i < len; i++) {
    for(CalendarFolder calendar : calendars){
      JSONObject jsonCalendar = new JSONObject();
      jsonCalendar.put("id",calendar.getId());
      jsonCalendar.put("title",calendar.getDisplayName());
      jsonCalendars.put(jsonCalendar);
    }

    return jsonCalendars;
  }

  public List<CalendarFolder> getCalendars() throws Exception {
    return this.getCalendars(new FolderId(WellKnownFolderName.PublicFoldersRoot));
  }

  public List<CalendarFolder> getCalendars(FolderId calId) throws Exception {
    if(this.calendars == null) {
      FindFoldersResults findResults = this.service.findFolders(calId, new FolderView(Integer.MAX_VALUE));

      this.calendars = new ArrayList<CalendarFolder>();

      for(Folder folder : findResults.getFolders()){
        this.calendars.add((CalendarFolder)folder);
      }

    }
    return this.calendars;
  }

  public String getCalendarId(String name) throws Exception {
    CalendarFolder calendar = this.getCalendarByName(name);

    if(calendar == null)
    return null;

    return calendar.getId().toString();
  }

  public String getCalendarName(String calId) throws Exception {
    return this.getCalendar(calId).getDisplayName();
  }

  public CalendarFolder getCalendarByName(String name) throws Exception {
    if(this.calendars == null)
    this.getCalendars();

    // for (int i = 0, len = this.calendars.length(); i < len; i++) {
    for(CalendarFolder calendar : this.calendars) {
      if(calendar.getDisplayName() == name) {
        return calendar;
      }
    }

    return this.getCalendar(new FolderId(WellKnownFolderName.Calendar));
  }

  public CalendarFolder selectCalendarByName(String name) throws Exception {
    this.defaultCalendar = this.getCalendarByName(name);

    if(this.defaultCalendar == null)
    this.defaultCalendar = this.createCalendar(name);

    return this.defaultCalendar;
  }

  public CalendarFolder selectCalendard(String calId) throws Exception {
    this.defaultCalendar = this.getCalendar(calId);
    return this.defaultCalendar;
  }

}
