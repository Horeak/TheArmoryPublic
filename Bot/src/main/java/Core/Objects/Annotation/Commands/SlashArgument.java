package Core.Objects.Annotation.Commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.FIELD )
public @interface SlashArgument {
	String name();
	String description();
	boolean required() default false;
	String[] choices() default "";
	double minValue() default -1;
	double maxValue() default -1;
}