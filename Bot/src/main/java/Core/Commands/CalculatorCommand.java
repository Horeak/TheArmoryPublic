package Core.Commands;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

@Command
public class CalculatorCommand implements ISlashCommand
{
	@SlashArgument( name = "input", description = "The math question you want answered.", required = true )
	public String input;

	@Override
	public String getDescription()
	{
		return "A command for answering simple math equations";
	}

	@Override
	public String commandName()
	{
		return "calculator";
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		try {
			ExpressionBuilder builder = new ExpressionBuilder(input);

			Expression exp = builder.build();
			String result = Double.toString(exp.evaluate());

			if (result.endsWith(".0")) {
				result = result.substring(0, result.length() - 2);
			}
			
			ChatUtils.sendEmbed(channel, "*Answer to* **" + input + "** *is* ***" + result + "*** ");
		} catch (Exception e) {
			ChatUtils.sendEmbed(channel, author.getAsMention() + " " + e.getMessage());
		}
	}
}