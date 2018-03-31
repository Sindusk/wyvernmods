package mod.sin.wyvern;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;
import org.nyxcode.wurm.discordrelay.DiscordRelay;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.modifiers.DoubleValueModifier;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.webinterface.WcKingdomChat;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.Enchants;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import mod.sin.creatures.Avenger;
import mod.sin.creatures.Charger;
import mod.sin.creatures.SpiritTroll;
import mod.sin.creatures.WyvernBlack;
import mod.sin.creatures.WyvernGreen;
import mod.sin.creatures.WyvernRed;
import mod.sin.creatures.WyvernWhite;
import mod.sin.items.SealedMap;
import mod.sin.lib.Util;
import mod.sin.wyvern.arena.Arena;
import mod.sin.wyvern.bestiary.MethodsBestiary;

public class MiscChanges {
	public static Logger logger = Logger.getLogger(MiscChanges.class.getName());
	
	public static byte newCreatureType(int templateid, byte ctype) throws Exception{
		CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(templateid);
		if(ctype == 0 && (template.isAggHuman() || template.getBaseCombatRating() > 10) && !template.isUnique() && !Arena.isTitan(templateid)){
			if(Server.rand.nextInt(5) == 0){
				ctype = (byte) (Server.rand.nextInt(11)+1);
				if(Server.rand.nextInt(50) == 0){
					ctype = 99;
				}
			}
		}
		return ctype;
	}
	
	protected static HashMap<Creature, Double> defDamage = new HashMap<Creature, Double>();
	public static void setDefDamage(Creature cret, double damage){
		defDamage.put(cret, damage);
	}
	public static double getDefDamage(Creature cret){
		if(defDamage.containsKey(cret)){
			double theDamage = defDamage.get(cret);
			defDamage.remove(cret);
			return theDamage;
		}
		logger.severe("Severe error: could not find defDamage for creature "+cret);
		return 0d;
	}
	
	public static void sendRumour(Creature creature){
		DiscordRelay.sendToDiscord("rumors", "Rumours of " + creature.getName() + " are starting to spread.");
	}
	
	public static int getNewVillageTiles(int tiles){
		float power = 2f;
		float changeRate = 1000;
		float maxNumTiles = 50000;
		float tilesFloat = tiles;
		// =(C2) * (1-POW(C2/$C$16, $A$24)) + (SQRT(C2)*$A$26) * POW(C2/$C$16, $A$24)
		int newTiles = (int) (tilesFloat * (1-Math.pow(tilesFloat/maxNumTiles, power)) + (Math.sqrt(tilesFloat)*changeRate) * Math.pow(tilesFloat/maxNumTiles, power));
		return newTiles;
	}
	
	private static final float PRICE_MARKUP = 1f/1.4f;
	public static int getNewValue(Item item){
		if(item.getTemplateId() == SealedMap.templateId){
			float qual = item.getQualityLevel();
			float dam = item.getDamage();
			// =($A$25*A2*A2 / 10000)
			float initialValue = ((float)item.getTemplate().getValue())*qual*qual/10000f;
			float baseCost = 100000f;
			float power = 11.0f;
			// =((10+B2/4.5)*(1-POW(A2/100, $A$27)) + B2*POW(A2/100, $A$27)) * ((100 - $A$29) / 100)
			int newValue = (int) (((baseCost+(initialValue/4.5f)) * (1f-Math.pow(qual/100f, power)) + initialValue*Math.pow(qual/100f, power)) * ((100f-dam)/100f) * PRICE_MARKUP);
			return newValue;
		}
		return -10;
	}
	
	public static void checkEnchantedBreed(Creature creature){
		int tile = Server.surfaceMesh.getTile(creature.getTileX(), creature.getTileY());
        byte type = Tiles.decodeType((int)tile);
        if (type == Tiles.Tile.TILE_ENCHANTED_GRASS.id){
        	logger.info("Creature "+creature.getName()+" was born on enchanted grass, and has a negative trait removed!");
        	Server.getInstance().broadCastAction(creature.getName()+" was born on enchanted grass, and feels more healthy!", creature, 10);
        	creature.removeRandomNegativeTrait();
        }
	}
	
	public static boolean hasCustomCorpseSize(Creature creature){
		int templateId = creature.getTemplate().getTemplateId();
		if(templateId == Avenger.templateId){
			return true;
		}else if(Arena.isTitan(creature)){
			return true;
		}
		return false;
	}
	
	public static boolean insertItemIntoVehicle(Item item, Item vehicle, Creature performer) {
        // If can put into crates, try that
        if (item.getTemplate().isBulk() && item.getRarity() == 0) {
            for (Item container : vehicle.getAllItems(true)) {
                if(container.getTemplateId() == ItemList.bulkContainer){
                    if(container.getFreeVolume() >= item.getVolume()){
                        if (item.AddBulkItem(performer, container)) {
                            performer.getCommunicator().sendNormalServerMessage(String.format("You put the %s in the %s in your %s.", item.getName(), container.getName(), vehicle.getName()));
                            return true;
                        }
                    }
                }
                if (container.isCrate() && container.canAddToCrate(item)) {
                    if (item.AddBulkItemToCrate(performer, container)) {
                        performer.getCommunicator().sendNormalServerMessage(String.format("You put the %s in the %s in your %s.", item.getName(), container.getName(), vehicle.getName()));
                        return true;
                    }
                }
            }
        }
        // No empty crates or disabled, try the vehicle itself
        if (vehicle.getNumItemsNotCoins() < 100 && vehicle.getFreeVolume() >= item.getVolume() && vehicle.insertItem(item)) {
            performer.getCommunicator().sendNormalServerMessage(String.format("You put the %s in the %s.", item.getName(), vehicle.getName()));
            return true;
        } else {
            // Send message if the vehicle is too full
            performer.getCommunicator().sendNormalServerMessage(String.format("The %s is too full to hold the %s.", vehicle.getName(), item.getName()));
            return false;
        }
    }
    public static Item getVehicleSafe(Creature pilot) {
        try {
            if (pilot.getVehicle() != -10)
                return Items.getItem(pilot.getVehicle());
        } catch (NoSuchItemException ignored) {
        }
        return null;
    }
 
