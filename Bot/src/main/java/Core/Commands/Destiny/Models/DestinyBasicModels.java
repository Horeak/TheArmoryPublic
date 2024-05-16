package Core.Commands.Destiny.Models;

import Core.Commands.Destiny.system.DestinyItemSystem;

import java.util.HashMap;

public class DestinyBasicModels
{
	public record ScaledStat(Long statHash, boolean displayAsNumeric, StatDisplayInterpolation[] displayInterpolation) {}
	
	public record StatDisplayInterpolation(int value, int weight) {}
	
	public record MasterworkObject(String name, String description, String icon, Long statTypeHash, int value, boolean isConditionallyActive, String statName, String statDescription) {}
	
	public static class Destiny2ClassObject extends DatabaseDisplayObject
	{
		public int classType;
	}
	
	public static class Destiny2PlugSetObject extends DatabaseObject
	{
		public PlugItem[] reusablePlugItems;
	}
	
	public static class Destiny2ItemBucket extends DatabaseDisplayObject
	{
		public int index;
	}
	
	public static class Destiny2CollectibleObject extends DatabaseDisplayObject
	{
		public String sourceString;
	}
	
	public static class Destiny2PowerCapObject extends DatabaseObject
	{
		public int powerCap;
		public int index;
	}
	
	public static class Destiny2StatGroupObject extends DatabaseObject
	{
		public ScaledStat[] scaledStats;
	}
	
	public static class Perk
	{
		public Long perkHash;
		
		public String getName()
		{
			DatabaseDisplayObject object = DestinyItemSystem.destinyPerkObjects.getOrDefault(perkHash.intValue(), null);
			
			if (object != null) {
				return object.displayProperties.name;
			}
			
			return null;
		}
		
		
		public String getDescription()
		{
			DatabaseDisplayObject object = DestinyItemSystem.destinyPerkObjects.getOrDefault(perkHash.intValue(), null);
			
			if (object != null) {
				return object.displayProperties.description;
			}
			
			return null;
		}
	}
	
	public static class Plug
	{
		public String plugCategoryIdentifier;
		public String uiPlugLabel;
		public int plugStyle;
	}
	
	public static class StatsEntryObject
	{
		public Long statGroupHash;
		public HashMap<Long, StatEntryObject> stats;
	}
	
	public static class StatEntryObject extends BaseStatObject
	{
		public String nameOveride;
		
		public String getName()
		{
			if (nameOveride != null) {
				return nameOveride;
			}
			
			if (statHash == null) return null;
			
			DatabaseDisplayObject object1 = DestinyItemSystem.destinyStatObjects.getOrDefault(statHash.intValue(), null);
			
			if (object1 != null) {
				return object1.displayProperties.name;
			}
			
			return null;
		}
		
		
		public String getDescription()
		{
			if (statHash == null) return null;
			
			DatabaseDisplayObject object1 = DestinyItemSystem.destinyStatObjects.getOrDefault(statHash.intValue(), null);
			
			if (object1 != null) {
				return object1.displayProperties.description;
			}
			
			return null;
		}
	}
	
	public static class InvestmentStat
	{
		public final int oldValue = -1;
		public Long statTypeHash;
		public int value;
	}
	
	public record SocketEntries(SocketEntry[] socketEntries) {}
	
	public static class SocketEntry
	{
		public boolean defaultVisible;
		public boolean preventInitializationOnVendorPurchase;
		public Integer plugSources;
		public Long singleInitialItemHash;
		
		public PlugItem[] reusablePlugItems;
		public Long randomizedPlugSetHash;
	}
	
	public record PlugItem(Long plugItemHash) {}
	
	public record Quality(ItemVersionObject[] versions) {}
	
	public record ItemVersionObject(Long powerCapHash) {}
	public record EquippingBlock(int ammoType) {}
	
	public record InventoryDatabaseObject(int tierType, Long bucketTypeHash) {}
	
	public record ItemObjectives(
			Long[] objectiveHashes,
			Long[] displayActivityHashes,
			boolean requireFullObjectiveCompletion,
			Long questlineItemHash,
			Long questTypeHash,
			boolean isGlobalObjectiveItem,
			boolean useOnObjectiveCompletion,
			String narrative,
			String objectiveVerbName
	) {}
	
	public record ItemValueHolder(ItemValue[] itemValue, String valueDescription){}
	public record ItemValue(Long itemHash, int quantity) {}
	public record DestinyRewardObject(Long itemHash, int quantity, boolean hasConditionalVisibility){}
}
