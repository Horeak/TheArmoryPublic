package Core.Util;

import Core.CommandSystem.PermissionsUtils;
import Core.Main.Logging;
import Core.Main.Startup;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Utils
{
	
	public static String limitString( String value, int length)
	{
		if (value != null && value.length() >= length) {
			String val = value.substring(0, length - 4).strip();
			return val + (!value.endsWith("..") ? "..." : "");
		}

		return value;
	}


	public static boolean isInteger(String str)
	{
		if (str == null) {
			return false;
		}
		int length = str.length();
		if (length == 0) {
			return false;
		}
		int i = 0;
		if (str.charAt(0) == '-') {
			if (length == 1) {
				return false;
			}
			i = 1;
		}
		for (; i < length; i++) {
			char c = str.charAt(i);
			if (c < '0' || c > '9') {
				return false;
			}
		}
		return true;
	}
	
	public static boolean isLong(String s)
	{
		Scanner sc = new Scanner(s.trim());
		if (!sc.hasNextLong(10)) return false;
		sc.nextLong(10);
		return !sc.hasNext();
	}
	
	public static boolean isDouble(String s)
	{
		Scanner sc = new Scanner(s.trim());
		if (!sc.hasNextDouble()) return false;
		sc.nextDouble();
		return !sc.hasNext();
	}

	public static boolean isFloat(String s)
	{
		Scanner sc = new Scanner(s.trim());
		if (!sc.hasNextFloat()) return false;
		sc.nextFloat();
		return !sc.hasNext();
	}

	public static double compareStrings(String stringA, String stringB)
	{
		return LevenshteinDistance.getDefaultInstance().apply(stringA, stringB);
	}

	public static String getString(HashMap<String, Object> objectHashMap)
	{
		StringJoiner builder = new StringJoiner(", ", "{", "}");

		for (Map.Entry<String, Object> ob : objectHashMap.entrySet()) {
			if (ob != null && ob.getValue() != null && ob.getKey() != null) {
				builder.add(ob.getKey() + "='" + (ob.getValue().getClass().isArray() ? Arrays.deepToString(
						(Object[])ob.getValue()) : ob.getValue().toString()) + "'");
			}
		}

		return builder.toString().replace("\n", "$_n");
	}


	public static String getUpTime()
	{
		StringJoiner joiner = new StringJoiner(" ");
		Long tt = System.currentTimeMillis();
		long time = tt - Startup.startTime;

		if (TimeUnit.DAYS.convert(time, TimeUnit.MILLISECONDS) > 0) {
			long tg = TimeUnit.DAYS.convert(time, TimeUnit.MILLISECONDS);
			time -= TimeUnit.MILLISECONDS.convert(tg, TimeUnit.DAYS);
			joiner.add(tg + "d");
		}

		if (TimeUnit.HOURS.convert(time, TimeUnit.MILLISECONDS) > 0) {
			long tg = TimeUnit.HOURS.convert(time, TimeUnit.MILLISECONDS);
			time -= TimeUnit.MILLISECONDS.convert(tg, TimeUnit.HOURS);
			joiner.add(tg + "h");
		}

		if (TimeUnit.MINUTES.convert(time, TimeUnit.MILLISECONDS) > 0) {
			long tg = TimeUnit.MINUTES.convert(time, TimeUnit.MILLISECONDS);
			time -= TimeUnit.MILLISECONDS.convert(tg, TimeUnit.MINUTES);
			joiner.add(tg + "m");
		}

		if (TimeUnit.SECONDS.convert(time, TimeUnit.MILLISECONDS) > 0) {
			long tg = TimeUnit.SECONDS.convert(time, TimeUnit.MILLISECONDS);
			joiner.add(tg + "s");
		}

		return joiner.toString();
	}


	public static Member getMember(Guild guild, User user){
		Member member = null;

		if (guild != null) {
			member = guild.getMember(user);

			if(member == null){
				member = guild.retrieveMember(user).complete();
			}
		}

		return member;
	}


	public static User getUser(String id){
		return getUser(Long.parseLong(id));
	}

	public static User getUser(Long id){
		try {
			User user = Startup.getClient().getUserById(id);

			if (user == null) {
				user = Startup.getClient().retrieveUserById(id).complete();
			}

			return user;

		}catch (InsufficientPermissionException e){
			return null;
		}
	}
	
	public static Message getMessage(TextChannel channel, Long id){
		Message mes = null;
		if(!PermissionsUtils.botHasPermission(channel, EnumSet.of(Permission.MESSAGE_HISTORY))){
			return null;
		}
		try{
			mes = channel.retrieveMessageById(id).complete();
		}catch (Exception e){
			if(e instanceof ErrorResponseException ex){
				if(ex.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE){
					return null;
				}
			}else if(e instanceof InsufficientPermissionException){
				return null;
			}

			Logging.exception(e);
		}

		return mes;
	}
}