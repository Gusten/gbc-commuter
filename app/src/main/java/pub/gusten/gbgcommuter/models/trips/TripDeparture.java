package pub.gusten.gbgcommuter.models.trips;

public class TripDeparture {
    private String name;
    private String type;
    private String time;
    private String date;
    private String rtTime;
    private String rtDate;
    private String track;
    private long id;
    private long routeIdx;

    @Override
    public String toString() {
        return "TripDeparture{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", time='" + time + '\'' +
                ", date='" + date + '\'' +
                ", rtTime='" + rtTime + '\'' +
                ", rtDate='" + rtDate + '\'' +
                ", track='" + track + '\'' +
                ", id=" + id +
                ", routeIdx=" + routeIdx +
                '}';
    }

    public String getTime() {
        return (rtTime != null) ? rtTime : time;
    }

    public String getDate() {
        return (rtDate != null) ? rtDate : date;
    }
}
