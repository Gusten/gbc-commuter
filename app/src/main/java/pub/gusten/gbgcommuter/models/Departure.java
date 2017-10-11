package pub.gusten.gbgcommuter.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDateTime;

import pub.gusten.gbgcommuter.helpers.DateUtils;

public class Departure {
    public final String name;
    public final String line;
    public final String type;
    public final String stopid;
    public final String stop;
    public final String time;
    public final String date;
    public final String rtTime;
    public final String rtDate;
    public final String journeyid;
    public final String direction;
    public final String track;
    public final String fgColor;
    public final String bgColor;
    public final String stroke;
    public final String JourneyDetailRef;
    public final LocalDateTime timeInstant;

    public Departure(JSONObject jsonObject) throws JSONException {
        name =              jsonObject.getString("name");
        line =             jsonObject.getString("sname");
        type =              jsonObject.getString("type");
        stopid =            jsonObject.getString("stopid");
        stop =              jsonObject.getString("stop");
        time =              jsonObject.getString("time");
        date =              jsonObject.getString("date");
        journeyid =         jsonObject.getString("journeyid");
        direction =         jsonObject.getString("direction");
        track =             jsonObject.getString("track");
        fgColor =           jsonObject.getString("fgColor");
        bgColor =           jsonObject.getString("bgColor");
        stroke =            jsonObject.getString("stroke");
        JourneyDetailRef =  jsonObject.getJSONObject("JourneyDetailRef").getString("ref");

        // Sometimes the json object from the api does not come with rtTime/rtDate fields
        if (jsonObject.has("rtTime") && jsonObject.has("rtDate")) {
            rtTime =            jsonObject.getString("rtTime");
            rtDate =            jsonObject.getString("rtDate");
        }
        else {
            rtTime = time;
            rtDate = date;
        }

        timeInstant = LocalDateTime.parse(rtDate + " " + rtTime, DateUtils.fullDateFormatter);
    }

    @Override
    public String toString() {
        return "Departure {" +
                    "name = " + name +
                    ", line = " + line +
                    ", type = " + type +
                    ", stopid = " + stopid +
                    ", stop = " + stop +
                    ", time = " + time +
                    ", date = " + date +
                    ", rtTime = " + rtTime +
                    ", rtDate = " + rtDate +
                    ", journeyid = " + journeyid +
                    ", direction = " + direction +
                    ", track = " + track +
                    ", fgColor = " + fgColor +
                    ", bgColor = " + bgColor +
                    ", stroke = " + stroke +
                    ", JourneyDetailRef = " + JourneyDetailRef +
                "}";
    }
}