    public static void miningHook(Creature performer, Item ore){
        Item vehicleItem = getVehicleSafe(performer);
        if(vehicleItem != null && vehicleItem.isHollow()){
            if(insertItemIntoVehicle(ore, vehicleItem, performer)){
                return;
            }
        }
 
        // Last resort, if no suitable vehicle is found.
        try {
            ore.putItemInfrontof(performer);
        } catch (NoSuchCreatureException | NoSuchItemException | NoSuchPlayerException | NoSuchZoneException e) {
            e.printStackTrace();
        }
    }
	
	public static void setCorpseSizes(Creature creature, Item corpse){
		if(corpse.getTemplateId() != ItemList.corpse){
			return;
		}
		int templateId = creature.getTemplate().getTemplateId();
		boolean sendStatus = false;
		int size = 50000;
		if(templateId == Avenger.templateId){
			size *= 1.2;
			corpse.setSizes(size);
			sendStatus = true;
		}else if(Arena.isTitan(creature)){
			size *= 1.5;
			corpse.setSizes(size);
			sendStatus = true;
		}else{
			corpse.setSizes((int)((float)(corpse.getSizeX() * (creature.getSizeModX() & 255)) / 64.0f), (int)((float)(corpse.getSizeY() * (creature.getSizeModY() & 255)) / 64.0f), (int)((float)(corpse.getSizeZ() * (creature.getSizeModZ() & 255)) / 64.0f));
		}
		if(sendStatus){
			try {
				Zone zone = Zones.getZone((int)corpse.getPosX() >> 2, (int)corpse.getPosY() >> 2, corpse.isOnSurface());
				zone.removeItem(corpse, true, true);
	            zone.addItem(corpse, true, false, false);
			} catch (NoSuchZoneException e) {
				e.printStackTrace();
			}
		}
		return;
	}
	
	public static void setNewMoveLimits(Creature cret){
        try {
            Skill strength = cret.getSkills().getSkill(102);
            ReflectionUtil.setPrivateField(cret, ReflectionUtil.getField(cret.getClass(), "moveslow"), strength.getKnowledge()*4000);
            ReflectionUtil.setPrivateField(cret, ReflectionUtil.getField(cret.getClass(), "encumbered"), strength.getKnowledge()*7000);
            ReflectionUtil.setPrivateField(cret, ReflectionUtil.getField(cret.getClass(), "cantmove"), strength.getKnowledge()*14000);
            MovementScheme moveScheme = cret.getMovementScheme();
            DoubleValueModifier stealthMod = ReflectionUtil.getPrivateField(moveScheme, ReflectionUtil.getField(moveScheme.getClass(), "stealthMod"));
            if (stealthMod == null) {
                stealthMod = new DoubleValueModifier((- 80.0 - Math.min(79.0, cret.getBodyControl())) / 100.0);
            } else {
                stealthMod.setModifier((- 80.0 - Math.min(79.0, cret.getBodyControl())) / 100.0);
            }
            ReflectionUtil.setPrivateField(moveScheme, ReflectionUtil.getField(moveScheme.getClass(), "stealthMod"), stealthMod);
        }
        catch (NoSuchSkillException | IllegalArgumentException | IllegalAccessException | ClassCastException | NoSuchFieldException nss) {
            logger.log(Level.WARNING, "No strength skill for " + cret, (Throwable)((Object)nss));
        }
	}
	
	public static boolean shouldBreedName(Creature creature){
		if(creature.getTemplate().getTemplateId() == WyvernBlack.templateId){
			return true;
		}else if(creature.getTemplate().getTemplateId() == WyvernGreen.templateId){
			return true;
		}else if(creature.getTemplate().getTemplateId() == WyvernRed.templateId){
			return true;
		}else if(creature.getTemplate().getTemplateId() == WyvernWhite.templateId){
			return true;
		}else if(creature.getTemplate().getTemplateId() == Charger.templateId){
			return true;
		}
		return creature.isHorse();
	}
	
	public static boolean isGhostCorpse(Creature creature){
		if(creature.getTemplate().getTemplateId() == Avenger.templateId){
			return true;
		}else if(creature.getTemplate().getTemplateId() == SpiritTroll.templateId){
			return true;
		}
		return false;
	}
	
	public static void doLifeTransfer(Creature creature, Item attWeapon, double defdamage, float armourMod){
		Wound[] w;
		if (attWeapon.getSpellLifeTransferModifier() > 0.0f && defdamage * (double)armourMod * (double)attWeapon.getSpellLifeTransferModifier() / (double)(creature.isChampion() ? 1000.0f : 500.0f) > 500.0 && creature.getBody() != null && creature.getBody().getWounds() != null && (w = creature.getBody().getWounds().getWounds()).length > 0) {
            w[0].modifySeverity(- (int)(defdamage * (double)attWeapon.getSpellLifeTransferModifier() / (double)(creature.isChampion() ? 1000.0f : (creature.getCultist() != null && creature.getCultist().healsFaster() ? 250.0f : 500.0f))));
        }
	}
	
	public static int getWeaponType(Item weapon){
		if(weapon.enchantment == Enchants.ACID_DAM){
			return Wound.TYPE_ACID;
		}else if(weapon.enchantment == Enchants.FROST_DAM){
			return Wound.TYPE_COLD;
		}else if(weapon.enchantment == Enchants.FIRE_DAM){
			return Wound.TYPE_BURN;
		}
		return -1;
	}
	
	public static void sendServerTabMessage(final String message, final int red, final int green, final int blue){
		DiscordRelay.sendToDiscord("event", message);
		Runnable r = new Runnable(){
        	public void run(){
		        com.wurmonline.server.Message mess;
		        for(Player rec : Players.getInstance().getPlayers()){
		        	mess = new com.wurmonline.server.Message(rec, (byte)16, "Server", message, red, green, blue);
		        	rec.getCommunicator().sendMessage(mess);
		        }
        	}
        };
        r.run();
	}
	
