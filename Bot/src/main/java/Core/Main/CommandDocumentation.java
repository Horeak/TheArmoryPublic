package Core.Main;

import Core.CommandSystem.SlashCommands.SlashCommandUtils;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Objects.Annotation.Debug;
import Core.Objects.Annotation.Method.Startup.PreInit;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class CommandDocumentation
{
	@PreInit
	public static void run(){
		if(Startup.jarFile || !Startup.debug) return;
		
		make(false);
		make(true);
	}
	
	public static void make(boolean internal){
		StringBuilder overview = new StringBuilder();
		HashMap<String, ArrayList<String>> commandOverviews = new HashMap<>();
		HashMap<String, ArrayList<String>> finalCommandOverviews = new HashMap<>();
		
		StringBuilder content = new StringBuilder();
		
		Set<Class<?>> commands = Startup.getReflection().getTypesAnnotatedWith(Command.class);
		Set<Class<?>> subCommands = Startup.getReflection().getTypesAnnotatedWith(SubCommand.class);
		
		ArrayList<ISlashCommand> slashCommands = new ArrayList<>();
		ArrayList<ISlashCommand> subSlashCommands = new ArrayList<>();
		
		if(!internal) {
			commands.removeIf(c -> c.isAnnotationPresent(Debug.class));
			subCommands.removeIf(c -> c.isAnnotationPresent(Debug.class));
		}
		
		for (Class<?> shC : commands) {
			try {
				ISlashCommand sh = (ISlashCommand)shC.getDeclaredConstructor().newInstance();
				slashCommands.add(sh);
				
				commandOverviews.computeIfAbsent(shC.getPackageName(), (s) -> new ArrayList<>());
				commandOverviews.get(shC.getPackageName()).add(sh.commandName());
				
			} catch (Exception e) {
				Logging.exception(e);
			}
		}
		
		overview.append("# Overview\n");
		for(Entry<String, ArrayList<String>> ent : commandOverviews.entrySet()) {
			String key = ent.getKey();
			key = key.replace("Core.Commands.", "");
			
			if (key.contains(".")) key = key.substring(0, key.indexOf("."));
			
			if(key.isBlank()) {
				key = "Commands";
			}
			
			finalCommandOverviews.computeIfAbsent(key, (s) -> new ArrayList<>());
			finalCommandOverviews.get(key).addAll(ent.getValue());
		}
		
		for(Entry<String, ArrayList<String>> ent : finalCommandOverviews.entrySet()){
			if(!ent.getKey().equalsIgnoreCase("commands")){
				overview.append("## ").append(ent.getKey()).append("\n");
			}else {
				overview.append("\n");
			}
			for(String s : ent.getValue()){
				overview.append("- ").append("[" + s + "](#" + s + ")").append("\n");
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
		
		content.append("\n---\n\n# Commands\n");
		
		for(ISlashCommand slashCommand : slashCommands) {
			var cname = "/" + slashCommand.commandName();
			
			if(internal){
				var path = slashCommand.getClass().getName().replace(".", "/");
				if(path.contains("$")) path = path.substring(0, path.indexOf("$"));
				cname = "[" + cname + "](" + ("../src/main/java/" + path + ".java") + ")";
			}
			
			content.append("# ").append(cname).append("\n");
			if (slashCommand.getDescription() != null) content.append(slashCommand.getDescription()).append("\n\n");
			List<OptionData> argumentList = SlashCommandUtils.getOptions(slashCommand);
			
			boolean hasSubCommands = false;
			
			StringBuilder arguments = new StringBuilder();
			
			if (!argumentList.isEmpty()) {
				arguments.append("| Argument | Description | Required |\n");
				arguments.append("| --- | --- | --- |\n");
				
				for (OptionData option : argumentList.stream().sorted((c1, c2) -> Boolean.compare(c2.isRequired(), c1.isRequired())).toList()) {
					arguments.append("| ").append(option.getName()).append(" | ").append(option.getDescription()).append(" | ").append(option.isRequired()).append(" |\n");
				}
				arguments.append("\n");
				content.append(arguments);
			}
			
			for (ISlashCommand subCommand : subSlashCommands) {
				SubCommand sd = subCommand.getClass().getDeclaredAnnotation(SubCommand.class);
				
				if (sd == null) continue;
				if (sd != null && sd.value() == slashCommand.getClass()) {
					hasSubCommands = true;
					break;
				}
			}
			
			if (!hasSubCommands) {
				StringBuilder usage = new StringBuilder();
				usage.append("/" + slashCommand.commandName());
				
				for (OptionData option : argumentList.stream().sorted((c1, c2) -> Boolean.compare(c2.isRequired(), c1.isRequired())).toList()) {
					usage.append(" ");
					var name = option.getName();
					if (option.isRequired()) {
						usage.append("\\<").append(name).append(">");
					} else {
						usage.append("[").append(name).append("]");
					}
				}
				
				content.append("Usage:\n> ").append(usage).append("\n\n");
			}
			
			if (hasSubCommands) {
				content.append("#### Sub Commands\n");
				
				for (ISlashCommand subCommand : subSlashCommands) {
					SubCommand sd = subCommand.getClass().getDeclaredAnnotation(SubCommand.class);
					
					if (sd == null) continue;
					if (sd != null && sd.value() == slashCommand.getClass()) {
						content.append("<details>\n").append("<summary>").append("/" + slashCommand.commandName() + " " + subCommand.commandName()).append("</summary>").append("\n");
						if (subCommand.getDescription() != null) content.append(subCommand.getDescription()).append("\n\n");
						
						List<OptionData> SubargumentList = SlashCommandUtils.getOptions(subCommand);
						StringBuilder Subarguments = new StringBuilder();
						
						if (!SubargumentList.isEmpty()) {
							Subarguments.append("| Argument | Description | Required |\n");
							Subarguments.append("| --- | --- | --- |\n");
							
							for (OptionData option : SubargumentList.stream().sorted((c1, c2) -> Boolean.compare(c2.isRequired(), c1.isRequired())).toList()) {
								Subarguments.append("| ").append(option.getName()).append(" | ").append(option.getDescription()).append(" | ").append(option.isRequired()).append(" |\n");
							}
							Subarguments.append("\n");
							content.append(Subarguments.toString());
						}
						
						content.append("\n");
						
						StringBuilder usage = new StringBuilder();
						usage.append("/" + slashCommand.commandName() + " " + subCommand.commandName());
						
						for (OptionData option : SubargumentList.stream().sorted((c1, c2) -> Boolean.compare(c2.isRequired(), c1.isRequired())).toList()) {
							usage.append(" ");
							var name = option.getName();
							if (option.isRequired()) {
								usage.append("< ").append(name).append(" >");
							} else {
								usage.append("[ ").append(name).append(" ]");
							}
						}
						
						content.append("Usage:\n> ").append(usage).append("\n</details>\n");
					}
				}
			}
			
			content.append("\n---\n\n");
		}
		
		File file = new File(Startup.baseFilePath, internal ? "docs/commands-internal.md" : "docs/commands.md");
		file.mkdirs();
		
		try {
			var list = new ArrayList<String>();
			list.addAll(List.of(overview.toString().split("\n")));
			list.addAll(List.of(content.toString().split("\n")));
			
			FileUtils.delete(file);
			file.createNewFile();
			
			FileUtils.writeLines(file, list);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
