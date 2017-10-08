package pub.gusten.gbgcommuter;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NetworkManager {

    private final String tokenUrl ="https://api.vasttrafik.se:443/token";

    private Context context;
    private String accessToken;
    private long expiresAt;

    public NetworkManager(Context context) {
        this.context = context;
        fetchAccessToken();
    }

    private void fetchAccessToken() {
        RequestQueue queue = Volley.newRequestQueue(context);

        String authStr = "";
        authStr = Base64.encodeToString(authStr.getBytes(), Base64.DEFAULT);

        Log.i("NetworkManager", authStr);
        final String finalAuthStr = authStr;
        StringRequest stringRequest = new StringRequest(
            Request.Method.POST,
            tokenUrl,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject res = new JSONObject(response.substring(0));
                        accessToken = res.getString("access_token");
                        expiresAt = System.currentTimeMillis()/1000 + res.getLong("expires_in");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Basic " + finalAuthStr);
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
}
