package Core.Commands.Destiny.system;

import Core.Commands.Destiny.Destiny2RegisterCommand;
import Core.Main.Logging;
import Core.Objects.Annotation.Method.EventListener;
import Core.Objects.Annotation.Method.Startup.Init;
import Core.Objects.Events.BotCloseEvent;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import static Core.Commands.Destiny.system.DestinyUpdateSystem.infoFile_d2;


public class DestinySystem
{
	public static final String BASE_RESOURCE_URL = "https://www.bungie.net";

	public static final String SOLAR_ICON =     "<:solar:484086950605750292>";
	public static final String ARC_ICON =       "<:arc:484087305636675584>";
	public static final String VOID_ICON =      "<:void:484087178201268224>";
	public static final String STASIS_ICON =    "<:stasis:847549825733623959>";
	public static final String STRAND_ICON =    "<:strand:1179146147852468344>";

	public static final String HUNTER_ICON =    "<:hunter:847550499259416626>";
	public static final String WARLOCK_ICON =   "<:warlock:847550499637297193>";
	public static final String TITAN_ICON =     "<:titan:847550499132538920>";

	public static final String KINETIC_ICON =   "<:kinetic:484087428773183566>";
	public static final String PRIMARY_ICON =   "<:primaryIcon:484414913717207042>";
	public static final String SPECIAL_ICON =   "<:specialIcon:484414914308472842>";
	public static final String HEAVY_ICON =     "<:heavyIcon:484414914174124032>";

	public static final String BAR_FULL_ICON =  "<:barFull:725358666903191633>";
	public static final String BAR_EMPTY_ICON = "<:barEmpty:725358461617176586>";

	public static String getIcon(String key)
	{
		return switch (key.toLowerCase()) {
			case "solar" -> SOLAR_ICON;
			case "arc" -> ARC_ICON;
			case "void" -> VOID_ICON;
			case "kinetic" -> KINETIC_ICON;
			case "stasis" -> STASIS_ICON;
			case "strand" -> STRAND_ICON;
			default -> "";
		};
		
	}

	public static String getClassIcon(String key)
	{
		return switch (key.toLowerCase()) {
			case "titan" -> TITAN_ICON;
			case "hunter" -> HUNTER_ICON;
			case "warlock" -> WARLOCK_ICON;
			default -> "";
		};
		
	}


	public static JSONObject getResponse(String URL) throws IOException
	{
		URL obj = new URL(URL);
		HttpURLConnection con = (HttpURLConnection)obj.openConnection();

		con.setRequestMethod("GET");
		con.setRequestProperty("X-API-KEY", Destiny2RegisterCommand.PUBLIC_API_KEY);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}

		in.close();
		return (JSONObject)new JSONTokener(response.toString()).nextValue();
	}

	private static GenericObjectPool<?> gPool2;
	private static PoolingDataSource ds2 = null;

	@Init
	public static void init()
	{
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			Logging.exception(e);
		}

		gPool2 = new GenericObjectPool<>();

		if (infoFile_d2 == null) {
			DestinyUpdateSystem.DestinyItemCommandInit();
		}

		String urlPrefix = "jdbc:sqlite:";

		ConnectionFactory cf2 = new DriverManagerConnectionFactory(
				urlPrefix + DestinyUpdateSystem.infoFile_d2.getAbsolutePath().replace("\\", "/"), "", "");
		PoolableConnectionFactory pcf2 = new PoolableConnectionFactory(cf2, gPool2, null, null, false, true);

		ds2 = new PoolingDataSource(gPool2);
	}

	@EventListener
	public static void onBotClose(BotCloseEvent e){
		try {
			gPool2.close();
		} catch (Exception exception) {
			Logging.exception(exception);
		}
	}

	public static Connection connect(File fe)
	{
		PoolingDataSource source = getSource(fe);

		if (source != null) {
			Connection con;

			try {
				con = source.getConnection();
				return con;
			} catch (SQLException e) {
				Logging.exception(e);
			}
		}

		return null;
	}

	public static PoolingDataSource getSource(File fe)
	{
		 if (fe == infoFile_d2) {
			return ds2;
		}

		return null;
	}


	public static Connection destiny_connect()
	{
		return connect(infoFile_d2);
	}
}