package pub.gusten.gbgcommuter.models;

import java.util.Objects;

public class Stop {

    public final String name;
    public final long id;
    public final double lat;
    public final double lon;
    public final int weight;

    public Stop(String name, long id, double lat, double lon, int weight) {
        this.name = name;
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stop otherStop = (Stop) o;
        return Objects.equals(name, otherStop.name);
    }

    @Override
    public String toString() {
        return "Stop {" +
                    "name = " + name +
                    ", id = " + id +
                    ", lat = " + lat +
                    ", lon = " + lon +
                    ", weight = " + weight +
                "}";
    }
}
