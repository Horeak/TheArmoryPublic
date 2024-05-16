package Core.CommandSystem.SlashCommands;

import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Commands.ArgumentAutoComplete;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Method.Startup.PreInit;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.AutoComplete;
import com.google.common.primitives.Primitives;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SlashCommandUtils
{
	public static final ConcurrentHashMap<String, ISlashCommand> command_registry = new ConcurrentHashMap<>();

	@PreInit
	public static void init(){
		/*
		 * Option choices
		 */
		addChoiceMapping(OptionType.NUMBER, (c) -> true, (c, opt, arg) -> {
			if(arg.maxValue() != -1 || arg.minValue() != -1) opt.setRequiredRange(arg.minValue(), arg.maxValue());
		});
		addChoiceMapping(OptionType.INTEGER, (c) -> true, (c, opt, arg) -> {
			if(arg.maxValue() != -1 || arg.minValue() != -1) opt.setRequiredRange((int)arg.minValue(), (int)arg.maxValue());
		});
		addChoiceMapping(OptionType.CHANNEL, StageChannel.class, (c, opt, arg) -> opt.getChannelTypes().add(ChannelType.STAGE));
		addChoiceMapping(OptionType.CHANNEL, VoiceChannel.class, (c, opt, arg) -> opt.getChannelTypes().add(ChannelType.VOICE));
		addChoiceMapping(OptionType.CHANNEL, PrivateChannel.class, (c, opt, arg) -> opt.getChannelTypes().add(ChannelType.PRIVATE));
		addChoiceMapping(OptionType.CHANNEL, TextChannel.class, (c, opt, arg) -> opt.getChannelTypes().add(ChannelType.TEXT));
		addChoiceMapping(OptionType.CHANNEL, Category.class, (c, opt, arg) -> opt.getChannelTypes().add(ChannelType.CATEGORY));
		addChoiceMapping(OptionType.CHANNEL, NewsChannel.class, (c, opt, arg) -> opt.getChannelTypes().add(ChannelType.NEWS));


		for(OptionType value : OptionType.values()){
			if(value.canSupportChoices()){
				addChoiceMapping(value, Class::isEnum, (c, opt, arg) -> {
					for(Object en : c.getEnumConstants()){
						opt.addChoice(StringUtils.capitalize(((Enum<?>)en).name().replace("_", " ").toLowerCase(Locale.ROOT)), ((Enum<?>)en).name());
					}
				});
				addChoiceMapping(value, (c) -> !c.isEnum(), (c, opt, arg) -> Arrays.stream(arg.choices()).forEach((s) -> {
					int l = 0;

					for(String t : arg.choices()){
						if(!t.isEmpty()){
							if(opt.getChoices().stream().anyMatch(c1 -> c1.getName().equals(t)) || opt.getChoices().size() >= OptionData.MAX_CHOICES){
								break;
							}

							if(value == OptionType.STRING)
								opt.addChoice(t, t);
							else
								opt.addChoice(t, l);
							l++;
						}
					}
				}));

			}
		}


		/*
		 * Option types
		 */
		addTypeMapping(boolean.class, OptionType.BOOLEAN);

		addTypeMapping(int.class, OptionType.INTEGER);
		addTypeMapping(long.class, OptionType.INTEGER);

		addTypeMapping(float.class, OptionType.NUMBER);
		addTypeMapping(double.class, OptionType.NUMBER);

		addTypeMapping(String.class, OptionType.STRING);
		addTypeMapping(Class::isEnum, OptionType.STRING);

		addTypeMapping(User.class, OptionType.USER);
		addTypeMapping(Member.class, OptionType.USER);

		addTypeMapping(Role.class, OptionType.ROLE);

		addTypeMapping(TextChannel.class, OptionType.CHANNEL);
		addTypeMapping(MessageChannel.class, OptionType.CHANNEL);
		addTypeMapping(GuildChannel.class, OptionType.CHANNEL);
		addTypeMapping(VoiceChannel.class, OptionType.CHANNEL);
		addTypeMapping(StageChannel.class, OptionType.CHANNEL);
		addTypeMapping(NewsChannel.class, OptionType.CHANNEL);
		addTypeMapping(ThreadChannel.class, OptionType.CHANNEL);

		addTypeMapping(Attachment.class, OptionType.ATTACHMENT);

		/*
		 * Option Data
		 */
		addReturnValueMapping(OptionType.STRING, Class::isEnum, (OptionMapping opt, Class<?> c) -> Enum.valueOf((Class)c, opt.getAsString()));
		addReturnValueMapping(OptionType.STRING, String.class, (opt, c) -> opt.getAsString());

		addReturnValueMapping(OptionType.BOOLEAN, boolean.class, (opt, c) -> opt.getAsBoolean());

		addReturnValueMapping(OptionType.INTEGER, int.class, (opt, c) -> opt.getAsInt());
		addReturnValueMapping(OptionType.INTEGER, float.class, (opt, c) -> opt.getAsDouble());
		addReturnValueMapping(OptionType.INTEGER, double.class, (opt, c) -> opt.getAsDouble());
		addReturnValueMapping(OptionType.INTEGER, long.class, (opt, c) -> opt.getAsLong());

		addReturnValueMapping(OptionType.NUMBER, int.class, (opt, c) -> opt.getAsInt());
		addReturnValueMapping(OptionType.NUMBER, float.class, (opt, c) -> opt.getAsDouble());
		addReturnValueMapping(OptionType.NUMBER, double.class, (opt, c) -> opt.getAsDouble());
		addReturnValueMapping(OptionType.NUMBER, long.class, (opt, c) -> opt.getAsLong());

		addReturnValueMapping(OptionType.CHANNEL, Category.class, (Opt, c) -> Opt.getAsChannel().asCategory());
		addReturnValueMapping(OptionType.CHANNEL, ThreadChannel.class, (Opt, c) -> Opt.getAsChannel().asThreadChannel());
		addReturnValueMapping(OptionType.CHANNEL, NewsChannel.class, (Opt, c) -> Opt.getAsChannel().asNewsChannel());
		addReturnValueMapping(OptionType.CHANNEL, VoiceChannel.class, (Opt, c) -> Opt.getAsChannel().asVoiceChannel());
		addReturnValueMapping(OptionType.CHANNEL, StageChannel.class, (Opt, c) -> Opt.getAsChannel().asStageChannel());
		addReturnValueMapping(OptionType.CHANNEL, GuildChannel.class, (Opt, c) -> Opt.getAsChannel().asGuildMessageChannel());
		addReturnValueMapping(OptionType.CHANNEL, MessageChannel.class, (Opt, c) -> Opt.getAsChannel());
		addReturnValueMapping(OptionType.CHANNEL, TextChannel.class, (Opt, c) -> Opt.getAsChannel().asTextChannel());

		addReturnValueMapping(OptionType.USER, User.class, (Opt, c) -> Opt.getAsUser());
		addReturnValueMapping(OptionType.USER, Member.class, (Opt, c) -> Opt.getAsMember());

		addReturnValueMapping(OptionType.ROLE, Role.class, (Opt, c) -> Opt.getAsRole());
		addReturnValueMapping(OptionType.ATTACHMENT, Attachment.class, (Opt, c) -> Opt.getAsAttachment());
	}

	private static final HashMap<OptionType, HashMap<Function<Class<?>, Boolean>, BiFunction<OptionMapping, Class<?>, Object>>> optionReturnValueMapping = new HashMap<>();
	private static void addReturnValueMapping(OptionType type, Class<?> c, BiFunction<OptionMapping, Class<?>, Object> result){
		addReturnValueMapping(type, c::isAssignableFrom, result);
	}
	private static void addReturnValueMapping(OptionType type, Function<Class<?>, Boolean> c, BiFunction<OptionMapping, Class<?>, Object> result){
		optionReturnValueMapping.computeIfAbsent(type, (s) -> new HashMap<>());
		optionReturnValueMapping.get(type).put(c, result);
	}

	private static final HashMap<Function<Class<?>, Boolean>, OptionType> optionTypeMapping = new HashMap<>();
	private static void addTypeMapping(Function<Class<?>, Boolean> c, OptionType type){
		optionTypeMapping.put(c, type);
	}
	private static void addTypeMapping(Class<?> c, OptionType type){
		addTypeMapping(c::isAssignableFrom, type);
	}

	private static final HashMap<OptionType, HashMap<Function<Class<?>, Boolean>, TriConsumer<Class<?>, OptionData, SlashArgument>>> optionChoiceMappings = new HashMap<>();
	private static void addChoiceMapping(OptionType type, Class<?> c, TriConsumer<Class<?>, OptionData, SlashArgument> result){
		addChoiceMapping(type, c::isAssignableFrom, result);
	}
	private static void addChoiceMapping(OptionType type, Function<Class<?>, Boolean> c, TriConsumer<Class<?>, OptionData, SlashArgument> result){
		optionChoiceMappings.computeIfAbsent(type, (s) -> new HashMap<>());
		optionChoiceMappings.get(type).put(c, result);
	}


	public static List<OptionData> getOptions(Object c){
		ArrayList<OptionData> datalist = new ArrayList<>();

		HashMap<String, AutoComplete> autoCompleteHashMap = new HashMap<>();

		for(Field fe : c.getClass().getDeclaredFields()){
			if(fe.isAnnotationPresent(ArgumentAutoComplete.class) && fe.getType() == AutoComplete.class){
				ArgumentAutoComplete arg = fe.getAnnotation(ArgumentAutoComplete.class);

				try{
					AutoComplete argument = (AutoComplete)fe.get(c);
					autoCompleteHashMap.put(arg.value(), argument);
				}catch(IllegalAccessException e){
					Logging.exception(e);
				}
			}
		}

		for(Field fe : c.getClass().getDeclaredFields()){
			if(fe.isAnnotationPresent(SlashArgument.class)){
				SlashArgument argument = fe.getAnnotation(SlashArgument.class);

				OptionType type = getOptionType(fe);

				if(type != null) {
					OptionData data = getOptionData(fe, argument, type);
					for(OptionData data1 : datalist){
						if(data1.getName().equals(data.getName())){
							System.err.println("Duplicate option! " + c.getClass() + " - " + fe);
						}
					}

					if(c instanceof ISlashCommand cc){
						for(DiscordLocale lang : Startup.LANGS){
							try{
								ResourceBundle COMMAND_LANG = ResourceBundle.getBundle("arguments", LocaleUtils.toLocale(lang.getLocale().replace("-", "_")));
								data.setDescriptionLocalization(lang, COMMAND_LANG.getString(cc.commandName().toLowerCase().replace(" ", "_") + ".arguments.description." + data.getName().toLowerCase().replace(" ", "_")));
								data.setNameLocalization(lang, COMMAND_LANG.getString(cc.commandName().toLowerCase().replace(" ", "_") + ".arguments.name." + data.getName().toLowerCase().replace(" ", "_")));
							}catch(Exception ignored){

							}
						}
					}

					if(autoCompleteHashMap.containsKey(argument.name())){
						AutoComplete complete = autoCompleteHashMap.get(argument.name());
						data.setAutoComplete(true);
					}

					datalist.add(data);
				}
			}
		}
		return datalist;
	}

	
	private static OptionData getOptionData(Field fe, SlashArgument argument, OptionType type)
	{
		if(argument.name().length() > OptionData.MAX_NAME_LENGTH) System.err.println("Name is too long for " + fe);
		if(argument.description().length() > OptionData.MAX_DESCRIPTION_LENGTH) System.err.println("Text is too long for " + fe);

		OptionData data = new OptionData(type, argument.name(), argument.description(), argument.required());
		Class<?> checkType = Primitives.unwrap(fe.getType());

		if(optionChoiceMappings.containsKey(type)){
			optionChoiceMappings.get(type).entrySet().stream().filter((s) -> s.getKey().apply(checkType)).findFirst().ifPresentOrElse((s) -> {
				try{
					s.getValue().accept(checkType, data, argument);
				}catch(Exception e){
					System.err.println(fe);
					System.err.println(argument);
					Logging.exception(e);
				}
			}, () -> System.err.println("Didn't find field type for " + fe));
		}

		return data;
	}

	@Nullable
	private static OptionType getOptionType(Field fe)
	{
		AtomicReference<OptionType> type = new AtomicReference<>();
		Class<?> checkType = Primitives.unwrap(fe.getType());

		optionTypeMapping.entrySet().stream().filter((s) -> s.getKey().apply(checkType)).findFirst().ifPresentOrElse((s) -> type.set(s.getValue()), () -> System.err.println("Didn't find field type for " + fe));

		if(type.get() == null){
			System.err.println("Didn't find field type for " + fe);
		}

		return type.get();
	}

	static void prepareCommandInstance(CommandInteractionPayload event, ISlashCommand slashCommand){
		for(Field fe : slashCommand.getClass().getDeclaredFields()){
			if(fe.isAnnotationPresent(SlashArgument.class)){
				SlashArgument argument = fe.getAnnotation(SlashArgument.class);

				OptionMapping option = event.getOption(argument.name());
				Class<?> checkType = Primitives.unwrap(fe.getType());

				if(option != null){
					if(optionReturnValueMapping.containsKey(option.getType())){
						optionReturnValueMapping.get(option.getType()).entrySet().stream().filter((s) -> s.getKey().apply(checkType)).findFirst().ifPresentOrElse((s) -> {
							try{
								fe.set(slashCommand, s.getValue().apply(option, checkType));
							}catch(IllegalAccessException e){
								Logging.exception(e);
							}
						}, () -> System.err.println("Invalid field type! " + checkType + " | " + option.getType()));
					}
				}
			}
		}
	}
	
}