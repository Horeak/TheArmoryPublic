package Core.Util;

import Core.Main.Logging;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static Core.Main.Startup.getReflection;

public class ReflectionUtils
{
	public static void invokeMethods(Class<? extends Annotation> c)
	{
		List<Method> methods = getMethods(c);
		System.out.println("Found " + methods.size() + " " + c.getSimpleName() + " method" + (methods.size() > 1 ? "s" : "") + "!");
		
		for (Method ob : methods) {
			invokeMethod(ob);
		}
	}
	
	public static void invokeMethod(Method method){
		try {
			method.invoke(null);
		} catch (Exception e1) {
			if (e1 instanceof InvocationTargetException e2) {
				
				if (e2.getCause() != null) {
					Logging.exception(e2.getCause());
				}
			} else {
				Logging.exception(e1);
			}
		}
	}
	
	public static List<Method> getMethods(Class<? extends Annotation> c)
	{
		//Do not cast null to Class, counts as empty list which causes issues
		return getMethods(c, (Class<?>[]) null);
	}
	
	
	public static List<Method> getMethods(Class<? extends Annotation> c,  Class<?>... parameters)
	{
		Set<Method> set1 = getReflection().getMethodsAnnotatedWith(c);
		CopyOnWriteArrayList<Method> list = new CopyOnWriteArrayList<>(set1);
		
		for (Method method : list) {
			if (!Modifier.isStatic(method.getModifiers())) {
				System.err.println("Method: " + method + " is not static!");
				list.remove(method);
				continue;
			}
			
			if (!method.canAccess(null)) {
				method.setAccessible(true);
			}
			
			Class<?>[] cc = method.getParameterTypes();
			
			if (parameters != null) {
				if (cc.length != parameters.length) {
					list.remove(method);
					continue;
				}
				
				for (int i = 0; i < cc.length; i++) {
					boolean isSame = cc[i] == parameters[i];
					
					if (parameters[i].isAssignableFrom(cc[i]) || cc[i].isAssignableFrom(parameters[i])) {
						isSame = true;
					}
					
					if (!isSame) {
						list.remove(method);
					}
				}
			}
		}
		
		return list;
	}
	
	public static List<Field> getFields(Class<? extends Annotation> c)
	{
		Set<Field> set1 = getReflection().getFieldsAnnotatedWith(c);
		CopyOnWriteArrayList<Field> list = new CopyOnWriteArrayList<>(set1);
		
		for (Field field : list) {
			if (!Modifier.isStatic(field.getModifiers())) {
				System.err.println("Field: " + field + " is not static!");
				list.remove(field);
				continue;
			}
			
			if (!field.canAccess(null)) {
				field.setAccessible(true);
			}
		}
		
		return list;
	}
}