package pub.gusten.gbgcommuter.models;

public class Line {
    public final String name;
    public final String bgColor;
    public final String fgColor;

    public Line(String name, String bgColor, String fgColor){
        this.name = name;
        this.bgColor = bgColor;
        this.fgColor = fgColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        Line otherLine = (Line) o;
        return name.equals(otherLine.name);
    }
}
