package Core.Commands.Destiny.system;

import Core.Commands.Destiny.Models.DestinySeasonObject;
import Core.Main.Logging;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class DestinySeasonSystem
{
	public static final ConcurrentHashMap<Long, Integer> destinySeasonNumbers = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<Integer, DestinySeasonObject> destinySeasons = new ConcurrentHashMap<>();
	public static final String SEASON_URL = "https://raw.githubusercontent.com/DestinyItemManager/d2-additional-info/master/data/seasons/seasons_unfiltered.json";
	public static final String SEASON_INFO_URL = "https://raw.githubusercontent.com/DestinyItemManager/d2-additional-info/dbeebd0f8ca1149c0561e34b8d879dfba7403d04/data/seasons/d2-season-info-static.ts";
	private static final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	
	static void generateDestinySeasons()
	{
		try (InputStream is = new URL(SEASON_URL).openStream()){
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			int cp;
			while ((cp = rd.read()) != -1) {
				sb.append((char)cp);
			}
			
			String jsonText = sb.toString();
			JSONObject json = new JSONObject(jsonText);
			
			for (String key : json.keySet()) {
				int season = json.getInt(key);
				Long keyNum = Long.parseLong(key);
				destinySeasonNumbers.put(keyNum, season);
			}
		} catch (IOException e) {
			Logging.exception(e);
		}
		
		try (InputStream is = new URL(SEASON_INFO_URL).openStream()) {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			int cp;
			while ((cp = rd.read()) != -1) {
				sb.append((char)cp);
			}
			
			String text = sb.toString();
			String[] seasons = text.split(": \\{");
			
			
			for (String t : seasons) {
				HashMap<String, String> values = new HashMap<>();
				String[] lines = t.split(",");
				
				for (String line : lines) {
					if (line.contains(":")) {
						String[] val = line.replace("\n", "").replace("\"", "").replace("'", "").split(":", 2);
						
						if (val.length == 2) {
							values.put(val[0].replace(" ", ""), val[1]);
						}
					}
				}
				
				if (values.containsKey("DLCName")) {
					DestinySeasonObject seasonObject = new DestinySeasonObject();
					
					if (values.containsKey("maxLevel")) {
						seasonObject.maxLevel = Integer.parseInt(values.get("maxLevel").replace(" ", ""));
					}
					
					if (values.containsKey("powerFloor")) {
						seasonObject.powerFloor = Integer.parseInt(values.get("powerFloor").replace(" ", ""));
					}
					
					if (values.containsKey("softCap")) {
						seasonObject.softCap = Integer.parseInt(values.get("softCap").replace(" ", ""));
					}
					
					if (values.containsKey("powerfulCap")) {
						seasonObject.powerfulCap = Integer.parseInt(values.get("powerfulCap").replace(" ", ""));
					}
					
					if (values.containsKey("pinnacleCap")) {
						seasonObject.pinnacleCap = Integer.parseInt(values.get("pinnacleCap").replace(" ", ""));
					}
					
					if (values.containsKey("year")) {
						seasonObject.year = Integer.parseInt(values.get("year").replace(" ", ""));
					}
					
					if (values.containsKey("season")) {
						seasonObject.season = Integer.parseInt(values.get("season").replace(" ", ""));
					}
					
					if (values.containsKey("releaseDate")) {
						String date = values.get("releaseDate").replace(" ", "");
						
						try {
							seasonObject.releaseDate = formatter.parse(date);
						} catch (ParseException e) {
							Logging.exception(e);
						}
						
					}
					
					if (values.containsKey("seasonName")) {
						seasonObject.seasonName = values.get("seasonName");
						
						if(seasonObject.seasonName != null){
							seasonObject.seasonName = seasonObject.seasonName.strip();
						}
					}
					
					if (values.containsKey("DLCName")) {
						seasonObject.DLCName = values.get("DLCName");
						
						if(seasonObject.DLCName != null){
							seasonObject.DLCName = seasonObject.DLCName.strip();
						}
					}
					
					destinySeasons.put(seasonObject.season, seasonObject);
				}
			}
			
		} catch (IOException e) {
			Logging.exception(e);
		}
	}
}
