package pub.gusten.gbgcommuter.models;

import org.threeten.bp.LocalTime;

public class TimeInterval {
    public final LocalTime start;
    public final LocalTime end;

    public TimeInterval(LocalTime start, LocalTime end) {
        this.start = start;
        this.end = end;
    }

    public boolean encompass(LocalTime now) {
        return start.isBefore(now) && end.isAfter(now);
    }
}
