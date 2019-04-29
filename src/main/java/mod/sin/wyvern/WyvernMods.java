package mod.sin.wyvern;

import com.wurmonline.server.Message;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.*;
import com.wurmonline.server.players.Player;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import mod.sin.actions.*;
import mod.sin.actions.items.SorcerySplitAction;
import mod.sin.creatures.*;
import mod.sin.creatures.titans.*;
import mod.sin.lib.Prop;
import mod.sin.lib.Util;
import mod.sin.wyvern.bestiary.MethodsBestiary;
import mod.sin.wyvern.mastercraft.Mastercraft;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import org.gotti.wurmunlimited.modsupport.creatures.ModCreatures;
import org.gotti.wurmunlimited.modsupport.vehicles.ModVehicleBehaviours;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

public class WyvernMods
implements WurmServerMod, Configurable, PreInitable, Initable, ItemTemplatesCreatedListener, ServerStartedListener, ServerPollListener, PlayerLoginListener, ChannelMessageListener {
	private static Logger logger = Logger.getLogger(WyvernMods.class.getName());
	public static boolean espCounter = false;
	public static boolean enableDepots = false;

	// Miscellaneous Changes Module Configuration
	public static boolean enableMiscChangesModule = true;
	public static boolean enableInfoTab = true;
	public static String infoTabName = "Server";
	public static ArrayList<String> infoTabLines = new ArrayList<>();
	public static boolean ignoreBridgeChecks = true;
	public static boolean disableMailboxUsageWhileLoaded = true;
	public static boolean increasedLegendaryCreatures = true;
	public static int increasedLegendaryFrequency = 5;
	public static boolean allowFacebreykerNaturalSpawn = true;
	public static boolean announcePlayerTitles = true;
	public static boolean improveCombinedLeather = true;
	public static boolean allowModdedImproveTemplates = true;
	public static boolean fatigueActionOverride = true;
	public static boolean fixPortalIssues = true;
	public static boolean disableMinimumShieldDamage = true;
	public static boolean disableGMEmoteLimit = true;
	public static boolean creatureArcheryWander = true;
	public static boolean globalDeathTabs = true;
	public static boolean disablePvPOnlyDeathTabs = true;
	public static boolean fixLibilaCrossingIssues = true;
	public static boolean higherFoodAffinities = true;
	public static boolean fasterCharcoalBurn = true;
	public static boolean uncapTraderItemCount = true;
	public static boolean logExcessiveActions = true;
	public static boolean useDynamicSkillRate = true;
	public static boolean reduceLockpickBreaking = true;
	public static boolean allowFreedomMyceliumAbsorb = true;
	public static boolean largerHouses = true;
	public static boolean reduceImbuePower = true;
	public static boolean fixVehicleSpeeds = true;
	public static boolean reduceMailingCosts = true;
	public static boolean guardTargetChanges = true;
	public static boolean enableLibilaStrongwallPvE = true;
	public static boolean royalCookNoFoodDecay = true;
	public static boolean mayorsCommandAbandonedVehicles = true;
	public static boolean opulenceFoodAffinityTimerIncrease = true;
	public static boolean disableFoodFirstBiteBonus = true;
	public static boolean bedQualitySleepBonus = true;
	public static boolean royalSmithImproveFaster = true;
	public static boolean fixMountedBodyStrength = true;
	public static boolean adjustedFoodBiteFill = true;
	public static boolean rareMaterialImprove = true;
	public static boolean rarityWindowBadLuckProtection = true;
	public static boolean rareCreationAdjustments = true;
	public static boolean alwaysArmourTitleBenefits = true;
	public static boolean tomeUsageAnyAltar = true;
	public static boolean keyOfHeavensLoginOnly = true;
	public static boolean lessFillingDrinks = true;
	public static boolean disableHelpGMCommands = true;
	public static boolean reduceActionInterruptOnDamage = true;
	public static boolean fixMissionNullPointerException = true;
	public static boolean disableSmeltingPots = true;
	public static boolean hideSorceryBuffBar = true;
	public static boolean sqlAchievementFix = true;

	// Arena Module Configuration
	public static boolean enableArenaModule = true;
	public static boolean equipHorseGearByLeading = true;
	public static boolean lockpickingImprovements = true;
	public static boolean placeDeedsOutsideKingdomInfluence = true;
	public static boolean disablePMKs = true;
	public static boolean disablePlayerChampions = true;
	public static boolean arenaAggression = true;
	public static boolean enemyTitleHook = true;
	public static boolean enemyPresenceOnAggression = true;
	public static boolean disableFarwalkerItems = true;
	public static boolean alwaysAllowAffinitySteal = true;
	public static boolean adjustFightSkillGain = true;
	public static boolean useAggressionForNearbyEnemies = true;
	public static boolean disablePvPCorpseProtection = true;
	public static boolean bypassHousePermissions = true;
	public static boolean allowStealingAgainstDeityWishes = true;
	public static boolean sameKingdomVehicleTheft = true;
	public static boolean adjustMineDoorDamage = true;
	public static boolean sameKingdomPermissionsAdjustments = true;
	public static boolean disableCAHelpOnPvP = true;
	public static boolean sameKingdomVillageWarfare = true;
	public static boolean adjustHotARewards = true;
	public static boolean capMaximumGuards = true;
	public static boolean disableTowerConstruction = true;
	public static boolean adjustLocalRange = true;
	public static boolean disableKarmaTeleport = true;
	public static boolean limitLeadCreatures = true;
	public static boolean adjustBashTimer = true;
	public static boolean discordRelayHotAMessages = true;
	public static boolean allowAttackingSameKingdomGuards = true;
	public static boolean fixGuardsAttackingThemselves = true;
	public static boolean reducedMineDoorOpenTime = true;
	public static boolean allowSameKingdomFightSkillGains = true;
	public static boolean allowArcheringOnSameKingdomDeeds = true;
	public static boolean sendNewSpawnQuestionOnPvP = true;
	public static boolean sendArtifactDigsToDiscord = true;
	public static boolean makeFreedomFavoredKingdom = true;
	public static boolean crownInfluenceOnAggression = true;
	public static boolean disableOWFL = true;
	public static boolean resurrectionStonesProtectSkill = true;
	public static boolean resurrectionStonesProtectFightSkill = true;
	public static boolean resurrectionStonesProtectAffinities = true;
	public static boolean bypassPlantedPermissionChecks = true;

	// Custom Titles Module Configuration
	public static boolean enableCustomTitlesModule = true;
	public static ArrayList<CustomTitle> customTitles = new ArrayList<>();
	public static HashMap<Integer,ArrayList<String>> awardTitles = new HashMap<>();

	// Anti-Cheat Module Configuration
	public static boolean enableAntiCheatModule = true;
	public static boolean enableSpoofHiddenOre = true;
	public static boolean prospectingVision = true;
	public static boolean mapSteamIds = true;

	// Quality Of Life Module Configuration
	public static boolean enableQualityOfLifeModule = true;
	public static boolean mineCaveToVehicle = true;
	public static boolean mineSurfaceToVehicle = true;
	public static boolean chopLogsToVehicle = true;
	public static boolean statuetteAnyMaterial = true;
	public static boolean mineGemsToVehicle = true;
	public static boolean regenerateStaminaOnVehicleAnySlope = true;

	// Treasure Chest Loot Module Configuration
	public static boolean enableTreasureChestLootModule = true;

	public static class CustomTitle{
		protected int titleId;
		protected String maleTitle;
		protected String femaleTitle;
		protected int skillId;
		protected String type;
		public CustomTitle(int titleId, String maleTitle, String femaleTitle, int skillId, String type){
			this.titleId = titleId;
			this.maleTitle = maleTitle;
			this.femaleTitle = femaleTitle;
			this.skillId = skillId;
			if (type.equals("NORMAL") || type.equals("MINOR") || type.equals("MASTER") || type.equals("LEGENDARY")){
				this.type = type;
			}else{
				logger.info("Failed to register proper custom title type \""+type+"\" for title ID "+titleId+". Defaulting to \"NORMAL\"");
				this.type = "NORMAL";
			}
		}
		public int getTitleId(){
			return titleId;
		}
		public String getMaleTitle(){
			return maleTitle;
		}
		public String getFemaleTitle(){
			return femaleTitle;
		}
		public int getSkillId(){
			return skillId;
		}
		public String getType(){
			return type;
		}
	}
    
    public static boolean customCommandHandler(ByteBuffer byteBuffer, Player player) throws UnsupportedEncodingException{
    	byte[] tempStringArr = new byte[byteBuffer.get() & 255];
        byteBuffer.get(tempStringArr);
        String message = new String(tempStringArr, "UTF-8");
        tempStringArr = new byte[byteBuffer.get() & 255];
        byteBuffer.get(tempStringArr);
        //String title = new String(tempStringArr, "UTF-8");
        if(player.mayMute() && message.startsWith("!")){
    		logger.info("Player "+player.getName()+" used custom WyvernMods command: "+message);
    		if(message.startsWith("!toggleESP") && player.getPower() >= 5){
                espCounter = !espCounter;
                player.getCommunicator().sendSafeServerMessage("ESP counter for this server is now = "+espCounter);
    		}else if(message.startsWith("!toggleDepots") && player.getPower() >= 5){
                enableDepots = !enableDepots;
                player.getCommunicator().sendSafeServerMessage("Arena depots for this server is now = "+enableDepots);
    		}else{
    			player.getCommunicator().sendSafeServerMessage("Custom command not found: "+message);
    		}
    		return true;
        }
        return false;
    }

    public void configure(Properties properties) {
    	Prop.properties = properties;

    	// -- Configuration Setting -- //

		// Miscellaneous Changes Module
		enableMiscChangesModule = Prop.getBooleanProperty("enableMiscChangesModule", enableMiscChangesModule);
		enableInfoTab = Prop.getBooleanProperty("enableInfoTab", enableInfoTab);
		infoTabName = Prop.getStringProperty("infoTabName", infoTabName);
		ignoreBridgeChecks = Prop.getBooleanProperty("ignoreBridgeChecks", ignoreBridgeChecks);
		disableMailboxUsageWhileLoaded = Prop.getBooleanProperty("disableMailboxUsageWhileLoaded", disableMailboxUsageWhileLoaded);
		increasedLegendaryCreatures = Prop.getBooleanProperty("increasedLegendaryCreatures", increasedLegendaryCreatures);
		increasedLegendaryFrequency = Prop.getIntegerProperty("increasedLegendaryFrequency", increasedLegendaryFrequency);
		allowFacebreykerNaturalSpawn = Prop.getBooleanProperty("allowFacebreykerNaturalSpawn", allowFacebreykerNaturalSpawn);
		announcePlayerTitles = Prop.getBooleanProperty("announcePlayerTitles", announcePlayerTitles);
		improveCombinedLeather = Prop.getBooleanProperty("improveCombinedLeather", improveCombinedLeather);
		allowModdedImproveTemplates = Prop.getBooleanProperty("allowModdedImproveTemplates", allowModdedImproveTemplates);
		fatigueActionOverride = Prop.getBooleanProperty("fatigueActionOverride", fatigueActionOverride);
		fixPortalIssues = Prop.getBooleanProperty("fixPortalIssues", fixPortalIssues);
		disableMinimumShieldDamage = Prop.getBooleanProperty("disableMinimumShieldDamage", disableMinimumShieldDamage);
		disableGMEmoteLimit = Prop.getBooleanProperty("disableGMEmoteLimit", disableGMEmoteLimit);
		creatureArcheryWander = Prop.getBooleanProperty("creatureArcheryWander", creatureArcheryWander);
		globalDeathTabs = Prop.getBooleanProperty("globalDeathTabs", globalDeathTabs);
		disablePvPOnlyDeathTabs = Prop.getBooleanProperty("disablePvPOnlyDeathTabs", disablePvPOnlyDeathTabs);
		fixLibilaCrossingIssues = Prop.getBooleanProperty("fixLibilaCrossingIssues", fixLibilaCrossingIssues);
		higherFoodAffinities = Prop.getBooleanProperty("higherFoodAffinities", higherFoodAffinities);
		fasterCharcoalBurn = Prop.getBooleanProperty("fasterCharcoalBurn", fasterCharcoalBurn);
		uncapTraderItemCount = Prop.getBooleanProperty("uncapTraderItemCount", uncapTraderItemCount);
		logExcessiveActions = Prop.getBooleanProperty("logExcessiveActions", logExcessiveActions);
		useDynamicSkillRate = Prop.getBooleanProperty("useDynamicSkillRate", useDynamicSkillRate);
		reduceLockpickBreaking = Prop.getBooleanProperty("reduceLockpickBreaking", reduceLockpickBreaking);
		allowFreedomMyceliumAbsorb = Prop.getBooleanProperty("allowFreedomMyceliumAbsorb", allowFreedomMyceliumAbsorb);
		largerHouses = Prop.getBooleanProperty("largerHouses", largerHouses);
		reduceImbuePower = Prop.getBooleanProperty("reduceImbuePower", reduceImbuePower);
		fixVehicleSpeeds = Prop.getBooleanProperty("fixVehicleSpeeds", fixVehicleSpeeds);
		reduceMailingCosts = Prop.getBooleanProperty("reduceMailingCosts", reduceMailingCosts);
		guardTargetChanges = Prop.getBooleanProperty("guardTargetChanges", guardTargetChanges);
		enableLibilaStrongwallPvE = Prop.getBooleanProperty("enableLibilaStrongwallPvE", enableLibilaStrongwallPvE);
		royalCookNoFoodDecay = Prop.getBooleanProperty("royalCookNoFoodDecay", royalCookNoFoodDecay);
		mayorsCommandAbandonedVehicles = Prop.getBooleanProperty("mayorsCommandAbandonedVehicles", mayorsCommandAbandonedVehicles);
		opulenceFoodAffinityTimerIncrease = Prop.getBooleanProperty("opulenceFoodAffinityTimerIncrease", opulenceFoodAffinityTimerIncrease);
		disableFoodFirstBiteBonus = Prop.getBooleanProperty("disableFoodFirstBiteBonus", disableFoodFirstBiteBonus);
		bedQualitySleepBonus = Prop.getBooleanProperty("bedQualitySleepBonus", bedQualitySleepBonus);
		royalSmithImproveFaster = Prop.getBooleanProperty("royalSmithImproveFaster", royalSmithImproveFaster);
		fixMountedBodyStrength = Prop.getBooleanProperty("fixMountedBodyStrength", fixMountedBodyStrength);
		adjustedFoodBiteFill = Prop.getBooleanProperty("adjustedFoodBiteFill", adjustedFoodBiteFill);
		rareMaterialImprove = Prop.getBooleanProperty("rareMaterialImprove", rareMaterialImprove);
		rarityWindowBadLuckProtection = Prop.getBooleanProperty("rarityWindowBadLuckProtection", rarityWindowBadLuckProtection);
		rareCreationAdjustments = Prop.getBooleanProperty("rareCreationAdjustments", rareCreationAdjustments);
		alwaysArmourTitleBenefits = Prop.getBooleanProperty("alwaysArmourTitleBenefits", alwaysArmourTitleBenefits);
		tomeUsageAnyAltar = Prop.getBooleanProperty("tomeUsageAnyAltar", tomeUsageAnyAltar);
		keyOfHeavensLoginOnly = Prop.getBooleanProperty("keyOfHeavensLoginOnly", keyOfHeavensLoginOnly);
		lessFillingDrinks = Prop.getBooleanProperty("lessFillingDrinks", lessFillingDrinks);
		disableHelpGMCommands = Prop.getBooleanProperty("disableHelpGMCommands", disableHelpGMCommands);
		reduceActionInterruptOnDamage = Prop.getBooleanProperty("reduceActionInterruptOnDamage", reduceActionInterruptOnDamage);
		fixMissionNullPointerException = Prop.getBooleanProperty("fixMissionNullPointerException", fixMissionNullPointerException);
		disableSmeltingPots = Prop.getBooleanProperty("disableSmeltingPots", disableSmeltingPots);
		hideSorceryBuffBar = Prop.getBooleanProperty("hideSorceryBuffBar", hideSorceryBuffBar);
		sqlAchievementFix = Prop.getBooleanProperty("sqlAchievementFix", sqlAchievementFix);

		// Arena Module
		enableArenaModule = Prop.getBooleanProperty("enableArenaModule", enableArenaModule);
		equipHorseGearByLeading = Prop.getBooleanProperty("equipHorseGearByLeading", equipHorseGearByLeading);
		lockpickingImprovements = Prop.getBooleanProperty("lockpickingImprovements", lockpickingImprovements);
		placeDeedsOutsideKingdomInfluence = Prop.getBooleanProperty("placeDeedsOutsideKingdomInfluence", placeDeedsOutsideKingdomInfluence);
		disablePMKs = Prop.getBooleanProperty("disablePMKs", disablePMKs);
		disablePlayerChampions = Prop.getBooleanProperty("disablePlayerChampions", disablePlayerChampions);
		arenaAggression = Prop.getBooleanProperty("arenaAggression", arenaAggression);
		enemyTitleHook = Prop.getBooleanProperty("enemyTitleHook", enemyTitleHook);
		enemyPresenceOnAggression = Prop.getBooleanProperty("enemyPresenceOnAggression", enemyPresenceOnAggression);
		disableFarwalkerItems = Prop.getBooleanProperty("disableFarwalkerItems", disableFarwalkerItems);
		alwaysAllowAffinitySteal = Prop.getBooleanProperty("alwaysAllowAffinitySteal", alwaysAllowAffinitySteal);
		adjustFightSkillGain = Prop.getBooleanProperty("adjustFightSkillGain", adjustFightSkillGain);
		useAggressionForNearbyEnemies = Prop.getBooleanProperty("useAggressionForNearbyEnemies", useAggressionForNearbyEnemies);
		disablePvPCorpseProtection = Prop.getBooleanProperty("disablePvPCorpseProtection", disablePvPCorpseProtection);
		bypassHousePermissions = Prop.getBooleanProperty("bypassHousePermissions", bypassHousePermissions);
		allowStealingAgainstDeityWishes = Prop.getBooleanProperty("allowStealingAgainstDeityWishes", allowStealingAgainstDeityWishes);
		sameKingdomVehicleTheft = Prop.getBooleanProperty("sameKingdomVehicleTheft", sameKingdomVehicleTheft);
		adjustMineDoorDamage = Prop.getBooleanProperty("adjustMineDoorDamage", adjustMineDoorDamage);
		sameKingdomPermissionsAdjustments = Prop.getBooleanProperty("sameKingdomPermissionsAdjustments", sameKingdomPermissionsAdjustments);
		disableCAHelpOnPvP = Prop.getBooleanProperty("disableCAHelpOnPvP", disableCAHelpOnPvP);
		sameKingdomVillageWarfare = Prop.getBooleanProperty("sameKingdomVillageWarfare", sameKingdomVillageWarfare);
		adjustHotARewards = Prop.getBooleanProperty("adjustHotARewards", adjustHotARewards);
		capMaximumGuards = Prop.getBooleanProperty("capMaximumGuards", capMaximumGuards);
		disableTowerConstruction = Prop.getBooleanProperty("disableTowerConstruction", disableTowerConstruction);
		adjustLocalRange = Prop.getBooleanProperty("adjustLocalRange", adjustLocalRange);
		disableKarmaTeleport = Prop.getBooleanProperty("disableKarmaTeleport", disableKarmaTeleport);
		limitLeadCreatures = Prop.getBooleanProperty("limitLeadCreatures", limitLeadCreatures);
		adjustBashTimer = Prop.getBooleanProperty("adjustBashTimer", adjustBashTimer);
		discordRelayHotAMessages = Prop.getBooleanProperty("discordRelayHotAMessages", discordRelayHotAMessages);
		allowAttackingSameKingdomGuards = Prop.getBooleanProperty("allowAttackingSameKingdomGuards", allowAttackingSameKingdomGuards);
		fixGuardsAttackingThemselves = Prop.getBooleanProperty("fixGuardsAttackingThemselves", fixGuardsAttackingThemselves);
		reducedMineDoorOpenTime = Prop.getBooleanProperty("reducedMineDoorOpenTime", reducedMineDoorOpenTime);
		allowSameKingdomFightSkillGains = Prop.getBooleanProperty("allowSameKingdomFightSkillGains", allowSameKingdomFightSkillGains);
		allowArcheringOnSameKingdomDeeds = Prop.getBooleanProperty("allowArcheringOnSameKingdomDeeds", allowArcheringOnSameKingdomDeeds);
		sendNewSpawnQuestionOnPvP = Prop.getBooleanProperty("sendNewSpawnQuestionOnPvP", sendNewSpawnQuestionOnPvP);
		sendArtifactDigsToDiscord = Prop.getBooleanProperty("sendArtifactDigsToDiscord", sendArtifactDigsToDiscord);
		makeFreedomFavoredKingdom = Prop.getBooleanProperty("makeFreedomFavoredKingdom", makeFreedomFavoredKingdom);
		crownInfluenceOnAggression = Prop.getBooleanProperty("crownInfluenceOnAggression", crownInfluenceOnAggression);
		disableOWFL = Prop.getBooleanProperty("disableOWFL", disableOWFL);
		resurrectionStonesProtectSkill = Prop.getBooleanProperty("resurrectionStonesProtectSkill", resurrectionStonesProtectSkill);
		resurrectionStonesProtectFightSkill = Prop.getBooleanProperty("resurrectionStonesProtectFightSkill", resurrectionStonesProtectFightSkill);
		resurrectionStonesProtectAffinities = Prop.getBooleanProperty("resurrectionStonesProtectAffinities", resurrectionStonesProtectAffinities);
		bypassPlantedPermissionChecks = Prop.getBooleanProperty("bypassPlantedPermissionChecks", bypassPlantedPermissionChecks);

		// Custom Titles Module
		enableCustomTitlesModule = Prop.getBooleanProperty("enableCustomTitlesModule", enableCustomTitlesModule);

		// Anti-Cheat Module
		enableAntiCheatModule = Prop.getBooleanProperty("enableAntiCheatModule", enableAntiCheatModule);
		enableSpoofHiddenOre = Prop.getBooleanProperty("enableSpoofHiddenOre", enableSpoofHiddenOre);
		prospectingVision = Prop.getBooleanProperty("prospectingVision", prospectingVision);
		mapSteamIds = Prop.getBooleanProperty("mapSteamIds", mapSteamIds);

		// Quality Of Life Module
		enableQualityOfLifeModule = Prop.getBooleanProperty("enableQualityOfLifeModule", enableQualityOfLifeModule);
		mineCaveToVehicle = Prop.getBooleanProperty("mineCaveToVehicle", mineCaveToVehicle);
		mineSurfaceToVehicle = Prop.getBooleanProperty("mineSurfaceToVehicle", mineSurfaceToVehicle);
		chopLogsToVehicle = Prop.getBooleanProperty("chopLogsToVehicle", chopLogsToVehicle);
		statuetteAnyMaterial = Prop.getBooleanProperty("statuetteAnyMaterial", statuetteAnyMaterial);
		mineGemsToVehicle = Prop.getBooleanProperty("mineGemsToVehicle", mineGemsToVehicle);
		regenerateStaminaOnVehicleAnySlope = Prop.getBooleanProperty("regenerateStaminaOnVehicleAnySlope", regenerateStaminaOnVehicleAnySlope);

    	// Treasure Chest Loot Module
		enableTreasureChestLootModule = Prop.getBooleanProperty("enableTreasureChestLootModule", enableTreasureChestLootModule);

		// Multiple-option Configuration Parsing
		// This handles all the configurations that allow multiple different configurations to be applied.
		for (String name : properties.stringPropertyNames()) {
			try {
				String value = properties.getProperty(name);
				switch (name) {
					case "debug":
					case "classname":
					case "classpath":
					case "sharedClassLoader":
					case "depend.requires":
					case "depend.import":
					case "depend.suggests":
						break; //ignore
					default:
						if (name.startsWith("infoTabLine")) {
							infoTabLines.add(value);
						}else if (name.startsWith("addCustomTitle")) {
							String[] values = value.split(",");
							if(values.length > 5 || values.length < 5){
								logger.warning("Error parsing Custom Title: Invalid amount of arguments for following property: "+value);
							}
							int titleId = Integer.valueOf(values[0]);
							String maleTitle = values[1];
							String femaleTitle = values[2];
							int skillId = Integer.valueOf(values[3]);
							String titleType = values[4];
							customTitles.add(new CustomTitle(titleId, maleTitle, femaleTitle, skillId, titleType));
						}else if (name.startsWith("awardTitle")) {
							String[] values = value.split(",");
							if(values.length < 2){
								logger.warning("Error parsing Award Title: Invalid amount of arguments for following property: "+value);
							}
							int titleId = Integer.valueOf(values[0]);
							ArrayList<String> playerList;
							if (awardTitles.containsKey(titleId)){
								// Has an entry already, add to the existing list.
								playerList = awardTitles.get(titleId);
							}else{
								// No entry, should create a new array and add it to the map
								playerList = new ArrayList<>();
							}
							for (int i = 1; i < values.length; i++){
								if(playerList.contains(values[i])){
									logger.warning("Duplicate player entry for single title: "+values[i]+" for title "+titleId+".");
								}else{
									playerList.add(values[i]);
								}
							}
							awardTitles.put(titleId, playerList);
						}
				}
			} catch (Exception e) {
				logger.severe("Error processing property " + name);
				e.printStackTrace();
			}
		}

		// -- Configuration Print -- //
		logger.info("Miscellaneous Changes Module: "+enableMiscChangesModule);
		if(enableMiscChangesModule) {
			logger.info("Information Tab: " + enableInfoTab);
			if (enableInfoTab) {
				logger.info("> Information Tab Name: " + infoTabName);
				for (String tabLine : infoTabLines) {
					logger.info("> Information Tab Line: " + tabLine);
				}
			}
			logger.info("Ignore Bridge Checks: " + ignoreBridgeChecks);
			logger.info("Disable Mailbox Usage While Loaded: " + disableMailboxUsageWhileLoaded);
			logger.info("Increased Legendary Creatures: " + increasedLegendaryCreatures);
			logger.info("Increased Legendary Frequency: " + increasedLegendaryFrequency + "x");
			logger.info("Allow Facebreyker Natural Spawn: " + allowFacebreykerNaturalSpawn);
			logger.info("Announce Player Titles: " + announcePlayerTitles);
			logger.info("Improve Combined Leather: " + improveCombinedLeather);
			logger.info("Allow Modded Improve Templates: " + allowModdedImproveTemplates);
			logger.info("Fatigue Action Override: " + fatigueActionOverride);
			logger.info("Fix Portal Issues: " + fixPortalIssues);
			logger.info("Disable Minimum Shield Damage: " + disableMinimumShieldDamage);
			logger.info("Disable GM Emote Limit: " + disableGMEmoteLimit);
			logger.info("Creature Archery Wander: " + creatureArcheryWander);
			logger.info("Global Death Tabs: " + globalDeathTabs);
			logger.info("Disable PvP Only Death Tabs: " + disablePvPOnlyDeathTabs);
			logger.info("Fix Libila Crossing Issues: " + fixLibilaCrossingIssues);
			logger.info("Higher Food Affinities: " + higherFoodAffinities);
			logger.info("Faster Charcoal Burn: " + fasterCharcoalBurn);
			logger.info("Uncap Trader Item Count: " + uncapTraderItemCount);
			logger.info("Log Excessive Actions: " + logExcessiveActions);
			logger.info("Use Dynamic Skill Rate: " + useDynamicSkillRate);
			logger.info("Reduce Lockpick Breaking: " + reduceLockpickBreaking);
			logger.info("Allow Freedom Mycelium Absorb: " + allowFreedomMyceliumAbsorb);
			logger.info("Larger Houses: " + largerHouses);
			logger.info("Reduce Imbue Power: " + reduceImbuePower);
			logger.info("Fix Vehicle Speeds: " + fixVehicleSpeeds);
			logger.info("Reduce Mailing Costs: " + reduceMailingCosts);
			logger.info("Guard Target Changes: " + guardTargetChanges);
			logger.info("Enable Libila Strongwall on PvE: " + enableLibilaStrongwallPvE);
			logger.info("Royal Cook No Food Decay: " + royalCookNoFoodDecay);
			logger.info("Mayors Command Abandoned Vehicles: " + mayorsCommandAbandonedVehicles);
			logger.info("Opulence Food Affinity Timer Increase: " + opulenceFoodAffinityTimerIncrease);
			logger.info("Disable Food First Bite Bonus: " + disableFoodFirstBiteBonus);
			logger.info("Bed Quality Sleep Bonus: " + bedQualitySleepBonus);
			logger.info("Royal Smith Improve Faster: " + royalSmithImproveFaster);
			logger.info("Fix Mounted Body Strength: " + fixMountedBodyStrength);
			logger.info("Adjusted Food Bite Fill: " + adjustedFoodBiteFill);
			logger.info("Rare Material Improve: " + rareMaterialImprove);
			logger.info("Rarity Window Bad Luck Protection: " + rarityWindowBadLuckProtection);
			logger.info("Rare Creation Adjustments: " + rareCreationAdjustments);
			logger.info("Always Armour Title Benefits: " + alwaysArmourTitleBenefits);
			logger.info("Tome Usage Any Altar: " + tomeUsageAnyAltar);
			logger.info("Key Of Heavens Login Only: " + keyOfHeavensLoginOnly);
			logger.info("Less Filling Drinks: " + lessFillingDrinks);
			logger.info("Disable Help GM Commands: " + disableHelpGMCommands);
			logger.info("Reduce Action Interrupt On Damage: " + reduceActionInterruptOnDamage);
			logger.info("Fix Mission Null Pointer Exception: " + fixMissionNullPointerException);
			logger.info("Disable Smelting Pots: " + disableSmeltingPots);
			logger.info("Hide Sorcery Buff Bar: " + hideSorceryBuffBar);
			logger.info("SQL Achievement Fix: " + sqlAchievementFix);
		}

		logger.info("Arena Module: "+enableArenaModule);
		if(enableArenaModule) {
			logger.info("Equip Horse Gear By Leading: " + equipHorseGearByLeading);
			logger.info("Lockpicking Improvements: "+lockpickingImprovements);
			logger.info("Place Deeds Outside Kingdom Influence: "+placeDeedsOutsideKingdomInfluence);
			logger.info("Disable PMK's: "+disablePMKs);
			logger.info("Disable Player Champions: "+disablePlayerChampions);
			logger.info("Arena Aggression: "+arenaAggression);
			logger.info("Enemy Title Hook: "+enemyTitleHook);
			logger.info("Enemy Presence On Aggression: "+enemyPresenceOnAggression);
			logger.info("Disable Farwalker Items: "+disableFarwalkerItems);
			logger.info("Always Allow Affinity Steal: "+alwaysAllowAffinitySteal);
			logger.info("Adjust Fight Skill Gain: "+adjustFightSkillGain);
			logger.info("Use Aggression For Nearby Enemies: "+useAggressionForNearbyEnemies);
			logger.info("Disable PvP Corpse Protection: "+disablePvPCorpseProtection);
			logger.info("Bypass House Permissions: "+bypassHousePermissions);
			logger.info("Allow Stealing Against Deity Wishes: "+allowStealingAgainstDeityWishes);
			logger.info("Same Kingdom Vehicle Theft: "+sameKingdomVehicleTheft);
			logger.info("Adjust Mine Door Damage: "+adjustMineDoorDamage);
			logger.info("Same Kingdom Permission Adjustments: "+sameKingdomPermissionsAdjustments);
			logger.info("Disable CA Help On PvP: "+disableCAHelpOnPvP);
			logger.info("Same Kingdom Village Warfare: "+sameKingdomVillageWarfare);
			logger.info("Adjust HotA Rewards: "+adjustHotARewards);
			logger.info("Cap Maximum Guards: "+capMaximumGuards);
			logger.info("Disable Tower Construction: "+disableTowerConstruction);
			logger.info("Adjust Local Range: "+adjustLocalRange);
			logger.info("Disable Karma Teleport: "+disableKarmaTeleport);
			logger.info("Limit Lead Creatures: "+limitLeadCreatures);
			logger.info("Adjust Bash Timer: "+adjustBashTimer);
			logger.info("Discord Relay HotA Messages: "+discordRelayHotAMessages);
			logger.info("Allow Attacking Same Kingdom Guards: "+allowAttackingSameKingdomGuards);
			logger.info("Fix Guards Attacking Themselves: "+fixGuardsAttackingThemselves);
			logger.info("Reduced Mine Door Open Time: "+reducedMineDoorOpenTime);
			logger.info("Allow Same Kingdom Fight Skill Gains: "+allowSameKingdomFightSkillGains);
			logger.info("Allow Archering On Same Kingdom Deeds: "+allowArcheringOnSameKingdomDeeds);
			logger.info("Send New Spawn Question On PvP: "+sendNewSpawnQuestionOnPvP);
			logger.info("Send Artifact Digs To Discord: "+sendArtifactDigsToDiscord);
			logger.info("Make Freedom Favored Kingdom: "+makeFreedomFavoredKingdom);
			logger.info("Crown Influence On Aggression: "+crownInfluenceOnAggression);
			logger.info("Disable Open World Full Loot: "+disableOWFL);
			logger.info("Resurrection Stones Protect Skill: "+resurrectionStonesProtectSkill);
			logger.info("Resurrection Stones Protect Fight Skill: "+resurrectionStonesProtectFightSkill);
			logger.info("Resurrection Stones Protect Affinities: "+resurrectionStonesProtectAffinities);
			logger.info("Bypass Planted Permission Checks: "+bypassPlantedPermissionChecks);
		}

		logger.info("Custom Titles Module: "+enableCustomTitlesModule);
		if (enableCustomTitlesModule){
			for (CustomTitle title : customTitles){
				logger.info(String.format("Custom Title ID #%d: %s / %s for skill ID #%d of type %s.",
						title.getTitleId(), title.getMaleTitle(), title.getFemaleTitle(), title.getSkillId(), title.getType()));
			}
			for (int titleId : awardTitles.keySet()){
				logger.info(String.format("Awarding Title ID #%d to players: %s",
						titleId, awardTitles.get(titleId).toString()));
			}
		}

		logger.info("Anti-Cheat Module: "+enableAntiCheatModule);
		if (enableAntiCheatModule){
			logger.info("Spoof Hidden Ore: "+enableSpoofHiddenOre);
			if (enableSpoofHiddenOre){
				logger.info("Prospecting Vision: "+prospectingVision);
			}
			logger.info("Map Steam IDs: "+mapSteamIds);
		}

		logger.info("Quality Of Life Module: "+enableQualityOfLifeModule);
		if (enableQualityOfLifeModule){
			logger.info("Mine Cave To Vehicle: "+mineCaveToVehicle);
			logger.info("Mine Surface To Vehicle: "+mineSurfaceToVehicle);
			logger.info("Chop Logs To Vehicle: "+chopLogsToVehicle);
			logger.info("Statuette Any Material: "+statuetteAnyMaterial);
			logger.info("Mine Gems To Vehicle: "+mineGemsToVehicle);
			logger.info("Regenerate Stamina On Vehicle Any Slope: "+regenerateStaminaOnVehicleAnySlope);
		}

		logger.info("Treasure Chest Loot Module: "+enableTreasureChestLootModule);

        //this.logger.log(Level.INFO, "Property: " + this.somevalue);
    }

    public static void handleExamine(Creature performer, Item target) {
        // Im just not a smart man.
        /*if(target.isContainerLiquid()){
            boolean found = false;
            for(Item i : Items.getAllItems()){
                if(i == target){
                    found = true;
                }
            }
            if(found){
                logger.info("Item exists!");
            }else{
                logger.info("Item not found.");
            }
        }*/
    }

    public void preInit() {
    	logger.info("Pre-Initializing.");
        try {
        	ModActions.init(); // Initialize ModActions from Modloader

			// Misc Changes Module Pre-Init
			if (enableMiscChangesModule) {
				MiscChanges.preInit();
			}

			// Arena Module Pre-Init
			if (enableArenaModule) {
				Arena.preInit();
			}

			// Custom Titles Module Pre-Init
			if (enableCustomTitlesModule) {
				PlayerTitles.preInit();
			}

			// Anti-Cheat Module Pre-Init
			if (enableAntiCheatModule) {
				AntiCheat.preInit();
			}

			// Quality Of Life Module Pre-Init
			if (enableQualityOfLifeModule) {
				QualityOfLife.preInit();
			}

			// Treasure Chest Loot Module Pre-Init
			if (enableTreasureChestLootModule) {
				TreasureChests.preInit();
			}

            Titans.preInit();
            RareSpawns.preInit();
            TeleportHandler.preInit();
            MethodsBestiary.preInit();
            MissionCreator.preInit();
            SkillChanges.preInit();
            MeditationPerks.preInit();
            MountedChanges.preInit();
            EconomicChanges.preInit();
            Bloodlust.preInit();
            Mastercraft.preInit();
            SupplyDepots.preInit();
            KeyEvent.preInit();
            CombatChanges.preInit();

			// Gem Augmentation is not complete.
            //GemAugmentation.preInit();

            Class<WyvernMods> thisClass = WyvernMods.class;
			ClassPool classPool = HookManager.getInstance().getClassPool();

            Util.setReason("Insert examine method.");
            CtClass ctItemBehaviour = classPool.get("com.wurmonline.server.behaviours.ItemBehaviour");
            String replace = WyvernMods.class.getName() + ".handleExamine($2, $3);";
            Util.insertAfterDeclared(thisClass, ctItemBehaviour, "examine", replace);

            // - Enable custom command handler - //
			CtClass ctCommunicator = classPool.get("com.wurmonline.server.creatures.Communicator");
        	ctCommunicator.getDeclaredMethod("reallyHandle").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("reallyHandle_CMD_MESSAGE")) {
                        m.replace("java.nio.ByteBuffer tempBuffer = $1.duplicate();"
                        		+ "if(!mod.sin.wyvern.WyvernMods.customCommandHandler($1, this.player)){"
                        		+ "  $_ = $proceed(tempBuffer);"
                        		+ "}");
                    }
                }
            });
        } catch (CannotCompileException | NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        }
    }
    
	@Override
	public void init() {
		logger.info("Initializing.");
		if (WyvernMods.enableCustomTitlesModule) {
			PlayerTitles.init();
		}
		ModCreatures.init();
		ModVehicleBehaviours.init();
		
		// Vanilla:
		logger.info("Registering Vanilla creature changes.");
		ModCreatures.addCreature(new Bison());
		
		// Epic:
		logger.info("Registering Epic creatures.");
		ModCreatures.addCreature(new LavaFiend());
		ModCreatures.addCreature(new SolDemon());
		ModCreatures.addCreature(new Worg());
		
		// Wyverns:
        logger.info("Registering Wyverns.");
        ModCreatures.addCreature(new WyvernBlack());
        ModCreatures.addCreature(new WyvernGreen());
        ModCreatures.addCreature(new WyvernRed());
        ModCreatures.addCreature(new WyvernWhite());
        ModCreatures.addCreature(new WyvernBlue());
		
		// Flavor Mobs:
        logger.info("Registering Flavor creatures.");
		ModCreatures.addCreature(new Avenger());
        ModCreatures.addCreature(new FireCrab());
		ModCreatures.addCreature(new ForestSpider());
		ModCreatures.addCreature(new Giant());
        ModCreatures.addCreature(new Charger());
        ModCreatures.addCreature(new HornedPony());
		ModCreatures.addCreature(new LargeBoar());
		ModCreatures.addCreature(new SpiritTroll());

		// Event Mobs:
        logger.info("Registering Event creatures.");
        ModCreatures.addCreature(new IceCat());
        ModCreatures.addCreature(new FireGiant());
        ModCreatures.addCreature(new GuardianMagranon());
		
		// Bosses:
		logger.info("Registering Custom Boss creatures.");
		ModCreatures.addCreature(new Reaper());
		ModCreatures.addCreature(new SpectralDrake());
		// Uniques:
		ModCreatures.addCreature(new Facebreyker());
		
		// Titans:
		ModCreatures.addCreature(new Ifrit());
		ModCreatures.addCreature(new Lilith());
		// Titan Spawns:
		ModCreatures.addCreature(new IfritFiend());
		ModCreatures.addCreature(new IfritSpider());
		ModCreatures.addCreature(new LilithWraith());
		ModCreatures.addCreature(new LilithZombie());
		
		// NPC's
		logger.info("Registering Custom NPC creatures.");
		//ModCreatures.addCreature(new RobZombie());
		//ModCreatures.addCreature(new MacroSlayer());
        ModCreatures.addCreature(new Terror());
		
		Bounty.init();
		
		Mastercraft.changeExistingTitles();
	}

	@Override
	public void onItemTemplatesCreated() {
		logger.info("Creating Item Mod items.");
		ItemMod.createItems();
		logger.info("Creating Cache items.");
		Caches.createItems();
		logger.info("Initiating Title changes.");
		//PlayerTitles.onItemTemplatesCreated();
		try {
			logger.info("Editing existing item templates.");
			ItemMod.modifyItems();
			logger.info("Registering permissions hook for custom items.");
			ItemMod.registerPermissionsHook();
		} catch (NoSuchTemplateException | IllegalArgumentException | IllegalAccessException | ClassCastException | NoSuchFieldException e) {
			e.printStackTrace();
		}
	}

	@Override
    public void onPlayerLogin(Player p) {
        DatabaseHelper.onPlayerLogin(p);
        PlayerTitles.awardCustomTitles(p);
    }

	@Override
	public void onServerStarted() {
		try {
			logger.info("Registering Item Mod creation entries.");
			ItemMod.initCreationEntries();
			logger.info("Registering Item Mod actions.");
			ItemMod.registerActions();
			logger.info("Registering Cache actions.");
			Caches.registerActions();
			logger.info("Registering Soulstealer actions.");
			Soulstealing.registerActions();
			logger.info("Registering Custom actions.");
			ModActions.registerAction(new UnequipAllAction());
			ModActions.registerAction(new ReceiveMailAction());
			ModActions.registerAction(new LeaderboardAction());
			ModActions.registerAction(new AddSubGroupAction());
			ModActions.registerAction(new SorcerySplitAction());
			ModActions.registerAction(new LeaderboardSkillAction());
			logger.info("Registering Arena actions.");
			ModActions.registerAction(new SorceryCombineAction());
			//ModActions.registerAction(new VillageTeleportAction()); // [3/28/18] Disabled - Highway Portals added instead.
			ModActions.registerAction(new ArenaTeleportAction());
			ModActions.registerAction(new ArenaEscapeAction());
			logger.info("Registering Dev actions.");
			ModActions.registerAction(new MissionAddAction());
			ModActions.registerAction(new MissionRemoveAction());
			ModActions.registerAction(new CreatureReportAction());
			ModActions.registerAction(new SmoothTerrainAction());
			logger.info("Setting custom creature corpse models.");
			MethodsBestiary.setTemplateVariables();
			logger.info("Setting up Achievement templates.");
			AchievementChanges.onServerStarted();

			DeityChanges.onServerStarted();
			
			//espCounter = Servers.localServer.PVPSERVER; // Enables on PvP server by default.
			//espCounter = false;

            SkillChanges.onServerStarted();

			CreationEntry lockpicks = CreationMatrix.getInstance().getCreationEntry(ItemList.lockpick);
			try {
				ReflectionUtil.setPrivateField(lockpicks, ReflectionUtil.getField(lockpicks.getClass(), "hasMinimumSkillRequirement"), false);
				ReflectionUtil.setPrivateField(lockpicks, ReflectionUtil.getField(lockpicks.getClass(), "minimumSkill"), 0.0);
			} catch (IllegalAccessException | NoSuchFieldException e) {
				logger.info("Failed to set lockpick creation entry changes!");
				e.printStackTrace();
			}

		} catch (IllegalArgumentException | ClassCastException e) {
			e.printStackTrace();
		}
		DatabaseHelper.onServerStarted();
	}
	
	public static long lastSecondPolled = 0;
	public static long lastPolledTitanSpawn = 0;
	public static final long pollTitanSpawnTime = TimeConstants.MINUTE_MILLIS*2;
	public static long lastPolledTitans = 0;
	public static final long pollTitanTime = TimeConstants.SECOND_MILLIS;
	public static long lastPolledDepots = 0;
	public static final long pollDepotTime = TimeConstants.MINUTE_MILLIS;
	public static long lastPolledRareSpawns = 0;
	public static final long pollRareSpawnTime = TimeConstants.MINUTE_MILLIS*5;
	public static long lastPolledEternalReservoirs = 0;
	public static final long pollEternalReservoirTime = TimeConstants.MINUTE_MILLIS*10;
	public static long lastPolledMissionCreator = 0;
	public static final long pollMissionCreatorTime = TimeConstants.HOUR_MILLIS*4;
    public static long lastPolledBloodlust = 0;
    public static final long pollBloodlustTime = TimeConstants.MINUTE_MILLIS;
    public static long lastPolledUniqueRegeneration = 0;
    public static final long pollUniqueRegenerationTime = TimeConstants.SECOND_MILLIS;
    public static long lastPolledUniqueCollection = 0;
    public static final long pollUniqueCollectionTime = TimeConstants.MINUTE_MILLIS*5;
    public static long lastPolledTerrainSmooth = 0;
    public static final long pollTerrainSmoothTime = TimeConstants.SECOND_MILLIS*5;
	@Override
	public void onServerPoll() {
		if((lastSecondPolled + TimeConstants.SECOND_MILLIS) < System.currentTimeMillis()){
			if(lastPolledDepots + pollDepotTime < System.currentTimeMillis()){
				SupplyDepots.pollDepotSpawn();
				lastPolledDepots += pollDepotTime;
			}
			if(lastPolledTitanSpawn + pollTitanSpawnTime < System.currentTimeMillis()){
				Titans.pollTitanSpawn();
				lastPolledTitanSpawn += pollTitanSpawnTime;
			}
			if(lastPolledTitans + pollTitanTime < System.currentTimeMillis()){
				Titans.pollTitans();
				lastPolledTitans += pollTitanTime;
			}
			if(lastPolledRareSpawns + pollRareSpawnTime < System.currentTimeMillis()){
			    RareSpawns.pollRareSpawns();
			    lastPolledRareSpawns += pollRareSpawnTime;
            }
			if(lastPolledEternalReservoirs + pollEternalReservoirTime < System.currentTimeMillis()){
				Soulstealing.pollSoulForges();
				lastPolledEternalReservoirs += pollEternalReservoirTime;
			}
			if(lastPolledMissionCreator + pollMissionCreatorTime < System.currentTimeMillis()){
				MissionCreator.pollMissions();
				lastPolledMissionCreator += pollMissionCreatorTime;
			}
            if(lastPolledBloodlust + pollBloodlustTime < System.currentTimeMillis()){
                Bloodlust.pollLusts();
                lastPolledBloodlust += pollBloodlustTime;
            }
            if(lastPolledUniqueRegeneration + pollUniqueRegenerationTime < System.currentTimeMillis()){
                CombatChanges.pollUniqueRegeneration();
                lastPolledUniqueRegeneration += pollUniqueRegenerationTime;
            }
            if(lastPolledUniqueCollection + pollUniqueCollectionTime < System.currentTimeMillis()){
                CombatChanges.pollUniqueCollection();
                lastPolledUniqueCollection += pollUniqueCollectionTime;
            }
            if(lastPolledTerrainSmooth + pollTerrainSmoothTime < System.currentTimeMillis()){
                SmoothTerrainAction.onServerPoll();
                lastPolledTerrainSmooth += pollTerrainSmoothTime;
            }
			
			// Update counter
			if(lastSecondPolled + TimeConstants.SECOND_MILLIS*10 > System.currentTimeMillis()){
				lastSecondPolled += TimeConstants.SECOND_MILLIS;
			}else{
				logger.info("Time between last poll was greater than 10 seconds. Resetting all poll counters...");
				lastSecondPolled = System.currentTimeMillis();
				lastPolledTitanSpawn = System.currentTimeMillis();
				lastPolledTitans = System.currentTimeMillis();
				lastPolledDepots = System.currentTimeMillis();
				lastPolledRareSpawns = System.currentTimeMillis();
				lastPolledEternalReservoirs = System.currentTimeMillis();
				lastPolledMissionCreator = System.currentTimeMillis();
				lastPolledBloodlust = System.currentTimeMillis();
				lastPolledUniqueRegeneration = System.currentTimeMillis();
				lastPolledUniqueCollection = System.currentTimeMillis();
				lastPolledTerrainSmooth = System.currentTimeMillis();
			}
		}
	}

	@Override
	public MessagePolicy onKingdomMessage(Message message) {
		String window = message.getWindow();
		if(window.startsWith("GL-Freedom") && KeyEvent.isActive()){
			KeyEvent.handlePlayerMessage(message);
		}

		return MessagePolicy.PASS;
	}

}