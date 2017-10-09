package pub.gusten.gbgcommuter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import pub.gusten.gbgcommuter.services.NotificationService;
import pub.gusten.gbgcommuter.services.TrackerService;

public class AutostartHandler extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent myIntent = new Intent(context, TrackerService.class);
        context.startService(myIntent);
    }
}