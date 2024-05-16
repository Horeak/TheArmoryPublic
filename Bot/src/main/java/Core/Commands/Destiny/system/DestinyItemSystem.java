package Core.Commands.Destiny.system;

import Core.Commands.Destiny.Models.*;
import Core.Commands.Destiny.Models.DestinyBasicModels.Destiny2ClassObject;
import Core.Commands.Destiny.Models.DestinyBasicModels.SocketEntry;
import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Fields.JsonExclude;
import Core.Util.Utils;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Core.Commands.Destiny.system.DestinySystem.destiny_connect;

public class DestinyItemSystem
{
	@Getter
	private static Gson GSON;
	
	@DestinyData( clazz = DestinyItemObject.class, table = "DestinyInventoryItemDefinition")
	public static ConcurrentHashMap<Integer, DestinyItemObject> destinyItemObjects = new ConcurrentHashMap<>();
	
	@DestinyData(clazz = DatabaseDisplayObject.class, table = "DestinyStatDefinition")
	public static ConcurrentHashMap<Integer, DatabaseDisplayObject> destinyStatObjects = new ConcurrentHashMap<>();
	
	@DestinyData( clazz = DestinyBasicModels.Destiny2PowerCapObject.class, table = "DestinyPowerCapDefinition")
	public static ConcurrentHashMap<Integer, DestinyBasicModels.Destiny2PowerCapObject> destinyPowerCapObjects = new ConcurrentHashMap<>();
	
	@DestinyData( clazz = DestinyBasicModels.Destiny2CollectibleObject.class, table = "DestinyCollectibleDefinition")
	public static ConcurrentHashMap<Integer, DestinyBasicModels.Destiny2CollectibleObject> destinyCollectibleObjects = new ConcurrentHashMap<>();
	
	@DestinyData(clazz = DatabaseDisplayObject.class, table = "DestinySandboxPerkDefinition")
	public static ConcurrentHashMap<Integer, DatabaseDisplayObject> destinyPerkObjects = new ConcurrentHashMap<>();
	
	@DestinyData(clazz = DatabaseDisplayObject.class, table = "DestinyDamageTypeDefinition")
	public static ConcurrentHashMap<Integer, DatabaseDisplayObject> destinyDamageTypeObjects = new ConcurrentHashMap<>();
	
	@DestinyData( clazz = Destiny2ClassObject.class, table = "DestinyClassDefinition")
	public static ConcurrentHashMap<Integer, DestinyBasicModels.Destiny2ClassObject> destinyClassObjects = new ConcurrentHashMap<>();
	
	@DestinyData( clazz = DestinyBasicModels.Destiny2StatGroupObject.class, table = "DestinyStatGroupDefinition")
	public static ConcurrentHashMap<Integer, DestinyBasicModels.Destiny2StatGroupObject> destinyStatGroupObjects = new ConcurrentHashMap<>();
	
	@DestinyData( clazz = DestinyBasicModels.Destiny2PlugSetObject.class, table = "DestinyPlugSetDefinition")
	public static ConcurrentHashMap<Integer, DestinyBasicModels.Destiny2PlugSetObject> destinyPlugSetObjects = new ConcurrentHashMap<>();
	
	@DestinyData( clazz = DestinyBasicModels.Destiny2ItemBucket.class, table = "DestinyInventoryBucketDefinition")
	public static ConcurrentHashMap<Integer, DestinyBasicModels.Destiny2ItemBucket> destinyBucketObjects = new ConcurrentHashMap<>();
	
	@DestinyData( clazz = DestinyRecordObject.class, table = "DestinyRecordDefinition")
	public static ConcurrentHashMap<Integer, DestinyRecordObject> destinyRecordObjects = new ConcurrentHashMap<>();
	
	@DestinyData( clazz = DestinyObjectiveObject.class, table = "DestinyObjectiveDefinition")
	public static ConcurrentHashMap<Integer, DestinyObjectiveObject> destinyObjectiveObjects = new ConcurrentHashMap<>();
	
	public static final String[] IGNORED_TYPES = new String[]{
			"clan"
	};
	
	public static void reInit()
	{
		try {
			clear();
			init();
		} catch (Exception e) {
			Logging.exception(e);
		}
	}
	
	@Retention( RetentionPolicy.RUNTIME )
	@Target( {ElementType.FIELD, ElementType.TYPE} )
	public @interface DestinyData
	{
		Class<? extends DatabaseObject> clazz();
		String table();
	}
	
