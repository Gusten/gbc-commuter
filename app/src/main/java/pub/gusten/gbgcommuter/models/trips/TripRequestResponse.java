package pub.gusten.gbgcommuter.models.trips;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TripRequestResponse {
    @SerializedName("TripList")
    private TripList tripList;

    public final List<Trip> getTrips() {
        return tripList.getTrips();
    }

    public boolean hasError() {
        return tripList.hasError();
    }

    public String getErrorText() {
        return tripList.getErrorText();
    }

    @Override
    public String toString() {
        return "TripRequestResponse{" +
                "tripList=" + tripList +
                '}';
    }
}
