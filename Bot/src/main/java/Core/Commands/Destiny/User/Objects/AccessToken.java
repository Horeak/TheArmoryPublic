package Core.Commands.Destiny.User.Objects;

import org.json.JSONObject;

public class AccessToken
{
	public String token;
	public String refreshToken;
	public String membershipId;
	public String tokenType;
	public Long expiryDate;
	public Long refreshExpiry;
	
	public boolean isExpired()
	{
		return System.currentTimeMillis() >= expiryDate;
	}
	public boolean isRefreshValid()
	{
		return System.currentTimeMillis() < refreshExpiry;
	}
	public String getAuthorization()
	{
		return tokenType + " " + token;
	}
	
	public AccessToken(JSONObject object)
	{
		token = object.getString("access_token");
		refreshToken = object.getString("refresh_token");
		
		expiryDate = System.currentTimeMillis() + object.getInt("expires_in") * 1000;
		refreshExpiry = System.currentTimeMillis() + object.getInt("refresh_expires_in") * 1000;
		
		membershipId = object.getString("membership_id");
		tokenType = object.getString("token_type");
	}
}
