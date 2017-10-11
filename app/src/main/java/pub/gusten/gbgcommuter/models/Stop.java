package pub.gusten.gbgcommuter.models;

public class Stop {

    public final String name;
    public final String id;
    public final double lat;
    public final double lon;
    public final int weight;

    public Stop(String name, String id, double lat, double lon, int weight) {
        this.name = name;
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.weight = weight;
    }
}
