package pub.gusten.gbgcommuter.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import pub.gusten.gbgcommuter.models.NotificationAction;
import pub.gusten.gbgcommuter.services.TrackerService;

public class ScreenActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Intent updateIntent = new Intent(context, TrackerService.class);
            updateIntent.putExtra("action", NotificationAction.UPDATE);
            context.startService(updateIntent);
        }
    }
}
