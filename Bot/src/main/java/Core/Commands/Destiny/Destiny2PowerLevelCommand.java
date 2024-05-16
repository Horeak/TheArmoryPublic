package Core.Commands.Destiny;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.ComponentSystem.ComponentResponseSystem;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.CommandSystem.SlashMessageBuilder;
import Core.Commands.Destiny.Models.DestinyBasicModels.Destiny2ItemBucket;
import Core.Commands.Destiny.Models.DestinyItemObject;
import Core.Commands.Destiny.system.DestinyItemSystem;
import Core.Commands.Destiny.Models.DestinySeasonObject;
import Core.Commands.Destiny.User.Destiny2UserUtil;
import Core.Commands.Destiny.system.DestinySeasonSystem;
import Core.Main.Logging;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.BotChannel;
import Core.Objects.CustomEntry;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.Utils;
import com.google.common.base.Strings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.JTextArea;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

@Command
public class Destiny2PowerLevelCommand implements ISlashCommand
{
	private void membershipSelected(Guild guild, BotChannel channel, User author, JSONObject profileObject)
	{
		ArrayList<ItemComponent> btns = new ArrayList<>();

		if(profileObject.has("characters")) {
			JSONObject charObject = profileObject.getJSONObject("characters");

			if (charObject.has("data")) {
				JSONObject data = charObject.getJSONObject("data");

				if(data.keySet().size() > 1) {
					for (String key : data.keySet()) {
						Object ob1 = data.get(key);

						if (ob1 instanceof JSONObject character) {
							String race = getRace(character);
							String clas = getClass(character);

							int classType = character.getInt("classType");

							btns.add(ComponentResponseSystem.addComponent(author, Button.secondary("id", race + " " + clas), (event) -> {
								EmbedBuilder builder = new EmbedBuilder();
								builder.setTitle("Stats for " + race + " " + clas);
								builder.setImage("attachment://items.png");
								builder.setDescription("Here is an overview of your max equippable power level.");

								HashMap<Long, JSONObject> items = new HashMap<>();
								HashMap<Long, CustomEntry<Integer, JSONObject>> powerItems = new HashMap<>();

								if(profileObject.has("itemComponents")){
									JSONObject tempObject = profileObject.getJSONObject("itemComponents");

									if (tempObject.has("instances")) {
										JSONObject instances = tempObject.getJSONObject("instances");

										if (instances.has("data")) {
											JSONObject itemsData = instances.getJSONObject("data");

											for (String itemKey : itemsData.keySet()) {
												if(Utils.isLong(itemKey)){
													items.put(Long.parseLong(itemKey), itemsData.getJSONObject(itemKey));
												}
											}
										}
									}
								}

								String charName = race + " " + clas;


								//Check items from select class first
								if(profileObject.has("characterEquipment")){
									JSONObject tempObject = profileObject.getJSONObject("characterEquipment");
									JSONObject dat = tempObject.getJSONObject("data");

									if(dat.has(key)){
										checkItemObject(profileObject, Long.valueOf(key), charName, classType, items, powerItems, dat.getJSONObject(key), "characterEquipment");
									}
								}

								//Check items from select class first
								if(profileObject.has("characterInventories")){
									JSONObject tempObject = profileObject.getJSONObject("characterInventories");
									JSONObject dat = tempObject.getJSONObject("data");

									if(dat.has(key)){
										checkItemObject(profileObject, Long.valueOf(key), charName, classType, items, powerItems, dat.getJSONObject(key), "characterInventories");
									}
								}

								//Then check items from vault second
								if(profileObject.has("profileInventory")){
									JSONObject tempObject = profileObject.getJSONObject("profileInventory");

									if (tempObject.has("data")) {
										JSONObject itemsData = tempObject.getJSONObject("data");
										checkItemObject(profileObject, Long.valueOf(key), charName, classType, items, powerItems, itemsData, "profileInventory");
									}
								}

								//Then check items in the inventory of other classes
								if(profileObject.has("characterInventories")){
									JSONObject tempObject = profileObject.getJSONObject("characterInventories");
									checkItems(profileObject, data, classType, items, powerItems, tempObject, "characterInventories");
								}

								//And finaly check equipped items from all classses
								if(profileObject.has("characterEquipment")){
									JSONObject tempObject = profileObject.getJSONObject("characterEquipment");
									checkItems(profileObject, data, classType, items, powerItems, tempObject, "characterEquipment");
								}


								TreeMap<Integer, CustomEntry<Integer, JSONObject>> map = new TreeMap<>();

								//Sort the map based on the bucket index
								for(Entry<Long, CustomEntry<Integer, JSONObject>> ent : powerItems.entrySet()){
									if(DestinyItemSystem.destinyBucketObjects.containsKey(ent.getKey().intValue())){
										Destiny2ItemBucket bucket = DestinyItemSystem.destinyBucketObjects.get(ent.getKey().intValue());
										map.put(bucket.index, ent.getValue());
									}
								}

								int width = 2000;
								int height = 2000;

								BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
								Graphics2D g2 = bufferedImage.createGraphics();

								g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

								String emblemPath = character.getString("emblemBackgroundPath");

								int scale = 1;

								BufferedImage emblem = null;

								try {
									URL url = new URL(Destiny2UserUtil.BASE_BUNGIE_URL + emblemPath);
									emblem = ImageIO.read(url);
									scale = (width / emblem.getWidth());
									g2.drawImage(emblem, 0, 0, width, ((width / emblem.getWidth()) * emblem.getHeight()), null);

								} catch (IOException e) {
									Logging.exception(e);
								}

								Color avgColor = averageColor(emblem, 0, 0, emblem.getWidth() / 2, emblem.getHeight());
								int colorScore = avgColor.getRed() + avgColor.getBlue() + avgColor.getGreen();

								g2.setColor(colorScore > 600 ? Color.black : Color.white);
								g2.setFont(new Font("Uni Sans Heavy", Font.PLAIN, 100));
								g2.drawString(race + " " + clas, scale * 100, scale * 25);

								double power = 0;
								int artifacts = 0;
								int artifactLevel = -1;

								for(Entry<Integer, CustomEntry<Integer, JSONObject>> ent : map.entrySet()){
									if(ent.getValue().getValue().has("bucketHash") && ent.getValue().getValue().getLong("bucketHash") == 1506418338L){
										artifacts++;

										if(ent.getValue().getKey() > artifactLevel){
											artifactLevel = ent.getValue().getKey();
										}

										continue;
									}

									CustomEntry<Integer, JSONObject> val = ent.getValue();
									power += val.getKey();
								}

								power /= (map.size() - artifacts);


								int maxSeasonPower = 0;
								DestinySeasonObject currentSeason = null;

								for(DestinySeasonObject season : DestinySeasonSystem.destinySeasons.values()){
									Date date = season.releaseDate;

									if(date.getTime() > System.currentTimeMillis()) continue;

									if(season.pinnacleCap > maxSeasonPower){
										maxSeasonPower = season.pinnacleCap;
										currentSeason = season;
									}
								}

								if(currentSeason != null){
									int checkPower = (int)power;
									int cap = 0;
									String capN = null;

									if(checkPower < currentSeason.softCap){
										cap = currentSeason.softCap;
										capN = "Soft cap";

									}else if(checkPower < currentSeason.powerfulCap){
										cap = currentSeason.powerfulCap;
										capN = "Powerful cap";

									}else if(checkPower < currentSeason.pinnacleCap){
										cap = currentSeason.pinnacleCap;
										capN = "Pinnacle cap";
									}

									int left = cap - checkPower;

									if(left > 0 && capN != null){
										builder.addField("Power level", "Missing **" + left + "** power to reach the **" + capN + "** of **" + cap + "** power.", false);
									}
								}

								if(power > 0) {
									g2.setColor(colorScore > 400 ? Color.darkGray : Color.lightGray);

									if(power >= maxSeasonPower){
										power = maxSeasonPower;
										g2.setColor(Color.cyan);
									}

									g2.setFont(new Font("Uni Sans Heavy", Font.ITALIC,80));
									g2.drawString("Max Power " + (int)power + (artifactLevel  > -1 ? " + " + artifactLevel : ""), scale * 100, scale * 50);
								}

								double dPower = power;
								double missingPow = (dPower - (int)power);
								int missing = 8 - (int)(missingPow * 8F);

								for(int i = 0; i < 8; i++){
									if(i >= (8 - missing)){
										g2.setColor(Color.darkGray);
									}else{
										g2.setColor(Color.white);
									}

									g2.fillRect(scale * 100 + (i * 110), scale * 50 + 100, 90, 40);
								}

								g2.setColor(colorScore > 400 ? Color.darkGray : Color.lightGray);
								g2.setFont(new Font("Uni Sans Heavy", Font.BOLD,80));

								int nextPower = (int)(power + 1);

								if(nextPower >= maxSeasonPower){
									nextPower = maxSeasonPower;
									g2.setColor(Color.cyan);
								}

								g2.drawString(String.valueOf(nextPower), scale * 100 + (8 * 110) + 25, scale * 50 + 145);

								Color avgColor1 = averageColor(emblem, 0, 0, emblem.getWidth() / 2, emblem.getHeight()).darker();
								Color avgColor2 = averageColor(emblem, emblem.getWidth() / 2, 0, emblem.getWidth() / 4, emblem.getHeight()).brighter();

								int x2 = width / 2;
								int y2 = ((width / emblem.getWidth()) * emblem.getHeight()) + (height - ((width / emblem.getWidth()) * emblem.getHeight())) / 2;

								GradientPaint gradient = new GradientPaint(0, (((float)width / emblem.getWidth()) * emblem.getHeight()), avgColor1, x2, y2, avgColor2, true);
								g2.setPaint(gradient);
								g2.fillRect(0, ((width / emblem.getWidth()) * emblem.getHeight()), width, height -  ((width / emblem.getWidth()) * emblem.getHeight()));

								int i = 0;
								for(Entry<Integer, CustomEntry<Integer, JSONObject>> ent : map.entrySet()){
									boolean isArtifact = ent.getValue().getValue().has("bucketHash") && ent.getValue().getValue().getLong("bucketHash") == 1506418338L;

									CustomEntry<Integer, JSONObject> val = ent.getValue();
									long itemHash = val.getValue().getLong("itemHash");
									DestinyItemObject item = DestinyItemSystem.destinyItemObjects.get((int)itemHash);

									if(item.displayProperties != null && item.displayProperties.hasIcon) {
										try {
											URL url = new URL(Destiny2UserUtil.BASE_BUNGIE_URL + item.displayProperties.icon);
											BufferedImage image = ImageIO.read(url);

											int size = (height - (emblem.getHeight() * scale) - (100)) / 6;
											int widthSize = 310;
											int spacing = 60;
											int startHeight = spacing + ((width / emblem.getWidth()) * emblem.getHeight());

											int iconScale = size / image.getWidth();

											int x = i >= 3 ? ((width - (int)(spacing * 1.5)) - (widthSize * 3)) : 0;
											int y = ((i >= 3 ? i - 3 : i) * (size + spacing));

											//If the item is the artifact, align it to the bottom left
											if(isArtifact){
												x = 0;
												y = 4 * (size + spacing);
											}

											g2.setColor(new Color(64, 64, 64, 128));
											g2.fillRect(spacing + x - 20, startHeight + y - 20, widthSize * 3 + 10, size + 40);

											for (int s = 10; s >= 0; s--) {
												g2.setColor(new Color(128, 128, 128, (100 + (s * 15))));
												g2.drawRect(spacing + x - s, startHeight + y - s, (widthSize * 3 - 32) + (s * 2), size + (s * 2));
											}

											g2.drawImage(image, spacing + x, startHeight + y, size, size, null);

											if(item.iconWatermark != null){
												URL url1 = new URL(Destiny2UserUtil.BASE_BUNGIE_URL + item.iconWatermark);
												BufferedImage image1 = ImageIO.read(url1);
												g2.drawImage(image1, spacing + x, startHeight + y, image1.getWidth() * iconScale, image1.getHeight() * iconScale, null);
											}


											if(val.getValue().has("masterworked")){
												for (int s = 10; s >= 0; s--) {
													g2.setColor(new Color(255, 200, 0, (100 + (s * 15))));

													g2.drawRect(spacing + x - s, startHeight + y - s, size + (s * 2),
													            size + (s * 2));
												}
											}else {
												for (int s = 10; s >= 0; s--) {
													g2.setColor(new Color(255, 255, 255, (100 + (s * 15))));

													g2.drawRect(spacing + x - s, startHeight + y - s, size + (s * 2), size + (s * 2));
												}
											}

											g2.setColor(Color.black);


											Font f = new Font("Uni Sans Heavy", Font.BOLD,50);
											Map<TextAttribute, Integer> fontAttributes = new HashMap<>();
											fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
											g2.setFont(f.deriveFont(fontAttributes));

											int xs = spacing + x + size + 20;
											int ys = startHeight + y;

											int xsSize = widthSize * 3 - 32;
											
											String itemName = item.displayProperties.name;

											JTextArea ta = new JTextArea(itemName);
											ta.setLineWrap(true);
											ta.setWrapStyleWord(true);
											ta.setBounds(xs, ys, xsSize - 20, size);
											ta.setForeground(Color.white);
											ta.setBackground(new Color(0,0,0,0));
											ta.setFont(g2.getFont());

											Graphics g3 = g2.create(xs, ys, xsSize, size);
											ta.paint(g3);

											if(val.getKey() >= maxSeasonPower){
												g2.setColor(Color.cyan);
												ta.setForeground(g2.getColor());
											}

											ta.setText(Strings.repeat("\n", (itemName.length() / 30) + 1) +  "Power " + (isArtifact ? "bonus " : "") + val.getKey());
											ta.setFont(new Font("Uni Sans Heavy", Font.ITALIC,50));
											ta.paint(g3);

											g2.setColor(Color.white);
											ta.setForeground(g2.getColor());

											if(!isArtifact) {
												if (val.getValue().has("item_location")) {
													g3 = g2.create(xs, ys + (size - 50), xsSize, 50);
													ta.setText(val.getValue().getString("item_location"));
													ta.setFont(new Font("Uni Sans Heavy", Font.ITALIC, 40));
													ta.paint(g3);
												}
											}

										} catch (IOException e) {
											Logging.exception(e);
										}
									}

									i++;
								}

								g2.dispose();

								File file = new File("items.png");

								try {
									ImageIO.write(bufferedImage, "png", file);
								} catch (IOException e) {
									Logging.exception(e);
								}


								ChatUtils.setEmbedColor(guild, author, builder);

								SlashMessageBuilder slashBuilder = ChatUtils.createSlashMessage(author, channel);
								slashBuilder.withEmbed(builder);
								slashBuilder.addAction((ComponentResponseSystem.addComponent(author, Button.success("id", "Transfer items"), (e) -> {
									top:
									for(CustomEntry<Integer,JSONObject> ent : map.values()){
										JSONObject itemObject = ent.getValue();

										int membershipType = character.getInt("membershipType");
										Long characterId = character.getLong("characterId");

										if(itemObject.has("item_location")){
											String location = itemObject.getString("item_location");

											if(location.contains("Equipped")) {
												if(itemObject.has("location_id") && itemObject.has("itemInstanceId")){
													Long id = itemObject.getLong("location_id");
													if(id.equals(characterId) || id.toString().equals(key)) continue;

													Long bucketHash = itemObject.getLong("bucketHash");
													long refId = itemObject.getLong("itemInstanceId");

													if(profileObject.has("characterInventories")) {
														JSONObject tempObject = profileObject.getJSONObject("characterInventories");
														JSONObject dat = tempObject.getJSONObject("data");

														if (dat.has(id.toString())) {
															JSONObject characterItems = dat.getJSONObject(id.toString());
															JSONArray array = characterItems.getJSONArray("items");

															for (Object ob : array) {
																JSONObject item = (JSONObject)ob;

																if (item.has("bucketHash") && item.has("itemInstanceId")) {
																	Long itemBucket = item.getLong("bucketHash");

																	if (itemBucket.equals(bucketHash)) {
																		if (refId != item.getLong("itemInstanceId")) {
																			JSONObject dataPacket = new JSONObject();

																			dataPacket.put("membershipType", membershipType);
																			dataPacket.put("itemId", item.getLong("itemInstanceId"));
																			dataPacket.put("characterId", id);

																			JSONObject response = Destiny2UserUtil.postData(
																					Destiny2UserUtil.getAccessToken(author),
																					"/Platform//Destiny2/Actions/Items/EquipItem/",
																					dataPacket);

																			long itemHash = itemObject.getLong("itemHash");
																			DestinyItemObject itemObj = DestinyItemSystem.destinyItemObjects.get((int)itemHash);

																			if(response != null) {
																				if (response.has("ErrorCode")) {
																					int errCode = response.getInt("ErrorCode");

																					if(errCode == 1){
																						continue top;
																					}
																				}
																			}
																		}
																	}
																}
															}
														}
													}
												}
											}
										}
									}

									int transfered = 0;
									for(CustomEntry<Integer,JSONObject> ent : map.values()){
										boolean success = true;

										JSONObject itemObject = ent.getValue();

										int membershipType = character.getInt("membershipType");
										Long characterId = character.getLong("characterId");

										JSONObject dataPacket = new JSONObject();

										dataPacket.put("membershipType", membershipType);

										boolean isVault = false;

										if(itemObject.has("location_id")){
											Long id = itemObject.getLong("location_id");
											if(id.equals(characterId)){
												transfered++;
												continue;
											}

											dataPacket.put("characterId", id);
										}

										if(itemObject.has("item_location")){
											String location = itemObject.getString("item_location");

											if(location.contains("Vault")) isVault = true;
										}

										dataPacket.put("itemId", itemObject.getLong("itemInstanceId"));
										dataPacket.put("itemReferenceHash", itemObject.getLong("itemHash"));

										dataPacket.put("stackSize", 1);
										dataPacket.put("transferToVault", !isVault);

										if(isVault){
											dataPacket.put("characterId", characterId);
										}

										JSONObject response = Destiny2UserUtil.postData(Destiny2UserUtil.getAccessToken(author), "/Platform/Destiny2/Actions/Items/TransferItem/", dataPacket);

										if(response != null) {
											if (response.has("ErrorCode")) {
												int errCode = response.getInt("ErrorCode");
												String errorStatus = response.getString("ErrorStatus");

												if (errorStatus.equalsIgnoreCase("DestinyCharacterNotLoggedIn"))
													continue;

												if (errCode == 1) {
													if ((!isVault)) {
														dataPacket.put("transferToVault", false);
														dataPacket.put("characterId", characterId);

														response = Destiny2UserUtil.postData(Destiny2UserUtil.getAccessToken(author),
														                                     "/Platform/Destiny2/Actions/Items/TransferItem/",
														                                     dataPacket);

														if (response != null) {
															if (response.has("ErrorCode")) {
																errCode = response.getInt("ErrorCode");
																errorStatus = response.getString("ErrorStatus");

																if (errorStatus.equalsIgnoreCase(
																		"DestinyCharacterNotLoggedIn")) continue;

																if (errCode != 1) {
																	success = false;
																}
															}
														}
													}
												} else {
													success = false;
												}
											}
										}

										if(success){
											transfered++;
										}
									}

									if(transfered == map.size()){
										ChatUtils.sendEmbed(channel, "All items have been transferred to " + charName);
									}else if(transfered > 0){
										ChatUtils.sendEmbed(channel, "Some items failed to transfer to " + charName);
									}else{
										ChatUtils.sendEmbed(channel, "Unable to transfer any items, please try again later.");
									}

								})));

								slashBuilder.withAttachment(file, "items.png");
								slashBuilder.send();
							}));
						}
					}
				}

			}
		}

		SlashMessageBuilder slashBuilder = ChatUtils.createSlashMessage(author, channel);
		slashBuilder.withEmbed(ChatUtils.makeEmbed(author, guild, channel, "Select which character to view."));
		slashBuilder.setMultiRow();
		slashBuilder.withActions(btns);
		slashBuilder.send();
	}

