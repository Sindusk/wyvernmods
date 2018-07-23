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
import mod.sin.wyvern.bestiary.MethodsBestiary;
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
            
            CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");

            /*CtMethod ctCheckBounty = CtMethod.make((String)
            		  "public void checkBounty(com.wurmonline.server.players.Player player, com.wurmonline.server.creatures.Creature mob){"
            		+ "  if(!mod.sin.wyvernmods.bounty.MethodsBounty.isCombatant(this.attackers, player.getWurmId()) || mob.isPlayer() || mob.isReborn()){"
            		+ "    return;"
            		+ "  }"
            		+ (mod.bDebug ? "logger.info(player.getName()+\" killed \"+mob.getName());" : "")
            		+ "  mod.sin.wyvernmods.bounty.MethodsBounty.checkPlayerReward(player, mob);"
            		+ "}", ctCreature);
            ctCreature.addMethod(ctCheckBounty);*/
            String replace;
            replace = ""
            		//+ "mod.sin.wyvern.bounty.MethodsBounty.checkBounty(player, this);"
            		+ PlayerBounty.class.getName()+".checkPlayerBounty(player, this);"
            		+ "$_ = $proceed($$);";
            Util.instrumentDeclared(thisClass, ctCreature, "modifyFightSkill", "checkCoinAward", replace);
          /*ctCreature.getDeclaredMethod("modifyFightSkill").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("checkCoinAward")) {
                        m.replace("mod.sin.wyvern.bounty.MethodsBounty.checkBounty(player, this);"
                        		+ "$_ = $proceed($$);");
                        logger.info("Instrumented checkCoinAward to call checkBounty as well.");
                        return;
                    }
                }
            });*/
            replace = "$_ = $proceed($$);"
          		  	//+ "mod.sin.wyvern.bounty.MethodsBounty.checkLootTable(this, corpse);";
          		  	+ LootBounty.class.getName()+".checkLootTable(this, corpse);";
            Util.instrumentDeclared(thisClass, ctCreature, "die", "setRotation", replace);
            /*ctCreature.getDeclaredMethod("die").instrument(new ExprEditor(){
              public void edit(MethodCall m) throws CannotCompileException {
                  if (m.getMethodName().equals("setRotation")) {
                      m.replace("$_ = $proceed($$);"
                    		  + "mod.sin.wyvern.bounty.MethodsBounty.checkLootTable(this, corpse);");
                      logger.info("Instrumented setRotation to call insertCorpseItems as well.");
                      return;
                  }
              }
            });*/

            // doNew(int templateid, boolean createPossessions, float aPosX, float aPosY, float aRot, int layer, String name, byte gender, byte kingdom, byte ctype, boolean reborn, byte age)
            CtClass[] params = {
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
            String desc = Descriptor.ofMethod(ctCreature, params);
            Util.insertBeforeDescribed(thisClass, ctCreature, "doNew", desc, "logger.info(\"Creating new creature: \"+templateid+\" - \"+(aPosX/4)+\", \"+(aPosY/4)+\" [\"+com.wurmonline.server.creatures.CreatureTemplateFactory.getInstance().getTemplate(templateid).getName()+\"]\");");
          // Debugging to show all new creatures created.
          //CtMethod ctDoNew = ctCreature.getMethod("doNew", "(IZFFFILjava/lang/String;BBBZB)Lcom/wurmonline/server/creatures/Creature;");
          //ctDoNew.insertBefore("logger.info(\"Creating new creature: \"+templateid+\" - \"+(aPosX/4)+\", \"+(aPosY/4)+\" [\"+com.wurmonline.server.creatures.CreatureTemplateFactory.getInstance().getTemplate(templateid).getName()+\"]\");");
          // Modify new creatures
            replace = "$_ = $proceed($$);"
              		//+ "mod.sin.wyvern.bestiary.MethodsBestiary.modifyNewCreature($1);";
            		+ MethodsBestiary.class.getName()+".modifyNewCreature($1);";
            Util.instrumentDescribed(thisClass, ctCreature, "doNew", desc, "sendToWorld", replace);
          /*ctDoNew.instrument(new ExprEditor(){
              public void edit(MethodCall m) throws CannotCompileException {
                  if (m.getMethodName().equals("sendToWorld")) {
                      m.replace("$_ = $proceed($$);"
                      		+ "mod.sin.wyvern.bestiary.MethodsBestiary.modifyNewCreature($1);");
                      return;
                  }
              }
          });*/
          
          // -- Enable adjusting size for creatures -- //
          CtClass ctCreatureStatus = classPool.get("com.wurmonline.server.creatures.CreatureStatus");
          Util.setBodyDeclared(thisClass, ctCreatureStatus, "getSizeMod", "{return "+MethodsBestiary.class.getName()+".getAdjustedSizeMod(this);}");
          //ctCreatureStatus.getDeclaredMethod("getSizeMod").setBody("{return mod.sin.wyvern.bestiary.MethodsBestiary.getAdjustedSizeMod(this);}");
          
          // -- Enable adjusting color for creatures -- //
          /*CtClass ctCreatureTemplate = classPool.get("com.wurmonline.server.creatures.CreatureTemplate");
          replace = "if("+MethodsBestiary.class.getName()+".checkColorTemplate(this)){"
          		+ "  return "+MethodsBestiary.class.getName()+".getCreatureColorRed(this);"
          		+ "}";
          Util.insertBeforeDeclared(thisClass, ctCreatureTemplate, "getColorRed", replace);
          replace = "if("+MethodsBestiary.class.getName()+".checkColorTemplate(this)){"
          		+ "  return "+MethodsBestiary.class.getName()+".getCreatureColorGreen(this);"
          		+ "}";
          Util.insertBeforeDeclared(thisClass, ctCreatureTemplate, "getColorGreen", replace);
          replace = "if("+MethodsBestiary.class.getName()+".checkColorTemplate(this)){"
          		+ "  return "+MethodsBestiary.class.getName()+".getCreatureColorBlue(this);"
          		+ "}";
          Util.insertBeforeDeclared(thisClass, ctCreatureTemplate, "getColorBlue", replace);*/
          /*ctCreatureTemplate.getDeclaredMethod("getColorRed").insertBefore("if(mod.sin.wyvern.bestiary.MethodsBestiary.checkColorTemplate(this)){"
            		+ "  return mod.sin.wyvern.bestiary.MethodsBestiary.getCreatureColorRed(this);"
            		+ "}");
          ctCreatureTemplate.getDeclaredMethod("getColorGreen").insertBefore("if(mod.sin.wyvern.bestiary.MethodsBestiary.checkColorTemplate(this)){"
            		+ "  return mod.sin.wyvern.bestiary.MethodsBestiary.getCreatureColorGreen(this);"
            		+ "}");
          ctCreatureTemplate.getDeclaredMethod("getColorBlue").insertBefore("if(mod.sin.wyvern.bestiary.MethodsBestiary.checkColorTemplate(this)){"
          			+ "  return mod.sin.wyvern.bestiary.MethodsBestiary.getCreatureColorBlue(this);"
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
