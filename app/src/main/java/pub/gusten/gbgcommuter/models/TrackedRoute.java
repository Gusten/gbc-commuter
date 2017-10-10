package pub.gusten.gbgcommuter.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class TrackedRoute {
    private final String from;
    private final String fromStopId;
    //private final String fromDirection;
    private final String to;
    private final String toStopId;
    //private final String toDirection;
    private final String line;

    public TrackedRoute(String from, String fromStopId, String to, String toStopId, String line) {
        this.from = from;
        this.fromStopId = fromStopId;
        //this.fromDirection = fromDirection;
        this.to = to;
        this.toStopId = toStopId;
        //this.toDirection = toDirection;
        this.line = line;
    }

    public String getFrom() {
        return from;
    }

    public String getFromStopId() {
        return fromStopId;
    }

    /*public String getFromDirection() {
        return fromDirection;
    }*/

    public String getTo() {
        return to;
    }

    public String getToStopId() {
        return toStopId;
    }

    /*public String getToDirection() {
        return toDirection;
    }*/

    public String getLine() {
        return line;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("from", from);
        jsonObj.put("fromStopId", fromStopId);
        //jsonObj.put("fromDirection", fromDirection);
        jsonObj.put("to", to);
        jsonObj.put("toStopId", toStopId);
        //jsonObj.put("toDirection", toDirection);
        jsonObj.put("line", line);
        return jsonObj;
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
                    //", fromDirection = " + fromDirection +
                    ", to = " + to +
                    ", toStopId = " + toStopId +
                    //", toDirection = " + toDirection +
                    ", line = " + line +
                "}";
    }
}
