package pub.gusten.gbgcommuter.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TrackedRoute {
    public final String from;
    public final String fromStopId;
    public final String to;
    public final String toStopId;
    public final String line;
    public final String bgColor;
    public final String fgColor;
    public final List<Departure> upComingDepartures;

    public TrackedRoute(String from, String fromStopId, String to, String toStopId, String line, String bgColor, String fgColor) {
        this.from = from;
        this.fromStopId = fromStopId;
        this.to = to;
        this.toStopId = toStopId;
        this.line = line;
        this.bgColor = bgColor;
        this.fgColor = fgColor;
        this.upComingDepartures = new ArrayList<>();
    }


    public JSONObject toJsonObject() throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("from", from);
        jsonObj.put("fromStopId", fromStopId);
        jsonObj.put("to", to);
        jsonObj.put("toStopId", toStopId);
        jsonObj.put("line", line);
        jsonObj.put("bgColor", bgColor);
        jsonObj.put("fgColor", fgColor);
        return jsonObj;
    }

    public boolean tracks(Departure departure) {
        return line.equals(departure.line);
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
                    ", toStopId = " + toStopId +
                    ", line = " + line +
                    ", bgColor = " + bgColor +
                    ", fgColor = " + fgColor +
                    ", nr of upcoming departures = " + upComingDepartures.size() +
                "}";
    }
}
