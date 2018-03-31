package mod.sin.wyvern;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.pveplands.treasurehunting.Treasuremap;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.shared.constants.Enchants;

import mod.sin.actions.items.TreasureCacheOpenAction;
import mod.sin.armour.*;
import mod.sin.items.*;
import mod.sin.items.caches.*;
import mod.sin.weapons.*;

public class Caches {
	public static final Logger logger = Logger.getLogger(Caches.class.getName());
	
	public static ArrayList<Integer> CACHE_IDS = new ArrayList<Integer>();
	
	public static AnimalCache ANIMAL_CACHE = new AnimalCache();
	public static ArmourCache ARMOUR_CACHE = new ArmourCache();
	public static ArtifactCache ARTIFACT_CACHE = new ArtifactCache();
	public static CrystalCache CRYSTAL_CACHE = new CrystalCache();
	public static DragonCache DRAGON_CACHE = new DragonCache();
	public static GemCache GEM_CACHE = new GemCache();
	public static MoonCache MOON_CACHE = new MoonCache();
	public static PotionCache POTION_CACHE = new PotionCache();
	public static RiftCache RIFT_CACHE = new RiftCache();
	public static ToolCache TOOL_CACHE = new ToolCache();
	public static TreasureMapCache TREASUREMAP_CACHE = new TreasureMapCache();
	public static WeaponCache WEAPON_CACHE = new WeaponCache();
	
	public static boolean isTreasureCache(Item item){
		int templateId = item.getTemplateId();
		if(CACHE_IDS.contains(templateId)){
			return true;
		}
		return false;
	}
	
	public static float getBaseQuality(float quality){
		return quality*0.5f;
	}
	public static float getRandomQuality(float quality){
		return quality*0.55f;
	}
	public static float getWeightMultiplier(int templateId, float quality){
		if(templateId == DragonCache.templateId){
			return (quality*0.005f)+(quality*0.001f*Server.rand.nextFloat());
		}else if(templateId == MoonCache.templateId){
			return 1f+(quality*0.05f)+(quality*0.05f*Server.rand.nextFloat());
		}
		return 1f+(quality*0.02f);
	}
	public static boolean adjustBasicWeight(int templateId){
		if(templateId == DragonCache.templateId){
			return true;
		}else if(templateId == MoonCache.templateId){
			return true;
		}
		return false;
	}
	public static void applyEnchant(Item item, byte enchant, float power){
		ItemSpellEffects effs = item.getSpellEffects();
		if(effs == null){
			effs = new ItemSpellEffects(item.getWurmId());
		}
		SpellEffect eff = new SpellEffect(item.getWurmId(), enchant, power, 20000000);
		effs.addSpellEffect(eff);
		if(item.getDescription().length() > 0){
			item.setDescription(item.getDescription()+" ");
		}
		item.setDescription(item.getDescription()+eff.getName().substring(0,1)+Math.round(power));
	}
	
	public static boolean createsCustomBasic(int templateId){
		if(templateId == TreasureMapCache.templateId){
			return true;
		}
		return false;
	}
	
