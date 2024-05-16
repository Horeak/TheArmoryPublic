package Core.CommandSystem;

import Core.Util.Utils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.util.EnumSet;

public class PermissionsUtils
{
	public static boolean hasPermissions(User user, Guild guild, GuildChannel channel, EnumSet<Permission> perms)
	{
		return hasPermissions(user, guild, channel, perms, true);
	}

	public static boolean hasPermissions(
			 User user,  Guild guild,  GuildChannel channel,
			 EnumSet<Permission> perms, boolean useChannelPerms)
	{
		if (useChannelPerms && guild != null) {
			if (channel == null) {
				return true; //Ignore permissions for private chats
			}
		}

		if (user == null) {
			return false; //Invalid parameters check
		}


		if (perms != null && perms.size() > 0) {
			Member mem = Utils.getMember(guild, user);

			if (useChannelPerms) {
				return PermissionUtil.checkPermission((IPermissionContainer)channel, mem, perms.toArray(new Permission[0]));
			} else {
				return PermissionUtil.checkPermission(mem, perms.toArray(new Permission[0]));
			}
		} else {
			return true;
		}
	}
	

	public static boolean botHasPermission(MessageChannel channel, EnumSet<Permission> perms){
		if(channel != null) {
			if(ChatUtils.isPrivate(channel)){
				return true;
			}

			if(channel instanceof TextChannel tChannel) {
				tChannel.getGuild();
				Member member = tChannel.getGuild().getSelfMember();
				
				PermissionOverride botOverridePermissions = tChannel.getPermissionOverride(member);
				EnumSet<Permission> botPermissions = member.getPermissions();
				
				boolean overrides = botOverridePermissions != null && botOverridePermissions.getAllowed().containsAll(perms);
				boolean normal = botPermissions.containsAll(perms);
				
				return overrides || normal;
			}
		}

		return false;
	}

	public static boolean botHasPermission(Guild guild, EnumSet<Permission> perms){
		if(guild != null) {
			Member member = guild.getSelfMember();
			EnumSet<Permission> botPermissions = member.getPermissions();
			return botPermissions.containsAll(perms);
		}

		return false;
	}
}