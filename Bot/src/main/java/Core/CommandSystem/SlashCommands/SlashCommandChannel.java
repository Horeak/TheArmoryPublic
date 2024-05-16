package Core.CommandSystem.SlashCommands;

import Core.Objects.BotChannel;
import lombok.Getter;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.sticker.StickerSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.*;
import net.dv8tion.jda.api.requests.restaction.pagination.ThreadChannelPaginationAction;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class SlashCommandChannel extends BotChannel implements TextChannel
{
	@Getter
	private final IDeferrableCallback event;
	
	public SlashCommandChannel(IDeferrableCallback  event)
	{
		super(event instanceof SlashCommandInteractionEvent ? ((SlashCommandInteractionEvent)event).getChannel() : event instanceof ButtonInteractionEvent ? (((ButtonInteractionEvent)event).getChannel()) : null);
		this.event = event;
	}

	public TextChannel getChannel(){
		if(event.getChannel() instanceof TextChannel){
			return (TextChannel)event.getChannel();
		}

		return null;
	}

	@Nullable
	@Override
	public String getTopic()
	{
		return getChannel() != null ? getChannel().getTopic() : null;
	}

	@Override
	public boolean isNSFW()
	{
		return getChannel() != null && getChannel().isNSFW();
	}

	@Override
	public int getSlowmode()
	{
		return getChannel() != null ? getChannel().getSlowmode() : 0;
	}

	
	@Override
	public List<Member> getMembers()
	{
		return getChannel() != null ? getChannel().getMembers() : null;
	}

	@Override
	public int getPosition()
	{
		return getChannel() != null ? getChannel().getPosition() : 0;
	}

	@Override
	public int getPositionRaw()
	{
		return getChannel() != null ? getChannel().getPositionRaw() : 0;
	}

	@Nullable
	@Override
	public PermissionOverride getPermissionOverride(
			 IPermissionHolder permissionHolder)
	{
		return getChannel() != null ? getChannel().getPermissionOverride(permissionHolder) : null;
	}

	
	@Override
	public List<PermissionOverride> getPermissionOverrides()
	{
		return getChannel() != null ? getChannel().getPermissionOverrides() : null;
	}

	
	@Override
	public List<PermissionOverride> getMemberPermissionOverrides()
	{
		return getChannel() != null ? getChannel().getMemberPermissionOverrides() : null;
	}

	
	@Override
	public List<PermissionOverride> getRolePermissionOverrides()
	{
		return getChannel() != null ? getChannel().getRolePermissionOverrides() : null;
	}

	
	@Override
	public PermissionOverrideAction upsertPermissionOverride( IPermissionHolder permissionHolder){
		return getChannel() != null ? getChannel().upsertPermissionOverride(permissionHolder) : null;
	}

	@Override
	public boolean isSynced()
	{
		return getChannel() != null && getChannel().isSynced();
	}

	
	@Override
	public ChannelAction<TextChannel> createCopy(
			 Guild guild)
	{
		return getChannel() != null ? getChannel().createCopy(guild) : null;
	}

	
	@Override
	public ChannelAction<TextChannel> createCopy()
	{
		return getChannel() != null ? getChannel().createCopy() : null;
	}

	@Override
	public TextChannelManager getManager()
	{
		return getChannel() != null ? getChannel().getManager() : null;
	}

	@Override
	public long getParentCategoryIdLong()
	{
		return 0;
	}

	
	@Override
	public AuditableRestAction<Void> delete()
	{
		return getChannel() != null ? getChannel().delete() : null;
	}

	@Override
	public IPermissionContainer getPermissionContainer()
	{
		return null;
	}

	
	@Override
	public InviteAction createInvite()
	{
		return getChannel() != null ? getChannel().createInvite() : null;
	}

	
	@Override
	public RestAction<List<Invite>> retrieveInvites()
	{
		return getChannel() != null ? getChannel().retrieveInvites() : null;
	}

	
	@Override
	public RestAction<List<Webhook>> retrieveWebhooks()
	{
		return getChannel() != null ? getChannel().retrieveWebhooks() : null;
	}

	
	@Override
	public WebhookAction createWebhook( String name)
	{
		return getChannel() != null ? getChannel().createWebhook(name) : null;
	}

	
	@Override
	public RestAction<Void> deleteMessages( Collection<Message> messages)
	{
		return getChannel() != null ? getChannel().deleteMessages(messages) : null;
	}

	
	@Override
	public RestAction<Void> deleteMessagesByIds( Collection<String> messageIds)
	{
		return getChannel() != null ? getChannel().deleteMessagesByIds(messageIds) : null;
	}

	
	@Override
	public AuditableRestAction<Void> deleteWebhookById( String id)
	{
		return getChannel() != null ? getChannel().deleteWebhookById(id) : null;
	}

	
	@Override
	public RestAction<Void> clearReactionsById( String messageId)
	{
		return getChannel() != null ? getChannel().clearReactionsById(messageId) : null;
	}

	
	@Override
	public RestAction<Void> clearReactionsById( String messageId,  Emoji emoji){
		return null;
	}

	
	@Override
	public MessageCreateAction sendStickers( Collection<? extends StickerSnowflake> stickers){
		return null;
	}


	@Override
	public boolean canTalk()
	{
		return getChannel() != null && getChannel().canTalk();
	}

	@Override
	public boolean canTalk( Member member)
	{
		return getChannel() != null && getChannel().canTalk(member);
	}

	
	@Override
	public RestAction<Void> removeReactionById( String messageId,  Emoji emoji,  User user){
		return null;
	}

	@Override
	public int compareTo(
	GuildChannel o)
	{
		return getChannel() != null ? getChannel().compareTo(o) : 0;
	}

	@Override
	public int getDefaultThreadSlowmode(){
		return 0;
	}

	
	@Override
	public ThreadChannelAction createThreadChannel(String s, boolean b)
	{
		return null;
	}

	
	@Override
	public ThreadChannelAction createThreadChannel(String s, long l)
	{
		return null;
	}

	
	@Override
	public ThreadChannelPaginationAction retrieveArchivedPublicThreadChannels()
	{
		return null;
	}

	
	@Override
	public ThreadChannelPaginationAction retrieveArchivedPrivateThreadChannels()
	{
		return null;
	}

	
	@Override
	public ThreadChannelPaginationAction retrieveArchivedPrivateJoinedThreadChannels()
	{
		return null;
	}
}