package mod.sin.wyvern;

import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.epic.Hota;
import com.wurmonline.server.items.*;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.NewSpawnQuestion;
import com.wurmonline.server.questions.SpawnQuestion;
import com.wurmonline.server.skills.Affinity;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import mod.sin.items.AffinityOrb;
import mod.sin.items.KeyFragment;
import mod.sin.items.caches.*;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.nyxcode.wurm.discordrelay.DiscordRelay;

import java.util.logging.Logger;

public class Arena {
	public static Logger logger = Logger.getLogger(Arena.class.getName());
	public static byte getArenaAttitude(Player player, Creature aTarget){
		if (player.getPower() > 0) {
            if (player.getPower() >= 5) {
                return 6;
            }
            return 3;
        }
		if (player == aTarget){
			return 1;
		}
		if (player.opponent == aTarget) {
            return 2;
        }
		if (player.getSaveFile().pet != -10 && aTarget.getWurmId() == player.getSaveFile().pet) {
            return 1;
        }
        if (aTarget.getDominator() != null && aTarget.getDominator() != player) {
            return player.getAttitude(aTarget.getDominator());
        }
        if (aTarget.isReborn() && player.getKingdomTemplateId() == 3) {
            return 0;
        }
        if (aTarget.hasAttackedUnmotivated() && (aTarget.isPlayer() || !aTarget.isDominated() || aTarget.getDominator() != player)) {
            return 2;
        }
        if (aTarget.citizenVillage != null && player.citizenVillage != null) {
            if (player.citizenVillage.isCitizen(aTarget)) {
                return 1;
            }
            if (player.citizenVillage.isAlly(aTarget)) {
                return 1;
            }
        }
        if (aTarget.isPlayer() && player.getTeam() != null && player.getTeam().contains(aTarget)){
        	return 1;
        }
        if (aTarget.isPlayer() && player.isFriend(aTarget.getWurmId())) {
            return 1;
        }
        if (aTarget.isPlayer()){
        	return 2;
        }
        if (aTarget.isAggHuman()) {
            return 2;
        }
		return 0;
	}

	public static void sendNewSpawnQuestion(SpawnQuestion sq){
	    NewSpawnQuestion nsq = new NewSpawnQuestion(sq.getResponder(), "Respawn", "Where would you like to spawn?", sq.getResponder().getWurmId());
	    nsq.sendQuestion();
    }

	public static void sendHotaMessage(String message){
	    if (SupplyDepots.host != null) {
            MiscChanges.sendGlobalFreedomChat(SupplyDepots.host, message, 200, 200, 200);
        }
	    DiscordRelay.sendToDiscord("arena", message, true);
    }

