package Core.Commands.Generic.Reminder;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Fields.Save;
import Core.Objects.Annotation.Method.EventListener;
import Core.Objects.Annotation.Method.Interval;
import Core.Objects.Annotation.Method.Startup.Init;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.Time.TimeParserUtil;
import Core.Util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.time.Instant;
import java.util.Date;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Command
public class ReminderCommand implements ISlashCommand
{
	@Save( "reminders.json")
	public static ConcurrentHashMap<UUID, remindObject> reminders = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<UUID, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();

	@SlashArgument( name = "time", description = "When you want to be reminded.", required = true )
	public String timeArg;

	@SlashArgument( name = "text", description = "The message you want to be reminded about.", required = true )
	public String text;

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		long time = TimeParserUtil.getTime(author, timeArg);

		if(time == 0){
			ChatUtils.sendEmbed(channel, author.getAsMention() + " Please specify a time in which you want to be reminded in!");
			return;
		}

		String timeText = TimeFormat.RELATIVE.format(System.currentTimeMillis() + time);

		Long timeToExcecute = System.currentTimeMillis() + time;
		UUID id = UUID.randomUUID();
		remindObject object = new remindObject(System.currentTimeMillis(), timeToExcecute, text.trim(), author.getIdLong(), message.getIdLong(), channel.getIdLong(), id);
		reminders.put(id, object);
		tasks.put(id, Startup.scheduledExecutor.schedule(() -> handle(object), time, TimeUnit.MILLISECONDS));

