package mod.sin.wyvern;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.wurmonline.server.Servers;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.combat.Armour;
import com.wurmonline.server.combat.Weapon;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
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
	public static EnchantOrb ENCHANT_ORB = new EnchantOrb();
	public static Eviscerator EVISCERATOR = new Eviscerator();
	public static FriyanTablet FRIYAN_TABLET = new FriyanTablet();
	public static HugeCrate HUGE_CRATE = new HugeCrate();
	public static Knuckles KNUCKLES = new Knuckles();
	public static MassStorageUnit MASS_STORAGE_UNIT = new MassStorageUnit();
	public static SealedMap SEALED_MAP = new SealedMap();
	public static SkeletonDecoration SKELETON_DECORATION = new SkeletonDecoration();
	public static Soul SOUL = new Soul();
	public static EternalReservoir SOUL_FORGE = new EternalReservoir();
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
			EVISCERATOR.createTemplate();
			FRIYAN_TABLET.createTemplate();
			HUGE_CRATE.createTemplate();
			KNUCKLES.createTemplate();
			MASS_STORAGE_UNIT.createTemplate();
			SEALED_MAP.createTemplate();
			SKELETON_DECORATION.createTemplate();
			SOUL.createTemplate();
			SOUL_FORGE.createTemplate();
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
		ModActions.registerAction(new FriyanTabletAction());
		ModActions.registerAction(new SealedMapAction());
		ModActions.registerAction(new SupplyDepotAction());
		ModActions.registerAction(new TreasureBoxAction());
	}
	
	public static void initCreationEntries(){
		logger.info("initCreationEntries()");
		ARROW_PACK_HUNTING.initCreationEntry();
		ARROW_PACK_WAR.initCreationEntry();
		BATTLE_YOYO.initCreationEntry();
		CLUB.initCreationEntry();
		//COIN_DECORATION.initCreationEntry();
		//CORPSE_DECORATION.initCreationEntry();
		DEPTH_DRILL.initCreationEntry();
		EVISCERATOR.initCreationEntry();
		KNUCKLES.initCreationEntry();
		MASS_STORAGE_UNIT.initCreationEntry();
		//SKELETON_DECORATION.initCreationEntry();
		SOUL_FORGE.initCreationEntry();
		//STATUETTE_BREYK.initCreationEntry();
		//STATUETTE_CYBERHUSKY.initCreationEntry();
		WARHAMMER.initCreationEntry();
		WARHAMMER_HEAD.initCreationEntry();

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
		
		// Allow sickle heads from steel & moon metals:
		// [3/28/18] Disabled: Implemented in WU 1.6.
		/*CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_BLADES, ItemList.anvilLarge, ItemList.steelBar,
				ItemList.sickleBlade, false, true, 0.0f, false, false, CreationCategories.BLADES);
		CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_BLADES, ItemList.anvilLarge, ItemList.adamantineBar,
				ItemList.sickleBlade, false, true, 0.0f, false, false, CreationCategories.BLADES);
		CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_BLADES, ItemList.anvilLarge, ItemList.glimmerSteelBar,
				ItemList.sickleBlade, false, true, 0.0f, false, false, CreationCategories.BLADES);
		CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_BLADES, ItemList.anvilLarge, ItemList.seryllBar,
				ItemList.sickleBlade, false, true, 0.0f, false, false, CreationCategories.BLADES);
		// Allow steel staff to be created from moon metals:
		CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_BLADES, ItemList.anvilLarge, ItemList.adamantineBar,
				ItemList.staffSteel, false, true, 0.0f, false, false, CreationCategories.BLADES);
		CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_BLADES, ItemList.anvilLarge, ItemList.glimmerSteelBar,
				ItemList.staffSteel, false, true, 0.0f, false, false, CreationCategories.BLADES);
		CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_BLADES, ItemList.anvilLarge, ItemList.seryllBar,
				ItemList.staffSteel, false, true, 0.0f, false, false, CreationCategories.BLADES);*/
	}
	
	public static void createCustomArmours(){
		try {
			logger.info("Beginning custom armour creation.");
			Map<Integer, Armour> armours = ReflectionUtil.getPrivateField(null, ReflectionUtil.getField(Armour.class, "armours"));

			armours.put(SpectralBoot.templateId, new Armour(SpectralBoot.templateId, 0.002f, 0.3f));
			armours.put(SpectralCap.templateId, new Armour(SpectralCap.templateId, 0.003f, 0.3f));
			armours.put(SpectralGlove.templateId, new Armour(SpectralGlove.templateId, 0.002f, 0.3f));
			armours.put(SpectralHose.templateId, new Armour(SpectralHose.templateId, 0.0075f, 0.3f));
			armours.put(SpectralJacket.templateId, new Armour(SpectralJacket.templateId, 0.01f, 0.3f));
			armours.put(SpectralSleeve.templateId, new Armour(SpectralSleeve.templateId, 0.004f, 0.3f));

			armours.put(GlimmerscaleBoot.templateId, new Armour(GlimmerscaleBoot.templateId, 0.002f, 0.15f));
			armours.put(GlimmerscaleGlove.templateId, new Armour(GlimmerscaleGlove.templateId, 0.001f, 0.15f));
			armours.put(GlimmerscaleHelmet.templateId, new Armour(GlimmerscaleHelmet.templateId, 0.008f, 0.15f));
			armours.put(GlimmerscaleHose.templateId, new Armour(GlimmerscaleHose.templateId, 0.05f, 0.15f));
			armours.put(GlimmerscaleSleeve.templateId, new Armour(GlimmerscaleSleeve.templateId, 0.008f, 0.15f));
			armours.put(GlimmerscaleVest.templateId, new Armour(GlimmerscaleVest.templateId, 0.05f, 0.15f));
			
			//ReflectionUtil.setPrivateField(null, ReflectionUtil.getField(Armour.class, "armours"), armours);
		} catch (IllegalArgumentException | IllegalAccessException | ClassCastException | NoSuchFieldException e) {
			e.printStackTrace();
		}
	}
	
	public static void createCustomWeapons(){
		try {
			logger.info("Beginning custom weapon creation.");
			new Weapon(BattleYoyo.templateId, 6.85f, 3.85f, 0.008f, 2, 2, 0.0f, 0d);
			new Weapon(Club.templateId, 8.1f, 4.5f, 0.002f, 3, 3, 0.4f, 0.5d);
			new Weapon(Knuckles.templateId, 3.6f, 2.2f, 0.002f, 1, 1, 0.2f, 0.5d);
			new Weapon(Warhammer.templateId, 9.55f, 5.5f, 0.008f, 4, 3, 1f, 0d);
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
	
	public static void modifyItems() throws NoSuchTemplateException, IllegalArgumentException, IllegalAccessException, ClassCastException, NoSuchFieldException{
		// Make leather able to be combined.
		ItemTemplate leather = ItemTemplateFactory.getInstance().getTemplate(ItemList.leather);
		ReflectionUtil.setPrivateField(leather, ReflectionUtil.getField(leather.getClass(), "combine"), true);

        // Make logs able to be combined.
        ItemTemplate log = ItemTemplateFactory.getInstance().getTemplate(ItemList.log);
        ReflectionUtil.setPrivateField(log, ReflectionUtil.getField(log.getClass(), "combine"), true);

		// Set silver mirror price to 10 silver instead of 1 iron.
		ItemTemplate handMirror = ItemTemplateFactory.getInstance().getTemplate(ItemList.handMirror);
		ReflectionUtil.setPrivateField(handMirror, ReflectionUtil.getField(handMirror.getClass(), "value"), 200000);
		ItemTemplate goldMirror = ItemTemplateFactory.getInstance().getTemplate(ItemList.goldenMirror);
		ReflectionUtil.setPrivateField(goldMirror, ReflectionUtil.getField(goldMirror.getClass(), "value"), 1000000);

		// Set transmutation rod to 2 gold instead of 50 silver.
		//ItemTemplate transmutationRod = ItemTemplateFactory.getInstance().getTemplate(668);
		//ReflectionUtil.setPrivateField(transmutationRod, ReflectionUtil.getField(transmutationRod.getClass(), "value"), 2000000);

		// "  return this.isTransportable || (this.getTemplateId() >= 510 && this.getTemplateId() <= 513) || this.getTemplateId() == 722 || this.getTemplateId() == 670;"
		// Make mailboxes loadable (PvE Only)
        if(!Servers.localServer.PVPSERVER) {
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
		ItemTemplate bellTower = ItemTemplateFactory.getInstance().getTemplate(ItemList.bellTower);
		ReflectionUtil.setPrivateField(bellTower, ReflectionUtil.getField(bellTower.getClass(), "isTransportable"), true);
		ItemTemplate trashBin = ItemTemplateFactory.getInstance().getTemplate(ItemList.trashBin);
		ReflectionUtil.setPrivateField(trashBin, ReflectionUtil.getField(trashBin.getClass(), "isTransportable"), true);
		
		// Make altars loadable
		ItemTemplate stoneAltar = ItemTemplateFactory.getInstance().getTemplate(ItemList.altarStone);
		ReflectionUtil.setPrivateField(stoneAltar, ReflectionUtil.getField(stoneAltar.getClass(), "isTransportable"), true);
		ItemTemplate woodAltar = ItemTemplateFactory.getInstance().getTemplate(ItemList.altarWood);
		ReflectionUtil.setPrivateField(woodAltar, ReflectionUtil.getField(woodAltar.getClass(), "isTransportable"), true);
		ItemTemplate silverAltar = ItemTemplateFactory.getInstance().getTemplate(ItemList.altarSilver);
		ReflectionUtil.setPrivateField(silverAltar, ReflectionUtil.getField(silverAltar.getClass(), "isTransportable"), true);
		ItemTemplate goldAltar = ItemTemplateFactory.getInstance().getTemplate(ItemList.altarGold);
		ReflectionUtil.setPrivateField(goldAltar, ReflectionUtil.getField(goldAltar.getClass(), "isTransportable"), true);
		
		// Make long spears one-handed.
		ItemTemplate longSpear = ItemTemplateFactory.getInstance().getTemplate(ItemList.spearLong);
		ReflectionUtil.setPrivateField(longSpear, ReflectionUtil.getField(longSpear.getClass(), "isTwohanded"), false);
		
		// Make dirt/sand difficulty easier
        ItemTemplate dirt = ItemTemplateFactory.getInstance().getTemplate(ItemList.dirtPile);
        ReflectionUtil.setPrivateField(dirt, ReflectionUtil.getField(dirt.getClass(), "difficulty"), 50.0f);

        ItemTemplate sand = ItemTemplateFactory.getInstance().getTemplate(ItemList.sand);
        ReflectionUtil.setPrivateField(sand, ReflectionUtil.getField(sand.getClass(), "difficulty"), 50.0f);

        ItemTemplate sandstone = ItemTemplateFactory.getInstance().getTemplate(ItemList.sandstone);
        ReflectionUtil.setPrivateField(sandstone, ReflectionUtil.getField(sandstone.getClass(), "difficulty"), 50.0f);

        // Make some useless items decorations for added interior design.
        ItemTemplate stoneKeystone = ItemTemplateFactory.getInstance().getTemplate(ItemList.stoneKeystone);
        ReflectionUtil.setPrivateField(stoneKeystone, ReflectionUtil.getField(stoneKeystone.getClass(), "decoration"), true);
        ItemTemplate marbleKeystone = ItemTemplateFactory.getInstance().getTemplate(ItemList.marbleKeystone);
        ReflectionUtil.setPrivateField(marbleKeystone, ReflectionUtil.getField(marbleKeystone.getClass(), "decoration"), true);
        ItemTemplate skull = ItemTemplateFactory.getInstance().getTemplate(ItemList.skull);
        ReflectionUtil.setPrivateField(skull, ReflectionUtil.getField(skull.getClass(), "decoration"), true);
		
		createCustomWeapons();
		createCustomArmours();

		// Make huge crates larger
		//ItemTemplate hugeCrate = ItemTemplateFactory.getInstance().getTemplate(HUGE_CRATE.getTemplateId());
		//ReflectionUtil.setPrivateField(hugeCrate, ReflectionUtil.getField(hugeCrate.getClass(), "combine"), true);
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
