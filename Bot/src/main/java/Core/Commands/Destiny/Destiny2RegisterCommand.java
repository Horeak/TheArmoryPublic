package Core.Commands.Destiny;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Commands.Destiny.User.Objects.Destiny2RegisterObject;
import Core.Commands.Destiny.User.Objects.UserAccount;
import Core.Commands.Destiny.User.DestinyRegisterHTTPHandler;
import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Method.Interval;
import Core.Objects.Annotation.Method.Startup.Init;
import Core.Objects.BotChannel;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

//TODO This is set to a debug command until more work is done
@Command
public class Destiny2RegisterCommand implements ISlashCommand
{
	public static final String PUBLIC_API_KEY = Startup.getEnvValue("destiny:public_api_key");
	public static final String PUBLIC_BOT_ID = Startup.getEnvValue("destiny:public_bot_id");
	private static final String PUBLIC_CLIENT_SECRET = Startup.getEnvValue("destiny:public_client_secret");

	public static final ConcurrentHashMap<UUID, Destiny2RegisterObject> auth = new ConcurrentHashMap<>();

	private static final String TEST_API_KEY = Startup.getEnvValue("destiny:test_api_key");
	private static final String TEST_BOT_ID = Startup.getEnvValue("destiny:test_bot_id");
	private static final String TEST_CLIENT_SECRET = Startup.getEnvValue("destiny:test_client_secret");

	@Init
	public static void init()
	{
		DestinyRegisterHTTPHandler.BOT_ID = Startup.debug ? TEST_BOT_ID : PUBLIC_BOT_ID;
		DestinyRegisterHTTPHandler.CLIENT_SECRET = Startup.debug ? TEST_CLIENT_SECRET : PUBLIC_CLIENT_SECRET;
		DestinyRegisterHTTPHandler.API_KEY = Startup.debug ? TEST_API_KEY : PUBLIC_API_KEY;

		try {
			InetSocketAddress address = new InetSocketAddress(8000);
			HttpsServer server = HttpsServer.create(address, 0);
			SSLContext sslContext = SSLContext.getInstance("TLSv1.3");

			String pass = Startup.getEnvValue("ssl");
			char[] password = pass.toCharArray();

			File fe = new File(Startup.baseFilePath + "/keys.jks");

			KeyStore ks = KeyStore.getInstance("JKS");
			FileInputStream fis = new FileInputStream(fe);
			ks.load(fis, password);

			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, password);

			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ks);

			// setup the HTTPS context and parameters
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
				public void configure(HttpsParameters params) {
					try {
						// initialise the SSL context
						SSLContext context = getSSLContext();
						SSLEngine engine = context.createSSLEngine();
						params.setNeedClientAuth(false);
						params.setCipherSuites(engine.getEnabledCipherSuites());
						params.setProtocols(engine.getEnabledProtocols());

						// Set the SSL parameters
						SSLParameters sslParameters = context.getSupportedSSLParameters();
						params.setSSLParameters(sslParameters);

					} catch (Exception ex) {
						System.out.println("Failed to create HTTPS port");
					}
				}
			});

			server.createContext("/auth", new DestinyRegisterHTTPHandler());
			server.setExecutor(null);
			server.start();

		} catch (Exception e) {
			Logging.exception(e);
		}
	}

	@Interval(time_interval = 5)
	public static void timeOut(){
		for (Map.Entry<UUID, Destiny2RegisterObject> ent : auth.entrySet()) {
			if (System.currentTimeMillis() >= ent.getValue().time) {
				auth.remove(ent.getKey());
				updateMessage(ent.getValue());
			}
		}
	}

	public static void updateMessage(Destiny2RegisterObject ob)
	{
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("Linking timed out!");
		builder.setDescription("Linking timed out, please try again.");
		builder.setThumbnail(Startup.getClient().getSelfUser().getAvatarUrl());

		if(ob.message != null) {
			ChatUtils.editMessage(ob.message, builder.build());
		}else if(ob.event != null){
			ChatUtils.setEmbedColor(ob.event.getGuild(), ob.event.getUser(), builder);
			ob.event.getHook().setEphemeral(true);
			ob.event.getHook().sendMessageEmbeds(builder.build()).queue();
		}
	}
	
	public static void finalMessage(Destiny2RegisterObject ob, String destiny, ArrayList<UserAccount> accounts, String discord)
	{
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("Linking was successful!");
		builder.setDescription("Linking your destiny account to your discord account was successful!");
		builder.addField("Discord", discord, true);
		builder.addField("Destiny name", destiny, true);

		for(UserAccount account : accounts){
			if(account.accountType() != null){
				builder.addField( account.accountType(), account.accountName(), true);
			}
		}

		builder.setThumbnail(Startup.getClient().getSelfUser().getAvatarUrl());

		if(ob.message != null) {
			ChatUtils.editMessage(ob.message, builder.build());
		}else if(ob.event != null){
			ChatUtils.setEmbedColor(ob.event.getGuild(), ob.event.getUser(), builder);
			ob.event.getHook().setEphemeral(true);
			ob.event.getHook().sendMessageEmbeds(builder.build()).queue();
		}

		for(Entry<UUID, Destiny2RegisterObject> ent : auth.entrySet()){
			if(ent.getValue() == ob){
				auth.remove(ent.getKey());
			}
		}
	}

	@Override
	public String getDescription()
	{
		return "Register you destiny account with the bot";
	}

	public static String getHTML(String title, String... text){
		String prefix = "<!DOCTYPE html><html>";
		String suffix = "</div></html>";

		String style = "<style>body {background-color: #808080;text-align: center;color: white;font-family: Arial, Helvetica, sans-serif;}.outer_box{background-color: #5B5B5B;border-radius: 25px;width: 620px;margin: 0 auto;padding: 10px;}.title_box{background-color: #333333;border-radius: 25px;width: 600px;margin: 0 auto;padding: 10px;}</style>";
		StringBuilder html = new StringBuilder(prefix + style + "<div class=\"outer_box\"><div class=\"title_box\"><h1>" + title + "</h1></div>");

		for(String t : text){
			html.append("<p>").append(t).append("</p>");
		}

		return html + suffix;
	}

	@Override
	public String commandName()
	{
		return "destiny-register";
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		UUID uuid = UUID.randomUUID();
		String tempUrl = "https://www.bungie.net/en/OAuth/Authorize" + "?client_id=" + DestinyRegisterHTTPHandler.BOT_ID + "&response_type=code&state=" + uuid;

		Destiny2RegisterObject object = new Destiny2RegisterObject();
		object.time = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
		object.userId = slashEvent.getUser().getIdLong();
		object.channel = new BotChannel(slashEvent.getChannel());
		object.event = slashEvent;

		auth.put(uuid, object);

		EmbedBuilder builder = new EmbedBuilder();

		builder.setDescription("Click the below link to be redirected to Bungie's website to start registering your account.");
		builder.setThumbnail(Startup.getClient().getSelfUser().getAvatarUrl());

		ChatUtils.setEmbedColor(slashEvent.getGuild(), slashEvent.getUser(), builder);
		ReplyCallbackAction action = slashEvent.replyEmbeds(builder.build());
		action.setEphemeral(true);
		action.addActionRow(Button.link(tempUrl, "Click here to register"));
		action.queue();
	}
}