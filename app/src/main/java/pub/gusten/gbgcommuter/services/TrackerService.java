package pub.gusten.gbgcommuter.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pub.gusten.gbgcommuter.models.Route;
import pub.gusten.gbgcommuter.receivers.ScreenActionReceiver;
import pub.gusten.gbgcommuter.models.Departure;
import pub.gusten.gbgcommuter.models.NotificationAction;
import pub.gusten.gbgcommuter.models.TrackedRoute;

public class TrackerService extends Service {

    private final String PREFS_REF = "trackerService";
    private final String PREFS_NAME = "trackedRoutes";

    private BroadcastReceiver screenReceiver;
    private List<TrackedRoute> trackedRoutes;
    private int trackedRouteIndex;
    private boolean flipRoute;

    private NotificationService notificationService;
    private boolean hasBoundNotification;
    private ServiceConnection notificationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            hasBoundNotification = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            hasBoundNotification = true;
            NotificationService.LocalBinder mLocalBinder = (NotificationService.LocalBinder)service;
            notificationService = mLocalBinder.getService();
            trackRoute();
        }
    };

    private ApiService apiService;
    private boolean hasBoundApiService;
    private ServiceConnection apiServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            hasBoundApiService = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            hasBoundApiService = true;
            ApiService.LocalBinder mLocalBinder = (ApiService.LocalBinder)service;
            apiService = mLocalBinder.getService();
            trackRoute();
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new TrackerService.LocalBinder();

    public class LocalBinder extends Binder {
        public TrackerService getService() {
            return TrackerService.this;
        }
    }

    @Override
    public void onCreate() {
        startService(new Intent(this, NotificationService.class));
        bindService(new Intent(this, NotificationService.class), notificationServiceConnection, BIND_AUTO_CREATE);

        startService(new Intent(this, ApiService.class));
        bindService(new Intent(this, ApiService.class), apiServiceConnection, BIND_AUTO_CREATE);

        trackedRoutes = new ArrayList<>();
        trackedRoutes.add(new TrackedRoute("Elisedal,Göteborg", "9021014002210000", "Valand,Göteborg", "9021014007220000", "4"));
        trackedRoutes.add(new TrackedRoute("Pilbågsgatan,Göteborg", "9021014005280000", "Vasaplatsen,Göteborg", "9021014007300000", "19"));
        //trackedRoutes.add(new TrackedRoute("Hjalmar Brantingsplatsen", "9021014003180000", "Vasaplatsen", "9021014007300000", "10"));
        trackedRouteIndex = 0;

        final IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        screenReceiver = new ScreenActionReceiver();
        registerReceiver(screenReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!hasBoundNotification ||
            !hasBoundApiService ||
            !intent.hasExtra("action") ||
            trackedRoutes.size() == 0) {
            return START_STICKY;
        }

        NotificationAction result = (NotificationAction)intent.getSerializableExtra("action");
        switch (result) {
            case PREVIOUS:
                trackedRouteIndex = trackedRouteIndex - 1 < 0 ? trackedRoutes.size() - 1 : trackedRouteIndex - 1;
                trackRoute();
                break;
            case NEXT:
                trackedRouteIndex = (trackedRouteIndex + 1) % trackedRoutes.size();
                trackRoute();
                break;
            case FLIP:
                flipRoute = !flipRoute;
                trackRoute();
                break;
            case UPDATE:
                trackRoute();
                break;
            default:
                break;
        }
        return START_STICKY;
    }

    public void startTracking(Route route) {
        TrackedRoute tmp = new TrackedRoute(route);
        if (!trackedRoutes.contains(tmp)) {
            trackedRoutes.add(tmp);
            updateLocalStorage();
        }
    }

    public void stopTracking(Route route) {
        for (Route trackedRoute : trackedRoutes) {
            if (route.equals(trackedRoute)) {
                trackedRoutes.remove(trackedRoute);
                updateLocalStorage();
                break;
            }
        }
    }

    private void updateLocalStorage() {
        JSONArray jsonArray = new JSONArray();
        try {
            for (TrackedRoute trackedRoute : trackedRoutes) {
                jsonArray.put(trackedRoute.toJsonObject());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_REF, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREFS_NAME, jsonArray.toString());
        editor.commit();
    }

    private void trackRoute() {
        if (!hasBoundNotification ||
            !hasBoundApiService ||
            trackedRoutes.isEmpty() ||
            trackedRoutes.size() <= trackedRouteIndex) {
            return;
        }

        TrackedRoute route = trackedRoutes.get(trackedRouteIndex);
        final String fromStopId = flipRoute ? route.toStopId : route.fromStopId;
        final String toStopId = flipRoute ? route.fromStopId : route.toStopId;

        apiService.fetchDepartures(fromStopId, toStopId,
                new ApiService.DeparturesRequest() {
                    @Override
                    public void onRequestCompleted(List<Departure> departures) {
                        route.getUpComingDepartures().clear();

                        int index = 0;
                        Iterator<Departure> iterator = departures.iterator();
                        while(index < 2 && iterator.hasNext()) {
                            Departure nextDeparture = iterator.next();
                            if (route.tracks(nextDeparture)) {
                                route.getUpComingDepartures().add(nextDeparture);
                                index++;
                            }
                        }

                        notificationService.showNotification(route, flipRoute, true);
                    }

                    @Override
                    public void onRequestFailed() {
                        notificationService.showNotification(route, flipRoute, false);
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(notificationServiceConnection);
        unbindService(apiServiceConnection);
        unregisterReceiver(screenReceiver);
    }
}