	public static void getCustomBasic(Creature performer, Item cache){
		int templateId = cache.getTemplateId();
		if(templateId == TreasureMapCache.templateId){
			Item map = Treasuremap.CreateTreasuremap(performer, cache, null, null, true);
			map.setRarity(cache.getRarity());
			performer.getInventory().insertItem(map, true);
		}
	}
	public static int[] getBasicTemplates(int templateId){
		if(templateId == ArmourCache.templateId){
			return new int[]{
					ItemList.clothGlove, ItemList.clothHood, ItemList.clothHose, ItemList.clothJacket, ItemList.clothJacket, ItemList.clothShirt, ItemList.clothShoes, ItemList.clothSleeve,
					ItemList.leatherBoot, ItemList.leatherCap, ItemList.leatherGlove, ItemList.leatherHose, ItemList.leatherJacket, ItemList.leatherSleeve,
					ItemList.studdedLeatherBoot, ItemList.studdedLeatherCap, ItemList.studdedLeatherGlove, ItemList.studdedLeatherHose, ItemList.studdedLeatherHose, ItemList.studdedLeatherJacket, ItemList.studdedLeatherSleeve,
					ItemList.chainBoot, ItemList.chainCoif, ItemList.chainGlove, ItemList.chainHose, ItemList.chainJacket, ItemList.chainSleeve,
					ItemList.plateBoot, ItemList.plateGauntlet, ItemList.plateHose, ItemList.plateJacket, ItemList.plateSleeve, ItemList.helmetGreat, ItemList.helmetBasinet, ItemList.helmetOpen
			};
		}else if(templateId == ArtifactCache.templateId){
			return new int[]{
					ItemList.swordShort, ItemList.swordLong, ItemList.swordTwoHander,
					ItemList.axeSmall, ItemList.axeMedium, ItemList.axeHuge,
					ItemList.maulSmall, ItemList.maulMedium, ItemList.maulLarge,
					ItemList.spearLong, ItemList.staffSteel, ItemList.halberd,
					Club.templateId, Knuckles.templateId, Warhammer.templateId
			};
		}else if(templateId == CrystalCache.templateId){
			return new int[]{
					ChaosCrystal.templateId, ChaosCrystal.templateId,
					EnchantersCrystal.templateId
			};
		}else if(templateId == DragonCache.templateId){
			return new int[]{
					ItemList.drakeHide,
					ItemList.drakeHide,
					ItemList.dragonScale,
					ItemList.dragonScale,
					SpectralHide.templateId
			};
		}else if(templateId == GemCache.templateId){
			return new int[]{
					ItemList.diamond,
					ItemList.emerald,
					ItemList.opal,
					ItemList.ruby,
					ItemList.sapphire
			};
		}else if(templateId == MoonCache.templateId){
			return new int[]{
					ItemList.glimmerSteelBar,
					ItemList.adamantineBar
			};
		}else if(templateId == PotionCache.templateId){
			return new int[]{
					ItemList.potionAcidDamage,
					ItemList.potionArmourSmithing,
					ItemList.potionBlacksmithing,
					ItemList.potionCarpentry,
					ItemList.potionFireDamage,
					ItemList.potionFletching,
					ItemList.potionFrostDamage,
					ItemList.potionLeatherworking,
					ItemList.potionMasonry,
					ItemList.potionMining,
					ItemList.potionRopemaking,
					ItemList.potionShipbuilding,
					ItemList.potionStonecutting,
					ItemList.potionTailoring,
					ItemList.potionWeaponSmithing,
					ItemList.potionWoodcutting
			};
		}else if(templateId == RiftCache.templateId){
			return new int[]{
					ItemList.riftCrystal,
					ItemList.riftWood,
					ItemList.riftStone
			};
		}
		return null;
	}
	public static void adjustBasicItem(int templateId, float quality, Item item){
		if(templateId == ArmourCache.templateId){
			if(quality > 50){
				if(quality > 95 && Server.rand.nextBoolean()){
					applyEnchant(item, Enchants.BUFF_SHARED_PAIN, quality*Server.rand.nextFloat()*1.2f);
					applyEnchant(item, Enchants.BUFF_WEBARMOUR, quality*Server.rand.nextFloat()*1.2f);
				}else if(Server.rand.nextBoolean()){
					byte[] armourEnchants = {
							Enchants.BUFF_SHARED_PAIN,
							Enchants.BUFF_WEBARMOUR
					};
					applyEnchant(item, armourEnchants[Server.rand.nextInt(armourEnchants.length)], quality*Server.rand.nextFloat()*1.5f);
				}
			}
			if(quality > 80 && Server.rand.nextInt(4) == 0){
				byte[] materials = {
						Materials.MATERIAL_ADAMANTINE,
						Materials.MATERIAL_COTTON,
						Materials.MATERIAL_GLIMMERSTEEL,
						Materials.MATERIAL_IRON,
						Materials.MATERIAL_LEATHER,
						Materials.MATERIAL_SERYLL,
						Materials.MATERIAL_STEEL
				};
				item.setMaterial(materials[Server.rand.nextInt(materials.length)]);
			}else{
				if(item.isMetal()){
					item.setMaterial(Materials.MATERIAL_IRON);
				}else if(item.isLeather()){
					item.setMaterial(Materials.MATERIAL_LEATHER);
				}
			}
		}else if(templateId == ArtifactCache.templateId){
			byte[] materials = {
					Materials.MATERIAL_ADAMANTINE,
					Materials.MATERIAL_GLIMMERSTEEL,
					Materials.MATERIAL_STEEL
			};
			item.setMaterial(materials[Server.rand.nextInt(materials.length)]);
			float qualBoost = (100f-item.getQualityLevel())*Server.rand.nextFloat();
			item.setQualityLevel(item.getQualityLevel()+qualBoost);
			if(quality > 50 && Server.rand.nextInt(200) < quality){
				if(item.getRarity() == 0){
					if(Server.rand.nextInt(600) < quality){
						item.setRarity((byte) 2);
					}else{
						item.setRarity((byte) 1);
					}
				}
			}
			if(quality > 70 && Server.rand.nextBoolean()){
				if(Server.rand.nextBoolean()){
					byte[] enchants = {
							Enchants.BUFF_WIND_OF_AGES,
							Enchants.BUFF_BLESSINGDARK
					};
					applyEnchant(item, enchants[Server.rand.nextInt(enchants.length)], quality*Server.rand.nextFloat()*0.9f);
					applyEnchant(item, Enchants.BUFF_NIMBLENESS, quality*Server.rand.nextFloat()*1.2f);
				}else{
					applyEnchant(item, Enchants.BUFF_LIFETRANSFER, quality+(quality*Server.rand.nextFloat()));
				}
			}
		}else if(templateId == CrystalCache.templateId){
			item.setQualityLevel(Server.rand.nextFloat()*quality);
		}
	}
	public static int getBasicNums(int templateId){
		if(templateId == CrystalCache.templateId){
			return 15;
		}else if(templateId == GemCache.templateId){
			return 2;
		}
		return 1;
	}
	public static int getExtraBasicNums(int templateId, float quality){
		if(templateId == ArmourCache.templateId){
			return Server.rand.nextInt(2);
		}else if(templateId == CrystalCache.templateId){
			return Server.rand.nextInt(Math.max((int) (quality*0.3f), 2));
		}else if(templateId == DragonCache.templateId){
			if(Server.rand.nextInt(200) <= quality){
				return 1;
			}
		}else if(templateId == GemCache.templateId){
			return Server.rand.nextInt(Math.max((int) (quality*0.03f), 2));
		}else if(templateId == PotionCache.templateId){
			if(Server.rand.nextInt(300) <= quality){
				return 1;
			}
		}else if(templateId == RiftCache.templateId){
			if(Server.rand.nextInt(300) <= quality){
				return 2;
			}else if(Server.rand.nextInt(100) <= quality){
				return 1;
			}
		}
		return 0;
	}
	public static int getExtraItemChance(int templateId){
		if(templateId == ArmourCache.templateId){
			return 1600;
		}else if(templateId == DragonCache.templateId){
			return 600;
		}else if(templateId == GemCache.templateId){
			return 200;
		}
		return -1;
	}
	public static int[] getExtraTemplates(int templateId){
		if(templateId == ArmourCache.templateId){
			return new int[]{
					GlimmerscaleBoot.templateId,
					GlimmerscaleGlove.templateId,
					GlimmerscaleHelmet.templateId,
					GlimmerscaleHose.templateId,
					GlimmerscaleSleeve.templateId,
					GlimmerscaleVest.templateId,
					SpectralBoot.templateId,
					SpectralCap.templateId,
					SpectralGlove.templateId,
					SpectralHose.templateId,
					SpectralJacket.templateId,
					SpectralSleeve.templateId
			};
		}else if(templateId == DragonCache.templateId){
			return new int[]{
					ItemList.dragonLeatherBoot,
					ItemList.dragonLeatherCap,
					ItemList.dragonLeatherGlove,
					ItemList.dragonLeatherHose,
					ItemList.dragonLeatherJacket,
					ItemList.dragonLeatherSleeve,
					ItemList.dragonScaleBoot,
					ItemList.dragonScaleGauntlet,
					ItemList.dragonScaleHose,
					ItemList.dragonScaleJacket,
					ItemList.dragonScaleSleeve
			};
		}else if(templateId == GemCache.templateId){
			return new int[]{
					ItemList.opalBlack,
					ItemList.diamondStar,
					ItemList.emeraldStar,
					ItemList.rubyStar,
					ItemList.sapphireStar
			};
		}
		return null;
	}
	
