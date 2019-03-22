package mod.sin.wyvern.bestiary;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.AutoEquipMethods;
import com.wurmonline.server.combat.Weapon;
import com.wurmonline.server.creatures.*;
import com.wurmonline.server.items.*;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.BodyPartConstants;
import com.wurmonline.shared.constants.CreatureTypes;
import javassist.*;
import javassist.bytecode.Descriptor;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import mod.sin.items.SealedMap;
import mod.sin.lib.Util;
import mod.sin.wyvern.RareSpawns;
import mod.sin.wyvern.Titans;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Server;
import com.wurmonline.server.combat.Archery;
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
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

public class MethodsBestiary {
	protected static Logger logger = Logger.getLogger(MethodsBestiary.class.getName());

	protected static boolean isUsuallyHitched(int templateId){
	    if(templateId == Charger.templateId){
	        return true;
        }else if(templateId == CreatureTemplateIds.HORSE_CID || templateId == CreatureTemplateIds.HELL_HORSE_CID){
	        return true;
        }else if(templateId == WyvernBlue.templateId){
	        return true;
        }else if(templateId == WyvernBlack.templateId){
	        return true;
        }else if(templateId == WyvernGreen.templateId){
	        return true;
        }else if(templateId == WyvernRed.templateId){
	        return true;
        }else if(templateId == WyvernWhite.templateId){
	        return true;
        }
        return false;
    }
	
	public static float getAdjustedSizeMod(CreatureStatus status){
		try {
			Creature statusHolder = ReflectionUtil.getPrivateField(status, ReflectionUtil.getField(status.getClass(), "statusHolder"));
			float aiDataModifier = 1.0f;
			if(statusHolder.getCreatureAIData() != null){
			    aiDataModifier = statusHolder.getCreatureAIData().getSizeModifier();
            }
			byte modtype = ReflectionUtil.getPrivateField(status, ReflectionUtil.getField(status.getClass(), "modtype"));
			float ageSizeModifier = ReflectionUtil.callPrivateMethod(status, ReflectionUtil.getMethod(status.getClass(), "getAgeSizeModifier"));
            float floatToRet = 1.0f;
			if (/*(!statusHolder.isVehicle() || statusHolder.isDragon()) &&*/ modtype != 0) {
	            float change = 0.0f;
	            switch (modtype) {
	                case CreatureTypes.C_MOD_RAGING: {
	                    change = 0.4f;
	                    break;
	                }
	                case CreatureTypes.C_MOD_SLOW: {
	                    change = 0.7f;
	                    break;
	                }
	                case CreatureTypes.C_MOD_GREENISH: {
	                    change = 1.0f;
	                    break;
	                }
	                case CreatureTypes.C_MOD_LURKING: {
	                    change = -0.2f;
	                    break;
	                }
	                case CreatureTypes.C_MOD_SLY: {
	                    change = -0.1f;
	                    break;
	                }
	                case CreatureTypes.C_MOD_HARDENED: {
	                    change = 0.5f;
	                    break;
	                }
	                case CreatureTypes.C_MOD_SCARED: {
	                    change = 0.3f;
	                    break;
	                }
	                case CreatureTypes.C_MOD_CHAMPION: {
	                    change = 2.0f;
	                    break;
	                }
                    case CreatureTypes.C_MOD_SIZESMALL: {
                        change = -0.5f;
                        break;
                    }
                    case CreatureTypes.C_MOD_SIZEMINI: {
                        change = -0.75f;
                        break;
                    }
                    case CreatureTypes.C_MOD_SIZETINY: {
                        change = -0.875f;
                        break;
                    }
	                default: {
	                    //return floatToRet * ageSizeModifier;
	                }
	            }
	            if(isUsuallyHitched(statusHolder.getTemplate().getTemplateId())){
	                change *= 0.2f;
                }
	            floatToRet += change;
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
			}else if(templateId == WyvernBlue.templateId){
				floatToRet *= 0.6f;
			}else if(templateId == MacroSlayer.templateId){
	        	floatToRet *= 1.5f;
	        }else if(templateId == ForestSpider.templateId){
	        	floatToRet *= 0.7f;
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
	        	floatToRet *= 1.15f;
	        }else if(templateId == Terror.templateId){
	            floatToRet *= 3.0f;
            }else if(templateId == IceCat.templateId){
	            floatToRet *= 1.7f;
            }

            if (statusHolder.getHitched() == null && statusHolder.getTemplate().getTemplateId() == 82 && !statusHolder.getNameWithoutPrefixes().equalsIgnoreCase(statusHolder.getTemplate().getName())) {
                floatToRet *= 2.0f;
            }
            if (!statusHolder.isVehicle() && statusHolder.hasTrait(28)) {
                floatToRet *= 1.5f;
            }
	        
	        return floatToRet * ageSizeModifier * aiDataModifier;
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
			    //effs.addSpellEffect(new SpellEffect(titanWeapon.getWurmId(), (byte) 111, 200, 20000000)); // Phasing
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
            return titanWeapon;
        } catch (FailedException | NoSuchTemplateException e) {
			e.printStackTrace();
		}
		return null;
	}

