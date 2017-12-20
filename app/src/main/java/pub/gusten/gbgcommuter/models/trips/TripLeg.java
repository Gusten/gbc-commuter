package pub.gusten.gbgcommuter.models.trips;

import com.google.gson.annotations.SerializedName;

import org.threeten.bp.LocalDateTime;

import pub.gusten.gbgcommuter.helpers.DateUtils;
import pub.gusten.gbgcommuter.models.JourneyDetailRef;

public class TripLeg {

    private long id;
    private String name;
    @SerializedName("sname")
    private String line;
    private String type;
    private String direction;
    private String track;
    private String fgColor;
    private String bgColor;
    private String stroke;
    private String accessibility;
    @SerializedName("origin")
    private TripDeparture tripStart;
    @SerializedName("destination")
    private TripDeparture tripEnd;
    @SerializedName("JourneyDetailRef")
    private JourneyDetailRef journeyDetailRef;

    @Override
    public String toString() {
        return "TripLeg{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", line='" + line + '\'' +
                ", type='" + type + '\'' +
                ", direction='" + direction + '\'' +
                ", track='" + track + '\'' +
                ", fgColor='" + fgColor + '\'' +
                ", bgColor='" + bgColor + '\'' +
                ", stroke='" + stroke + '\'' +
                ", accessibility='" + accessibility + '\'' +
                ", tripStart=" + tripStart +
                ", tripEnd=" + tripEnd +
                '}';
    }

    public LocalDateTime getDepartureTime() {
        String dateString = tripStart.getDate() + " " + tripStart.getTime();
        return LocalDateTime.parse(dateString, DateUtils.fullDateFormatter);
    }

    public LocalDateTime getArrivalTime() {
        String dateString = tripEnd.getDate() + " " + tripEnd.getTime();
        return LocalDateTime.parse(dateString, DateUtils.fullDateFormatter);
    }
}
