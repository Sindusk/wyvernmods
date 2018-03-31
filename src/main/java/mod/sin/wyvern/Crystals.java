package mod.sin.wyvern;

import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.NotOwnedException;
import com.wurmonline.shared.constants.Enchants;

import mod.sin.items.ChaosCrystal;
import mod.sin.items.EnchantersCrystal;

public class Crystals {
	public static byte[] enchs = { // Valid enchants to apply to an item with Enchanters Crystals
			Enchants.BUFF_BLESSINGDARK,
			Enchants.BUFF_BLOODTHIRST,
			Enchants.BUFF_CIRCLE_CUNNING,
			Enchants.BUFF_COURIER,
			Enchants.BUFF_FLAMING_AURA,
			Enchants.BUFF_FROSTBRAND,
			Enchants.BUFF_LIFETRANSFER,
			Enchants.BUFF_MINDSTEALER,
			Enchants.BUFF_NIMBLENESS,
			Enchants.BUFF_OPULENCE,
			Enchants.BUFF_ROTTING_TOUCH,
			Enchants.BUFF_SHARED_PAIN,
			Enchants.BUFF_VENOM,
			Enchants.BUFF_WEBARMOUR,
			Enchants.BUFF_WIND_OF_AGES,
			110, 111 // Harden and Phasing
	};
	public static byte getNewRandomEnchant(Item target){
		int i = 0;
		while(i < 10){
			byte ench = enchs[Server.rand.nextInt(enchs.length)];
			if(target.getBonusForSpellEffect(ench) == 0f){
				return ench;
			}
			i++;
		}
		return -10;
	}
	public static double getInfusionDifficulty(Creature performer, Item source, Item target){
		double diff = (Servers.localServer.PVPSERVER ? 150 : 120)-source.getCurrentQualityLevel();
		diff += source.getRarity()*(Servers.localServer.PVPSERVER ? 50 : 30);
		diff += 40f - (target.getCurrentQualityLevel()*0.4f);
		diff -= performer.getSoulDepth().getKnowledge();
		if(Servers.localServer.PVPSERVER){ // Added difficulty to account for PvP epic curve:
			diff *= 1.4f;
		}
		return diff;
	}
	public static double getEnchantersInfusionDifficulty(Creature performer, Item source, Item target){
		double diff = (Servers.localServer.PVPSERVER ? 220 : 180)-source.getCurrentQualityLevel();
		diff += 40f - (target.getCurrentQualityLevel()*0.4f);
		diff -= performer.getSoulDepth().getKnowledge();
		if(Servers.localServer.PVPSERVER){ // Added difficulty to account for PvP epic curve:
			diff *= 1.4f;
		}
		return diff;
	}
	public static boolean shouldCancelEnchantersInfusion(Creature performer, Item target){
		if(target.getOwnerId() != performer.getWurmId() && target.getLastOwnerId() != performer.getWurmId()){
			performer.getCommunicator().sendNormalServerMessage("You must own the item you wish to infuse.");
			return true;
		}
		ItemSpellEffects effs = target.getSpellEffects();
		if(effs == null || effs.getEffects().length == 0){
			performer.getCommunicator().sendNormalServerMessage("The item must be enchanted to be infused.");
			return true;
		}
		return false;
	}
	public static boolean shouldCancelInfusion(Creature performer, Item source, Item target){
		if(target.getOwnerId() != performer.getWurmId() && target.getLastOwnerId() != performer.getWurmId()){
			performer.getCommunicator().sendNormalServerMessage("You must own the item you wish to infuse.");
			return true;
		}
		if(source.getRarity() > target.getRarity()+1){
			performer.getCommunicator().sendNormalServerMessage("The "+source.getName()+" is too powerful, and would outright destroy the "+target.getName()+".");
			return true;
		}else if(source.getRarity() < target.getRarity()+1){
			performer.getCommunicator().sendNormalServerMessage("The "+source.getName()+" is not powerful enough to have an effect on the "+target.getName()+". You will need to combine with other crystals first.");
			return true;
		}
		return false;
	}
	public static boolean shouldCancelCombine(Creature performer, Item source, Item target){
		if(source.getWurmId() == target.getWurmId()){
			performer.getCommunicator().sendNormalServerMessage("You can't combine a crystal with itself, silly!");
			return true;
		}
		if(!Crystals.isCrystal(source) || !Crystals.isCrystal(target)){
			performer.getCommunicator().sendNormalServerMessage("Both objects must be Crystals to combine.");
			return true;
		}
		if(source.getTemplateId() != target.getTemplateId()){
			performer.getCommunicator().sendNormalServerMessage("Both crystals must be of the same type to combine.");
			return true;
		}
		try {
			if(source.getOwner() != performer.getWurmId() || target.getOwner() != performer.getWurmId()){
				performer.getCommunicator().sendNormalServerMessage("You must hold both crystals in your hands to combine them.");
				return true;
			}
		} catch (NotOwnedException e) {
			e.printStackTrace();
		}
		if(source.getRarity() < target.getRarity()){
			performer.getCommunicator().sendNormalServerMessage("That crystal is too potent for this combination.");
			return true;
		}else if(source.getRarity() > target.getRarity()){
			performer.getCommunicator().sendNormalServerMessage("That crystal is not potent enough for this combination.");
			return true;
		}else if(source.getRarity() >= 3 && target.getRarity() >= 3 && source.getCurrentQualityLevel() + target.getCurrentQualityLevel() >= 100){
			performer.getCommunicator().sendNormalServerMessage("Those crystals would be far too powerful if combined.");
			return true;
		}
		return false;
	}
	public static boolean isCrystal(Item item){
		if(item.getTemplateId() == ChaosCrystal.templateId){
			return true;
		}else if(item.getTemplateId() == EnchantersCrystal.templateId){
			return true;
		}
		return false;
	}
}
