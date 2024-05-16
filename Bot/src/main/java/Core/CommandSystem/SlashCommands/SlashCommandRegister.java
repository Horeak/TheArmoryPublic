package Core.CommandSystem.SlashCommands;

import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Commands.CommandGroup;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Objects.Annotation.Method.EventListener;
import Core.Objects.Annotation.Method.Startup.Init;
import Core.Objects.Interfaces.Commands.IBaseSlashCommand;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.Utils;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.apache.commons.lang3.LocaleUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SlashCommandRegister
{
	public static final ConcurrentHashMap<String, String> defaultTranslations = new ConcurrentHashMap<>();
	private static final CopyOnWriteArrayList<CommandData> commands = new CopyOnWriteArrayList<>();
	
	@Init
	public static void PostInit()
	{
		Set<Class<?>> commands = Startup.getReflection().getTypesAnnotatedWith(Command.class);
		Set<Class<?>> subCommands = Startup.getReflection().getTypesAnnotatedWith(SubCommand.class);
		Set<Class<?>> subCommandGroups = Startup.getReflection().getTypesAnnotatedWith(CommandGroup.class);
		
		ArrayList<ISlashCommand> slashCommands = new ArrayList<>();
		ArrayList<ISlashCommand> subSlashCommands = new ArrayList<>();
		ArrayList<IBaseSlashCommand> subSlashCommandGroups = new ArrayList<>();
		
		for (Class<?> shC : commands) {
			try {
				ISlashCommand sh = (ISlashCommand)shC.getDeclaredConstructor().newInstance();
				slashCommands.add(sh);
			} catch (Exception e) {
				Logging.exception(e);
			}
		}
		
		for (Class<?> shC : subCommands) {
			try {
				ISlashCommand sh = (ISlashCommand)shC.getDeclaredConstructor().newInstance();
				subSlashCommands.add(sh);
			} catch (Exception e) {
				Logging.exception(e);
			}
		}
		
		for (Class<?> shC : subCommandGroups) {
			try {
				IBaseSlashCommand sh = (IBaseSlashCommand)shC.getDeclaredConstructor().newInstance();
				subSlashCommandGroups.add(sh);
			} catch (Exception e) {
				Logging.exception(e);
			}
		}
		
		System.out.println("Found " + commands.size() + " commands, " + subCommands.size() + " subcommands and " + subCommandGroups.size() + " subcommand groups");
		
		for (ISlashCommand command : slashCommands) {
			if (command == null || command.commandName() == null) {
				System.err.println("Ignoring command from " + command);
				continue;
			}
			
			String name = Utils.limitString(command.commandName().toLowerCase(), 32);
			String desc = command.getDescription() != null ? Utils.limitString(command.getDescription(), 100) : "Missing description.";
			
			if (name.isBlank()) continue;
			
			SlashCommandData newCommand = Commands.slash(name, desc);
			List<OptionData> argumentList = SlashCommandUtils.getOptions(command);
			
			newCommand.setDefaultPermissions(command.commandPrivileges());
			
			if (command.getDescription() != null) defaultTranslations.put("commands.description." + command.commandName().toLowerCase().replace(" ", "_"), command.getDescription());
			if (command.commandName() != null) defaultTranslations.put("commands.name." + command.commandName().toLowerCase().replace(" ", "_"), command.commandName());
			
			for (DiscordLocale lang : Startup.LANGS) {
				try {
					ResourceBundle COMMAND_LANG = ResourceBundle.getBundle("commands", LocaleUtils.toLocale(lang.getLocale().replace("-", "_")));
					try {
						newCommand.setDescriptionLocalization(lang, COMMAND_LANG.getString("commands.description." + command.commandName().toLowerCase().replace(" ", "_")));
					} catch (MissingResourceException e1) {
						System.err.println("Missing localization for: " + e1.getKey() + ", for lang: " + lang);
					}
					
					try {
						newCommand.setNameLocalization(lang, COMMAND_LANG.getString("commands.name." + command.commandName().toLowerCase().replace(" ", "_")).toLowerCase());
					} catch (MissingResourceException e1) {
						System.err.println("Missing localization for: " + e1.getKey() + ", for lang: " + lang);
					} catch (Exception e2) {
						System.err.println("Invalid localization for: " + e2.getMessage() + ", for lang: " + lang);
					}
					
				} catch (Exception e) {
					Logging.exception(e);
				}
			}
			
			if (argumentList.size() > 0) {
				argumentList.sort((c1, c2) -> Boolean.compare(c2.isRequired(), c1.isRequired()));
				newCommand.addOptions(argumentList);
			}
			
			if (command.commandOptions().length > 0) {
				newCommand.addOptions(command.commandOptions());
			}
			
			for (ISlashCommand subCommand : subSlashCommands) {
				SubCommand sd = subCommand.getClass().getDeclaredAnnotation(SubCommand.class);
				
				if (sd != null && sd.value() == command.getClass()) {
					String sub_name = Utils.limitString(subCommand.commandName().toLowerCase(), 32);
					String sub_desc = subCommand.getDescription() != null ? Utils.limitString(subCommand.getDescription(), 100) : "Missing description.";
					
					SubcommandData subCommandData = new SubcommandData(sub_name, sub_desc);
					List<OptionData> subArgumentList = SlashCommandUtils.getOptions(subCommand);
					
					if (subArgumentList.size() > 0) {
						subArgumentList.sort((c1, c2) -> Boolean.compare(c2.isRequired(), c1.isRequired()));
						subCommandData.addOptions(subArgumentList);
					}
					
					if (subCommand.commandOptions().length > 0) {
						try {
							subCommandData.addOptions(subCommand.commandOptions());
						} catch (Exception e) {
							System.err.println("Error with sub command: " + subCommand);
							Logging.exception(e);
						}
					}
					
					newCommand.addSubcommands(subCommandData);
					SlashCommandUtils.command_registry.put(name + "/" + sub_name, subCommand);
				}
			}
			
			for (IBaseSlashCommand subCommandGroup : subSlashCommandGroups) {
				CommandGroup sc = subCommandGroup.getClass().getDeclaredAnnotation(CommandGroup.class);
				
				if (sc != null && sc.value() == command.getClass()) {
					String sub_g_name = Utils.limitString(subCommandGroup.commandName().toLowerCase(), 32);
					String sub_g_desc = subCommandGroup.getDescription() != null ? Utils.limitString(subCommandGroup.getDescription(), 100) : "Missing description.";
					
					SubcommandGroupData subCommandGroupData = new SubcommandGroupData(sub_g_name, sub_g_desc);
					
					for (ISlashCommand subCommand : subSlashCommands) {
						SubCommand sd = subCommand.getClass().getDeclaredAnnotation(SubCommand.class);
						
						if (sd != null && sd.value() == subCommandGroup.getClass()) {
							String sub_name = Utils.limitString(subCommand.commandName().toLowerCase(), 32);
							String sub_desc = subCommand.getDescription() != null ? Utils.limitString(subCommand.getDescription(), 100) : "Missing description.";
							
							SubcommandData subCommandData = new SubcommandData(sub_name, sub_desc);
							List<OptionData> subGroupArgumentList = SlashCommandUtils.getOptions(subCommand);
							
							if (subGroupArgumentList.size() > 0) {
								subGroupArgumentList.sort((c1, c2) -> Boolean.compare(c2.isRequired(), c1.isRequired()));
								subCommandData.addOptions(subGroupArgumentList);
							}
							
							if (subCommand.commandOptions().length > 0) {
								subCommandData.addOptions(subCommand.commandOptions());
							}
							
							subCommandGroupData.addSubcommands(subCommandData);
							SlashCommandUtils.command_registry.put(name + "/" + sub_g_name + "/" + sub_name, subCommand);
						}
					}
					
					newCommand.addSubcommandGroups(subCommandGroupData);
				}
			}
			
			SlashCommandRegister.commands.add(newCommand);
			SlashCommandUtils.command_registry.put(name, command);
		}
		
		var error = "Missing access to register commands globally.";
		if (!Startup.debug) {
			Startup.getClient().updateCommands().addCommands(SlashCommandRegister.commands).queue(e -> {}, e -> System.err.println(error));
		} else {
			Startup.getClient().updateCommands().queue(e -> {}, e -> System.err.println(error));
		}
	}
	
	@EventListener
	public static void registerCommands(GuildReadyEvent event)
	{
		var error = "Missing access to register commands in guild: " + event.getGuild().getName() + " (" + event.getGuild().getId() + ")";
		if (Startup.debug) {
			event.getGuild().updateCommands().addCommands(commands).queue(e -> {}, e -> System.err.println(error));
		} else {
			event.getGuild().updateCommands().queue(e -> {}, e -> System.err.println(error));
		}
	}
}