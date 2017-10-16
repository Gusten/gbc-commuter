package pub.gusten.gbgcommuter.models;

import org.threeten.bp.LocalTime;

public class TimeInterval {
    public LocalTime start;
    public LocalTime end;

    public TimeInterval() {
        start = LocalTime.of(0, 0);
        end = LocalTime.of(23, 59);
    }

    public TimeInterval(LocalTime start, LocalTime end) {
        this.start = start;
        this.end = end;
    }

    public boolean encompass(LocalTime now) {
        return start.isBefore(now) && end.isAfter(now);
    }
}
