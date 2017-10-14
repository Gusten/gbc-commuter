package pub.gusten.gbgcommuter.models;

import com.google.gson.annotations.SerializedName;

public class AccessTokenResponse {
    private String scope;
    @SerializedName("token_type")
    private String tokenType;
    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("expires_in")
    private int expiresIn;

    public String getAccessToken() {
        return accessToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }
}
