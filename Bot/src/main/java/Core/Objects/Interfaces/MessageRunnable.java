package Core.Objects.Interfaces;

import net.dv8tion.jda.api.entities.Message;

@FunctionalInterface
public interface MessageRunnable
{
	void run(Message message, Throwable error);
}
