package Core.Objects.Events;

import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Method.EventListener;
import Core.Objects.Annotation.Method.Startup.Init;
import Core.Util.ReflectionUtils;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class EventHandler
{
	static final HashMap<Class<?>, ArrayList<Consumer<GenericEvent>>> eventListeners = new HashMap<>();
	
	@Init
	public static void initListeners()
	{
		System.out.println("Start listener register");
		int i = 0;

		if (!Startup.initListeners) {
			List<Method> listeners = ReflectionUtils.getMethods(EventListener.class);

			for (Method method : listeners) {
				Class<?>[] cc = method.getParameterTypes();

				if (cc.length == 1) {
					if (!eventListeners.containsKey(cc[0])) {
						eventListeners.put(cc[0], new ArrayList<>());
					}

					eventListeners.get(cc[0]).add((e) -> {
						try {
							method.invoke(method.getDeclaringClass(), e);
						} catch (IllegalAccessException | InvocationTargetException e1) {
							if (e1 instanceof InvocationTargetException e2) {
								
								if (e2.getCause() != null) {
									Logging.exception(e2.getCause());
								}
							} else {
								Logging.exception(e1);
							}
						}catch (InsufficientPermissionException e1){
							//TODO Surround with try/catch for InsufficientPermissionException and post a message in the channel
							//Add a message here for certain events, like message history, message edit, message embed etc etc
						}
					});
					i++;
				}
			}

			Startup.initListeners = true;
		}

		Startup.getClient().getEventManager().register(new ListenerAdapter()
		{
			@Override
			public void onGenericEvent(@Nonnull GenericEvent e)
			{
				Startup.executor.submit(() -> eventListeners.getOrDefault(e.getClass(), new ArrayList<>()).forEach(s -> s.accept(e)));
			}
		});

		System.out.println("End listener register, found " + i + " listeners");
	}
}