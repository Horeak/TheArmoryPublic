package Core.Commands.Destiny;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.ComponentSystem.ComponentResponseSystem;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.CommandSystem.SlashMessageBuilder;
import Core.Commands.Destiny.Models.*;
import Core.Commands.Destiny.Models.DestinyBasicModels.DestinyRewardObject;
import Core.Commands.Destiny.Models.DestinyBasicModels.ItemValue;
import Core.Commands.Destiny.Models.DestinyBasicModels.MasterworkObject;
import Core.Commands.Destiny.User.Destiny2UserUtil;
import Core.Commands.Destiny.system.DestinyItemSystem;
import Core.Commands.Destiny.system.DestinySystem;
import Core.Objects.Annotation.Commands.ArgumentAutoComplete;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Method.EventListener;
import Core.Objects.BotChannel;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.AutoComplete;
import Core.Util.Utils;
import com.google.common.base.Strings;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.apache.commons.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Command
public class DestinyItemCommand implements ISlashCommand
{
	public ArrayList<DestinyItemObject> searchList = new ArrayList<>();

	@SlashArgument( name = "item", description = "Which item you are looking for", required = true )
	public String item;
	
	@SlashArgument(name = "public", description = "Show the item in chat")
	public boolean publicCommand = true;
	
	@SlashArgument(name = "stats", description = "Show the stats of the item")
	public boolean showStats = true;
	
	@SlashArgument(name = "perks", description = "Show the perks of the item")
	public boolean showPerks = true;

	@ArgumentAutoComplete("item")
	public AutoComplete itemAutoComplete = (arg) -> {
		findItems(arg);
		return searchList.stream().map(s-> Utils.limitString(s.displayProperties.name, net.dv8tion.jda.api.interactions.commands.Command.Choice.MAX_NAME_LENGTH)).collect(Collectors.toSet()).stream().toList();
	};

	private void findItems(String arg){
		searchList = DestinyItemSystem.getItemsByName(arg);
		searchList.removeIf((o) -> o.itemType == 20); //Remove dummy items

		searchList.sort((j1, j2) -> {
			Pattern p = Pattern.compile("(?i)(?:^|\\W)" + item + "(?:$|\\W)");
			Matcher m1 = p.matcher(j1.getName());
			Matcher m2 = p.matcher(j2.getName());

			boolean e1 = m1.find();
			boolean e2 = m2.find();

			return e1 && e2 ? 0 : e1 ? -1 : e2 ? 1 : 0;
		});

		searchList.removeIf(Objects::isNull);

		if (searchList.size() > 5) {
			List<DestinyItemObject> list = searchList.subList(0, 5);
			searchList = new ArrayList<>(list);
		}
	}

	@Override
	public String commandName()
	{
		return "destiny";
	}

	@Override
	public String getDescription(){
		return "Look up any item from Destiny 2";
	}

	@Override
	public void onExecute(@NonNull SlashCommandInteractionEvent slashEvent, @NonNull Guild guild,
		@NonNull User author, @NonNull SlashCommandChannel channel, @NonNull SlashCommandMessage message){
		findItems(item);

		for(DestinyItemObject destinyItemObject : searchList){
			if(destinyItemObject.displayProperties.name.equals(item)){
				searchList = new ArrayList<>(List.of(destinyItemObject));
				break;
			}
		}
		
		if (searchList.size() > 1) {
			StringJoiner joiner = new StringJoiner("\n");

			int i = 1;
			for (DestinyItemObject object : searchList) {
				String dlc = (object.dlc != null ? " - " + WordUtils.capitalize(object.dlc) : object.getSeasonName() != null ? " - " + WordUtils.capitalize(object.getSeasonName()) : "");

				String versionPre = "**[Destiny 2 " + dlc + "]**";
				String namePre = "**" + object.getName() + "** (*" + object.getItemTierAndType() + "*)";
				String sourcePre = (object.source != null && !object.source.isEmpty() ? "\n > " + (!object.source.startsWith("Source: ") ? "Source: " : "") + object.source : "");
				
				joiner.add(
					"**" + i + "**) " + versionPre + " " + namePre + sourcePre + "\n");
				i++;
			}

			EmbedBuilder builder = new EmbedBuilder();
			builder.setDescription(joiner.toString());
			builder.setTitle("Select which item you would like to view from the list below by using the below buttons.");

			ArrayList<ItemComponent> actions = new ArrayList<>();

			for(int g = 1; g <= searchList.size(); g++){
				DestinyItemObject object = searchList.get(g - 1);
				actions.add(ComponentResponseSystem.addComponent(author, channel.getEvent(), Button.secondary("id", Integer.toString(g)), (e) -> showInfo(slashEvent, channel, object)));
			}

			SlashMessageBuilder slashBuilder = ChatUtils.createSlashMessage(author, channel);
			slashBuilder.withEmbed(builder);
			slashBuilder.withActions(actions);
			slashBuilder.send();

		} else if (searchList.size() == 1) {
			DestinyItemObject object = searchList.get(0);
			showInfo(slashEvent, channel, object);

		} else {
			ChatUtils.sendEmbed(channel, author.getAsMention() + " Found no items with that name!");
		}
	}
	
