package pub.gusten.gbgcommuter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Created by Gusten on 2017-10-07.
 */

public class NotificationService extends Service {
    private NotificationManager notificationManager;
    private final int NOTIFICATION_ID = R.integer.notification_id;
    private String NOTIFICATION_CHANNEL_ID;
    private NetworkManager networkManager;


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        NotificationService getService() {
            return NotificationService.this;
        }
    }

    @Override
    public void onCreate() {
        networkManager = new NetworkManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NOTIFICATION_CHANNEL_ID = getString(R.string.notification_channel_id);
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "testChannel", NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(mChannel);
        showNotification("Nothing to show here", "4min");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent custom_notification.
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public void showNotification(String routeName, String timeTilDeparture) {
        Intent updateNotificationIntent = new Intent(this, NotificationService.class);

        PendingIntent pendingIntent = PendingIntent.getService(
                this,
                0,
                updateNotificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.custom_notification);
        contentView.setTextViewText(R.id.notification_route, routeName);
        contentView.setTextViewText(R.id.notification_timeTilDeparture, timeTilDeparture);
        contentView.setOnClickPendingIntent(R.id.notification_prevBtn, pendingIntent);
        contentView.setOnClickPendingIntent(R.id.notification_nextBtn, pendingIntent);
        contentView.setOnClickPendingIntent(R.id.notification_flipBtn, pendingIntent);

        Notification notification = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_train_24dp)
            .setContent(contentView)
            .build();

        startForeground(NOTIFICATION_ID, notification);
    }
}
