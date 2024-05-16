package Core.Commands.Voice.Commands.Queue;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Commands.Voice.Events.ClearQueueEvent;
import Core.Commands.Voice.MusicCommand;
import Core.Commands.Voice.MusicCommand.MusicInfoCommand;
import Core.Main.Startup;
import Core.Objects.Annotation.Commands.SubCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@SubCommand( value = MusicInfoCommand.class)
public class ClearQueue extends MusicCommand
{
	@Override
	public String commandName()
	{
		return "clear";
	}

	@Override
	public String getDescription()
	{
		return "clear the current music queue";
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		if((getGuildAudioPlayer(guild).currentTrack != null ? 1 : 0) + getGuildAudioPlayer(guild).getQueueSize() + getGuildAudioPlayer(guild).urlQueue.size() <= 0){
			ChatUtils.sendEmbed(channel, author.getAsMention() + " The queue is already empty!");
			return;
		}

		ChatUtils.sendEmbed(channel, author.getAsMention() + " Clearing current queue!");

		MusicCommand.getGuildAudioPlayer(guild).repeat = false;
		MusicCommand.getGuildAudioPlayer(guild).getPlayer().setPaused(false);
		MusicCommand.getGuildAudioPlayer(guild).getPlayer().stopTrack();
		MusicCommand.getGuildAudioPlayer(guild).clear();
		MusicCommand.getGuildAudioPlayer(guild).queueSize = 0;
		Startup.getClient().getEventManager().handle(new ClearQueueEvent(guild.getJDA(), guild));
	}
}