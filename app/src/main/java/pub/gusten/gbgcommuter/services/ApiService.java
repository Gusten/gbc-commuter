package pub.gusten.gbgcommuter.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pub.gusten.gbgcommuter.R;
import pub.gusten.gbgcommuter.helpers.DateUtils;
import pub.gusten.gbgcommuter.models.AccessTokenResponse;
import pub.gusten.gbgcommuter.models.departures.Departure;
import pub.gusten.gbgcommuter.models.departures.DepartureBoardResponse;
import pub.gusten.gbgcommuter.models.trips.Trip;
import pub.gusten.gbgcommuter.models.trips.TripRequestResponse;

public class ApiService extends Service {

    public interface DeparturesRequest {
        void onRequestCompleted(List<Departure> departures);
        void onRequestFailed(String error);
    }
    public interface TripRequest {
        void onRequestCompleted(List<Trip> trips);
        void onRequestFailed(String error);
    }
    private interface AccessTokenCallback {
        void onRequestCompleted();
        void onRequestFailed();
    }

    private final String TOKEN_URL = "https://api.vasttrafik.se:443/token";
    private final String DEPARTURE_URL = "https://api.vasttrafik.se/bin/rest.exe/v2/departureBoard";
    private final String TRIP_URL = "https://api.vasttrafik.se/bin/rest.exe/v2/trip";

    private String authStr;
    private String accessToken;
    private Instant tokenExpirationDate;
    private Gson gson;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new ApiService.LocalBinder();

    public class LocalBinder extends Binder {
        public ApiService getService() {
            return ApiService.this;
        }
    }

    @Override
    public void onCreate() {
        authStr = getResources().getString(R.string.vasttrafik_key) + ":"
                + getResources().getString(R.string.vasttrafik_secret);
        authStr = Base64.encodeToString(authStr.getBytes(), Base64.DEFAULT);
        tokenExpirationDate = Instant.now();
        fetchAccessToken(new AccessTokenCallback() {
            @Override
            public void onRequestCompleted() {}

            @Override
            public void onRequestFailed() {}
        });

        gson = new Gson();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void fetchAccessToken(final AccessTokenCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(
            Request.Method.POST,
                TOKEN_URL,
            response -> {
                AccessTokenResponse tokenResponse = gson.fromJson(response, AccessTokenResponse.class);
                accessToken = tokenResponse.getAccessToken();
                tokenExpirationDate = Instant.ofEpochSecond(System.currentTimeMillis()/1000 + tokenResponse.getExpiresIn());
                callback.onRequestCompleted();
            },
            error -> callback.onRequestFailed())
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Basic " + authStr);
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("grant_type", "client_credentials");
                params.put("scope", "1");
                return params;
            }
        };
        queue.add(stringRequest);
    }

    public void fetchDepartures(final long fromStopId, final long toStopId, final DeparturesRequest callback) {
        // Check if we need to refresh access token
        if(Instant.now().isAfter(tokenExpirationDate)) {
            fetchAccessToken(new AccessTokenCallback() {
                @Override
                public void onRequestCompleted() {
                    try {
                        mFetchDepartures(fromStopId, toStopId, callback);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onRequestFailed() {
                    callback.onRequestFailed("Failed fetching access token");
                }
            });
        }
        else {
            try {
                mFetchDepartures(fromStopId, toStopId, callback);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private void mFetchDepartures(final long fromStopId, final long toStopId, final DeparturesRequest callback) throws UnsupportedEncodingException {
        final LocalDateTime now = LocalDateTime.now();
        String requestQuery = "?id=" + fromStopId +
                "&date=" + now.format(DateUtils.dateOnlyFormatter) +
                "&time=" + now.format(DateUtils.timeOnlyFormatter) +
                "&direction=" + toStopId +
                "&format=json";

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                DEPARTURE_URL + requestQuery,
                response -> {
                    DepartureBoardResponse boardResponse = gson.fromJson(response, DepartureBoardResponse.class);

                    // Check if error
                    if (boardResponse.hasError()) {
                        callback.onRequestFailed(boardResponse.getErrorText());
                        return;
                    }

                    List<Departure> departures = new ArrayList<>();
                    for (Departure departure: boardResponse.getDepartures()) {
                        // Sometimes old objects sneaks through. Throw em away
                        if (departure.getDepartureDateTime().isBefore(now)) {
                            continue;
                        }
                        departures.add(departure);
                    }
                    callback.onRequestCompleted(departures);
                },
                error -> callback.onRequestFailed("Could not connect to Västtrafik"))
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
        };
        queue.add(stringRequest);
    }

    public void fetchTrips(final long fromStopId, final long toStopId, final TripRequest callback) {
        // Check if we need to refresh access token
        if(Instant.now().isAfter(tokenExpirationDate)) {
            fetchAccessToken(new AccessTokenCallback() {
                @Override
                public void onRequestCompleted() {
                    try {
                        mFetchTrips(fromStopId, toStopId, true, true, true, callback);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onRequestFailed() {
                    callback.onRequestFailed("Failed fetching access token");
                }
            });
        }
        else {
            try {
                mFetchTrips(fromStopId, toStopId, true, true, true,callback);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private void mFetchTrips(final long fromStopId, final long toStopId, final boolean allowTrams, final boolean allowBuses, final boolean allowBoats, final TripRequest callback) throws UnsupportedEncodingException {
        String requestQuery =
            "?originId=" + fromStopId +
            "&destId=" + toStopId +
            "&useTram=" + ((allowTrams) ? 1 : 0) +
            "&useBus=" + ((allowBuses) ? 1 : 0) +
            "&useBoat=" + ((allowBoats) ? 1 : 0) +
            "&originWalk=0" +
            "&format=json";

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,TRIP_URL + requestQuery,
                response -> {
                    LocalDateTime now = LocalDateTime.now();
                    TripRequestResponse responseObj = gson.fromJson(response, TripRequestResponse.class);

                    // Check if error
                    if (responseObj.hasError()) {
                        callback.onRequestFailed(responseObj.getErrorText());
                        return;
                    }

                    List<Trip> trips = new ArrayList<>();
                    for (Trip trip: responseObj.getTrips()) {
                        // Sometimes old objects sneaks through. Throw em away
                        if (trip.getDepartureDateTime().isBefore(now)) {
                            continue;
                        }
                        trips.add(trip);
                    }
                    callback.onRequestCompleted(trips);
                },
                error -> callback.onRequestFailed("Could not connect to Västtrafik"))
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
        };
        queue.add(stringRequest);
    }
}
