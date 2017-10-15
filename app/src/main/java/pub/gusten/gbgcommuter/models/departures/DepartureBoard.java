package pub.gusten.gbgcommuter.models.departures;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DepartureBoard {

    private String noNamespaceSchemaLocation;
    private String servertime;
    private String serverdate;
    private String error;
    private String errorText;
    @SerializedName("Departure")
    private List<Departure> departures;

    public List<Departure> getDepartures() {
        return departures;
    }

    public boolean hasError() {
        return (error != null);
    }

    public String getErrorText() {
        return errorText;
    }
}