	public void showInfo(SlashCommandInteractionEvent event, BotChannel channel, DestinyItemObject object)
	{
		if (object == null || object.getName() == null || object.getName().isEmpty()) {
			ChatUtils.sendEmbed(channel, "Found no item with that name!");
			return;
		}
		
		String itemType = (object.itemType == 3 ? object.itemTypeAndTierDisplayName : object.itemTypeDisplayName);
		
		if (itemType == null || itemType.isEmpty()) {
			if (object.itemTypeAndTierDisplayName != null && !object.itemTypeAndTierDisplayName.isEmpty()) {
				itemType = object.itemTypeAndTierDisplayName;
			} else {
				itemType = object.itemTypeDisplayName;
			}
		}
		
		EmbedBuilder builder = new EmbedBuilder();
		builder.setAuthor("Destiny 2" + (itemType.isBlank() ? "" : " | " + itemType), null, null);

		if (object.getImage() != null) {
			builder.setImage(Destiny2UserUtil.BASE_BUNGIE_URL + object.getImage());
		}
		if (object.getIcon() != null) {
			builder.setThumbnail(Destiny2UserUtil.BASE_BUNGIE_URL + object.getIcon());
		}
		if (object.getName() != null) {
			builder.setTitle(object.getName());
		}
		if (object.getDescription() != null) {
			builder.setDescription(object.getDescription());
		}

		builder.setColor(new Color(195, 188, 180));

		if (object.getItemTier() == 3) {
			builder.setColor(new Color(54, 111, 66));
		}
		if (object.getItemTier() == 4) {
			builder.setColor(new Color(80, 118, 163));
		}
		if (object.getItemTier() == 5) {
			builder.setColor(new Color(82, 47, 101));
		}
		if (object.getItemTier() == 6) {
			builder.setColor(new Color(206, 174, 51));
		}

		StringBuilder builder1 = new StringBuilder();
		
		if(object.slotInfo != null) {
			builder.appendDescription(object.slotInfo + "\n");
		}
		
		for (Map.Entry<String, String> ent : object.getInfo().entrySet()) {
			builder1.append("*").append(ent.getKey()).append("*").append(ent.getKey().equals(EmbedBuilder.ZERO_WIDTH_SPACE) ? "" : ": ").append(ent.getValue()).append("\n");
		}

		{
			StringBuilder builder2 = new StringBuilder();

			if(object.seasonInfo.size() > 0){
				for (Map.Entry<String, String> ent : object.seasonInfo.entrySet()) {
					builder2.append("*").append(ent.getKey()).append("*: ").append(ent.getValue()).append("\n");
				}
			}

			if (builder2.toString().length() > 0) {
				builder.addField("Season", builder2.toString(), false);
			}
		}

		{
			StringBuilder builder2 = new StringBuilder();

			if(object.powerInfo.size() > 0){
				for (Map.Entry<String, String> ent : object.powerInfo.entrySet()) {
					builder2.append("*").append(ent.getKey()).append("*: ").append(ent.getValue()).append("\n");
				}
			}

			if (builder2.toString().length() > 0) {
				builder.addField("Power", builder2.toString(), false);
			}
		}

		if (builder1.toString().length() > 0) {
			builder.setDescription(builder1.toString());
		}

		if(showStats) {
			List<BaseStatObject> list = new LinkedList<>(object.getStats().values());
			list.removeIf(Objects::isNull);
			list.removeIf((o) -> o.getName() == null || o.getName().isEmpty());
			list.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
			list.removeIf((o) -> o.getName().equalsIgnoreCase("charge time") && list.stream().anyMatch((o1) -> o1.getName().equalsIgnoreCase("draw time")));
			list.removeIf((o) -> o.getName().equalsIgnoreCase("attack"));
			list.removeIf((o) -> o.getName().equalsIgnoreCase("power"));
			Collections.reverse(list);
			
			StringBuilder stringsBuilder = new StringBuilder();
			StringBuilder valuesBuilder = new StringBuilder();
			
			int numerics = 0;
			
			for (BaseStatObject ent : list) {
				String name = Utils.limitString(ent.getName(), 20);
				
				float percent = ent.value != -1 ? (float)ent.value / (float)(ent.displayMaximum > 0 ? ent.displayMaximum : 100) : 0;
				int num = Math.round(percent * 5F);
				int leftNum = 5 - num;
				
				boolean displayTime = name.toLowerCase().contains("time");
				String value = ent.value != -1 ? ent.value + (displayTime ? " ms " : "") : "X";
				
				if (displayTime && ent.value == 0) continue;
				
				//TODO Make sure non numeric stats are all displayed before any numerics
				if (!name.isEmpty()) {
					//The equippable check is for catalysts but may need to be changed
					if (object.isEquippable() || ent.value <= 0) {
						if (!ent.displayAsNumeric && num >= 0 && leftNum >= 0) {
							String text = "*" + name + "*:\n";
							String valueText = "[" + Strings.repeat(DestinySystem.BAR_FULL_ICON, num) + Strings.repeat(DestinySystem.BAR_EMPTY_ICON, leftNum) + "] **" + value + "**\n";
							
							if (stringsBuilder.toString().length() + text.length() >= MessageEmbed.VALUE_MAX_LENGTH || valuesBuilder.toString().length() + valueText.length() >= MessageEmbed.VALUE_MAX_LENGTH) {
								if (!stringsBuilder.toString().isBlank()) {
									builder.addField("Stats", stringsBuilder.toString(), true);
								}
								
								if (!valuesBuilder.toString().isBlank()) {
									builder.addField(EmbedBuilder.ZERO_WIDTH_SPACE, valuesBuilder.toString(), true);
								}
								
								if (!valuesBuilder.toString().isBlank() || !stringsBuilder.toString().isBlank()) {
									builder.addField(EmbedBuilder.ZERO_WIDTH_SPACE, "", false);
								}
								
								stringsBuilder = new StringBuilder();
								valuesBuilder = new StringBuilder();
							}
							stringsBuilder.append(text);
							valuesBuilder.append(valueText);
						} else {
							numerics++;
						}
					}
				}
			}
			
			if (numerics > 0) {
				for (BaseStatObject ent : list) {
					String name = Utils.limitString(ent.getName(), 20);
					
					boolean displayTime = name.toLowerCase().contains("time");
					String value = ent.value + (displayTime ? " ms " : "");
					
					if (displayTime && ent.value == 0) continue;
					
					float percent = ent.value != -1 ? (float)ent.value / (float)(ent.displayMaximum > 0 ? ent.displayMaximum : 100) : 0;
					int num = Math.round(percent * 5F);
					int leftNum = 5 - num;
					
					if (!name.isEmpty()) {
						if (ent.displayAsNumeric || (num < 0 || leftNum < 0)) {
							String text = "*" + name + "*:\n";
							String valueText = "**" + value + "**\n";
							if (stringsBuilder.toString().length() + text.length() >= MessageEmbed.VALUE_MAX_LENGTH || valuesBuilder.toString().length() + valueText.length() >= MessageEmbed.VALUE_MAX_LENGTH) {
								if (!stringsBuilder.toString().isBlank()) {
									builder.addField("Stats", stringsBuilder.toString(), true);
								}
								
								if (!valuesBuilder.toString().isBlank()) {
									builder.addField(EmbedBuilder.ZERO_WIDTH_SPACE, valuesBuilder.toString(), true);
								}
								
								if (!valuesBuilder.toString().isBlank() || !stringsBuilder.toString().isBlank()) {
									builder.addField(EmbedBuilder.ZERO_WIDTH_SPACE, EmbedBuilder.ZERO_WIDTH_SPACE, false);
								}
								
								stringsBuilder = new StringBuilder();
								valuesBuilder = new StringBuilder();
							}
							
							stringsBuilder.append(text);
							valuesBuilder.append(valueText);
						}
					}
				}
			}
			
			for (BaseStatObject ent : list) {
				String name = Utils.limitString(ent.getName(), 20);
				boolean displayTime = name.toLowerCase().contains("time");
				String value = ent.value + (displayTime ? " ms " : "");
				
				if (displayTime && ent.value == 0) continue;
				
				if (!object.isEquippable() && ent.value > 0) {
					stringsBuilder.append("*").append(name).append("*: **").append("+").append(value).append("**\n");
				}
			}
			
			
			if (!stringsBuilder.toString().isBlank()) {
				builder.addField("Stats", stringsBuilder.toString(), true);
			}
			
			if (!valuesBuilder.toString().isBlank()) {
				builder.addField(EmbedBuilder.ZERO_WIDTH_SPACE, valuesBuilder.toString(), true);
			}
		}

		if(showPerks) {
			//Get socket names and descriptions
			HashMap<Integer, ArrayList<SocketObject>> objects = object.getPerks();
			
			for (Map.Entry<Integer, ArrayList<SocketObject>> ent : objects.entrySet()) {
				StringJoiner joiner = new StringJoiner(" | ");
				StringBuilder builder2 = new StringBuilder();
				boolean isMod = false;
				
				int cur = 1;
				for (SocketObject socket : ent.getValue()) {
					String subDesc = "";
					
					if (socket.hash != null) {
						DestinyItemObject perkObject = DestinyItemSystem.destinyItemObjects.getOrDefault(socket.hash.intValue(), null);
						
						if (perkObject != null) {
							Collection<BaseStatObject> stats;
							
							stats = perkObject.getStats().values();
							if (perkObject.itemTypeDisplayName.toLowerCase().contains("mod")) {
								isMod = true;
							}
							
							//noinspection ConstantValue
							if (stats != null) {
								if (stats.size() > 0) {
									StringJoiner joiner1 = new StringJoiner("\n");
									
									for (BaseStatObject stat : stats) {
										if (stat.getName() == null) continue;
										joiner1.add("*" + stat.getName() + "*: **" + (stat.value > 0 ? "+" : "") + stat.value + "**");
									}
									
									subDesc = joiner1.toString();
								}
								
								if (subDesc.length() > 1) {
									subDesc = subDesc.substring(0, subDesc.length() - 1);
								}
							}
						}
					}
					
					
					//noinspection ConstantValue
					if (socket != null) {
						if (socket.name != null && socket.description != null) {
							
							if (socket.socketGroup == 8) continue;
							
							if (!socket.name.isEmpty() && !socket.description.isEmpty()) {
								//TODO Fix these more
								if (joiner.toString().length() + socket.name.length() >= MessageEmbed.TITLE_MAX_LENGTH) {
									break;
								}
								
								if (builder2.toString().length() + socket.description.length() >= MessageEmbed.VALUE_MAX_LENGTH) {
									break;
								}
								
								boolean isMulti = ent.getValue().size() > 1;
								
								String desc = socket.description;
								
								if (desc.startsWith("\n")) {
									desc = desc.replaceFirst("\n", "");
								}
								
								if (!subDesc.isEmpty()) {
									if (!(builder2.toString().length() + socket.description.length() + subDesc.length() >= MessageEmbed.VALUE_MAX_LENGTH)) {
										desc += "*\n" + subDesc;
									}
								}
								
								joiner.add((isMulti ? (cur + " - ") : "") + socket.name);
								builder2.append(isMulti ? (cur + ") ") : "").append("*").append(desc).append("*\n\n");
							}
						}
					}
					
					
					cur += 1;
				}
				
				if (joiner.toString().length() > 0 && builder2.toString().length() > 0) {
					builder.addField((isMod ? "Mod: **" : "[") + joiner + (isMod ? "**" : "]"), builder2.toString(), false);
				}
			}
		}
		
		DestinyItemObject d2Object = object;

		if (d2Object.masterworkObjects.size() > 0) {
			StringJoiner joiner = new StringJoiner(", ");

			for (MasterworkObject object1 : d2Object.masterworkObjects) {
				joiner.add("`" + object1.statName() + " " + object1.name() + "`");
			}

			builder.addField("Masterworks", String.valueOf(joiner), false);
		}

		if (object.source != null && !object.source.isEmpty()) {
			String text = object.source;

			if (text.startsWith("Source: ")) {
				text = text.substring("Source: ".length());
			}

			builder.addField("Source", "***" + text + "***", false);
		}
		
		if(d2Object.objectives != null){
			StringBuilder objectives = new StringBuilder();
			for(Long objective : d2Object.objectives.objectiveHashes()){
				DestinyObjectiveObject objectiveObject = DestinyItemSystem.destinyObjectiveObjects.get(objective.intValue());
				
				if(objectiveObject == null){
					continue;
				}
				
				String valueText = "[" + Strings.repeat(DestinySystem.BAR_FULL_ICON, 0) + Strings.repeat(DestinySystem.BAR_EMPTY_ICON, 8) + "] **0 / " + objectiveObject.completionValue + "**\n";
				objectives.append("*" + objectiveObject.progressDescription + "*").append("\n").append(valueText).append("\n\n");
			}
			
			if(objectives.toString().length() > 0){
				builder.addField("Objectives", objectives.toString(), false);
			}
		}
		
		if(d2Object.value != null){
			StringBuilder rewards = new StringBuilder();
			
			for (ItemValue itemValue : d2Object.value.itemValue()) {
				DestinyItemObject item = DestinyItemSystem.destinyItemObjects.get(itemValue.itemHash().intValue());
				
				if(item == null){
					continue;
				}
				rewards.append("- ");
				
				if(itemValue.quantity() > 1){
					rewards.append(itemValue.quantity()).append("x ");
				}
				
				rewards.append("*").append(item.getName()).append("*\n");
			}
			
			if(rewards.toString().length() > 0){
				builder.addField("Rewards", rewards.toString(), false);
			}
		}
		
		List<ActionComponent> buttons = new ArrayList<>();
		buttons.add(Button.link("https://www.light.gg/db/items/" + object.hash, "View on Light.gg"));
		
		top:
		for (DestinyRecordObject record : DestinyItemSystem.destinyRecordObjects.values()) {
			if(record.rewardItems != null){
				for (DestinyRewardObject rewardsItem : record.rewardItems) {
					if(rewardsItem.itemHash() == object.hash){
						buttons.add(Button.secondary("destiny_source_record:" + record.hash, "View Source"));
						break top;
					}
				}
			}
		}
		
		event.replyEmbeds(builder.build())
				.setEphemeral(!publicCommand)
				.setActionRow(buttons)
				.queue(e -> {}, e -> ChatUtils.sendMessage(channel, "Unable to show the item in public in this channel.", builder.build()));
	}
	
