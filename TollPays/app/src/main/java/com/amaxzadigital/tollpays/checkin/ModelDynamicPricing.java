package com.amaxzadigital.tollpays.checkin;

import android.util.Log;

import com.amaxzadigital.tollpays.Common;
import com.instacart.library.truetime.TrueTime;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ModelDynamicPricing {
    private String startTime, endTime, scenarioId, price, reason, formattedDate;
    private DateTime formatedStartTime, formatedEndTime, currentDateTime;
    private DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
    private DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd hh:mm:ss aa");
    private long differenceFromCurrentTime;
    private Duration duration;

    public ModelDynamicPricing(String startTime, String endTime, String scenarioId, String price, String reason) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.scenarioId = scenarioId;
        this.price = price;
        this.reason = reason;

        currentDateTime = new DateTime(TrueTime.now().getTime()).withZone(DateTimeZone.forID(Common.plazaDetails.getZone_name()));
        formattedDate = dateFormatter.print(currentDateTime);
        formatedStartTime = new DateTime(dateTimeFormatter.parseDateTime(formattedDate + " " + startTime)).withZone(DateTimeZone.forID(Common.plazaDetails.getZone_name()));
        formatedEndTime = new DateTime(dateTimeFormatter.parseDateTime(formattedDate + " " + endTime)).withZone(DateTimeZone.forID(Common.plazaDetails.getZone_name()));

    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public String getPrice() {
        return price;
    }

    public DateTime getFormatedStartTime() {
        return formatedStartTime;
    }

    public DateTime getFormatedEndTime() {
        return formatedEndTime;
    }

    public String getReason() {
        return reason;
    }

    public DateTime getCurrentDateTime() {
        return currentDateTime;
    }

    public long getDifferenceFromCurrentTime() {
        currentDateTime = new DateTime(TrueTime.now().getTime());
        if (currentDateTime.isAfter(formatedStartTime))
            duration = new Duration(currentDateTime.getMillis(), formatedEndTime.getMillis());
        else if(currentDateTime.isBefore(formatedStartTime))
            duration = new Duration(currentDateTime.getMillis(),formatedStartTime.getMillis());
        differenceFromCurrentTime = duration.getMillis();
        return differenceFromCurrentTime;
    }
}
