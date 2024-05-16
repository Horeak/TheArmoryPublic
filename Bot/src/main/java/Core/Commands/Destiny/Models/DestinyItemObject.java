package Core.Commands.Destiny.Models;

import Core.Commands.Destiny.Models.DestinyBasicModels.*;
import Core.Commands.Destiny.system.DestinyItemSystem;
import Core.Commands.Destiny.system.DestinySeasonSystem;
import Core.Commands.Destiny.system.DestinySystem;
import Core.Objects.Annotation.Fields.JsonExclude;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import java.util.*;


public class DestinyItemObject extends DestinyBaseItemObject
{
	@JsonExclude
	public final HashMap<String, String> infoValues = new HashMap<>();
	
	@JsonExclude
	public final HashMap<String, String> seasonInfo = new HashMap<>();
	
	@JsonExclude
	public final HashMap<String, String> powerInfo = new HashMap<>();
	
	@JsonExclude
	public final HashMap<Integer, ArrayList<SocketObject>> perkMap = new HashMap<>();
	
	@JsonExclude
	public final ArrayList<DestinyBasicModels.MasterworkObject> masterworkObjects = new ArrayList<>();
	
	@JsonExclude
	public boolean preset = false;
	
	public Perk[] perks;
	public Plug plug;
	public EquippingBlock equippingBlock;
	public InventoryDatabaseObject inventory;
	public StatsEntryObject stats;
	public InvestmentStat[] investmentStats;
	public SocketEntries sockets;
	public ItemObjectives objectives;
	public ItemValueHolder value;
	
	public int classType;
	public Long defaultDamageTypeHash;
	public Long[] itemCategoryHashes;
	public Long collectibleHash;
	public boolean equippable;
	public int itemType;
	public int itemSubType;
	public String itemTypeAndTierDisplayName;
	public String itemTypeDisplayName;
	public String screenshot;
	public String iconWatermark;
	public int itemVersion = -1;
	public Quality quality;
	
	public String slotInfo = null;
	