    public static void checkEnchantedBreed(Creature creature){
        int tile = Server.surfaceMesh.getTile(creature.getTileX(), creature.getTileY());
        byte type = Tiles.decodeType(tile);
        if (type == Tiles.Tile.TILE_ENCHANTED_GRASS.id){
            logger.info("Creature "+creature.getName()+" was born on enchanted grass, and has a negative trait removed!");
            Server.getInstance().broadCastAction(creature.getName()+" was born on enchanted grass, and feels more healthy!", creature, 10);
            creature.removeRandomNegativeTrait();
        }
    }

    public static boolean shouldBreedName(Creature creature){
        if(creature.getTemplate().getTemplateId() == WyvernBlack.templateId){
            return true;
        }else if(creature.getTemplate().getTemplateId() == WyvernGreen.templateId){
            return true;
        }else if(creature.getTemplate().getTemplateId() == WyvernRed.templateId){
            return true;
        }else if(creature.getTemplate().getTemplateId() == WyvernWhite.templateId){
			return true;
		}else if(creature.getTemplate().getTemplateId() == WyvernBlue.templateId){
			return true;
		}else if(creature.getTemplate().getTemplateId() == Charger.templateId){
            return true;
        }
        return creature.isHorse();
    }

    public static boolean isGhostCorpse(Creature creature){
        int templateId = creature.getTemplate().getTemplateId();
        if(templateId == Avenger.templateId){
            return true;
        }else if(templateId == SpiritTroll.templateId){
            return true;
        }else if(templateId == Charger.templateId){
            return true;
        }
        return false;
    }

    public static float getCustomSpellResistance(Creature creature){
        int templateId = creature.getTemplate().getTemplateId();
        if(templateId == Avenger.templateId){
            return 0.5f;
        }else if(templateId == Charger.templateId){
            return 1.4f;
        }else if(templateId == Giant.templateId){
            return 0.3f;
        }else if(templateId == LargeBoar.templateId){
            return 0.8f;
        }else if(templateId == Reaper.templateId){
            return 0.1f;
        }else if(templateId == SpectralDrake.templateId){
            return 0.1f;
        }else if(templateId == SpiritTroll.templateId){
            return 0.2f;
        }else if(templateId == WyvernBlack.templateId){
            return 0.4f;
        }else if(templateId == WyvernGreen.templateId){
            return 0.6f;
        }else if(templateId == WyvernWhite.templateId){
            return 0.5f;
        }else if(templateId == WyvernRed.templateId){
			return 0.25f;
		}else if(templateId == WyvernBlue.templateId){
			return 0.20f;
		}
        return -1f;
    }

    public static boolean isNotHitchable(Creature creature){
	    if(creature.isUnique()){
	        return true;
        }
        int templateId = creature.getTemplate().getTemplateId();
	    if(templateId == Avenger.templateId){
	        return true;
        }else if(templateId == Giant.templateId){
	        return true;
        }else if(templateId == SpiritTroll.templateId){
	        return true;
        }else if(templateId == Creatures.TROLL_CID){
	        return true;
        }else if(templateId == Creatures.GOBLIN_CID){
	        return true;
        }
        return false;
    }

