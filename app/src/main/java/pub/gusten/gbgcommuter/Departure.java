package pub.gusten.gbgcommuter;

import org.json.JSONException;
import org.json.JSONObject;

public class Departure {
    private String name;
    private String sname;
    private String type;
    private String stopid;
    private String stop;
    private String time;
    private String date;
    private String journeyid;
    private String direction;
    private String track;
    private String fgColor;
    private String bgColor;
    private String stroke;
    private String JourneyDetailRef;

    public Departure(JSONObject jsonObject) throws JSONException {
        name =              jsonObject.getString("name");
        sname =             jsonObject.getString("sname");
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
    }

    public String getName() {
        return name;
    }

    public String getSname() {
        return sname;
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

    public String getFgColor() {
        return fgColor;
    }

    public String getBgColor() {
        return bgColor;
    }

    @Override
    public String toString() {
        return "Departure {" +
                    "name = " + name +
                    ", sname = " + sname +
                    ", type = " + type +
                    ", stopid = " + stopid +
                    ", stop = " + stop +
                    ", time = " + time +
                    ", date = " + date +
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
