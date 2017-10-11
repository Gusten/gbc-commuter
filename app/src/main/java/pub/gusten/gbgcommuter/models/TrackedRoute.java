package pub.gusten.gbgcommuter.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TrackedRoute {
    private final String from;
    private final String fromStopId;
    private final String to;
    private final String toStopId;
    private final String line;
    private final List<Departure> upComingDepartures;

    //private final String fromDirection;
    //private final String toDirection;

    public TrackedRoute(String from, String fromStopId, String to, String toStopId, String line) {
        this.from = from;
        this.fromStopId = fromStopId;
        this.to = to;
        this.toStopId = toStopId;
        this.line = line;
        this.upComingDepartures = new ArrayList<>();

        //this.fromDirection = fromDirection;
        //this.toDirection = toDirection;
    }

    public String getFrom() {
        return from;
    }

    public String getFromStopId() {
        return fromStopId;
    }

    public String getTo() {
        return to;
    }

    public String getToStopId() {
        return toStopId;
    }

    public String getLine() {
        return line;
    }

    public List<Departure> getUpComingDepartures() {
        return upComingDepartures;
    }

    /*public String getFromDirection() {
        return fromDirection;
    }

    public String getToDirection() {
        return toDirection;
    }*/

    public JSONObject toJsonObject() throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("from", from);
        jsonObj.put("fromStopId", fromStopId);
        jsonObj.put("to", to);
        jsonObj.put("toStopId", toStopId);
        jsonObj.put("line", line);
        return jsonObj;

        //jsonObj.put("fromDirection", fromDirection);
        //jsonObj.put("toDirection", toDirection);
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
                Objects.equals(line, otherTrackedRoute.line);
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
