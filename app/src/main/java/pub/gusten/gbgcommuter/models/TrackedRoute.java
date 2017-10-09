package pub.gusten.gbgcommuter.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class TrackedRoute {
    private String from;
    private String fromStopId;
    private String to;
    private String toStopId;
    private String line;

    public TrackedRoute(String from, String fromStopId, String to, String toStopId, String line) {
        this.from = from;
        this.fromStopId = fromStopId;
        this.to = to;
        this.toStopId = toStopId;
        this.line = line;
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

    public JSONObject toJsonObject() throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("from", from);
        jsonObj.put("fromStopId", fromStopId);
        jsonObj.put("to", to);
        jsonObj.put("toStopId", toStopId);
        jsonObj.put("line", line);
        return jsonObj;
    }

    public boolean tracks(Departure departure) {
        return getLine().equals(departure.getSname());
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
                "}";
    }
}
