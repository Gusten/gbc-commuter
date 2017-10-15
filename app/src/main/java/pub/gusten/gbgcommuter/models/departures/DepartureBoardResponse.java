package pub.gusten.gbgcommuter.models.departures;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DepartureBoardResponse {

    @SerializedName("DepartureBoard")
    private DepartureBoard departureBoard;

    public List<Departure> getDepartures() {
        return departureBoard.getDepartures();
    }

    public boolean hasError() {
        return departureBoard.hasError();
    }

    public String getErrorText() {
        return departureBoard.getErrorText();
    }
}
