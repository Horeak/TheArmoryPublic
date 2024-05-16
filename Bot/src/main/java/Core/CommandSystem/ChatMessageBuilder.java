package Core.CommandSystem;

import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.Main.Startup;
import Core.Objects.BotChannel;
import Core.Objects.CustomEntry;
import Core.Objects.Interfaces.MessageRunnable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;

public class ChatMessageBuilder
{
	protected final User author;
	protected final Guild guild;
	protected BotChannel channel;
	private MessageEmbed embed;
	private CustomEntry<File, String> attachment;
	private String content;

	private boolean singleRow = true;
	private ArrayList<ActionRow> actionRows = new ArrayList<>();
	private final ArrayList<MessageRunnable> runnables = new ArrayList<>();

	public ChatMessageBuilder(User author, BotChannel channel)
	{
		this.author = author;
		this.guild = channel.getGuild();
		this.channel = channel;
	}

	public ChatMessageBuilder withEmbed(EmbedBuilder embed)
	{
		this.embed = embed.build();
		return this;
	}

	public ChatMessageBuilder withEmbed(MessageEmbed embed)
	{
		this.embed = embed;
		return this;
	}

	public ChatMessageBuilder withEmbed(String content){
		this.embed = ChatUtils.makeEmbed(author, guild, channel, content).build();
		return this;
	}

	public ChatMessageBuilder withContent(String content)
	{
		this.content = content;
		return this;
	}

	public ChatMessageBuilder withRunnables(MessageRunnable... runnables){
		this.runnables.addAll(Arrays.asList(runnables));
		return this;
	}

	public ChatMessageBuilder addRunnable(MessageRunnable runnable){
		runnables.add(runnable);
		return this;
	}

	public ChatMessageBuilder setMultiRow()
	{
		this.singleRow = false;
		return this;
	}

	public ChatMessageBuilder withActions(ArrayList<ItemComponent> actions)
	{
		if (singleRow) {
			actionRows.add(ActionRow.of(actions));
		} else {
			for (ItemComponent act : actions) {
				actionRows.add(ActionRow.of(act));
			}
		}

		return this;
	}

	public ChatMessageBuilder addAction(ItemComponent row)
	{
		if (!singleRow || actionRows.size() == 0) {
			actionRows.add(ActionRow.of(row));
		} else {
			actionRows.get(0).getComponents().add(row);
		}
		return this;
	}

	public ChatMessageBuilder withActionRows(ArrayList<ActionRow> rows)
	{
		actionRows = rows;
		return this;
	}

	public ChatMessageBuilder addActionRow(ActionRow row)
	{
		actionRows.add(row);
		return this;
	}

	public ChatMessageBuilder withAttachment(File file, String fileName)
	{
		attachment = new CustomEntry<>(file, fileName);
		return this;
	}

	public ArrayList<ActionRow> getActionRows()
	{
		return actionRows;
	}

	public User getAuthor()
	{
		return author;
	}

	public Guild getGuild()
	{
		return guild;
	}

	public BotChannel getChannel()
	{
		return channel;
	}

	public MessageEmbed getEmbed()
	{
		return embed;
	}

	public CustomEntry<File, String> getAttachment()
	{
		return attachment;
	}

	public String getContent()
	{
		return content;
	}

	public boolean isSingleRow()
	{
		return singleRow;
	}

	public void send()
	{
		if (!PermissionsUtils.botHasPermission(channel, EnumSet.of(Permission.MESSAGE_SEND))
		    || (embed != null && !PermissionsUtils.botHasPermission(channel, EnumSet.of(Permission.MESSAGE_EMBED_LINKS)))
		       || (attachment != null && !PermissionsUtils.botHasPermission(channel, EnumSet.of(Permission.MESSAGE_ATTACH_FILES)))) {

			if(author != null && author != Startup.getClient().getSelfUser()) {
				author.openPrivateChannel().queue((ch) -> {
					if(ch != null) handle();
				});
			}

			return;
		}

		handle();
	}


	protected void prepare(){
		if(getEmbed() != null) {
			MessageEmbed object = getEmbed();

			if (object.getColor() == null || object.getFooter() == null || object.getFooter().getText() == null || object.getFooter().getText().isBlank()) {
				User author = getAuthor();
				Guild guild = getGuild();

				if(author == null || guild == null){
					if(channel instanceof SlashCommandChannel ch) {
						
						if (author == null) {
							author = ch.getEvent().getUser();
						}

						if (guild == null) {
							guild = ch.getEvent().getGuild();
						}

					}
				}

				EmbedBuilder builder = new EmbedBuilder(object);

				if (object.getColor() == null) builder.setColor(ChatUtils.getEmbedColor(guild, author));

				withEmbed(builder);
			}
		}
	}

	protected void handle()
	{
		prepare();

		MessageCreateAction action = content != null ? channel.sendMessage(content) : embed != null ? channel.sendMessageEmbeds(embed) : null;

		if (action != null) {
			if (content != null && embed != null) {
				action.setEmbeds(embed);
			}

			if(actionRows != null && actionRows.size() > 0){
				action.addComponents(actionRows);
			}

			if (attachment != null && attachment.getKey() != null && attachment.getValue() != null) {
				action.addFiles(FileUpload.fromData(attachment.getKey(), attachment.getValue()));
			}

			action.queue((mes) -> {
				for (MessageRunnable runnable1 : runnables) {
					runnable1.run(mes, null);
				}
			}, (T) -> {
				for (MessageRunnable runnable1 : runnables) {
					runnable1.run(null, T);
				}
			});
		}
	}
}