	public static void sendGlobalFreedomChat(final Creature sender, final String message, final int red, final int green, final int blue){
		Runnable r = new Runnable(){
        	public void run(){
		        com.wurmonline.server.Message mess;
		        for(Player rec : Players.getInstance().getPlayers()){
		        	mess = new com.wurmonline.server.Message(sender, (byte)10, "GL-Freedom", "<"+sender.getNameWithoutPrefixes()+"> "+message, red, green, blue);
		        	rec.getCommunicator().sendMessage(mess);
		        }
		        if (message.trim().length() > 1) {
                    WcKingdomChat wc = new WcKingdomChat(WurmId.getNextWCCommandId(), sender.getWurmId(), sender.getNameWithoutPrefixes(), message, false, (byte) 4, red, green, blue);
                    if (!Servers.isThisLoginServer()) {
                        wc.sendToLoginServer();
                    } else {
                        wc.sendFromLoginServer();
                    }
                }
        	}
        };
        r.run();
	}
	public static void sendImportantMessage(Creature sender, String message, int red, int green, int blue){
		sendServerTabMessage("<"+sender.getNameWithoutPrefixes()+"> "+message, red, green, blue);
		sendGlobalFreedomChat(sender, message, red, green, blue);
	}
	
	public static void broadCastDeaths(Creature player, String slayers){
		sendGlobalFreedomChat(player, "slain by "+slayers, 200, 25, 25);
		DiscordRelay.sendToDiscord("deaths", player+" slain by "+slayers);
	}
	
