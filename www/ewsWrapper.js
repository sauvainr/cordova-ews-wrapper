var exec = require('cordova/exec');

var errCallback = function (err) {
  console.error('ERROR: ' + err);
};

function EWSWrapper(server, email, password, callback) {
  console.log('EWS-Cordova init..');
  exec(callback, errCallback, 'EWSWrapper', 'init', [server, email, password]);
}

EWSWrapper.prototype = {
  'connect': function (email, password, callback) {
    exec(callback, errCallback, 'EWSWrapper', 'connect', [email, password]);
  },
  'getCalendars': function (callback) {
    exec(callback, errCallback, 'EWSWrapper', 'getCalendars', []);
  },
  'createCalendar': function (title, callback) {
    exec(callback, errCallback, 'EWSWrapper', 'createCalendar', [title]);
  },
  'selectCalendar': function (calId, callback) {
    exec(callback, errCallback, 'EWSWrapper', 'selectCalendar', [calId]);
  },
  'findMeetings': function (startISO, endISO, callback) {
    exec(callback, errCallback, 'EWSWrapper', 'findMeetings', [startISO, endISO]);
  },
  'createMeeting': function (jsMeeting, callback) {
    exec(callback, errCallback, 'EWSWrapper', 'createMeeting', [jsMeeting]);
  },
  'updateMeeting': function (jsMeeting, callback) {
    exec(callback, errCallback, 'EWSWrapper', 'updateMeeting', [jsMeeting]);
  }
};

module.exports = EWSWrapper;