	public static void adjustExtraItem(int templateId, Item item){
		if(templateId == ArmourCache.templateId){
			item.setColor(WurmColor.createColor(100, 100, 100));
		}else if(templateId == DragonCache.templateId){
			item.setMaterial(Materials.MATERIAL_LEATHER);
		}
	}
	
	public static void openCache(Creature performer, Item cache){
		int templateId = cache.getTemplateId();
		Item inv = performer.getInventory();
		float quality = cache.getCurrentQualityLevel();
		float baseQL = getBaseQuality(quality);
		float randQL = getRandomQuality(quality);
		if(createsCustomBasic(templateId)){
			getCustomBasic(performer, cache);
		}else{
			int[] basicTemplates = getBasicTemplates(templateId);
			if(basicTemplates == null){
				logger.warning("Error: Basic Templates are null for cache with template id "+templateId);
				return;
			}
			int basicNums = getBasicNums(templateId);
			basicNums += getExtraBasicNums(templateId, quality);
			int i = 0;
			while(i < basicNums){
				try {
					float basicQuality = Math.max(baseQL+(randQL*Server.rand.nextFloat()), baseQL+(randQL*Server.rand.nextFloat()));
					basicQuality = Math.min(basicQuality, 100f);
					Item basicItem = ItemFactory.createItem(basicTemplates[Server.rand.nextInt(basicTemplates.length)], basicQuality, "");
					basicItem.setRarity(cache.getRarity());
					adjustBasicItem(templateId, quality, basicItem);
					if(adjustBasicWeight(templateId)){
						float weightMult = getWeightMultiplier(templateId, quality);
						basicItem.setWeight((int) (basicItem.getWeightGrams()*weightMult), true);
					}
					inv.insertItem(basicItem, true);
				} catch (FailedException | NoSuchTemplateException e) {
					logger.warning("Error: Failed to create item for cache with template id "+templateId);
					e.printStackTrace();
				}
				i++;
			}
		}
		int chance = getExtraItemChance(templateId);
		if(chance > 0 && Server.rand.nextInt(chance) <= quality){
			try {
				int[] extraTemplates = getExtraTemplates(templateId);
				if(extraTemplates != null){
					float extraQuality = Math.max(baseQL+(randQL*Server.rand.nextFloat()), baseQL+(randQL*Server.rand.nextFloat()));
					extraQuality = Math.min(extraQuality, 100f);
					Item extraItem = ItemFactory.createItem(extraTemplates[Server.rand.nextInt(extraTemplates.length)], extraQuality, "");
					extraItem.setRarity(cache.getRarity());
					adjustExtraItem(templateId, extraItem);
					inv.insertItem(extraItem, true);
				}
			} catch (FailedException | NoSuchTemplateException e) {
				logger.warning("Error: Failed to create item for cache with template id "+templateId);
				e.printStackTrace();
			}
		}
	}
	