	public static void clear()
	{
		Set<Field> fields = Startup.getReflection().getFieldsAnnotatedWith(DestinyData.class);
		fields.forEach(field -> {
			DestinyData data = field.getAnnotation(DestinyData.class);
			ConcurrentHashMap map = makeMakeMap(data.clazz());
			try {
				field.set(null, map);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});
		
		DestinySeasonSystem.destinySeasonNumbers.clear();
		DestinySeasonSystem.destinySeasons.clear();
	}
	
	public static void init()
	{
		var builder = new GsonBuilder().serializeNulls().addSerializationExclusionStrategy(new ExclusionStrategy()
		{
			public boolean shouldSkipField(FieldAttributes field)
			{
				return field.getAnnotation(JsonExclude.class) != null;
			}
			public boolean shouldSkipClass(Class<?> clazz)
			{
				return clazz.getAnnotation(JsonExclude.class) != null;
			}
		}).setPrettyPrinting();
		
		
		Set<Field> fields = Startup.getReflection().getFieldsAnnotatedWith(DestinyData.class);
		
		
		fields.forEach(field -> {
			DestinyData data = field.getAnnotation(DestinyData.class);
			
			//TODO When value is Long try to parse to destiny object. Might have to fetch these values after initial load to get the correct values
			//builder.registerTypeAdapter(data.clazz(), new DestinyDataParser<>(data.clazz()));
		});
		
		GSON = builder.create();
		
		DestinySeasonSystem.generateDestinySeasons();
		
		fields.forEach(field -> {
			DestinyData data = field.getAnnotation(DestinyData.class);
			ConcurrentHashMap map = makeMakeMap(data.clazz());
			fetchAndStore(data.table(), data.clazz(), map);
			try {
				field.set(null, map);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});
		
		for (Map.Entry<Integer, DestinyItemObject> ent : destinyItemObjects.entrySet()) {
			if (ent.getValue() != null) {
				ent.getValue().finalizeObject();
			} else {
				destinyItemObjects.remove(ent.getKey());
			}
		}
	}
	
	public static <T> ConcurrentHashMap<Integer,T> makeMakeMap(Class<T> clazz){
		return new ConcurrentHashMap<>();
	}
	
	public static <T> void fetchAndStore(String query, Class<T> clazz, Map<Integer, T> map) {
		try (
				Connection con = destiny_connect();
				PreparedStatement statement = con.prepareStatement("select * from " + query);
				ResultSet rs = statement.executeQuery()
		) {
			while (rs.next()) {
				long id = Long.parseLong(rs.getString(1));
				String json = rs.getString(2);
				
				if (json == null || json.isEmpty()) {
					continue;
				}
				
				T object = getGSON().fromJson(json, clazz);
				
				if(object instanceof DatabaseDisplayObject databaseDisplayObject){
					if ((databaseDisplayObject.displayProperties == null || databaseDisplayObject.displayProperties.name == null || databaseDisplayObject.displayProperties.name.isEmpty())) {
						continue;
					}
					
				}
				
				map.put((int) id, object);
			}
		} catch (Exception e) {
			Logging.exception(e);
		}
	}
	
	public static ArrayList<DestinyItemObject> getItemsByName(String name)
	{
		ArrayList<DestinyItemObject> inObjects = new ArrayList<>(destinyItemObjects.values());
		
		addSandboxPerks(name, inObjects);
		
		if (inObjects.isEmpty()) return new ArrayList<>();
		
		inObjects.removeIf(Objects::isNull);
		
		inObjects.removeIf(
				(ob) -> ob.displayProperties == null || ob.displayProperties.name == null || ob.displayProperties.name.isEmpty());
		//		inObjects.removeIf((ob) -> ob.displayProperties.description == null || ob.displayProperties.description.isEmpty());
		inObjects.removeIf((ob) -> ob.displayProperties.name == null || ob.displayProperties.name.isEmpty());
		inObjects.removeIf((ob) -> {
			String j1 = ob.displayProperties.name.toLowerCase().replace(" ", "");
			String j2 = name.toLowerCase().replace(" ", "");
			
			return !j1.contains(j2);
		});
		
		inObjects.removeIf((ob) -> Arrays.stream(IGNORED_TYPES).anyMatch(
				(s) -> ob.itemTypeDisplayName != null && ob.itemTypeDisplayName.toLowerCase().contains(s.toLowerCase())));
		
		inObjects.sort(
				Comparator.comparingDouble(o -> Utils.compareStrings(o.getName().toLowerCase().replace(" ", ""), name.toLowerCase().replace(" ", ""))));
		inObjects.sort((j1, j2) -> (j1.equippable && j2.equippable) ? (Integer.compare(j2.itemType,
		                                                                               j1.itemType)) : (j1.equippable ? -1 : j2.equippable ? 1 : 0));
		inObjects.sort(
				(j1, j2) -> (j1.inventory == null && j2.inventory != null) ? -1 : (j1.inventory != null && j2.inventory == null) ? 1 : (j1.inventory == null && j2.inventory == null) ? 0 : Integer.compare(
						j2.inventory.tierType(), j1.inventory.tierType()));
		inObjects.sort((j1, j2) -> (Integer.compare(j2.source != null && !j2.source.isEmpty() ? 1 : 0,
		                                            j1.source != null && !j1.source.isEmpty() ? 1 : 0)));
		
		//If items have same rarity then weapons get priority
		inObjects.sort((j1, j2) -> {
			// Null checks for inventory
			if (j1.inventory == null && j2.inventory == null) return 0;
			if (j1.inventory == null) return 1;  // put j1 after j2
			if (j2.inventory == null) return -1; // put j1 before j2
			
			// Both inventory are not null, check tierType
			if (j1.inventory.tierType() == j2.inventory.tierType()) {
				if (j1.itemType == 3 && j2.itemType != 3) {
					return -1; // put j1 before j2
				} else if (j2.itemType == 3 && j1.itemType != 3) {
					return 1;  // put j1 after j2
				} else {
					return 0;  // equal
				}
			} else {
				return 0; // If tierType is not equal, consider them equal for this sorting
			}
		});
		
		handleEmptyItemType(inObjects);
		
		return inObjects;
	}
	
	//This is hard coded and needs to be changed as soon as possible
	@Deprecated
	protected static void handleEmptyItemType(ArrayList<DestinyItemObject> inObjects)
	{
		inObjects.forEach((o) -> {
			if (o.itemTypeDisplayName == null || o.itemTypeDisplayName.isEmpty() || o.itemTypeAndTierDisplayName == null || o.itemTypeAndTierDisplayName.isEmpty()) {
				
				if (o.plug != null) {
					if (o.plug.uiPlugLabel != null && !o.plug.uiPlugLabel.isEmpty()) {
						if (o.plug.uiPlugLabel.equalsIgnoreCase("masterwork")) {
							if (o.displayProperties.name.toLowerCase().contains("catalyst")) {
								o.itemTypeDisplayName = "Catalyst";
								o.itemTypeAndTierDisplayName = "Catalyst";
							} else {
								o.itemTypeDisplayName = "Masterwork";
								o.itemTypeAndTierDisplayName = "Masterwork";
							}
						}
					}
				}
			}
		});
	}
	
	protected static void addSandboxPerks(
			String name, ArrayList<DestinyItemObject> inObjects)
	{
		ArrayList<DatabaseDisplayObject> perkObjects = new ArrayList<>(destinyPerkObjects.values());
		
		perkObjects.removeIf(Objects::isNull);
		
		perkObjects.removeIf(
				(ob) -> ob.displayProperties == null || ob.displayProperties.name == null || ob.displayProperties.name.isEmpty());
		perkObjects.removeIf(
				(ob) -> ob.displayProperties.description == null || ob.displayProperties.description.isEmpty());
		perkObjects.removeIf((ob) -> ob.displayProperties.name == null || ob.displayProperties.name.isEmpty());
		perkObjects.removeIf((ob) -> !ob.displayProperties.name.toLowerCase().contains(name.toLowerCase()));
		
		
		perkObjects.sort((j1, j2) -> {
			Pattern p = Pattern.compile("(?i)(?:^|\\W)" + name + "(?:$|\\W)");
			Matcher m1 = p.matcher(j1.displayProperties.name);
			Matcher m2 = p.matcher(j2.displayProperties.name);
			
			boolean e1 = m1.find();
			boolean e2 = m2.find();
			
			return e1 && e2 ? 0 : e1 ? -1 : e2 ? 1 : 0;
		});
		
		if (perkObjects.size() > 0) {
			for (DatabaseDisplayObject object : perkObjects) {
				DestinyItemObject object1 = new DestinyItemObject();
				object1.displayProperties = object.displayProperties;
				object1.hash = object.hash;
				
				object1.itemTypeDisplayName = "Perk";
				object1.itemTypeAndTierDisplayName = "Perk";
				
				inObjects.add(object1);
			}
		}
	}
	
	public static ArrayList<DestinyItemObject> getSinglePerks(DestinyItemObject object)
	{
		ArrayList<DestinyItemObject> list = new ArrayList<>();
		
		if (object.sockets != null && object.sockets.socketEntries() != null) {
			for (SocketEntry socket : object.sockets.socketEntries()) {
				if (socket != null && socket.singleInitialItemHash != null) {
					DestinyItemObject perkObject = DestinyItemSystem.destinyItemObjects.getOrDefault(
							socket.singleInitialItemHash.intValue(), null);
					
					if (perkObject == null) {
						continue;
					}
					
					list.add(perkObject);
				}
			}
		}
		
		return list;
	}
}