package mod.sin.wyvern;

import com.wurmonline.server.*;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.webinterface.WcKingdomChat;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.Enchants;
import javassist.*;
import javassist.bytecode.Descriptor;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modsupport.ModSupportDb;
import org.nyxcode.wurm.discordrelay.DiscordRelay;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Logger;

public class MiscChanges {
	public static Logger logger = Logger.getLogger(MiscChanges.class.getName());
	
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
	
	public static void sendServerTabMessage(String channel, final String message, final int red, final int green, final int blue){
		DiscordRelay.sendToDiscord(channel, message, true);
		// WARNING: Never change this from a new Runnable. Lambdas are a lie and will break everything.
		Runnable r = new Runnable() {
            public void run() {
                Message mess;
                for (Player rec : Players.getInstance().getPlayers()) {
                    mess = new Message(rec, (byte) 16, "Server", message, red, green, blue);
                    rec.getCommunicator().sendMessage(mess);
                }
            }
        };
        r.run();
	}
	
	public static void sendGlobalFreedomChat(final Creature sender, final String message, final int red, final int green, final int blue){
		Runnable r = () -> {
            Message mess;
            for(Player rec : Players.getInstance().getPlayers()){
                mess = new Message(sender, (byte)10, "GL-Freedom", "<"+sender.getNameWithoutPrefixes()+"> "+message, red, green, blue);
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
        };
        r.run();
	}

	public static void broadCastDeaths(Creature player, String slayers){
	    String slayMessage = "slain by ";
		sendGlobalFreedomChat(player, slayMessage+slayers, 200, 25, 25);
		addPlayerStatsDeath(player.getName());
		addPlayerStatsKill(slayers);
		DiscordRelay.sendToDiscord("deaths", player.getName()+" "+slayMessage+slayers, true);
	}

	public static void addPlayerStatsDeath(String playerName){
        Connection dbcon;
        PreparedStatement ps;
        try {
            dbcon = ModSupportDb.getModSupportDb();
            ps = dbcon.prepareStatement("UPDATE PlayerStats SET DEATHS = DEATHS + 1 WHERE NAME = \""+playerName+"\"");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addPlayerStatsKill(String slayers){
	    String[] slayerNames = slayers.split(" ");
	    Connection dbcon;
        PreparedStatement ps;
        try {
            dbcon = ModSupportDb.getModSupportDb();
            for(String slayer : slayerNames) {
                if(slayer.length() < 2) continue;
                ps = dbcon.prepareStatement("UPDATE PlayerStats SET KILLS = KILLS + 1 WHERE NAME = \"" + slayer + "\"");
                ps.executeUpdate();
                ps.close();
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean checkMayorCommand(Item item, Creature creature){
	    if(Servers.localServer.PVPSERVER){
	        return false;
        }
	    PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(item.getLastOwnerId());
	    if(pinf != null){
            if(pinf.getLastLogout() < System.currentTimeMillis()-TimeConstants.DAY_MILLIS*7){
                if(creature.getCitizenVillage() != null){
                    Village v = creature.getCitizenVillage();
                    if(v.getMayor().getId() == creature.getWurmId()){
                        VolaTile vt = Zones.getTileOrNull(item.getTilePos(), item.isOnSurface());
                        if(vt != null && vt.getVillage() != null && vt.getVillage() == v){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
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
            String[] infoTabLine = {"Server Thread: https://forum.wurmonline.com/index.php?/topic/162067-revenant-modded-pvepvp-3x-action-new-skillgain/",
            		"Server Discord: https://discord.gg/r8QNXAC",
            		"Server Maps: https://www.sarcasuals.com/revenant/"};
            StringBuilder str = new StringBuilder("{"
                    + "        com.wurmonline.server.Message mess;");
            for (String anInfoTabLine : infoTabLine) {
                str.append("        mess = new com.wurmonline.server.Message(player, (byte)16, \"").append(infoTabTitle).append("\",\"").append(anInfoTabLine).append("\", 0, 255, 0);").append("        player.getCommunicator().sendMessage(mess);");
            }
            str.append("}");
            m.insertAfter(str.toString());

            // - Enable bridges to be built inside/over/through houses - //
            CtClass ctPlanBridgeChecks = classPool.get("com.wurmonline.server.structures.PlanBridgeChecks");
            replace = "{ return new com.wurmonline.server.structures.PlanBridgeCheckResult(false); }";
            Util.setBodyDeclared(thisClass, ctPlanBridgeChecks, "checkForBuildings", replace);

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

            // - Enable creature custom colors - (Used for creating custom color creatures eg. Lilith) - //
            CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");
            Util.setBodyDeclared(thisClass, ctCreature, "hasCustomColor", "{ return true; }");

            // - Increase the amount of checks for new unique spawns by 5x - //
            CtClass ctServer = classPool.get("com.wurmonline.server.Server");
            replace = "for(int i = 0; i < 5; i++){"
            		+ "  $_ = $proceed($$);"
            		+ "}";
            Util.instrumentDeclared(thisClass, ctServer, "run", "checkDens", replace);
            
            // - Add Facebreyker to the list of spawnable uniques - //
            CtClass ctDens = classPool.get("com.wurmonline.server.zones.Dens");
            replace = "com.wurmonline.server.zones.Dens.checkTemplate(2147483643, whileRunning);";
            Util.insertBeforeDeclared(thisClass, ctDens, "checkDens", replace);
            //ctDens.getDeclaredMethod("checkDens").insertAt(0, "com.wurmonline.server.zones.Dens.checkTemplate(2147483643, whileRunning);");

            // - Announce player titles in the Server tab - //
            CtClass ctPlayer = classPool.get("com.wurmonline.server.players.Player");
            replace = "$_ = $proceed($$);"
            		+ "if(!com.wurmonline.server.Servers.localServer.PVPSERVER && this.getPower() < 1){"
            		+ "  "+MiscChanges.class.getName()+".sendServerTabMessage(\"event\", this.getName()+\" just earned the title of \"+title.getName(this.isNotFemale())+\"!\", 200, 100, 0);"
            		+ "}";
            Util.instrumentDeclared(thisClass, ctPlayer, "addTitle", "sendNormalServerMessage", replace);

            // - Make leather not suck even after it's able to be combined. - //
            CtClass ctMethodsItems = classPool.get("com.wurmonline.server.behaviours.MethodsItems");
            replace = "if(com.wurmonline.server.behaviours.MethodsItems.getImproveTemplateId(target) != 72){"
            		+ "  $_ = $proceed($$);"
            		+ "}else{"
            		+ "  $_ = false;"
            		+ "}";
            Util.instrumentDeclared(thisClass, ctMethodsItems, "improveItem", "isCombine", replace);
            
            // - Check new improve materials - //
            // TODO: Re-enable when custom items are created that require it.
            /*replace = "int temp = "+ItemMod.class.getName()+".getModdedImproveTemplateId($1);"
            		+ "if(temp != -10){"
            		+ "  return temp;"
            		+ "}";
            Util.insertBeforeDeclared(thisClass, ctMethodsItems, "getImproveTemplateId", replace);*/
            
            // - Remove fatiguing actions requiring you to be on the ground - //
            CtClass ctAction = classPool.get("com.wurmonline.server.behaviours.Action");
            CtConstructor[] ctActionConstructors = ctAction.getConstructors();
            for(CtConstructor constructor : ctActionConstructors){
            	constructor.instrument(new ExprEditor(){
                    public void edit(MethodCall m) throws CannotCompileException {
                        if (m.getMethodName().equals("isFatigue")) {
                            m.replace("" +
                                    "if(com.wurmonline.server.Servers.localServer.PVPSERVER){" +
                                    "  if(!com.wurmonline.server.behaviours.Actions.isActionDestroy(this.getNumber())){" +
                                    "    $_ = false;" +
                                    "  }else{" +
                                    "    $_ = $proceed($$);" +
                                    "  }" +
                                    "}else{" +
                                    "  $_ = false;" +
                                    "}");
                            logger.info("Set isFatigue to false in action constructor.");
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
                    logger.info("Instrumented Mission Ruler to display all creatures.");
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

        	Util.setReason("Allow Life Transfer to stack with Rotting Touch (Mechanics-Wise).");
        	replace = MiscChanges.class.getName()+".doLifeTransfer(this.creature, attWeapon, defdamage, armourMod);"
            		+ "$_ = $proceed($$);";
        	Util.instrumentDeclared(thisClass, ctCombatHandler, "setDamage", "isWeaponCrush", replace);

        	// - Allow GM's to bypass the 5 second emote sound limit. - //
        	replace = "if(this.getPower() > 0){"
        			+ "  return true;"
        			+ "}";
        	Util.insertBeforeDeclared(thisClass, ctPlayer, "mayEmote", replace);

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

        	// -- Identify players making over 10 commands per second and causing the server log message -- //
        	CtClass ctCommunicator = classPool.get("com.wurmonline.server.creatures.Communicator");
        	replace = "$_ = $proceed($$);"
        			+ "if(this.player != null){"
        			+ "  logger.info(\"Potential player macro: \"+this.player.getName()+\" [\"+this.commandsThisSecond+\" commands]\");"
        			+ "}";
        	Util.instrumentDeclared(thisClass, ctCommunicator, "reallyHandle_CMD_ITEM_CREATION_LIST", "log", replace);

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

            // double advanceMultiplicator, boolean decay, float times, boolean useNewSystem, double skillDivider)
            CtClass[] params = {
                    CtClass.doubleType,
                    CtClass.booleanType,
                    CtClass.floatType,
                    CtClass.booleanType,
                    CtClass.doubleType
            };
            String desc = Descriptor.ofMethod(CtClass.voidType, params);
            double minRate = 1.0D;
            double maxRate = 8.0D;
            double newPower = 2.5;

            Util.setReason("Adjust skill rate to a new, dynamic rate system.");
            replace = "double minRate = " + String.valueOf(minRate) + ";" +
                    "double maxRate = " + String.valueOf(maxRate) + ";" +
                    "double newPower = " + String.valueOf(newPower) + ";" +
                    "$1 = $1*(minRate+(maxRate-minRate)*Math.pow((100-this.knowledge)*0.01, newPower));";
            Util.insertBeforeDescribed(thisClass, ctSkill,"alterSkill", desc, replace);

            Util.setReason("Adjust the amount of scale/hide to distribute after a slaying (1/5).");
            replace = "{ return (1.0f + (float)$1.getWeightGrams() * $2)*0.2f; }";
            Util.setBodyDeclared(thisClass, ctCreature, "calculateDragonLootTotalWeight", replace);

			CtClass ctCargoTransportationMethods = classPool.get("com.wurmonline.server.behaviours.CargoTransportationMethods");
            Util.setReason("Disable strength requirement checks for load/unload.");
			replace = "{ return true; }";
			Util.setBodyDeclared(thisClass, ctCargoTransportationMethods, "strengthCheck", replace);

            Util.setReason("Reduce chance of lockpicks breaking.");
            replace = "$_ = 40f + $proceed($$);";
            Util.instrumentDeclared(thisClass, ctMethodsItems, "checkLockpickBreakage", "getCurrentQualityLevel", replace);

			CtClass ctTileBehaviour = classPool.get("com.wurmonline.server.behaviours.TileBehaviour");
			CtMethod[] ctGetBehavioursFors = ctTileBehaviour.getDeclaredMethods("getBehavioursFor");
			for(CtMethod method : ctGetBehavioursFors){
				method.instrument(new ExprEditor(){
					public void edit(MethodCall m) throws CannotCompileException {
						if (m.getMethodName().equals("getKingdomTemplateId")) {
							m.replace("$_ = 3;");
						}
					}
				});
			}

			Util.setReason("Enable Mycelium to be absorbed from Freedom Isles.");
			replace = "$_ = 3;";
			CtClass[] params7 = {
					ctAction,
					ctCreature,
					CtClass.intType,
					CtClass.intType,
					CtClass.booleanType,
					CtClass.intType,
					CtClass.shortType,
					CtClass.floatType
			};
			String desc7 = Descriptor.ofMethod(CtClass.booleanType, params7);
			Util.instrumentDescribed(thisClass, ctTileBehaviour, "action", desc7, "getKingdomTemplateId", replace);

			CtClass ctMethodsStructure = classPool.get("com.wurmonline.server.behaviours.MethodsStructure");
			Util.setReason("Allow players to construct larger houses.");
			float carpentryMultiplier = 2f;
			replace = "if(!com.wurmonline.server.Servers.localServer.PVPSERVER){" +
					"  $_ = $proceed($$)*"+String.valueOf(carpentryMultiplier)+";" +
					"}else{" +
					"  $_ = $proceed($$);" +
					"}";
			Util.instrumentDeclared(thisClass, ctMethodsStructure, "hasEnoughSkillToExpandStructure", "getKnowledge", replace);
			Util.setReason("Allow players to construct larger houses.");
			Util.instrumentDeclared(thisClass, ctMethodsStructure, "hasEnoughSkillToContractStructure", "getKnowledge", replace);

			CtClass ctPlayerInfo = classPool.get("com.wurmonline.server.players.PlayerInfo");
			Util.setReason("Remove waiting time between converting deity.");
			replace = "{ return true; }";
			Util.setBodyDeclared(thisClass, ctPlayerInfo, "mayChangeDeity", replace);

            CtClass ctBless = classPool.get("com.wurmonline.server.spells.Bless");
			Util.setReason("Fix Bless infidel error.");
			replace = "$_ = true;";
            Util.instrumentDeclared(thisClass, ctBless, "precondition", "accepts", replace);

            CtClass ctRefresh = classPool.get("com.wurmonline.server.spells.Refresh");
            Util.setReason("Fix Refresh infidel error.");
            replace = "$_ = true;";
            Util.instrumentDeclared(thisClass, ctRefresh, "precondition", "accepts", replace);

            CtClass ctArmourTypes = classPool.get("com.wurmonline.server.combat.ArmourTypes");
            Util.setReason("Use epic armor DR values.");
            replace = "$_ = true;";
            Util.instrumentDeclared(thisClass, ctArmourTypes, "getArmourBaseDR", "isChallengeOrEpicServer", replace);

            Util.setReason("Use epic armor effectiveness values.");
            replace = "$_ = true;";
            Util.instrumentDeclared(thisClass, ctArmourTypes, "getArmourEffModifier", "isChallengeOrEpicServer", replace);

            Util.setReason("Use epic armor material values.");
            replace = "$_ = true;";
            Util.instrumentDeclared(thisClass, ctArmourTypes, "getArmourMatBonus", "isChallengeOrEpicServer", replace);

            CtClass ctArmour = classPool.get("com.wurmonline.server.combat.Armour");
            Util.setReason("Use epic armor initialization values.");
            replace = "$_ = true;";
            Util.instrumentDeclared(thisClass, ctArmour, "initialize", "isChallengeOrEpicServer", replace);

            Util.setReason("Reduce power of imbues.");
            replace = "$_ = Math.max(-80d, -80d+$2);";
            Util.instrumentDeclared(thisClass, ctMethodsItems, "smear", "max", replace);

            Util.setReason("Update vehicle speeds reliably.");
            replace = "if($1 == 8){" +
                    "  $_ = 0;" +
                    "}else{" +
                    "  $_ = $proceed($$);" +
                    "}";
            Util.instrumentDeclared(thisClass, ctPlayer, "checkVehicleSpeeds", "nextInt", replace);

            Util.setReason("Reduce mailing costs by 90%.");
            CtClass ctMailSendConfirmQuestion = classPool.get("com.wurmonline.server.questions.MailSendConfirmQuestion");
            replace = "$_ = $_ / 10;";
            Util.insertAfterDeclared(thisClass, ctMailSendConfirmQuestion, "getCostForItem", replace);

            Util.setReason("Remove spam from creature enchantments on zombies.");
            CtClass ctCreatureEnchantment = classPool.get("com.wurmonline.server.spells.CreatureEnchantment");
            replace = "$_ = false;";
            Util.instrumentDeclared(thisClass, ctCreatureEnchantment, "precondition", "isReborn", replace);

            Util.setReason("Fix epic mission naming.");
            CtClass ctEpicServerStatus = classPool.get("com.wurmonline.server.epic.EpicServerStatus");
            replace = "if($2.equals(\"\")){" +
                    "  $2 = com.wurmonline.server.deities.Deities.getDeityName($1);" +
                    "}";
            Util.insertBeforeDeclared(thisClass, ctEpicServerStatus, "generateNewMissionForEpicEntity", replace);

            Util.setReason("Remove guard tower guards helping against certain types of enemies.");
            CtClass ctGuardTower = classPool.get("com.wurmonline.server.kingdom.GuardTower");
            replace = "if($0.isUnique() || "+Titans.class.getName()+".isTitan($0) || "+RareSpawns.class.getName()+".isRareCreature($0)){" +
                    "  $_ = false;" +
                    "}else{" +
                    "  $_ = $proceed($$);" +
                    "}";
            Util.instrumentDeclared(thisClass, ctGuardTower, "alertGuards", "isWithinTileDistanceTo", replace);

            Util.setReason("Ensure unique creatures cannot be hitched to vehicles.");
            CtClass ctVehicle = classPool.get("com.wurmonline.server.behaviours.Vehicle");
            replace = "if($1.isUnique()){" +
                    "  return false;" +
                    "}";
            Util.insertBeforeDeclared(thisClass, ctVehicle, "addDragger", replace);

            // Enable Strongwall for Libila and other spells on PvE
            CtClass ctSpellGenerator = classPool.get("com.wurmonline.server.spells.SpellGenerator");
            ctSpellGenerator.getDeclaredMethod("createSpells").instrument(new ExprEditor() {
                @Override
                public void edit(FieldAccess fieldAccess) throws CannotCompileException {
                    if (Objects.equals("PVPSERVER", fieldAccess.getFieldName()))
                        fieldAccess.replace("$_ = true;");
                    logger.info("Instrumented SpellGenerator PVPSERVER field to enable all spells.");
                }
            });

            Util.setReason("Make heated food never decay if cooked by a royal cook.");
            CtClass ctTempStates = classPool.get("com.wurmonline.server.items.TempStates");
            replace = "$_ = $proceed($$);" +
                    "if(chefMade){" +
                    "  $0.setName(\"royal \"+$0.getName());" +
                    "  $0.setHasNoDecay(true);" +
                    "}";
            Util.instrumentDeclared(thisClass, ctTempStates, "checkForChange", "setName", replace);

            Util.setReason("Stop royal food decay.");
            // Item parent, int parentTemp, boolean insideStructure, boolean deeded, boolean saveLastMaintained, boolean inMagicContainer, boolean inTrashbin
            CtClass[] params11 = {
                    ctItem,
                    CtClass.intType,
                    CtClass.booleanType,
                    CtClass.booleanType,
                    CtClass.booleanType,
                    CtClass.booleanType,
                    CtClass.booleanType
            };
            String desc11 = Descriptor.ofMethod(CtClass.booleanType, params11);
            replace = "if($0.isFood() && $0.hasNoDecay()){" +
                    "  $_ = false;" +
                    "}else{" +
                    "  $_ = $proceed($$);" +
                    "}";
            Util.instrumentDescribed(thisClass, ctItem, "poll", desc11, "setDamage", replace);

            Util.setReason("Allow mayors to command abandoned vehicles off their deed.");
            replace = "if("+MiscChanges.class.getName()+".checkMayorCommand($0, $1)){" +
                    "  return true;" +
                    "}";
            Util.insertBeforeDeclared(thisClass, ctItem, "mayCommand", replace);

        } catch (CannotCompileException | NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        }
	}
}
