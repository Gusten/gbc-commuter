package pub.gusten.gbgcommuter;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pub.gusten.gbgcommuter.R;
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
    private long expiresAt;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    public NetworkManager(Context context) {
        this.context = context;
        authStr = context.getResources().getString(R.string.vasttrafik_key) + ":"
                + context.getResources().getString(R.string.vasttrafik_secret);
        authStr = Base64.encodeToString(authStr.getBytes(), Base64.DEFAULT);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        timeFormat = new SimpleDateFormat("HH:mm");
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
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject res = new JSONObject(response);
                        accessToken = res.getString("access_token");
                        expiresAt = System.currentTimeMillis()/1000 + res.getLong("expires_in");
                        callback.onRequestCompleted();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onRequestFailed();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    callback.onRequestFailed();
                }
        }) {
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

    public void fetchDepartures(final String stopId, final String direction, final DeparturesRequest callback) {
        if (System.currentTimeMillis()/1000 >= expiresAt) {
            fetchAccessToken(new AccessTokenCallback() {
                @Override
                public void onRequestCompleted() {
                    try {
                        mFetchDepartures(stopId, direction, callback);
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
                mFetchDepartures(stopId, direction, callback);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private void mFetchDepartures(String stopId, String direction, final DeparturesRequest callback) throws UnsupportedEncodingException {
        Calendar calendar = Calendar.getInstance();
        String requestQuery = "?id=" + stopId +
                "&date=" + dateFormat.format(calendar.getTime()) +
                "&time=" + timeFormat.format(calendar.getTime()) +
                "&direction=" + direction +
                "&format=json";

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                departureUrl + requestQuery,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            List<Departure> departures = new ArrayList<>();
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONObject("DepartureBoard").getJSONArray("Departure");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                departures.add(new Departure(jsonArray.getJSONObject(i)));
                            }
                            callback.onRequestCompleted(departures);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callback.onRequestFailed();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onRequestFailed();
                    }
                }) {
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