    public static boolean isSacrificeImmune(Creature creature){
        if(Titans.isTitan(creature) || Titans.isTitanMinion(creature)){
            return true;
        }else if(RareSpawns.isRareCreature(creature)){
            return true;
        }else if(creature.isUnique()){
            return true;
        }
        return false;
    }
	
	public static boolean isArcheryImmune(Creature performer, Creature defender){
		if(Titans.isTitan(defender) || Titans.isTitanMinion(defender)){
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
		}/*else if(defender.getTemplate().isNotRebirthable()){
			immune = true;
		}*/else if(defender.isUnique()){
			immune = true;
		}
		if(immune){
			performer.getCommunicator().sendCombatNormalMessage(message);
		}
		return immune;
	}

    public static boolean blockSkillFrom(Creature defender, Creature attacker){
        if(defender == null || attacker == null){
            return false;
        }
        if(defender.isPlayer() && defender.getTarget() != attacker){
            return true;
        }
        if(defender.isPlayer()){
            Item weap = defender.getPrimWeapon();
            if(weap != null && weap.isWeapon()){
                try {
                    double dam = Weapon.getModifiedDamageForWeapon(weap, defender.getSkills().getSkill(SkillList.BODY_STRENGTH), true) * 1000.0;
                    dam += Server.getBuffedQualityEffect(weap.getCurrentQualityLevel() / 100.0f) * (double)Weapon.getBaseDamageForWeapon(weap) * 2400.0;
                    if(attacker.getArmourMod() < 0.1f){
                    	return false;
					}
                    if(dam * attacker.getArmourMod() < 3000){
                        return true;
                    }
                } catch (NoSuchSkillException e) {
                    e.printStackTrace();
                }
            }else{
                if(defender.getBonusForSpellEffect(Enchants.CRET_BEARPAW) < 50f){
                    return true;
                }
            }
        }
        try {
            if(defender.isPlayer() && attacker.getArmour(BodyPartConstants.TORSO) != null){
                return true;
            }
        } catch (NoArmourException | NoSpaceException ignored) {
        }
        return false;
    }

    public static boolean denyPathingOverride(Creature creature){
        if(creature.getTemplate().getTemplateId() == Charger.templateId){
            return true;
        }
        return false;
    }

    public static boolean hasCustomCorpseSize(Creature creature){
        int templateId = creature.getTemplate().getTemplateId();
        if(templateId == Avenger.templateId){
            return true;
        }else{
            return Titans.isTitan(creature);
        }
    }

    public static void setCorpseSizes(Creature creature, Item corpse){
        if(corpse.getTemplateId() != ItemList.corpse){
            return;
        }
        int templateId = creature.getTemplate().getTemplateId();
        boolean sendStatus = false;
        int size = 50000;
        if(templateId == Avenger.templateId){
            size *= 1.2;
            corpse.setSizes(size);
            sendStatus = true;
        }else if(Titans.isTitan(creature)){
            size *= 1.5;
            corpse.setSizes(size);
            sendStatus = true;
        }else{
            corpse.setSizes((int)((float)(corpse.getSizeX() * (creature.getSizeModX() & 255)) / 64.0f), (int)((float)(corpse.getSizeY() * (creature.getSizeModY() & 255)) / 64.0f), (int)((float)(corpse.getSizeZ() * (creature.getSizeModZ() & 255)) / 64.0f));
        }
        if(sendStatus){
            try {
                Zone zone = Zones.getZone((int)corpse.getPosX() >> 2, (int)corpse.getPosY() >> 2, corpse.isOnSurface());
                zone.removeItem(corpse, true, true);
                zone.addItem(corpse, true, false, false);
            } catch (NoSuchZoneException e) {
                e.printStackTrace();
            }
        }
    }

