package pub.gusten.gbgcommuter.models.trips;

import com.google.gson.annotations.SerializedName;

import org.threeten.bp.LocalDateTime;

import java.util.List;

public class Trip {

    @SerializedName("Leg")
    private List<TripLeg> tripLegs;

    @Override
    public String toString() {
        return "Trip{" +
                "TripLegs=" + tripLegs +
                '}';
    }

    public LocalDateTime getDepartureDateTime() {
        if (tripLegs.isEmpty()) {
            return LocalDateTime.MIN;
        }

        return tripLegs.get(0).getDepartureTime();
    }

    public LocalDateTime getArrivalDateTime() {
        if (tripLegs.isEmpty()) {
            return LocalDateTime.MAX;
        }

        return tripLegs.get(tripLegs.size() - 1).getArrivalTime();
    }
}