	public static void createItems(){
		try {
			ANIMAL_CACHE.createTemplate();
			//CACHE_IDS.add(AnimalCache.templateId);
			ARMOUR_CACHE.createTemplate();
			CACHE_IDS.add(ArmourCache.templateId);
			ARTIFACT_CACHE.createTemplate();
			CACHE_IDS.add(ArtifactCache.templateId);
			CRYSTAL_CACHE.createTemplate();
			CACHE_IDS.add(CrystalCache.templateId);
			DRAGON_CACHE.createTemplate();
			CACHE_IDS.add(DragonCache.templateId);
			GEM_CACHE.createTemplate();
			CACHE_IDS.add(GemCache.templateId);
			MOON_CACHE.createTemplate();
			CACHE_IDS.add(MoonCache.templateId);
			POTION_CACHE.createTemplate();
			CACHE_IDS.add(PotionCache.templateId);
			RIFT_CACHE.createTemplate();
			CACHE_IDS.add(RiftCache.templateId);
			TOOL_CACHE.createTemplate();
			//CACHE_IDS.add(ToolCache.templateId);
			TREASUREMAP_CACHE.createTemplate();
			CACHE_IDS.add(TreasureMapCache.templateId);
			WEAPON_CACHE.createTemplate();
			//CACHE_IDS.add(WeaponCache.templateId);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void registerActions(){
		ModActions.registerAction(new TreasureCacheOpenAction());
	}
}
