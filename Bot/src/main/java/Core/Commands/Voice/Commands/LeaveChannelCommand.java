package Core.Commands.Voice.Commands;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Commands.Voice.LavaLinkClient;
import Core.Commands.Voice.MusicCommand;
import Core.Commands.Voice.MusicCommand.MusicInfoCommand;
import Core.Objects.Annotation.Commands.SubCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@SubCommand( value = MusicInfoCommand.class)
public class LeaveChannelCommand extends MusicCommand
{
	@Override
	public String commandName()
	{
		return "leave";
	}

	@Override
	public String getDescription()
	{
		return "Remove bot from voice channel";
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		if (ChatUtils.getConnectedBotChannel(guild) == null) {
			ChatUtils.sendEmbed(channel, author.getAsMention() + " The bot is not connected to a voice channel!");
			return;
		}

		LavaLinkClient.disconnectFromChannel(guild);

		if (ChatUtils.getConnectedBotChannel(guild) == null) {
			ChatUtils.sendEmbed(channel,"*Left channel!*");
			return;
		}
		ChatUtils.sendEmbed(channel, "*Left channel `" + ChatUtils.getConnectedBotChannel(guild).getName() + "`!*");
	}
}