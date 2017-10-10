package pub.gusten.gbgcommuter.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

import pub.gusten.gbgcommuter.R;
import pub.gusten.gbgcommuter.models.NotificationAction;

public class NotificationService extends Service {
    private NotificationManager notificationManager;
    private final int NOTIFICATION_ID = R.integer.notification_id;
    private String NOTIFICATION_CHANNEL_ID;
    private SimpleDateFormat timeDateFormat = new SimpleDateFormat("HH:mm");

    private PendingIntent prevPendingIntent;
    private PendingIntent nextPendingIntent;
    private PendingIntent flipPendingIntent;

    @Nullable
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
        // Intent for selecting previous route
        Intent prevIntent = new Intent(this, TrackerService.class);
        prevIntent.putExtra("action", NotificationAction.PREVIOUS);
        prevPendingIntent = PendingIntent.getService(
                this,
                0,
                prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        // Intent for selecting next route
        Intent nextIntent = new Intent(this, TrackerService.class);
        nextIntent.putExtra("action", NotificationAction.NEXT);
        nextPendingIntent = PendingIntent.getService(
                this,
                1,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        // Intent for selecting flipping route
        Intent flipIntent = new Intent(this, TrackerService.class);
        flipIntent.putExtra("action", NotificationAction.FLIP);
        flipPendingIntent = PendingIntent.getService(
                this,
                2,
                flipIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NOTIFICATION_CHANNEL_ID = getString(R.string.notification_channel_id);
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "testChannel", NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(mChannel);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent custom_notification.
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public void showNotification(String routeName, String timeTilDeparture, String timeTilNextDeparture, String line) {
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.custom_notification);
        String departureText = timeTilDeparture + "min  (" + timeTilNextDeparture + "min)";
        contentView.setTextViewText(R.id.notification_timeTilDeparture, departureText);
        contentView.setTextViewText(R.id.notification_route, routeName);
        contentView.setTextViewText(R.id.notification_updatedAt, "Updated: " + timeDateFormat.format(new Date()));
        contentView.setTextViewText(R.id.notification_line, line);
        contentView.setOnClickPendingIntent(R.id.notification_prevBtn, prevPendingIntent);
        contentView.setOnClickPendingIntent(R.id.notification_nextBtn, nextPendingIntent);
        contentView.setOnClickPendingIntent(R.id.notification_flipBtn, flipPendingIntent);

        Notification notification = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_train_24dp)
            .setContent(contentView)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .build();

        startForeground(NOTIFICATION_ID, notification);
    }
}
