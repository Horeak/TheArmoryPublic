package Core.Commands;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Random;

@Command
public class RandomNumberCommand implements ISlashCommand
{
	@Override
	public String getDescription()
	{
		return "Generates a random number between the upper and lower limit";
	}

	@Override
	public String commandName()
	{
		return "random";
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message) { }

	@SubCommand( value = RandomNumberCommand.class)
	public static class RNG_Between implements ISlashCommand{

		@SlashArgument( name = "upper", description = "The upper limit for the number", required = true )
		public int upper;

		@SlashArgument( name = "lower", description = "The lower limit for the number")
		public final int lower = 1;

		@Override
		public String commandName()
		{
			return "between";
		}

		@Override
		public String getDescription()
		{
			return "Simulate a random number with a upper and lower limit";
		}

		@Override
		public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
		{
			if (lower >= upper) {
				ChatUtils.sendEmbed(channel, "The lower limit needs to be a smaller number then the upper limit!");
				return;
			}

			int number = lower + new Random().nextInt(upper);

			ChatUtils.sendEmbed(channel, "Random number between **" + lower + "** and **" + upper + "** is: " + "**" + number + "**");
		}
	}

	@SubCommand( value = RandomNumberCommand.class)
	public static class RNG_Chance implements ISlashCommand{

		@SlashArgument( name = "chance", description = "The success chance you want to simulate", required = true )
		public String chance;

		@Override
		public String getDescription()
		{
			return "Simulate a random roll with a specific chance of success";
		}

		@Override
		public String commandName()
		{
			return "chance";
		}

		@Override
		public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
		{
			String chanceString = chance.replace("%", "").trim();
			double chance = 0;

			try {
				chance = Double.parseDouble(chanceString);
			}catch (Exception e){
				ChatUtils.sendEmbed(channel, "Please provide a number as the chance you wish to simulate!");
				return;
			}

			if(Utils.isDouble(chanceString)){
				chance = Double.parseDouble(chanceString);
			}else if(Utils.isFloat(chanceString)){
				chance = Float.parseFloat(chanceString);
			}else if(Utils.isInteger(chanceString)){
				chance = Integer.parseInt(chanceString);
			}

			if(chance == 0){
				ChatUtils.sendEmbed(channel, "Please provide a larger chance then 0");
				return;
			}

			double finalChance = 100 - chance;
			int number = 1 + new Random().nextInt(100);
			boolean success = number >= finalChance;

			double roundDbl = Math.round(chance*100.0)/100.0;

			ChatUtils.sendEmbed(channel, "You **" + (success ? "Succeeded" : "Failed") + "** with " + roundDbl + "% chance. There is a average of 1:" + Math.round(100 / roundDbl) + " chance of success.");
		}
	}
}