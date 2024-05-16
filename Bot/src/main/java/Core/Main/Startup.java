package Core.Main;

import Core.Commands.Voice.LavaLinkClient;
import Core.Data.DataHandler;
import Core.Objects.Annotation.Fields.JsonExclude;
import Core.Objects.Annotation.Method.Startup.Init;
import Core.Objects.Annotation.Method.Startup.PostInit;
import Core.Objects.Annotation.Method.Startup.PreInit;
import Core.Objects.Events.BotCloseEvent;
import Core.Util.CustomReflections;
import Core.Util.FileUtil;
import Core.Util.ReflectionUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.*;
import com.mashape.unirest.http.Unirest;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import org.apache.commons.io.FileUtils;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import javax.management.ReflectionException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class Startup
{
	public static final Long startTime = System.currentTimeMillis();

	private static JDA discordClient;
	
	public static String FilePath = "";
	public static File baseFilePath;
	public static URL launchDir = null;
	public static File tempFolder = null;
	public static final List<DiscordLocale> LANGS = List.of(DiscordLocale.ENGLISH_US);
	private static String filePath;

	public static final String folder = "discordData";

	public static final boolean USE_LAVA_LINK = true;

	public static boolean debug = false;
	public static boolean jarFile = false;

	public static boolean preInit = false;
	public static boolean init = false;
	public static boolean initListeners = false;
	
	public static ApplicationInfo appInfo = null;

	private static CustomReflections customReflections;

	public static final ExecutorService executor = Executors.newCachedThreadPool();
	public static final ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor(128);
	
	@Getter
	private static Gson GSON;
	
	public static Integer CLIENT_SHARDS = 10;
	
	//TODO Implement sharding eventually
	public static JDA getClient(){
//		int currentShardId = 0; //Get shard id from current executing thread
//		return discordClient.getShardManager().getShardById(currentShardId);
		return discordClient;
	}
	
	public static void main(String[] args)
	{
		scheduledExecutor.setKeepAliveTime(10, TimeUnit.SECONDS);
		scheduledExecutor.allowCoreThreadTimeOut(true);

		System.setProperty("java.awt.headless", "true");

		debug = System.getenv("DEBUG") != null && Boolean.parseBoolean(System.getenv("DEBUG"));
		filePath = System.getenv("FILE_PATH");
		
		if(filePath != null && filePath.contains("..")){
			System.out.println("Invalid file path");
			System.exit(0);
		}
		
		System.out.println("debug: " + debug);
		
		GSON = new GsonBuilder().serializeNulls().addSerializationExclusionStrategy(new ExclusionStrategy()
		{
			public boolean shouldSkipField(FieldAttributes field)
			{
				return field.getAnnotation(JsonExclude.class) != null;
			}
			public boolean shouldSkipClass(Class<?> clazz)
			{
				return clazz.getAnnotation(JsonExclude.class) != null;
			}
		}).setPrettyPrinting().create();
		
		Unirest.setTimeouts(0, 0);
		
		try {
			initReflection(false);
			System.out.println("jarFile: " + jarFile);
		} catch (IOException | URISyntaxException e) {
			Logging.exception(e);
		}
		
		try {
			initFile();
			
			if(!jarFile){
				File secrets = new File(Startup.baseFilePath + "/secrets.json");
				populateEnvVariables(GSON.fromJson(FileUtils.readFileToString(secrets, Charset.defaultCharset()), JsonElement.class), "");
			}
			
			initReflection(true);

			System.out.println("Starting bot with launch dir: \""  + baseFilePath + "\"");

			initBot();
			init();
			postInit();
		} catch (Exception e) {
			Logging.exception(e);
		}
	}
	
	private static void populateEnvVariables(JsonElement jsonElement, String prefix) {
		if (jsonElement.isJsonObject()) {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			
			for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
				String key = entry.getKey();
				JsonElement value = entry.getValue();
				
				String newPrefix = prefix.isEmpty() ? key.toUpperCase() : prefix + "_" + key.toUpperCase();
				
				if (value.isJsonObject()) {
					populateEnvVariables(value, newPrefix);
				} else {
					values.put(newPrefix, value.getAsString());
				}
			}
		}
	}
	
	private static final HashMap<String, String> values = new HashMap<>();
	
	public static String getEnvValue(String key){
		var formattedKey = key.replace(":", "_").toUpperCase();
		var value = values.getOrDefault(formattedKey, null);
		
		if(value == null){
			return System.getenv(formattedKey);
		}
		
		return value;
	}

	public static void initReflection(boolean preinit) throws URISyntaxException, MalformedURLException
	{
		File fe = new File(Startup.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		jarFile = fe.exists() && fe.isFile();

		URL url = new URL("file:" + (jarFile ? fe.getPath() : System.getProperty("user.dir").replace("\\", "/")));
		Predicate<String> filter = new FilterBuilder().includePackage("Core");

		launchDir = url;

		if (preinit) {
			preInit(url, filter);
		}

		ConfigurationBuilder builder = new ConfigurationBuilder();

		builder.setInputsFilter(filter::test);
		builder.setUrls(url);
		builder.forPackages(Startup.class.getPackage().getName());
		builder.setScanners(new FieldAnnotationsScanner(), new TypeAnnotationsScanner(), new MethodAnnotationsScanner(),
		                    new SubTypesScanner());

		customReflections = new CustomReflections(builder);
	}

	private static void preInit(URL url, Predicate<String> filter)
	{
		try {
			ConfigurationBuilder builder = new ConfigurationBuilder();

			builder.filterInputsBy(filter::test);
			builder.setUrls(url);
			builder.forPackages(Startup.class.getPackage().getName());
			builder.setScanners(new MethodAnnotationsScanner());

			CustomReflections reflections = new CustomReflections(builder);
			Set<Method> methods = reflections.getMethodsAnnotatedWith(PreInit.class);

			System.out.println("Found " + methods.size() + " preInit method" + (methods.size() > 1 ? "s" : "") + "!");

			for (Method ob : methods) {
				if(ob == null) continue;

				if (!ob.canAccess(null)) {
					ob.setAccessible(true);
				}

				if (!Modifier.isStatic(ob.getModifiers())) {
					System.err.println("preInit method: " + ob + " is not static!");
					continue;
				}

				try {
					ob.invoke(null);
				} catch (Exception e) {
					System.err.println("Error on preInit method: " + ob + "!");
					Logging.exception(e);
				}
			}

			System.out.println("preInit done.");
		} catch (Exception e) {
			if (!(e instanceof ReflectionException)) {
				Logging.exception(e);
			}
		}

		preInit = true;
	}

	private static void initFile() throws IOException
	{
		baseFilePath = new File(System.getProperty("user.dir") + File.separator);
		
		String fe = filePath == null ? baseFilePath.getPath() : filePath;

		if (filePath != null) {
			System.out.println("Custom filepath given: " + filePath);
		}

		FilePath = FileUtil.getFolder(fe + File.separator + folder + File.separator).getCanonicalPath();
		tempFolder = FileUtil.getFolder(Startup.FilePath + "/tmp/");

		Logging.activate();
		DataHandler.init(FilePath);

		System.out.println("File init done.");
	}
	
	private static void init()
	{
		ReflectionUtils.invokeMethods(Init.class);
		System.out.println("Init done.");
		init = true;
	}

	private static void postInit()
	{
		ReflectionUtils.invokeMethods(PostInit.class);
		System.out.println("PostInit done.");
	}

	@PostInit
	public static void initAppInfo(){
		appInfo = discordClient.retrieveApplicationInfo().complete();
	}

	@JsonIgnore
	private static void initBot()
	{
		String key = "discordToken:" + (debug ? "debug" : "production");
		String token = getEnvValue(key);

		if (token == null) {
			System.err.println("Invalid bot token!");
			System.exit(0);
		}

		JDABuilder builder = JDABuilder.createDefault(token);
		builder.setChunkingFilter(ChunkingFilter.NONE);
		builder.setAutoReconnect(true);

		//LavaLink stuff
		if(LavaLinkClient.init) {
			builder.addEventListeners(LavaLinkClient.lavalink);
			builder.setVoiceDispatchInterceptor(LavaLinkClient.lavalink.getVoiceInterceptor());
		}else{
			System.err.println("LavaLink failed Init!");
		}

		discordClient = builder.build().setRequiredScopes("applications.commands");
		System.out.println("Bot init done.");
	}

	public static void onBotClose()
	{
		System.err.println("Shutting down bot!");
		discordClient.getEventManager().handle(new BotCloseEvent(discordClient));

		try {
			FileUtils.deleteDirectory(tempFolder);
		} catch (IOException e) {
			Logging.exception(e);
		}


		discordClient = null;
	}
	
	public static Reflections getReflection()
	{
		return customReflections;
	}
}