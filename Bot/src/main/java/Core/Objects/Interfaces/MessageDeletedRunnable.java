package Core.Objects.Interfaces;

@FunctionalInterface
public interface MessageDeletedRunnable
{
	void run(boolean wasSuccessful, Throwable T);
}
