package pub.gusten.gbgcommuter;

import org.threeten.bp.format.DateTimeFormatter;

public final class DateUtils {

    public final static DateTimeFormatter dateOnlyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public final static DateTimeFormatter timeOnlyFormatter = DateTimeFormatter.ofPattern("HH:mm");
    public final static DateTimeFormatter fullDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

}
