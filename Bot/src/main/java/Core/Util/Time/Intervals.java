package Core.Util.Time;

import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Method.Interval;
import Core.Objects.Annotation.Method.Startup.PostInit;
import Core.Util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;

public class Intervals
{
	@PostInit
	public static void init(){
		List<Method> methods = ReflectionUtils.getMethods(Interval.class);
		
		for (Method ob : methods) {
			try {
				if (ob.isAnnotationPresent(Interval.class)) {
					Interval intervalOb = ob.getAnnotation(Interval.class);
					
					if (intervalOb != null) {
						Startup.scheduledExecutor.scheduleAtFixedRate(() -> ReflectionUtils.invokeMethod(ob), intervalOb.initial_delay(), intervalOb.time_interval(), intervalOb.time_unit());
					}
				}
			}catch (Exception e){
				Logging.exception(e);
			}
		}
	}
}