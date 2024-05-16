package Core.Commands.Voice.Commands;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Commands.Voice.MusicCommand;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;

@Command
public class MusicChannel extends MusicCommand
{
	@Override
	public String commandName()
	{
		return "music-channel";
	}

	@Override
	public String getDescription(){
		return "Designate a specific channel to post music related info";
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message) { }

	@Override
	public DefaultMemberPermissions commandPrivileges(){
		return DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR);
	}

	@SubCommand( value = MusicChannel.class)
	public static class addMusicChannel implements ISlashCommand{

		@SlashArgument( name = "channel", description = "Which channel you want to use for music notifications", required = true )
		public TextChannel targetMusicChannel;

		@Override
		public String getDescription()
		{
			return "Change what channel to post the currently playing message";
		}

		@Override
		public String commandName()
		{
			return "add";
		}

		@Override
		public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
		{
			MusicCommand.musicChannel.put(guild.getIdLong(), targetMusicChannel.getIdLong());
			ChatUtils.sendEmbed(channel, channel.getAsMention() + " has now been set as the music channel for this server!");
		}

		@Override
		public DefaultMemberPermissions commandPrivileges(){
			return DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR);
		}

	}

	@SubCommand( value = MusicChannel.class)
	public static class clearMusicChannel implements ISlashCommand{

		@Override
		public String commandName()
		{
			return "clear";
		}

		@Override
		public String getDescription()
		{
			return "Remove the currently set music notification channel";
		}

		@Override
		public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
		{
			MusicCommand.musicChannel.remove(guild.getIdLong());
			ChatUtils.sendEmbed(channel, author.getAsMention() + " Music channel has now been reset!");
		}

		@Override
		public DefaultMemberPermissions commandPrivileges(){
			return DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR);
		}

	}
}