	public static Color averageColor(BufferedImage bi, int x0, int y0, int w,
			int h) {
		int x1 = x0 + w;
		int y1 = y0 + h;
		long sumr = 0, sumg = 0, sumb = 0;
		for (int x = x0; x < x1; x++) {
			for (int y = y0; y < y1; y++) {
				Color pixel = new Color(bi.getRGB(x, y));
				sumr += pixel.getRed();
				sumg += pixel.getGreen();
				sumb += pixel.getBlue();
			}
		}
		int num = w * h;
		float red = (float)sumr / num;
		float green = (float)sumg / num;
		float blue = (float)sumb / num;

		return new Color((int)red, (int)green, (int)blue);
	}

	private void checkItems(JSONObject profileObject, JSONObject characters, int classType, HashMap<Long, JSONObject> items, HashMap<Long, CustomEntry<Integer, JSONObject>> powerItems,JSONObject tempObject, String type)
	{
		if (tempObject.has("data")) {
			JSONObject itemsData = tempObject.getJSONObject("data");
			for (String charId : itemsData.keySet()) {
				JSONObject charD = itemsData.getJSONObject(charId);

				JSONObject character = characters.getJSONObject(charId);

				String race = getRace(character);
				String clas = getClass(character);
				String charName = race + " " + clas;

				checkItemObject(profileObject, Long.parseLong(charId), charName, classType, items, powerItems, charD, type);
			}
		}
	}

