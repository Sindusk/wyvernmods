package mod.sin.wyvern;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.wurmonline.server.Servers;
import com.wurmonline.server.combat.ArmourTemplate;
import com.wurmonline.server.items.*;
import mod.sin.items.caches.*;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.combat.Weapon;
import com.wurmonline.server.creatures.Creature;
import javassist.CtClass;
import javassist.bytecode.Descriptor;
import mod.sin.actions.items.*;
import mod.sin.armour.*;
import mod.sin.items.*;
import mod.sin.weapons.*;
import mod.sin.weapons.heads.*;
import mod.sin.weapons.titan.*;

public class ItemMod {
	public static Logger logger = Logger.getLogger(ItemMod.class.getName());

	public static AffinityCatcher AFFINITY_CATCHER = new AffinityCatcher();
	public static AffinityOrb AFFINITY_ORB = new AffinityOrb();
	public static ArenaCache ARENA_CACHE = new ArenaCache();
	public static ArenaSupplyDepot ARENA_SUPPLY_DEPOT = new ArenaSupplyDepot();
	public static ArrowPackHunting ARROW_PACK_HUNTING = new ArrowPackHunting();
	public static ArrowPackWar ARROW_PACK_WAR = new ArrowPackWar();
	public static BattleYoyo BATTLE_YOYO = new BattleYoyo();
	public static BookOfConversion BOOK_OF_CONVERSION = new BookOfConversion();
	public static Club CLUB = new Club();
	public static CoinDecoration COIN_DECORATION = new CoinDecoration();
	public static CorpseDecoration CORPSE_DECORATION = new CorpseDecoration();
	public static DepthDrill DEPTH_DRILL = new DepthDrill();
	public static DisintegrationRod DISINTEGRATION_ROD = new DisintegrationRod();
	//public static TitaniumLump ELECTRUM_LUMP = new TitaniumLump();
	public static EnchantOrb ENCHANT_ORB = new EnchantOrb();
	public static EternalOrb ETERNAL_ORB = new EternalOrb();
	public static EternalReservoir ETERNAL_RESERVOIR = new EternalReservoir();
	public static Eviscerator EVISCERATOR = new Eviscerator();
	public static FriyanTablet FRIYAN_TABLET = new FriyanTablet();
	public static HugeCrate HUGE_CRATE = new HugeCrate();
	public static Knuckles KNUCKLES = new Knuckles();
	public static MassStorageUnit MASS_STORAGE_UNIT = new MassStorageUnit();
	public static SealedMap SEALED_MAP = new SealedMap();
	public static SkeletonDecoration SKELETON_DECORATION = new SkeletonDecoration();
	public static Soul SOUL = new Soul();
	public static StatuetteBreyk STATUETTE_BREYK = new StatuetteBreyk();
	public static StatuetteCyberhusky STATUETTE_CYBERHUSKY = new StatuetteCyberhusky();
	public static TreasureBox TREASURE_BOX = new TreasureBox();
	public static Warhammer WARHAMMER = new Warhammer();
	public static WarhammerHead WARHAMMER_HEAD = new WarhammerHead();

	// Arena Fragments
	public static KeyFragment KEY_FRAGMENT = new KeyFragment();
	public static SorceryFragment SORCERY_FRAGMENT = new SorceryFragment();
	
	// Crystals
	public static ChaosCrystal CHAOS_CRYSTAL = new ChaosCrystal();
	public static EnchantersCrystal ENCHANTERS_CRYSTAL = new EnchantersCrystal();
	
	// Titan weaponry
	public static MaartensMight MAARTENS_MIGHT = new MaartensMight();
	public static RaffehsRage RAFFEHS_RAGE = new RaffehsRage();
	public static VindictivesVengeance VINDICTIVES_VENGEANCE = new VindictivesVengeance();
	public static WilhelmsWrath WILHELMS_WRATH = new WilhelmsWrath();