	public static void preInit(){
		try{
			ClassPool classPool = HookManager.getInstance().getClassPool();
			final Class<MiscChanges> thisClass = MiscChanges.class;
			String replace;

			// - Create Server tab with initial messages - //
        	CtClass ctPlayers = classPool.get("com.wurmonline.server.Players");
            CtMethod m = ctPlayers.getDeclaredMethod("sendStartGlobalKingdomChat");
            String infoTabTitle = "Server";
            // Initial messages:
            String[] infoTabLine = {"Server Thread: https://forum.wurmonline.com/index.php?/topic/155981-wyvern-reborn-modded-pve-pvp-3x5x/",
            		"Custom Server Data: https://goo.gl/QRVJyC",
            		"Server Discord: https://discordapp.com/invite/wxEeS7d",
            		"Server Maps: https://www.sarcasuals.com/"};
            String str = "{"
                    + "        com.wurmonline.server.Message mess;";
            for(int i = 0; i < infoTabLine.length; i++){
            	str = str + "        mess = new com.wurmonline.server.Message(player, (byte)16, \"" + infoTabTitle + "\",\"" + infoTabLine[i] + "\", 0, 255, 0);"
            			  + "        player.getCommunicator().sendMessage(mess);";
            }
            str = str + "}";
            m.insertAfter(str);

            // - Enable bridges to be built inside/over/through houses - //
            CtClass ctPlanBridgeChecks = classPool.get("com.wurmonline.server.structures.PlanBridgeChecks");
            replace = "{ return new com.wurmonline.server.structures.PlanBridgeCheckResult(false); }";
            Util.setBodyDeclared(thisClass, ctPlanBridgeChecks, "checkForBuildings", replace);
            /*ctPlanBridgeChecks.getDeclaredMethod("checkForBuildings").setBody("{"
            		+ "  return new com.wurmonline.server.structures.PlanBridgeCheckResult(false);"
            		+ "}");*/

            // - Allow mailboxes and bell towers to be loaded - //
            // [Disabled 10/30 by Sindusk] - Added to ItemMod using reflection instead of editing the method.
            /*CtClass ctItemTemplate = classPool.get("com.wurmonline.server.items.ItemTemplate");
            ctItemTemplate.getDeclaredMethod("isTransportable").setBody("{"
            		+ "  return this.isTransportable || (this.getTemplateId() >= 510 && this.getTemplateId() <= 513) || this.getTemplateId() == 722 || this.getTemplateId() == 670;"
            		+ "}");*/
            
            // - Disable mailboxes from being used while loaded - //
            CtClass ctItem = classPool.get("com.wurmonline.server.items.Item");
            replace = "$_ = $proceed($$);"
            		+ "com.wurmonline.server.items.Item theTarget = com.wurmonline.server.Items.getItem(targetId);"
            		+ "if(theTarget != null && theTarget.getTemplateId() >= 510 && theTarget.getTemplateId() <= 513){"
            		+ "  if(theTarget.getTopParent() != theTarget.getWurmId()){"
            		+ "    mover.getCommunicator().sendNormalServerMessage(\"Mailboxes cannot be used while loaded.\");"
            		+ "    return false;"
            		+ "  }"
            		+ "}";
            Util.instrumentDeclared(thisClass, ctItem, "moveToItem", "getOwnerId", replace);
            /*ctItem.getDeclaredMethod("moveToItem").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("getOwnerId")) {
                        m.replace("$_ = $proceed($$);"
                        		+ "com.wurmonline.server.items.Item theTarget = com.wurmonline.server.Items.getItem(targetId);"
                        		+ "if(theTarget != null && theTarget.getTemplateId() >= 510 && theTarget.getTemplateId() <= 513){"
                        		+ "  if(theTarget.getTopParent() != theTarget.getWurmId()){"
                        		+ "    mover.getCommunicator().sendNormalServerMessage(\"Mailboxes cannot be used while loaded.\");"
                        		+ "    return false;"
                        		+ "  }"
                        		+ "}");
                        return;
                    }
                }
            });*/

            // - Enable creature custom colors - (Used for creating custom color creatures eg. Lilith) - //
            CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");
            Util.setBodyDeclared(thisClass, ctCreature, "hasCustomColor", "{ return true; }");
            //ctCreature.getDeclaredMethod("hasCustomColor").setBody("{ return true; }");

            // - Increase the amount of checks for new unique spawns by 5x - //
            CtClass ctServer = classPool.get("com.wurmonline.server.Server");
            replace = "for(int i = 0; i < 5; i++){"
            		+ "  $_ = $proceed($$);"
            		+ "}";
            Util.instrumentDeclared(thisClass, ctServer, "run", "checkDens", replace);
            /*ctServer.getDeclaredMethod("run").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("checkDens")) {
                        m.replace("for(int i = 0; i < 5; i++){$_ = $proceed($$);}");
                        return;
                    }
                }
            });*/
            
            // - Change rarity odds when a player obtains a rarity window - //
            // [3/27] Removed: Merged to ServerTweaks
            /*CtClass ctPlayer = classPool.get("com.wurmonline.server.players.Player");
            replace = "{ return "+MiscChanges.class.getName()+".newGetPlayerRarity(this); }";
            Util.setBodyDeclared(thisClass, ctPlayer, "getRarity", replace);*/
            //ctPlayer.getDeclaredMethod("getRarity").setBody("{ return mod.sin.wyvern.MiscChanges.newGetPlayerRarity(this); }");
            
            // - Add Facebreyker to the list of spawnable uniques - //
            CtClass ctDens = classPool.get("com.wurmonline.server.zones.Dens");
            replace = "com.wurmonline.server.zones.Dens.checkTemplate(2147483643, whileRunning);";
            Util.insertBeforeDeclared(thisClass, ctDens, "checkDens", replace);
            //ctDens.getDeclaredMethod("checkDens").insertAt(0, "com.wurmonline.server.zones.Dens.checkTemplate(2147483643, whileRunning);");

            // - Announce player titles in the Server tab - //
            CtClass ctPlayer = classPool.get("com.wurmonline.server.players.Player");
            replace = "$_ = $proceed($$);"
            		+ "if(!com.wurmonline.server.Servers.localServer.PVPSERVER){"
            		+ "  "+MiscChanges.class.getName()+".sendServerTabMessage(this.getName()+\" just earned the title of \"+title.getName(this.isNotFemale())+\"!\", 200, 100, 0);"
            		+ "}";
            Util.instrumentDeclared(thisClass, ctPlayer, "addTitle", "sendNormalServerMessage", replace);
            /*ctPlayer.getDeclaredMethod("addTitle").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("sendNormalServerMessage")) {
                        m.replace("$_ = $proceed($$);"
                        		+ "if(!com.wurmonline.server.Servers.localServer.PVPSERVER){"
                        		+ "  mod.sin.wyvern.MiscChanges.sendServerTabMessage(this.getName()+\" just earned the title of \"+title.getName(this.isNotFemale())+\"!\", 200, 100, 0);"
                        		+ "}");
                        return;
                    }
                }
            });*/

            // - Make leather not suck even after it's able to be combined. - //
            CtClass ctMethodsItems = classPool.get("com.wurmonline.server.behaviours.MethodsItems");
            replace = "if(com.wurmonline.server.behaviours.MethodsItems.getImproveTemplateId(target) != 72){"
            		+ "  $_ = $proceed($$);"
            		+ "}else{"
            		+ "  $_ = false;"
            		+ "}";
            Util.instrumentDeclared(thisClass, ctMethodsItems, "improveItem", "isCombine", replace);
            /*ctMethodsItems.getDeclaredMethod("improveItem").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("isCombine")) {
                        m.replace("if(com.wurmonline.server.behaviours.MethodsItems.getImproveTemplateId(target) != 72){"
                        		+ "  $_ = $proceed($$);"
                        		+ "}else{"
                        		+ "  $_ = false;"
                        		+ "}");
                        return;
                    }
                }
            });*/
            
            // - Check new improve materials - //
            replace = "int temp = "+ItemMod.class.getName()+".getModdedImproveTemplateId($1);"
            		+ "if(temp != -10){"
            		+ "  return temp;"
            		+ "}";
            Util.insertBeforeDeclared(thisClass, ctMethodsItems, "getImproveTemplateId", replace);
            /*ctMethodsItems.getDeclaredMethod("getImproveTemplateId").insertBefore(""
            		+ "int temp = mod.sin.wyvern.ItemMod.getModdedImproveTemplateId($1);"
            		+ "if(temp != -10){"
            		+ "  return temp;"
            		+ "}");*/
            
            // - Make food/drink affinities based on Item ID instead of creature ID - //
            // [3/27] Removed: Merged to ServerTweaks
            /*CtClass ctAffinitiesTimed = classPool.get("com.wurmonline.server.skills.AffinitiesTimed");
            replace = "if(item.getCreatorName() != null){"
            		+ "  $_ = $proceed("+MiscChanges.class.getName()+".getTimedAffinitySeed(item));"
            		+ "}else{"
            		+ "  $_ = $proceed($$);"
            		+ "}";
            Util.instrumentDeclared(thisClass, ctAffinitiesTimed, "getTimedAffinitySkill", "setSeed", replace);
            CtClass ctItemBehaviour = classPool.get("com.wurmonline.server.behaviours.ItemBehaviour");
            replace = "$_ = $proceed($1, $2, $3, $4, performer.getName());";
            Util.instrumentDeclared(thisClass, ctItemBehaviour, "handleRecipe", "createItem", replace);
            replace = "$_ = $proceed($1, $2, $3, $4, com.wurmonline.server.players.PlayerInfoFactory.getPlayerName(lastowner));";
            Util.instrumentDeclared(thisClass, ctItem, "pollFermenting", "createItem", replace);
            Util.instrumentDeclared(thisClass, ctItem, "pollDistilling", "createItem", replace);
            CtClass ctTempStates = classPool.get("com.wurmonline.server.items.TempStates");
            Util.instrumentDeclared(thisClass, ctTempStates, "checkForChange", "createItem", replace);*/
            
            // - Fix de-priesting when gaining faith below 30 - //
            // [Disabled 10/30 Sindusk] - Added to SpellCraft.SpellcraftTweaks
            /*CtClass ctDbPlayerInfo = classPool.get("com.wurmonline.server.players.DbPlayerInfo");
            ctDbPlayerInfo.getDeclaredMethod("setFaith").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("min")) {
                        m.replace("if($2 == 20.0f && $1 < 30){"
                        		+ "  $_ = $proceed(30.0f, lFaith);"
                        		+ "}else{"
                        		+ "  $_ = $proceed($$);"
                        		+ "}");
                        return;
                    }
                }
            });
            ctDbPlayerInfo.getDeclaredMethod("setFaith").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("setPriest")) {
                        m.replace("$_ = $proceed(true);");
                        return;
                    }
                }
            });
            ctDbPlayerInfo.getDeclaredMethod("setFaith").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("sendAlertServerMessage")) {
                        m.replace("$_ = null;");
                        return;
                    }
                }
            });*/
            
            // - Removal of eye/face shots to headshots instead - //
            HookManager.getInstance().registerHook("com.wurmonline.server.combat.Armour", "getArmourPosForPos", "(I)I", new InvocationHandlerFactory() {
            	 
                @Override
                public InvocationHandler createInvocationHandler() {
                    return new InvocationHandler() {

						@Override
						public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
							int pos = (int) args[0];
						     
                            if (pos == 18 || pos == 19 || pos == 20 || pos == 17) {
                                args[0] = 34;
                                //System.out.println("changed eye or face shot into headshot");
                            }
     
                            return method.invoke(proxy, args);
						}
                    };
                }
            });
            
            // - Remove requirement to bless for Libila taming - //
            CtClass ctMethodsCreatures = classPool.get("com.wurmonline.server.behaviours.MethodsCreatures");
            Util.instrumentDeclared(thisClass, ctMethodsCreatures, "tame", "isPriest", "$_ = false;");
            /*ctMethodsCreatures.getDeclaredMethod("tame").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("isPriest")) {
                        m.replace("$_ = false;");
                        return;
                    }
                }
            });*/
            
            // - Remove fatiguing actions requiring you to be on the ground - //
            CtClass ctAction = classPool.get("com.wurmonline.server.behaviours.Action");
            CtConstructor[] ctActionConstructors = ctAction.getConstructors();
            for(CtConstructor constructor : ctActionConstructors){
            	constructor.instrument(new ExprEditor(){
                    public void edit(MethodCall m) throws CannotCompileException {
                        if (m.getMethodName().equals("isFatigue")) {
                            m.replace("$_ = false;");
                            logger.info("Set isFatigue to false in action constructor.");
                            return;
                        }
                    }
                });
            }
            
            // - Allow all creatures to be displayed in the Mission Ruler - //
            CtClass ctMissionManager = classPool.get("com.wurmonline.server.questions.MissionManager");
            ctMissionManager.getDeclaredMethod("dropdownCreatureTemplates").instrument(new ExprEditor() {
                @Override
                public void edit(FieldAccess fieldAccess) throws CannotCompileException {
                    if (Objects.equals("baseCombatRating", fieldAccess.getFieldName()))
                        fieldAccess.replace("$_ = 1.0f;");
                    //logger.info("Instrumented Mission Ruler to display all creatures.");
                }
            });
            
            Util.setReason("Fix Portal Issues.");
            CtClass ctPortal = classPool.get("com.wurmonline.server.questions.PortalQuestion");
            Util.instrumentDeclared(thisClass, ctPortal, "sendQuestion", "willLeaveServer", "$_ = true;");
            Util.setReason("Fix Portal Issues.");
            Util.instrumentDeclared(thisClass, ctPortal, "sendQuestion", "getKnowledge", "$_ = true;");

            Util.setReason("Disable the minimum 0.01 damage on shield damage, allowing damage modifiers to rule.");
        	CtClass ctCombatHandler = classPool.get("com.wurmonline.server.creatures.CombatHandler");
        	replace = "if($1 < 0.5f){"
            		+ "  $_ = $proceed((float) 0, (float) $2);"
            		+ "}else{"
            		+ "  $_ = $proceed($$);"
            		+ "}";
        	Util.instrumentDeclared(thisClass, ctCombatHandler, "checkShield", "max", replace);

        	Util.setReason("Allow ghost creatures to breed (Chargers).");
        	Util.instrumentDeclared(thisClass, ctMethodsCreatures, "breed", "isGhost", "$_ = false;");
        	
        	Util.setReason("Allow Life Transfer to stack with Rotting Touch.");
        	replace = MiscChanges.class.getName()+".doLifeTransfer(this.creature, attWeapon, defdamage, armourMod);"
            		+ "$_ = $proceed($$);";
        	Util.instrumentDeclared(thisClass, ctCombatHandler, "setDamage", "isWeaponCrush", replace);
        	
        	/* - REMOVED: Added to core game -
        	Util.setReason("Fix dragon armour dropping on logout.");
        	Util.instrumentDeclared(thisClass, ctItem, "sleep", "isDragonArmour", "$_ = false;");
        	Util.setReason("Fix dragon armour dropping on logout.");
        	Util.instrumentDeclared(thisClass, ctItem, "sleepNonRecursive", "isDragonArmour", "$_ = false;");*/
        	
        	// - Type creatures randomly in the wild - //
        	CtClass[] params1 = {
            		CtClass.intType,
            		CtClass.booleanType,
            		CtClass.floatType,
            		CtClass.floatType,
            		CtClass.floatType,
            		CtClass.intType,
            		classPool.get("java.lang.String"),
            		CtClass.byteType,
            		CtClass.byteType,
            		CtClass.byteType,
            		CtClass.booleanType,
            		CtClass.byteType,
            		CtClass.intType
            };
            String desc1 = Descriptor.ofMethod(ctCreature, params1);
            replace = "$10 = "+MiscChanges.class.getName()+".newCreatureType($1, $10);";
            Util.insertBeforeDescribed(thisClass, ctCreature, "doNew", desc1, replace);
            
            // - Send rumour messages to discord - //
            Util.setReason("Send rumour messages to Discord.");
            replace = MiscChanges.class.getName()+".sendRumour(toReturn);"
            		+ "$proceed($$);";
            Util.instrumentDescribed(thisClass, ctCreature, "doNew", desc1, "broadCastSafe", replace);
            
            // - Allow custom creatures to be given special names when bred - //
            replace = "$_ = "+MiscChanges.class.getName()+".shouldBreedName(this);";
            Util.instrumentDeclared(thisClass, ctCreature, "checkPregnancy", "isHorse", replace);
        	/*ctCreature.getDeclaredMethod("checkPregnancy").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("isHorse")) {
                        m.replace("$_ = mod.sin.wyvern.MiscChanges.shouldBreedName(this);");
                        return;
                    }
                }
            });*/
        	
            // - Auto-Genesis a creature born on enchanted grass - //
            Util.setReason("Auto-Genesis a creature born on enchanted grass");
            replace = MiscChanges.class.getName()+".checkEnchantedBreed(newCreature);"
            		+ "$_ = $proceed($$);";
            Util.instrumentDeclared(thisClass, ctCreature, "checkPregnancy", "saveCreatureName", replace);
        	/*ctCreature.getDeclaredMethod("checkPregnancy").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("saveCreatureName")) {
                        m.replace("mod.sin.wyvern.MiscChanges.checkEnchantedBreed(newCreature);"
                        		+ "$_ = $proceed($$);");
                        return;
                    }
                }
            });*/
            
        	// - Allow statuettes to be used for casting even when not silver/gold - //
        	String desc2 = Descriptor.ofMethod(CtClass.booleanType, new CtClass[]{});
        	Util.setBodyDescribed(thisClass, ctItem, "isHolyItem", desc2, "return this.template.holyItem;");
        	//ctItem.getMethod("isHolyItem", desc2).setBody("return this.template.holyItem;");
        	
        	// - Allow GM's to bypass the 5 second emote sound limit. - //
        	replace = "if(this.getPower() > 0){"
        			+ "  return true;"
        			+ "}";
        	Util.insertBeforeDeclared(thisClass, ctPlayer, "mayEmote", replace);
        	/*ctPlayer.getDeclaredMethod("mayEmote").insertBefore(""
        			+ "if(this.getPower() > 0){"
        			+ "  return true;"
        			+ "}");*/
        	
        	// - Allow archery against ghost targets - //
        	CtClass ctArchery = classPool.get("com.wurmonline.server.combat.Archery");
        	CtMethod[] archeryAttacks = ctArchery.getDeclaredMethods("attack");
        	for(CtMethod method : archeryAttacks){
            	method.instrument(new ExprEditor(){
                    public void edit(MethodCall m) throws CannotCompileException {
                        if (m.getMethodName().equals("isGhost")) {
                            m.replace("$_ = false;");
                            logger.info("Enabled archery against ghost targets in archery attack method.");
                            return;
                        }
                    }
                });
        	}
        	
        	// - Prevent archery altogether against certain creatures - //
        	CtClass[] params3 = {
        			ctCreature,
        			ctCreature,
        			ctItem,
        			CtClass.floatType,
        			ctAction
        	};
        	String desc3 = Descriptor.ofMethod(CtClass.booleanType, params3);
        	replace = "if("+MethodsBestiary.class.getName()+".isArcheryImmune($1, $2)){"
        			+ "  return true;"
        			+ "}";
        	Util.insertBeforeDescribed(thisClass, ctArchery, "attack", desc3, replace);
        	
        	// - Make creatures wander slightly if they are shot from afar by an arrow - //
        	CtClass ctArrows = classPool.get("com.wurmonline.server.combat.Arrows");
        	replace = "if(!defender.isPathing()){"
            		+ "  defender.startPathing(com.wurmonline.server.Server.rand.nextInt(100));"
            		+ "}"
            		+ "$_ = $proceed($$);";
        	Util.instrumentDeclared(thisClass, ctArrows, "addToHitCreature", "addAttacker", replace);

        	Util.setReason("Broadcast death tabs to GL-Freedom.");
        	Util.insertBeforeDeclared(thisClass, ctPlayers, "broadCastDeathInfo", MiscChanges.class.getName()+".broadCastDeaths($1, $2);");
        	//ctPlayers.getDeclaredMethod("broadCastDeathInfo").insertBefore("mod.sin.wyvern.MiscChanges.broadCastDeaths($1, $2);");
        	
        	// - Reduce meditation cooldowns - //
        	CtClass ctCultist = classPool.get("com.wurmonline.server.players.Cultist");
        	replace = "return this.path == 1 && this.level > 3 && System.currentTimeMillis() - this.cooldown1 > "+(TimeConstants.HOUR_MILLIS*8)+";";
        	Util.setBodyDeclared(thisClass, ctCultist, "mayRefresh", replace);
        	//ctCultist.getDeclaredMethod("mayRefresh").setBody("return this.path == 1 && this.level > 3 && System.currentTimeMillis() - this.cooldown1 > 28800000;");
        	replace = "return this.path == 1 && this.level > 6 && System.currentTimeMillis() - this.cooldown2 > "+(TimeConstants.HOUR_MILLIS*8)+";";
        	Util.setBodyDeclared(thisClass, ctCultist, "mayEnchantNature", replace);
        	//ctCultist.getDeclaredMethod("mayEnchantNature").setBody("return this.path == 1 && this.level > 6 && System.currentTimeMillis() - this.cooldown2 > 28800000;");
        	replace = "return this.path == 1 && this.level > 8 && System.currentTimeMillis() - this.cooldown3 > "+(TimeConstants.HOUR_MILLIS*4)+";";
        	Util.setBodyDeclared(thisClass, ctCultist, "mayStartLoveEffect", replace);
        	//ctCultist.getDeclaredMethod("mayStartLoveEffect").setBody("return this.path == 1 && this.level > 8 && System.currentTimeMillis() - this.cooldown3 > 14400000;");
        	replace = "return this.path == 2 && this.level > 6 && System.currentTimeMillis() - this.cooldown1 > "+(TimeConstants.HOUR_MILLIS*6)+";";
        	Util.setBodyDeclared(thisClass, ctCultist, "mayStartDoubleWarDamage", replace);
        	//ctCultist.getDeclaredMethod("mayStartDoubleWarDamage").setBody("return this.path == 2 && this.level > 6 && System.currentTimeMillis() - this.cooldown1 > 21600000;");
        	replace = "return this.path == 2 && this.level > 3 && System.currentTimeMillis() - this.cooldown2 > "+(TimeConstants.HOUR_MILLIS*4)+";";
        	Util.setBodyDeclared(thisClass, ctCultist, "mayStartDoubleStructDamage", replace);
        	//ctCultist.getDeclaredMethod("mayStartDoubleStructDamage").setBody("return this.path == 2 && this.level > 3 && System.currentTimeMillis() - this.cooldown2 > 14400000;");
        	replace = "return this.path == 2 && this.level > 8 && System.currentTimeMillis() - this.cooldown3 > "+(TimeConstants.HOUR_MILLIS*6)+";";
        	Util.setBodyDeclared(thisClass, ctCultist, "mayStartFearEffect", replace);
        	//ctCultist.getDeclaredMethod("mayStartFearEffect").setBody("return this.path == 2 && this.level > 8 && System.currentTimeMillis() - this.cooldown3 > 21600000;");
        	replace = "return this.path == 5 && this.level > 8 && System.currentTimeMillis() - this.cooldown1 > "+(TimeConstants.HOUR_MILLIS*6)+";";
        	Util.setBodyDeclared(thisClass, ctCultist, "mayStartNoElementalDamage", replace);
        	//ctCultist.getDeclaredMethod("mayStartNoElementalDamage").setBody("return this.path == 5 && this.level > 8 && System.currentTimeMillis() - this.cooldown1 > 21600000;");
        	replace = "return this.path == 5 && this.level > 6 && System.currentTimeMillis() - this.cooldown2 > "+(TimeConstants.HOUR_MILLIS*8)+";";
        	Util.setBodyDeclared(thisClass, ctCultist, "maySpawnVolcano", replace);
        	//ctCultist.getDeclaredMethod("maySpawnVolcano").setBody("return this.path == 5 && this.level > 6 && System.currentTimeMillis() - this.cooldown2 > 28800000;");
        	replace = "return this.path == 5 && this.level > 3 && System.currentTimeMillis() - this.cooldown3 > "+(TimeConstants.HOUR_MILLIS*4)+";";
        	Util.setBodyDeclared(thisClass, ctCultist, "mayStartIgnoreTraps", replace);
        	//ctCultist.getDeclaredMethod("mayStartIgnoreTraps").setBody("return this.path == 5 && this.level > 3 && System.currentTimeMillis() - this.cooldown3 > 14400000;");
        	replace = "return this.path == 3 && this.level > 3 && System.currentTimeMillis() - this.cooldown1 > "+(TimeConstants.HOUR_MILLIS*4)+";";
        	Util.setBodyDeclared(thisClass, ctCultist, "mayCreatureInfo", replace);
        	//ctCultist.getDeclaredMethod("mayCreatureInfo").setBody("return this.path == 3 && this.level > 3 && System.currentTimeMillis() - this.cooldown1 > 14400000;");
        	replace = "return this.path == 3 && this.level > 6 && System.currentTimeMillis() - this.cooldown2 > "+(TimeConstants.HOUR_MILLIS*4)+";";
        	Util.setBodyDeclared(thisClass, ctCultist, "mayInfoLocal", replace);
        	//ctCultist.getDeclaredMethod("mayInfoLocal").setBody("return this.path == 3 && this.level > 6 && System.currentTimeMillis() - this.cooldown2 > 14400000;");

        	Util.setReason("Adjust weapon damage type based on the potion/salve applied.");
        	replace = "int wt = mod.sin.wyvern.MiscChanges.getWeaponType($1);"
        			+ "if(wt != -1){"
        			+ "  type = wt;"
        			+ "  return wt;"
        			+ "}";
        	Util.insertBeforeDeclared(thisClass, ctCombatHandler, "getType", replace);

        	Util.setReason("Attempt to prevent libila from losing faith when crossing servers.");
            CtClass ctIntraServerConnection = classPool.get("com.wurmonline.server.intra.IntraServerConnection");
            ctIntraServerConnection.getDeclaredMethod("savePlayerToDisk").instrument(new ExprEditor() {
                @Override
                public void edit(FieldAccess fieldAccess) throws CannotCompileException {
                    if (Objects.equals("PVPSERVER", fieldAccess.getFieldName())){
                        fieldAccess.replace("$_ = false;");
                        logger.info("Instrumented PVPSERVER = false for Libila faith transfers.");
                    }
                }
            });
            ctIntraServerConnection.getDeclaredMethod("savePlayerToDisk").instrument(new ExprEditor() {
                @Override
                public void edit(FieldAccess fieldAccess) throws CannotCompileException {
                    if (Objects.equals("HOMESERVER", fieldAccess.getFieldName())){
                        fieldAccess.replace("$_ = false;");
                        logger.info("Instrumented HOMESERVER = false for Libila faith transfers.");
                    }
                }
            });

            Util.setReason("Increase food affinity to give 30% increased skillgain instead of 10%.");
    		CtClass ctSkill = classPool.get("com.wurmonline.server.skills.Skill");
        	CtClass[] params4 = {
        			CtClass.doubleType,
        			CtClass.booleanType,
        			CtClass.floatType,
        			CtClass.booleanType,
        			CtClass.doubleType
        	};
        	String desc4 = Descriptor.ofMethod(CtClass.voidType, params4);
        	replace = "int timedAffinity = (com.wurmonline.server.skills.AffinitiesTimed.isTimedAffinity(pid, this.getNumber()) ? 2 : 0);"
            		+ "advanceMultiplicator *= (double)(1.0f + (float)timedAffinity * 0.1f);"
            		+ "$_ = $proceed($$);";
        	Util.instrumentDescribed(thisClass, ctSkill, "alterSkill", desc4, "hasSleepBonus", replace);

        	Util.setReason("Double the rate at which charcoal piles produce items.");
        	CtClass[] params5 = {
        			CtClass.booleanType,
        			CtClass.booleanType,
        			CtClass.longType
        	};
        	String desc5 = Descriptor.ofMethod(CtClass.booleanType, params5);
        	replace = "this.createDaleItems();"
            		+ "decayed = this.setDamage(this.damage + 1.0f * this.getDamageModifier());"
            		+ "$_ = $proceed($$);";
        	Util.instrumentDescribed(thisClass, ctItem, "poll", desc5, "createDaleItems", replace);

        	Util.setReason("Allow traders to display more than 9 items of a single type.");
            CtClass ctTradeHandler = classPool.get("com.wurmonline.server.creatures.TradeHandler");
        	ctTradeHandler.getDeclaredMethod("addItemsToTrade").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if(m.getMethodName().equals("size") && m.getLineNumber() > 200){ // I don't think the line number check matters, but I'm leaving it here anyway.
                    	m.replace("$_ = 1;");
                    	logger.info("Instrumented size for trades to allow traders to show more than 9 items at a time.");
                    }
                }
            });

        	Util.setReason("Increase deed upkeep by modifying the amount of tiles it thinks it has.");
            CtClass ctGuardPlan = classPool.get("com.wurmonline.server.villages.GuardPlan");
            replace = "$_ = "+MiscChanges.class.getName()+".getNewVillageTiles(vill.getNumTiles());";
            Util.instrumentDeclared(thisClass, ctGuardPlan, "getMonthlyCost", "getNumTiles", replace);

            Util.setReason("Adjust value for certain items.");
            replace = "int newVal = "+MiscChanges.class.getName()+".getNewValue(this);"
        			+ "if(newVal > 0){"
        			+ "  return newVal;"
        			+ "}";
            Util.insertBeforeDeclared(thisClass, ctItem, "getValue", replace);
        	
            //Util.setReason("Fix Glimmersteel & Adamantine veins from being depleted rapidly.");
        	CtClass ctCaveWallBehaviour = classPool.get("com.wurmonline.server.behaviours.CaveWallBehaviour");
        	CtClass[] params6 = {
        			ctAction,
        			ctCreature,
        			ctItem,
        			CtClass.intType,
        			CtClass.intType,
        			CtClass.booleanType,
        			CtClass.intType,
        			CtClass.intType,
        			CtClass.intType,
        			CtClass.shortType,
        			CtClass.floatType
        	};
        	String desc6 = Descriptor.ofMethod(CtClass.booleanType, params6);
            // [3/27] Removed: Merged to ServerTweaks.
        	/*replace = "resource = com.wurmonline.server.Server.getCaveResource(tilex, tiley);"
            		+ "if (resource == 65535) {"
            		+ "  resource = com.wurmonline.server.Server.rand.nextInt(10000);"
            		+ "}"
            		+ "if (resource > 1000 && (itemTemplateCreated == 693 || itemTemplateCreated == 697)) {"
            		+ "  resource = com.wurmonline.server.Server.rand.nextInt(1000);"
            		+ "}"
            		+ "$_ = $proceed($$);";
        	Util.instrumentDescribed(thisClass, ctCaveWallBehaviour, "action", desc6, "getDifficultyForTile", replace);*/
        	
        	Util.setReason("Allow players to mine directly to BSB's in vehicles.");
        	replace = "$_ = null;"
        			+ MiscChanges.class.getName()+".miningHook(performer, newItem);";
        	Util.instrumentDescribed(thisClass, ctCaveWallBehaviour, "action", desc6, "putItemInfrontof", replace);
        	
        	// -- Identify players making over 10 commands per second and causing the server log message -- //
        	CtClass ctCommunicator = classPool.get("com.wurmonline.server.creatures.Communicator");
        	replace = "$_ = $proceed($$);"
        			+ "if(this.player != null){"
        			+ "  logger.info(\"Potential player macro: \"+this.player.getName()+\" [\"+this.commandsThisSecond+\" commands]\");"
        			+ "}";
        	Util.instrumentDeclared(thisClass, ctCommunicator, "reallyHandle_CMD_ITEM_CREATION_LIST", "log", replace);
        	
        	Util.setReason("Allow ghost creatures to drop corpses.");
        	replace = "if("+MiscChanges.class.getName()+".isGhostCorpse(this)){"
        			+ "  $_ = false;"
        			+ "}else{"
        			+ "  $_ = $proceed($$);"
        			+ "}";
        	Util.instrumentDeclared(thisClass, ctCreature, "die", "isGhost", replace);
        	
        	Util.setReason("Set custom corpse sizes.");
        	replace = "$_ = $proceed($$);"
        			+ "if("+MiscChanges.class.getName()+".hasCustomCorpseSize(this)){"
        			+ "  "+MiscChanges.class.getName()+".setCorpseSizes(this, corpse);"
        			+ "}";
        	Util.instrumentDeclared(thisClass, ctCreature, "die", "addItem", replace);
        	
        	Util.setReason("Fix permissions in structures so players cannot cast spells unless they have enter permission.");
        	CtClass ctStructure = classPool.get("com.wurmonline.server.structures.Structure");
        	replace = "if(com.wurmonline.server.behaviours.Actions.isActionDietySpell(action)){"
        			+ "  return this.mayPass(performer);"
        			+ "}"
        			+ "$_ = $proceed($$);";
        	Util.instrumentDeclared(thisClass, ctStructure, "isActionAllowed", "isActionImproveOrRepair", replace);
        	
        	//1f+0.5f*(1f-Math.pow(2, -Math.pow((eff-1f), pow1)/pow2))
        	Util.setReason("Fix 100+ quality or power making certain interaction broken.");
        	replace = "{"
        			+ "double pow1 = 1.0;"
        			+ "double pow2 = 3.0;"
        			+ "double newEff = $1 >= 1.0 ? 1.0+0.5*(1.0-Math.pow(2.0, -Math.pow(($1-1.0), pow1)/pow2)) : Math.max(0.05, 1.0 - (1.0 - $1) * (1.0 - $1));"
        			+ "return newEff;"
        			+ "}";
        	Util.setBodyDeclared(thisClass, ctServer, "getBuffedQualityEffect", replace);
        	
        	/*Util.setReason("Fix Oakshell glance rates from going above 100% when cast power is above 100.");
        	replace = "if(defender.getBonusForSpellEffect((byte)22) >= 0.0f){"
        			+ "  evasionChance = Math.min(0.4f, evasionChance);"
        			+ "}"
        			+ "$_ = $proceed($$);";
        	Util.instrumentDeclared(thisClass, ctCombatHandler, "setDamage", "isTowerBasher", replace);*/
        	
        	/*Util.setReason("Ensure players always deal a wound and are not limited by 'does no real damage' messages.");
        	replace = MiscChanges.class.getName()+".setDefDamage(this.creature, defdamage);"
        			+ "defdamage = 99999d;"
        			+ "$_ = $proceed($$);";
        	Util.instrumentDeclared(thisClass, ctCombatHandler, "setDamage", "isTowerBasher", replace);
        	replace = "defdamage = "+MiscChanges.class.getName()+".getDefDamage(this.creature);"
        			+ "$_ = $proceed($$);";
        	Util.instrumentDeclared(thisClass, ctCombatHandler, "setDamage", "getBattle", replace);*/
        	
        } catch (CannotCompileException | NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException((Throwable)e);
        }
	}
}
