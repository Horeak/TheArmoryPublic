package Core.Commands.Generic.Status;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Main.Startup;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Command
public class PingCommand implements ISlashCommand
{
	@Override
	public String getDescription()
	{
		return "Shows current ping";
	}

	@Override
	public String commandName()
	{
		return "ping";
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		ChatUtils.sendEmbed(channel, "REST Ping: **" + Startup.getClient().getRestPing().complete() + "**ms | Gateway ping: **" + Startup.getClient().getGatewayPing() + "**ms");
	}
}