	public void finalizeObject()
	{
		String dlc = getDLCName();
		String season = getSeasonName();
		
		boolean multipleVersions = quality != null && quality.versions() != null && quality.versions().length > 1;
		
		//For some reason items that dont have a limit just has a very very high number so this is to limit those
		int powerLimit = 10000;
		
		if (dlc != null) {
			this.dlc = dlc;
			if (getItemTier() != 6) {
				if (multipleVersions) {
					int num = 0;
					StringJoiner joiner = new StringJoiner(", ");
					joiner.add("**" + dlc + "**");
					
					for (ItemVersionObject object : quality.versions()) {
						DestinyBasicModels.Destiny2PowerCapObject val = DestinyItemSystem.destinyPowerCapObjects.getOrDefault(object.powerCapHash().intValue(), null);
						
						if (val != null) {
							if (DestinySeasonSystem.destinySeasons.containsKey(val.index)) {
								DestinySeasonObject seasonObject = DestinySeasonSystem.destinySeasons.get(val.index);
								
								if (seasonObject.DLCName != null && !seasonObject.DLCName.isBlank()) {
									if (num != 0) { //Cheaty way to fix bungie setting the first season to 7 or 8 even if they came before. It skips the first dlc because it is already added earlier
										joiner.add("**" + seasonObject.DLCName + "**");
									}
									num++;
								}
							}
						}
					}
					
					if (!joiner.toString().isBlank()) {
						seasonInfo.put(num > 1 ? "DLCs" : "DLC", joiner.toString());
					}
					
				} else {
					if (itemVersion == -1) {
						seasonInfo.put("DLC", "**" + dlc + "**");
					} else {
						Long hashId = quality.versions()[itemVersion].powerCapHash();
						
						DestinyBasicModels.Destiny2PowerCapObject val = DestinyItemSystem.destinyPowerCapObjects.getOrDefault(hashId.intValue(), null);
						
						if (val != null) {
							if (val.index == 7) {
								seasonInfo.put("DLC", "**" + dlc + "**");
								
							} else {
								if (DestinySeasonSystem.destinySeasons.containsKey(val.index)) {
									DestinySeasonObject seasonObject = DestinySeasonSystem.destinySeasons.get(val.index);
									
									if (seasonObject.DLCName != null && !seasonObject.DLCName.isBlank()) {
										seasonInfo.put("DLC", "**" + seasonObject.DLCName + "**");
									}
								}
							}
						}
					}
				}
			} else {
				seasonInfo.put("DLC", "**" + dlc + "**");
				
			}
		}
		
		if (season != null) {
			if (getItemTier() != 6) {
				if (multipleVersions) {
					int num = 0;
					StringJoiner joiner = new StringJoiner(", ");
					joiner.add("**" + season + "**");
					for (ItemVersionObject object : quality.versions()) {
						DestinyBasicModels.Destiny2PowerCapObject val = DestinyItemSystem.destinyPowerCapObjects.getOrDefault(object.powerCapHash().intValue(), null);
						
						if (val != null) {
							if (DestinySeasonSystem.destinySeasons.containsKey(val.index)) {
								DestinySeasonObject seasonObject = DestinySeasonSystem.destinySeasons.get(val.index);
								
								if (seasonObject.seasonName != null && !seasonObject.seasonName.isBlank()) {
									if (num != 0) { //Cheaty way to fix bungie setting the first season to 7 or 8 even if they came before. It skips the first season because it is already added earlier
										joiner.add("**" + seasonObject.seasonName + "**");
									}
									num++;
								}
							}
						}
					}
					
					if (!joiner.toString().isBlank()) {
						seasonInfo.put(num > 1 ? "Seasons" : "Season", joiner.toString());
					}
				} else {
					if (itemVersion == -1) {
						seasonInfo.put("Season", "**" + season + "**");
					} else {
						Long hashId = quality.versions()[itemVersion].powerCapHash();
						
						DestinyBasicModels.Destiny2PowerCapObject val = DestinyItemSystem.destinyPowerCapObjects.getOrDefault(hashId.intValue(), null);
						
						if (val != null) {
							if (val.index == 7) {
								seasonInfo.put("Season", "**" + season + "**");
								
							} else {
								if (DestinySeasonSystem.destinySeasons.containsKey(val.index)) {
									DestinySeasonObject seasonObject = DestinySeasonSystem.destinySeasons.get(val.index);
									
									if (seasonObject.seasonName != null && !seasonObject.seasonName.isBlank()) {
										seasonInfo.put("Season", "**" + seasonObject.seasonName + "**");
									}
								}
							}
						}
					}
				}
			} else {
				seasonInfo.put("Season", "**" + season + "**");
			}
		}
		
		if (getItemTier() != 6) {
			if (quality != null) {
				if (quality.versions() != null && quality.versions().length > 0) {
					if (quality.versions().length > 1) {
						if (itemVersion != -1) {
							Long hashId = quality.versions()[itemVersion].powerCapHash();
							
							DestinyBasicModels.Destiny2PowerCapObject val = DestinyItemSystem.destinyPowerCapObjects.getOrDefault(hashId.intValue(), null);
							
							if (val != null) {
								if (val.powerCap < powerLimit) {
									powerInfo.put("Power Cap", "**" + val.powerCap + "**");
								}
							}
						} else {
							StringJoiner joiner = new StringJoiner(", ");
							for (ItemVersionObject object : quality.versions()) {
								DestinyBasicModels.Destiny2PowerCapObject val = DestinyItemSystem.destinyPowerCapObjects.getOrDefault(object.powerCapHash().intValue(), null);
								
								if (val != null) {
									if (val.powerCap < powerLimit) {
										joiner.add("**" + val.powerCap + "**");
									}
								}
							}
							
							if (!joiner.toString().isBlank()) {
								powerInfo.put("Power Cap", joiner.toString());
							}
						}
					} else {
						Long hashId = quality.versions()[0].powerCapHash();
						
						DestinyBasicModels.Destiny2PowerCapObject val = DestinyItemSystem.destinyPowerCapObjects.getOrDefault(hashId.intValue(), null);
						
						if (val != null) {
							if (val.powerCap < powerLimit) {
								powerInfo.put("Power Cap", "**" + val.powerCap + "**");
							}
						}
					}
				}
			}
		}
		
		if (sockets != null && sockets.socketEntries() != null && Arrays.stream(sockets.socketEntries()).noneMatch(
				(ob) -> ob.randomizedPlugSetHash != null && DestinyItemSystem.destinyPlugSetObjects.getOrDefault(ob.randomizedPlugSetHash.intValue(), null) != null && DestinyItemSystem.destinyPlugSetObjects.getOrDefault(ob.randomizedPlugSetHash.intValue(), null).reusablePlugItems.length > 0)) {
			
			preset = true;
		}
		
		if (stats != null && stats.stats != null && stats.stats.size() > 0) {
			stats.stats.keySet().removeIf(Objects::isNull);
			stats.stats.values().removeIf(Objects::isNull);
			
			stats.stats.entrySet().removeIf((ent) -> ent.getValue() == null || ent.getValue().getName() == null || ent.getKey() == null);
		}
		
		//Handle curated and non curated stats
		if (!preset) {
			//Correct the stats to account for random perks
			//TODO This may be using the wrong statGroup?
			for (DestinyItemObject perkObject : DestinyItemSystem.getSinglePerks(this)) {
				for (BaseStatObject stat : perkObject.getStats().values()) {
					BaseStatObject curStat = null;
					
					for (BaseStatObject stat1 : getStats().values()) {
						if (stat1.statHash.equals(stat.statHash)) {
							curStat = stat1;
							break;
						}
					}
					
					if (curStat != null) {
						if (curStat.getName() == null) continue;
						
						if (curStat.getName().equalsIgnoreCase("charge time") || curStat.getName().equalsIgnoreCase("draw time")) {
							stat.value = stat.value * -1;
						}
						
						curStat.value += stat.value;
					}
				}
			}
		}
		
		if (preset) {
			//Increase stats based on preset perks
			for (DestinyItemObject perkObject : DestinyItemSystem.getSinglePerks(this)) {
				
				for (InvestmentStat stat : perkObject.investmentStats) {
					InvestmentStat curStat = null;
					
					for (InvestmentStat stat1 : investmentStats) {
						if (stat1.statTypeHash.equals(stat.statTypeHash)) {
							curStat = stat1;
							break;
						}
					}
					
					if (curStat != null) {
						curStat.value += stat.value;
					}
				}
			}
		}
		
		if (preset) {
			//Currated and preset shouldnt show max/min as it has predeterminted stats
			for (BaseStatObject stat : getStats().values()) {
				stat.maximum = 0;
				stat.minimum = 0;
			}
		}
		
		//Class type if there is one
		if (classType != 3) {
			String className = getClassName();
			
			if (className != null) {
				infoValues.put("Class", "**" + className + "**");
			}
		}
		
		String slotText = null;
		
		//Set ammo type/slot type
		if (equippingBlock != null) {
			String slot = equippingBlock.ammoType() == 1 ? "**Primary** " + DestinySystem.PRIMARY_ICON : equippingBlock.ammoType() == 2 ? "**Special** " + DestinySystem.SPECIAL_ICON : equippingBlock.ammoType() == 3 ? "**Heavy** " + DestinySystem.HEAVY_ICON : "";
			
			if (!slot.isEmpty()) {
				slotText = slot;
			}
		}
		
		//Set damage type
		if (defaultDamageTypeHash != null) {
			DatabaseDisplayObject damageTypeObject = DestinyItemSystem.destinyDamageTypeObjects.getOrDefault(defaultDamageTypeHash.intValue(), null);
			
			if (damageTypeObject != null) {
				String damageType = "**" + damageTypeObject.displayProperties.name + "** " + DestinySystem.getIcon(damageTypeObject.displayProperties.name);
				if(slotText != null) {
					slotText +=  " | " + damageType;
				}else{
					slotText = damageType;
				}
			}
		}
		
		if(slotText != null){
			slotInfo = slotText;
		}
		
		//Add perks and masterworks
		if (sockets != null) {
			if (sockets.socketEntries() != null) {
				int i = 0;
				
				for (SocketEntry entry : sockets.socketEntries()) {
					i++;
					
					
					//Masterwork
					if (entry.defaultVisible) {
						if (!preset && entry.reusablePlugItems != null && entry.reusablePlugItems.length > 0) {
							for (PlugItem item : entry.reusablePlugItems) {
								getMasterworkFromObject(DestinyItemSystem.destinyItemObjects.getOrDefault(item.plugItemHash().intValue(), null));
							}
						} else {
							if (entry.singleInitialItemHash != null) {
								getMasterworkFromObject(DestinyItemSystem.destinyItemObjects.getOrDefault(entry.singleInitialItemHash.intValue(), null));
							}
						}
					}
					
					masterworkObjects.removeIf((m) -> {
						for (BaseStatObject object1 : getStats().values()) {
							if (object1 != null && object1.getName() != null) {
								if (object1.getName().equalsIgnoreCase(m.statName())) {
									return false;
								}
							}
						}
						
						return true;
					});
					
					masterworkObjects.removeIf((m) -> m.statName().equalsIgnoreCase("impact") && itemSubType != 18); //18 is subtype for sword
					masterworkObjects.removeIf((m) -> m.statName().equalsIgnoreCase("accuracy") && itemSubType != 31); //31 is subtype for bow
					
					if (entry.preventInitializationOnVendorPurchase) {
						continue;
					}
					
					if (entry.plugSources == 3 || entry.plugSources == 7 || entry.plugSources == 13) {
						continue;
					}
					
					if (entry.singleInitialItemHash == 2285418970L || entry.singleInitialItemHash == 4248210736L) {
						continue;
					}
					
					if (!preset && entry.randomizedPlugSetHash != null) {
						Destiny2PlugSetObject setObject = DestinyItemSystem.destinyPlugSetObjects.getOrDefault(entry.randomizedPlugSetHash.intValue(), null);
						
						if (setObject != null) {
							StringJoiner joiner = new StringJoiner(", ");
							int added = 0;
							boolean addedSocket = false;
							
							
							if(setObject.reusablePlugItems.length == 1){
								PlugItem socket = setObject.reusablePlugItems[0];
								DestinyItemObject socketItem = DestinyItemSystem.destinyItemObjects.getOrDefault(socket.plugItemHash().intValue(), null);
								
								if (!perkMap.containsKey(i)) {
									perkMap.put(i, new ArrayList<>());
								}
								
								perkMap.get(i).add(new SocketObject(socketItem.getName(), socketItem.getDescription(), i, socket.plugItemHash()));
								
							}else if(setObject.reusablePlugItems.length > 1){
								for (PlugItem socket : setObject.reusablePlugItems) {
									DestinyItemObject socketItem = DestinyItemSystem.destinyItemObjects.getOrDefault(socket.plugItemHash().intValue(), null);
									
									if (socketItem != null) {
										if (joiner.length() + socketItem.getName().length() >= (MessageEmbed.VALUE_MAX_LENGTH - 20)) {
											joiner.add("and " + (entry.reusablePlugItems.length - added) + " more.");
											
											if (!perkMap.containsKey(i)) {
												perkMap.put(i, new ArrayList<>());
											}
											
											perkMap.get(i).add(new SocketObject("Possible Perks", joiner.toString(), i, socket.plugItemHash()));
											addedSocket = true;
											break;
										} else {
											if (!joiner.toString().contains("`" + socketItem.getName() + "`")) {
												joiner.add("`" + socketItem.getName() + "`");
												added++;
											}
										}
									}
								}
								
								if (!addedSocket) {
									if (!perkMap.containsKey(i)) {
										perkMap.put(i, new ArrayList<>());
									}
									
									perkMap.get(i).add(new SocketObject("Possible Perks", joiner.toString(), i));
								}
							}
						}
					} else if (entry.reusablePlugItems != null && entry.reusablePlugItems.length > 0) {
						for (PlugItem item : entry.reusablePlugItems) {
							DestinyItemObject socketItem = DestinyItemSystem.destinyItemObjects.getOrDefault(item.plugItemHash().intValue(), null);
							
							if (socketItem != null) {
								if (!perkMap.containsKey(i)) {
									perkMap.put(i, new ArrayList<>());
								}
								
								perkMap.get(i).add(new SocketObject(socketItem.getName(), socketItem.getDescription(), i, item.plugItemHash()));
							}
						}
						
					} else {
						DestinyItemObject socketItem = DestinyItemSystem.destinyItemObjects.getOrDefault(entry.singleInitialItemHash.intValue(), null);
						
						if (socketItem != null) {
							if (!perkMap.containsKey(i)) {
								perkMap.put(i, new ArrayList<>());
							}
							
							perkMap.get(i).add(new SocketObject(socketItem.getName(), socketItem.getDescription(), i, entry.singleInitialItemHash));
						}
					}
				}
			}
		}
		
		if (collectibleHash != null) {
			DestinyBasicModels.Destiny2CollectibleObject collectibleObject = DestinyItemSystem.destinyCollectibleObjects.getOrDefault(collectibleHash.intValue(), null);
			
			if (collectibleObject != null) {
				if (collectibleObject.sourceString != null && !collectibleObject.sourceString.isEmpty()) {
					source = collectibleObject.sourceString;
				}
			}
		}
		
		//Add any perks the item may have to the end of the perk map, this will likely only affect catalysts
		if (perks != null) {
			for (Perk perk : perks) {
				DatabaseDisplayObject perkObject = DestinyItemSystem.destinyPerkObjects.getOrDefault(perk.perkHash.intValue(), null);
				
				if (perkObject != null) {
					if (perkObject.displayProperties != null && perkObject.displayProperties.name != null) {
						int perkMapSize = perkMap.size() + 1;
						
						if(perkObject.displayProperties.name.equals(displayProperties.name)){
							continue;
						}
						
						perkMap.put(perkMapSize, new ArrayList<>());
						perkMap.get(perkMapSize).add(new SocketObject(perkObject.displayProperties.name, perkObject.displayProperties.description, perkMapSize));
					}
				}
			}
		}
	}
	