		ChatUtils.sendEmbed(channel, author.getAsMention() + " A reminder has now been set for " + (text != null ? "**" + text + "** " + timeText : timeText));
	}

	@Override
	public String commandName()
	{
		return "reminder";
	}

	@Override
	public String getDescription()
	{
		return "Allows setting up custom reminders";
	}
	
	
	@Interval(time_interval = 30)
	public static void clearOldReminders(){
		for(Entry<UUID, remindObject> objectEntry : reminders.entrySet()){
			boolean done = objectEntry.getValue().timeToRemind < System.currentTimeMillis() && objectEntry.getValue().posted;
			
			if(objectEntry.getValue().timeToRemind == -1 || done && System.currentTimeMillis() >= objectEntry.getValue().timeToRemind + TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS) || System.currentTimeMillis() >= objectEntry.getValue().timeToRemind + TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS)){
				reminders.remove(objectEntry.getValue().id);
				return;
			}
		}
	}
	
	@Init
	public static void init(){
		for(Entry<UUID, remindObject> objectEntry : reminders.entrySet()){
			boolean done = objectEntry.getValue().timeToRemind < System.currentTimeMillis() && objectEntry.getValue().posted;

			if(objectEntry.getValue().timeToRemind == -1 || System.currentTimeMillis() >= objectEntry.getValue().timeToRemind + TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS)){
				reminders.remove(objectEntry.getValue().id);
				return;
			}

			if(done){
				handle(objectEntry.getValue(), true);
				continue;
			}

			tasks.put(objectEntry.getKey(), Startup.scheduledExecutor.schedule(() -> handle(objectEntry.getValue()), objectEntry.getValue().timeToRemind - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
		}
	}

	public static void handle(remindObject ob){
		handle(ob, false);
		tasks.remove(ob.id);
	}

	public static void handle(remindObject ob, boolean late){
		if(ob.userId != null && !ob.posted) {
			User user = Utils.getUser(ob.userId);
			PrivateChannel sendChannel = user.openPrivateChannel().complete();

			String timeSinceText = null;
			String origMessageLink = null;


			if(ob.channelId != null && ob.messageId != null) {
				TextChannel origChannel = Startup.getClient().getTextChannelById(ob.channelId);

				if(origChannel != null) {
					Message origMessage = Utils.getMessage(origChannel, ob.messageId);

					if (origMessage != null) {
						Instant time = origMessage.getTimeCreated().toInstant();

						timeSinceText = TimeParserUtil.timeFormat.formatDuration(new Date(time.toEpochMilli()));
						origMessageLink = origMessage.getJumpUrl();
					}
				}
			}

			String timeAgoString = timeSinceText != null && !timeSinceText.isBlank() ? timeSinceText + " ago" : "";
			String aboutString = (ob.text != null && !ob.text.isBlank()) ? "about **" + ob.text.trim() + "**. " : "";

			EmbedBuilder builder = new EmbedBuilder();

			builder.setTitle("Here is your reminder!");
			builder.setDescription("You asked to be reminded " + (aboutString) + timeAgoString);

			if(late){
				builder.appendDescription("\n\n**The reminder was supposed to go off " + TimeParserUtil.getTime(ob.timeToRemind) + "**");
			}

			if(origMessageLink != null && !origMessageLink.isBlank()){
				builder.appendDescription("\n\n*Original message can be found [Here](" + origMessageLink + ")*");
			}

			try{
				ChatUtils.setEmbedColor(null, builder);
				var createAction = sendChannel.sendMessageEmbeds(builder.build());
				createAction.addActionRow(Button.danger("reminder_delete_" + ob.id, "Dismiss"), Button.secondary("reminder_repeat_" + ob.id, "Repeat"));
				createAction.queue();
				ob.posted = true;
			}catch(Exception e){
				Logging.exception(e);
			}
		}

		if(ob.timeToRemind != -1 && System.currentTimeMillis() > ob.timeToRemind + TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS)) {
			reminders.remove(ob.id);
		}
	}

	@EventListener
	public static void buttonEvent(ButtonInteractionEvent event)
	{
		if (event.getButton().getId().startsWith("reminder_delete_")) {
			UUID id = UUID.fromString(event.getButton().getId().replace("reminder_delete_", ""));
			var object = reminders.get(id);
			
			if (object != null) {
				reminders.remove(id);
				tasks.remove(id);
				event.getMessage().delete().queue();
				event.reply("Reminder dismissed!").setEphemeral(true).queue();
			}
		}else if(event.getButton().getId().startsWith("reminder_repeat_")){
			UUID id = UUID.fromString(event.getButton().getId().replace("reminder_repeat_", ""));
			var object = reminders.get(id);
			
			if (object != null) {
				Modal.Builder modal = Modal.create(event.getButton().getId(), "Repeat Reminder");
				var input = TextInput.create("reminder_repeat_text", "When would you like to be reminded?", TextInputStyle.SHORT);
				input.setPlaceholder("1h 30m");
				modal.addActionRow(input.build());
				event.replyModal(modal.build()).queue();
				event.getMessage().delete().queue();
			}
		}
	}
	
	@EventListener
	public static void modalSubmit(ModalInteractionEvent event){
		if(event.getModalId().startsWith("reminder_repeat_")){
			UUID id = UUID.fromString(event.getModalId().replace("reminder_repeat_", ""));
			var object = reminders.get(id);
			
			if (object != null) {
				String text = event.getValues().get(0).getAsString();
				Long time = TimeParserUtil.getTime(text);
				
				UUID id1 = UUID.randomUUID();
				remindObject newObject = new remindObject(System.currentTimeMillis(),
				                                          System.currentTimeMillis() + time,
				                                       object.text,
				                                       object.userId,
				                                       object.messageId,
				                                       object.channelId,
				                                       id1);
				reminders.put(id1, newObject);
				tasks.put(id, Startup.scheduledExecutor.schedule(() -> handle(newObject), time, TimeUnit.MILLISECONDS));
				event.reply("Reminder set for " + TimeParserUtil.getTime(newObject.timeToRemind)).setEphemeral(true).queue();
			}
		}
	}

	public static class remindObject{
		public final Long timeToRemind;
		
		public Long setAt = -1L;
		public final String text;

		public final Long userId;
		public final Long messageId;
		public final Long channelId;
		
		public boolean posted = false;

		public final UUID id;

		public remindObject(Long setAt, Long timeToRemind, String text, Long userId, Long messageId, Long channelId, UUID id)
		{
			this.setAt = setAt;
			this.timeToRemind = timeToRemind;
			this.text = text;
			this.userId = userId;
			this.messageId = messageId;
			this.channelId = channelId;
			this.id = id;
		}
	}
}