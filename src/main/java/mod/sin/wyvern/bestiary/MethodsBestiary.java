package mod.sin.wyvern.bestiary;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modloader.ReflectionUtil;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Server;
import com.wurmonline.server.combat.Archery;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.skills.SkillsFactory;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.shared.constants.Enchants;

import mod.sin.creatures.*;
import mod.sin.creatures.titans.*;
import mod.sin.weapons.Club;
import mod.sin.weapons.titan.*;
import mod.sin.wyvern.MiscChanges;
import mod.sin.wyvern.arena.Arena;

public class MethodsBestiary {
	protected static Logger logger = Logger.getLogger(MethodsBestiary.class.getName());
	
	public static boolean checkColorTemplate(CreatureTemplate template){
		try {
			int templateId = template.getTemplateId();
			if(templateId == Lilith.templateId){
	        	return true;
	        }else if(templateId == ForestSpider.templateId){
				return true;
			}else if(templateId == Avenger.templateId){
				return true;
			}else if(templateId == HornedPony.templateId){
				return true;
			}else if(templateId == LilithZombie.templateId){
				return true;
			}
		} catch (IllegalArgumentException | ClassCastException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static byte getCreatureColorRed(CreatureTemplate template){
		try {
			int templateId = template.getTemplateId();
			if(templateId == ForestSpider.templateId){
	        	return (byte)0;
	        }else if(templateId == Avenger.templateId){
	        	return (byte)70;
	        }
		} catch (IllegalArgumentException | ClassCastException e) {
			e.printStackTrace();
		}
		return (byte)127;
	}

	public static byte getCreatureColorGreen(CreatureTemplate template){
		try {
			int templateId = template.getTemplateId();
			if(templateId == Lilith.templateId){
	        	return (byte)0;
	        }else if(templateId == Avenger.templateId){
	        	return (byte)70;
	        }else if(templateId == HornedPony.templateId){
	        	return (byte)10;
	        }else if(templateId == LilithZombie.templateId){
	        	return (byte)0;
	        }
		} catch (IllegalArgumentException | ClassCastException e) {
			e.printStackTrace();
		}
		return (byte)127;
	}
	
	public static byte getCreatureColorBlue(CreatureTemplate template){
		try {
			int templateId = template.getTemplateId();
			if(templateId == Lilith.templateId){
				return (byte)0;
			}else if(templateId == ForestSpider.templateId){
	        	return (byte)0;
	        }else if(templateId == HornedPony.templateId){
	        	return (byte)70;
	        }else if(templateId == LilithZombie.templateId){
	        	return (byte)0;
	        }
		} catch (IllegalArgumentException | ClassCastException e) {
			e.printStackTrace();
		}
		return (byte)127;
	}
	
	public static float getAdjustedSizeMod(CreatureStatus status){
		try {
			float floatToRet = 1.0f;
			Creature statusHolder = ReflectionUtil.getPrivateField(status, ReflectionUtil.getField(status.getClass(), "statusHolder"));
			byte modtype = ReflectionUtil.getPrivateField(status, ReflectionUtil.getField(status.getClass(), "modtype"));
			float ageSizeModifier = ReflectionUtil.callPrivateMethod(status, ReflectionUtil.getMethod(status.getClass(), "getAgeSizeModifier"));
	        if ((!statusHolder.isVehicle() || statusHolder.isDragon()) && modtype > 0) {
	            switch (modtype) {
	                case 3: {
	                    floatToRet = 1.4f;
	                    break;
	                }
	                case 4: {
	                    floatToRet = 2.0f;
	                    break;
	                }
	                case 6: {
	                    floatToRet = 2.0f;
	                    break;
	                }
	                case 7: {
	                    floatToRet = 0.8f;
	                    break;
	                }
	                case 8: {
	                    floatToRet = 0.9f;
	                    break;
	                }
	                case 9: {
	                    floatToRet = 1.5f;
	                    break;
	                }
	                case 10: {
	                    floatToRet = 1.3f;
	                    break;
	                }
	                case 99: {
	                    floatToRet = 3.0f;
	                    break;
	                }
	                default: {
	                    //return floatToRet * ageSizeModifier;
	                }
	            }
	        }
	        int templateId = statusHolder.getTemplate().getTemplateId();
	        if(templateId == Lilith.templateId){
	        	floatToRet *= 0.45f;
	        }else if(templateId == Ifrit.templateId){
	        	floatToRet *= 0.15f; // The base model is way too big. I'm tilted.
	        }else if(templateId == WyvernBlack.templateId){
	        	floatToRet *= 0.6f;
	        }else if(templateId == WyvernGreen.templateId){
	        	floatToRet *= 0.6f;
	        }else if(templateId == WyvernRed.templateId){
	        	floatToRet *= 0.6f;
	        }else if(templateId == WyvernWhite.templateId){
	        	floatToRet *= 0.6f;
	        }else if(templateId == MacroSlayer.templateId){
	        	floatToRet *= 1.5f;
	        }else if(templateId == ForestSpider.templateId){
	        	floatToRet *= 0.4f;
	        }else if(templateId == Avenger.templateId){
	        	floatToRet *= 0.35f;
	        }else if(templateId == LargeBoar.templateId){
	        	floatToRet *= 1.8f;
	        }else if(templateId == SpiritTroll.templateId){
	        	floatToRet *= 1.2f;
	        }else if(templateId == Giant.templateId){
	        	floatToRet *= 0.75f;
	        }else if(templateId == LilithZombie.templateId){
	        	floatToRet *= 0.75f;
	        }else if(templateId == Charger.templateId){
	        	floatToRet *= 1.5f;
	        }
	        
	        return floatToRet * ageSizeModifier;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | ClassCastException | NoSuchFieldException e) {
			e.printStackTrace();
		}
        return 1.0f;
	}
	
	public static Item createNewTitanWeapon(String name, int[] templates){
		try {
			Item titanWeapon;
			int templateId = templates[Server.rand.nextInt(templates.length)];
			titanWeapon = ItemFactory.createItem(templateId, 90f+(Server.rand.nextFloat()*5f), Materials.MATERIAL_ADAMANTINE, Server.rand.nextBoolean() ? (byte) 2 : (byte) 3, name);
			ItemSpellEffects effs = titanWeapon.getSpellEffects();
		    if(effs == null){
		    	effs = new ItemSpellEffects(titanWeapon.getWurmId());
		    }
		    if(templateId == MaartensMight.templateId){
			    effs.addSpellEffect(new SpellEffect(titanWeapon.getWurmId(), Enchants.BUFF_NIMBLENESS, 250, 20000000));
			    effs.addSpellEffect(new SpellEffect(titanWeapon.getWurmId(), (byte) 111, 200, 20000000)); // Phasing
		    }else if(templateId == RaffehsRage.templateId){
			    effs.addSpellEffect(new SpellEffect(titanWeapon.getWurmId(), Enchants.BUFF_FLAMING_AURA, 150, 20000000));
			    effs.addSpellEffect(new SpellEffect(titanWeapon.getWurmId(), Enchants.BUFF_FROSTBRAND, 150, 20000000));
		    }else if(templateId == VindictivesVengeance.templateId){
			    effs.addSpellEffect(new SpellEffect(titanWeapon.getWurmId(), Enchants.BUFF_BLESSINGDARK, 300, 20000000));
			    effs.addSpellEffect(new SpellEffect(titanWeapon.getWurmId(), Enchants.BUFF_NIMBLENESS, 200, 20000000));
			}else if(templateId == WilhelmsWrath.templateId){
			    effs.addSpellEffect(new SpellEffect(titanWeapon.getWurmId(), Enchants.BUFF_ROTTING_TOUCH, 300, 20000000));
			    effs.addSpellEffect(new SpellEffect(titanWeapon.getWurmId(), Enchants.BUFF_BLOODTHIRST, 100, 20000000));
			}
			if(titanWeapon != null){
				return titanWeapon;
			}
		} catch (FailedException | NoSuchTemplateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean isArcheryImmune(Creature performer, Creature defender){
		if(Arena.isTitan(defender) || Arena.isTitanMinion(defender)){
			performer.getCommunicator().sendCombatNormalMessage("You cannot archer "+defender.getName()+", as it is protected by a Titan.");
			return true;
		}
		String message = "The "+defender.getName()+" would be unaffected by your arrows.";
		boolean immune = false;
		Item arrow = Archery.getArrow(performer);
		if(arrow == null){ // Copied directly from the attack() method in Archery.
			performer.getCommunicator().sendCombatNormalMessage("You have no arrows left to shoot!");
            return true;
		}
		//int defenderTemplateId = defender.getTemplate().getTemplateId();
		if(defender.isRegenerating() && arrow.getTemplateId() == ItemList.arrowShaft){
			message = "The "+defender.getName()+" would be unaffected by the "+arrow.getName()+".";
			immune = true;
		}else if(defender.getTemplate().isNotRebirthable()){
			immune = true;
		}else if(defender.isUnique()){
			immune = true;
		}
		if(immune){
			performer.getCommunicator().sendCombatNormalMessage(message);
		}
		return immune;
	}
	
	public static void modifyNewCreature(Creature creature){
		try{
			if(Arena.isTitan(creature)){
				Arena.addTitan(creature);
				MiscChanges.sendGlobalFreedomChat(creature, "The titan "+creature.getName()+" has stepped into the mortal realm. Challenge "+creature.getHimHerItString()+" in the Arena if you dare.", 255, 105, 180);
				if(creature.getTemplate().getTemplateId() == Lilith.templateId){
				    Item titanWeapon = createNewTitanWeapon(creature.getName(), new int[]{VindictivesVengeance.templateId, WilhelmsWrath.templateId});
					creature.getInventory().insertItem(titanWeapon);
				}else if(creature.getTemplate().getTemplateId() == Ifrit.templateId){
				    Item titanWeapon = createNewTitanWeapon(creature.getName(), new int[]{MaartensMight.templateId, RaffehsRage.templateId});
					creature.getInventory().insertItem(titanWeapon);
				}
			}else if(creature.getTemplate().getTemplateId() == Facebreyker.templateId){
				Item club = ItemFactory.createItem(Club.templateId, 80f+(Server.rand.nextFloat()*15f), Server.rand.nextBoolean() ? Materials.MATERIAL_GLIMMERSTEEL : Materials.MATERIAL_ADAMANTINE, Server.rand.nextBoolean() ? (byte) 1 : (byte) 2, "Facebreyker");
				creature.getInventory().insertItem(club);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private static void setNaturalArmour(int templateId, float value){
		try{
			CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(templateId);
			if(template != null){
				ReflectionUtil.setPrivateField(template, ReflectionUtil.getField(template.getClass(), "naturalArmour"), value);
			}
		} catch (NoSuchCreatureTemplateException | IllegalArgumentException | IllegalAccessException | ClassCastException | NoSuchFieldException e) {
			e.printStackTrace();
		}
	}
	
	private static void setCorpseModel(int templateId, String model){
		try{
			CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(templateId);
			if(template != null){
				ReflectionUtil.setPrivateField(template, ReflectionUtil.getField(template.getClass(), "corpsename"), model);
			}
		} catch (NoSuchCreatureTemplateException | IllegalArgumentException | IllegalAccessException | ClassCastException | NoSuchFieldException e) {
			e.printStackTrace();
		}
	}
	
	private static void setGhost(int templateId){
		try{
			CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(templateId);
			if(template != null){
				ReflectionUtil.setPrivateField(template, ReflectionUtil.getField(template.getClass(), "ghost"), true);
			}
		} catch (NoSuchCreatureTemplateException | IllegalArgumentException | IllegalAccessException | ClassCastException | NoSuchFieldException e) {
			e.printStackTrace();
		}
	}
	
	private static void setGrazer(int templateId){
		try{
			CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(templateId);
			if(template != null){
				ReflectionUtil.setPrivateField(template, ReflectionUtil.getField(template.getClass(), "grazer"), true);
			}
		} catch (NoSuchCreatureTemplateException | IllegalArgumentException | IllegalAccessException | ClassCastException | NoSuchFieldException e) {
			e.printStackTrace();
		}
	}
	
	private static void setWorgFields(int templateId) {
		try {
			CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(templateId);
			if(template != null) {
				ReflectionUtil.setPrivateField(template, ReflectionUtil.getField(template.getClass(), "isVehicle"), true);
				ReflectionUtil.setPrivateField(template, ReflectionUtil.getField(template.getClass(), "dominatable"), true);
				ReflectionUtil.setPrivateField(template, ReflectionUtil.getField(template.getClass(), "isHorse"), true);
				ReflectionUtil.setPrivateField(template, ReflectionUtil.getField(template.getClass(), "isDetectInvis"), false);
				ReflectionUtil.setPrivateField(template, ReflectionUtil.getField(template.getClass(), "monster"), true);
				Skills skills = SkillsFactory.createSkills("Worg");
				skills.learnTemp(SkillList.BODY_STRENGTH, 40.0f);
		        skills.learnTemp(SkillList.BODY_CONTROL, 25.0f);
		        skills.learnTemp(SkillList.BODY_STAMINA, 35.0f);
		        skills.learnTemp(SkillList.MIND_LOGICAL, 10.0f);
		        skills.learnTemp(SkillList.MIND_SPEED, 15.0f);
		        skills.learnTemp(SkillList.SOUL_STRENGTH, 20.0f);
		        skills.learnTemp(SkillList.SOUL_DEPTH, 12.0f);
		        skills.learnTemp(SkillList.WEAPONLESS_FIGHTING, 50.0f);
		        ReflectionUtil.setPrivateField(template, ReflectionUtil.getField(template.getClass(), "skills"), skills);
			} // if
		} catch (Exception e) {
			e.printStackTrace();
		} // catch
	} // setWorgFields
	
	public static void setTemplateVariables(){
		// Set corpse models
		setCorpseModel(Avenger.templateId, "fogspider.");
		setCorpseModel(SpiritTroll.templateId, "fogspider.");
		setCorpseModel(SpectralDrake.templateId, "fogspider.");
		/*CreatureTemplate spectralDrake = CreatureTemplateFactory.getInstance().getTemplate(SpectralDrake.templateId);
		if(spectralDrake != null){
			ReflectionUtil.setPrivateField(spectralDrake, ReflectionUtil.getField(spectralDrake.getClass(), "corpsename"), "fogspider.");
		}*/
		setCorpseModel(WyvernBlack.templateId, "blackdragonhatchling.");
		/*CreatureTemplate blackWyvern = CreatureTemplateFactory.getInstance().getTemplate(WyvernBlack.templateId);
		if(blackWyvern != null){
			ReflectionUtil.setPrivateField(blackWyvern, ReflectionUtil.getField(blackWyvern.getClass(), "corpsename"), "blackdragonhatchling.");
		}*/
		setCorpseModel(WyvernGreen.templateId, "greendragonhatchling.");
		/*CreatureTemplate greenWyvern = CreatureTemplateFactory.getInstance().getTemplate(WyvernGreen.templateId);
		if(greenWyvern != null){
			ReflectionUtil.setPrivateField(greenWyvern, ReflectionUtil.getField(greenWyvern.getClass(), "corpsename"), "greendragonhatchling.");
		}*/
		setCorpseModel(WyvernRed.templateId, "reddragonhatchling.");
		/*CreatureTemplate redWyvern = CreatureTemplateFactory.getInstance().getTemplate(WyvernRed.templateId);
		if(redWyvern != null){
			ReflectionUtil.setPrivateField(redWyvern, ReflectionUtil.getField(redWyvern.getClass(), "corpsename"), "reddragonhatchling.");
		}*/
		setCorpseModel(WyvernWhite.templateId, "whitedragonhatchling.");
		/*CreatureTemplate whiteWyvern = CreatureTemplateFactory.getInstance().getTemplate(WyvernWhite.templateId);
		if(whiteWyvern != null){
			ReflectionUtil.setPrivateField(whiteWyvern, ReflectionUtil.getField(whiteWyvern.getClass(), "corpsename"), "whitedragonhatchling.");
		}*/
		setCorpseModel(Facebreyker.templateId, "riftogre.");
		/*CreatureTemplate facebreyker = CreatureTemplateFactory.getInstance().getTemplate(Facebreyker.templateId);
		if(facebreyker != null){
			ReflectionUtil.setPrivateField(facebreyker, ReflectionUtil.getField(facebreyker.getClass(), "corpsename"), "riftogre.");
		}*/
		setCorpseModel(ForestSpider.templateId, "hugespider.");
		/*CreatureTemplate forestSpider = CreatureTemplateFactory.getInstance().getTemplate(ForestSpider.templateId);
		if(forestSpider != null){
			ReflectionUtil.setPrivateField(forestSpider, ReflectionUtil.getField(forestSpider.getClass(), "corpsename"), "hugespider.");
		}*/
		setCorpseModel(Giant.templateId, "forestgiant.");
		/*CreatureTemplate giant = CreatureTemplateFactory.getInstance().getTemplate(Giant.templateId);
		if(giant != null){
			ReflectionUtil.setPrivateField(giant, ReflectionUtil.getField(giant.getClass(), "corpsename"), "forestgiant.");
		}*/
		setCorpseModel(LargeBoar.templateId, "wildboar.");
		/*CreatureTemplate largeBoar = CreatureTemplateFactory.getInstance().getTemplate(LargeBoar.templateId);
		if(largeBoar != null){
			ReflectionUtil.setPrivateField(largeBoar, ReflectionUtil.getField(largeBoar.getClass(), "corpsename"), "wildboar.");
		}*/
		setCorpseModel(HornedPony.templateId, "unicorn.");
		/*CreatureTemplate hornedPony = CreatureTemplateFactory.getInstance().getTemplate(HornedPony.templateId);
		if(hornedPony != null){
			ReflectionUtil.setPrivateField(hornedPony, ReflectionUtil.getField(hornedPony.getClass(), "corpsename"), "unicorn.");
		}*/
		setCorpseModel(IfritSpider.templateId, "lavaspider.");
		/*CreatureTemplate ifritSpider = CreatureTemplateFactory.getInstance().getTemplate(IfritSpider.templateId);
		if(ifritSpider != null){
			ReflectionUtil.setPrivateField(ifritSpider, ReflectionUtil.getField(ifritSpider.getClass(), "corpsename"), "lavaspider.");
		}*/
		setCorpseModel(IfritFiend.templateId, "lavafiend.");
		/*CreatureTemplate ifritFiend = CreatureTemplateFactory.getInstance().getTemplate(IfritFiend.templateId);
		if(ifritFiend != null){
			ReflectionUtil.setPrivateField(ifritFiend, ReflectionUtil.getField(ifritFiend.getClass(), "corpsename"), "lavafiend.");
		}*/
		// Also apply the ghost modifier
		setGhost(SpiritTroll.templateId);
		setGhost(Avenger.templateId);
		setGhost(LilithWraith.templateId);
		setGhost(Charger.templateId);
		/*CreatureTemplate spiritTroll = CreatureTemplateFactory.getInstance().getTemplate(SpiritTroll.templateId);
		if(spiritTroll != null){
			ReflectionUtil.setPrivateField(spiritTroll, ReflectionUtil.getField(spiritTroll.getClass(), "ghost"), true);
		}
		CreatureTemplate avenger = CreatureTemplateFactory.getInstance().getTemplate(Avenger.templateId);
		if(avenger != null){
			ReflectionUtil.setPrivateField(avenger, ReflectionUtil.getField(avenger.getClass(), "ghost"), true);
		}
		CreatureTemplate lilithWight = CreatureTemplateFactory.getInstance().getTemplate(LilithWraith.templateId);
		if(lilithWight != null){
			ReflectionUtil.setPrivateField(lilithWight, ReflectionUtil.getField(lilithWight.getClass(), "ghost"), true);
		}
		CreatureTemplate charger = CreatureTemplateFactory.getInstance().getTemplate(Charger.templateId);
		if(charger != null){
			ReflectionUtil.setPrivateField(charger, ReflectionUtil.getField(charger.getClass(), "ghost"), true);
		}*/
		
		// Dragon natural armour increases:
		setNaturalArmour(CreatureTemplate.DRAGON_BLUE_CID, 0.04f);
		/*CreatureTemplate dragonBlue = CreatureTemplateFactory.getInstance().getTemplate(CreatureTemplate.DRAGON_BLUE_CID);
		ReflectionUtil.setPrivateField(dragonBlue, ReflectionUtil.getField(dragonBlue.getClass(), "naturalArmour"), 0.05f);*/
		setNaturalArmour(CreatureTemplate.DRAGON_WHITE_CID, 0.04f);
		/*CreatureTemplate dragonGreen = CreatureTemplateFactory.getInstance().getTemplate(CreatureTemplate.DRAGON_WHITE_CID);
		ReflectionUtil.setPrivateField(dragonGreen, ReflectionUtil.getField(dragonGreen.getClass(), "naturalArmour"), 0.05f);*/
		setNaturalArmour(CreatureTemplate.DRAGON_BLACK_CID, 0.055f);
		/*CreatureTemplate dragonBlack = CreatureTemplateFactory.getInstance().getTemplate(CreatureTemplate.DRAGON_BLACK_CID);
		ReflectionUtil.setPrivateField(dragonBlack, ReflectionUtil.getField(dragonBlack.getClass(), "naturalArmour"), 0.07f);*/
		setNaturalArmour(CreatureTemplate.DRAGON_WHITE_CID, 0.04f);
		/*CreatureTemplate dragonWhite = CreatureTemplateFactory.getInstance().getTemplate(CreatureTemplate.DRAGON_WHITE_CID);
		ReflectionUtil.setPrivateField(dragonWhite, ReflectionUtil.getField(dragonWhite.getClass(), "naturalArmour"), 0.05f);*/
		// Drake natural armour increases:
		setNaturalArmour(CreatureTemplate.DRAKE_RED_CID, 0.075f);
		/*CreatureTemplate drakeRed = CreatureTemplateFactory.getInstance().getTemplate(CreatureTemplate.DRAKE_RED_CID);
		ReflectionUtil.setPrivateField(drakeRed, ReflectionUtil.getField(drakeRed.getClass(), "naturalArmour"), 0.085f);*/
		setNaturalArmour(CreatureTemplate.DRAKE_BLUE_CID, 0.075f);
		/*CreatureTemplate drakeBlue = CreatureTemplateFactory.getInstance().getTemplate(CreatureTemplate.DRAKE_BLUE_CID);
		ReflectionUtil.setPrivateField(drakeBlue, ReflectionUtil.getField(drakeBlue.getClass(), "naturalArmour"), 0.085f);*/
		setNaturalArmour(CreatureTemplate.DRAKE_WHITE_CID, 0.085f);
		/*CreatureTemplate drakeWhite = CreatureTemplateFactory.getInstance().getTemplate(CreatureTemplate.DRAKE_WHITE_CID);
		ReflectionUtil.setPrivateField(drakeWhite, ReflectionUtil.getField(drakeWhite.getClass(), "naturalArmour"), 0.1f);*/
		setNaturalArmour(CreatureTemplate.DRAKE_GREEN_CID, 0.075f);
		/*CreatureTemplate drakeGreen = CreatureTemplateFactory.getInstance().getTemplate(CreatureTemplate.DRAKE_GREEN_CID);
		ReflectionUtil.setPrivateField(drakeGreen, ReflectionUtil.getField(drakeGreen.getClass(), "naturalArmour"), 0.085f);*/
		setNaturalArmour(CreatureTemplate.DRAKE_BLACK_CID, 0.065f);
		/*CreatureTemplate drakeBlack = CreatureTemplateFactory.getInstance().getTemplate(CreatureTemplate.DRAKE_BLACK_CID);
		ReflectionUtil.setPrivateField(drakeBlack, ReflectionUtil.getField(drakeBlack.getClass(), "naturalArmour"), 0.065f);*/
		// Goblin leader natural armour increase:
		setNaturalArmour(CreatureTemplate.GOBLIN_LEADER_CID, 0.075f);
		/*CreatureTemplate goblinLeader = CreatureTemplateFactory.getInstance().getTemplate(CreatureTemplate.GOBLIN_LEADER_CID);
		ReflectionUtil.setPrivateField(goblinLeader, ReflectionUtil.getField(goblinLeader.getClass(), "naturalArmour"), 0.085f);*/
		
		// Set hens and roosters as grazers
		setGrazer(CreatureTemplate.HEN_CID);
		setGrazer(CreatureTemplate.CHICKEN_CID);
		setGrazer(CreatureTemplate.ROOSTER_CID);
		setGrazer(CreatureTemplate.PIG_CID);
		
		// Set worg fields
		setWorgFields(CreatureTemplate.WORG_CID);
	}
}
