package Core.Commands.Generic.Status;


import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Objects.Annotation.Commands.ArgumentAutoComplete;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Debug;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.AutoComplete;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

@Debug
@Command
public class TestingCommand implements ISlashCommand
{
	@SlashArgument( name = "target", description = "The user to poke", required = true)
	public User user;

	@SlashArgument( name = "choice", description = "make a choice", choices = {"Choice 1", "Choice 2", "Choice 3"})
	public String choice;

	@SlashArgument(name = "choices", description = "Pick something")
	public String autoCompleteTest;

	@ArgumentAutoComplete("choices")
	public AutoComplete func = (e) -> List.of("val1", "val2", "val3");

	@Override
	public String commandName()
	{
		return "test";
	}

	@Override
	public String getDescription(){
		return null;
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		slashEvent.reply(choice + " | Poke -> " + user.getAsMention()).queue();
	}
}