package Core.Commands.Destiny.User;

import Core.Commands.Destiny.User.Objects.AccessToken;
import Core.Main.Logging;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DestinyTokenUtils
{
	public static void refreshToken(AccessToken token)
	{
		List<NameValuePair> params = new ArrayList<>(6);
		params.add(new BasicNameValuePair("client_id", DestinyRegisterHTTPHandler.BOT_ID));
		params.add(new BasicNameValuePair("X-API-Key", DestinyRegisterHTTPHandler.API_KEY));
		params.add(new BasicNameValuePair("grant_type", "refresh_token"));
		params.add(new BasicNameValuePair("refresh_token", token.refreshToken));
		params.add(new BasicNameValuePair("Authorization", token.getAuthorization()));
		params.add(new BasicNameValuePair("client_secret", DestinyRegisterHTTPHandler.CLIENT_SECRET));
		
		try {
			JSONObject object = DestinyRegisterHTTPHandler.getFormRequest("https://www.bungie.net/Platform/App/OAuth/Token/", params);
			
			if (object != null) {
				token.token = object.getString("access_token");
				token.refreshToken = object.getString("refresh_token");
				
				token.expiryDate = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(object.getInt("expires_in"), TimeUnit.SECONDS);
				token.refreshExpiry = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(object.getInt("refresh_expires_in"), TimeUnit.SECONDS);
				
				token.membershipId = object.getString("membership_id");
				token.tokenType = object.getString("token_type");
			}
			
		} catch (IOException e) {
			Logging.exception(e);
		}
	}
}
