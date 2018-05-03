package mod.sin.wyvern;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.logging.Logger;

public class CombatChanges {
    public static Logger logger = Logger.getLogger(CombatChanges.class.getName());

    public static float combatRatingAdditive(float combatRating, Creature cret, Creature opponent){
        //logger.info("Checking additive ("+cret.getName()+" vs "+opponent.getName()+"), combatRating = "+combatRating);
        float add = 0.0f;
        if((cret != null && !cret.isPlayer()) || (opponent != null && !opponent.isPlayer())){
            if(cret.isRoyalExecutioner()){
                add += 2.0f;
            }
        }
        return add;
    }
    public static float combatRatingMultiplicative(float combatRating, Creature cret, Creature opponent){
        //logger.info("Checking mult ("+cret.getName()+" vs "+opponent.getName()+"), combatRating = "+combatRating);
        float mult = 1.0f;
        if(cret != null && cret.isDominated()){
            //logger.info("Cret is a pet.");
            if(cret.getDominator() != null) {
                if (cret.getDominator() instanceof Player) {
                    Player owner = (Player) cret.getDominator();
                    double depth = owner.getSoulDepth().getKnowledge();
                    //logger.info("Multiplying combat rating by "+(depth*0.02d)+" due to owner Soul Depth.");
                    mult *= depth * 0.02d;
                } else {
                    logger.info("Somehow a pet is dominated by a non-player? (" + cret.getDominator().getName() + ")");
                }
            }
        }
        return mult;
    }

    public static void preInit(){
        try{
            ClassPool classPool = HookManager.getInstance().getClassPool();
            final Class<CombatChanges> thisClass = CombatChanges.class;
            String replace;

            Util.setReason("Make custom combat rating changes.");
            CtClass ctCombatHandler = classPool.get("com.wurmonline.server.creatures.CombatHandler");
            replace = "combatRating += "+CombatChanges.class.getName()+".combatRatingAdditive(combatRating, this.creature, $1);" +
                    "crmod *= "+CombatChanges.class.getName()+".combatRatingMultiplicative(combatRating, this.creature, $1);" +
                    "$_ = $proceed($$);";
            Util.instrumentDeclared(thisClass, ctCombatHandler, "getCombatRating", "getFlankingModifier", replace);

            Util.setReason("Increase unique damage to pets.");
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
            replace = "if($2.isDominated() && $1 != null && $1.isUnique()){" +
                    //"  logger.info(\"Detected unique hit on a pet. Adding damage.\");" +
                    "  $5 = $5 * 2d;" +
                    "}";
            Util.insertBeforeDescribed(thisClass, ctCombatEngine, "addWound", desc1, replace);

        } catch ( NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        }
    }
}
