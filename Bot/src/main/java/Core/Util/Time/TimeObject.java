package Core.Util.Time;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.Date;
import java.util.StringJoiner;

@Builder
public class TimeObject
{
	private Long time;
	
	@Setter
	@Getter
	private boolean showYears, showMonths, showWeeks, showDays, showHours, showMinutes, showSeconds;
	
	@Override
	public String toString()
	{
		return getTimeText();
	}
	
	public String getTimeText()
	{
		Date date = new Date(time + 1000);
		
		Instant instant1 = Instant.ofEpochMilli(Math.min(date.getTime(), System.currentTimeMillis()));
		Instant instant2 = Instant.ofEpochMilli(Math.max(System.currentTimeMillis(), date.getTime()));
		
		Interval interval = new Interval(instant1, instant2);
		Period period = interval.toPeriod();
		
		StringJoiner joiner = new StringJoiner(", ");
		
		int years = period.getYears();
		if (years > 0 && showYears) {
			joiner.add(years + " year" + (years > 1 ? "s" : ""));
			period = period.minusYears(years);
		}
		
		int months = period.getMonths();
		if (months > 0 && showMonths) {
			joiner.add(months + " month" + (months > 1 ? "s" : ""));
			period = period.minusMonths(months);
		}
		
		int weeks = period.getWeeks();
		if (weeks > 0 && showWeeks) {
			joiner.add(weeks + " week" + (weeks > 1 ? "s" : ""));
			period = period.minusWeeks(weeks);
		}
		
		int days = period.getDays();
		if (days > 0 && showDays) {
			joiner.add(days + " day" + (days > 1 ? "s" : ""));
			period = period.minusDays(days);
		}
		
		int hours = period.getHours();
		if (hours > 0 && showHours) {
			joiner.add(hours + " hour" + (hours > 1 ? "s" : ""));
			period = period.minusHours(hours);
		}
		
		int minutes = period.getMinutes();
		if (minutes > 0 && showMinutes) {
			joiner.add(minutes + " minute" + (minutes > 1 ? "s" : ""));
			period = period.minusMinutes(minutes);
		}
		
		int seconds = period.getSeconds();
		if (seconds > 0 && showSeconds) {
			joiner.add(seconds + " second" + (seconds > 1 ? "s" : ""));
			period = period.minusSeconds(seconds);
			
		}
		int millis = period.getMillis();
		if (joiner.toString().isEmpty() && millis > 0) {
			joiner.add(seconds + " millisecond" + (millis > 1 ? "s" : ""));
		}
		
		return joiner.toString();
	}
}