package Core.Util.Time;

import Core.Commands.TimezoneCommand;
import Core.Objects.Annotation.Method.Startup.PreInit;
import Core.Util.Utils;
import net.dv8tion.jda.api.entities.User;
import org.joda.time.*;
import org.jsoup.internal.StringUtil;
import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;
import org.ocpsoft.prettytime.nlp.parse.DateGroup;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeParserUtil
{
	public static final HashMap<String[], TimeRunnable> timeRuns = new HashMap<>();
	public static final PrettyTime timeFormat = new PrettyTime();
	
	@PreInit
	public static void preInit(){
		timeRuns.put(new String[]{"second", "sec", "s"}, (num, delay) -> delay += Seconds.seconds(num).toStandardDuration().getMillis());
		timeRuns.put(new String[]{"minute", "minutes", "min", "mins", "m"}, (num, delay) -> delay += Minutes.minutes(num).toStandardDuration().getMillis());
		timeRuns.put(new String[]{"hour", "hours", "h"}, (num, delay) -> delay += Hours.hours(num).toStandardDuration().getMillis());
		timeRuns.put(new String[]{"day", "days", "d"}, (num, delay) -> delay += Days.days(num).toStandardDuration().getMillis());
		timeRuns.put(new String[]{"week", "weeks", "w"}, (num, delay) -> delay += Weeks.weeks(num).toStandardDuration().getMillis());
		
		timeRuns.put(new String[]{"month", "months"}, (num, delay) -> {
			DateTime start = new DateTime();
			DateTime end = start.plus(Months.months(num));
			long millis = end.getMillis() - start.getMillis();
			return delay + millis;
		});
		
		timeRuns.put(new String[]{"year", "years"}, (num, delay) -> {
			DateTime start = new DateTime();
			DateTime end = start.plus(Years.years(num));
			long millis = end.getMillis() - start.getMillis();
			return delay + millis;
		});
	}
	
	public static long getTime(User user, String input)
	{
		TimeZone zone = TimezoneCommand.TIME_ZONES.containsKey(user.getIdLong()) ? TimeZone.getTimeZone(TimezoneCommand.TIME_ZONES.get(user.getIdLong())) : TimeZone.getDefault();
		return getTime(zone, input);
	}
	
	public static long getTime(String input)
	{
		return getTime(TimeZone.getDefault(), input);
	}
	
	public static long getTime(TimeZone timeZone, String input)
	{
		try {
			List < DateGroup > parse = new PrettyTimeParser(timeZone).parseSyntax(input);
			
			if (!input.isEmpty() && !parse.isEmpty()) {
				Date date = parse.get(0).getDates().get(0);
				
				if(date != null){
					return date.getTime() - System.currentTimeMillis();
				}
			}
		}catch (Exception ignored){}
		
		long delay = 0;
		
		for(Entry<String[], TimeRunnable> ent : timeRuns.entrySet()){
			String nameMatcher = StringUtil.join(ent.getKey(), "|");
			String matcher = "(\\d+)[ ]?(" + nameMatcher + ")";
			
			Pattern p = Pattern.compile(matcher, Pattern.CASE_INSENSITIVE);
			Matcher m1 = p.matcher(input);
			
			if(m1.find()){
				String num = m1.group(1);
				
				if(Utils.isInteger(num)){
					delay = ent.getValue().run(Integer.parseInt(num), delay);
				}
				
				input = input.replace(m1.group(), "");
			}
		}
		
		return delay;
	}
	
	
	public static String getTime(Date date){
		return timeFormat.format(date);
	}
	
	public static String getTime(Long date){
		return getTime(new Date(date));
	}
	
	public static String getTimeText(long millis)
	{
		return getTimeText(millis, true, true, true, true, true, true, true);
	}
	
	public static String getTimeText(long millis, boolean yearsB, boolean monthsB, boolean weeksB, boolean daysB, boolean hoursB, boolean minsB, boolean secondsB)
	{
		return new TimeObject.TimeObjectBuilder()
				.time(System.currentTimeMillis() + millis)
				.showYears(yearsB)
				.showMonths(monthsB)
				.showWeeks(weeksB)
				.showDays(daysB)
				.showHours(hoursB)
				.showMinutes(minsB)
				.showSeconds(secondsB)
				.build().getTimeText();
	}
	

}