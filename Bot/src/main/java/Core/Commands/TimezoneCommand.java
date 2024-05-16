package Core.Commands;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Objects.Annotation.Commands.ArgumentAutoComplete;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Fields.Save;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.AutoComplete;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.lang3.StringUtils;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

@Command
public class TimezoneCommand implements ISlashCommand
{
	@Save("timezones.json")
	public static ConcurrentHashMap<Long, String> TIME_ZONES = new ConcurrentHashMap<>();
	
	@SlashArgument( name = "timezone", description = "The specific timezone to use", required = true )
	public String timezone;
	
	@ArgumentAutoComplete("timezone")
	public AutoComplete timeZoneAutoComplete = (arg) -> ZoneId.getAvailableZoneIds().stream().filter(s -> s.toLowerCase().contains(arg.toLowerCase())).sorted(Comparator.comparingInt(s -> StringUtils.compare(s, arg))).limit(OptionData.MAX_CHOICES).toList();
	
	@Override
	public String commandName()
	{
		return "timezone";
	}
	
	@Override
	public String getDescription()
	{
		return "select which timezone the bot will use, defaults to GMT";
	}
	
	@Override
	public void onExecute(@NonNull SlashCommandInteractionEvent slashEvent, @NonNull Guild guild, @NonNull User author, @NonNull SlashCommandChannel channel, @NonNull SlashCommandMessage message)
	{
		TimeZone timeZone = TimeZone.getTimeZone(timezone);
		TIME_ZONES.put(author.getIdLong(), timeZone.getID());
		ChatUtils.sendEmbed(channel, "Your timezone has now been set to `" + timeZone.getDisplayName() + "`");
	}
}