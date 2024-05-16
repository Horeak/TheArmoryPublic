package Core.Objects.Annotation.Commands;

import Core.Objects.Interfaces.Commands.ISlashCommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface CommandGroup
{
	Class<? extends ISlashCommand> value();
}