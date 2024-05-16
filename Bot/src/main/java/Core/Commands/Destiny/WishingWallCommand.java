package Core.Commands.Destiny;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.ComponentSystem.ComponentResponseSystem;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.CommandSystem.SlashMessageBuilder;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Method.Startup.Init;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Command
public class WishingWallCommand implements ISlashCommand
{
	private static final ConcurrentHashMap<Integer, wishObject> wishes = new ConcurrentHashMap<>();

	public enum wishObject
	{
		Wish1(1, "TO FEED AN ADDICTION", "Gives an Etheral Key, used in the final room to open a chest.\n" + "This plate is single use. Once redeemed, can not be redeemed again.", "https://idleanimation.com/img/plates/wish1.jpg", "Loot", "The Shattered Throne: After the Ogre boss fight, under the broken bridge."),
		Wish2(2, "MATERIAL VALIDATION", "Will spawn a chest between the third and fourth encounters. Requires Glittering Key to open.", "https://idleanimation.com/img/plates/wish2.jpg", "Loot", "Last Wish: On the ceiling before the Riven encounter."),
		Wish3(3, "OTHERS TO CELEBRATE YOUR SUCCESS", "Unlocks the \"Numbers of Power\" Emblem.", "https://idleanimation.com/img/plates/wish3.jpg", "Loot", " Dreaming City Cut-scene: On the Titans shield."),
		Wish4(4, "TO LOOK ATHLETIC AND ELEGANT", "Teleport to the second encounter (Shuro Chi).", "https://idleanimation.com/img/plates/wish4.jpg", "Checkpoint", "Last Wish: In a cave after the large bridge, before the second encounter."),
		Wish5(5, "FOR A PROMISING FUTURE", "Teleport to the third encounter (Morgeth).", "https://idleanimation.com/img/plates/wish5.jpg", "Checkpoint", "Last Wish: In the ascendant realm after Shuro Chi."),
		Wish6(6, "TO MOVE THE HANDS OF TIME", "Teleport to the fourth encounter (The Vault).", "https://idleanimation.com/img/plates/wish6.jpg", "Checkpoint", "Last Wish: Before the elevator to The Vault."),
		Wish7(7, "TO HELP A FRIEND IN NEED", "Teleport to the fifth encounter (Riven).", "https://idleanimation.com/img/plates/wish7.jpg", "Checkpoint", "Last Wish: In Riven's jumping puzzle room."),
		Wish8(8, "TO STAY HERE FOREVER", "Plays the song \"Hope for the Future\".", "https://idleanimation.com/img/plates/wish8.jpg", "Audio", "Last Wish: On a ledge near Shuro Chii."),
		Wish9(9, "TO STAY HERE FOREVER", "Failsafe will join you in the raid for each encounter.", "https://idleanimation.com/img/plates/wish9.jpg", "Audio", "Last Wish: In the roof of a building between encounter 2 and 3."),
		Wish10(10, "TO STAY HERE FOREVER", "The Drifter will join you in the raid for each encounter. ", "https://idleanimation.com/img/plates/wish10.jpg", "Audio", "Cathedral of Stars: Dreaming City Gambit Map."),
		Wish11(11, "TO STAY HERE FOREVER", "Grunt Birthday Party!", "https://idleanimation.com/img/plates/wish11.jpg", "Audio", "Nessus: In the Sunkern Cavern."),
		Wish12(12, "TO OPEN YOUR MIND TO NEW IDEAS", "Gives all Fireteam members a random head ornament/effect.", "https://idleanimation.com/img/plates/wish12.jpg", "Visual", "Titan: In Sirens Watch."),
		Wish13(13, "FOR THE MEANS TO FEED AN ADDICTION", "If a single person in the Fireteam dies, the entire Raid fails and you are sent to orbit.", "https://idleanimation.com/img/plates/wish13.jpg", "Petras Run", "Last Wish: In the Chest room after Riven."),
		Wish14(14, "FOR LOVE AND SUPPORT", "Spawns corrupted eggs throughout the raid, which can be destroyed with Wish Ender.", "https://idleanimation.com/img/plates/wish14.jpg", "Eggs", "The Shattered Throne: Mara Sov's throne world."),
		Wish15(15, "UNKWNON", "UNKWNON", null, "UNKWNON", "UNKWNON");

		final int num;
		final String name;
		final String description;
		final String effect;
		final String imageUrl;
		final String plateLocation;

		wishObject(int num, String name, String description, String imageUrl, String effect, String plateLocation)
		{
			this.num = num;
			this.name = name.trim();
			this.description = description.trim();
			this.imageUrl = imageUrl;
			this.effect = effect.trim();
			this.plateLocation = plateLocation.trim();
		}
	}

	@Init
	public static void init(){
		for(wishObject object : wishObject.values()){
			wishes.put(object.num, object);
		}
	}

	@Override
	public String commandName()
	{
		return "destiny-wish";
	}

	@Override
	public String getDescription()
	{
		return "Search up Last wish, wishing wall combinations";
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent event1, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		EmbedBuilder builder = new EmbedBuilder();

		ArrayList<ItemComponent> actions = new ArrayList<>();

		UUID menuID = UUID.randomUUID();
		StringSelectMenu.Builder menuBuilder = StringSelectMenu.create(menuID.toString()).setPlaceholder("Select the wish").setRequiredRange(1, 1);

		for(Entry<Integer, wishObject> ent : wishes.entrySet()){
			if(ent.getValue().name.equalsIgnoreCase("UNKWNON")) continue;

			menuBuilder.addOptions(SelectOption.of("Wish " + ent.getKey() + " | " + ent.getValue().effect, ent.getKey().toString()).withDescription(
					Utils.limitString(ent.getValue().description, 50)));
		}

		actions.add(ComponentResponseSystem.addComponent(menuID, author, event1, menuBuilder.build(), (e) -> {
			StringSelectInteractionEvent event = (StringSelectInteractionEvent)e;

			wishObject object = wishes.get(Integer.parseInt(event.getValues().get(0)));
			EmbedBuilder builder1 = new EmbedBuilder();

			builder1.setTitle("Wish " + object.num + " - " + object.name);
			builder1.setImage(object.imageUrl);

			if (!object.effect.isBlank()) {
				builder1.setDescription("**Effect: " + object.effect + "**\n\n*" + object.description + "*");
			} else {
				builder1.setDescription("*" + object.description + "*");
			}

			if (!object.plateLocation.isBlank()) {
				String t = object.plateLocation;

				if (t.startsWith("Plate Location: ")) {
					t = t.substring("Plate Location: ".length());
				}

				t = "*" + t + "*";

				builder1.addField("Plate Location", t, false);
			}

			builder1.setFooter("https://idleanimation.com/last-wish-plates", null);

			ChatUtils.sendMessage(channel, builder1.build());
		}));

		builder.setTitle("Please select which of the wish codes you want to view by selecting it in the selection menu below.");

		SlashMessageBuilder slashBuilder = ChatUtils.createSlashMessage(author, channel);
		slashBuilder.withEmbed(builder);
		slashBuilder.withActions(actions);
		slashBuilder.send();
	}
}