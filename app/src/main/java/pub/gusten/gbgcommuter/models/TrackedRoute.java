package pub.gusten.gbgcommuter.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TrackedRoute extends Route {
    private final List<Departure> upComingDepartures;

    public TrackedRoute(String from, String fromStopId, String to, String toStopId, String line) {
        super(from, fromStopId, to, toStopId, line);
        this.upComingDepartures = new ArrayList<>();
    }

    public List<Departure> getUpComingDepartures() {
        return upComingDepartures;
    }

    public boolean tracks(Departure departure) {
        return getLine().equals(departure.getLine());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackedRoute otherTrackedRoute = (TrackedRoute) o;
        return Objects.equals(from, otherTrackedRoute.from) &&
                Objects.equals(to, otherTrackedRoute.to) &&
                Objects.equals(line, otherTrackedRoute.line) &&
                upComingDepartures == otherTrackedRoute.upComingDepartures;
    }

    @Override
    public String toString() {
        return "TrackedRoute {" +
                    "from = " + from +
                    ", fromStopId = " + fromStopId +
                    ", to = " + to +
                    ", toStopId = " + toStopId +
                    ", line = " + line +
                    ", nr of upcoming departures = " + upComingDepartures.size() +
                "}";

        //", fromDirection = " + fromDirection +
        //", toDirection = " + toDirection +
    }
}
