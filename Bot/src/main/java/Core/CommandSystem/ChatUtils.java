package Core.CommandSystem;

import Core.Objects.BotChannel;
import Core.Objects.Interfaces.MessageDeletedRunnable;
import Core.Objects.Interfaces.MessageRunnable;
import Core.Util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;

import java.awt.Color;
import java.util.EnumSet;

@SuppressWarnings( {"unused", "SameParameterValue"} )
public class ChatUtils{
	/* Edit messages */
	public static void editMessage(Message messageId, String title, MessageEmbed object, MessageRunnable... runnable){
		MessageEditAction action = null;

		if(messageId == null)
			return;

		if(title != null && !title.isBlank()){
			action = messageId.editMessage(title);
		}

		if(action == null && object != null){
			action = messageId.editMessageEmbeds(object);
		}

		if(action == null){
			System.err.println("Unable to edit message. No title or embed!");
			return;
		}
		
		messageId.getChannel();
		action.queue((mes) -> {
			if (runnable != null) {
				for (MessageRunnable runnable1 : runnable) {
					runnable1.run(mes, null);
				}
			}
		}, (T) -> {
			if (runnable != null) {
				for (MessageRunnable runnable1 : runnable) {
					runnable1.run(null, T);
				}
			}
		});
	}

	public static void editMessage(Message messageId, String message, MessageRunnable... runnable){
		editMessage(messageId, message, null, runnable);
	}

	public static void editMessage(Message messageId, MessageEmbed object, MessageRunnable... runnable){
		editMessage(messageId, null, object, runnable);
	}

	/* Send Message */
	public static void sendMessage(MessageChannel channel, String title, MessageEmbed object, MessageRunnable... runnable){
		ChatMessageBuilder builder = getCorrectBuilder(null, getChannel(channel));
		builder.withContent(title);
		builder.withEmbed(object);
		builder.withRunnables(runnable);
		builder.send();
	}

	public static void sendMessage(MessageChannel channel, String title, MessageRunnable... runnable){
		ChatMessageBuilder builder = getCorrectBuilder(null, getChannel(channel));
		builder.withContent(title);
		builder.withRunnables(runnable);
		builder.send();
	}

	public static void sendMessage(MessageChannel channel, MessageEmbed object, MessageRunnable... runnable){
		ChatMessageBuilder builder = getCorrectBuilder(null, getChannel(channel));
		builder.withEmbed(object);
		builder.withRunnables(runnable);
		builder.send();
	}

	public static void sendEmbed(MessageChannel chat, String message, MessageRunnable... runnable){
		ChatMessageBuilder builder = getCorrectBuilder(null, getChannel(chat));
		builder.withEmbed(message);
		builder.withRunnables(runnable);
		builder.send();
	}

	private static BotChannel getChannel(MessageChannel channel){
		if(channel instanceof BotChannel)
			return (BotChannel)channel;

		return new BotChannel(channel);
	}

	public static void deleteMessage(Message mes, MessageDeletedRunnable... runnable){
		if(mes == null) {
			return;
		} else {mes.getGuild();}
		
		if(PermissionsUtils.botHasPermission(mes.getGuild(), EnumSet.of(Permission.MESSAGE_MANAGE))){
			//TODO This gives a Unknown message error
			mes.delete().queue((T) -> {
				if(runnable != null){
					for(MessageDeletedRunnable runnable1 : runnable){
						runnable1.run(true, null);
					}
				}
			}, (T) -> {
				if(runnable != null){
					for(MessageDeletedRunnable runnable1 : runnable){
						runnable1.run(false, T);
					}
				}
			});
		}else{
			System.err.println("The bot was unable to delete a message because missing permissions!");
		}
	}

	public static VoiceChannel getConnectedBotChannel(Guild guild){
		GuildVoiceState state = guild.getSelfMember().getVoiceState();

		if(state != null){
			if(state.getChannel() != null){
				return (VoiceChannel)state.getChannel();
			}
		}

		return null;
	}

	public static VoiceChannel getVoiceChannelFromUser(User user, Guild guild){
		Member mem = Utils.getMember(guild, user);

		if(mem != null){
			GuildVoiceState state = mem.getVoiceState();

			if(state != null && state.getChannel() != null){
				return (VoiceChannel)state.getChannel();
			}
		}
		return null;
	}

	public static boolean isPrivate(MessageChannel channel){
		if(channel == null) {
			return false;
		} else {channel.getType();}
		
		return (channel.getType() == ChannelType.PRIVATE || channel.getType() == ChannelType.GROUP);
	}

	public static final Color DEFAULT_EMBED_COLOR = new Color(13, 129, 104);

	public static Color getEmbedColor(Guild guild, User author){
		Member member = Utils.getMember(guild, author);
		return getEmbedColor(member);
	}

	public static Color getEmbedColor(Member member){
		Color c = DEFAULT_EMBED_COLOR;

		if(member != null && member.getColor() != null){
			c = member.getColor();
		}

		return c;
	}

	public static void setEmbedColor(Guild guild, User author, EmbedBuilder builder){
		if(guild == null || author == null || builder == null)
			return;
		Member member = Utils.getMember(guild, author);
		setEmbedColor(member, builder);
	}

	public static void setEmbedColor(Member member, EmbedBuilder builder){
		builder.setColor(getEmbedColor(member));
	}

	public static EmbedBuilder makeEmbed(User user, Guild guild, BotChannel channel, String text){
		EmbedBuilder builder = new EmbedBuilder().setDescription(text);
		setEmbedColor(guild, user, builder);
		return builder;
	}

	public static ChatMessageBuilder getCorrectBuilder(User user, MessageChannel channel){
		return createSlashMessage(user, getChannel(channel));
	}

	public static ChatMessageBuilder createMessage(User user, MessageChannel channel){
		return new ChatMessageBuilder(user, getChannel(channel));
	}

	public static SlashMessageBuilder createSlashMessage(User user, MessageChannel channel){
		return new SlashMessageBuilder(user, getChannel(channel));
	}
}