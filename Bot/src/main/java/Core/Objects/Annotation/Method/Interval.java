package Core.Objects.Annotation.Method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Retention( RetentionPolicy.RUNTIME )
@Target( {ElementType.METHOD} )
public @interface Interval {
	TimeUnit time_unit() default TimeUnit.MINUTES;
	
	int time_interval();
	int initial_delay() default 0;
}
