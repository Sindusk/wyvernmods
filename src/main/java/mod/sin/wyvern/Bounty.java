package mod.sin.wyvern;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.wurmonline.server.TimeConstants;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import mod.sin.lib.Util;
import mod.sin.wyvern.bounty.LootBounty;
import mod.sin.wyvern.bounty.PlayerBounty;

public class Bounty {
	public static final Logger logger = Logger.getLogger(Bounty.class.getName());
	//protected static WyvernMods mod;
	public static HashMap<String, Integer> reward = new HashMap<>();

    public static long lastAttacked(Map<Long, Long> attackers, long playerId){
    	return System.currentTimeMillis()-attackers.get(playerId);
    }
    
	public static boolean isCombatant(Map<Long, Long> attackers, long playerId){
    	long now = System.currentTimeMillis();
    	long delta = now-attackers.get(playerId);
    	return delta < TimeConstants.MINUTE_MILLIS*2;
    	/*if(delta > 120000){
    		return false;
    	}
    	return true;*/
    }
	
	public static Map<Long, Long> getAttackers(Creature mob){
		try {
			return ReflectionUtil.getPrivateField(mob, ReflectionUtil.getField(mob.getClass(), "attackers"));
		} catch (IllegalArgumentException | IllegalAccessException | ClassCastException | NoSuchFieldException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static double getCreatureStrength(Creature mob){
		float combatRating = mob.getBaseCombatRating() + mob.getBonusCombatRating();
	    float maxDmg = Math.max(mob.getTemplate().getBreathDamage(), mob.getHandDamage());
	    maxDmg = Math.max(maxDmg, mob.getBiteDamage());
	    maxDmg = Math.max(maxDmg, mob.getKickDamage());
	    maxDmg = Math.max(maxDmg, mob.getHeadButtDamage());
	    double fighting = mob.getFightingSkill().getKnowledge();
	    double weaponlessFighting = mob.getWeaponLessFightingSkill().getKnowledge();
	    double fs = Math.max(fighting, weaponlessFighting);
	    double bodyStr = mob.getBodyStrength().getKnowledge();
	    //double cretStr = 2000D + ((double)combatRating*(double)maxDmg*Math.sqrt(fs)*bodyStr);
	    //logger.info("pre-armour: "+cretStr);
	    //cretStr /= Math.max(mob.getArmourMod(), 0.001d);
	    fs /= mob.getArmourMod();
	    double cretStr = 100D + (combatRating*Math.cbrt(maxDmg)*Math.cbrt(fs)*Math.cbrt(bodyStr));
	    cretStr *= 0.8d;
	    //logger.info("post-armour: "+cretStr);
	    //cretStr *= 1-(Math.min(Math.max(mob.getArmourMod(), 0.001d), 0.8f));
	    //cretStr = 2000D + ((double)combatRating*(double)maxDmg*Math.sqrt(fs)*bodyStr);
	    double k = 100000d;
	    cretStr = (cretStr*Math.pow(2, (-(cretStr/k)))+k*(1-Math.pow(2, -cretStr/k)))/(1+Math.pow(2, -cretStr/k));
	    if(mob.isAggHuman() && cretStr < 100D){
	    	cretStr *= 1+(Server.rand.nextFloat()*0.2f);
	    	cretStr = Math.max(cretStr, 100D);
	    }else if(!mob.isAggHuman() && cretStr < 300D){
			cretStr *= 0.4f;
	    	cretStr *= 1+(Server.rand.nextFloat()*0.2f);
	    	cretStr = Math.max(cretStr, 10D);
		}
	    //logger.info("capped: "+cretStr);
	    return cretStr;
	}
	
	/*public static void preInit(WyvernMods mod){
		Bounty.mod = mod;
		//reward.put("black wolf", 750);
    }*/
	
	public static void init(){
		try {
        	ClassPool classPool = HookManager.getInstance().getClassPool();
            final Class<Bounty> thisClass = Bounty.class;
			String replace;
            
            CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");

            if (WyvernMods.usePlayerBounty) {
                Util.setReason("Hook for Player Bounty.");
                replace = PlayerBounty.class.getName() + ".checkPlayerBounty(player, this);"
                        + "$_ = $proceed($$);";
                Util.instrumentDeclared(thisClass, ctCreature, "modifyFightSkill", "checkCoinAward", replace);
            }

			// Die method description
			CtClass ctString = classPool.get("java.lang.String");
			CtClass[] params1 = new CtClass[]{
					CtClass.booleanType,
					ctString,
					CtClass.booleanType
			};
			String desc1 = Descriptor.ofMethod(CtClass.voidType, params1);

            replace = "$_ = $proceed($$);"
          		  	+ LootBounty.class.getName()+".checkLootTable(this, corpse);";
            Util.instrumentDescribed(thisClass, ctCreature, "die", desc1, "setRotation", replace);

            // doNew(int templateid, boolean createPossessions, float aPosX, float aPosY, float aRot, int layer, String name, byte gender, byte kingdom, byte ctype, boolean reborn, byte age)

            // -- Enable adjusting color for creatures -- //
            /*CtClass ctCreatureTemplate = classPool.get("com.wurmonline.server.creatures.CreatureTemplate");
            replace = "if("+Bestiary.class.getName()+".checkColorTemplate(this)){"
          		+ "  return "+Bestiary.class.getName()+".getCreatureColorRed(this);"
          		+ "}";
            Util.insertBeforeDeclared(thisClass, ctCreatureTemplate, "getColorRed", replace);
            replace = "if("+Bestiary.class.getName()+".checkColorTemplate(this)){"
          		+ "  return "+Bestiary.class.getName()+".getCreatureColorGreen(this);"
          		+ "}";
            Util.insertBeforeDeclared(thisClass, ctCreatureTemplate, "getColorGreen", replace);
            replace = "if("+Bestiary.class.getName()+".checkColorTemplate(this)){"
          		+ "  return "+Bestiary.class.getName()+".getCreatureColorBlue(this);"
          		+ "}";
            Util.insertBeforeDeclared(thisClass, ctCreatureTemplate, "getColorBlue", replace);*/
            /*ctCreatureTemplate.getDeclaredMethod("getColorRed").insertBefore("if(mod.sin.wyvern.Bestiary.checkColorTemplate(this)){"
            		+ "  return mod.sin.wyvern.Bestiary.getCreatureColorRed(this);"
            		+ "}");
            ctCreatureTemplate.getDeclaredMethod("getColorGreen").insertBefore("if(mod.sin.wyvern.Bestiary.checkColorTemplate(this)){"
            		+ "  return mod.sin.wyvern.Bestiary.getCreatureColorGreen(this);"
            		+ "}");
            ctCreatureTemplate.getDeclaredMethod("getColorBlue").insertBefore("if(mod.sin.wyvern.Bestiary.checkColorTemplate(this)){"
          			+ "  return mod.sin.wyvern.Bestiary.getCreatureColorBlue(this);"
          			+ "}");*/
          
            // -- When a creature takes damage, track the damage taken -- //
            /*CtClass[] params2 = {
        		  ctCreature,
        		  ctCreature,
        		  CtClass.byteType,
        		  CtClass.intType,
        		  CtClass.doubleType,
        		  CtClass.floatType,
        		  classPool.get("java.lang.String"),
        		  classPool.get("com.wurmonline.server.combat.Battle"),
        		  CtClass.floatType,
        		  CtClass.floatType,
        		  CtClass.booleanType,
        		  CtClass.booleanType
            };
            String desc2 = Descriptor.ofMethod(CtClass.booleanType, params2);
            CtClass ctCombatEngine = classPool.get("com.wurmonline.server.combat.CombatEngine");
            replace = "if($1 != null && $2 != null){"
          		+ "  "+Bounty.class.getName()+".addDealtDamage($2.getWurmId(), $1.getWurmId(), $5);"
          		+ "}";
            Util.insertBeforeDescribed(thisClass, ctCombatEngine, "addWound", desc2, replace);*/
            //ctCombatEngine.getMethod("addWound", desc2).insertBefore("if($1 != null && $2 != null){mod.sin.wyvern.bounty.MethodsBounty.addDealtDamage($2.getWurmId(), $1.getWurmId(), $5);}");
          
        }
        catch (NotFoundException e) {
		    throw new HookException(e);
        }
	}
}