	// Spectral set
	public static SpectralHide SPECTRAL_HIDE = new SpectralHide();
	public static SpectralBoot SPECTRAL_BOOT = new SpectralBoot();
	public static SpectralCap SPECTRAL_CAP = new SpectralCap();
	public static SpectralGlove SPECTRAL_GLOVE = new SpectralGlove();
	public static SpectralHose SPECTRAL_HOSE = new SpectralHose();
	public static SpectralJacket SPECTRAL_JACKET = new SpectralJacket();
	public static SpectralSleeve SPECTRAL_SLEEVE = new SpectralSleeve();

	// Glimmerscale set
	public static Glimmerscale GLIMMERSCALE = new Glimmerscale();
	public static GlimmerscaleBoot GLIMMERSCALE_BOOT = new GlimmerscaleBoot();
	public static GlimmerscaleGlove GLIMMERSCALE_GLOVE = new GlimmerscaleGlove();
	public static GlimmerscaleHelmet GLIMMERSCALE_HELMET = new GlimmerscaleHelmet();
	public static GlimmerscaleHose GLIMMERSCALE_HOSE = new GlimmerscaleHose();
	public static GlimmerscaleSleeve GLIMMERSCALE_SLEEVE = new GlimmerscaleSleeve();
	public static GlimmerscaleVest GLIMMERSCALE_VEST = new GlimmerscaleVest();
	