	protected void getMasterworkFromObject(DestinyItemObject object)
	{
		if (object != null) {
			if (!object.displayProperties.name.replace(" ", "").equalsIgnoreCase("masterwork")) {
				return;
			}
			
			DatabaseDisplayObject statOb = null;
			InvestmentStat stat = null;
			
			if (object.investmentStats != null) {
				for (InvestmentStat stat1 : object.investmentStats) {
					statOb = DestinyItemSystem.destinyStatObjects.getOrDefault(stat1.statTypeHash.intValue(), null);
					
					if (statOb != null) {
						stat = stat1;
						break;
					}
				}
			}
			
			if (statOb != null) {
				//TODO Maybe add support for tier 1-9 masterwork later down the line
				DestinyBasicModels.MasterworkObject masterworkObject = new DestinyBasicModels.MasterworkObject(object.displayProperties.name, object.displayProperties.description, object.displayProperties.icon, stat.statTypeHash, stat.value, false, statOb.displayProperties.name,
				                                                                                               statOb.displayProperties.description);
				masterworkObjects.add(masterworkObject);
			}
		}
	}
	
	@Override
	public String getName()
	{
		if (itemType == 19) {
			if (displayProperties.name.equalsIgnoreCase("Masterwork")) {
				if (investmentStats != null) {
					for (InvestmentStat stat : investmentStats) {
						DatabaseDisplayObject object = DestinyItemSystem.destinyStatObjects.getOrDefault(stat.statTypeHash.intValue(), null);
						
						if (object != null && object.displayProperties != null) {
							return object.displayProperties.name + " " + displayProperties.name;
						}
					}
				}
			}
		}
		return displayProperties.name;
	}
	
