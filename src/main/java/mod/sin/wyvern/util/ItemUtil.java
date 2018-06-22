package mod.sin.wyvern.util;

import java.util.Random;
import java.util.logging.Logger;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Server;
import com.wurmonline.server.items.*;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.shared.constants.Enchants;

import mod.sin.items.EnchantOrb;
import mod.sin.items.TreasureBox;

public class ItemUtil {
	public static final Logger logger = Logger.getLogger(ItemUtil.class.getName());
	protected static final Random random = new Random();
	public static int[] sorceryIds = {
			ItemList.bloodAngels,
			ItemList.smokeSol,
			ItemList.slimeUttacha,
			ItemList.tomeMagicRed,
			ItemList.scrollBinding,
			ItemList.cherryWhite,
			ItemList.cherryRed,
			ItemList.cherryGreen,
			ItemList.giantWalnut,
			//ItemList.tomeEruption,
			//ItemList.wandOfTheSeas,
			ItemList.libramNight,
			ItemList.tomeMagicGreen,
			ItemList.tomeMagicBlack,
			ItemList.tomeMagicBlue,
			ItemList.tomeMagicWhite
	};
	public static boolean isSorcery(Item item){
		for(int id : sorceryIds){
			if(item.getTemplateId() == id){
				return true;
			}
		}
		return false;
	}
	public static int[] plateChainTemplates = {
			ItemList.plateBoot,
			ItemList.plateGauntlet,
			ItemList.plateHose,
			ItemList.plateJacket,
			ItemList.plateSleeve,
			ItemList.chainBoot,
			ItemList.chainCoif,
			ItemList.chainGlove,
			ItemList.chainHose,
			ItemList.chainJacket,
			ItemList.chainSleeve,
			ItemList.helmetBasinet,
			ItemList.helmetGreat,
			ItemList.helmetOpen
	};
	public static int[] toolWeaponTemplates = {
			ItemList.axeSmall,
			ItemList.shieldMedium,
			ItemList.hatchet,
			ItemList.knifeCarving,
			ItemList.pickAxe,
			ItemList.swordLong,
			ItemList.saw,
			ItemList.shovel,
			ItemList.rake,
			ItemList.hammerMetal,
			ItemList.hammerWood,
			ItemList.anvilSmall,
			ItemList.cheeseDrill,
			ItemList.swordShort,
			ItemList.swordTwoHander,
			ItemList.shieldSmallWood,
			ItemList.shieldSmallMetal,
			ItemList.shieldMediumWood,
			ItemList.shieldLargeWood,
			ItemList.shieldLargeMetal,
			ItemList.axeHuge,
			ItemList.axeMedium,
			ItemList.knifeButchering,
			ItemList.fishingRodIronHook,
			ItemList.stoneChisel,
            ItemList.spindle,
            ItemList.anvilLarge,
            ItemList.grindstone,
            ItemList.needleIron,
            ItemList.knifeFood,
            ItemList.sickle,
            ItemList.scythe,
            ItemList.maulLarge,
            ItemList.maulSmall,
            ItemList.maulMedium,
            ItemList.file,
            ItemList.awl,
            ItemList.leatherKnife,
            ItemList.scissors,
            ItemList.clayShaper,
            ItemList.spatula,
            ItemList.fruitpress,
            ItemList.bowShortNoString,
            ItemList.bowMediumNoString,
            ItemList.bowLongNoString,
            ItemList.trowel,
            ItemList.groomingBrush,
            ItemList.spearLong,
            ItemList.halberd,
            ItemList.spearSteel,
            ItemList.staffSteel
	};
	// 3,4,7,8,20,21,24,25,27,62,63,64,65,80,81,82,83,84,85,86,87,90,93,94,97,
	// 103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,135,139,143,152,185,
	// 202,215,257,258,259,267,268,274,275,276,277,278,279,280,281,282,283,284,285,286,287,290,291,292,296,
	// 314,350,351,374,376,378,380,382,388,390,392,394,396,397,413,447,448,449,463,480,581,
	// 621,623,623,623,623,640,641,642,643,647,702,703,704,705,706,707,710,711,747,749,774,922
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
	public static Item createRandomSorcery(byte charges){
		try {
			Item sorcery = ItemFactory.createItem(sorceryIds[Server.rand.nextInt(sorceryIds.length)], 90+(10*Server.rand.nextFloat()), null);
			sorcery.setAuxData((byte) (3-charges));
			return sorcery;
		} catch (FailedException | NoSuchTemplateException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static Item createEnchantOrb(float power){
		byte[] enchantOrbEnchants = {
				Enchants.BUFF_CIRCLE_CUNNING,
				Enchants.BUFF_FLAMING_AURA,
				Enchants.BUFF_SHARED_PAIN,
				Enchants.BUFF_ROTTING_TOUCH,
				Enchants.BUFF_LIFETRANSFER, Enchants.BUFF_LIFETRANSFER, // 2 rolls for LT
				Enchants.BUFF_NIMBLENESS,
                Enchants.BUFF_MINDSTEALER,
				Enchants.BUFF_FROSTBRAND,
				Enchants.BUFF_WEBARMOUR,
				Enchants.BUFF_BLESSINGDARK, Enchants.BUFF_BLESSINGDARK, // 2 rolls for BotD
				Enchants.BUFF_VENOM,
				Enchants.BUFF_WIND_OF_AGES,
				110, 110, //Harden
                114, //Efficiency
                115, //Quarry
                116, //Prowess
                117, //Industry
                118, //Endurance
                119, //Acuity
			};
		try {
			Item enchantOrb = ItemFactory.createItem(EnchantOrb.templateId, 99+(1*Server.rand.nextFloat()), "");
			ItemSpellEffects effs = enchantOrb.getSpellEffects();
			if(effs == null){
				effs = new ItemSpellEffects(enchantOrb.getWurmId());
			}
			byte enchant = enchantOrbEnchants[Server.rand.nextInt(enchantOrbEnchants.length)];
			SpellEffect eff = new SpellEffect(enchantOrb.getWurmId(), enchant, power, 20000000);
			effs.addSpellEffect(eff);
			enchantOrb.setDescription(eff.getName()+" "+Math.round(power));
			return enchantOrb;
		} catch (FailedException | NoSuchTemplateException e) {
			e.printStackTrace();
		}
		return null;
	}
    public static Item createRandomPlateChain(float minQL, float maxQL, byte material, String creator){
        try {
            Item armour = ItemFactory.createItem(plateChainTemplates[Server.rand.nextInt(plateChainTemplates.length)], minQL+((maxQL-minQL)*Server.rand.nextFloat()), creator);
            armour.setMaterial(material);
            return armour;
        } catch (FailedException | NoSuchTemplateException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static Item createRandomToolWeapon(float minQL, float maxQL, String creator){
        try {
            return ItemFactory.createItem(toolWeaponTemplates[Server.rand.nextInt(toolWeaponTemplates.length)], minQL+((maxQL-minQL)*Server.rand.nextFloat()), creator);
        } catch (FailedException | NoSuchTemplateException e) {
            e.printStackTrace();
        }
        return null;
    }
	public static Item createRandomLootTool(){
		try{
		    int[] templates = {
		    		ItemList.hatchet,
					ItemList.knifeCarving,
					ItemList.pickAxe,
					ItemList.saw,
					ItemList.shovel,
					ItemList.rake,
					ItemList.hammerMetal,
					ItemList.knifeButchering,
					ItemList.stoneChisel,
					ItemList.anvilSmall
		    };
		    int template = templates[random.nextInt(templates.length)];
		    float quality = 100;
		    for(int i = 0; i < 2; i++){
		    	quality = Math.min(quality, Math.max((float)10, 70*random.nextFloat()));
		    }
		    byte[] materials = {
		    		Materials.MATERIAL_GOLD,
					Materials.MATERIAL_SILVER,
					Materials.MATERIAL_STEEL, Materials.MATERIAL_STEEL, Materials.MATERIAL_STEEL,
					Materials.MATERIAL_COPPER, Materials.MATERIAL_COPPER,
					Materials.MATERIAL_IRON, Materials.MATERIAL_IRON, Materials.MATERIAL_IRON, Materials.MATERIAL_IRON,
					Materials.MATERIAL_LEAD, Materials.MATERIAL_LEAD,
					Materials.MATERIAL_ZINC, Materials.MATERIAL_ZINC,
					Materials.MATERIAL_BRASS, Materials.MATERIAL_BRASS,
					Materials.MATERIAL_BRONZE, Materials.MATERIAL_BRONZE,
					Materials.MATERIAL_TIN, Materials.MATERIAL_TIN,
					Materials.MATERIAL_ADAMANTINE,
					Materials.MATERIAL_GLIMMERSTEEL,
					Materials.MATERIAL_SERYLL
		    };
		    byte material = materials[random.nextInt(materials.length)];
		    byte rarity = 0;
		    if(random.nextInt(80) <= 2){
		    	rarity = 1;
		    }else if(random.nextInt(250) <= 2){
	    		rarity = 2;
		    }
		    byte[] enchants = {
                    Enchants.BUFF_WIND_OF_AGES, Enchants.BUFF_WIND_OF_AGES,
                    Enchants.BUFF_CIRCLE_CUNNING, Enchants.BUFF_CIRCLE_CUNNING,
                    Enchants.BUFF_BLESSINGDARK
		    };
		    byte enchant = enchants[random.nextInt(enchants.length)];
		    float power = 100;
		    for(int i = 0; i < 2; i++){
		    	power = Math.min(power, 20+(60*random.nextFloat()));
		    }
			Item tool = ItemFactory.createItem(template, quality, material, rarity, "");
		    ItemSpellEffects effs = tool.getSpellEffects();
		    if(effs == null){
		    	effs = new ItemSpellEffects(tool.getWurmId());
		    }
		    SpellEffect eff = new SpellEffect(tool.getWurmId(), enchant, power, 20000000);
		    effs.addSpellEffect(eff);
		    tool.setDescription(eff.getName()+" "+String.valueOf((byte)power));
		    return tool;
	    } catch (FailedException | NoSuchTemplateException e) {
			e.printStackTrace();
		}
		return null;
    }
	public static Item createTreasureBox(){
		try {
			Item treasureBox = ItemFactory.createItem(TreasureBox.templateId, 10f+(90f*random.nextFloat()), "");
			if(Server.rand.nextInt(20) == 0){
				treasureBox.setRarity((byte) 3);
			}else if(Server.rand.nextInt(5) == 0){
				treasureBox.setRarity((byte) 2);
			}else if(Server.rand.nextBoolean()){
				treasureBox.setRarity((byte) 1);
			}
			return treasureBox;
		} catch (FailedException | NoSuchTemplateException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static boolean isSingleUseRune(byte rune){
        if(rune == -80){
            return true;
        }else if(rune == -81){
            return true;
        }else if(rune == -91){
            return true;
        }else if(rune == -97){
            return true;
        }else if(rune == -104){
            return true;
        }else if(rune == -107){
            return true;
        }else if(rune == -119){
            return true;
        }else if(rune == -126){
            return true;
        }
        return false;
    }
}
