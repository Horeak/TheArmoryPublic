package Core.Commands.Destiny.User;

import Core.Commands.Destiny.User.Objects.AccessToken;
import Core.Commands.Destiny.User.Objects.UserAccount;
import Core.Main.Logging;
import Core.Objects.Annotation.Fields.Save;
import Core.Objects.Annotation.Method.Interval;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Destiny2UserUtil
{
	@Save("destiny/destiny_user_ids.json")
	public static ConcurrentHashMap<Long, AccessToken> accessTokens = new ConcurrentHashMap<>();

	@Interval(time_interval = 2, initial_delay = 1, time_unit = TimeUnit.DAYS)
	public static void refreshTokens(){
		for(AccessToken token : accessTokens.values()){
			if(token.isRefreshValid() && (token.isExpired() || (token.refreshExpiry - System.currentTimeMillis() <= TimeUnit.MILLISECONDS.convert(3, TimeUnit.DAYS)))){
				DestinyTokenUtils.refreshToken(token);
			}
		}
	}

	public static final String BASE_BUNGIE_URL = "https://www.bungie.net";
	public static final String GET_MEMBERSHIP_PATH = "/Platform/User/GetMembershipsForCurrentUser/";
	
	public static ArrayList<UserAccount> getUserAccounts(AccessToken accessToken)
	{
		JSONObject object = DestinyRegisterHTTPHandler.getUserObject(accessToken);
		ArrayList<UserAccount> list = new ArrayList<>();
		
		if(object == null) return list;
		
		if (object.has("steamDisplayName")) {
			list.add(new UserAccount("Steam", object.getString("steamDisplayName")));
		}

		if (object.has("psnDisplayName")) {
			list.add(new UserAccount("Xbox", object.getString("psnDisplayName")));
		}

		if (object.has("xboxDisplayName")) {
			list.add(new UserAccount("Playstation", object.getString("xboxDisplayName")));
		}

		return list;
	}


	public static String getUserTag(AccessToken accessToken)
	{
		JSONObject object = DestinyRegisterHTTPHandler.getUserObject(accessToken);

		if (object != null) {
			if (object.has("displayName")) {
				return object.getString("displayName");
			}
		}

		return null;
	}
	
	
	public static JSONObject postData( AccessToken accessToken, String path, JSONObject data)
	{
		if (accessToken == null) return null;

		HttpURLConnection con = null;
		try {
			URL obj = new URL(BASE_BUNGIE_URL + path);
			con = (HttpURLConnection)obj.openConnection();
			con.setRequestMethod("POST");

			con.setRequestProperty("Authorization", accessToken.tokenType + " " + accessToken.token);
			con.setRequestProperty("X-API-Key", DestinyRegisterHTTPHandler.API_KEY);

			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setRequestProperty("Accept", "application/json");

			con.setDoOutput(true);

			try(OutputStream os = con.getOutputStream()) {
				byte[] input = data.toString().getBytes(StandardCharsets.UTF_8);
				os.write(input, 0, input.length);
			}

			JSONObject t = DestinyRegisterHTTPHandler.getJsonObject(con.getInputStream());

			if (t != null) {
				if (t.has("ErrorCode")) {
					int errorCode = t.getInt("ErrorCode");
					
					if (errorCode != 1) {
						System.err.println("Error: " + t.getString("ErrorStatus"));
						System.out.println(t);
						return null;
					}
				}
				
				return t;
			}

		} catch (IOException e) {
			return DestinyRegisterHTTPHandler.handleException(e, con);
		}

		return null;
	}

	public static JSONObject getData( AccessToken accessToken, String path)
	{
		if (accessToken == null) return null;

		HttpURLConnection con = null;
		try {
			URL obj = new URL(BASE_BUNGIE_URL + path);
			con = (HttpURLConnection)obj.openConnection();

			con.setRequestMethod("GET");
			con.setRequestProperty("Authorization", accessToken.tokenType + " " + accessToken.token);
			con.setRequestProperty("X-API-Key", DestinyRegisterHTTPHandler.API_KEY);

			JSONObject t = DestinyRegisterHTTPHandler.getJsonObject(con.getInputStream());
			return DestinyRegisterHTTPHandler.getResponseObject(t);


		} catch (IOException e) {
			return DestinyRegisterHTTPHandler.handleException(e, con);
		}
	}

	public static AccessToken getOrCreateAccessToken(User user, String auth)
	{
		AccessToken token = getAccessToken(user);

		if (token != null && !token.isExpired()) {
			return token;
		}

		try {
			JSONObject object = DestinyRegisterHTTPHandler.getUserAuthenticateObject(auth);

			if (object != null) {
				AccessToken token1 = new AccessToken(object);
				
				if (token1.token != null && !token1.isExpired()) {
					accessTokens.put(user.getIdLong(), token1);
					return token1;
				} else {
					System.err.println(object);
				}
			}

		} catch (IOException e) {
			Logging.exception(e);
		}

		return null;
	}

	public static AccessToken getAccessToken(User user)
	{
		return getAccessToken(user.getIdLong());
	}

	public static AccessToken getAccessToken(Long user)
	{
		AccessToken token = accessTokens.getOrDefault(user, null);

		if (token != null && token.isExpired()) {
			if (token.isRefreshValid()) {
				Long expiry = token.expiryDate;
				DestinyTokenUtils.refreshToken(token);

				if (expiry.equals(token.expiryDate)) {
					System.err.println("Token didnt refresh!");
				}
			} else {
				accessTokens.remove(user);
			}
		}

		return token;
	}
	
}