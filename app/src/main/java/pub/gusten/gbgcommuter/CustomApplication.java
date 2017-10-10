package pub.gusten.gbgcommuter;

import com.jakewharton.threetenabp.AndroidThreeTen;

public class CustomApplication extends android.app.Application{
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
    }
}
