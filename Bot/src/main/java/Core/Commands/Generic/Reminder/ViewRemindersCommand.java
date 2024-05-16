package Core.Commands.Generic.Reminder;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.ComponentSystem.ComponentResponseSystem;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.CommandSystem.SlashMessageBuilder;
import Core.Commands.Generic.Reminder.ReminderCommand.remindObject;
import Core.Main.Startup;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.Time.TimeParserUtil;
import Core.Util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.UUID;

@Command
public class ViewRemindersCommand  implements ISlashCommand
{
	@Override
	public String getDescription()
	{
		return "View all your current reminders";
	}
	@Override
	public String commandName()
	{
		return "reminders";
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		ArrayList<Entry<UUID, remindObject>> entList = new ArrayList<>(ReminderCommand.reminders.entrySet());
		entList.sort(Comparator.comparingLong(c -> c.getValue().timeToRemind - System.currentTimeMillis()));
		entList.removeIf(s -> System.currentTimeMillis() > s.getValue().timeToRemind);
		entList.removeIf((e) -> e.getValue().userId != author.getIdLong());

		if(entList.size() > 0){
			EmbedBuilder builder = new EmbedBuilder();

			builder.appendDescription("You currently have " + entList.size() + " reminder" + (entList.size() > 1 ? "s" : "") + " set, ");

			if(entList.size() > 1) builder.appendDescription("the closest one being " + TimeFormat.RELATIVE.format(entList.get(0).getValue().timeToRemind) + ".");
			else builder.appendDescription("which is set to go off " + TimeFormat.RELATIVE.format(entList.get(0).getValue().timeToRemind) + ".");
			builder.appendDescription("\nIf you wish to view or cancel " + (entList.size() > 1 ? "a" : "the") + " reminder, please select it from the list below.");

			if(entList.size() >= 25){
				builder.appendDescription("\nOnly the first 25 upcoming reminders can be selected. ");
				entList = new ArrayList<>(entList.subList(0, 25));
			}

			UUID selectionId = UUID.randomUUID();

			StringSelectMenu.Builder menuBuilder = StringSelectMenu.create(selectionId.toString()).setPlaceholder("Choose reminder");

			for(Entry<UUID, remindObject> ent : entList){
				String text = 	TimeParserUtil.getTime(ent.getValue().timeToRemind);
				String[] tk = text.split(",");
				menuBuilder.addOptions(SelectOption.of(tk[0], ent.getKey().toString()).withDescription("\"" + Utils.limitString(ent.getValue().text, 45) + "\""));
			}

			ArrayList<Entry<UUID, remindObject>> finalEntList = entList;
			SelectMenu menu = (SelectMenu)ComponentResponseSystem.addComponent(selectionId, author, (channel instanceof SlashCommandChannel ? channel.getEvent() : null), menuBuilder.build(), (e) -> {
				String id = ((StringSelectInteractionEvent)e).getValues().get(0);

				if(id != null){
					remindObject ob = null;

					for(Entry<UUID, remindObject> ent : finalEntList){
						if(ent.getKey().toString().equals(id)){
							ob = ent.getValue();
							break;
						}
					}

					if(ob != null){
						String origMessageLink = null;

						if(ob.channelId != null && ob.messageId != null) {
							TextChannel origChannel = Startup.getClient().getTextChannelById(ob.channelId);

							if(origChannel != null) {
								Message origMessage = Utils.getMessage(origChannel, ob.messageId);

								if (origMessage != null) {
									origMessageLink = origMessage.getJumpUrl();
								}
							}
						}


						EmbedBuilder builder1 = new EmbedBuilder();
						builder1.appendDescription("You set a reminder");

						if(ob.setAt != null && ob.setAt != -1L){
							builder1.appendDescription(" " + TimeFormat.RELATIVE.format(ob.setAt));
						}

						builder1.appendDescription(" with the following text \"" + ob.text + "\"");
						builder1.appendDescription("\nThe reminder is set to go off " + TimeFormat.RELATIVE.format(ob.timeToRemind) + " at " + TimeFormat.DATE_TIME_LONG.format(ob.timeToRemind));

						if(ob.setAt != null && ob.setAt != -1L){
							if(origMessageLink != null){
								builder1.appendDescription("\nThe original message can be found [Here](" + origMessageLink + ")");
							}
						}

						Button cancel = (Button)ComponentResponseSystem.addComponent(author, Button.secondary("id", "Cancel reminder"), (ev) -> {
							remindObject object = ReminderCommand.reminders.remove(UUID.fromString(id));

							ReminderCommand.tasks.get(UUID.fromString(id)).cancel(true);

							if(object != null){
								ChatUtils.sendEmbed(channel, "That reminder has now been canceled!");
							}else{
								ChatUtils.sendEmbed(channel, "Opps, there was an error trying to cancel that reminder!");
							}
						});

						SlashMessageBuilder slashBuilder = ChatUtils.createSlashMessage(author, channel);
						slashBuilder.withEmbed(builder1);
						slashBuilder.addAction(cancel);
						slashBuilder.send();
					}
				}
			});

			SlashMessageBuilder slashBuilder = ChatUtils.createSlashMessage(author, channel);
			slashBuilder.withEmbed(builder);
			slashBuilder.addAction(menu);
			slashBuilder.send();
		}else{
			ChatUtils.sendEmbed(channel, "There is no reminders to show you, you may not have set any yet!");
		}
	}
}