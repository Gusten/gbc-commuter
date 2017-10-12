package pub.gusten.gbgcommuter.helpers;

import org.apache.commons.lang.StringUtils;

public final class TextUtils {

    public final static String getNameWithoutArea(String fullName) {
        return fullName.split(",")[0];
    }

    public final static String splitCamelCase(String camelCasedString) {
        return StringUtils.join(
                StringUtils.splitByCharacterTypeCamelCase(camelCasedString),
                ' '
        );
    }
}
