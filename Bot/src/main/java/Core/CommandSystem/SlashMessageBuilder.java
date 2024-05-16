package Core.CommandSystem;

import Core.CommandSystem.ComponentSystem.ComponentResponseSystem;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.Objects.BotChannel;
import Core.Objects.Interfaces.MessageRunnable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.EnumSet;

public class SlashMessageBuilder extends ChatMessageBuilder
{
	private boolean ephemeral = true;

	public SlashMessageBuilder(User author, BotChannel channel)
	{
		super(author, channel);
	}

	public SlashMessageBuilder disableEphemeral()
	{
		this.ephemeral = false;
		return this;
	}

	public boolean isEphemeral()
	{
		return ephemeral;
	}

	@Override
	public ChatMessageBuilder withRunnables(MessageRunnable... runnables)
	{
		return this;
	}

	@Override
	public ChatMessageBuilder addRunnable(MessageRunnable runnable)
	{
		return this;
	}

	@Override
	public void send()
	{
		if (!ephemeral || getAttachment() != null) {
			if (!PermissionsUtils.botHasPermission(getChannel(), EnumSet.of(Permission.MESSAGE_SEND))
			    || (getEmbed() != null && !PermissionsUtils.botHasPermission(getChannel(), EnumSet.of(Permission.MESSAGE_EMBED_LINKS)))
			    || (getAttachment() != null && !PermissionsUtils.botHasPermission(getChannel(), EnumSet.of(Permission.MESSAGE_ATTACH_FILES)))) {

				ChatMessageBuilder builder = ChatUtils.createSlashMessage(getAuthor(), getChannel());
				builder.withEmbed(ChatUtils.makeEmbed(getAuthor(), getGuild(), getChannel(), "I am unable to post it in the current channel, would you like it sent as a DM?"));

				builder.addAction(ComponentResponseSystem.addComponent(getAuthor(), Button.success("id", "Yes"), (event1 -> getAuthor().openPrivateChannel().queue((ch) -> {
					this.channel = new BotChannel(ch);
					handle();
				}))));

				builder.addAction(ComponentResponseSystem.addComponent(getAuthor(), Button.danger("id", "No"), (event1 -> ChatUtils.sendEmbed(getChannel(), "Well i am afraid i am unable to help then. Please try again later if you change your mind."))));

				builder.send();
				return;
			}
		}

		handle();
	}

	@Override
	protected void handle()
	{
		prepare();

		if(!(getChannel() instanceof PrivateChannel) && (getContent() == null || getContent().isEmpty()) && !ephemeral){
			withContent(getAuthor().getAsMention());
		}

		if (getChannel() instanceof SlashCommandChannel ch) {
			EmbedBuilder embedBuilder = new EmbedBuilder(getEmbed());

			ChatUtils.setEmbedColor(getGuild(), getAuthor(), embedBuilder);

			MessageCreateData mes = new MessageCreateBuilder().setContent(getContent()).setEmbeds(embedBuilder.build()).build();

			if (ch.getEvent().isAcknowledged()) {
				WebhookMessageCreateAction<?> action = ch.getEvent().getHook().setEphemeral(ephemeral).sendMessage(mes);

				if(getActionRows() != null && getActionRows().size() > 0){
					action.addComponents(getActionRows());
				}

				if (getAttachment() != null && getAttachment().getKey() != null && getAttachment().getValue() != null) {
					action.addFiles(FileUpload.fromData(getAttachment().getKey(), getAttachment().getValue()));
				}

				action.queue();
			} else {
				if(ch.getEvent() instanceof IReplyCallback) {
					ReplyCallbackAction action = ((IReplyCallback)ch.getEvent()).reply(mes);
					action.setEphemeral(ephemeral);

					if (getActionRows() != null && getActionRows().size() > 0) {
						action.addComponents(getActionRows());
					}

					if (getAttachment() != null && getAttachment().getKey() != null && getAttachment().getValue() != null) {
						action.addFiles(FileUpload.fromData(getAttachment().getKey(), getAttachment().getValue()));
					}

					action.queue();
				}
			}
		} else {
			super.handle();
		}
	}
}