	private void checkItemObject(JSONObject profile, Long charId, String charName, int classType, HashMap<Long, JSONObject> items, HashMap<Long, CustomEntry<Integer, JSONObject>> powerItems, JSONObject charD, String type)
	{
		if (charD.has("items")) {
			JSONArray array = charD.getJSONArray("items");

			for (Object itemOb : array) {
				JSONObject itemObject = (JSONObject)itemOb;

				if (itemObject.has("itemInstanceId")) {
					Long id = itemObject.getLong("itemInstanceId");
					long itemHash = itemObject.getLong("itemHash");

					if (items.containsKey(id)) {
						JSONObject instanceObject = items.get(id);

						if (instanceObject != null && instanceObject.has(
								"primaryStat")) {
							JSONObject stat = instanceObject.getJSONObject(
									"primaryStat");

							//This is the stat has for POWER LEVEL
							if (stat.has("statHash")){
								long statHash = stat.getLong("statHash");

								if(statHash == 1480404414L || statHash == 1935470627L || statHash == 3897883278L || statHash == 3289069874L){
									int value = stat.getInt("value");

									DestinyItemObject itemObject1 = DestinyItemSystem.destinyItemObjects.get((int)itemHash);

									if(itemObject1.inventory == null) continue;

									Long bucketHash = itemObject1.inventory.bucketTypeHash();

									if(itemObject1.itemSubType == 0 && bucketHash != 1506418338L) continue;

									if (itemObject != null) {
										if (itemObject1.classType == 3 || itemObject1.classType == classType) {
											
											switch (type) {
												case "profileInventory" -> itemObject.put("item_location", "Located in Vault");
												case "characterEquipment" -> {
													itemObject.put("item_location", "Equipped on " + charName);
													itemObject.put("location_id", charId);
												}
												case "characterInventories" -> {
													itemObject.put("item_location", "Located on " + charName);
													itemObject.put("location_id", charId);
												}
											}

											if(profile.has("itemComponents")){
												JSONObject plugs = profile.getJSONObject("itemComponents");
												JSONObject data = plugs.getJSONObject("sockets");
												JSONObject plug = data.getJSONObject("data");

												if(plug.has(id.toString())){
													JSONObject plugInfo = plug.getJSONObject(id.toString());
													JSONArray plugsArray = plugInfo.getJSONArray("sockets");

													for(Object sock : plugsArray){
														JSONObject socket = (JSONObject)sock;

														if(socket.has("plugHash")){
															long plugHash = socket.getLong("plugHash");
															DestinyItemObject obj = DestinyItemSystem.destinyItemObjects.get((int)plugHash);

															if(obj != null && obj.displayProperties != null){
																if(obj.investmentStats != null && obj.investmentStats.length > 0 && obj.investmentStats[0].value < 10) continue;
																if(obj.plug.plugCategoryIdentifier.contains("empty")) continue;

																if(obj.displayProperties.name.equalsIgnoreCase("masterwork") || obj.plug.plugCategoryIdentifier.contains("masterworks.stat") || obj.plug.plugCategoryIdentifier.endsWith("masterwork")){
																	itemObject.put("masterworked", true);
																}
															}
														}
													}
												}
											}

											if (powerItems.containsKey(
													bucketHash)) {
												CustomEntry<Integer, JSONObject> powerObject = powerItems.get(
														bucketHash);

												if (value > powerObject.getKey()) {
													powerItems.put(bucketHash, new CustomEntry<>(
															value, itemObject));
												}
											} else {
												powerItems.put(bucketHash, new CustomEntry<>(
														value, itemObject));
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@Nullable
	private String getGender(JSONObject character)
	{
		if (character.has("genderType")) {
			int genderType = character.getInt("genderType");
			return genderType == 0 ? "Male" : "Female";
		}
		return null;
	}

	@Nullable
	private String getRace(JSONObject character)
	{
		if (character.has("raceType")) {
			int racen = character.getInt("raceType");

			switch (racen) {
				case 0:
					return "Human";

				case 1:
					return "Awoken";

				case 2:
					return "Exo";
			}
		}
		return null;
	}

	@Nullable
	private String getClass(JSONObject character)
	{
		if (character.has("classType")) {
			int classType = character.getInt("classType");

			switch (classType) {
				case 0:
					return "Titan";

				case 1:
					return "Hunter";

				case 2:
					return "Warlock";
			}
		}
		return null;
	}

	@Override
	public String getDescription()
	{
		return "Shows current character power level";
	}

	@Override
	public String commandName()
	{
		return "destiny-powerlevel";
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		JSONObject object = Destiny2UserUtil.getData(Destiny2UserUtil.getAccessToken(slashEvent.getUser()), Destiny2UserUtil.GET_MEMBERSHIP_PATH);

		if (object == null) {
			ChatUtils.sendEmbed(channel, slashEvent.getUser().getAsMention() + " I didn't find a registered user for you! Please use the destiny register command and then try again!");
			return;
		}

		ArrayList<JSONObject> memberships = new ArrayList<>();

		if (object != null) {
			if (object.has("destinyMemberships")) {
				JSONArray array = object.getJSONArray("destinyMemberships");

				for (Object ob1 : array) {
					if (ob1 instanceof JSONObject arrayObject) {
						
						if (arrayObject.has("crossSaveOverride") && arrayObject.getInt("crossSaveOverride") != 0) {
							if (arrayObject.getInt("crossSaveOverride") == arrayObject.getInt("membershipType")) {
								memberships.add(arrayObject);
							}
						} else {
							memberships.add(arrayObject);
						}
					}
				}
			}
		}


		String components = "?components=Characters,ProfileInventories,CharacterInventories,CharacterEquipment,ItemInstances,ItemSockets";

		if(memberships.size() > 0) {
			if (memberships.size() > 1) {
				ArrayList<ItemComponent> btns = new ArrayList<>();

				for (JSONObject object1 : memberships) {
					String membershipId = object1.getString("membershipId");
					int membershipType = object1.getInt("membershipType");
					String displayName = object1.getString("displayName");

					btns.add(ComponentResponseSystem.addComponent(slashEvent.getUser(), Button.secondary("id", displayName), (event1 -> {
						JSONObject profileObject = Destiny2UserUtil.getData(Destiny2UserUtil.getAccessToken(slashEvent.getUser()),
						                                                    "/Platform/Destiny2/" + membershipType + "/Profile/" + membershipId + "/" + components);
						membershipSelected(slashEvent.getGuild(), channel, slashEvent.getUser(), profileObject);
					})));
				}

				SlashMessageBuilder builder = ChatUtils.createSlashMessage(slashEvent.getUser(), channel);
				builder.withEmbed(ChatUtils.makeEmbed(slashEvent.getUser(), slashEvent.getGuild(), channel, "Select which account to view"));
				builder.withActions(btns);
				builder.send();

			} else {
				JSONObject object1 = memberships.get(0);
				String membershipId = object1.getString("membershipId");
				int membershipType = object1.getInt("membershipType");

				JSONObject profileObject = Destiny2UserUtil.getData(Destiny2UserUtil.getAccessToken(slashEvent.getUser()),
				                                                    "/Platform/Destiny2/" + membershipType + "/Profile/" + membershipId + "/" + components);
				membershipSelected(slashEvent.getGuild(), channel, slashEvent.getUser(), profileObject);
			}
		}
	}
}