	public static void createNewHotaPrize(Village v, int winStreak){
		try {
			Item lump;
			int x;
			Item statue = ItemFactory.createItem(ItemList.statueHota, 99.0f, null);
			byte material = Materials.MATERIAL_GOLD;
			if (winStreak > 30) {
				material = Materials.MATERIAL_ADAMANTINE;
			}else if (winStreak > 20) {
				material = Materials.MATERIAL_GLIMMERSTEEL;
			}else if (winStreak > 10) {
				material = Materials.MATERIAL_DIAMOND;
			}else if (winStreak > 5) {
				material = Materials.MATERIAL_CRYSTAL;
			}
			statue.setMaterial(material);
			float posX = v.getToken().getPosX() - 2.0f + Server.rand.nextFloat() * 4.0f;
			float posY = v.getToken().getPosY() - 2.0f + Server.rand.nextFloat() * 4.0f;
			statue.setPosXYZRotation(posX, posY, Zones.calculateHeight(posX, posY, true), Server.rand.nextInt(350));
			for (int i = 0; i < winStreak; ++i) {
				if (i / 11 == winStreak % 11) {
					statue.setAuxData((byte) 0);
					statue.setData1(1);
					continue;
				}
				statue.setAuxData((byte)winStreak);
			}
			int r = (winStreak + Server.rand.nextInt(5)) * 50 & 255;
			int g = (winStreak + Server.rand.nextInt(5)) * 80 & 255;
			int b = (winStreak + Server.rand.nextInt(5)) * 120 & 255;
			statue.setColor(WurmColor.createColor(r, g, b));
			statue.getColor();
			Zone z = Zones.getZone(statue.getTileX(), statue.getTileY(), true);
			int numHelpers = 0;
			for (Citizen c : v.citizens.values()) {
				if (Hota.getHelpValue(c.getId()) <= 0) continue;
				++numHelpers;
			}
			numHelpers = Math.min(5, numHelpers);
			for (x = 0; x < numHelpers; ++x) {
				Item sleepPowder = ItemFactory.createItem(ItemList.sleepPowder, 99f, null);
				statue.insertItem(sleepPowder, true);
			}
			for (x = 0; x < 5; ++x) {
				lump = ItemFactory.createItem(ItemList.adamantineBar, Math.min(99f, 60 + (winStreak*Server.rand.nextFloat()*1.5f)), null);
				float baseWeight = lump.getWeightGrams();
				float multiplier = 1f;//+(winStreak*0.4f*Server.rand.nextFloat());
				lump.setWeight((int) (baseWeight*multiplier), true);
				statue.insertItem(lump, true);
			}
			for (x = 0; x < 5; ++x) {
				lump = ItemFactory.createItem(ItemList.glimmerSteelBar, Math.min(99f, 60 + (winStreak*Server.rand.nextFloat()*1.5f)), null);
                float baseWeight = lump.getWeightGrams();
                float multiplier = 1f;//+(winStreak*0.2f*Server.rand.nextFloat());
                lump.setWeight((int) (baseWeight*multiplier), true);
				statue.insertItem(lump, true);
			}
			// Key fragment
            Item keyFragment = ItemFactory.createItem(KeyFragment.templateId, 99f, null);
			statue.insertItem(keyFragment, true);
			// Add affinity orb
            Item affinityOrb = ItemFactory.createItem(AffinityOrb.templateId, 99f, null);
            statue.insertItem(affinityOrb, true);
			// Add 5 medium-quality caches
            int[] cacheIds = {
                    ArtifactCache.templateId,
                    CrystalCache.templateId, CrystalCache.templateId,
                    DragonCache.templateId, DragonCache.templateId, DragonCache.templateId,
                    RiftCache.templateId, RiftCache.templateId, RiftCache.templateId,
                    ToolCache.templateId,
                    TreasureMapCache.templateId
            };
            int i = 5+Server.rand.nextInt(4); // 5-8 caches.
            while(i > 0){
                Item cache = ItemFactory.createItem(cacheIds[Server.rand.nextInt(cacheIds.length)], 40f+(50f*Server.rand.nextFloat()), "");
                statue.insertItem(cache, true);
                i--;
            }
            i = 10+Server.rand.nextInt(11); // 10 - 20 kingdom tokens
            while(i > 0){
                Item token = ItemFactory.createItem(22765, 40f+(50f*Server.rand.nextFloat()), "");
                statue.insertItem(token, true);
                i--;
            }
            // Add 4-6 seryll lumps of medium ql
            i = 4+Server.rand.nextInt(3); // 4-6 lumps
            while(i > 0){
                Item seryll = ItemFactory.createItem(ItemList.seryllBar, 40f+(60f*Server.rand.nextFloat()), null);
                statue.insertItem(seryll, true);
                i--;
            }
            // Add 3-6 silver
            long iron = 30000; // 3 silver
            iron += Server.rand.nextInt(30000); // add up to 3 more silver
            Item[] coins = Economy.getEconomy().getCoinsFor(iron);
            for(Item coin : coins){
                statue.insertItem(coin, true);
            }
			z.addItem(statue);
		}
		catch (Exception ex) {
			logger.warning(ex.getMessage());
		}
	}
	public static void respawnPlayer(Creature player, ServerEntry server){
		ServerEntry targetserver = server.serverWest;
		if(player instanceof Player){
			Player p = (Player) player;
			int tilex = targetserver.SPAWNPOINTJENNX;
			int tiley = targetserver.SPAWNPOINTJENNY;
			p.sendTransfer(Server.getInstance(), targetserver.INTRASERVERADDRESS, Integer.parseInt(targetserver.INTRASERVERPORT), targetserver.INTRASERVERPASSWORD, targetserver.id, tilex, tiley, true, false, p.getKingdomId());
		}
	}

	public static Affinity[] getNullAffinities(){
	    return new Affinity[0];
    }

