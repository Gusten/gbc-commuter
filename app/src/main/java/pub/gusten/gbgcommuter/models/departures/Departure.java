package pub.gusten.gbgcommuter.models.departures;

import com.google.gson.annotations.SerializedName;

import org.threeten.bp.LocalDateTime;

import java.util.Objects;

import pub.gusten.gbgcommuter.helpers.DateUtils;

public class Departure {
    private String name;
    @SerializedName("sname")
    private String line;
    private String type;
    private String stop;
    private String time;
    private String date;
    private String rtTime;
    private String rtDate;
    private String direction;
    private String track;
    private String fgColor;
    private String bgColor;
    private String stroke;
    private long stopid;
    private long journeyid;
    @SerializedName("JourneyDetailRef")
    private JourneyDetailRef journeyDetailRef;

    public LocalDateTime getDepartureDateTime() {
        String dateString = (rtDate != null && rtTime != null) ? rtDate + " " + rtTime : date + " " + time;
        return LocalDateTime.parse(dateString, DateUtils.fullDateFormatter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Departure otherDeparture = (Departure) o;
        return Objects.equals(name, otherDeparture.name) &&
                Objects.equals(line, otherDeparture.line);
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
                    ", JourneyDetailRef = " + journeyDetailRef.getRef() +
                "}";
    }

    public String getLine() {
        return line;
    }

    public String getBgColor() {
        return bgColor;
    }

    public String getFgColor() {
        return fgColor;
    }
}
