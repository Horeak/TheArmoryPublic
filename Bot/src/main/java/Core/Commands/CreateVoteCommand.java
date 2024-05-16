package Core.Commands;

import Core.CommandSystem.ChatMessageBuilder;
import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Main.Startup;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Fields.Save;
import Core.Objects.Annotation.Method.EventListener;
import Core.Objects.Annotation.Method.Interval;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.Time.TimeParserUtil;
import com.google.common.base.Strings;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Command
public class CreateVoteCommand implements ISlashCommand
{
	@Save("votes.json")
	public static CopyOnWriteArrayList<VoteObject> ONGOING_VOTES = new CopyOnWriteArrayList<>();
	
	@Interval( time_interval = 1)
	public static void voteCheck(){
		for (VoteObject ongoingVote : ONGOING_VOTES) {
			if(ongoingVote.endTime < System.currentTimeMillis()){
				closeVote(ongoingVote);
			}
		}
	}
	
	@EventListener
	public static void buttonClicked(ButtonInteractionEvent event){
		if(event.getComponentId().startsWith("vote:")){
			String full_id = event.getComponentId().replace("vote:", "");
			String[] ids = full_id.split("_");
			String voteId = ids[0];
			String option = ids[1];
			
			ONGOING_VOTES.stream().filter(s -> Objects.equals(s.getVoteId().toString(), voteId)).forEach(s -> {
				s.getUsersVoted().put(event.getUser().getIdLong(), option);
				updateVote(s);
			});
			event.reply("Vote has been registered!").setEphemeral(true).queue();
		}
	}
	
	@SlashArgument(name = "name", description = "The name of the post", required = true)
	public String name;
	
	@SlashArgument(name = "options", description = "A comma seperated list of what options to include, defaults to \"Yes,No\"")
	public final String options = "Yes,No";
	
	@SlashArgument( name = "end_time", description = "When will the vote end", required = true )
	public String endTime;
	
	@Override
	public String commandName()
	{
		return "vote";
	}
	
	@Override
	public String getDescription()
	{
		return "Create a custom post for users to be able to vote over";
	}
	
	
	public static final String BAR_FULL_ICON =  "<:barFull:725358666903191633>";
	public static final String BAR_EMPTY_ICON = "<:barEmpty:725358461617176586>";
	
	@Override
	public void onExecute(@NonNull SlashCommandInteractionEvent slashEvent, @NonNull Guild guild, @NonNull User author, @NonNull SlashCommandChannel channel, @NonNull SlashCommandMessage message)
	{
		List<String> options = Arrays.stream(this.options.split(",")).map(String::strip).map(StringUtils::capitalize).toList();
		long time = TimeParserUtil.getTime(author, endTime) + System.currentTimeMillis();
		
		if(options.size() > 10){
			ChatUtils.sendEmbed(channel, "The vote can have max 10 options!");
			return;
		}
		
		if(channel.isPrivate()){
			ChatUtils.sendEmbed(channel, "This command is unavailable in DMs!");
			return;
		}
		
		VoteObject object = VoteObject.builder()
				.author(author.getIdLong())
				.active(true)
				.voteId(UUID.randomUUID())
				.name(name)
				.usersVoted(new HashMap<>())
				.options(options)
				.endTime(time)
				.build();
		ChatMessageBuilder builder = new ChatMessageBuilder(author, channel);
		EmbedBuilder embed = createVoteMessage(object, channel, author);
		
		builder.withEmbed(embed);
		
		ArrayList < ActionRow > rows = new ArrayList<>();
		
		int num = 0;
		for(String opt : options){
			Button btn = Button.secondary("vote:" + object.getVoteId() + "_" + opt, opt);
			if(rows.size() == 0 || num >= 5){
				num = 0;
				rows.add(ActionRow.of(btn));
				num++;
				continue;
			}
			
			rows.get(rows.size()-1).getComponents().add(btn);
			num++;
		}
		
		builder.withActionRows(rows);
		
		ONGOING_VOTES.add(object);
		builder.addRunnable((msg, err) -> ONGOING_VOTES.stream().filter(s1 -> s1.getVoteId() == object.getVoteId()).forEach(s -> {
			s.setChannelId(msg.getChannel().getIdLong());
			s.setMessageId(msg.getIdLong());
		}));
		
		builder.send();
		ChatUtils.sendEmbed(channel, "The vote has been created!");
	}
	