	public static void preInit(){
		try {
			ClassPool classPool = HookManager.getInstance().getClassPool();
			Class<Arena> thisClass = Arena.class;
			String replace;

            // - Allow horse gear to be added/removed from horses without branding or taming (PvP Only) - //
            CtClass ctCommunicator = classPool.get("com.wurmonline.server.creatures.Communicator");
            replace = "if(this.player.getPower() > 0){" +
                    "  $_ = this.player;" +
                    "}else if(com.wurmonline.server.Servers.isThisAPvpServer() && owner.getDominator() != this.player){"
            		+ "  $_ = owner.getLeader();"
            		+ "}else{"
            		+ "  $_ = $proceed($$);"
            		+ "}";
            Util.instrumentDeclared(thisClass, ctCommunicator, "reallyHandle_CMD_MOVE_INVENTORY", "getDominator", replace);
            Util.instrumentDeclared(thisClass, ctCommunicator, "equipCreatureCheck", "getDominator", replace);
            /*ctCommunicator.getDeclaredMethod("reallyHandle_CMD_MOVE_INVENTORY").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("getDominator")) {
                        m.replace("if(com.wurmonline.server.Servers.isThisAPvpServer() && owner.getDominator() != this.player){"
                        		+ "  $_ = owner.getLeader();"
                        		+ "}else{"
                        		+ "  if(this.player.getPower() > 0){"
                        		+ "    $_ = this.player;"
                        		+ "  }else{"
                        		+ "    $_ = $proceed($$);"
                        		+ "  }"
                        		+ "}");
                        return;
                    }
                }
            });*/
            
            // - Allow lockpicking on PvP server, as well as treasure chests on PvE - //
            CtClass ctItemBehaviour = classPool.get("com.wurmonline.server.behaviours.ItemBehaviour");
            CtClass ctAction = classPool.get("com.wurmonline.server.behaviours.Action");
            CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");
            CtClass ctItem = classPool.get("com.wurmonline.server.items.Item");
            CtClass[] params1 = {
            		ctAction,
            		ctCreature,
            		ctItem,
            		ctItem,
            		CtClass.shortType,
            		CtClass.floatType
            };
            String desc1 = Descriptor.ofMethod(CtClass.booleanType, params1);
            Util.setReason("Allow lockpicking on the PvP server and improve PvE treasure chest lockpicking.");
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER){"
            		+ "  $_ = true;"
            		+ "}else{"
            		+ "  $_ = target.getLastOwnerId() == -10 || target.getLastOwnerId() == 0 || target.getTemplateId() == 995;"
            		+ "}";
            Util.instrumentDescribed(thisClass, ctItemBehaviour, "action", desc1, "isInPvPZone", replace);

            CtClass ctMethodsItems = classPool.get("com.wurmonline.server.behaviours.MethodsItems");
            replace = "$_ = $proceed($$);"
            		+ "if($_ == -10 || $_ == 0){ ok = true; }";
            Util.instrumentDeclared(thisClass, ctMethodsItems, "picklock", "getLastOwnerId", replace);
            /*ctMethodsItems.getDeclaredMethod("picklock").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("getLastOwnerId")) {
                        m.replace("$_ = $proceed($$);"
                        		+ "if($_ == -10 || $_ == 0){ ok = true; }");
                        return;
                    }
                }
            });*/
            
            // - Disable villages and PMK's on the PvP server - //
            CtClass ctVillageFoundationQuestion = classPool.get("com.wurmonline.server.questions.VillageFoundationQuestion");
            Util.setReason("Allow placing deed outside of kingdom border.");
            replace = "$_ = (byte) 4;";
            Util.instrumentDeclared(thisClass, ctVillageFoundationQuestion, "checkSize", "getKingdom", replace);

            CtClass ctKingdomFoundationQuestion = classPool.get("com.wurmonline.server.questions.KingdomFoundationQuestion");
            Util.setReason("Disable PMK's on the Arena server.");
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER){"
            		+ "  this.getResponder().getCommunicator().sendSafeServerMessage(\"Player-Made Kingdoms are disabled on this server.\");"
            		+ "  return;"
            		+ "}";
            Util.insertBeforeDeclared(thisClass, ctKingdomFoundationQuestion, "sendQuestion", replace);

            // - Disable champion players altogether - //
            CtClass ctRealDeathQuestion = classPool.get("com.wurmonline.server.questions.RealDeathQuestion");
            Util.setReason("Disable player champions.");
            replace = "this.getResponder().getCommunicator().sendSafeServerMessage(\"Champion players are disabled on this server.\");"
            		+ "return;";
            Util.insertBeforeDeclared(thisClass, ctRealDeathQuestion, "sendQuestion", replace);
            
            // - Re-sort player aggression on the PvP server - //
            CtClass ctPlayer = classPool.get("com.wurmonline.server.players.Player");
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER){"
            		+ "  return "+Arena.class.getName()+".getArenaAttitude(this, $1);"
            		+ "}";
            Util.insertBeforeDeclared(thisClass, ctPlayer, "getAttitude", replace);

            Util.setReason("Re-sort creature-player aggression on the PvP server.");
            replace = "" +
                    "if(com.wurmonline.server.Servers.localServer.PVPSERVER && ($1.isPlayer() || this.isPlayer())){" +
                    "  if($1.citizenVillage != null && this.citizenVillage != null){" +
                    "    if($1.citizenVillage == this.citizenVillage){" +
                    "      return 1;" +
                    "    }" +
                    "    if($1.citizenVillage.isAlly(this.citizenVillage)){" +
                    "      return 1;" +
                    "    }" +
                    "  }" +
                    "}";
            Util.insertBeforeDeclared(thisClass, ctCreature, "getAttitude", replace);

            // - Hook for (ENEMY) declaration to allow for enemy presence blocking - //
            CtClass ctVirtualZone = classPool.get("com.wurmonline.server.zones.VirtualZone");
            CtClass[] params2 = {
            		CtClass.longType,
            		CtClass.booleanType,
            		CtClass.longType,
            		CtClass.floatType,
            		CtClass.floatType,
            		CtClass.floatType
            };
            String desc2 = Descriptor.ofMethod(CtClass.booleanType, params2);
            replace = "if(this.watcher.isPlayer()){" +
                    "  if("+PlayerTitles.class.getName()+".hasCustomTitle(creature)){" +
                    "    suff = suff + "+PlayerTitles.class.getName()+".getCustomTitle(creature);" +
                    "  }" +
                    "  if(com.wurmonline.server.Servers.localServer.PVPSERVER && creature.isPlayer() && "+Arena.class.getName()+".getArenaAttitude((com.wurmonline.server.players.Player)this.watcher, creature) == 2){"
            		+ "  suff = suff + \" (ENEMY)\";"
            		+ "  enemy = true;" +
                    "  }"
            		+ "}"
            		+ "$_ = $proceed($$);";
            Util.instrumentDescribed(thisClass, ctVirtualZone, "addCreature", desc2, "isChampion", replace);
            
            // - Modify when an enemy is present or not to use attitude instead of kingdom - //
            replace = "if(this.watcher.isPlayer() && creature.isPlayer() && com.wurmonline.server.Servers.localServer.PVPSERVER && "+Arena.class.getName()+".getArenaAttitude((com.wurmonline.server.players.Player)this.watcher, creature) == 2){"
            		+ "  $_ = 1;"
            		+ "}else{"
            		+ "  $_ = $proceed($$);"
            		+ "}";
            Util.instrumentDeclared(thisClass, ctVirtualZone, "checkIfEnemyIsPresent", "getKingdomId", replace);
            /*ctVirtualZone.getDeclaredMethod("checkIfEnemyIsPresent").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("getKingdomId")) {
                        m.replace("if(this.watcher.isPlayer() && creature.isPlayer() && com.wurmonline.server.Servers.localServer.PVPSERVER && mod.sin.wyvern.Arena.getArenaAttitude((com.wurmonline.server.players.Player)this.watcher, creature) == 2){"
                        		+ "  $_ = 1;"
                        		+ "}else{"
                        		+ "  $_ = $proceed($$);"
                        		+ "}");
                        return;
                    }
                }
            });*/

            // - Block twigs and stones on the PvP server - //
            CtClass ctMethodsCreatures = classPool.get("com.wurmonline.server.behaviours.MethodsCreatures");
            Util.setReason("Block farwalker twigs and stones on PvP.");
            replace = "$_ = com.wurmonline.server.Servers.localServer.PVPSERVER;";
            Util.instrumentDeclared(thisClass, ctMethodsCreatures, "teleportCreature", "isInPvPZone", replace);
            /*ctMethodsCreatures.getDeclaredMethod("teleportCreature").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("isInPvPZone")) {
                        m.replace("$_ = com.wurmonline.server.Servers.localServer.PVPSERVER;");
                        return;
                    }
                }
            });*/

            // - After respawn on PvP, send directly to PvE server - //
            /*CtClass ctSpawnQuestion = classPool.get("com.wurmonline.server.questions.SpawnQuestion");
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER){"
            		+ "  "+Arena.class.getName()+".respawnPlayer(this.getResponder(), com.wurmonline.server.Servers.localServer);"
            		+ "  return;"
            		+ "}";
            Util.insertBeforeDeclared(thisClass, ctSpawnQuestion, "sendQuestion", replace);*/
            /*ctSpawnQuestion.getDeclaredMethod("sendQuestion").insertBefore(""
            		+ "if(com.wurmonline.server.Servers.localServer.PVPSERVER){"
            		+ "  mod.sin.wyvern.Arena.respawnPlayer(this.getResponder(), com.wurmonline.server.Servers.localServer);"
            		+ "  return;"
            		+ "}");*/

            // - Allow affinity stealing and battle rank changes - //
            Util.setReason("Allow affinity stealing and battle rank changes.");
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER){"
        			+ "  $_ = true;"
        			+ "}else{"
        			+ "  $_ = $proceed($$);"
        			+ "}";
            Util.instrumentDeclared(thisClass, ctPlayer, "modifyRanking", "isEnemyOnChaos", replace);

            //CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");
            Util.setReason("Increase fight skill gain on PvP server.");
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER){"
        			+ "  p.getFightingSkill().setKnowledge(pskill + (skillGained*1.5d), false);"
        			+ "}"
        			+ "$_ = $proceed($$);";
            Util.instrumentDeclared(thisClass, ctCreature, "modifyFightSkill", "checkInitialTitle", replace);
            
            // - Fix nearby enemy check to find aggression instead of kingdom - //
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER){"
        			+ "  $_ = c.getAttitude(performer) != 2 && c.getAttitude(performer) != 1;"
        			+ "}else{"
        			+ "  $_ = $proceed($$);"
        			+ "}";
            Util.instrumentDeclared(thisClass, ctMethodsItems, "isEnemiesNearby", "isFriendlyKingdom", replace);
            /*ctMethodsItems.getDeclaredMethod("isEnemiesNearby").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("isFriendlyKingdom")) {
                        m.replace("if(com.wurmonline.server.Servers.localServer.PVPSERVER){"
                    			+ "  $_ = c.getAttitude(performer) != 2;"
                    			+ "}else{"
                    			+ "  $_ = $proceed($$);"
                    			+ "}");
                        return;
                    }
                }
            });*/

            // Die method description
            CtClass ctString = classPool.get("java.lang.String");
            CtClass[] params8 = new CtClass[]{
                    CtClass.booleanType,
                    ctString,
                    CtClass.booleanType
            };
            String desc8 = Descriptor.ofMethod(CtClass.voidType, params8);

            // - Ensure corpses are not loot protected on PvP - //
            Util.setReason("Ensure corpses are not loot protected.");
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER){"
        			+ "  $_ = $proceed(false);"
        			+ "}else{"
        			+ "  $_ = $proceed($$);"
        			+ "}";
            Util.instrumentDescribed(thisClass, ctCreature, "die", desc8, "setProtected", replace);
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER){"
        			+ "  $_ = true;"
        			+ "}else{"
        			+ "  $_ = $proceed($$);"
        			+ "}";
            Util.instrumentDescribed(thisClass, ctCreature, "die", desc8, "isInPvPZone", replace);

            // - Allow players to do actions in PvP houses - //
            CtClass ctMethods = classPool.get("com.wurmonline.server.behaviours.Methods");
            Util.setReason("Enable players to do actions in PvP houses.");
            replace = "$_ = com.wurmonline.server.Servers.localServer.PVPSERVER;";
            Util.instrumentDeclared(thisClass, ctMethods, "isNotAllowedMessage", "isEnemy", replace);

            // - Allow stealing against deity wishes without being punished on Arena - //
            //CtClass ctAction = classPool.get("com.wurmonline.server.behaviours.Action");
            Util.setReason("Allow stealing against deity wishes without being punished.");
            replace = "$_ = $proceed($$) || com.wurmonline.server.Servers.localServer.PVPSERVER;";
            Util.instrumentDeclared(thisClass, ctAction, "checkLegalMode", "isLibila", replace);
            /*ctAction.getDeclaredMethod("checkLegalMode").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("isLibila")) {
                        m.replace("$_ = $proceed($$) || com.wurmonline.server.Servers.localServer.PVPSERVER;");
                        return;
                    }
                }
            });*/

            // - Allow taking ownership of vehicles on Arena - //
            // TODO: Fix.
            Util.setReason("Allow taking ownership of vehicles on Arena.");
            CtClass[] params3 = new CtClass[]{
            		CtClass.longType,
            		CtClass.booleanType,
            		CtClass.byteType,
            		CtClass.intType,
            		CtClass.intType
            };
            String desc3 = Descriptor.ofMethod(CtClass.voidType, params3);
            replace = "$_ = com.wurmonline.server.Servers.localServer.PVPSERVER && !lVehicle.isLocked();";
            Util.instrumentDescribed(thisClass, ctCreature, "setVehicle", desc3, "isThisAChaosServer", replace);

            // - Multiply mine door bash damage by 3 on Arena - //
            CtClass ctTerraforming = classPool.get("com.wurmonline.server.behaviours.Terraforming");
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER){"
            		+ "  damage *= 3d;"
            		+ "}"
            		+ "$_ = $proceed($$);";
            Util.instrumentDeclared(thisClass, ctTerraforming, "destroyMineDoor", "getOrCreateTile", replace);
            /*ctTerraforming.getDeclaredMethod("destroyMineDoor").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("getOrCreateTile")) {
                        m.replace("if(com.wurmonline.server.Servers.localServer.PVPSERVER){"
                        		+ "  damage *= 3d;"
                        		+ "}"
                        		+ "$_ = $proceed($$);");
                        return;
                    }
                }
            });*/

            // - Prevent tons of errors for legality on Arena. - //
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER){"
            		+ "  return true;"
            		+ "}";
            Util.insertBeforeDeclared(thisClass, ctCreature, "isOkToKillBy", replace);
            /*ctCreature.getDeclaredMethod("isOkToKillBy").insertBefore(""
            		+ "if(com.wurmonline.server.Servers.localServer.PVPSERVER){"
            		+ "  return true;"
            		+ "}");*/
            Util.insertBeforeDeclared(thisClass, ctCreature, "hasBeenAttackedBy", replace);
            /*ctCreature.getDeclaredMethod("hasBeenAttackedBy").insertBefore(""
            		+ "if(com.wurmonline.server.Servers.localServer.PVPSERVER){"
            		+ "  return true;"
            		+ "}");*/
            
            // - Disable CA Help on Arena - //
			Util.setReason("Disable CA HELP on Arena.");
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER){"
            		+ "  return false;"
            		+ "}";
            Util.insertBeforeDeclared(thisClass, ctPlayer, "seesPlayerAssistantWindow", replace);

            Util.setReason("Make players who are non-allied enemies of villages.");
			CtClass ctVillage = classPool.get("com.wurmonline.server.villages.Village");
			CtClass[] params4 = new CtClass[]{
					ctCreature,
					CtClass.booleanType
			};
			String desc4 = Descriptor.ofMethod(CtClass.booleanType, params4);
            replace = "" +
					"if(com.wurmonline.server.Servers.localServer.PVPSERVER && $1.isPlayer()){" +
					"  if($1.getPower() > 0){" +
					"    return false;" +
					"  }" +
					"  if(this.isCitizen($1) || this.isAlly($1)){" +
					"    return false;" +
					"  }" +
					"  return true;" +
					"}" +
                    // Additional code added to ensure village guards do not attack titans or rare creatures.
                    "if("+Titans.class.getName()+".isTitan($1) || "+RareSpawns.class.getName()+".isRareCreature($1)){" +
                    "  return false;" +
                    "}";
            Util.insertBeforeDescribed(thisClass, ctVillage, "isEnemy", desc4, replace);

            Util.setReason("Make all deeds enemies of eachother unless allied.");
            CtClass[] params5 = new CtClass[]{
                    ctVillage
            };
            String desc5 = Descriptor.ofMethod(CtClass.booleanType, params5);
            replace = "{ if($1 == null){" +
                    "    return false;" +
                    "  }" +
                    "  if($1.kingdom != this.kingdom){" +
                    "    return true;" +
                    "  }" +
                    "  if(com.wurmonline.server.Servers.localServer.PVPSERVER){" +
                    "  if(this.isAlly($1)){" +
                    "    return false;" +
                    "  }" +
                    "  if($0 == $1){" +
                    "    return false;" +
                    "  }" +
                    "  return true;" +
                    "}" +
                    "return false; }";
            Util.setBodyDescribed(thisClass, ctVillage, "isEnemy", desc5, replace);

            Util.setReason("Change HotA reward");
            replace = Arena.class.getName()+".createNewHotaPrize(this, $1);";
            Util.setBodyDeclared(thisClass, ctVillage, "createHotaPrize", replace);

            CtClass ctGuardPlan = classPool.get("com.wurmonline.server.villages.GuardPlan");
            CtClass[] params6 = new CtClass[]{
                    CtClass.intType,
                    CtClass.intType
            };
            String desc6 = Descriptor.ofMethod(CtClass.intType, params6);
            Util.setReason("Cap maximum guards to 5.");
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER){" +
                    "  return Math.min(5, Math.max(3, $1 * $2 / 49));" +
                    "}";
            Util.insertBeforeDescribed(thisClass, ctGuardPlan, "getMaxGuards", desc6, replace);

            Util.setReason("Disable towers");
            CtClass ctAdvancedCreationEntry = classPool.get("com.wurmonline.server.items.AdvancedCreationEntry");
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER){" +
                    "  performer.getCommunicator().sendAlertServerMessage(\"Towers are disabled for now. A new system is in progress.\");" +
                    "  throw new com.wurmonline.server.NoSuchItemException(\"Towers are disabled.\");" +
                    "}else{" +
                    "  $_ = $proceed($$);" +
                    "}";
            Util.instrumentDeclared(thisClass, ctAdvancedCreationEntry, "cont", "isTowerTooNear", replace);
            Util.instrumentDeclared(thisClass, ctAdvancedCreationEntry, "run", "isTowerTooNear", replace);

            Util.setReason("Reduce local range (player).");
            replace = "if($3 > 5){" +
                    "  $_ = $proceed($1, $2, 50);" +
                    "}else{" +
                    "  $_ = $proceed($$);" +
                    "}";
            Util.instrumentDeclared(thisClass, ctVirtualZone, "coversCreature", "isWithinDistanceTo", replace);
            Util.setReason("Reduce local range (creature).");
            replace = "$_ = true;";
            Util.instrumentDeclared(thisClass, ctVirtualZone, "coversCreature", "isPlayer", replace);

            CtClass ctKarmaQuestion = classPool.get("com.wurmonline.server.questions.KarmaQuestion");
            Util.setReason("Disable Karma teleport.");
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER){" +
                    "  $_ = true;" +
                    "}else{" +
                    "  $_ = $proceed($$);" +
                    "}";
            Util.instrumentDeclared(thisClass, ctKarmaQuestion, "answer", "isInPvPZone", replace);

            Util.setReason("Make players only able to lead one creature.");
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER){" +
                    "  return this.followers == null || this.followers.size() < 1;" +
                    "}";
            Util.insertBeforeDeclared(thisClass, ctPlayer, "mayLeadMoreCreatures", replace);

            CtClass ctMethodsStructure = classPool.get("com.wurmonline.server.behaviours.MethodsStructure");
            Util.setReason("Increase bash timer to 15 seconds.");
            replace = "time = 600;" +
                    "$_ = $proceed($$);";
            Util.instrumentDeclared(thisClass, ctMethodsStructure, "destroyWall", "getStructure", replace);

            CtClass ctHota = classPool.get("com.wurmonline.server.epic.Hota");
            Util.setReason("Display discord message for HotA announcements.");
            replace = Arena.class.getName()+".sendHotaMessage($1);" +
                    "$_ = $proceed($$);";
            Util.instrumentDeclared(thisClass, ctHota, "poll", "broadCastSafe", replace);
            Util.setReason("Display discord message for HotA wins.");
            Util.instrumentDeclared(thisClass, ctHota, "win", "broadCastSafe", replace);
            Util.setReason("Display discord message for HotA conquers & neutralizes.");
            replace = "if($2.getData1() == 0){" +
                    "  "+Arena.class.getName()+".sendHotaMessage($1.getName() + \" neutralizes the \" + $2.getName() + \".\");" +
                    "}else{" +
                    "  "+Arena.class.getName()+".sendHotaMessage($1.getName() + \" conquers the \" + $2.getName() + \".\");" +
                    "}";
            Util.insertBeforeDeclared(thisClass, ctHota, "addPillarConquered", replace);

            // handle_TARGET_and_TARGET_HOSTILE
            CtClass ctCreatureBehaviour = classPool.get("com.wurmonline.server.behaviours.CreatureBehaviour");
            Util.setReason("Allow players to attack enemy guards.");
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER){" +
                    "  $_ = false;" +
                    "}else{" +
                    "  $_ = $proceed($$);" +
                    "}";
            Util.instrumentDeclared(thisClass, ctCreatureBehaviour, "handle_TARGET_and_TARGET_HOSTILE", "isFriendlyKingdom", replace);

            Util.setReason("Fix templars attacking themselves.");
            replace = "if($1.isSpiritGuard()){" +
                    "  return;" +
                    "}";
            Util.insertBeforeDeclared(thisClass, ctVillage, "addTarget", replace);

            Util.setReason("Keep mine doors open for shorter durations.");
            replace = "$_ = true;";
            Util.instrumentDeclared(thisClass, ctCreature, "checkOpenMineDoor", "isThisAChaosServer", replace);

            Util.setReason("Fix fight skill gains against enemy players.");
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER){" +
                    "  if($0 == this){" +
                    "    $_ = -1;" +
                    "  }else{" +
                    "    $_ = $proceed($$);" +
                    "  }" +
                    "}else{" +
                    "  $_ = $proceed($$);" +
                    "}";
            Util.instrumentDeclared(thisClass, ctCreature, "modifyFightSkill", "getKingdomId", replace);

            Util.setReason("Enable archering enemies on deeds.");
            replace = "$_ = true;";
            Util.instrumentDeclared(thisClass, ctVillage, "mayAttack", "isEnemyOnChaos", replace);

            // Server.rand.nextFloat()*(35/(1-2*desiredPercent))
            Util.setReason("Nerf magranon faith protection.");
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER){" +
                    "  $_ = com.wurmonline.server.Server.rand.nextInt(40);" +
                    "}else{" +
                    "  $_ = $proceed($$);" +
                    "}";
            Util.instrumentDescribed(thisClass, ctCreature, "die", desc8, "getFavor", replace);

            Util.setReason("Adjust spawn question mechanics.");
            CtClass ctSpawnQuestion = classPool.get("com.wurmonline.server.questions.SpawnQuestion");
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER){" +
                    Arena.class.getName()+".sendNewSpawnQuestion(this);" +
                    "return;" +
                    "}";
            Util.insertBeforeDeclared(thisClass, ctSpawnQuestion, "sendQuestion", replace);
            CtClass ctMeshIO = classPool.get("com.wurmonline.mesh.MeshIO");
            CtClass[] params7 = new CtClass[]{
                    ctCreature,
                    ctItem,
                    CtClass.intType,
                    CtClass.intType,
                    CtClass.intType,
                    CtClass.floatType,
                    CtClass.booleanType,
                    ctMeshIO,
                    CtClass.booleanType
            };
            String desc7 = Descriptor.ofMethod(CtClass.booleanType, params7);
            replace = Arena.class.getName()+".sendHotaMessage($1+\" \"+$2);" +
                    "$_ = $proceed($$);";
            Util.instrumentDescribed(thisClass, ctTerraforming, "dig", desc7, "addHistory", replace);

            // Creature performer, Item source, int tilex, int tiley, int tile, float counter, boolean corner, MeshIO mesh, boolean toPile
            Util.setReason("Set favored kingdom for PvP");
            CtClass ctDeities = classPool.get("com.wurmonline.server.deities.Deities");
            replace = "{ return (byte) 4; }";
            Util.setBodyDeclared(thisClass, ctDeities, "getFavoredKingdom", replace);

            Util.setReason("Set favored kingdom for PvP");
            CtClass ctDeity = classPool.get("com.wurmonline.server.deities.Deity");
            replace = "{ return (byte) 4; }";
            Util.setBodyDeclared(thisClass, ctDeities, "getFavoredKingdom", replace);

            /*Util.setReason("Decrease PvP combat damage.");
            CtClass ctString = classPool.get("java.lang.String");
            CtClass ctBattle = classPool.get("com.wurmonline.server.combat.Battle");
            CtClass ctCombatEngine = classPool.get("com.wurmonline.server.combat.CombatEngine");
            // @Nullable Creature performer, Creature defender, byte type, int pos, double damage, float armourMod,
            // String attString, @Nullable Battle battle, float infection, float poison, boolean archery, boolean alreadyCalculatedResist
            CtClass[] params8 = {
                    ctCreature,
                    ctCreature,
                    CtClass.byteType,
                    CtClass.intType,
                    CtClass.doubleType,
                    CtClass.floatType,
                    ctString,
                    ctBattle,
                    CtClass.floatType,
                    CtClass.floatType,
                    CtClass.booleanType,
                    CtClass.booleanType
            };
            String desc8 = Descriptor.ofMethod(CtClass.booleanType, params8);
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER && ($2.isDominated() || $2.isPlayer()) && $1 != null && $1.isPlayer()){" +
                    "  logger.info(\"Detected player hit against player/pet opponent. Halving damage.\");" +
                    "  $5 = $5 * 0.5d;" +
                    "}";
            Util.insertBeforeDescribed(thisClass, ctCombatEngine, "addWound", desc8, replace);*/

            Util.setReason("Reduce player vs player damage by half.");
            CtClass ctCombatHandler = classPool.get("com.wurmonline.server.creatures.CombatHandler");
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER && ($1.isDominated() || $1.isPlayer()) && $0.creature.isPlayer()){" +
                    //"  logger.info(\"Detected player hit against player/pet opponent. Halving damage.\");" +
                    "  $3 = $3 * 0.7d;" +
                    "}";
            Util.insertBeforeDeclared(thisClass, ctCombatHandler, "setDamage", replace);

            Util.setReason("Disable crown influence from spreading to enemies.");
            replace = "$_ = $0.getAttitude(this) == 1;";
            Util.instrumentDeclared(thisClass, ctPlayer, "spreadCrownInfluence", "isFriendlyKingdom", replace);

            Util.setReason("Disable item drops from players on Arena.");
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER && this.isPlayer()){" +
                    "  this.getCommunicator().sendSafeServerMessage(\"You have died on the Arena server and your items are kept safe.\");" +
                    "  keepItems = true;" +
                    "}" +
                    "$_ = $proceed($$);";
            Util.instrumentDescribedCount(thisClass, ctCreature, "die", desc8, "isOnCurrentServer", 1, replace);

            Util.setReason("Disable player skill loss on Arena.");
            replace = "if(this.isPlayer() && this.isDeathProtected()){" +
                    "  this.getCommunicator().sendSafeServerMessage(\"You have died with a Resurrection Stone and your knowledge is kept safe.\");" +
                    "  return;" +
                    "}else{" +
                    "  this.getCommunicator().sendAlertServerMessage(\"You have died without a Resurrection Stone, resulting in some of your knowledge being lost.\");" +
                    "}";
            Util.insertBeforeDeclared(thisClass, ctCreature, "punishSkills", replace);

            Util.setReason("Disable player fight skill loss on Arena.");
            replace = "if(this.isPlayer() && this.isDeathProtected()){" +
                    "  $_ = null;" +
                    "}else{" +
                    "  $_ = $proceed($$);" +
                    "}";
            Util.instrumentDeclaredCount(thisClass, ctCreature, "modifyFightSkill", "setKnowledge", 1, replace);

            Util.setReason("Disable player affinity loss on Arena.");
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER && this.isPlayer() && this.isDeathProtected()){" +
                    "  this.getCommunicator().sendSafeServerMessage(\"Your resurrection stone keeps your affinities safe from your slayers.\");" +
                    "  $_ = "+Arena.class.getName()+".getNullAffinities();" +
                    "}else{" +
                    "  $_ = $proceed($$);" +
                    "}";
            Util.instrumentDeclared(thisClass, ctPlayer, "modifyRanking", "getAffinities", replace);

            /*Util.setReason("Enable stealing from deeds.");
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER){" +
                    "  $_ = true;" +
                    "}else{" +
                    "  $_ = $proceed($$);" +
                    "}";
            Util.instrumentDeclared(thisClass, ctMethodsItems, "checkIfStealing", "mayPass", replace);*/


		}catch (NotFoundException e) {
			throw new HookException(e);
        }
	}
}
