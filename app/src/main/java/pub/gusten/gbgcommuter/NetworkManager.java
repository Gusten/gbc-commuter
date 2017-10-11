package pub.gusten.gbgcommuter;

import android.content.Context;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pub.gusten.gbgcommuter.helpers.DateUtils;
import pub.gusten.gbgcommuter.models.Departure;

public class NetworkManager {

    public interface DeparturesRequest {
        void onRequestCompleted(List<Departure> departures);
        void onRequestFailed();
    }
    private interface AccessTokenCallback {
        void onRequestCompleted();
        void onRequestFailed();
    }

    private final String tokenUrl = "https://api.vasttrafik.se:443/token";
    private final String departureUrl = "https://api.vasttrafik.se/bin/rest.exe/v2/departureBoard";

    private Context context;
    private String authStr;
    private String accessToken;
    private Instant expirationDate;

    public NetworkManager(Context context) {
        this.context = context;
        authStr = context.getResources().getString(R.string.vasttrafik_key) + ":"
                + context.getResources().getString(R.string.vasttrafik_secret);
        authStr = Base64.encodeToString(authStr.getBytes(), Base64.DEFAULT);
        expirationDate = Instant.now();
        fetchAccessToken(new AccessTokenCallback() {
            @Override
            public void onRequestCompleted() {}

            @Override
            public void onRequestFailed() {}
        });
    }

    private void fetchAccessToken(final AccessTokenCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(
            Request.Method.POST,
            tokenUrl,
            response -> {
                try {
                    JSONObject res = new JSONObject(response);
                    accessToken = res.getString("access_token");
                    expirationDate = Instant.ofEpochSecond(System.currentTimeMillis()/1000 + res.getLong("expires_in"));
                    callback.onRequestCompleted();
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onRequestFailed();
                }
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

    public void fetchDepartures(final String fromStopId, final String toStopId, final DeparturesRequest callback) {
        // Check if we need to refresh access token
        if(Instant.now().isAfter(expirationDate)) {
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
                    callback.onRequestFailed();
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

    private void mFetchDepartures(String fromStopId, String toStopId, final DeparturesRequest callback) throws UnsupportedEncodingException {
        final LocalDateTime now = LocalDateTime.now();
        String requestQuery = "?id=" + fromStopId +
                "&date=" + now.format(DateUtils.dateOnlyFormatter) +
                "&time=" + now.format(DateUtils.timeOnlyFormatter) +
                "&direction=" + toStopId +
                "&format=json";

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                departureUrl + requestQuery,
                response -> {
                    try {
                        List<Departure> departures = new ArrayList<>();
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONObject("DepartureBoard").getJSONArray("Departure");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Departure tmp = new Departure(jsonArray.getJSONObject(i));
                            // Sometimes old objects sneaks through. Throw em away
                            if (tmp.getTimeInstant().isBefore(now)) {
                                continue;
                            }
                            departures.add(tmp);
                        }
                        callback.onRequestCompleted(departures);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onRequestFailed();
                    }
                },
                error -> callback.onRequestFailed())
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