    public static byte newCreatureType(int templateid, byte ctype) throws Exception{
        CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(templateid);
        if(ctype == 0 && (template.isAggHuman() || template.getBaseCombatRating() > 10) && !template.isUnique() && !Titans.isTitan(templateid)){
            if(Server.rand.nextInt(5) == 0){
                ctype = (byte) (Server.rand.nextInt(11)+1);
                if(Server.rand.nextInt(50) == 0){
                    ctype = 99;
                }
            }
        }
        return ctype;
    }
	
	public static void modifyNewCreature(Creature creature){
		try{
			if(Titans.isTitan(creature)){
				Titans.addTitan(creature);
				MiscChanges.sendGlobalFreedomChat(creature, "The titan "+creature.getName()+" has stepped into the mortal realm. Challenge them if you dare.", 255, 105, 180);
				/*if(creature.getTemplate().getTemplateId() == Lilith.templateId){
				    Item titanWeapon = createNewTitanWeapon(creature.getName(), new int[]{VindictivesVengeance.templateId, WilhelmsWrath.templateId});
					creature.getInventory().insertItem(titanWeapon);
				}else if(creature.getTemplate().getTemplateId() == Ifrit.templateId){
				    Item titanWeapon = createNewTitanWeapon(creature.getName(), new int[]{MaartensMight.templateId, RaffehsRage.templateId});
					creature.getInventory().insertItem(titanWeapon);
				}*/
				Titans.addTitanLoot(creature);
			}else if(creature.getTemplate().getTemplateId() == Facebreyker.templateId){
				Item club = ItemFactory.createItem(Club.templateId, 80f+(Server.rand.nextFloat()*15f), Server.rand.nextBoolean() ? Materials.MATERIAL_GLIMMERSTEEL : Materials.MATERIAL_ADAMANTINE, Server.rand.nextBoolean() ? (byte) 0 : (byte) 1, "Facebreyker");
				creature.getInventory().insertItem(club);
			} else if(RareSpawns.isRareCreature(creature)){
                MiscChanges.sendServerTabMessage("event", "A rare "+creature.getName()+" has surfaced.", 123, 104, 238);
                Item sealedMap = ItemFactory.createItem(SealedMap.templateId, 60f+(30f*Server.rand.nextFloat()), creature.getName());
                creature.getInventory().insertItem(sealedMap, true);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private static void setCombatRating(int templateId, float value){
		try{
			CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(templateId);
			if(template != null){
				ReflectionUtil.setPrivateField(template, ReflectionUtil.getField(template.getClass(), "baseCombatRating"), value);
			}
		} catch (NoSuchCreatureTemplateException | IllegalArgumentException | IllegalAccessException | ClassCastException | NoSuchFieldException e) {
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

    private static void setUniqueTypes(int templateId){
        try{
            CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(templateId);
            if(template != null){
                ReflectionUtil.setPrivateField(template, ReflectionUtil.getField(template.getClass(), "isNotRebirthable"), true);
                ReflectionUtil.setPrivateField(template, ReflectionUtil.getField(template.getClass(), "regenerating"), false);
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

	private static void setNoCorpse(int templateId){
		try{
			CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(templateId);
			if(template != null){
				ReflectionUtil.setPrivateField(template, ReflectionUtil.getField(template.getClass(), "noCorpse"), true);
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

	private static void setSkill(int templateId, int skillId, float value){
		try{
			CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(templateId);
			if(template != null){
				Skills skills = ReflectionUtil.getPrivateField(template, ReflectionUtil.getField(template.getClass(), "skills"));
				skills.learnTemp(skillId, value);
				ReflectionUtil.setPrivateField(template, ReflectionUtil.getField(template.getClass(), "skills"), skills);
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
		setCorpseModel(Charger.templateId, "fogspider.");

		// Non-fog spider models
		setCorpseModel(WyvernBlack.templateId, "blackdragonhatchling.");
		setCorpseModel(WyvernGreen.templateId, "greendragonhatchling.");
		setCorpseModel(WyvernRed.templateId, "reddragonhatchling.");
		setCorpseModel(WyvernWhite.templateId, "whitedragonhatchling.");
		setCorpseModel(WyvernBlue.templateId, "bluedragonhatchling.");
		setCorpseModel(Facebreyker.templateId, "riftogre.");
		setCorpseModel(FireCrab.templateId, "crab.");
		setCorpseModel(ForestSpider.templateId, "fogspider.");
		setCorpseModel(Giant.templateId, "forestgiant.");
		setCorpseModel(LargeBoar.templateId, "wildboar.");
		setCorpseModel(HornedPony.templateId, "unicorn.");
		setCorpseModel(IfritSpider.templateId, "lavaspider.");
		setCorpseModel(IfritFiend.templateId, "lavafiend.");
		// Also apply the ghost modifier
		setGhost(SpiritTroll.templateId);
		setGhost(Avenger.templateId);
		setGhost(LilithWraith.templateId);
		setGhost(Charger.templateId);

		// Make uniques no rebirth and non-regenerative.
        setUniqueTypes(CreatureTemplate.DRAGON_BLACK_CID);
        setUniqueTypes(CreatureTemplate.DRAGON_BLUE_CID);
        setUniqueTypes(CreatureTemplate.DRAGON_GREEN_CID);
        setUniqueTypes(CreatureTemplate.DRAGON_RED_CID);
        setUniqueTypes(CreatureTemplate.DRAGON_WHITE_CID);
        setUniqueTypes(CreatureTemplate.DRAKE_BLACK_CID);
        setUniqueTypes(CreatureTemplate.DRAKE_BLUE_CID);
        setUniqueTypes(CreatureTemplate.DRAKE_GREEN_CID);
        setUniqueTypes(CreatureTemplate.DRAKE_RED_CID);
        setUniqueTypes(CreatureTemplate.DRAKE_WHITE_CID);
        setUniqueTypes(CreatureTemplate.GOBLIN_LEADER_CID);
        setUniqueTypes(CreatureTemplate.FOREST_GIANT_CID);
        setUniqueTypes(CreatureTemplate.TROLL_KING_CID);
        setUniqueTypes(CreatureTemplate.CYCLOPS_CID);

		// Dragon natural armour increases:
		setNaturalArmour(CreatureTemplate.DRAGON_BLUE_CID, 0.025f);
		setNaturalArmour(CreatureTemplate.DRAGON_WHITE_CID, 0.025f);
		setNaturalArmour(CreatureTemplate.DRAGON_BLACK_CID, 0.035f);
		setNaturalArmour(CreatureTemplate.DRAGON_WHITE_CID, 0.025f);
		// Drake natural armour increases:
		setNaturalArmour(CreatureTemplate.DRAKE_RED_CID, 0.055f);
		setNaturalArmour(CreatureTemplate.DRAKE_BLUE_CID, 0.055f);
		setNaturalArmour(CreatureTemplate.DRAKE_WHITE_CID, 0.065f);
		setNaturalArmour(CreatureTemplate.DRAKE_GREEN_CID, 0.055f);
		setNaturalArmour(CreatureTemplate.DRAKE_BLACK_CID, 0.045f);
		// Goblin leader natural armour increase:
		setNaturalArmour(CreatureTemplate.GOBLIN_LEADER_CID, 0.045f);

		// Worg armour reduction on Arena
        if(Servers.localServer.PVPSERVER) {
            setNaturalArmour(CreatureTemplate.WORG_CID, 0.3f);
        }

		// Make titan minions drop no corpse
		setNoCorpse(IfritFiend.templateId);
		setNoCorpse(IfritSpider.templateId);
		setNoCorpse(LilithWraith.templateId);
		setNoCorpse(LilithZombie.templateId);

		setNoCorpse(IceCat.templateId);
		setNoCorpse(FireGiant.templateId);
		setNoCorpse(Terror.templateId);
		
		// Set hens and roosters as grazers
		setGrazer(CreatureTemplate.HEN_CID);
		setGrazer(CreatureTemplate.CHICKEN_CID);
		setGrazer(CreatureTemplate.ROOSTER_CID);
		setGrazer(CreatureTemplate.PIG_CID);
		
		// Set worg fields
		setWorgFields(CreatureTemplate.WORG_CID);

		// Set skills for certain creatures
		setSkill(CreatureTemplate.CYCLOPS_CID, SkillList.GROUP_FIGHTING, 80.0f);

		// Set combat rating for valrei creatures to improve their bounty
		setCombatRating(CreatureTemplate.SON_OF_NOGUMP_CID, 30.0f);
	}

	protected static void sendParticleEffect(Communicator comm, long creatureId, Creature creature, String particle, float duration){
        comm.sendAddEffect(creatureId, (short) 27, creature.getPosX(), creature.getPosY(), creature.getPositionZ(), (byte) creature.getLayer(), particle, duration, 0);
    }
	protected static void sendAddEffect(Communicator comm, long creatureId, byte effectNum){
	    comm.sendAttachEffect(creatureId, effectNum, (byte) 1, (byte) -1, (byte) -1, (byte) 1);
    }
    // - Good Effects -
    // rift01 [large], rift02 [small]
    // treasureP [light bubbles]
    // spawneffect2 [sparkle fireworks]
    // reindeer [light sparkles]
    // iceBall_1_1 [clean ice effect]
    // acidBall_1_1 [clean green ball effect]
    // - Bad Effects -
    // spawneffect [sparkle eye cancer]
    // snow1emitter [disappears instantly}, snow2emitter [disappears instantly]
    // spawneffectshort [disappears instantly]
    // iceWispSpark [tiny and unnoticeable]
    // iceBolt1 [very flickery]
    // iceTail1 [small and points downwards]
    // acidWispSpark [basically invisible]
    // acidTail1 [inconsistent trail]
    // acidBolt1 [very flickery]
    // lightningTail1 [weird effect]
	public static void addCreatureSpecialEffect(long creatureId, Communicator comm, Creature creature){
	    int templateId = creature.getTemplate().getTemplateId();
	    if(templateId == IceCat.templateId){
            String particle = "iceBall_1_1";
            sendParticleEffect(comm, creatureId, creature, particle, Float.MAX_VALUE);
        }else if(templateId == FireCrab.templateId){
	        sendAddEffect(comm, creatureId, (byte) 1);
        }else if(templateId == FireGiant.templateId){
            sendAddEffect(comm, creatureId, (byte) 1);
        }
    }

	public static void preInit(){
        try{
            ClassPool classPool = HookManager.getInstance().getClassPool();
            final Class<MethodsBestiary> thisClass = MethodsBestiary.class;
            String replace;

            Util.setReason("Disable sacrificing strong creatures.");
            CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");
            CtClass ctItem = classPool.get("com.wurmonline.server.items.Item");
            CtClass ctAction = classPool.get("com.wurmonline.server.behaviours.Action");
            CtClass ctMethodsReligion = classPool.get("com.wurmonline.server.behaviours.MethodsReligion");
            CtClass[] params1 = {
                    ctCreature,
                    ctCreature,
                    ctItem,
                    ctAction,
                    CtClass.floatType
            };
            String desc1 = Descriptor.ofMethod(CtClass.booleanType, params1);
            replace = "if("+MethodsBestiary.class.getName()+".isSacrificeImmune($2)){" +
                    "  performer.getCommunicator().sendNormalServerMessage(\"This creature cannot be sacrificed.\");" +
                    "  return true;" +
                    "}";
            Util.insertBeforeDescribed(thisClass, ctMethodsReligion, "sacrifice", desc1, replace);

            Util.setReason("Disable afk training.");
            CtClass ctCombatHandler = classPool.get("com.wurmonline.server.creatures.CombatHandler");
            replace = "if("+MethodsBestiary.class.getName()+".blockSkillFrom($1, $0)){"+//"if($1.isPlayer() && $1.getTarget() != $0){" +
                    //"  logger.info(\"Non-targeted mob detected - \" + $1.getName());" +
                    "  $_ = true;" +
                    "}else{" +
                    "  $_ = $proceed($$);" +
                    "}";
            Util.instrumentDeclared(thisClass, ctCombatHandler, "setDamage", "isNoSkillFor", replace);
            Util.instrumentDeclared(thisClass, ctCombatHandler, "checkDefenderParry", "isNoSkillFor", replace);
            Util.instrumentDeclared(thisClass, ctCombatHandler, "checkShield", "isNoSkillFor", replace);
            Util.instrumentDeclared(thisClass, ctCombatHandler, "setBonuses", "isNoSkillFor", replace);
            CtMethod[] ctGetDamages = ctCombatHandler.getDeclaredMethods("getDamage");
            for(CtMethod method : ctGetDamages){
                method.instrument(new ExprEditor(){
                    public void edit(MethodCall m) throws CannotCompileException {
                        if (m.getMethodName().equals("isNoSkillFor")) {
                            m.replace("if("+MethodsBestiary.class.getName()+".blockSkillFrom($1, $0)){" + //"if($1.isPlayer() && $1.getTarget() != $0){" +
                                    //"  logger.info(\"Non-targeted mob detected - \" + $1.getName());" +
                                    "  $_ = true;" +
                                    "}else{" +
                                    "  $_ = $proceed($$);" +
                                    "}");
                        }
                    }
                });
            }

			// Die method description
			CtClass ctString = classPool.get("java.lang.String");
			CtClass[] params5 = new CtClass[]{
					CtClass.booleanType,
					ctString,
					CtClass.booleanType
			};
			String desc5 = Descriptor.ofMethod(CtClass.voidType, params5);

            Util.setReason("Deny chargers walking through walls.");
            CtClass ctPathFinder = classPool.get("com.wurmonline.server.creatures.ai.PathFinder");
            replace = "if("+MethodsBestiary.class.getName()+".denyPathingOverride($0)){" +
                    "  $_ = false;" +
                    "}else{" +
                    "  $_ = $proceed($$);" +
                    "}";
            Util.instrumentDeclared(thisClass, ctPathFinder, "canPass", "isGhost", replace);
            Util.instrumentDeclared(thisClass, ctCreature, "setPathing", "isGhost", replace);
            Util.instrumentDeclared(thisClass, ctCreature, "startPathingToTile", "isGhost", replace);
            Util.instrumentDeclared(thisClass, ctCreature, "moveAlongPath", "isGhost", replace);
            Util.instrumentDeclared(thisClass, ctCreature, "takeSimpleStep", "isGhost", replace);
            Util.instrumentDescribed(thisClass, ctCreature, "die", desc5, "isGhost", replace);

            Util.setReason("Apply random types to creatures in the wilderness.");
            CtClass[] params2 = {
                    CtClass.intType,
                    CtClass.booleanType,
                    CtClass.floatType,
                    CtClass.floatType,
                    CtClass.floatType,
                    CtClass.intType,
                    classPool.get("java.lang.String"),
                    CtClass.byteType,
                    CtClass.byteType,
                    CtClass.byteType,
                    CtClass.booleanType,
                    CtClass.byteType,
                    CtClass.intType
            };
            String desc2 = Descriptor.ofMethod(ctCreature, params2);
            replace = "$10 = "+MethodsBestiary.class.getName()+".newCreatureType($1, $10);";
            Util.insertBeforeDescribed(thisClass, ctCreature, "doNew", desc2, replace);

            Util.setReason("Enable archery against ghost targets.");
            CtClass ctArchery = classPool.get("com.wurmonline.server.combat.Archery");
            CtMethod[] archeryAttacks = ctArchery.getDeclaredMethods("attack");
            for(CtMethod method : archeryAttacks){
                method.instrument(new ExprEditor(){
                    public void edit(MethodCall m) throws CannotCompileException {
                        if (m.getMethodName().equals("isGhost")) {
                            m.replace("$_ = false;");
                            logger.info("Enabled archery against ghost targets in archery attack method.");
                        }
                    }
                });
            }

            Util.setReason("Disable archery altogether against certain creatures.");
            CtClass[] params3 = {
                    ctCreature,
                    ctCreature,
                    ctItem,
                    CtClass.floatType,
                    ctAction
            };
            String desc3 = Descriptor.ofMethod(CtClass.booleanType, params3);
            replace = "if("+MethodsBestiary.class.getName()+".isArcheryImmune($1, $2)){"
                    + "  return true;"
                    + "}";
            Util.insertBeforeDescribed(thisClass, ctArchery, "attack", desc3, replace);

            Util.setReason("Auto-Genesis a creature born on enchanted grass");
            replace = MethodsBestiary.class.getName()+".checkEnchantedBreed(newCreature);"
                    + "$_ = $proceed($$);";
            Util.instrumentDeclared(thisClass, ctCreature, "checkPregnancy", "saveCreatureName", replace);

            Util.setReason("Set custom corpse sizes.");
            replace = "$_ = $proceed($$);"
                    + "if("+MethodsBestiary.class.getName()+".hasCustomCorpseSize(this)){"
                    + "  "+MethodsBestiary.class.getName()+".setCorpseSizes(this, corpse);"
                    + "}";
            Util.instrumentDescribed(thisClass, ctCreature, "die", desc5, "addItem", replace);

            Util.setReason("Add spell resistance to custom creatures.");
            replace = "float cResist = "+MethodsBestiary.class.getName()+".getCustomSpellResistance(this);" +
                    "if(cResist >= 0f){" +
                    "  return cResist;" +
                    "}";
            Util.insertBeforeDeclared(thisClass, ctCreature, "addSpellResistance", replace);

            Util.setReason("Allow custom creatures to have breeding names.");
            replace = "$_ = "+MethodsBestiary.class.getName()+".shouldBreedName(this);";
            Util.instrumentDeclared(thisClass, ctCreature, "checkPregnancy", "isHorse", replace);

            Util.setReason("Allow ghost creatures to breed (Chargers).");
            CtClass ctMethodsCreatures = classPool.get("com.wurmonline.server.behaviours.MethodsCreatures");
            replace = "$_ = false;";
            Util.instrumentDeclared(thisClass, ctMethodsCreatures, "breed", "isGhost", replace);

            Util.setReason("Allow ghost creatures to drop corpses.");
            replace = "if("+MethodsBestiary.class.getName()+".isGhostCorpse(this)){"
                    + "  $_ = false;"
                    + "}else{"
                    + "  $_ = $proceed($$);"
                    + "}";
            Util.instrumentDescribed(thisClass, ctCreature, "die", desc5, "isGhost", replace);

            Util.setReason("Attach special effects to creatures.");
            CtClass ctVirtualZone = classPool.get("com.wurmonline.server.zones.VirtualZone");
            CtClass[] params4 = {
                    CtClass.longType,
                    CtClass.booleanType,
                    CtClass.longType,
                    CtClass.floatType,
                    CtClass.floatType,
                    CtClass.floatType
            };
            String desc4 = Descriptor.ofMethod(CtClass.booleanType, params4);
            replace = "$_ = $proceed($$);" +
                    MethodsBestiary.class.getName()+".addCreatureSpecialEffect(copyId != -10 ? copyId : creatureId, $0, creature);";
            Util.instrumentDescribed(thisClass, ctVirtualZone, "addCreature", desc4, "sendNewCreature", replace);

			Util.setReason("Ensure unique creatures cannot be hitched to vehicles.");
			CtClass ctVehicle = classPool.get("com.wurmonline.server.behaviours.Vehicle");
			replace = "if("+MethodsBestiary.class.getName()+".isNotHitchable($1)){" +
					"  return false;" +
					"}";
			Util.insertBeforeDeclared(thisClass, ctVehicle, "addDragger", replace);

		} catch ( CannotCompileException | NotFoundException | IllegalArgumentException | ClassCastException e) {
        throw new HookException(e);
    }
    }
}
