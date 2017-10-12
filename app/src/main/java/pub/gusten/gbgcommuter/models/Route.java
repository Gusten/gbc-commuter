package pub.gusten.gbgcommuter.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class Route {
    public final String from;
    public final String fromStopId;
    public final String to;
    public final String toStopId;
    public final String line;

    public Route(String from, String fromStopId, String to, String toStopId, String line) {
        this.from = from;
        this.fromStopId = fromStopId;
        this.to = to;
        this.toStopId = toStopId;
        this.line = line;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route otherRoute = (TrackedRoute) o;
        boolean tst = Objects.equals(from, otherRoute.from);
        boolean tst2 = Objects.equals(to, otherRoute.to);
        boolean tst3 = Objects.equals(line, otherRoute.line);
        return Objects.equals(from, otherRoute.from) &&
                Objects.equals(to, otherRoute.to) &&
                Objects.equals(line, otherRoute.line);
    }

    @Override
    public String toString() {
        return "Route {" +
                    "from = " + from +
                    ", fromStopId = " + fromStopId +
                    ", to = " + to +
                    ", toStopId = " + toStopId +
                    ", line = " + line +
                "}";
    }
}
