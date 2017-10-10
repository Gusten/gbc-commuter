package pub.gusten.gbgcommuter.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDateTime;

import pub.gusten.gbgcommuter.DateUtils;

public class Departure {
    private final String name;
    private final String line;
    private final String type;
    private final String stopid;
    private final String stop;
    private final String time;
    private final String date;
    private final String rtTime;
    private final String rtDate;
    private final String journeyid;
    private final String direction;
    private final String track;
    private final String fgColor;
    private final String bgColor;
    private final String stroke;
    private final String JourneyDetailRef;
    private final LocalDateTime timeInstant;

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

    public String getName() {
        return name;
    }

    public String getLine() {
        return line;
    }

    public String getType() {
        return type;
    }

    public String getStop() {
        return stop;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

    public String getRtTime() {
        return rtTime;
    }

    public String getRtDate() {
        return rtDate;
    }

    public String getDirection() {
        return direction;
    }

    public String getFgColor() {
        return fgColor;
    }

    public String getBgColor() {
        return bgColor;
    }

    public LocalDateTime getTimeInstant() {
        return timeInstant;
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