	@Override
	public String getDescription()
	{
		if (itemType == 19) {//4104513227 is the ID for armor mods
			if (itemCategoryHashes != null && Arrays.asList(itemCategoryHashes).contains(4104513227L)) {
				return displayProperties.description + " (" + itemTypeDisplayName + ")";
			}
			
			if (displayProperties.description == null || displayProperties.description.isBlank()) {
				if (perks != null) {
					for (Perk perk : perks) {
						DatabaseDisplayObject object = DestinyItemSystem.destinyPerkObjects.getOrDefault(perk.perkHash.intValue(), null);
						
						if (object != null && object.displayProperties != null && !object.displayProperties.description.isBlank()) {
							return object.displayProperties.description;
						}
					}
				}
			}
		}
		return displayProperties.description;
	}
	
	@Override
	public String getIcon()
	{
		return displayProperties.icon;
	}
	
	@Override
	public String getImage()
	{
		return screenshot;
	}
	
	@Override
	public int getItemTier()
	{
		return inventory != null ? inventory.tierType() : 0;
	}
	
	@Override
	public String getItemTierAndType()
	{
		return itemTypeAndTierDisplayName;
	}
	
	@Override
	public boolean isEquippable()
	{
		return equippable;
	}
	
	public String getClassName()
	{
		if (classType != 3) {
			String className = null;
			
			for (DestinyBasicModels.Destiny2ClassObject clas : DestinyItemSystem.destinyClassObjects.values()) {
				if (clas.classType == classType) {
					className = clas.displayProperties.name;
					break;
				}
			}
			
			return className;
		}
		
		return null;
	}
	
	
	public int getSeasonNumber()
	{
		return DestinySeasonSystem.destinySeasonNumbers.getOrDefault(hash, -1);
	}
	