	@EventListener
	public static void buttonEvent(ButtonInteractionEvent event)
	{
		boolean ephemeral = event.getMessage().isEphemeral();
		String id = event.getButton().getId();
		
		if(id.startsWith("destiny_source_record:")){
			String recordId = id.substring(id.indexOf(":")+1);
			Long longId = Long.parseLong(recordId);
			DestinyRecordObject record = DestinyItemSystem.destinyRecordObjects.get(longId.intValue());
			
			if(record == null){
				event.reply("Unable to find the triumph.").setEphemeral(ephemeral).queue();
				return;
			}
			
			EmbedBuilder builder = new EmbedBuilder();
			
			builder.setAuthor("Destiny 2", null, null);
			builder.setThumbnail(Destiny2UserUtil.BASE_BUNGIE_URL + record.displayProperties.icon);
			builder.setTitle(record.displayProperties.name);
			builder.setDescription(record.displayProperties.description);
			
			builder.setColor(new Color(195, 188, 180));
			
			JSONObject object = Destiny2UserUtil.getData(Destiny2UserUtil.getAccessToken(event.getUser()), Destiny2UserUtil.GET_MEMBERSHIP_PATH);
			
			JSONObject profileProgress = null;
			
			if (object != null) {
				JSONObject membership = null;
				String primaryId = object.getString("primaryMembershipId");
				
				if (object.has("destinyMemberships")) {
					JSONArray array = object.getJSONArray("destinyMemberships");
					
					for (Object ob1 : array) {
						if (ob1 instanceof JSONObject arrayObject) {
							
							if(arrayObject.getString("membershipId").equals(primaryId)){
								membership = arrayObject;
								break;
							}
						}
					}
				}
				
				
				if(membership != null) {
					String components = "?components=900";
					String membershipId = membership.getString("membershipId");
					int membershipType = membership.getInt("membershipType");
					JSONObject profileObject = Destiny2UserUtil.getData(Destiny2UserUtil.getAccessToken(event.getUser()), "/Platform/Destiny2/" + membershipType + "/Profile/" + membershipId + "/" + components);
					
					if (profileObject != null) {
						if (profileObject.has("profileRecords")) {
							JSONObject profileRecords = profileObject.getJSONObject("profileRecords");
							JSONObject data = profileRecords.getJSONObject("data");
							JSONObject records = data.getJSONObject("records");
							Long hash = record.hash;
							
							if (records.has(hash.toString())) {
								profileProgress = records.getJSONObject(hash.toString());
							}
						}
					}
				}
			}
			
			for(Long objective : record.objectiveHashes){
				DestinyObjectiveObject objectiveObject = DestinyItemSystem.destinyObjectiveObjects.get(objective.intValue());
				
				if(objectiveObject == null){
					continue;
				}
				
				int barLength = 8;
				
				int curNum = 0;
				int completeNum = objectiveObject.completionValue;
				boolean complete = curNum > completeNum;
				
				if(profileProgress != null){
					if(profileProgress.has("objectives")){
						JSONArray objectives = profileProgress.getJSONArray("objectives");
						
						for(Object ob1 : objectives){
							if(ob1 instanceof JSONObject objective1){
								if(objective1.has("objectiveHash") && objective1.getLong("objectiveHash") == objective){
									if(objective1.has("progress")){
										curNum = objective1.getInt("progress");
									}
									
									if(objective1.has("complete")) {
										complete = objective1.getBoolean("complete");
									}
								}
							}
						}
					}
				}
				
				int progress = (completeNum > 0 ? Math.round((float)curNum / (float)completeNum) : (complete ? 1 : 0)) * barLength;
				
				String valueText = "[" + Strings.repeat(DestinySystem.BAR_FULL_ICON, progress) + Strings.repeat(DestinySystem.BAR_EMPTY_ICON, barLength-progress) + "] **" + curNum + " / " + completeNum + "**\n";
				builder.addField(objectiveObject.progressDescription, valueText, false);
			}
			
			StringBuilder rewards = new StringBuilder();
			
			for(DestinyRewardObject reward : record.rewardItems){
				DestinyItemObject item = DestinyItemSystem.destinyItemObjects.get(reward.itemHash().intValue());
				
				if(item == null){
					continue;
				}
				
				rewards.append("- ");
				
				if(reward.quantity() > 1){
					rewards.append(reward.quantity()).append("x ");
				}
				
				rewards.append("*").append(item.getName()).append("*\n");
			}
			
			if(rewards.toString().length() > 0){
				builder.addField("Rewards", rewards.toString(), false);
			}
			
			event.replyEmbeds(builder.build())
					.setEphemeral(ephemeral)
					.setActionRow(Button.link("https://www.light.gg/db/legend/triumphs/" + record.hash, "View on Light.gg"))
					.queue(e -> {}, e -> ChatUtils.sendMessage(event.getChannel(), "Unable to show the item in public in this channel.", builder.build()));
		}
	}
}