	public static void closeVote(VoteObject object){
		object.active = false;
		ONGOING_VOTES.remove(object);
		TextChannel channel = Startup.getClient().getTextChannelById(object.getChannelId());
		if(channel == null) return;
		
		Message message = channel.retrieveMessageById(object.getMessageId()).complete();
		User user = Startup.getClient().getUserById(object.author);
		
		EmbedBuilder embed = createVoteMessage(object, channel, user);
		
		String largest = null;
		long votesLargest = 0;
		
		HashMap<String, Integer> votes = new HashMap<>();
		
		for(String opt : object.options){
			long count = object.usersVoted.values().stream().filter(s -> Objects.equals(s, opt)).count();
			votes.put(opt, (int)count);
			if(count > 0 && count > votesLargest){
				votesLargest = count;
				largest = opt;
			}
		}
		embed.setDescription("");
		
		if(largest != null) {
			long finalVotesLargest = votesLargest;
			if(votes.values().stream().filter(s -> s == finalVotesLargest).count() > 1){
				embed.appendDescription("It was a tie!\n\n");
			}else {
				embed.appendDescription("**" + largest + "** Won!\n\n");
			}
		}
		
		embed.appendDescription("Ended " + TimeFormat.RELATIVE.format(object.endTime));
		
		message.editMessageEmbeds(embed.build()).setComponents().queue();
	}
	
	public static void updateVote(VoteObject object){
		TextChannel channel = Startup.getClient().getTextChannelById(object.getChannelId());
		Message message = channel.retrieveMessageById(object.getMessageId()).complete();
		User user = Startup.getClient().getUserById(object.author);
		
		EmbedBuilder embed = createVoteMessage(object, channel, user);
		message.editMessageEmbeds(embed.build()).queue();
	}
	
	private static EmbedBuilder createVoteMessage(VoteObject object, TextChannel channel, User user)
	{
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle(object.name);
		embed.setDescription("Ends in " + TimeFormat.RELATIVE.format(object.endTime));
		
		StringBuilder optionNames = new StringBuilder();
		StringBuilder votedForOption = new StringBuilder();
		StringBuilder voteBars = new StringBuilder();
		
		long votesLargest = 0;
		
		for(String opt : object.options){
			votesLargest = Math.max(votesLargest, object.usersVoted.values().stream().filter(s -> Objects.equals(s, opt)).count());
		}
		
		for(String opt : object.options){
			float proc = (float)object.usersVoted.values().stream().filter(s -> Objects.equals(s, opt)).count() / (float)object.usersVoted.size();
			int count = (int)Math.floor(proc * 6f);
			long optCount = object.usersVoted.values().stream().filter(s -> Objects.equals(s, opt)).count();
			
			optionNames.append(Objects.equals(optCount, votesLargest) ? "**" + opt + "**" : opt).append("\n");
			voteBars.append("[").append(Strings.repeat(BAR_FULL_ICON, count)).append(Strings.repeat(BAR_EMPTY_ICON, 6 - count)).append("]\n");
			votedForOption.append(object.usersVoted.values().stream().filter(s -> Objects.equals(s, opt)).count()).append(" / ").append(object.usersVoted.size()).append("\n");
		}
		
		embed.addField("", optionNames.toString(), true);
		embed.addField("", voteBars.toString(), true);
		embed.addField("", votedForOption.toString(), true);
		
		ChatUtils.setEmbedColor(channel.getGuild(), user, embed);
		return embed;
	}
	
	@Builder
	@Data
	public static class VoteObject{
		private Long author;
		private boolean active;
		private UUID voteId;
		private String name;
		private Map<Long, String> usersVoted;
		private List<String> options;
		private Long messageId;
		private Long channelId;
		private Long endTime;
	}
}