package pub.gusten.gbgcommuter.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import pub.gusten.gbgcommuter.models.Departure;
import pub.gusten.gbgcommuter.models.NotificationAction;
import pub.gusten.gbgcommuter.models.TrackedRoute;

public class TrackerService extends Service {


    private final String PREFS_REF = "trackerService";
    private NetworkManager networkManager;
    private NotificationService notificationService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            hasBoundNotification = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            hasBoundNotification = true;
            NotificationService.LocalBinder mLocalBinder = (NotificationService.LocalBinder)service;
            notificationService = mLocalBinder.getService();
            notificationService.showNotification("", "", "");
        }
    };
    private boolean hasBoundNotification;
    private List<TrackedRoute> trackedRoutes;
    private int trackedRouteIndex;
    private boolean flipRoute;
    private SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new TrackerService.LocalBinder();

    public class LocalBinder extends Binder {
        TrackerService getService() {
            return TrackerService.this;
        }
    }

    @Override
    public void onCreate() {
        networkManager = new NetworkManager(this);
        startService(new Intent(this, NotificationService.class));
        bindService(new Intent(this, NotificationService.class), serviceConnection, BIND_AUTO_CREATE);

        trackedRoutes = new ArrayList<>();
        trackedRoutes.add(new TrackedRoute("elisedal", "9021014002210000", "valand", "9021014007220000", "4"));
        trackedRoutes.add(new TrackedRoute("pilbågsgatan", "9021014005280000", "vasaplatsen", "9021014007300000", "19"));
        trackedRouteIndex = 0;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!hasBoundNotification || !intent.hasExtra("action") || trackedRoutes.size() == 0) {
            return START_STICKY;
        }

        NotificationAction result = (NotificationAction)intent.getSerializableExtra("action");
        switch (result) {
            case PREVIOUS:
                trackedRouteIndex = trackedRouteIndex - 1 < 0 ? trackedRoutes.size() - 1 : trackedRouteIndex - 1;
                trackRoute(trackedRoutes.get(trackedRouteIndex));
                break;
            case NEXT:
                trackedRouteIndex = (trackedRouteIndex + 1) % trackedRoutes.size();
                trackRoute(trackedRoutes.get(trackedRouteIndex));
                break;
            case FLIP:
                flipRoute = !flipRoute;
                trackRoute(trackedRoutes.get(trackedRouteIndex));
                break;
            default:
                break;
        }
        return START_STICKY;
    }

    private void trackRoute(final TrackedRoute route) {
        try {
            final String fromStopId = flipRoute ? route.getToStopId() : route.getFromStopId();
            final String fromName = flipRoute ? route.getTo() : route.getFrom();
            final String toName = flipRoute ? route.getFrom() : route.getTo();

            networkManager.fetchDepartures(fromStopId,
                    new NetworkManager.DeparturesRequest() {
                        @Override
                        public void onRequestCompleted(List<Departure> departures) {
                            for (Departure departure: departures) {
                                if (route.tracks(departure)) {
                                    try {
                                        fullDateFormat.setTimeZone(TimeZone.getDefault());
                                        Date tmpDate = fullDateFormat.parse(departure.getDate() + " " + departure.getTime());
                                        notificationService.showNotification(fromName + " -> " + toName, timeFormat.format(tmpDate), departure.getSname());
                                        break;
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onRequestFailed() {
                            notificationService.showNotification("Could not fetch departures", "", "");
                        }
                    });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }
}
