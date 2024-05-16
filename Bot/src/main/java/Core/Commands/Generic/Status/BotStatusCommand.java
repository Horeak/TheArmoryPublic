package Core.Commands.Generic.Status;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Main.Startup;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Command
public class BotStatusCommand implements ISlashCommand
{
	@Override
	public String commandName()
	{
		return "status";
	}

	@Override
	public String getDescription(){
		return "Bot status information";
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		EmbedBuilder builder = new EmbedBuilder();

		builder.setTitle("Status");
		builder.setDescription("Current status of `" + Startup.getClient().getSelfUser().getName() + "`");
		
		builder.addField("Uptime", Utils.getUpTime(), true);
		builder.addField("Gateway Ping", Startup.getClient().getGatewayPing() + "ms", true);
		builder.addField("Rest Ping", Startup.getClient().getRestPing().complete() + "ms", true);

		builder.addField("Servers", Long.toString(Startup.getClient().getGuilds().size()), true);

		builder.setThumbnail(Startup.getClient().getSelfUser().getAvatarUrl());

		ChatUtils.sendMessage(channel, author.getAsMention(), builder.build());
	}
}