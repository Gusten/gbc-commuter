package pub.gusten.gbgcommuter.helpers;

public final class TextUtils {

    public final static String getNameWithoutArea(String fullName) {
        return fullName.split(",")[0];
    }
}
