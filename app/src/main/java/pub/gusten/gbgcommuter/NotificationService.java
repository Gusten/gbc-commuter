package pub.gusten.gbgcommuter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.app.Notification.Action.Builder;
import android.widget.RemoteViews;

/**
 * Created by Gusten on 2017-10-07.
 */

public class NotificationService extends Service {
    private NotificationManager notificationManager;
    private final int NOTIFICATION_ID = R.integer.notification_id;
    private String NOTIFICATION_CHANNEL_ID;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        NotificationService getService() {
            return NotificationService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NOTIFICATION_CHANNEL_ID = getString(R.string.notification_channel_id);
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "testChannel", NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(mChannel);
        showNotification();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent custom_notification.
        notificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    private void showNotification() {
        Intent mainActivityIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification.Action previousRoute = new Notification.Action.Builder(R.drawable.ic_add_24dp, "Previous", pendingIntent).build();
        Notification.Action flipRoute = new Notification.Action.Builder(R.drawable.ic_add_24dp, "Flip", pendingIntent).build();
        Notification.Action nextRoute = new Notification.Action.Builder(R.drawable.ic_add_24dp, "Next", pendingIntent).build();

        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.custom_notification);

        Notification notification = new Notification.Builder(this, AUDIO_SERVICE)
            .setSmallIcon(R.drawable.ic_train_24dp)
            .setContent(contentView)
            .setChannelId(NOTIFICATION_CHANNEL_ID)
            .build();

        startForeground(NOTIFICATION_ID, notification);
    }
}
