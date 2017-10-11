package pub.gusten.gbgcommuter.models;

public class Stop {

    private final String name;
    private final String id;
    private final double lat;
    private final double lon;
    private final int weight;

    public Stop(String name, String id, double lat, double lon, int weight) {
        this.name = name;
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public int getWeight() {
        return weight;
    }
}
