package Core.Commands.Voice.Commands;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.PermissionsUtils;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Commands.Voice.LavaLinkClient;
import Core.Commands.Voice.MusicCommand;
import Core.Commands.Voice.MusicCommand.MusicInfoCommand;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.EnumSet;

@SubCommand( value = MusicInfoCommand.class)
public class JoinChannelCommand extends MusicCommand
{
	@SlashArgument( name = "channel", description = "The voice channel you want the bot to join", required = true)
	public VoiceChannel targetChannel;

	@Override
	public String commandName()
	{
		return "join";
	}

	@Override
	public String getDescription()
	{
		return "Add bot to voice channel";
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel12, SlashCommandMessage message)
	{
		if (!canAccess(author, guild, targetChannel)) {
			ChatUtils.sendEmbed(channel12, author.getAsMention() + " Unable to join that channel as you do not have permissions to connect to it.");
			return;
		}

		if (targetChannel != null) {
			if(LavaLinkClient.connectToChannel(targetChannel)){
				ChatUtils.sendEmbed(channel12, "*Joined channel*  `" + targetChannel.getName() + "`");
			}
		}
	}

	public static boolean canAccess(User author, Guild guild,  VoiceChannel channelS)
	{
		if (channelS == null) {
			return true;
		}
		
		boolean canJoin = PermissionsUtils.hasPermissions(author, guild, channelS, EnumSet.of(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK));
		boolean canLeave = ChatUtils.getConnectedBotChannel(guild) == null || PermissionsUtils.hasPermissions(author, guild, ChatUtils.getConnectedBotChannel(guild), EnumSet.of(Permission.VOICE_CONNECT));

		return canJoin && canLeave;
	}
}