	public static void createItems(){
		logger.info("createItems()");
		try{
		    AFFINITY_CATCHER.createTemplate();
			AFFINITY_ORB.createTemplate();
			ARENA_CACHE.createTemplate();
			ARENA_SUPPLY_DEPOT.createTemplate();
			ARROW_PACK_HUNTING.createTemplate();
			ARROW_PACK_WAR.createTemplate();
			BATTLE_YOYO.createTemplate();
            BOOK_OF_CONVERSION.createTemplate();
			CLUB.createTemplate();
			COIN_DECORATION.createTemplate();
			CORPSE_DECORATION.createTemplate();
			DEPTH_DRILL.createTemplate();
			DISINTEGRATION_ROD.createTemplate();
			ENCHANT_ORB.createTemplate();
			ETERNAL_ORB.createTemplate();
			ETERNAL_RESERVOIR.createTemplate();
			EVISCERATOR.createTemplate();
			FRIYAN_TABLET.createTemplate();
			HUGE_CRATE.createTemplate();
			KNUCKLES.createTemplate();
			MASS_STORAGE_UNIT.createTemplate();
			SEALED_MAP.createTemplate();
			SKELETON_DECORATION.createTemplate();
			SOUL.createTemplate();
			STATUETTE_BREYK.createTemplate();
			STATUETTE_CYBERHUSKY.createTemplate();
			TREASURE_BOX.createTemplate();
			WARHAMMER.createTemplate();
			WARHAMMER_HEAD.createTemplate();

			// Arena Fragments
			KEY_FRAGMENT.createTemplate();
			SORCERY_FRAGMENT.createTemplate();
			
			// Crystals
			CHAOS_CRYSTAL.createTemplate();
			ENCHANTERS_CRYSTAL.createTemplate();
			
			// Titan weaponry
			MAARTENS_MIGHT.createTemplate();
			RAFFEHS_RAGE.createTemplate();
			VINDICTIVES_VENGEANCE.createTemplate();
			WILHELMS_WRATH.createTemplate();

			// Spectral set
			SPECTRAL_HIDE.createTemplate();
			SPECTRAL_BOOT.createTemplate();
			SPECTRAL_CAP.createTemplate();
			SPECTRAL_GLOVE.createTemplate();
			SPECTRAL_HOSE.createTemplate();
			SPECTRAL_JACKET.createTemplate();
			SPECTRAL_SLEEVE.createTemplate();
			
			// Glimmerscale set
			GLIMMERSCALE.createTemplate();
			GLIMMERSCALE_BOOT.createTemplate();
			GLIMMERSCALE_GLOVE.createTemplate();
			GLIMMERSCALE_HELMET.createTemplate();
			GLIMMERSCALE_HOSE.createTemplate();
			GLIMMERSCALE_SLEEVE.createTemplate();
			GLIMMERSCALE_VEST.createTemplate();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static void registerActions(){
	    ModActions.registerAction(new AffinityCatcherCaptureAction());
	    ModActions.registerAction(new AffinityCatcherConsumeAction());
		ModActions.registerAction(new AffinityOrbAction());
		ModActions.registerAction(new ArenaCacheOpenAction());
		ModActions.registerAction(new ArrowPackUnpackAction());
		ModActions.registerAction(new BookConversionAction());
		ModActions.registerAction(new CrystalCombineAction());
		ModActions.registerAction(new ChaosCrystalInfuseAction());
		ModActions.registerAction(new DepthDrillAction());
		ModActions.registerAction(new DisintegrationRodAction());
		ModActions.registerAction(new EnchantersCrystalInfuseAction());
		ModActions.registerAction(new EnchantOrbAction());
		ModActions.registerAction(new EternalOrbAction());
		ModActions.registerAction(new FriyanTabletAction());
        ModActions.registerAction(new KeyCombinationAction());
		ModActions.registerAction(new SealedMapAction());
		ModActions.registerAction(new SupplyDepotAction());
		ModActions.registerAction(new TreasureBoxAction());
	}
	
	public static void initCreationEntries(){
		if (WyvernMods.craftHuntingArrowPacks) {
			ARROW_PACK_HUNTING.initCreationEntry();
		}
		if (WyvernMods.craftWarArrowPacks) {
			ARROW_PACK_WAR.initCreationEntry();
		}
		if (WyvernMods.craftBattleYoyo) {
			BATTLE_YOYO.initCreationEntry();
		}
		if (WyvernMods.craftClub) {
			CLUB.initCreationEntry();
		}
		//COIN_DECORATION.initCreationEntry();
		//CORPSE_DECORATION.initCreationEntry();
		if (WyvernMods.craftDepthDrill) {
			DEPTH_DRILL.initCreationEntry();
		}
		if (WyvernMods.craftEternalReservoir) {
			ETERNAL_RESERVOIR.initCreationEntry();
		}
		if (WyvernMods.craftEviscerator) {
			EVISCERATOR.initCreationEntry();
		}
		if (WyvernMods.craftKnuckles) {
			KNUCKLES.initCreationEntry();
		}
		if (WyvernMods.craftMassStorageUnit) {
			MASS_STORAGE_UNIT.initCreationEntry();
		}
		//SKELETON_DECORATION.initCreationEntry();
		if (WyvernMods.craftStatuetteDeities) {
			STATUETTE_BREYK.initCreationEntry();
			STATUETTE_CYBERHUSKY.initCreationEntry();
		}
		if (WyvernMods.craftWarhammer) {
			WARHAMMER.initCreationEntry();
			WARHAMMER_HEAD.initCreationEntry();
		}

		// Spectral set
		/*SPECTRAL_BOOT.initCreationEntry();
		SPECTRAL_CAP.initCreationEntry();
		SPECTRAL_GLOVE.initCreationEntry();
		SPECTRAL_HOSE.initCreationEntry();
		SPECTRAL_JACKET.initCreationEntry();
		SPECTRAL_SLEEVE.initCreationEntry();
		
		// Glimmerscale set
		GLIMMERSCALE.initCreationEntry();
		GLIMMERSCALE_BOOT.initCreationEntry();
		GLIMMERSCALE_GLOVE.initCreationEntry();
		GLIMMERSCALE_HELMET.initCreationEntry();
		GLIMMERSCALE_HOSE.initCreationEntry();
		GLIMMERSCALE_SLEEVE.initCreationEntry();
		GLIMMERSCALE_VEST.initCreationEntry();*/
	}
	
	public static void createCustomArmours(){
		try {
			logger.info("Beginning custom armour creation.");

			new ArmourTemplate(SpectralBoot.templateId, ArmourTemplate.ARMOUR_TYPE_LEATHER_DRAGON, 0.002f);
			new ArmourTemplate(SpectralCap.templateId, ArmourTemplate.ARMOUR_TYPE_LEATHER_DRAGON, 0.003f);
			new ArmourTemplate(SpectralGlove.templateId, ArmourTemplate.ARMOUR_TYPE_LEATHER_DRAGON, 0.002f);
			new ArmourTemplate(SpectralHose.templateId, ArmourTemplate.ARMOUR_TYPE_LEATHER_DRAGON, 0.0075f);
			new ArmourTemplate(SpectralJacket.templateId, ArmourTemplate.ARMOUR_TYPE_LEATHER_DRAGON, 0.01f);
			new ArmourTemplate(SpectralSleeve.templateId, ArmourTemplate.ARMOUR_TYPE_LEATHER_DRAGON, 0.004f);

			new ArmourTemplate(GlimmerscaleBoot.templateId, ArmourTemplate.ARMOUR_TYPE_SCALE_DRAGON, 0.002f);
			new ArmourTemplate(GlimmerscaleGlove.templateId, ArmourTemplate.ARMOUR_TYPE_SCALE_DRAGON, 0.001f);
			new ArmourTemplate(GlimmerscaleHelmet.templateId, ArmourTemplate.ARMOUR_TYPE_SCALE_DRAGON, 0.008f);
			new ArmourTemplate(GlimmerscaleHose.templateId, ArmourTemplate.ARMOUR_TYPE_SCALE_DRAGON, 0.05f);
			new ArmourTemplate(GlimmerscaleSleeve.templateId, ArmourTemplate.ARMOUR_TYPE_SCALE_DRAGON, 0.008f);
			new ArmourTemplate(GlimmerscaleVest.templateId, ArmourTemplate.ARMOUR_TYPE_SCALE_DRAGON, 0.05f);
			
			//ReflectionUtil.setPrivateField(null, ReflectionUtil.getField(Armour.class, "armours"), armours);
		} catch (IllegalArgumentException | ClassCastException e) {
			e.printStackTrace();
		}
	}
	
	public static void createCustomWeapons(){
		try {
			logger.info("Beginning custom weapon creation.");
			new Weapon(BattleYoyo.templateId, 6.85f, 3.75f, 0.012f, 2, 2, 0.0f, 0d);
			new Weapon(Club.templateId, 8.3f, 4.5f, 0.002f, 3, 3, 0.4f, 0.5d);
			new Weapon(Knuckles.templateId, 3.8f, 2.2f, 0.002f, 1, 1, 0.2f, 0.5d);
			new Weapon(Warhammer.templateId, 9.50f, 5.6f, 0.015f, 4, 3, 1f, 0d);
			//new Weapon(ItemList.stoneChisel, 50f, 1f, 0.5f, 8, 1, 3f, -5f);
			// Titan weaponry
			new Weapon(MaartensMight.templateId, 11, 5, 0.02f, 4, 4, 1.0f, 0d);
			new Weapon(RaffehsRage.templateId, 9.5f, 4.25f, 0.02f, 3, 3, 1.0f, 0d);
			new Weapon(VindictivesVengeance.templateId, 9, 4f, 0.02f, 3, 3, 0.5f, 0d);
			new Weapon(WilhelmsWrath.templateId, 6f, 4.5f, 0.02f, 6, 6, 0.5f, 0d);
			// Genocide weapon
			new Weapon(Eviscerator.templateId, 100, 3f, 0.02f, 5, 5, 0.4f, 0.5d);
		} catch (IllegalArgumentException | ClassCastException e) {
			e.printStackTrace();
		}
	}
	
	public static int getModdedImproveTemplateId(Item item){
		if(item.getTemplateId() == SpectralBoot.templateId){
			return SpectralHide.templateId;
		}else if(item.getTemplateId() == SpectralCap.templateId){
			return SpectralHide.templateId;
		}else if(item.getTemplateId() == SpectralGlove.templateId){
			return SpectralHide.templateId;
		}else if(item.getTemplateId() == SpectralHose.templateId){
			return SpectralHide.templateId;
		}else if(item.getTemplateId() == SpectralJacket.templateId){
			return SpectralHide.templateId;
		}else if(item.getTemplateId() == SpectralSleeve.templateId){
			return SpectralHide.templateId;
		}else if(item.getTemplateId() == GlimmerscaleBoot.templateId){
			return Glimmerscale.templateId;
		}else if(item.getTemplateId() == GlimmerscaleGlove.templateId){
			return Glimmerscale.templateId;
		}else if(item.getTemplateId() == GlimmerscaleHelmet.templateId){
			return Glimmerscale.templateId;
		}else if(item.getTemplateId() == GlimmerscaleHose.templateId){
			return Glimmerscale.templateId;
		}else if(item.getTemplateId() == GlimmerscaleSleeve.templateId){
			return Glimmerscale.templateId;
		}else if(item.getTemplateId() == GlimmerscaleVest.templateId){
			return Glimmerscale.templateId;
		}
		return -10;
	}

	private static void setFragments(int templateId, int fragmentCount){
        try {
            ItemTemplate item = ItemTemplateFactory.getInstance().getTemplate(templateId);
            ReflectionUtil.setPrivateField(item, ReflectionUtil.getField(item.getClass(), "fragmentAmount"), fragmentCount);
        } catch (IllegalAccessException | NoSuchFieldException | NoSuchTemplateException e) {
            e.printStackTrace();
        }
    }
	
	public static void modifyItems() throws NoSuchTemplateException, IllegalArgumentException, IllegalAccessException, ClassCastException, NoSuchFieldException{
		// Make leather able to be combined.
		if (WyvernMods.combineLeather) {
			ItemTemplate leather = ItemTemplateFactory.getInstance().getTemplate(ItemList.leather);
			ReflectionUtil.setPrivateField(leather, ReflectionUtil.getField(leather.getClass(), "combine"), true);
		}

        // Make logs able to be combined. Also reduce their volume.
        ItemTemplate log = ItemTemplateFactory.getInstance().getTemplate(ItemList.log);
		if (WyvernMods.combineLogs) {
			ReflectionUtil.setPrivateField(log, ReflectionUtil.getField(log.getClass(), "combine"), true);
		}
		if (WyvernMods.reduceLogVolume) {
			ReflectionUtil.setPrivateField(log, ReflectionUtil.getField(log.getClass(), "centimetersZ"), 50);
			int newVolume = log.getSizeX() * log.getSizeY() * log.getSizeZ();
			ReflectionUtil.setPrivateField(log, ReflectionUtil.getField(log.getClass(), "volume"), newVolume);
		}

		// Reduce kindling volume as well to make sure they're not larger than logs.
		if (WyvernMods.reduceKindlingVolume) {
			ItemTemplate kindling = ItemTemplateFactory.getInstance().getTemplate(ItemList.kindling);
			ReflectionUtil.setPrivateField(kindling, ReflectionUtil.getField(kindling.getClass(), "centimetersY"), 10);
			ReflectionUtil.setPrivateField(kindling, ReflectionUtil.getField(kindling.getClass(), "centimetersZ"), 10);
			int newKindlingVolume = kindling.getSizeX() * kindling.getSizeY() * kindling.getSizeZ();
			ReflectionUtil.setPrivateField(kindling, ReflectionUtil.getField(kindling.getClass(), "volume"), newKindlingVolume);
		}

        // Allow sleep powder to be dropped.
		if (WyvernMods.droppableSleepPowder) {
			ItemTemplate sleepPowder = ItemTemplateFactory.getInstance().getTemplate(ItemList.sleepPowder);
			ReflectionUtil.setPrivateField(sleepPowder, ReflectionUtil.getField(sleepPowder.getClass(), "nodrop"), false);
		}

		// Set silver mirror price to 20 silver instead of 1 iron.
		if (WyvernMods.setSilverMirrorPrice) {
			ItemTemplate handMirror = ItemTemplateFactory.getInstance().getTemplate(ItemList.handMirror);
			ReflectionUtil.setPrivateField(handMirror, ReflectionUtil.getField(handMirror.getClass(), "value"), 200000);
		}
		// Set golden mirror price to 1 gold instead of 1 iron.
		if (WyvernMods.setGoldMirrorPrice) {
			ItemTemplate goldMirror = ItemTemplateFactory.getInstance().getTemplate(ItemList.goldenMirror);
			ReflectionUtil.setPrivateField(goldMirror, ReflectionUtil.getField(goldMirror.getClass(), "value"), 1000000);
		}

		// Creature crates to 10 silver.
		if (WyvernMods.setCreatureCratePrice) {
			ItemTemplate creatureCage = ItemTemplateFactory.getInstance().getTemplate(ItemList.creatureCrate);
			ReflectionUtil.setPrivateField(creatureCage, ReflectionUtil.getField(creatureCage.getClass(), "value"), 100000);
			ReflectionUtil.setPrivateField(creatureCage, ReflectionUtil.getField(creatureCage.getClass(), "fullprice"), true);
		}

        // Resurrection Stones to 2 silver instead of 5 silver.
		if (WyvernMods.setResurrectionStonePrice) {
			ItemTemplate resurrectionStone = ItemTemplateFactory.getInstance().getTemplate(ItemList.resurrectionStone);
			ReflectionUtil.setPrivateField(resurrectionStone, ReflectionUtil.getField(resurrectionStone.getClass(), "value"), 20000);
		}

		// Shaker Orbs to 2 silver instead of 5 silver.
		if (WyvernMods.setShakerOrbPrice) {
			ItemTemplate shakerOrb = ItemTemplateFactory.getInstance().getTemplate(ItemList.shakerOrb);
			ReflectionUtil.setPrivateField(shakerOrb, ReflectionUtil.getField(shakerOrb.getClass(), "value"), 20000);
		}

		// Set transmutation rod to 2 gold instead of 50 silver.
		//ItemTemplate transmutationRod = ItemTemplateFactory.getInstance().getTemplate(668);
		//ReflectionUtil.setPrivateField(transmutationRod, ReflectionUtil.getField(transmutationRod.getClass(), "value"), 2000000);

		// "  return this.isTransportable || (this.getTemplateId() >= 510 && this.getTemplateId() <= 513) || this.getTemplateId() == 722 || this.getTemplateId() == 670;"
		// Make mailboxes loadable (PvE Only)
        if(WyvernMods.loadableMailbox && !Servers.localServer.PVPSERVER) {
            ItemTemplate mailboxWood = ItemTemplateFactory.getInstance().getTemplate(ItemList.mailboxWood);
            ReflectionUtil.setPrivateField(mailboxWood, ReflectionUtil.getField(mailboxWood.getClass(), "isTransportable"), true);
            ItemTemplate mailboxStone = ItemTemplateFactory.getInstance().getTemplate(ItemList.mailboxStone);
            ReflectionUtil.setPrivateField(mailboxStone, ReflectionUtil.getField(mailboxStone.getClass(), "isTransportable"), true);
            ItemTemplate mailboxWood2 = ItemTemplateFactory.getInstance().getTemplate(ItemList.mailboxWoodTwo);
            ReflectionUtil.setPrivateField(mailboxWood2, ReflectionUtil.getField(mailboxWood2.getClass(), "isTransportable"), true);
            ItemTemplate mailboxStone2 = ItemTemplateFactory.getInstance().getTemplate(ItemList.mailboxStoneTwo);
            ReflectionUtil.setPrivateField(mailboxStone2, ReflectionUtil.getField(mailboxStone2.getClass(), "isTransportable"), true);
        }
		
		// Make bell towers and trash bins loadable
		if (WyvernMods.loadableBellTower) {
			ItemTemplate bellTower = ItemTemplateFactory.getInstance().getTemplate(ItemList.bellTower);
			ReflectionUtil.setPrivateField(bellTower, ReflectionUtil.getField(bellTower.getClass(), "isTransportable"), true);
		}
		if (WyvernMods.loadableTrashBin) {
			ItemTemplate trashBin = ItemTemplateFactory.getInstance().getTemplate(ItemList.trashBin);
			ReflectionUtil.setPrivateField(trashBin, ReflectionUtil.getField(trashBin.getClass(), "isTransportable"), true);
		}
		
		// Make altars loadable
		if (WyvernMods.loadableAltars) {
			ItemTemplate stoneAltar = ItemTemplateFactory.getInstance().getTemplate(ItemList.altarStone);
			ReflectionUtil.setPrivateField(stoneAltar, ReflectionUtil.getField(stoneAltar.getClass(), "isTransportable"), true);
			ItemTemplate woodAltar = ItemTemplateFactory.getInstance().getTemplate(ItemList.altarWood);
			ReflectionUtil.setPrivateField(woodAltar, ReflectionUtil.getField(woodAltar.getClass(), "isTransportable"), true);
			ItemTemplate silverAltar = ItemTemplateFactory.getInstance().getTemplate(ItemList.altarSilver);
			ReflectionUtil.setPrivateField(silverAltar, ReflectionUtil.getField(silverAltar.getClass(), "isTransportable"), true);
			ItemTemplate goldAltar = ItemTemplateFactory.getInstance().getTemplate(ItemList.altarGold);
			ReflectionUtil.setPrivateField(goldAltar, ReflectionUtil.getField(goldAltar.getClass(), "isTransportable"), true);
		}
		
		// Make long spears one-handed.
		if (WyvernMods.oneHandedLongSpear) {
			ItemTemplate longSpear = ItemTemplateFactory.getInstance().getTemplate(ItemList.spearLong);
			ReflectionUtil.setPrivateField(longSpear, ReflectionUtil.getField(longSpear.getClass(), "isTwohanded"), false);
		}
		
		// Make dirt/sand difficulty easier
		if (WyvernMods.reduceDirtDifficulty) {
			ItemTemplate dirt = ItemTemplateFactory.getInstance().getTemplate(ItemList.dirtPile);
			ReflectionUtil.setPrivateField(dirt, ReflectionUtil.getField(dirt.getClass(), "difficulty"), 50.0f);
		}

		if (WyvernMods.reduceSandDifficulty) {
			ItemTemplate sand = ItemTemplateFactory.getInstance().getTemplate(ItemList.sand);
			ReflectionUtil.setPrivateField(sand, ReflectionUtil.getField(sand.getClass(), "difficulty"), 50.0f);
		}

		if (WyvernMods.reduceSandstoneDifficulty) {
			ItemTemplate sandstone = ItemTemplateFactory.getInstance().getTemplate(ItemList.sandstone);
			ReflectionUtil.setPrivateField(sandstone, ReflectionUtil.getField(sandstone.getClass(), "difficulty"), 50.0f);
		}

        // Make some useless items decorations for added interior design.
		if (WyvernMods.decorationStoneKeystone) {
			ItemTemplate stoneKeystone = ItemTemplateFactory.getInstance().getTemplate(ItemList.stoneKeystone);
			ReflectionUtil.setPrivateField(stoneKeystone, ReflectionUtil.getField(stoneKeystone.getClass(), "decoration"), true);
		}
		if (WyvernMods.decorationMarbleKeystone) {
			ItemTemplate marbleKeystone = ItemTemplateFactory.getInstance().getTemplate(ItemList.marbleKeystone);
			ReflectionUtil.setPrivateField(marbleKeystone, ReflectionUtil.getField(marbleKeystone.getClass(), "decoration"), true);
		}
		if (WyvernMods.decorationSkull) {
			ItemTemplate skull = ItemTemplateFactory.getInstance().getTemplate(ItemList.skull);
			ReflectionUtil.setPrivateField(skull, ReflectionUtil.getField(skull.getClass(), "decoration"), true);
		}

        // Modify fragment counts
		if (WyvernMods.useCustomCacheFragments) {
			setFragments(ArmourCache.templateId, 18);
			setFragments(ArtifactCache.templateId, 33);
			setFragments(CrystalCache.templateId, 11);
			setFragments(DragonCache.templateId, 19);
			setFragments(GemCache.templateId, 7);
			setFragments(MoonCache.templateId, 14);
			setFragments(PotionCache.templateId, 18);
			setFragments(RiftCache.templateId, 24);
			setFragments(TitanCache.templateId, 100);
			setFragments(ToolCache.templateId, 27);
			setFragments(TreasureMapCache.templateId, 38);

			setFragments(AffinityOrb.templateId, 20);
		}

		if (WyvernMods.adjustStatueFragmentCount) {
			// Tier 4
			setFragments(ItemList.statueWorg, 40);
			setFragments(ItemList.statueEagle, 40);

			// Tier 5
			setFragments(ItemList.statueHellHorse, 45);
			setFragments(ItemList.statueDrake, 45);

			// Tier 6
			setFragments(ItemList.statueFo, 50);
			setFragments(ItemList.statueMagranon, 50);
			setFragments(ItemList.statueLibila, 50);
			setFragments(ItemList.statueVynora, 50);
		}

		if (WyvernMods.enableCustomItemCreation) {
			createCustomWeapons();
			createCustomArmours();
		}

		// Make huge crates larger
		//ItemTemplate hugeCrate = ItemTemplateFactory.getInstance().getTemplate(HUGE_CRATE.getTemplateId());
		//ReflectionUtil.setPrivateField(hugeCrate, ReflectionUtil.getField(hugeCrate.getClass(), "combine"), true);
	}

	public static void onServerStarted(){
		if (WyvernMods.removeLockpickSkillRequirement) {
			CreationEntry lockpicks = CreationMatrix.getInstance().getCreationEntry(ItemList.lockpick);
			try {
				ReflectionUtil.setPrivateField(lockpicks, ReflectionUtil.getField(lockpicks.getClass(), "hasMinimumSkillRequirement"), false);
				ReflectionUtil.setPrivateField(lockpicks, ReflectionUtil.getField(lockpicks.getClass(), "minimumSkill"), 0.0);
			} catch (IllegalAccessException | NoSuchFieldException e) {
				logger.info("Failed to set lockpick creation entry changes!");
				e.printStackTrace();
			}
		}
	}
	
	public static void registerPermissionsHook(){
		try {
            CtClass[] input = {
                HookManager.getInstance().getClassPool().get("com.wurmonline.server.creatures.Creature"),
                HookManager.getInstance().getClassPool().get("com.wurmonline.server.items.Item")

            };
            CtClass output = HookManager.getInstance().getClassPool().get("java.util.List");

            HookManager.getInstance().registerHook("com.wurmonline.server.behaviours.VehicleBehaviour", "getVehicleBehaviours",
                    Descriptor.ofMethod(output, input), new InvocationHandlerFactory() {
                        @Override
                        public InvocationHandler createInvocationHandler() {
                            return new InvocationHandler() {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                    @SuppressWarnings("unchecked")
									List<ActionEntry> original = (List<ActionEntry>) method.invoke(proxy, args);
                                    Item item = (Item) args[1];
                                    Creature performer = (Creature) args[0];
                                    LinkedList<ActionEntry> permissions = new LinkedList<ActionEntry>();
                                    if (item.mayManage(performer)) {
                                        int itemId = item.getTemplateId();
                                        if (itemId == MassStorageUnit.templateId) {
                                            //debug("Adding manage permissions");
                                            permissions.add(new ActionEntry((short)669, "Manage Storage Unit", "viewing"));
                                        }
                                    }
                                    if (item.maySeeHistory(performer)) {
                                        int itemId = item.getTemplateId();
                                        if (itemId == MassStorageUnit.templateId) {
                                            permissions.add(new ActionEntry((short)691, "History of Storage Unit", "viewing"));
                                        }
                                    }
                                    if (!permissions.isEmpty()) {
                                        if (permissions.size() > 1) {
                                            Collections.sort(permissions);
                                            original.add(new ActionEntry((short)(- permissions.size()), "Permissions", "viewing"));
                                        }
                                        original.addAll(permissions);
                                    }
                                    return original;
                                }

                            };
                        }
                    });
        }
        catch (Exception e) {
            logger.info("Permission hook: " + e.toString());
        }
	}
}
