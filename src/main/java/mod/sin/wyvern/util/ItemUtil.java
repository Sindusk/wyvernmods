package mod.sin.wyvern.util;

import java.util.Random;
import java.util.logging.Logger;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Server;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.NoSuchTemplateException;
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
			ItemList.tomeEruption,
			ItemList.wandOfTheSeas,
			ItemList.libramNight,
			ItemList.tomeMagicGreen,
			ItemList.tomeMagicBlack,
			ItemList.tomeMagicBlue,
			ItemList.tomeMagicWhite
	};
	public static Item createEnchantOrb(float power){
		byte[] enchantOrbEnchants = {
				Enchants.BUFF_CIRCLE_CUNNING,
				Enchants.BUFF_FLAMING_AURA,
				Enchants.BUFF_SHARED_PAIN,
				Enchants.BUFF_ROTTING_TOUCH,
				Enchants.BUFF_LIFETRANSFER, Enchants.BUFF_LIFETRANSFER, // 2 rolls for LT
				Enchants.BUFF_NIMBLENESS,
				Enchants.BUFF_FROSTBRAND,
				Enchants.BUFF_WEBARMOUR,
				Enchants.BUFF_BLESSINGDARK, Enchants.BUFF_BLESSINGDARK, // 2 rolls for BotD
				Enchants.BUFF_VENOM,
				Enchants.BUFF_WIND_OF_AGES
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
	public static Item createRandomLootTool(){
		try{
		    int[] templates = {7, 8, 20, 24, 25, 27, 62, 93, 97};
		    int template = templates[random.nextInt(templates.length)];
		    float quality = 100;
		    for(int i = 0; i < 3; i++){
		    	quality = java.lang.Math.min(quality, java.lang.Math.max((float)10, 90*random.nextFloat()));
		    }
		    byte[] materials = {7, 8, 9, 9, 9, 10, 10, 11, 11, 11, 11, 12, 12, 13, 13, 30, 30, 31, 31, 34, 34, 56, 57, 67};
		    byte material = materials[random.nextInt(materials.length)];
		    byte rarity = 0;
		    if(random.nextInt(50) <= 2){
		    	rarity = 1;
		    }else if(random.nextInt(200) <= 2){
	    		rarity = 2;
		    }
		    byte[] enchants = {13, 13, 16, 16, 47};
		    byte enchant = enchants[random.nextInt(enchants.length)];
		    float power = 130;
		    for(int i = 0; i < 2; i++){
		    	power = java.lang.Math.min(power, 30+(100*random.nextFloat()));
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
