package mod.sin.wyvern;

import com.wurmonline.server.Message;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NoSuchTemplateException;
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
import mod.sin.lib.SkillAssist;
import mod.sin.lib.Util;
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
	public static boolean changePumpkinKingTitle = true;
	public static boolean changeDeityPassives = true;

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

	// Combat Module Configuration
	public static boolean enableCombatModule = true;
	public static boolean enableCombatRatingAdjustments = true;
	public static boolean royalExecutionerBonus = true;
	public static boolean petSoulDepthScaling = true;
	public static boolean vehicleCombatRatingPenalty = true;
	public static boolean fixMagranonDamageStacking = true;
	public static boolean adjustCombatRatingSpellPower = true;
	public static boolean disableLegendaryRegeneration = true;
	public static boolean useStaticLegendaryRegeneration = true;

	// Mastercraft Module Configuration
	public static boolean enableMastercraftModule = true;
	public static boolean enableDifficultyAdjustments = true;
	public static boolean affinityDifficultyBonus = true;
	public static boolean legendDifficultyBonus = true;
	public static boolean masterDifficultyBonus = true;
	public static boolean itemRarityDifficultyBonus = true;
	public static boolean legendItemDifficultyBonus = true;
	public static boolean masterItemDifficultyBonus = true;
	public static boolean empoweredChannelers = true;
	public static boolean channelSkillFavorReduction = true;

	// Skill Module Configuration
	public static boolean enableSkillModule = true;
	public static boolean enableHybridSkillGain = true;
	public static float hybridNegativeDecayRate = 5f;
	public static float hybridPositiveDecayRate = 3f;
	public static float hybridValueAtZero = 3.74f;
	public static float hybridValueAtOneHundred = 0.9f;
	public static HashMap<Integer,String> skillName = new HashMap<>();
	public static HashMap<Integer,Float> skillDifficulty = new HashMap<>();
	public static HashMap<Integer,Long> skillTickTime = new HashMap<>();
	public static boolean changePreachingLocation = true;

	// Meditation Module Configuration
	public static boolean enableMeditationModule = true;
	public static boolean simplifyMeditationTerrain = true;
	public static boolean removeInsanitySotG = true;
	public static boolean removeHateWarBonus = true;
	public static boolean insanitySpeedBonus = true;
	public static boolean hateMovementBonus = true;
	public static boolean scalingPowerStaminaBonus = true;
	public static boolean scalingKnowledgeSkillGain = true;
	public static boolean removeMeditationTickTimer = true;
	public static boolean newMeditationBuffs = true;
	public static boolean enableMeditationAbilityCooldowns = true;
	public static long loveRefreshCooldown = 64800000L; // 18 hours default
	public static long loveEnchantNatureCooldown = 64800000L; // 18 hours default
	public static long loveLoveEffectCooldown = 64800000L; // 18 hours default
	public static long hateWarDamageCooldown = 64800000L; // 18 hours default
	public static long hateStructureDamageCooldown = 64800000L; // 18 hours default
	public static long hateFearCooldown = 64800000L; // 18 hours default
	public static long powerElementalImmunityCooldown = 64800000L; // 18 hours default
	public static long powerEruptFreezeCooldown = 64800000L; // 18 hours default
	public static long powerIgnoreTrapsCooldown = 64800000L; // 18 hours default
	public static long knowledgeInfoCreatureCooldown = 64800000L; // 18 hours default
	public static long knowledgeInfoTileCooldown = 64800000L; // 18 hours default

	// Titan Module Configuration
	public static boolean enableTitanModule = true;
	public static boolean disableTitanNaturalRegeneration = true;
	public static long pollTitanSpawnTime = TimeConstants.MINUTE_MILLIS*2;
	public static long pollTitanTime = TimeConstants.SECOND_MILLIS;
	public static long titanRespawnTime = TimeConstants.HOUR_MILLIS*80L;

	// Rare Spawn Module Configuration
	public static boolean enableRareSpawnModule = true;
	public static long pollRareSpawnTime = TimeConstants.MINUTE_MILLIS*5;

	// Mission Module Configuration
	public static boolean enableMissionModule = true;
	public static boolean enableNewMissionCreator = true;
	public static long pollMissionCreatorTime = TimeConstants.HOUR_MILLIS*4;
	public static boolean useValreiEntities = true;
	public static boolean addMissionCurrencyReward = true;
	public static boolean preventMissionOceanSpawns = true;
	public static boolean additionalHerbivoreChecks = true;
	public static boolean additionalMissionSlayableChecks = true;
	public static boolean disableEpicMissionTypes = true;

	// Mounted Module Configuration
	public static boolean enableMountedModule = true;
	public static boolean newMountSpeedScaling = true;
	public static boolean updateMountSpeedOnDamage = true;
	public static boolean allowBisonMounts = true;

	// Teleport Module Configuration
	public static boolean enableTeleportModule = true;
	public static boolean useArenaTeleportMethod = true;

	// Economic Module Configuration
	public static boolean enableEconomyModule = true;
	public static boolean adjustSealedMapValue = true;
	public static boolean disableTraderRefill = true;
	public static boolean voidTraderMoney = true;

	// Supply Depot Module Configuration
	public static boolean enableSupplyDepotModule = true;
	public static boolean useSupplyDepotLights = true;
	public static long pollDepotTime = TimeConstants.MINUTE_MILLIS;
	public static long captureMessageInterval = TimeConstants.MINUTE_MILLIS*3L;
	public static long depotRespawnTime = TimeConstants.HOUR_MILLIS*11L;

	// Bestiary Module Configuration
	public static boolean enableBestiaryModule = true;
	public static boolean fixSacrificingStrongCreatures = true;
	public static boolean disableAfkTraining = true;
	public static boolean fixChargersWalkingThroughWalls = true;
	public static boolean conditionWildCreatures = true;
	public static boolean allowGhostArchery = true;
	public static boolean disableArcheryOnStrongCreatures = true;
	public static boolean genesisEnchantedGrassNewborns = true;
	public static boolean useCustomCorpseSizes = true;
	public static boolean allowCustomCreatureBreedNames = true;
	public static boolean allowGhostBreeding = true;
	public static boolean allowGhostCorpses = true;
	public static boolean useCustomCreatureSizes = true;
	public static boolean useCustomCreatureSFX = true;
	public static boolean preventLegendaryHitching = true;
	public static boolean modifyNewCreatures = true;
	public static boolean logCreatureSpawns = true;
	public static boolean allowEpicCreatureNaturalSpawns = true;
	public static boolean enableCustomCreatures = true;
	public static boolean enableWyverns = true;
	public static boolean enableFlavorMobs = true;
	public static boolean enableEventMobs = true;
	public static boolean enableRareSpawns = true;
	public static boolean enableCustomLegendaries = true;
	public static boolean enableTitans = true;

	// Bounty Module Configuration
	public static boolean enableBountyModule = true;
	public static boolean usePlayerBounty = true;
	public static boolean useLootTable = true;

	// Item Module Configuration
	public static boolean enableItemModule = true;
	public static boolean combineLeather = true;
	public static boolean combineLogs = true;
	public static boolean reduceLogVolume = true;
	public static boolean reduceKindlingVolume = true;
	public static boolean droppableSleepPowder = true;
	public static boolean setSilverMirrorPrice = true;
	public static boolean setGoldMirrorPrice = true;
	public static boolean setCreatureCratePrice = true;
	public static boolean setResurrectionStonePrice = true;
	public static boolean setShakerOrbPrice = true;
	public static boolean loadableMailbox = true;
	public static boolean loadableBellTower = true;
	public static boolean loadableTrashBin = true;
	public static boolean loadableAltars = true;
	public static boolean oneHandedLongSpear = true;
	public static boolean reduceDirtDifficulty = true;
	public static boolean reduceSandDifficulty = true;
	public static boolean reduceSandstoneDifficulty = true;
	public static boolean decorationStoneKeystone = true;
	public static boolean decorationMarbleKeystone = true;
	public static boolean decorationSkull = true;
	public static boolean useCustomCacheFragments = true;
	public static boolean adjustStatueFragmentCount = true;
	public static boolean removeLockpickSkillRequirement = true;
	public static boolean createCustomItemTemplates = true;
	public static boolean enableCustomItemCreation = true;
	public static boolean craftHuntingArrowPacks = true;
	public static boolean craftWarArrowPacks = true;
	public static boolean craftBattleYoyo = true;
	public static boolean craftClub = true;
	public static boolean craftDepthDrill = true;
	public static boolean craftEternalReservoir = true;
	public static boolean craftEviscerator = true;
	public static boolean craftKnuckles = true;
	public static boolean craftMassStorageUnit = true;
	public static boolean craftStatuetteDeities = true;
	public static boolean craftWarhammer = true;

	// Soulstealing Module Configuration
	public static boolean enableSoulstealingModule = true;
	public static long pollEternalReservoirTime = TimeConstants.MINUTE_MILLIS*10;

	// Action Module Configuration
	public static boolean enableActionModule = true;
	public static boolean actionUnequipAll = true;
	public static boolean actionReceiveAllMail = true;
	public static boolean actionSplitSorcery = true;
	public static boolean actionLeaderboard = true;
	public static boolean actionSorceryFragmentCombine = true;
	public static boolean actionArenaTeleports = true;
	public static boolean actionAddMissionDev = true;
	public static boolean actionRemoveMissionDev = true;
	public static boolean actionCreatureReportDev = true;
	public static boolean actionSmoothTerrainDev = true;

	// Erosion Module Configuration
    public static boolean enableErosionModule = true;
    public static long pollTerrainSmoothTime = TimeConstants.SECOND_MILLIS*5;

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
		changePumpkinKingTitle = Prop.getBooleanProperty("changePumpkinKingTitle", changePumpkinKingTitle);
        changeDeityPassives = Prop.getBooleanProperty("changeDeityPassives", changeDeityPassives);

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

		// Combat Module
		enableCombatModule = Prop.getBooleanProperty("enableCombatModule", enableCombatModule);
		enableCombatRatingAdjustments = Prop.getBooleanProperty("enableCombatRatingAdjustments", enableCombatRatingAdjustments);
		royalExecutionerBonus = Prop.getBooleanProperty("royalExecutionerBonus", royalExecutionerBonus);
		petSoulDepthScaling = Prop.getBooleanProperty("petSoulDepthScaling", petSoulDepthScaling);
		vehicleCombatRatingPenalty = Prop.getBooleanProperty("vehicleCombatRatingPenalty", vehicleCombatRatingPenalty);
		fixMagranonDamageStacking = Prop.getBooleanProperty("fixMagranonDamageStacking", fixMagranonDamageStacking);
		adjustCombatRatingSpellPower = Prop.getBooleanProperty("adjustCombatRatingSpellPower", adjustCombatRatingSpellPower);
		disableLegendaryRegeneration = Prop.getBooleanProperty("disableLegendaryRegeneration", disableLegendaryRegeneration);
		useStaticLegendaryRegeneration = Prop.getBooleanProperty("useStaticLegendaryRegeneration", useStaticLegendaryRegeneration);

		// Mastercraft Module
		enableMastercraftModule = Prop.getBooleanProperty("enableMastercraftModule", enableMastercraftModule);
		enableDifficultyAdjustments = Prop.getBooleanProperty("enableDifficultyAdjustments", enableDifficultyAdjustments);
		affinityDifficultyBonus = Prop.getBooleanProperty("affinityDifficultyBonus", affinityDifficultyBonus);
		legendDifficultyBonus = Prop.getBooleanProperty("legendDifficultyBonus", legendDifficultyBonus);
		masterDifficultyBonus = Prop.getBooleanProperty("masterDifficultyBonus", masterDifficultyBonus);
		itemRarityDifficultyBonus = Prop.getBooleanProperty("itemRarityDifficultyBonus", itemRarityDifficultyBonus);
		legendItemDifficultyBonus = Prop.getBooleanProperty("legendItemDifficultyBonus", legendItemDifficultyBonus);
		masterItemDifficultyBonus = Prop.getBooleanProperty("masterItemDifficultyBonus", masterItemDifficultyBonus);
		empoweredChannelers = Prop.getBooleanProperty("empoweredChannelers", empoweredChannelers);
		channelSkillFavorReduction = Prop.getBooleanProperty("channelSkillFavorReduction", channelSkillFavorReduction);

		// Skill Module
		enableSkillModule = Prop.getBooleanProperty("enableSkillModule", enableSkillModule);
		enableHybridSkillGain = Prop.getBooleanProperty("enableHybridSkillGain", enableHybridSkillGain);
		hybridNegativeDecayRate = Prop.getFloatProperty("hybridNegativeDecayRate", hybridNegativeDecayRate);
		hybridPositiveDecayRate = Prop.getFloatProperty("hybridPositiveDecayRate", hybridPositiveDecayRate);
		hybridValueAtZero = Prop.getFloatProperty("hybridValueAtZero", hybridValueAtZero);
		hybridValueAtOneHundred = Prop.getFloatProperty("hybridValueAtOneHundred", hybridValueAtOneHundred);
		changePreachingLocation = Prop.getBooleanProperty("changePreachingLocation", changePreachingLocation);

		// Meditation Module
		enableMeditationModule = Prop.getBooleanProperty("enableMeditationModule", enableMeditationModule);
		simplifyMeditationTerrain = Prop.getBooleanProperty("simplifyMeditationTerrain", simplifyMeditationTerrain);
		removeInsanitySotG = Prop.getBooleanProperty("removeInsanitySotG", removeInsanitySotG);
		removeHateWarBonus = Prop.getBooleanProperty("removeHateWarBonus", removeHateWarBonus);
		insanitySpeedBonus = Prop.getBooleanProperty("insanitySpeedBonus", insanitySpeedBonus);
		hateMovementBonus = Prop.getBooleanProperty("hateMovementBonus", hateMovementBonus);
		scalingPowerStaminaBonus = Prop.getBooleanProperty("scalingPowerStaminaBonus", scalingPowerStaminaBonus);
		scalingKnowledgeSkillGain = Prop.getBooleanProperty("scalingKnowledgeSkillGain", scalingKnowledgeSkillGain);
		removeMeditationTickTimer = Prop.getBooleanProperty("removeMeditationTickTimer", removeMeditationTickTimer);
		newMeditationBuffs = Prop.getBooleanProperty("newMeditationBuffs", newMeditationBuffs);
		enableMeditationAbilityCooldowns = Prop.getBooleanProperty("enableMeditationAbilityCooldowns", enableMeditationAbilityCooldowns);
		loveRefreshCooldown = Prop.getLongProperty("loveRefreshCooldown", loveRefreshCooldown);
		loveEnchantNatureCooldown = Prop.getLongProperty("loveEnchantNatureCooldown", loveEnchantNatureCooldown);
		loveLoveEffectCooldown = Prop.getLongProperty("loveLoveEffectCooldown", loveLoveEffectCooldown);
		hateWarDamageCooldown = Prop.getLongProperty("hateWarDamageCooldown", hateWarDamageCooldown);
		hateStructureDamageCooldown = Prop.getLongProperty("hateStructureDamageCooldown", hateStructureDamageCooldown);
		hateFearCooldown = Prop.getLongProperty("hateFearCooldown", hateFearCooldown);
		powerElementalImmunityCooldown = Prop.getLongProperty("powerElementalImmunityCooldown", powerElementalImmunityCooldown);
		powerEruptFreezeCooldown = Prop.getLongProperty("powerEruptFreezeCooldown", powerEruptFreezeCooldown);
		powerIgnoreTrapsCooldown = Prop.getLongProperty("powerIgnoreTrapsCooldown", powerIgnoreTrapsCooldown);
		knowledgeInfoCreatureCooldown = Prop.getLongProperty("knowledgeInfoCreatureCooldown", knowledgeInfoCreatureCooldown);
		knowledgeInfoTileCooldown = Prop.getLongProperty("knowledgeInfoTileCooldown", knowledgeInfoTileCooldown);

		// Titan Module
		enableTitanModule = Prop.getBooleanProperty("enableTitanModule", enableTitanModule);
		disableTitanNaturalRegeneration = Prop.getBooleanProperty("disableTitanNaturalRegeneration", disableTitanNaturalRegeneration);
		pollTitanSpawnTime = Prop.getLongProperty("pollTitanSpawnTime", pollTitanSpawnTime);
		pollTitanTime = Prop.getLongProperty("pollTitanTime", pollTitanTime);
		titanRespawnTime = Prop.getLongProperty("titanRespawnTime", titanRespawnTime);

		// Rare Spawn Module
		enableRareSpawnModule = Prop.getBooleanProperty("enableRareSpawnModule", enableRareSpawnModule);
		pollRareSpawnTime = Prop.getLongProperty("pollRareSpawnTime", pollRareSpawnTime);

		// Mission Module
		enableMissionModule = Prop.getBooleanProperty("enableMissionModule", enableMissionModule);
		enableNewMissionCreator = Prop.getBooleanProperty("enableNewMissionCreator", enableNewMissionCreator);
		pollMissionCreatorTime = Prop.getLongProperty("pollMissionCreatorTime", pollMissionCreatorTime);
		useValreiEntities = Prop.getBooleanProperty("useValreiEntities", useValreiEntities);
		addMissionCurrencyReward = Prop.getBooleanProperty("addMissionCurrencyReward", addMissionCurrencyReward);
		preventMissionOceanSpawns = Prop.getBooleanProperty("preventMissionOceanSpawns", preventMissionOceanSpawns);
		additionalHerbivoreChecks = Prop.getBooleanProperty("additionalHerbivoreChecks", additionalHerbivoreChecks);
		additionalMissionSlayableChecks = Prop.getBooleanProperty("additionalMissionSlayableChecks", additionalMissionSlayableChecks);
		disableEpicMissionTypes = Prop.getBooleanProperty("disableEpicMissionTypes", disableEpicMissionTypes);

		// Mounted Module
		enableMountedModule = Prop.getBooleanProperty("enableMountedModule", enableMountedModule);
		newMountSpeedScaling = Prop.getBooleanProperty("newMountSpeedScaling", newMountSpeedScaling);
		updateMountSpeedOnDamage = Prop.getBooleanProperty("updateMountSpeedOnDamage", updateMountSpeedOnDamage);
		allowBisonMounts = Prop.getBooleanProperty("allowBisonMounts", allowBisonMounts);

		// Teleport Module
		enableTeleportModule = Prop.getBooleanProperty("enableTeleportModule", enableTeleportModule);
		useArenaTeleportMethod = Prop.getBooleanProperty("useArenaTeleportMethod", useArenaTeleportMethod);

		// Economy Module
		enableEconomyModule = Prop.getBooleanProperty("enableEconomyModule", enableEconomyModule);
		adjustSealedMapValue = Prop.getBooleanProperty("adjustSealedMapValue", adjustSealedMapValue);
		disableTraderRefill = Prop.getBooleanProperty("disableTraderRefill", disableTraderRefill);
		voidTraderMoney = Prop.getBooleanProperty("voidTraderMoney", voidTraderMoney);

		// Supply Depot Module
		enableSupplyDepotModule = Prop.getBooleanProperty("enableSupplyDepotModule", enableSupplyDepotModule);
		useSupplyDepotLights = Prop.getBooleanProperty("useSupplyDepotLights", useSupplyDepotLights);
		pollDepotTime = Prop.getLongProperty("pollDepotTime", pollDepotTime);
		captureMessageInterval = Prop.getLongProperty("captureMessageInterval", captureMessageInterval);
		depotRespawnTime = Prop.getLongProperty("depotRespawnTime", depotRespawnTime);

		// Bestiary Module
		enableBestiaryModule = Prop.getBooleanProperty("enableBestiaryModule", enableBestiaryModule);
		fixSacrificingStrongCreatures = Prop.getBooleanProperty("fixSacrificingStrongCreatures", fixSacrificingStrongCreatures);
		disableAfkTraining = Prop.getBooleanProperty("disableAfkTraining", disableAfkTraining);
		fixChargersWalkingThroughWalls = Prop.getBooleanProperty("fixChargersWalkingThroughWalls", fixChargersWalkingThroughWalls);
		conditionWildCreatures = Prop.getBooleanProperty("conditionWildCreatures", conditionWildCreatures);
		allowGhostArchery = Prop.getBooleanProperty("allowGhostArchery", allowGhostArchery);
		disableArcheryOnStrongCreatures = Prop.getBooleanProperty("disableArcheryOnStrongCreatures", disableArcheryOnStrongCreatures);
		genesisEnchantedGrassNewborns = Prop.getBooleanProperty("genesisEnchantedGrassNewborns", genesisEnchantedGrassNewborns);
		useCustomCorpseSizes = Prop.getBooleanProperty("useCustomCorpseSizes", useCustomCorpseSizes);
		allowCustomCreatureBreedNames = Prop.getBooleanProperty("allowCustomCreatureBreedNames", allowCustomCreatureBreedNames);
		allowGhostBreeding = Prop.getBooleanProperty("allowGhostBreeding", allowGhostBreeding);
		useCustomCreatureSizes = Prop.getBooleanProperty("useCustomCreatureSizes", useCustomCreatureSizes);
		useCustomCreatureSFX = Prop.getBooleanProperty("useCustomCreatureSFX", useCustomCreatureSFX);
		preventLegendaryHitching = Prop.getBooleanProperty("preventLegendaryHitching", preventLegendaryHitching);
		modifyNewCreatures = Prop.getBooleanProperty("modifyNewCreatures", modifyNewCreatures);
		logCreatureSpawns = Prop.getBooleanProperty("logCreatureSpawns", logCreatureSpawns);
		allowEpicCreatureNaturalSpawns = Prop.getBooleanProperty("allowEpicCreatureNaturalSpawns", allowEpicCreatureNaturalSpawns);
		enableCustomCreatures = Prop.getBooleanProperty("enableCustomCreatures", enableCustomCreatures);
		enableWyverns = Prop.getBooleanProperty("enableWyverns", enableWyverns);
		enableFlavorMobs = Prop.getBooleanProperty("enableFlavorMobs", enableFlavorMobs);
		enableEventMobs = Prop.getBooleanProperty("enableEventMobs", enableEventMobs);
		enableRareSpawns = Prop.getBooleanProperty("enableRareSpawns", enableRareSpawns);
		enableCustomLegendaries = Prop.getBooleanProperty("enableCustomLegendaries", enableCustomLegendaries);
		enableTitans = Prop.getBooleanProperty("enableTitans", enableTitans);

		// Bounty Module
		enableBountyModule = Prop.getBooleanProperty("enableBountyModule", enableBountyModule);
		usePlayerBounty = Prop.getBooleanProperty("usePlayerBounty", usePlayerBounty);
		useLootTable = Prop.getBooleanProperty("useLootTable", useLootTable);

		// Item Module
		enableItemModule = Prop.getBooleanProperty("enableItemModule", enableItemModule);
		combineLeather = Prop.getBooleanProperty("combineLeather", combineLeather);
		combineLogs = Prop.getBooleanProperty("combineLogs", combineLogs);
		reduceLogVolume = Prop.getBooleanProperty("reduceLogVolume", reduceLogVolume);
		reduceKindlingVolume = Prop.getBooleanProperty("reduceKindlingVolume", reduceKindlingVolume);
		droppableSleepPowder = Prop.getBooleanProperty("droppableSleepPowder", droppableSleepPowder);
		setSilverMirrorPrice = Prop.getBooleanProperty("setSilverMirrorPrice", setSilverMirrorPrice);
		setGoldMirrorPrice = Prop.getBooleanProperty("setGoldMirrorPrice", setGoldMirrorPrice);
		setCreatureCratePrice = Prop.getBooleanProperty("setCreatureCratePrice", setCreatureCratePrice);
		setResurrectionStonePrice = Prop.getBooleanProperty("setResurrectionStonePrice", setResurrectionStonePrice);
		setShakerOrbPrice = Prop.getBooleanProperty("setShakerOrbPrice", setShakerOrbPrice);
		loadableMailbox = Prop.getBooleanProperty("loadableMailbox", loadableMailbox);
		loadableBellTower = Prop.getBooleanProperty("loadableBellTower", loadableBellTower);
		loadableTrashBin = Prop.getBooleanProperty("loadableTrashBin", loadableTrashBin);
		loadableAltars = Prop.getBooleanProperty("loadableAltars", loadableAltars);
		oneHandedLongSpear = Prop.getBooleanProperty("oneHandedLongSpear", oneHandedLongSpear);
		reduceDirtDifficulty = Prop.getBooleanProperty("reduceDirtDifficulty", reduceDirtDifficulty);
		reduceSandDifficulty = Prop.getBooleanProperty("reduceSandDifficulty", reduceSandDifficulty);
		reduceSandstoneDifficulty = Prop.getBooleanProperty("reduceSandstoneDifficulty", reduceSandstoneDifficulty);
		decorationStoneKeystone = Prop.getBooleanProperty("decorationStoneKeystone", decorationStoneKeystone);
		decorationMarbleKeystone = Prop.getBooleanProperty("decorationMarbleKeystone", decorationMarbleKeystone);
		decorationSkull = Prop.getBooleanProperty("decorationSkull", decorationSkull);
		useCustomCacheFragments = Prop.getBooleanProperty("useCustomCacheFragments", useCustomCacheFragments);
		adjustStatueFragmentCount = Prop.getBooleanProperty("adjustStatueFragmentCount", adjustStatueFragmentCount);
		removeLockpickSkillRequirement = Prop.getBooleanProperty("removeLockpickSkillRequirement", removeLockpickSkillRequirement);
		createCustomItemTemplates = Prop.getBooleanProperty("createCustomItemTemplates", createCustomItemTemplates);
		enableCustomItemCreation = Prop.getBooleanProperty("enableCustomItemCreation", enableCustomItemCreation);
		craftHuntingArrowPacks = Prop.getBooleanProperty("craftHuntingArrowPacks", craftHuntingArrowPacks);
		craftWarArrowPacks = Prop.getBooleanProperty("craftWarArrowPacks", craftWarArrowPacks);
		craftBattleYoyo = Prop.getBooleanProperty("craftBattleYoyo", craftBattleYoyo);
		craftClub = Prop.getBooleanProperty("craftClub", craftClub);
		craftDepthDrill = Prop.getBooleanProperty("craftDepthDrill", craftDepthDrill);
		craftEternalReservoir = Prop.getBooleanProperty("craftEternalReservoir", craftEternalReservoir);
		craftEviscerator = Prop.getBooleanProperty("craftEviscerator", craftEviscerator);
		craftKnuckles = Prop.getBooleanProperty("craftKnuckles", craftKnuckles);
		craftMassStorageUnit = Prop.getBooleanProperty("craftMassStorageUnit", craftMassStorageUnit);
		craftStatuetteDeities = Prop.getBooleanProperty("craftStatuetteDeities", craftStatuetteDeities);
		craftWarhammer = Prop.getBooleanProperty("craftWarhammer", craftWarhammer);

		// Soulstealing Module
		enableSoulstealingModule = Prop.getBooleanProperty("enableSoulstealingModule", enableSoulstealingModule);
		pollEternalReservoirTime = Prop.getLongProperty("pollEternalReservoirTime", pollEternalReservoirTime);

		// Action Module
		enableActionModule = Prop.getBooleanProperty("enableActionModule", enableActionModule);
		actionUnequipAll = Prop.getBooleanProperty("actionUnequipAll", actionUnequipAll);
		actionReceiveAllMail = Prop.getBooleanProperty("actionReceiveAllMail", actionReceiveAllMail);
        actionSplitSorcery = Prop.getBooleanProperty("actionSplitSorcery", actionSplitSorcery);
        actionLeaderboard = Prop.getBooleanProperty("actionLeaderboard", actionLeaderboard);
        actionSorceryFragmentCombine = Prop.getBooleanProperty("actionSorceryFragmentCombine", actionSorceryFragmentCombine);
        actionArenaTeleports = Prop.getBooleanProperty("actionArenaTeleports", actionArenaTeleports);
        actionAddMissionDev = Prop.getBooleanProperty("actionAddMissionDev", actionAddMissionDev);
        actionRemoveMissionDev = Prop.getBooleanProperty("actionRemoveMissionDev", actionRemoveMissionDev);
        actionCreatureReportDev = Prop.getBooleanProperty("actionCreatureReportDev", actionCreatureReportDev);
        actionSmoothTerrainDev = Prop.getBooleanProperty("actionSmoothTerrainDev", actionSmoothTerrainDev);

        // Erosion Module
        enableErosionModule = Prop.getBooleanProperty("enableErosionModule", enableErosionModule);
        pollTerrainSmoothTime = Prop.getLongProperty("pollTerrainSmoothTime", pollTerrainSmoothTime);

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
						}else if (name.startsWith("skillName")) {
							String[] values = value.split(",");
							if(values.length < 2 || values.length > 2){
								logger.warning("Error parsing Skill Name: Invalid amount of arguments for following property: "+value);
							}
							int skillId = SkillAssist.getSkill(values[0]);
							if (skillId < 0){
								skillId = Integer.parseInt(values[0]);
							}
							if (skillName.containsKey(skillId)){
								logger.warning("Duplicate skill name configurations for skill id "+skillId);
							}else{
								String newName = values[1];
								skillName.put(skillId, newName);
							}
						}else if (name.startsWith("skillDifficulty")) {
							String[] values = value.split(",");
							if(values.length < 2 || values.length > 2){
								logger.warning("Error parsing Skill Difficulty: Invalid amount of arguments for following property: "+value);
							}
							int skillId = SkillAssist.getSkill(values[0]);
							if (skillId < 0){
								skillId = Integer.parseInt(values[0]);
							}
							if (skillDifficulty.containsKey(skillId)){
								logger.warning("Duplicate difficulty configurations for skill id "+skillId);
							}else{
								float difficulty = Float.parseFloat(values[1]);
								skillDifficulty.put(skillId, difficulty);
							}
						}else if (name.startsWith("skillTickTime")) {
							String[] values = value.split(",");
							if(values.length < 2 || values.length > 2){
								logger.warning("Error parsing Skill Tick Time: Invalid amount of arguments for following property: "+value);
							}
							int skillId = SkillAssist.getSkill(values[0]);
							if (skillId < 0){
								skillId = Integer.parseInt(values[0]);
							}
							if (skillTickTime.containsKey(skillId)){
								logger.warning("Duplicate tick time configurations for skill id "+skillId);
							}else{
								long difficulty = Long.parseLong(values[1]);
								skillTickTime.put(skillId, difficulty);
							}
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
			logger.info("Change Pumpkin King Title: "+changePumpkinKingTitle);
			logger.info("Change Deity Passives: "+changeDeityPassives);
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

		logger.info("Combat Module: "+enableCombatModule);
		if (enableCombatModule){
			logger.info("Combat Rating Adjustments: "+enableCombatRatingAdjustments);
			if (enableCombatRatingAdjustments){
				logger.info("Royal Executioner Bonus: "+royalExecutionerBonus);
				logger.info("Pet Soul Depth Scaling: "+petSoulDepthScaling);
				logger.info("Vehicle Combat Rating Penalty: "+vehicleCombatRatingPenalty);
			}
			logger.info("Fix Magranon Damage Stacking: "+fixMagranonDamageStacking);
			logger.info("Adjust Combat Rating Spell Power: "+adjustCombatRatingSpellPower);
			logger.info("Disable Legendary Regeneration: "+disableLegendaryRegeneration);
			logger.info("Use Static Legendary Regeneration: "+useStaticLegendaryRegeneration);
		}

		logger.info("Mastercraft Module: "+enableMastercraftModule);
		if (enableMastercraftModule){
			logger.info("Difficulty Adjustments: "+enableDifficultyAdjustments);
			if (enableDifficultyAdjustments){
				logger.info("Affinity Difficulty Bonus: "+affinityDifficultyBonus);
				logger.info("Legend Difficulty Bonus: "+legendDifficultyBonus);
				logger.info("Master Difficulty Bonus: "+masterDifficultyBonus);
				logger.info("Item Rarity Difficulty Bonus: "+itemRarityDifficultyBonus);
				logger.info("Legend Item Difficulty Bonus: "+legendItemDifficultyBonus);
				logger.info("Master Item Difficulty Bonus: "+masterItemDifficultyBonus);
			}
			logger.info("Empowered Channelers: "+empoweredChannelers);
			logger.info("Channel Skill Favor Reduction: "+channelSkillFavorReduction);
		}

		logger.info("Skill Module: "+enableSkillModule);
		if (enableSkillModule){
			logger.info("Hybrid Skill Gain: "+enableHybridSkillGain);
			if (enableHybridSkillGain){
				logger.info("Hybrid Negative Decay Rate: "+hybridNegativeDecayRate);
				logger.info("Hybrid Positive Decay Rate: "+hybridPositiveDecayRate);
				logger.info("Hybrid Value At Zero: "+hybridValueAtZero);
				logger.info("Hybrid Value At One Hundred: "+hybridValueAtOneHundred);
			}
			for (int skillId : skillName.keySet()){
				logger.info(String.format("Changing name of skill %s to %s.",
						SkillAssist.getSkill(skillId), skillName.get(skillId)));
			}
			for (int skillId : skillDifficulty.keySet()){
				logger.info(String.format("Setting difficulty of skill %s to %.2f.",
						SkillAssist.getSkill(skillId), skillDifficulty.get(skillId)));
			}
			for (int skillId : skillTickTime.keySet()){
				logger.info(String.format("Setting tick time of skill %s to %d.",
						SkillAssist.getSkill(skillId), skillTickTime.get(skillId)));
			}
			logger.info("Change Preaching Location: "+changePreachingLocation);
		}

		logger.info("Meditation Module: "+enableMeditationModule);
		if (enableMeditationModule){
			logger.info("Simplify Meditation Terrain: "+simplifyMeditationTerrain);
			logger.info("Remove Insanity Shield of the Gone: "+removeInsanitySotG);
			logger.info("Remove Hate War Bonus: "+removeHateWarBonus);
			logger.info("Insanity Speed Bonus: "+insanitySpeedBonus);
			logger.info("Hate Movement Bonus: "+hateMovementBonus);
			logger.info("Scaling Power Stamina Bonus: "+scalingPowerStaminaBonus);
			logger.info("Scaling Knowledge Skill Gain: "+scalingKnowledgeSkillGain);
			logger.info("Remove Meditation Tick Timer: "+removeMeditationTickTimer);
			logger.info("New Meditation Buffs: "+newMeditationBuffs);
			logger.info("Meditation Ability Cooldowns: "+enableMeditationAbilityCooldowns);
			if (enableMeditationAbilityCooldowns){
				logger.info("Love Refresh Cooldown: "+loveRefreshCooldown);
				logger.info("Love Enchant Nature Cooldown: "+loveEnchantNatureCooldown);
				logger.info("Love Love Effect Cooldown: "+loveLoveEffectCooldown);
				logger.info("Hate War Bonus Cooldown: "+hateWarDamageCooldown);
				logger.info("Hate Structure Damage Cooldown: "+hateStructureDamageCooldown);
				logger.info("Hate Fear Cooldown: "+hateFearCooldown);
				logger.info("Power Elemental Immunity Cooldown: "+powerElementalImmunityCooldown);
				logger.info("Power Erupt/Freeze Cooldown: "+powerEruptFreezeCooldown);
				logger.info("Power Ignore Traps Cooldown: "+powerIgnoreTrapsCooldown);
				logger.info("Knowledge Info Creature Cooldown: "+knowledgeInfoCreatureCooldown);
				logger.info("Knowledge Info Tile Cooldown: "+knowledgeInfoTileCooldown);
			}
		}

		logger.info("Titan Module: "+enableTitanModule);
		if (enableTitanModule){
			logger.info("Disable Titan Natural Regeneration: "+disableTitanNaturalRegeneration);
			logger.info("Poll Titan Spawn Timer: "+pollTitanSpawnTime);
			logger.info("Poll Titan Timer: "+pollTitanTime);
			logger.info("Titan Respawn Timer: "+titanRespawnTime);
		}

		logger.info("Rare Spawn Module: "+enableRareSpawnModule);
		if (enableRareSpawnModule){
			logger.info("Poll Rare Spawn Timer: "+pollRareSpawnTime);
		}

		logger.info("Mission Module: "+enableMissionModule);
		if (enableMissionModule){
			logger.info("New Mission Creator: "+enableNewMissionCreator);
			if (enableNewMissionCreator){
				logger.info("New Mission Creator Timer: " + pollMissionCreatorTime);
				logger.info("Use Valrei Entities: "+useValreiEntities);
			}
			logger.info("Add Mission Currency Reward: "+addMissionCurrencyReward);
			logger.info("Prevent Ocean Mission Spawns: "+preventMissionOceanSpawns);
			logger.info("Additional Herbivore Checks: "+additionalHerbivoreChecks);
			logger.info("Additional Mission Slayable Checks: "+additionalMissionSlayableChecks);
			logger.info("Disable Epic Mission Types: "+disableEpicMissionTypes);
		}

		logger.info("Mounted Module: "+enableMountedModule);
		if (enableMountedModule){
			logger.info("New Mount Speed Scaling: "+newMountSpeedScaling);
			logger.info("Update Mount Speed On Damage: "+updateMountSpeedOnDamage);
			logger.info("Allow Bison Mounts: "+allowBisonMounts);
		}

		logger.info("Teleport Module: "+enableTeleportModule);
		if (enableTeleportModule){
			logger.info("Use Arena Teleport Method: "+useArenaTeleportMethod);
		}

		logger.info("Economy Module: "+enableEconomyModule);
		if (enableEconomyModule){
			logger.info("Adjust Sealed Map Value: "+adjustSealedMapValue);
			logger.info("Disable Trader Refill: "+disableTraderRefill);
			logger.info("Void Trader Money: "+voidTraderMoney);
		}

		logger.info("Supply Depot Module: "+enableSupplyDepotModule);
		if (enableSupplyDepotModule){
			logger.info("Use Supply Depot Lights: "+useSupplyDepotLights);
			logger.info("Poll Depot Timer: "+pollDepotTime);
			logger.info("Capture Message Interval: "+captureMessageInterval);
			logger.info("Depot Respawn Timer: "+depotRespawnTime);
		}

		logger.info("Bestiary Module: "+enableBestiaryModule);
		if (enableBestiaryModule){
			logger.info("Fix Sacrificing Strong Creatures: "+fixSacrificingStrongCreatures);
			logger.info("Disable AFK Training: "+disableAfkTraining);
			logger.info("Fix Chargers Walking Through Walls: "+fixChargersWalkingThroughWalls);
			logger.info("Condition Wild Creatures: "+conditionWildCreatures);
			logger.info("Allow Ghost Archery: "+allowGhostArchery);
			logger.info("Disable Archery On Strong Creatures: "+disableArcheryOnStrongCreatures);
			logger.info("Genesis Enchanted Grass Newborns: "+genesisEnchantedGrassNewborns);
			logger.info("Use Custom Corpse Sizes: "+useCustomCorpseSizes);
			logger.info("Allow Custom Creature Breed Names: "+allowCustomCreatureBreedNames);
			logger.info("Allow Ghost Breeding: "+allowGhostBreeding);
			logger.info("Allow Ghost Corpses: "+allowGhostCorpses);
			logger.info("Use Custom Creature Sizes: "+useCustomCreatureSizes);
			logger.info("Use Custom Creature SFX: "+useCustomCreatureSFX);
			logger.info("Prevent Legendary Hitching: "+preventLegendaryHitching);
			logger.info("Modify New Creatures: "+modifyNewCreatures);
			logger.info("Log Creature Spawns: "+logCreatureSpawns);
			logger.info("Enable Epic Creature Natural Spawns: "+allowEpicCreatureNaturalSpawns);
			logger.info("Custom Creatures: "+enableCustomCreatures);
			if (enableCustomCreatures){
				logger.info("Enable Wyverns: "+enableWyverns);
				logger.info("Enable Flavor Mobs: "+enableFlavorMobs);
				logger.info("Enable Event Mobs: "+enableEventMobs);
				logger.info("Enable Rare Spawn Mobs: "+enableRareSpawns);
				logger.info("Enable Custom Legendaries: "+enableCustomLegendaries);
				logger.info("Enable Titans: "+enableTitans);
			}
		}

		logger.info("Bounty Module: "+enableBountyModule);
		if (enableBountyModule){
			logger.info("Use Player Bounty: "+usePlayerBounty);
			logger.info("Use Loot Table: "+useLootTable);
		}

		logger.info("Item Module: "+enableItemModule);
		if (enableItemModule){
			logger.info("Combine Leather: "+combineLeather);
			logger.info("Combine Logs: "+combineLogs);
			logger.info("Reduce Log Volume: "+reduceLogVolume);
			logger.info("Reduce Kindling Volume: "+reduceKindlingVolume);
			logger.info("Droppable Sleep Powder: "+droppableSleepPowder);
			logger.info("Set Silver Mirror Price: "+setSilverMirrorPrice);
			logger.info("Set Gold Mirror Price: "+setGoldMirrorPrice);
			logger.info("Set Creature Crate Price: "+setCreatureCratePrice);
			logger.info("Set Resurrection Stone Price: "+setResurrectionStonePrice);
			logger.info("Set Shaker Orb Price: "+setShakerOrbPrice);
			logger.info("Loadable Mailbox: "+loadableMailbox);
			logger.info("Loadable Bell Tower: "+loadableBellTower);
			logger.info("Loadable Trash Bin: "+loadableTrashBin);
			logger.info("Loadable Altars: "+loadableAltars);
			logger.info("One Handed Long Spear: "+oneHandedLongSpear);
			logger.info("Reduce Dirt Difficulty: "+reduceDirtDifficulty);
			logger.info("Reduce Sand Difficulty: "+reduceSandDifficulty);
			logger.info("Reduce Sandstone Difficulty: "+reduceSandstoneDifficulty);
			logger.info("Decoration Stone Keystone: "+decorationStoneKeystone);
			logger.info("Decoration Marble Keystone: "+decorationMarbleKeystone);
			logger.info("Decoration Skull: "+decorationSkull);
			logger.info("Use Custom Cache Fragments: "+useCustomCacheFragments);
			logger.info("Adjust Statue Fragment Count: "+adjustStatueFragmentCount);
			logger.info("Remove Lockpick Skill Requirement: "+removeLockpickSkillRequirement);
			logger.info("Create Custom Item Templates: "+createCustomItemTemplates);
			logger.info("Custom Item Creation: "+enableCustomItemCreation);
			if (enableCustomItemCreation){
				logger.info("Craft Hunting Arrow Packs: "+craftHuntingArrowPacks);
				logger.info("Craft War Arrow Packs: "+craftWarArrowPacks);
				logger.info("Craft Battle Yoyo: "+craftBattleYoyo);
				logger.info("Craft Club: "+craftClub);
				logger.info("Craft Depth Drill: "+craftDepthDrill);
				logger.info("Craft Eternal Reservoir: "+craftEternalReservoir);
				logger.info("Craft Eviscerator: "+craftEviscerator);
				logger.info("Craft Knuckles: "+craftKnuckles);
				logger.info("Craft Mass Storage Unit: "+craftMassStorageUnit);
				logger.info("Craft Statuette Deities: "+craftStatuetteDeities);
				logger.info("Craft Warhammer: "+craftWarhammer);
			}
		}

		logger.info("Soulstealing Module: "+enableSoulstealingModule);
		if (enableSoulstealingModule){
			logger.info("Poll Eternal Reservoir Timer: "+pollEternalReservoirTime);
		}

		logger.info("Action Module: "+enableActionModule);
		if (enableActionModule){
			logger.info("Unequip All Action: "+actionUnequipAll);
			logger.info("Receive All Mail Action: "+actionReceiveAllMail);
			logger.info("Split Sorcery Action: "+actionSplitSorcery);
			logger.info("Leaderboard Action: "+actionLeaderboard);
			logger.info("Sorcery Fragment Combine Action: "+actionSorceryFragmentCombine);
			logger.info("Arena Teleport Actions: "+actionArenaTeleports);
			logger.info("Add Mission Dev Action: "+actionAddMissionDev);
			logger.info("Remove Mission Dev Action: "+actionRemoveMissionDev);
			logger.info("Creature Report Dev Action: "+actionCreatureReportDev);
			logger.info("Smooth Terrain Dev Action: "+actionSmoothTerrainDev);
		}

		logger.info("Erosion Module: "+enableErosionModule);
		if (enableErosionModule){
		    logger.info("Poll Terrain Smooth Timer: "+pollTerrainSmoothTime);
        }

		logger.info("Treasure Chest Loot Module: "+enableTreasureChestLootModule);
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

			// Anti-Cheat Module Pre-Init
			if (enableAntiCheatModule) {
				AntiCheat.preInit();
			}

			// Quality Of Life Module Pre-Init
			if (enableQualityOfLifeModule) {
				QualityOfLife.preInit();
			}

			// Combat Module Pre-Init
			if (enableCombatModule) {
				CombatChanges.preInit();
			}

			// Mastercraft Module Pre-Init
			if (enableMastercraftModule) {
				Mastercraft.preInit();
			}

			// Skill Module Pre-Init
			if (enableSkillModule) {
				SkillChanges.preInit();
			}

			// Meditation Module Pre-Init
			if (enableMeditationModule) {
				MeditationPerks.preInit();
			}

			// Titan Module Pre-Init
			if (enableTitanModule) {
				Titans.preInit();
			}

			// Mission Module Pre-Init
			if (enableMissionModule) {
				MissionCreator.preInit();
			}

			// Mounted Module Pre-Init
			if (enableMountedModule) {
				MountedChanges.preInit();
			}

			// Teleport Module Pre-Init
			if (enableTeleportModule) {
				TeleportHandler.preInit();
			}

			// Economy Module Pre-Init
			if (enableEconomyModule) {
				EconomicChanges.preInit();
			}

			// Supply Depot Module Pre-Init
			if (enableSupplyDepotModule) {
				SupplyDepots.preInit();
			}

			// Bestiary Module Pre-Init
			if (enableBestiaryModule) {
				Bestiary.preInit();
			}

			// Treasure Chest Loot Module Pre-Init
			if (enableTreasureChestLootModule) {
				TreasureChests.preInit();
			}

            // Only clears responses, doesn't have any effect. Harmless to run even if key fragments are not used.
            KeyEvent.preInit();

            // Bloodlust might no longer be necessary. Code remains for reference.
			//Bloodlust.preInit();

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
		ModCreatures.init();
		ModVehicleBehaviours.init();

		if (WyvernMods.enableCustomTitlesModule) {
			PlayerTitles.init();
		}

		if (WyvernMods.enableMiscChangesModule) {
			MiscChanges.changeExistingTitles();
		}

		if (WyvernMods.enableBountyModule) {
			Bounty.init();
		}
		
		// Vanilla:
		if (WyvernMods.enableMountedModule && WyvernMods.allowBisonMounts) {
			logger.info("Allowing Bison to be mounted.");
			ModCreatures.addCreature(new Bison());
		}
		
		// Epic:
		if (WyvernMods.enableBestiaryModule && WyvernMods.allowEpicCreatureNaturalSpawns) {
			logger.info("Allowing epic creatures to spawn naturally.");
			ModCreatures.addCreature(new LavaFiend());
			ModCreatures.addCreature(new SolDemon());
			ModCreatures.addCreature(new Worg());
		}

		if (WyvernMods.enableBestiaryModule && WyvernMods.enableCustomCreatures) {
			// Wyverns:
			if (WyvernMods.enableWyverns) {
				logger.info("Registering Wyverns.");
				ModCreatures.addCreature(new WyvernBlack());
				ModCreatures.addCreature(new WyvernGreen());
				ModCreatures.addCreature(new WyvernRed());
				ModCreatures.addCreature(new WyvernWhite());
				ModCreatures.addCreature(new WyvernBlue());
			}

			// Flavor Mobs:
			if (WyvernMods.enableFlavorMobs) {
				logger.info("Registering Flavor creatures.");
				ModCreatures.addCreature(new Avenger());
				ModCreatures.addCreature(new FireCrab());
				ModCreatures.addCreature(new ForestSpider());
				ModCreatures.addCreature(new Giant());
				ModCreatures.addCreature(new Charger());
				ModCreatures.addCreature(new HornedPony());
				ModCreatures.addCreature(new LargeBoar());
				ModCreatures.addCreature(new SpiritTroll());
			}

			// Event Mobs:
			if (WyvernMods.enableEventMobs) {
				logger.info("Registering Event creatures.");
				ModCreatures.addCreature(new IceCat());
				ModCreatures.addCreature(new FireGiant());
				ModCreatures.addCreature(new GuardianMagranon());
				ModCreatures.addCreature(new Terror());
			}

			// Rare Spawns:
			if (WyvernMods.enableRareSpawns) {
				logger.info("Registering Rare Spawn creatures.");
				ModCreatures.addCreature(new Reaper());
				ModCreatures.addCreature(new SpectralDrake());
			}

			// Legendaries:
			if (WyvernMods.enableCustomLegendaries) {
				logger.info("Registering Legendary creatures.");
				ModCreatures.addCreature(new Facebreyker());
			}

			// Titans:
			if (WyvernMods.enableTitans) {
				logger.info("Registering Titans.");
				ModCreatures.addCreature(new Ifrit());
				ModCreatures.addCreature(new Lilith());
				// Titan Spawns:
				logger.info("Register Titan Spawns.");
				ModCreatures.addCreature(new IfritFiend());
				ModCreatures.addCreature(new IfritSpider());
				ModCreatures.addCreature(new LilithWraith());
				ModCreatures.addCreature(new LilithZombie());
			}
		}
	}

	@Override
	public void onItemTemplatesCreated() {
		if (WyvernMods.enableItemModule) {
			if (WyvernMods.createCustomItemTemplates) {
				logger.info("Creating Item Mod item templates.");
				ItemMod.createItems();
				logger.info("Creating Cache item templates.");
				Caches.createItems();
			}
			try {
				logger.info("Editing existing item templates.");
				ItemMod.modifyItems();
				logger.info("Registering permissions hook for custom items.");
				ItemMod.registerPermissionsHook();
			} catch (NoSuchTemplateException | IllegalArgumentException | IllegalAccessException | ClassCastException | NoSuchFieldException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
    public void onPlayerLogin(Player p) {
        DatabaseHelper.onPlayerLogin(p);

        // Award Custom Titles on player login
        if(enableCustomTitlesModule) {
			PlayerTitles.awardCustomTitles(p);
		}
    }

	@Override
	public void onServerStarted() {
		try {
			if (WyvernMods.enableBestiaryModule) {
				logger.info("Setting custom creature template variables.");
				Bestiary.setTemplateVariables();
			}
			if (WyvernMods.enableItemModule) {
				ItemMod.onServerStarted();
				if (WyvernMods.enableCustomItemCreation) {
					logger.info("Registering Item Mod creation entries.");
					ItemMod.initCreationEntries();
				}
				if (WyvernMods.createCustomItemTemplates) {
					logger.info("Registering Item Mod actions.");
					ItemMod.registerActions();
					logger.info("Registering Cache actions.");
					Caches.registerActions();
				}
			}
			if (WyvernMods.enableSoulstealingModule) {
				logger.info("Registering Soulstealer actions.");
				Soulstealing.registerActions();
			}
			if (WyvernMods.enableActionModule) {
				logger.info("Registering Custom actions.");
				if (WyvernMods.actionUnequipAll) {
					ModActions.registerAction(new UnequipAllAction());
				}
				if (WyvernMods.actionReceiveAllMail) {
					ModActions.registerAction(new ReceiveMailAction());
				}
				if (WyvernMods.actionSplitSorcery) {
                    ModActions.registerAction(new SorcerySplitAction());
                }
                if (WyvernMods.actionLeaderboard) {
                    ModActions.registerAction(new LeaderboardAction());
                    ModActions.registerAction(new LeaderboardSkillAction());
                }
				//ModActions.registerAction(new AddSubGroupAction()); // [5/14/19] Disabled - Added to base game.
				logger.info("Registering Arena actions.");
				if (WyvernMods.actionSorceryFragmentCombine) {
                    ModActions.registerAction(new SorceryCombineAction());
                }
				if (WyvernMods.actionArenaTeleports) {
                    ModActions.registerAction(new ArenaTeleportAction());
                    ModActions.registerAction(new ArenaEscapeAction());
                }
				logger.info("Registering Dev actions.");
				if (WyvernMods.actionAddMissionDev) {
                    ModActions.registerAction(new MissionAddAction());
                }
                if (WyvernMods.actionRemoveMissionDev) {
                    ModActions.registerAction(new MissionRemoveAction());
                }
                if (WyvernMods.actionCreatureReportDev) {
                    ModActions.registerAction(new CreatureReportAction());
                }
                if (WyvernMods.actionSmoothTerrainDev) {
                    ModActions.registerAction(new SmoothTerrainAction());
                }
			}

			// Sets up achievement changes specifically for the Leaderboard system.
			if (WyvernMods.enableActionModule && WyvernMods.actionLeaderboard) {
                logger.info("Setting up Leaderboard Achievement templates.");
                AchievementChanges.onServerStarted();
            }

            if (WyvernMods.enableMiscChangesModule && WyvernMods.changeDeityPassives) {
                DeityChanges.onServerStarted();
            }

			if (WyvernMods.enableSkillModule) {
				SkillChanges.onServerStarted();
			}

		} catch (IllegalArgumentException | ClassCastException e) {
			e.printStackTrace();
		}
		DatabaseHelper.onServerStarted();
	}
	
	public static long lastSecondPolled = 0;
	public static long lastPolledTitanSpawn = 0;
	public static long lastPolledTitans = 0;
	public static long lastPolledDepots = 0;
	public static long lastPolledRareSpawns = 0;
	public static long lastPolledEternalReservoirs = 0;
	public static long lastPolledMissionCreator = 0;
    /* Disabled for now, might need to be revisited.
    public static long lastPolledBloodlust = 0;
    public static final long pollBloodlustTime = TimeConstants.MINUTE_MILLIS;*/
    public static long lastPolledUniqueRegeneration = 0;
    public static final long pollUniqueRegenerationTime = TimeConstants.SECOND_MILLIS;
    public static long lastPolledUniqueCollection = 0;
    public static final long pollUniqueCollectionTime = TimeConstants.MINUTE_MILLIS*5;
    public static long lastPolledTerrainSmooth = 0;
	@Override
	public void onServerPoll() {
		if((lastSecondPolled + TimeConstants.SECOND_MILLIS) < System.currentTimeMillis()){
			if(WyvernMods.enableSupplyDepotModule && lastPolledDepots + pollDepotTime < System.currentTimeMillis()){
				SupplyDepots.pollDepotSpawn();
				lastPolledDepots += pollDepotTime;
			}
			if(WyvernMods.enableTitanModule && lastPolledTitanSpawn + pollTitanSpawnTime < System.currentTimeMillis()){
				Titans.pollTitanSpawn();
				lastPolledTitanSpawn += pollTitanSpawnTime;
			}
			if(WyvernMods.enableTitanModule && lastPolledTitans + pollTitanTime < System.currentTimeMillis()){
				Titans.pollTitans();
				lastPolledTitans += pollTitanTime;
			}
			if(WyvernMods.enableRareSpawnModule && lastPolledRareSpawns + pollRareSpawnTime < System.currentTimeMillis()){
			    RareSpawns.pollRareSpawns();
			    lastPolledRareSpawns += pollRareSpawnTime;
            }
			if(WyvernMods.enableSoulstealingModule && lastPolledEternalReservoirs + pollEternalReservoirTime < System.currentTimeMillis()){
				Soulstealing.pollSoulForges();
				lastPolledEternalReservoirs += pollEternalReservoirTime;
			}
			if(WyvernMods.enableMissionModule && enableNewMissionCreator && lastPolledMissionCreator + pollMissionCreatorTime < System.currentTimeMillis()){
				MissionCreator.pollMissions();
				lastPolledMissionCreator += pollMissionCreatorTime;
			}
            /* Disabled for now, might need to be revisited.
            if(lastPolledBloodlust + pollBloodlustTime < System.currentTimeMillis()){
                Bloodlust.pollLusts();
                lastPolledBloodlust += pollBloodlustTime;
            }*/
            if(WyvernMods.enableCombatModule && WyvernMods.useStaticLegendaryRegeneration && lastPolledUniqueRegeneration + pollUniqueRegenerationTime < System.currentTimeMillis()){
                CombatChanges.pollUniqueRegeneration();
                lastPolledUniqueRegeneration += pollUniqueRegenerationTime;
            }
            if(WyvernMods.enableCombatModule && lastPolledUniqueCollection + pollUniqueCollectionTime < System.currentTimeMillis()){
                CombatChanges.pollUniqueCollection();
                lastPolledUniqueCollection += pollUniqueCollectionTime;
            }
            if(WyvernMods.enableErosionModule && lastPolledTerrainSmooth + pollTerrainSmoothTime < System.currentTimeMillis()){
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
				//lastPolledBloodlust = System.currentTimeMillis();
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