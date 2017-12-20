package pub.gusten.gbgcommuter.models.trips;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class TripList {

    private String noNamespaceSchemaLocation;
    private String servertime;
    private String serverdate;
    private String error;
    private String errorText;
    @SerializedName("Trip")
    private List<Trip> trips;

    public final List<Trip> getTrips() {
        return trips;
    }

    public final boolean hasError() {
        return (error != null);
    }

    public final String getErrorText() {
        return errorText;
    }

    @Override
    public String toString() {
        return "TripList{" +
                "noNamespaceSchemaLocation='" + noNamespaceSchemaLocation + '\'' +
                ", servertime='" + servertime + '\'' +
                ", serverdate='" + serverdate + '\'' +
                ", error='" + error + '\'' +
                ", errorText='" + errorText + '\'' +
                ", trips=" + trips +
                '}';
    }
}
