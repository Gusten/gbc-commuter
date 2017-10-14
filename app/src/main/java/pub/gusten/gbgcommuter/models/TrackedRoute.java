package pub.gusten.gbgcommuter.models;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import pub.gusten.gbgcommuter.models.departures.Departure;

public class TrackedRoute {
    public final List<Line> lines;
    public final List<Departure> upComingDepartures;

    private Stop from;
    private Stop to;

    public TrackedRoute() {
        lines = new ArrayList<>();
        upComingDepartures = new ArrayList<>();
    }

    public TrackedRoute(Stop from, Stop to, List<Line> lines) {
        this.from = from;
        this.to = to;
        this.lines = lines;
        this.upComingDepartures = new ArrayList<>();
    }

    public Stop getFrom() {
        return from;
    }

    public Stop getTo() {
        return to;
    }

    public boolean tracks(Departure departure) {
        return lines.contains(departure.getLine());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackedRoute otherTrackedRoute = (TrackedRoute) o;
        return Objects.equals(from, otherTrackedRoute.from) &&
                Objects.equals(to, otherTrackedRoute.to) &&
                Objects.equals(lines, otherTrackedRoute.lines);
    }

    @Override
    public String toString() {
        return "TrackedRoute {" +
                    "from = { " + from.toString() + " }" +
                    ", to = { " + to.toString() + " }" +
                    ", lines = { " + StringUtils.join(lines, ", ") + " }" +
                    ", nr of upcoming departures = " + upComingDepartures.size() +
                "}";
    }
}