	public String getSeasonName()
	{
		if (getSeasonNumber() != -1) {
			DestinySeasonObject object = DestinySeasonSystem.destinySeasons.getOrDefault(getSeasonNumber(), null);
			
			if (object != null) {
				if (object.seasonName != null && !object.seasonName.isEmpty()) {
					return object.seasonName;
				}
			}
		}
		
		return null;
	}
	
	public String getDLCName()
	{
		if (getSeasonNumber() != -1) {
			DestinySeasonObject object = DestinySeasonSystem.destinySeasons.getOrDefault(getSeasonNumber(), null);
			
			if (object != null) {
				if (object.DLCName != null && !object.DLCName.isEmpty()) {
					return object.DLCName;
				}
			}
		}
		
		return null;
	}
	
	
	public HashMap<Long, BaseStatObject> getStats()
	{
		if (stats != null && stats.statGroupHash != null) {
			DestinyBasicModels.Destiny2StatGroupObject statGroup = DestinyItemSystem.destinyStatGroupObjects.getOrDefault(stats.statGroupHash.intValue(), null);
			
			if (statGroup != null && statGroup.scaledStats != null) {
				//TODO If investment stat is used for an item it will need to be calculated using the stat group
				if (investmentStats != null && investmentStats.length > 0) {
					HashMap<Long, BaseStatObject> map = new HashMap<>();
					
					for (ScaledStat scaledStat : statGroup.scaledStats) {
						for (InvestmentStat stat : investmentStats) {
							if (scaledStat.statHash().equals(stat.statTypeHash)) {
								if (scaledStat.displayInterpolation() != null && scaledStat.displayInterpolation().length >= 2) {
									StatEntryObject object = new StatEntryObject();
									
									StatDisplayInterpolation start = scaledStat.displayInterpolation()[0];
									StatDisplayInterpolation end = scaledStat.displayInterpolation()[scaledStat.displayInterpolation().length - 1];
									
									object.minimum = start.weight();
									object.maximum = end.weight();
									
									object.displayAsNumeric = scaledStat.displayAsNumeric();
									
									ArrayList<Double> xPos = new ArrayList<>();
									ArrayList<Double> yPos = new ArrayList<>();
									
									for (StatDisplayInterpolation dis : scaledStat.displayInterpolation()) {
										xPos.add((double)dis.value());
										yPos.add((double)dis.weight());
									}
									
									//TODO Replace this with something more reliable and faster
									LinearInterpolator li = new LinearInterpolator();
									PolynomialSplineFunction function = li.interpolate(ArrayUtils.toPrimitive(xPos.toArray(new Double[0])), ArrayUtils.toPrimitive(yPos.toArray(new Double[0])));
									
									
									//TODO This is a cheaty way to do it. Look more at expanding the model if it goes out of bounds
									if (stat.value < start.value()) {
										stat.value = start.value();
									}
									
									if (stat.value > end.value()) {
										stat.value = end.value();
									}
									
									object.value = (int)Math.round(function.value(stat.value));
									object.statHash = stat.statTypeHash;
									object.oldValue = stat.oldValue;
									
									if (stats != null && stats.stats != null && stats.stats.size() > 0) {
										for (StatEntryObject statEntry : stats.stats.values()) {
											if (stat.statTypeHash.equals(statEntry.statHash)) {
												if (statEntry.minimum != 0) object.minimum = statEntry.minimum;
												if (statEntry.maximum != 0) object.maximum = statEntry.maximum;
												break;
											}
										}
									}
									
									map.put(stat.statTypeHash, object);
									break;
								}
							}
						}
						
						if (!map.containsKey(scaledStat.statHash())) {
							StatEntryObject statObject = new StatEntryObject();
							statObject.statHash = scaledStat.statHash();
							statObject.value = -1;
							
							StatDisplayInterpolation start = scaledStat.displayInterpolation()[0];
							StatDisplayInterpolation end = scaledStat.displayInterpolation()[scaledStat.displayInterpolation().length - 1];
							
							statObject.minimum = start.weight();
							statObject.maximum = end.weight();
							
							map.put(scaledStat.statHash(), statObject);
						}
					}
					return map;
				}
			}
		} else if (investmentStats != null) {
			if (investmentStats.length > 0) {
				HashMap<Long, BaseStatObject> map = new HashMap<>();
				
				for (InvestmentStat stat : investmentStats) {
					StatEntryObject object = new StatEntryObject();
					object.value = stat.value;
					object.statHash = stat.statTypeHash;
					object.oldValue = stat.oldValue;
					
					if (stats != null && stats.stats != null && stats.stats.size() > 0) {
						for (StatEntryObject statEntry : stats.stats.values()) {
							if (statEntry.value == 0 && statEntry.maximum == 0 && statEntry.minimum == 0) continue;
							
							if (stat.statTypeHash.equals(statEntry.statHash)) {
								if (statEntry.minimum != 0) object.minimum = statEntry.minimum;
								if (statEntry.maximum != 0) object.maximum = statEntry.maximum;
								
								break;
							}
						}
					}
					
					map.put(stat.statTypeHash, object);
				}
				
				return map;
			}
		}
		
		if (stats != null && stats.stats != null && stats.stats.size() > 0) {
			return new HashMap<>(stats.stats);
		}
		
		return new HashMap<>();
	}
	
	@Override
	public HashMap<String, String> getInfo()
	{
		return infoValues;
	}
	
	
	@Override
	public HashMap<Integer, ArrayList<SocketObject>> getPerks()
	{
		return perkMap;
	}
}