package com.ironsource.sdk.data;

import com.facebook.share.internal.ShareConstants;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import cz.msebera.android.httpclient.cookie.ClientCookie;

public class SSAEventCalendar extends SSAObj {
    private String DAILY = "daily";
    private String DAYS_IN_MONTH = "daysInMonth";
    private String DAYS_IN_WEEK = "daysInWeek";
    private String DAYS_IN_YEAR = "daysInYear";
    private String DESCRIPTION = ShareConstants.WEB_DIALOG_PARAM_DESCRIPTION;
    private String END = "end";
    private String EXCEPTIONDATES = "exceptionDates";
    private String EXPIRES = ClientCookie.EXPIRES_ATTR;
    private String FREQUENCY = "frequency";
    private String ID = ShareConstants.WEB_DIALOG_PARAM_ID;
    private String INTERVAL = "interval";
    private String MONTHLY = "monthly";
    private String MONTHS_IN_YEAR = "monthsInYear";
    private String RECURRENCE = "recurrence";
    private String REMINDER = "reminder";
    private String START = "start";
    private String STATUS = ParametersKeys.VIDEO_STATUS;
    private String WEEKLY = "weekly";
    private String WEEKS_IN_MONTH = "weeksInMonth";
    private String YEARLY = "yearly";
    private String mDescription;
    private String mEnd;
    private String mStart;

    public SSAEventCalendar(String value) {
        super(value);
        if (containsKey(this.DESCRIPTION)) {
            setDescription(getString(this.DESCRIPTION));
        }
        if (containsKey(this.START)) {
            setStart(getString(this.START));
        }
        if (containsKey(this.END)) {
            setEnd(getString(this.END));
        }
    }

    public String getDescription() {
        return this.mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public String getStart() {
        return this.mStart;
    }

    public void setStart(String Start) {
        this.mStart = Start;
    }

    public String getEnd() {
        return this.mEnd;
    }

    public void setEnd(String end) {
        this.mEnd = end;
    }
}
