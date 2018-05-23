package mod.sin.wyvern;

import com.wurmonline.server.Server;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.HashMap;
import java.util.logging.Logger;

public class Bloodlust {
    public static Logger logger = Logger.getLogger(Bloodlust.class.getName());

    protected static HashMap<Long,Float> lusts = new HashMap<>();
    protected static HashMap<Long,Long> lastLusted = new HashMap<>();
    public static float lustUnique(Creature creature){
        long wurmid = creature.getWurmId();
        if(lusts.containsKey(wurmid)){
            float currentLust = lusts.get(wurmid);
            if(currentLust >= 1.0f){ // When dealing more than 100% extra damage
                Server.getInstance().broadCastAction(creature.getName()+" becomes enraged!", creature, 50);
            }else if(currentLust >= 0.49f){ // When dealing between 50% and 100% extra damage.
                Server.getInstance().broadCastAction(creature.getName()+" is becoming enraged!", creature, 50);
            }else{
                Server.getInstance().broadCastAction(creature.getName()+" is beginning to see red!", creature, 50);
            }
            lusts.put(wurmid, currentLust+0.01f);
        }else{
            lusts.put(wurmid, 0.01f);
        }
        lastLusted.put(wurmid, System.currentTimeMillis());
        return 1.0f+lusts.get(wurmid);
    }

    public static float getLustMult(Creature creature){
        long wurmid = creature.getWurmId();
        if(lusts.containsKey(wurmid)){
            return 1.0f+lusts.get(wurmid);
        }
        return 1.0f;
    }

    public static void pollLusts(){
        for(Long wurmid : lastLusted.keySet()){
            if(System.currentTimeMillis() >= lastLusted.get(wurmid) + TimeConstants.MINUTE_MILLIS*10){
                logger.info("Bloodlust for "+wurmid+" expired. Removing from lists.");
                Creature creature = Creatures.getInstance().getCreatureOrNull(wurmid);
                if(creature != null && !creature.isDead()){
                    Server.getInstance().broadCastAction(creature.getName()+" calms down and is no longer enraged.", creature, 50);
                }
                lastLusted.remove(wurmid);
                lusts.remove(wurmid);
            }
        }
    }

    public static void preInit(){
        try{
            ClassPool classPool = HookManager.getInstance().getClassPool();
            final Class<Bloodlust> thisClass = Bloodlust.class;
            String replace;

            Util.setReason("Hook for bloodlust system.");
            CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");
            CtClass ctString = classPool.get("java.lang.String");
            CtClass ctBattle = classPool.get("com.wurmonline.server.combat.Battle");
            CtClass ctCombatEngine = classPool.get("com.wurmonline.server.combat.CombatEngine");
            // @Nullable Creature performer, Creature defender, byte type, int pos, double damage, float armourMod,
            // String attString, @Nullable Battle battle, float infection, float poison, boolean archery, boolean alreadyCalculatedResist
            CtClass[] params1 = {
                    ctCreature,
                    ctCreature,
                    CtClass.byteType,
                    CtClass.intType,
                    CtClass.doubleType,
                    CtClass.floatType,
                    ctString,
                    ctBattle,
                    CtClass.floatType,
                    CtClass.floatType,
                    CtClass.booleanType,
                    CtClass.booleanType
            };
            String desc1 = Descriptor.ofMethod(CtClass.booleanType, params1);
            replace = "if($2.isDominated() && $1 != null && ($1.isUnique() || "+RareSpawns.class.getName()+".isRareCreature($1))){" +
                    //"  logger.info(\"Detected unique hit on a pet. Adding damage.\");" +
                    "  "+Bloodlust.class.getName()+".lustUnique($1);" +
                    "}" +
                    "if($1 != null && ($1.isUnique() || "+RareSpawns.class.getName()+".isRareCreature($1))){" +
                    "  float lustMult = "+Bloodlust.class.getName()+".getLustMult($1);" +
                    "  $5 = $5 * lustMult;" +
                    "}";
            Util.insertBeforeDescribed(thisClass, ctCombatEngine, "addWound", desc1, replace);
        } catch ( NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        }
    }
}
