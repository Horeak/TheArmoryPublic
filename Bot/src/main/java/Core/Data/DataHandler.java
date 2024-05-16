package Core.Data;

import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Fields.Save;
import Core.Util.FileUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

public class DataHandler
{
	protected static final CopyOnWriteArrayList<Field> objects = new CopyOnWriteArrayList<>();
	protected static final ConcurrentHashMap<Field, String> values = new ConcurrentHashMap<>();
	protected static final ConcurrentHashMap<Field, Class<?>> fieldClasses = new ConcurrentHashMap<>();
	protected static final ArrayList<Field> toSave = new ArrayList<>();
	protected static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	protected static final CopyOnWriteArrayList<Field> queueDataLoad = new CopyOnWriteArrayList<>();
	public static boolean load_done = false;
	protected static String prefix = "";

	public static void init(String path)
	{
		prefix = path;
		new SaveDataThread().start();
	}

	protected static void initFields(Collection<Field> fields)
	{
		for (Field fe : fields) {
			if (Modifier.isStatic(fe.getModifiers())) {
				objects.add(fe);

				load(fe);
				Object t = getValue(fe);

				if (t != null) {
					updateValue(fe, t);
				}
			} else {
				System.err.println("Field: " + fe + ", is not static!");
			}
		}
	}

	protected static void check()
	{
		for (Field fe : objects) {
			if (values.containsKey(fe)) {
				String t = genChecksum(getValue(fe));
				String tg = values.get(fe);

				if (!Objects.equals(t, tg)) {
					differentValue(fe, getValue(fe));
				}
			}
		}
	}

	public static String genChecksum(Object t)
	{
		return DigestUtils.md5Hex(Startup.getGSON().toJson(t));
	}

	protected static void differentValue(Field fe, Object value)
	{
		updateValue(fe, value);

		if (!toSave.contains(fe)) {
			toSave.add(fe);

			executor.schedule(() -> {
				toSave.remove(fe);
				save(fe, value);
			}, 1000, TimeUnit.MILLISECONDS);
		}
	}

	protected static void updateValue(Field fe, Object t)
	{
		String checkSum = genChecksum(t);
		values.put(fe, checkSum);
	}

	protected static void save(Field fe,  Object value)
	{
		HashMap<String, Object> saveData = new HashMap<>();
		ArrayList<Field> obs = new ArrayList<>();

		if (value == null) {
			value = getValue(fe);
		}

		Save ob = null;

		boolean clas = fe.getDeclaringClass().isAnnotationPresent(Save.class);

		if (clas) {
			ob = fe.getDeclaringClass().getAnnotation(Save.class);

		} else if (fe.isAnnotationPresent(Save.class)) {
			ob = fe.getAnnotation(Save.class);
		}

		if (ob == null) {
			return;
		}

		String key = fe.getName();
		File fes = FileUtil.getFile(prefix + File.separator + ob.value());
		
		obs.add(fe);

		if (clas) {
			HashMap<String, Object> values = new HashMap<>();

			for (Field fe1 : fe.getDeclaringClass().getFields()) {
				values.put(fe1.getName(), getValue(fe1));
			}

			saveData.put(key, values);
		} else {
			saveData.put(key, value);
		}

		for (Field field : objects) {
			if (field.isAnnotationPresent(Save.class)) {
				Save ob1 = field.getAnnotation(Save.class);

				if (ob1.value().equals(ob.value())) {
					if (!obs.contains(field)) {
						obs.add(field);

						Object t = getValue(field);
						String key1 = field.getName();

						if (!saveData.containsKey(key1)) {
							saveData.put(key1, t);
						}
					}
				}
			}
		}

//		Startup.getDaprClient().saveState(Startup.getStateStore(), ob.value(), saveData).subscribe();
		try {
			write(fes.toPath(),Startup.getGSON().toJson(saveData));
		} catch (IOException e) {
			Logging.exception(e);
		}
	}

	protected static Object getValue(Field field)
	{
		try {
			if (Modifier.isStatic(field.getModifiers())) {
				if (!field.canAccess(null)) {
					field.setAccessible(true);
				}

				if (fieldClasses.containsKey(field)) {
					return field.get(fieldClasses.get(field));
				} else {
					Class<?> t = field.getDeclaringClass();
					fieldClasses.put(field, t);
					return field.get(t);
				}
			} else {
				System.err.println("Field: " + field + ", is not static!");
			}
		} catch (IllegalAccessException e) {
			Logging.exception(e);
		}

		return null;
	}

	protected static void load(Field fe)
	{
		try {
			if (fe.isAnnotationPresent(Save.class)) {
				Save ob = fe.getAnnotation(Save.class);
	
				
				File fes = FileUtil.getFile(prefix + File.separator + ob.value());
				JsonElement el = Startup.getGSON().fromJson(read(fes.toPath()), JsonElement.class);
				
				String key = fe.getName();
				
				if (el != null) {
					JsonObject obs = el.getAsJsonObject();
					if (obs.has(key)) {
						try {
							JsonElement t1 = obs.get(key);
							Object tg = Startup.getGSON().fromJson(t1, fe.getGenericType());

							setValue(fe, tg);
							updateValue(fe, tg);
						}catch (Exception e){
							Logging.exception(e);
						}
					}
				} else {
					save(fe, null);
				}
			}
		} catch (IOException e) {
			Logging.exception(e);
		}
	}

	protected static void loadClass(Class<?> fe)
	{
		try {
			if (fe.isAnnotationPresent(Save.class)) {
				Save ob = fe.getAnnotation(Save.class);

				File fes = FileUtil.getFile(prefix + File.separator + ob.value());
				JsonElement el = Startup.getGSON().fromJson(read(fes.toPath()), JsonElement.class);
				String key = fe.getName();

				if (el != null) {
					JsonObject obs = el.getAsJsonObject();

					if (obs.has(key)) {
						JsonElement t1 = obs.get(key);
						Map<String, Object> tg = Startup.getGSON().fromJson(t1,
						                                                    new TypeToken<Map<String, Object>>() {}.getType());

						for (Field fe1 : fe.getFields()) {
							if (tg.containsKey(fe1.getName())) {
								Object fg = Startup.getGSON().fromJson(Startup.getGSON().toJson(tg.get(fe1.getName())), fe1.getGenericType());

								setValue(fe1, fg);
								updateValue(fe1, fg);
							} else {
								save(fe1, null);
							}
						}

					} else {
						for (Field fe1 : fe.getFields()) {
							save(fe1, null);
						}
					}
				} else {
					for (Field fe1 : fe.getFields()) {
						save(fe1, null);
					}
				}
			}
		} catch (IOException e) {
			Logging.exception(e);
		}
	}

	protected static void setValue(Field field, Object value)
	{
		try {
			if (Modifier.isStatic(field.getModifiers())) {
				if (!field.canAccess(null)) {
					field.setAccessible(true);
				}

				Class<?> t;

				if (fieldClasses.containsKey(field)) {
					t = fieldClasses.get(field);
				} else {
					t = field.getDeclaringClass();
					fieldClasses.put(field, t);
				}

				if (t != null) {
					field.set(t, value);
				}
			} else {
				System.err.println("Field: " + field + ", is not static!");
			}
		} catch (IllegalAccessException e) {
			Logging.exception(e);
		}
	}

	public static String read(Path path) throws IOException
	{
		return Files.readString(path, StandardCharsets.UTF_8);
	}

	public static void write(Path path, String contents) throws IOException
	{
		Files.writeString(path, contents);
	}
}