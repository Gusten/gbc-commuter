package pub.gusten.gbgcommuter.helpers;

import android.graphics.Color;

public final class ColorUtils {

    public final static int getColorFromHex(String colorString) {
        int color = (int)Long.parseLong(colorString, 16);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color >> 0) & 0xFF;
        return Color.rgb(r, g, b);
    }
}
