package Core.Commands.Destiny.User;

import Core.Commands.Destiny.Destiny2RegisterCommand;
import Core.Commands.Destiny.User.Objects.AccessToken;
import Core.Commands.Destiny.User.Objects.Destiny2RegisterObject;
import Core.Commands.Destiny.User.Objects.UserAccount;
import Core.Main.Logging;
import Core.Util.Utils;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.dv8tion.jda.api.entities.User;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DestinyRegisterHTTPHandler implements HttpHandler
{
	public static String BOT_ID = null;
	public static String CLIENT_SECRET = null;
	public static String API_KEY = null;
	
	public static JSONObject getFormRequest(String url, List<NameValuePair> list) throws IOException
	{
		try(CloseableHttpClient httpClient = HttpClients.createDefault()){
			HttpPost httpPost = new HttpPost(url);
			
			httpPost.setEntity(new UrlEncodedFormEntity(list, "UTF-8"));
			
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			
			if (entity != null) {
				return getJsonObject(entity.getContent());
			}
		}catch (Exception e){
			Logging.exception(e);
		}
		
		return null;
	}
	
	public static JSONObject getJsonObject(InputStream stream) throws IOException
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));
		String inputLine;
		StringBuilder response1 = new StringBuilder();
		
		while ((inputLine = in.readLine()) != null) {
			response1.append(inputLine);
		}
		
		in.close();
		
		Object t = new JSONTokener(response1.toString()).nextValue();
		
		if (t instanceof JSONObject object) {
			
			if (object.has("error") && object.has("error_description")) {
				System.err.println(object);
				return null;
			} else {
				return object;
			}
			
		} else {
			return null;
		}
		
	}
	
	public static JSONObject getUserAuthenticateObject(String auth) throws IOException
	{
		List<NameValuePair> params = new ArrayList<>(3);
		params.add(new BasicNameValuePair("client_id", BOT_ID));
		params.add(new BasicNameValuePair("grant_type", "authorization_code"));
		params.add(new BasicNameValuePair("code", auth));
		params.add(new BasicNameValuePair("client_secret", CLIENT_SECRET));
		
		return getFormRequest("https://www.bungie.net/Platform/App/OAuth/Token/", params);
	}
	
	public static JSONObject getUserObject(AccessToken accessToken)
	{
		if (accessToken.isExpired()) return null;
		
		HttpURLConnection con = null;
		
		try {
			URL obj = new URL("https://www.bungie.net/Platform/User/GetCurrentBungieNetUser/");
			con = (HttpURLConnection)obj.openConnection();
			
			con.setRequestMethod("GET");
			con.setRequestProperty("X-API-Key", API_KEY);
			con.setRequestProperty("Authorization", accessToken.getAuthorization());
			
			JSONObject object = getJsonObject(con.getInputStream());
			return getResponseObject(object);
		} catch (Exception e) {
			handleException(e, con);
		}
		
		return null;
	}
	
	public static JSONObject getResponseObject(JSONObject object){
		if (object != null) {
			if (object.has("ErrorCode")) {
				int errorCode = object.getInt("ErrorCode");
				
				if (errorCode != 1) {
					System.err.println("Error: " + object.getString("ErrorStatus"));
					System.out.println(object);
					return null;
				}
			}
			
			if (object.has("Response")) {
				return object.getJSONObject("Response");
			}
		}
		return null;
	}
	
	public static JSONObject handleException(Exception e,  HttpURLConnection con)
	{
		if (con != null) {
			if (con.getErrorStream() != null) {
				try {
					JSONObject object = getJsonObject(con.getErrorStream());
					
					System.err.println(e.getMessage());
					System.err.println(object);
					
					return object;
				} catch (Exception e1) {
					Logging.exception(e1);
				}
			} else {
				Logging.exception(e);
			}
		}
		
		return null;
	}
	
	@Override
	public void handle(HttpExchange t) throws IOException
	{
		URI uri = t.getRequestURI();
		String code = uri.getQuery();
		String state = null;
		
		if (code.contains("&")) {
			String[] j = code.split("&");
			
			for (String temp : j) {
				if (temp.startsWith("code=")) {
					code = temp.substring("code=".length());
					
				} else if (temp.startsWith("state=")) {
					state = temp.substring("state=".length());
				}
			}
		} else {
			code = code.substring("code=".length());
		}
		
		String response = Destiny2RegisterCommand.getHTML("There was an error linking the account!",
		                                                  "Please try again.");
		
		if (state == null) {
			response = Destiny2RegisterCommand.getHTML("There was an error linking the account!", "Please try again.");
		}
		
		Destiny2RegisterObject ent = null;
		
		if (state != null) {
			UUID uuid = UUID.fromString(state);
			
			if (Destiny2RegisterCommand.auth.containsKey(uuid)) {
				ent = Destiny2RegisterCommand.auth.get(uuid);
				
				if (System.currentTimeMillis() >= ent.time) {
					Destiny2RegisterCommand.auth.remove(uuid);
					Destiny2RegisterCommand.updateMessage(ent);
				}
			}
		}
		
		
		if (ent == null) {
			response = Destiny2RegisterCommand.getHTML("The link process timed out!", "Please try again.");
		}
		
		
		if (ent != null) {
			User userObject = Utils.getUser(ent.userId);
			
			if (userObject != null) {
				AccessToken accessToken = Destiny2UserUtil.getOrCreateAccessToken(userObject, code);
				
				if (accessToken != null) {
					String dUsername = Destiny2UserUtil.getUserTag(accessToken);
					ArrayList<UserAccount> accounts = Destiny2UserUtil.getUserAccounts(accessToken);
					Destiny2RegisterCommand.finalMessage(ent, dUsername, accounts, userObject.getName());
					
					response = Destiny2RegisterCommand.getHTML("Your account has now been linked!",
					                                           "Registered with the Bungie account \"" + dUsername + "\"",
					                                           "You can safely close this window.");
				}
			}
		}
		
		
		Headers h = t.getResponseHeaders();
		h.set("Content-Type", "text/html");
		t.sendResponseHeaders(200, response.length());
		
		OutputStream os = t.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}
}