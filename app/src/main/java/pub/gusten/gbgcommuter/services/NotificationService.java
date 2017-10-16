package pub.gusten.gbgcommuter.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;

import pub.gusten.gbgcommuter.R;
import pub.gusten.gbgcommuter.helpers.DateUtils;
import pub.gusten.gbgcommuter.models.NotificationAction;
import pub.gusten.gbgcommuter.models.TrackedRoute;
import pub.gusten.gbgcommuter.models.departures.Departure;

import static pub.gusten.gbgcommuter.helpers.ColorUtils.getColorFromHex;
import static pub.gusten.gbgcommuter.helpers.TextUtils.getNameWithoutArea;
import static pub.gusten.gbgcommuter.helpers.TextUtils.splitCamelCase;

public class NotificationService extends Service {
    private NotificationManager notificationManager;
    private final int NOTIFICATION_ID = R.integer.notification_id;
    private String NOTIFICATION_CHANNEL_ID;

    private PendingIntent nextPendingIntent;
    private PendingIntent flipPendingIntent;
    private Notification currentNotification;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();

    class LocalBinder extends Binder {
        NotificationService getService() {
            return NotificationService.this;
        }
    }

    @Override
    public void onCreate() {
        NOTIFICATION_CHANNEL_ID = getString(R.string.notification_channel_id);
        String channelName = getString(R.string.notification_channel_name);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(mChannel);
        }

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
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public void showNotification(TrackedRoute route, boolean flipRoute, boolean dataIsFresh, boolean singleRoute) {
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.custom_notification);

        String from = getNameWithoutArea(flipRoute ? route.getTo().name : route.getFrom().name);
        String to = getNameWithoutArea(flipRoute ? route.getFrom().name : route.getTo().name);
        from = splitCamelCase(from);
        to = splitCamelCase(to);
        contentView.setTextViewText(R.id.notification_route, from + "  >>  " + to);

        if (dataIsFresh) {
            String updatedAt = DateUtils.timeOnlyFormatter.format(LocalTime.now());
            contentView.setTextViewText(R.id.notification_updatedAt, "Updated: " + updatedAt);
        }

        if (route.upComingDepartures.isEmpty()) {
            contentView.setViewVisibility(R.id.notification_empty_line, View.VISIBLE);
            contentView.setViewVisibility(R.id.notification_line, View.INVISIBLE);

            contentView.setTextViewText(R.id.notification_timeTilDeparture, "No departures found");
        }
        else {
            contentView.setViewVisibility(R.id.notification_empty_line, View.INVISIBLE);
            contentView.setViewVisibility(R.id.notification_line, View.VISIBLE);

            // Set time til departure
            Departure nextDeparture = route.upComingDepartures.get(0);
            LocalDateTime now = LocalDateTime.now();

            String timeTilDepartureText = "";
            long minutesTilDeparture = Duration.between(now, nextDeparture.getDepartureDateTime()).toMinutes();
            if (minutesTilDeparture <= 0) {
                timeTilDepartureText += "<1min";
            }
            else {
                timeTilDepartureText += minutesTilDeparture + "min";
            }

            if (route.upComingDepartures.size() > 1) {
                timeTilDepartureText += "  (" + Duration.between(now, route.upComingDepartures.get(1).getDepartureDateTime()).toMinutes() + "min)";
            }

            contentView.setTextViewText(R.id.notification_timeTilDeparture, timeTilDepartureText);
            contentView.setInt(R.id.notification_line, "setBackgroundColor", getColorFromHex(nextDeparture.getFgColor().substring(1)));
            contentView.setTextColor(R.id.notification_line, getColorFromHex(nextDeparture.getBgColor().substring(1)));
            contentView.setTextViewText(R.id.notification_line, nextDeparture.getLine());
        }

        // Set button actions/callbacks
        contentView.setViewVisibility(R.id.notification_nextBtn, (singleRoute) ? View.GONE : View.VISIBLE);
        contentView.setOnClickPendingIntent(R.id.notification_nextBtn, nextPendingIntent);
        contentView.setOnClickPendingIntent(R.id.notification_flipBtn, flipPendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            currentNotification = new Notification.Builder(this)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.tramicon)
                .setContent(contentView)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .build();
        }
        else {
            currentNotification = new NotificationCompat.Builder(this)
                .setChannel(NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.tramicon)
                .setContent(contentView)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .build();
        }

        startForeground(NOTIFICATION_ID, currentNotification);
    }

    public void pause() {
        stopForeground(true);
    }

    public void resume() {
        if (currentNotification != null) {
            startForeground(NOTIFICATION_ID, currentNotification);
        }
    }
}
