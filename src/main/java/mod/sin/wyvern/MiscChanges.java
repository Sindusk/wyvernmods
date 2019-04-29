package mod.sin.wyvern;

import com.wurmonline.server.*;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.SpellEffectsEnum;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.SimpleCreationEntry;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.Titles;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.webinterface.WcKingdomChat;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.Enchants;
import com.wurmonline.shared.util.StringUtilities;
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
import java.util.*;
import java.util.logging.Logger;

public class MiscChanges {
	public static Logger logger = Logger.getLogger(MiscChanges.class.getName());
	
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
	    sendGlobalFreedomChat(sender, sender.getNameWithoutPrefixes(), message, red, green, blue);
    }
	public static void sendGlobalFreedomChat(final Creature sender, final String name, final String message, final int red, final int green, final int blue){
		Runnable r = () -> {
            Message mess;
            for(Player rec : Players.getInstance().getPlayers()){
                mess = new Message(sender, (byte)10, "GL-Freedom", "<"+name+"> "+message, red, green, blue);
                rec.getCommunicator().sendMessage(mess);
            }
            if (message.trim().length() > 1) {
                WcKingdomChat wc = new WcKingdomChat(WurmId.getNextWCCommandId(), sender.getWurmId(), name, message, false, (byte) 4, red, green, blue);
                if (!Servers.isThisLoginServer()) {
                    wc.sendToLoginServer();
                } else {
                    wc.sendFromLoginServer();
                }
            }
        };
        r.run();
	}

	public static void broadCastDeathsPvE(Player player, Map<Long, Long> attackers){
	    StringBuilder attackerString = new StringBuilder();
        final long now = System.currentTimeMillis();
        for (final Long attackerId : attackers.keySet()) {
            final Long time = attackers.get(attackerId);
            try {
                final Creature creature = Creatures.getInstance().getCreature(attackerId);
                if (now - time >= 600000L) {
                    continue;
                }
                if(attackerString.length() > 0){
                    attackerString.append(" ");
                }
                attackerString.append(StringUtilities.raiseFirstLetter(creature.getName()));
                if (creature.isPlayer()) {
                    return;
                }
            }
            catch (NoSuchCreatureException ignored) {}
        }
        if(!attackerString.toString().isEmpty()) {
            Players.getInstance().broadCastDeathInfo(player, attackerString.toString());
        }
    }

	public static void broadCastDeaths(Creature player, String slayers){
	    String slayMessage = "slain by ";
		sendGlobalFreedomChat(player, slayMessage+slayers, 200, 25, 25);
		addPlayerStatsDeath(player.getName());
		addPlayerStatsKill(slayers);
		DiscordRelay.sendToDiscord("deaths", player.getName()+" "+slayMessage+slayers, true);
	}

    public static void addPlayerStat(String playerName, String stat){
        Connection dbcon;
        PreparedStatement ps;
        try {
            dbcon = ModSupportDb.getModSupportDb();
            ps = dbcon.prepareStatement("UPDATE PlayerStats SET "+stat+" = "+stat+" + 1 WHERE NAME = \""+playerName+"\"");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

    public static float getFoodOpulenceBonus(Item food){
	    float mult = 1.0f;
        if(food.getSpellEffectPower(Enchants.BUFF_OPULENCE) > 0f){
            mult *= 1.0f + (food.getSpellEffectPower(Enchants.BUFF_OPULENCE)*0.0025f);
        }
        return food.getFoodComplexity()*mult;
    }

    public static long getBedBonus(long secs, long bed){
        Optional<Item> beds = Items.getItemOptional(bed);
        if(beds.isPresent()) {
            Item bedItem = beds.get();
            if(bedItem.isBed()){
                secs *= 1+(bedItem.getCurrentQualityLevel()*0.005f);
            }
        }
        secs *= 2;
	    return secs;
    }

    public static boolean royalSmithImprove(Creature performer, Skill improve){
	    if(performer.isRoyalSmith()){
	        if(improve.getNumber() == SkillList.SMITHING_ARMOUR_CHAIN
                    || improve.getNumber() == SkillList.SMITHING_ARMOUR_PLATE
                    || improve.getNumber() == SkillList.SMITHING_BLACKSMITHING
                    || improve.getNumber() == SkillList.SMITHING_GOLDSMITHING
                    || improve.getNumber() == SkillList.SMITHING_LOCKSMITHING
                    || improve.getNumber() == SkillList.SMITHING_METALLURGY
                    || improve.getNumber() == SkillList.SMITHING_SHIELDS
                    || improve.getNumber() == SkillList.SMITHING_WEAPON_BLADES
                    || improve.getNumber() == SkillList.SMITHING_WEAPON_HEADS){
	            return true;
            }
        }
	    return false;
    }

    public static int getNewFoodFill(float qlevel){
	    float startPercent = 0.004f;
	    float endPercent = 0.015f;
	    return (int) ((startPercent*(1f-qlevel/100f)+endPercent*(qlevel/100f))*65535);
    }

    public static boolean rollRarityImprove(Item source, int usedWeight){
	    int templateWeight = source.getTemplate().getWeightGrams();
	    float percentUsage = (float) usedWeight / (float) templateWeight;
	    float chance = percentUsage * 0.05f;
	    if(Server.rand.nextFloat() < chance){
	        return true;
        }
	    return false;
    }

    protected static final int rarityChance = 3600;
    protected static HashMap<Long,Integer> pseudoMap = new HashMap<>();
    public static boolean getRarityWindowChance(long wurmid){ //nextInt checks against 0. False is true, true is false.
        if(pseudoMap.containsKey(wurmid)){
            int currentChance = pseudoMap.get(wurmid);
            boolean success = Server.rand.nextInt(currentChance) == 0;
            if(success){
                pseudoMap.put(wurmid, currentChance+rarityChance-1);
            }else{
                pseudoMap.put(wurmid, currentChance-1);
            }
            return !success;
        }else{
            pseudoMap.put(wurmid, rarityChance-1);
            return !(Server.rand.nextInt(rarityChance) == 0);
        }
    }

    public static byte getNewCreationRarity(SimpleCreationEntry entry, Item source, Item target, ItemTemplate template){
        if(source.getRarity() > 0 || target.getRarity() > 0) {
            byte sRarity = source.getRarity();
            byte tRarity = target.getRarity();
            int sourceid = entry.getObjectSource();
            int targetid = entry.getObjectTarget();
            Item realSource = null;
            if(source.getTemplateId() == sourceid){
                realSource = source;
            }else if(target.getTemplateId() == sourceid){
                realSource = target;
            }
            Item realTarget = null;
            if(source.getTemplateId() == targetid){
                realTarget = source;
            }else if(target.getTemplateId() == targetid){
                realTarget = target;
            }
            if (entry.depleteSource && entry.depleteTarget) {
                int min = Math.min(sRarity, tRarity);
                int max = Math.max(sRarity, tRarity);
                return (byte) (min+Server.rand.nextInt(1+(max-min)));
            }
            if(realSource == null || realTarget == null){
                logger.info("Null source or target.");
                return 0;
            }
            if(entry.depleteSource && realSource.getRarity() > 0){
                int templateWeight = realSource.getTemplate().getWeightGrams();
                int usedWeight = entry.getSourceWeightToRemove(realSource, realTarget, template, false);
                float percentUsage = (float) usedWeight / (float) templateWeight;
                float chance = percentUsage * 0.05f;
                if(Server.rand.nextFloat() < chance){
                    return realSource.getRarity();
                }
            }else if(entry.depleteTarget && realTarget.getRarity() > 0){
                int templateWeight = realTarget.getTemplate().getWeightGrams();
                int usedWeight = entry.getTargetWeightToRemove(realSource, realTarget, template, false);
                float percentUsage = (float) usedWeight / (float) templateWeight;
                float chance = percentUsage * 0.05f;
                if(Server.rand.nextFloat() < chance){
                    return target.getRarity();
                }
            }
        }
        return 0;
    }

    public static Titles.Title[] cleanTitles(Titles.Title[] titles){
        ArrayList<Titles.Title> arrTitles = new ArrayList<>();
        for(Titles.Title title : titles){
            logger.info("Checking title "+title);
            if(title != null){
                logger.info("Title "+title.getName()+" is valid.");
                arrTitles.add(title);
            }else{
                logger.info("Title invalid. Discarding.");
            }
        }
        return arrTitles.toArray(new Titles.Title[0]);
    }

    public static boolean shouldSendBuff(SpellEffectsEnum effect){
        // Continue not showing any that don't have a buff in the first place
        if (!effect.isSendToBuffBar()){
            return false;
        }
        // Resistances and vulnerabilities are 20 - 43
        if (effect.getTypeId() <= 43 && effect.getTypeId() >= 20){
            return false;
        }

        // Is send to buff bar and not something we're stopping, so allow it.
        return true;
    }

	public static void preInit(){
		try{
			ClassPool classPool = HookManager.getInstance().getClassPool();
			final Class<MiscChanges> thisClass = MiscChanges.class;
			String replace;

			// - Create Server tab with initial messages - //
        	CtClass ctPlayers = classPool.get("com.wurmonline.server.Players");
        	if (WyvernMods.enableInfoTab) {
                CtMethod m = ctPlayers.getDeclaredMethod("sendStartGlobalKingdomChat");
                String infoTabTitle = WyvernMods.infoTabName;
                // Initial messages:
                //String[] infoTabLine = (String[]) WyvernMods.infoTabLines.toArray();
                /*String[] infoTabLine = {"Server Thread: https://forum.wurmonline.com/index.php?/topic/162067-revenant-modded-pvepvp-3x-action-new-skillgain/",
                        "Website/Maps: https://www.sarcasuals.com/",
                        "Server Discord: https://discord.gg/r8QNXAC",
                        "Server Data: https://docs.google.com/spreadsheets/d/1yjqTHoxUan4LIldI3jgrXZgXj1M2ENQ4MXniPUz0rE4",
                        "Server Wiki/Documentation: https://docs.google.com/document/d/1cbPi7-vZnjaiYrENhaefzjK_Wz7_F1CcPYJtC6uCi98/edit?usp=sharing",
                        "Patreon: https://www.patreon.com/sindusk"};*/
                StringBuilder str = new StringBuilder("{ com.wurmonline.server.Message mess;");
                for (String anInfoTabLine : WyvernMods.infoTabLines) {
                    str.append(" mess = new com.wurmonline.server.Message(player, (byte)16, \"").append(infoTabTitle).append("\",\"").append(anInfoTabLine).append("\", 0, 255, 0);").append("        player.getCommunicator().sendMessage(mess);");
                }
                str.append("}");
                m.insertAfter(str.toString());
            }

            // - Enable bridges to be built inside/over/through houses - //
            CtClass ctPlanBridgeChecks = classPool.get("com.wurmonline.server.structures.PlanBridgeChecks");
        	if (WyvernMods.ignoreBridgeChecks) {
        	    Util.setReason("Disable bridge construction checks.");
                replace = "{ return new com.wurmonline.server.structures.PlanBridgeCheckResult(false); }";
                Util.setBodyDeclared(thisClass, ctPlanBridgeChecks, "checkForBuildings", replace);
            }

            // - Disable mailboxes from being used while loaded - //
            CtClass ctItem = classPool.get("com.wurmonline.server.items.Item");
        	if (WyvernMods.disableMailboxUsageWhileLoaded) {
        	    Util.setReason("Disable mailbox usage while loaded.");
                replace = "$_ = $proceed($$);"
                        + "com.wurmonline.server.items.Item theTarget = com.wurmonline.server.Items.getItem(targetId);"
                        + "if(theTarget != null && theTarget.getTemplateId() >= 510 && theTarget.getTemplateId() <= 513){"
                        + "  if(theTarget.getTopParent() != theTarget.getWurmId()){"
                        + "    mover.getCommunicator().sendNormalServerMessage(\"Mailboxes cannot be used while loaded.\");"
                        + "    return false;"
                        + "  }"
                        + "}";
                Util.instrumentDeclared(thisClass, ctItem, "moveToItem", "getOwnerId", replace);
            }

            CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");

            // - Increase the amount of checks for new legendary creature to spawn - //
            CtClass ctServer = classPool.get("com.wurmonline.server.Server");
            if (WyvernMods.increasedLegendaryCreatures) {
                Util.setReason("Increase chances of a Legendary Creature spawning.");
                replace = "for(int i = 0; i < "+String.valueOf(WyvernMods.increasedLegendaryFrequency)+"; i++){"
                        + "  $_ = $proceed($$);"
                        + "}";
                Util.instrumentDeclared(thisClass, ctServer, "run", "checkDens", replace);
            }
            
            // - Add Facebreyker to the list of spawnable legendary creatures - //
            CtClass ctDens = classPool.get("com.wurmonline.server.zones.Dens");
            if (WyvernMods.allowFacebreykerNaturalSpawn) {
                Util.setReason("Add Facebreyker to the natural legendary spawn list.");
                replace = "com.wurmonline.server.zones.Dens.checkTemplate(2147483643, whileRunning);";
                Util.insertBeforeDeclared(thisClass, ctDens, "checkDens", replace);
            }

            // - Announce player titles in the Server tab - //
            CtClass ctPlayer = classPool.get("com.wurmonline.server.players.Player");
            if (WyvernMods.announcePlayerTitles) {
                Util.setReason("Announce player titles in the server tab.");
                replace = "$_ = $proceed($$);"
                        + "if(!com.wurmonline.server.Servers.localServer.PVPSERVER && this.getPower() < 1){"
                        + "  " + MiscChanges.class.getName() + ".sendServerTabMessage(\"event\", this.getName()+\" just earned the title of \"+title.getName(this.isNotFemale())+\"!\", 200, 100, 0);"
                        + "}";
                Util.instrumentDeclared(thisClass, ctPlayer, "addTitle", "sendNormalServerMessage", replace);
            }

            // - Make leather not suck even after it's able to be combined. - //
            CtClass ctMethodsItems = classPool.get("com.wurmonline.server.behaviours.MethodsItems");
            if (WyvernMods.improveCombinedLeather) {
                Util.setReason("Allow leather to improve beyond QL after being combinable.");
                replace = "if(com.wurmonline.server.behaviours.MethodsItems.getImproveTemplateId(target) != 72){"
                        + "  $_ = $proceed($$);"
                        + "}else{"
                        + "  $_ = false;"
                        + "}";
                Util.instrumentDeclared(thisClass, ctMethodsItems, "improveItem", "isCombine", replace);
            }
            
            // - Check new improve materials - //
            if (WyvernMods.allowModdedImproveTemplates) {
                Util.setReason("Enable modded improve templates.");
                replace = "int temp = " + ItemMod.class.getName() + ".getModdedImproveTemplateId($1);"
                        + "if(temp != -10){"
                        + "  return temp;"
                        + "}";
                Util.insertBeforeDeclared(thisClass, ctMethodsItems, "getImproveTemplateId", replace);
            }
            
            // - Remove fatiguing actions requiring you to be on the ground - //
            CtClass ctAction = classPool.get("com.wurmonline.server.behaviours.Action");
            if (WyvernMods.fatigueActionOverride) {
                CtConstructor[] ctActionConstructors = ctAction.getConstructors();
                for (CtConstructor constructor : ctActionConstructors) {
                    constructor.instrument(new ExprEditor() {
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
            }

            if (WyvernMods.fixPortalIssues) {
                Util.setReason("Fix Portal Issues.");
                CtClass ctPortal = classPool.get("com.wurmonline.server.questions.PortalQuestion");
                Util.instrumentDeclared(thisClass, ctPortal, "sendQuestion", "willLeaveServer", "$_ = true;");
                Util.setReason("Fix Portal Issues.");
                Util.instrumentDeclared(thisClass, ctPortal, "sendQuestion", "getKnowledge", "$_ = true;");
            }

            if (WyvernMods.disableMinimumShieldDamage) {
                Util.setReason("Disable the minimum 0.01 damage on shield damage, allowing damage modifiers to rule.");
                CtClass ctCombatHandler = classPool.get("com.wurmonline.server.creatures.CombatHandler");
                replace = "if($1 < 0.5f){"
                        + "  $_ = $proceed((float) 0, (float) $2);"
                        + "}else{"
                        + "  $_ = $proceed($$);"
                        + "}";
                Util.instrumentDeclared(thisClass, ctCombatHandler, "checkShield", "max", replace);
            }

            if (WyvernMods.disableGMEmoteLimit) {
                // - Allow GM's to bypass the 5 second emote sound limit. - //
                Util.setReason("Allow GM's to bypass the 5 second emote sound limit.");
                replace = "if(this.getPower() > 0){"
                        + "  return true;"
                        + "}";
                Util.insertBeforeDeclared(thisClass, ctPlayer, "mayEmote", replace);
            }

        	// - Make creatures wander slightly if they are shot from afar by an arrow - //
        	CtClass ctArrows = classPool.get("com.wurmonline.server.combat.Arrows");
            if (WyvernMods.creatureArcheryWander) {
                Util.setReason("Make creatures wander slightly when archered.");
                replace = "if(!defender.isPathing()){"
                        + "  defender.startPathing(com.wurmonline.server.Server.rand.nextInt(100));"
                        + "}"
                        + "$_ = $proceed($$);";
                Util.instrumentDeclared(thisClass, ctArrows, "addToHitCreature", "addAttacker", replace);
            }

            if (WyvernMods.globalDeathTabs) {
                Util.setReason("Broadcast death tabs to GL-Freedom.");
                replace = MiscChanges.class.getName() + ".broadCastDeaths($1, $2);";
                Util.insertBeforeDeclared(thisClass, ctPlayers, "broadCastDeathInfo", replace);
            }

            /* Disabled 1.9 - PvE Death Tabs are now part of vanilla.
            Util.setReason("Broadcast player death tabs always.");
            replace = MiscChanges.class.getName()+".broadCastDeathsPvE($0, $0.attackers);";
            Util.insertBeforeDeclared(thisClass, ctPlayer, "modifyRanking", replace);*/

            if (WyvernMods.disablePvPOnlyDeathTabs) {
                Util.setReason("Disable PvP only death tabs.");
                replace = "$_ = true;";
                Util.instrumentDeclared(thisClass, ctPlayers, "broadCastDeathInfo", "isThisAPvpServer", replace);
            }

            if (WyvernMods.fixLibilaCrossingIssues) {
                Util.setReason("Attempt to prevent libila from losing faith when crossing servers.");
                CtClass ctIntraServerConnection = classPool.get("com.wurmonline.server.intra.IntraServerConnection");
                ctIntraServerConnection.getDeclaredMethod("savePlayerToDisk").instrument(new ExprEditor() {
                    @Override
                    public void edit(FieldAccess fieldAccess) throws CannotCompileException {
                        if (Objects.equals("PVPSERVER", fieldAccess.getFieldName())) {
                            fieldAccess.replace("$_ = false;");
                            logger.info("Instrumented PVPSERVER = false for Libila faith transfers.");
                        }
                    }
                });
                ctIntraServerConnection.getDeclaredMethod("savePlayerToDisk").instrument(new ExprEditor() {
                    @Override
                    public void edit(FieldAccess fieldAccess) throws CannotCompileException {
                        if (Objects.equals("HOMESERVER", fieldAccess.getFieldName())) {
                            fieldAccess.replace("$_ = false;");
                            logger.info("Instrumented HOMESERVER = false for Libila faith transfers.");
                        }
                    }
                });
            }

            CtClass ctSkill = classPool.get("com.wurmonline.server.skills.Skill");
            CtClass[] params4 = {
                    CtClass.doubleType,
                    CtClass.booleanType,
                    CtClass.floatType,
                    CtClass.booleanType,
                    CtClass.doubleType
            };
            String desc4 = Descriptor.ofMethod(CtClass.voidType, params4);

            if (WyvernMods.higherFoodAffinities) {
                Util.setReason("Increase food affinity to give 30% increased skillgain instead of 10%.");
                replace = "int timedAffinity = (com.wurmonline.server.skills.AffinitiesTimed.isTimedAffinity(pid, this.getNumber()) ? 2 : 0);"
                        + "advanceMultiplicator *= (double)(1.0f + (float)timedAffinity * 0.1f);"
                        + "$_ = $proceed($$);";
                Util.instrumentDescribed(thisClass, ctSkill, "alterSkill", desc4, "hasSleepBonus", replace);
            }

        	CtClass[] params5 = {
        			CtClass.booleanType,
        			CtClass.booleanType,
        			CtClass.longType
        	};
        	String desc5 = Descriptor.ofMethod(CtClass.booleanType, params5);
        	if (WyvernMods.fasterCharcoalBurn) {
                Util.setReason("Double the rate at which charcoal piles produce items.");
                replace = "this.createDaleItems();"
                        + "decayed = this.setDamage(this.damage + 1.0f * this.getDamageModifier());"
                        + "$_ = $proceed($$);";
                Util.instrumentDescribed(thisClass, ctItem, "poll", desc5, "createDaleItems", replace);
            }

        	Util.setReason("Allow traders to display more than 9 items of a single type.");
            CtClass ctTradeHandler = classPool.get("com.wurmonline.server.creatures.TradeHandler");
            if (WyvernMods.uncapTraderItemCount) {
                ctTradeHandler.getDeclaredMethod("addItemsToTrade").instrument(new ExprEditor() {
                    public void edit(MethodCall m) throws CannotCompileException {
                        if (m.getMethodName().equals("size") && m.getLineNumber() > 200) { // I don't think the line number check matters, but I'm leaving it here anyway.
                            m.replace("$_ = 1;");
                            logger.info("Instrumented size for trades to allow traders to show more than 9 items at a time.");
                        }
                    }
                });
            }

        	// -- Identify players making over 10 commands per second and causing the server log message -- //
        	CtClass ctCommunicator = classPool.get("com.wurmonline.server.creatures.Communicator");
            if (WyvernMods.logExcessiveActions) {
                Util.setReason("Log excessive actions per second.");
                replace = "$_ = $proceed($$);"
                        + "if(this.player != null){"
                        + "  logger.info(\"Potential player macro: \"+this.player.getName()+\" [\"+this.commandsThisSecond+\" commands]\");"
                        + "}";
                Util.instrumentDeclared(thisClass, ctCommunicator, "reallyHandle_CMD_ITEM_CREATION_LIST", "log", replace);
            }
        	
        	//1f+0.5f*(1f-Math.pow(2, -Math.pow((eff-1f), pow1)/pow2))
        	/* Disabled in 1.9 - Fixed with Priest Update.
        	Util.setReason("Fix 100+ quality or power making certain interaction broken.");
        	replace = "{"
        			+ "double pow1 = 1.0;"
        			+ "double pow2 = 3.0;"
        			+ "double newEff = $1 >= 1.0 ? 1.0+0.5*(1.0-Math.pow(2.0, -Math.pow(($1-1.0), pow1)/pow2)) : Math.max(0.05, 1.0 - (1.0 - $1) * (1.0 - $1));"
        			+ "return newEff;"
        			+ "}";
        	Util.setBodyDeclared(thisClass, ctServer, "getBuffedQualityEffect", replace);*/

            // double advanceMultiplicator, boolean decay, float times, boolean useNewSystem, double skillDivider)
            CtClass[] params = {
                    CtClass.doubleType,
                    CtClass.booleanType,
                    CtClass.floatType,
                    CtClass.booleanType,
                    CtClass.doubleType
            };
            String desc = Descriptor.ofMethod(CtClass.voidType, params);

            if (WyvernMods.useDynamicSkillRate) {
                double minRate = 1.0D;
                double maxRate = 8.0D;
                double newPower = 2.5;

                Util.setReason("Adjust skill rate to a new, dynamic rate system.");
                replace = "double minRate = " + String.valueOf(minRate) + ";" +
                        "double maxRate = " + String.valueOf(maxRate) + ";" +
                        "double newPower = " + String.valueOf(newPower) + ";" +
                        "$1 = $1*(minRate+(maxRate-minRate)*Math.pow((100-this.knowledge)*0.01, newPower));";
                Util.insertBeforeDescribed(thisClass, ctSkill, "alterSkill", desc, replace);
            }

            if (WyvernMods.reduceLockpickBreaking) {
                Util.setReason("Reduce chance of lockpicks breaking.");
                replace = "$_ = 40f + $proceed($$);";
                Util.instrumentDeclared(thisClass, ctMethodsItems, "checkLockpickBreakage", "getCurrentQualityLevel", replace);
            }

            // Allow Freedom players to absorb mycelium
			CtClass ctTileBehaviour = classPool.get("com.wurmonline.server.behaviours.TileBehaviour");
			CtMethod[] ctGetBehavioursFors = ctTileBehaviour.getDeclaredMethods("getBehavioursFor");
			if (WyvernMods.allowFreedomMyceliumAbsorb) {
                for (CtMethod method : ctGetBehavioursFors) {
                    method.instrument(new ExprEditor() {
                        public void edit(MethodCall m) throws CannotCompileException {
                            if (m.getMethodName().equals("getKingdomTemplateId")) {
                                m.replace("$_ = 3;");
                            }
                        }
                    });
                }
            }

			CtClass ctMethodsStructure = classPool.get("com.wurmonline.server.behaviours.MethodsStructure");
			if (WyvernMods.largerHouses) {
                Util.setReason("Allow players to construct larger houses.");
                float carpentryMultiplier = 2f;
                replace = "if(!com.wurmonline.server.Servers.localServer.PVPSERVER){" +
                        "  $_ = $proceed($$)*" + String.valueOf(carpentryMultiplier) + ";" +
                        "}else{" +
                        "  $_ = $proceed($$);" +
                        "}";
                Util.instrumentDeclared(thisClass, ctMethodsStructure, "hasEnoughSkillToExpandStructure", "getKnowledge", replace);
                Util.setReason("Allow players to construct larger houses.");
                Util.instrumentDeclared(thisClass, ctMethodsStructure, "hasEnoughSkillToContractStructure", "getKnowledge", replace);
            }

            if (WyvernMods.reduceImbuePower) {
                Util.setReason("Reduce power of imbues.");
                replace = "$_ = Math.max(-80d, -80d+$2);";
                Util.instrumentDeclared(thisClass, ctMethodsItems, "smear", "max", replace);
            }

            if (WyvernMods.fixVehicleSpeeds) {
                Util.setReason("Update vehicle speeds reliably.");
                replace = "if($1 == 8){" +
                        "  $_ = 0;" +
                        "}else{" +
                        "  $_ = $proceed($$);" +
                        "}";
                Util.instrumentDeclared(thisClass, ctPlayer, "checkVehicleSpeeds", "nextInt", replace);
            }

            if (WyvernMods.reduceMailingCosts) {
                Util.setReason("Reduce mailing costs by 90%.");
                CtClass ctMailSendConfirmQuestion = classPool.get("com.wurmonline.server.questions.MailSendConfirmQuestion");
                replace = "$_ = $_ / 10;";
                Util.insertAfterDeclared(thisClass, ctMailSendConfirmQuestion, "getCostForItem", replace);
            }

            if (WyvernMods.guardTargetChanges) {
                Util.setReason("Remove guard tower guards helping against certain types of enemies.");
                CtClass ctGuardTower = classPool.get("com.wurmonline.server.kingdom.GuardTower");
                replace = "if($0.isUnique() || " + Titans.class.getName() + ".isTitan($0) || " + RareSpawns.class.getName() + ".isRareCreature($0)){" +
                        "  $_ = false;" +
                        "}else{" +
                        "  $_ = $proceed($$);" +
                        "}";
                Util.instrumentDeclared(thisClass, ctGuardTower, "alertGuards", "isWithinTileDistanceTo", replace);
            }

            // Enable Strongwall for Libila and other spells on PvE
            CtClass ctSpellGenerator = classPool.get("com.wurmonline.server.spells.SpellGenerator");
			if (WyvernMods.enableLibilaStrongwallPvE) {
                ctSpellGenerator.getDeclaredMethod("createSpells").instrument(new ExprEditor() {
                    @Override
                    public void edit(FieldAccess fieldAccess) throws CannotCompileException {
                        if (Objects.equals("PVPSERVER", fieldAccess.getFieldName()))
                            fieldAccess.replace("$_ = true;");
                        logger.info("Instrumented SpellGenerator PVPSERVER field to enable all spells.");
                    }
                });
            }

            CtClass ctTempStates = classPool.get("com.wurmonline.server.items.TempStates");

            if (WyvernMods.royalCookNoFoodDecay) {
                Util.setReason("Make heated food never decay if cooked by a royal cook.");
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
            }

            if (WyvernMods.mayorsCommandAbandonedVehicles) {
                Util.setReason("Allow mayors to command abandoned vehicles off their deed.");
                replace = "if(" + MiscChanges.class.getName() + ".checkMayorCommand($0, $1)){" +
                        "  return true;" +
                        "}";
                Util.insertBeforeDeclared(thisClass, ctItem, "mayCommand", replace);
            }

            CtClass ctAffinitiesTimed = classPool.get("com.wurmonline.server.skills.AffinitiesTimed");

            if (WyvernMods.opulenceFoodAffinityTimerIncrease) {
                Util.setReason("Add opulence bonus to food affinity timers.");
                replace = "$_ = " + MiscChanges.class.getName() + ".getFoodOpulenceBonus($0);";
                Util.instrumentDeclared(thisClass, ctAffinitiesTimed, "addTimedAffinityFromBonus", "getFoodComplexity", replace);
            }

            if (WyvernMods.disableFoodFirstBiteBonus) {
                Util.setReason("Food affinity timer normalization.");
                replace = "long time = " + WurmCalendar.class.getName() + ".getCurrentTime();" +
                        "if($0.getExpires($1) == null){" +
                        "  $_ = Long.valueOf(time);" +
                        "}else{" +
                        "  $_ = $proceed($$);" +
                        "}";
                Util.instrumentDeclared(thisClass, ctAffinitiesTimed, "add", "getExpires", replace);
            }

            if (WyvernMods.bedQualitySleepBonus) {
                Util.setReason("Make bed QL affect sleep bonus timer.");
                CtClass ctPlayerInfo = classPool.get("com.wurmonline.server.players.PlayerInfo");
                replace = "secs = " + MiscChanges.class.getName() + ".getBedBonus(secs, this.bed);" +
                        "$_ = $proceed($$);";
                Util.instrumentDeclared(thisClass, ctPlayerInfo, "calculateSleep", "setSleep", replace);
            }

            if (WyvernMods.royalSmithImproveFaster) {
                Util.setReason("Allow royal smith to improve smithing items faster.");
                replace = "if(" + MiscChanges.class.getName() + ".royalSmithImprove($1, improve)){" +
                        "  $_ = $proceed($$) * 0.9f;" +
                        "}else{" +
                        "  $_ = $proceed($$);" +
                        "}";
                Util.instrumentDeclared(thisClass, ctMethodsItems, "improveItem", "getImproveActionTime", replace);
                Util.setReason("Allow royal smith to improve smithing items faster.");
                Util.instrumentDeclared(thisClass, ctMethodsItems, "polishItem", "getImproveActionTime", replace);
                Util.setReason("Allow royal smith to improve smithing items faster. Also make tempering use water enchants.");
                replace = "if(" + MiscChanges.class.getName() + ".royalSmithImprove($1, improve)){" +
                        "  $_ = $proceed($1, target) * 0.9f;" +
                        "}else{" +
                        "  $_ = $proceed($1, target);" +
                        "}";
                Util.instrumentDeclared(thisClass, ctMethodsItems, "temper", "getImproveActionTime", replace);
            }

            // Fix for body strength not working properly when mounted. (Bdew)
            if (WyvernMods.fixMountedBodyStrength) {
                ctCreature.getMethod("getTraitMovePercent", "(Z)F").instrument(new ExprEditor() {
                    private boolean first = true;

                    @Override
                    public void edit(MethodCall m) throws CannotCompileException {
                        if (m.getMethodName().equals("getStrengthSkill")) {
                            if (first)
                                m.replace("wmod = wmod * 3D; $_ = $proceed() * (this.isUnicorn()?3D:2D);");
                            else
                                m.replace("$_ = $proceed() * (this.isUnicorn()?3D:2D);");
                            first = false;
                        }
                    }
                });
            }

            if (WyvernMods.adjustedFoodBiteFill) {
                Util.setReason("Modify food fill percent.");
                CtClass[] params12 = {
                        ctAction,
                        ctCreature,
                        ctItem,
                        CtClass.floatType
                };
                String desc12 = Descriptor.ofMethod(CtClass.booleanType, params12);
                replace = "$_ = $proceed($1, $2, $3, $4, " + MiscChanges.class.getName() + ".getNewFoodFill(qlevel));";
                Util.instrumentDescribed(thisClass, ctMethodsItems, "eat", desc12, "modifyHunger", replace);
            }

            // Fix for butchering not giving skill gain when butchering too many items
            /* Disabled in 1.9 - No longer necessary due to fishing changes.
            ctMethodsItems.getDeclaredMethod("filet").instrument(new ExprEditor() {
                private boolean first = true;

                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("skillCheck")) {
                        if (first) {
                            first = false;
                        }else {
                            m.replace("$_ = $proceed($1, $2, $3, false, $5);");
                            logger.info("Replaced filet skill check to ensure butchering skill is always gained.");
                        }
                    }
                }
            });*/

            // How to add a skill!
            /*CtClass ctSkillSystem = classPool.get("com.wurmonline.server.skills.SkillSystem");
            CtConstructor ctSkillSystemConstructor = ctSkillSystem.getClassInitializer();
            ctSkillSystemConstructor.insertAfter("com.wurmonline.server.skills.SkillSystem.addSkillTemplate(new "+SkillTemplate.class.getName()+"(10096,
                     \"Battle Yoyos\", 4000.0f, new int[]{1022}, 1209600000l, (short) 4, true, true));");*/

            if (WyvernMods.rareMaterialImprove) {
                Util.setReason("Hook for rare material usage in improvement.");
                replace = "if(" + MiscChanges.class.getName() + ".rollRarityImprove($0, usedWeight)){" +
                        "  rarity = source.getRarity();" +
                        "}" +
                        "$_ = $proceed($$);";
                Util.instrumentDeclared(thisClass, ctMethodsItems, "improveItem", "setWeight", replace);
            }

            if (WyvernMods.rarityWindowBadLuckProtection) {
                Util.setReason("Bad luck protection on rarity windows.");
                replace = "if($1 == 3600){" +
                        "  $_ = " + MiscChanges.class.getName() + ".getRarityWindowChance(this.getWurmId());" +
                        "}else{" +
                        "  $_ = $proceed($$);" +
                        "}";
                Util.instrumentDeclared(thisClass, ctPlayer, "poll", "nextInt", replace);
            }

            CtClass ctSimpleCreationEntry = classPool.get("com.wurmonline.server.items.SimpleCreationEntry");

            if (WyvernMods.rareCreationAdjustments) {
                ctSimpleCreationEntry.getDeclaredMethod("run").instrument(new ExprEditor() {
                    private boolean first = true;

                    @Override
                    public void edit(MethodCall m) throws CannotCompileException {
                        if (m.getMethodName().equals("getRarity")) {
                            if (first) {
                                m.replace("byte newRarity = " + MiscChanges.class.getName() + ".getNewCreationRarity(this, source, target, template);" +
                                        "if(newRarity > 0){" +
                                        "  act.setRarity(newRarity);" +
                                        "}" +
                                        "$_ = $proceed($$);");
                                logger.info("Replaced getRarity in SimpleCreationEntry to allow functional rare creations.");
                                first = false;
                            }
                        }
                    }
                });
            }

            if (WyvernMods.alwaysArmourTitleBenefits) {
                Util.setReason("Make armour title benefits always occur.");
                replace = "$_ = improve.getNumber();";
                Util.instrumentDeclared(thisClass, ctMethodsItems, "improveItem", "getSkillId", replace);
                Util.setReason("Make armour title benefits always occur.");
                Util.instrumentDeclared(thisClass, ctMethodsItems, "polishItem", "getSkillId", replace);
            }

            CtClass ctAbilities = classPool.get("com.wurmonline.server.players.Abilities");

            if (WyvernMods.tomeUsageAnyAltar) {
                Util.setReason("Make it so sorceries can be used anywhere with a flat 3x3 altar.");
                replace = "$_ = 1;";
                Util.instrumentDeclared(thisClass, ctAbilities, "isInProperLocation", "getTemplateId", replace);
            }

            if (WyvernMods.keyOfHeavensLoginOnly) {
                Util.setReason("Make the key of the heavens only usable on PvE");
                replace = "if($1.getTemplateId() == 794 && com.wurmonline.server.Servers.localServer.PVPSERVER){" +
                        "  $2.getCommunicator().sendNormalServerMessage(\"The \"+$1.getName()+\" must be used on the login server.\");" +
                        "  return false;" +
                        "}";
                Util.insertBeforeDeclared(thisClass, ctAbilities, "isInProperLocation", replace);
            }

            if (WyvernMods.lessFillingDrinks) {
                Util.setReason("Make drinks less filling.");
                CtClass[] params13 = {
                        ctAction,
                        ctCreature,
                        ctItem,
                        CtClass.floatType
                };
                String desc13 = Descriptor.ofMethod(CtClass.booleanType, params13);
                replace = "if(template != 128){" +
                        "  $_ = $proceed($1, $2, $3*5);" +
                        "}else{" +
                        "  $_ = $proceed($$);" +
                        "}";
                Util.instrumentDescribed(thisClass, ctMethodsItems, "drink", desc13, "sendActionControl", replace);
                replace = "if(template != 128){" +
                        "  $_ = $proceed($1/5, $2, $3, $4, $5);" +
                        "}else{" +
                        "  $_ = $proceed($$);" +
                        "}";
                Util.instrumentDescribed(thisClass, ctMethodsItems, "drink", desc13, "modifyThirst", replace);
            }

            if (WyvernMods.disableHelpGMCommands) {
                Util.setReason("Disable GM commands from displaying in /help unless the player is a GM.");
                CtClass ctServerTweaksHandler = classPool.get("com.wurmonline.server.ServerTweaksHandler");
                replace = "if($1.getPower() < 1){" +
                        "  return;" +
                        "}";
                Util.insertBeforeDeclared(thisClass, ctServerTweaksHandler, "sendHelp", replace);
            }

            if (WyvernMods.reduceActionInterruptOnDamage) {
                Util.setReason("Make damage less likely to interrupt actions during combat.");
                replace = "$1 = $1/2;";
                Util.insertBeforeDeclared(thisClass, ctCreature, "maybeInterruptAction", replace);
            }

            if (WyvernMods.fixMissionNullPointerException) {
                Util.setReason("Fix mission null pointer exception.");
                CtClass ctEpicServerStatus = classPool.get("com.wurmonline.server.epic.EpicServerStatus");
                replace = "if(itemplates.size() < 1){" +
                        "  com.wurmonline.server.epic.EpicServerStatus.setupMissionItemTemplates();" +
                        "}";
                Util.insertBeforeDeclared(thisClass, ctEpicServerStatus, "getRandomItemTemplateUsed", replace);
            }

            if (WyvernMods.disableSmeltingPots) {
                Util.setReason("Disable smelting pots from being used.");
                CtClass ctItemBehaviour = classPool.get("com.wurmonline.server.behaviours.ItemBehaviour");
                CtClass[] params14 = {
                        ctAction,
                        ctCreature,
                        ctItem,
                        ctItem,
                        CtClass.shortType,
                        CtClass.floatType
                };
                String desc14 = Descriptor.ofMethod(CtClass.booleanType, params14);
                replace = "if($5 == 519){" +
                        "  $2.getCommunicator().sendNormalServerMessage(\"Smelting is disabled.\");" +
                        "  return true;" +
                        "}";
                Util.insertBeforeDescribed(thisClass, ctItemBehaviour, "action", desc14, replace);
            }

            if (WyvernMods.hideSorceryBuffBar) {
                Util.setReason("Hide buff bar icons for sorceries.");
                CtClass ctSpellEffectsEnum = classPool.get("com.wurmonline.server.creatures.SpellEffectsEnum");
                CtClass ctString = classPool.get("java.lang.String");
                CtClass[] params15 = {
                        ctSpellEffectsEnum,
                        CtClass.intType,
                        ctString
                };
                String desc15 = Descriptor.ofMethod(CtClass.voidType, params15);
                CtClass[] params16 = {
                        ctSpellEffectsEnum,
                        CtClass.intType
                };
                String desc16 = Descriptor.ofMethod(CtClass.voidType, params16);
                replace = "$_ = " + MiscChanges.class.getName() + ".shouldSendBuff($0);";
                Util.instrumentDescribed(thisClass, ctCommunicator, "sendAddStatusEffect", desc15, "isSendToBuffBar", replace);
                Util.setReason("Hide buff bar icons for sorceries.");
                Util.instrumentDescribed(thisClass, ctCommunicator, "sendAddStatusEffect", desc16, "isSendToBuffBar", replace);
            }

            // 1.9 Achievement fix [Bdew]
            if (WyvernMods.sqlAchievementFix) {
                classPool.getCtClass("com.wurmonline.server.players.Achievements").getMethod("loadAllAchievements", "()V")
                        .instrument(new ExprEditor() {
                            @Override
                            public void edit(MethodCall m) throws CannotCompileException {
                                if (m.getMethodName().equals("getTimestamp"))
                                    m.replace("$_=com.wurmonline.server.utils.DbUtilities.getTimestampOrNull(rs.getString($1)); " +
                                            "if ($_==null) $_=new java.sql.Timestamp(java.lang.System.currentTimeMillis());");
                            }
                        });
            }

        } catch (CannotCompileException | NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        }
	}
}
