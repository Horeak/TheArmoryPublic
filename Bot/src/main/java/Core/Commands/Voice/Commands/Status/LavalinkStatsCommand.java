package Core.Commands.Voice.Commands.Status;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Commands.Voice.LavaLinkClient;
import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Objects.Annotation.Debug;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Debug
@Command
public class LavalinkStatsCommand implements ISlashCommand
{
	@Override
	public String commandName()
	{
		return "lavalink";
	}
	
	@Override
	public String getDescription()
	{
		return null;
	}
	
	@Override
	public void onExecute(@NonNull SlashCommandInteractionEvent slashEvent, @NonNull Guild guild, @NonNull User author, @NonNull SlashCommandChannel channel, @NonNull SlashCommandMessage message) {}
	
	@Debug
	@SubCommand( LavalinkStatsCommand.class)
	public static class LavalinkInfoCommand implements ISlashCommand{
		
		@Override
		public String commandName()
		{
			return "info";
		}
		
		@Override
		public String getDescription()
		{
			return null;
		}
		
		@Override
		public void onExecute(@NonNull SlashCommandInteractionEvent slashEvent, @NonNull Guild guild, @NonNull User author, @NonNull SlashCommandChannel channel, @NonNull SlashCommandMessage message)
		{
			try {
				String url = "http://" +  (Startup.jarFile ? "lavalink/" : (Startup.getEnvValue("lavalink:ip") + "/"));
				GetRequest t = Unirest.get(url + LavaLinkClient.version + "/info").header("Authorization", LavaLinkClient.password);
				JsonNode info = t.asJson().getBody();
				ChatUtils.sendEmbed(channel, "```json\n" + Startup.getGSON().toJson(Startup.getGSON().fromJson(String.valueOf(info.getObject()), JsonObject.class)) + "\n```");
				
			} catch (UnirestException e) {
				Logging.exception(e);
			}
		}
	}
	
	@Debug
	@SubCommand( LavalinkStatsCommand.class)
	public static class LavalinkStatusCommand implements ISlashCommand{
		
		@Override
		public String commandName()
		{
			return "status";
		}
		
		@Override
		public String getDescription()
		{
			return null;
		}
		
		@Override
		public void onExecute(@NonNull SlashCommandInteractionEvent slashEvent, @NonNull Guild guild, @NonNull User author, @NonNull SlashCommandChannel channel, @NonNull SlashCommandMessage message)
		{
			try {
				String url = "http://" +  (Startup.jarFile ? "lavalink/" : (Startup.getEnvValue("lavalink:ip") + "/"));
				GetRequest t = Unirest.get(url + LavaLinkClient.version + "/stats").header("Authorization", LavaLinkClient.password);
				JsonNode info = t.asJson().getBody();
				ChatUtils.sendEmbed(channel, "```json\n" + Startup.getGSON().toJson(Startup.getGSON().fromJson(String.valueOf(info.getObject()), JsonObject.class)) + "\n```");
				
			} catch (UnirestException e) {
				Logging.exception(e);
			}
		}
	}
}