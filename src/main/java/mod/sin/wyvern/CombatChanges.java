package mod.sin.wyvern;

import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.shared.constants.Enchants;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.*;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.ArrayList;
import java.util.logging.Logger;

public class CombatChanges {
    public static Logger logger = Logger.getLogger(CombatChanges.class.getName());

    public static float combatRatingAdditive(float combatRating, Creature cret, Creature opponent){
        //logger.info("Checking additive ("+cret.getName()+" vs "+opponent.getName()+"), combatRating = "+combatRating);
        float add = 0.0f;
        if(cret != null && cret.isPlayer() && (opponent != null && !opponent.isPlayer())){
            // 2 CR against non-players for Royal Executioner kingdom title.
            if(WyvernMods.royalExecutionerBonus && cret.isRoyalExecutioner()){
                add += 2.0f;
            }
        }
        return add;
    }
    public static float combatRatingMultiplicative(float combatRating, Creature cret, Creature opponent){
        //logger.info("Checking mult ("+cret.getName()+" vs "+opponent.getName()+"), combatRating = "+combatRating);
        float mult = 1.0f;
        if(cret != null){
            //logger.info("Cret is a pet.");
            if(WyvernMods.petSoulDepthScaling && cret.isDominated() && cret.getDominator() != null) {
                if (cret.getDominator() instanceof Player) {
                    Player owner = (Player) cret.getDominator();
                    double depth = owner.getSoulDepth().getKnowledge();
                    //logger.info("Multiplying combat rating by "+(depth*0.02d)+" due to owner Soul Depth.");
                    mult *= depth * 0.02d;
                } else {
                    logger.info("Somehow a pet is dominated by a non-player? (" + cret.getDominator().getName() + ")");
                }
            }
            if(WyvernMods.vehicleCombatRatingPenalty && QualityOfLife.getVehicleSafe(cret) != null){
                mult *= 0.75f;
            }
        }
        return mult;
    }

    // Added to CombatHandled
    public static int getLifeTransferAmountModifier(Wound wound, int initial){
        byte type = wound.getType();
        if(type == Wound.TYPE_ACID || type == Wound.TYPE_BURN || type == Wound.TYPE_COLD){
            initial *= 0.5;
        }else if(type == Wound.TYPE_INTERNAL || type == Wound.TYPE_INFECTION || type == Wound.TYPE_POISON){
            initial *= 0.3;
        }
        return initial;
    }

    // Added to CombatHandled
    public static float getLifeTransferModifier(Creature creature, Creature defender){
        if(Servers.localServer.PVPSERVER && (defender.isDominated() || defender.isPlayer()) && creature.isPlayer()){
            return 0.5f;
        }
        return 1.0f;
    }

    // Added to CombatHandled
    public static void doLifeTransfer(Creature creature, Creature defender, Item attWeapon, double defdamage, float armourMod){
        float lifeTransfer = attWeapon.getSpellLifeTransferModifier()*getLifeTransferModifier(creature, defender);
        Wound[] w;
        if (lifeTransfer > 0.0f && defdamage * (double)armourMod * (double)lifeTransfer / (double)(creature.isChampion() ? 1000.0f : 500.0f) > 500.0 && creature.getBody() != null && creature.getBody().getWounds() != null && (w = creature.getBody().getWounds().getWounds()).length > 0) {
            int amount = - (int)(defdamage * (double)lifeTransfer / (double)(creature.isChampion() ? 1000.0f : (creature.getCultist() != null && creature.getCultist().healsFaster() ? 250.0f : 500.0f)));
            amount = getLifeTransferAmountModifier(w[0], amount);
            w[0].modifySeverity(amount);
        }
    }

    protected static ArrayList<Creature> uniques = new ArrayList<>();
    public static void pollUniqueCollection(){
        for(Creature cret : Creatures.getInstance().getCreatures()){
            if(cret.isUnique() && !uniques.contains(cret)){
                logger.info("Found unique not in unique list, adding now: "+cret.getName());
                uniques.add(cret);
            }
        }
        int i = 0;
        while(i < uniques.size()){
            if(uniques.get(i).isDead()){
                logger.info("Unique was found dead ("+uniques.get(i).getName()+"). Removing from uniques list.");
                uniques.remove(uniques.get(i));
            }else{
                i++;
            }
        }
    }
    public static void pollUniqueRegeneration(){
        if(!uniques.isEmpty()) {
            for (Creature cret : uniques) {
                if (cret.getBody().isWounded()) {
                    Wounds tWounds = cret.getBody().getWounds();
                    int toHeal = 75;
                    Wound w = tWounds.getWounds()[Server.rand.nextInt(tWounds.getWounds().length)];
                    if (w.getSeverity() > toHeal) {
                        w.modifySeverity(-toHeal);
                        break;
                    } else {
                        w.heal();
                    }
                }
            }
        }
    }

    // Added to CombatHandled
    public static boolean canDoDamage(double damage, Creature attacker, Creature defender) {
        //logger.info(String.format("canDoDamage from %s to %s - %.1f", attacker.getName(), defender.getName(), damage));
        return damage > 1D;
    }

    // Added to CombatHandled
    public static void pollCreatureActionStacks(){
        for(Creature c : Creatures.getInstance().getCreatures()){
            if(c.isFighting()) {
                c.getActions().poll(c);
            }
        }
    }

    public static void goodLog(String str){
        logger.info(str);
    }

    public static void preInit(){
        try{
            ClassPool classPool = HookManager.getInstance().getClassPool();
            final Class<CombatChanges> thisClass = CombatChanges.class;
            String replace;

            CtClass ctCombatHandler = classPool.get("com.wurmonline.server.creatures.CombatHandler");
            CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");
            CtClass ctItem = classPool.get("com.wurmonline.server.items.Item");
            CtClass ctAttackAction = classPool.get("com.wurmonline.server.creatures.AttackAction");

            if (WyvernMods.enableCombatRatingAdjustments) {
                Util.setReason("Make custom combat rating changes.");
                replace = "combatRating += " + CombatChanges.class.getName() + ".combatRatingAdditive(combatRating, this.creature, $1);" +
                        "crmod *= " + CombatChanges.class.getName() + ".combatRatingMultiplicative(combatRating, this.creature, $1);" +
                        "$_ = $proceed($$);";
                Util.instrumentDeclared(thisClass, ctCombatHandler, "getCombatRating", "getFlankingModifier", replace);
            }

            if (WyvernMods.fixMagranonDamageStacking) {
                // Normal combat version
                CtClass[] params2 = {
                        ctCreature,
                        ctItem,
                        ctCreature
                };
                String desc2 = Descriptor.ofMethod(CtClass.doubleType, params2);

                Util.setReason("Fix magranon damage bonus stacking.");
                replace = "if(mildStack){" +
                        "  $_ = $proceed($$) * 8 / 5;" +
                        "}else{" +
                        "  $_ = $proceed($$);" +
                        "}";
                Util.instrumentDescribed(thisClass, ctCombatHandler, "getDamage", desc2, "getModifiedFloatEffect", replace);

                // AttackAction version
                CtClass[] params3 = {
                        ctCreature,
                        ctAttackAction,
                        ctCreature
                };
                String desc3 = Descriptor.ofMethod(CtClass.doubleType, params3);

                Util.setReason("Fix magranon damage bonus stacking.");
                replace = "if(mildStack){" +
                        "  $_ = $proceed($$) * 8 / 5;" +
                        "}else{" +
                        "  $_ = $proceed($$);" +
                        "}";
                Util.instrumentDescribed(thisClass, ctCombatHandler, "getDamage", desc3, "getModifiedFloatEffect", replace);
            }

            if (WyvernMods.adjustCombatRatingSpellPower) {
                Util.setReason("Nerf truehit/excel.");
                replace = "$_ = $proceed($$) * 0.5f;";
                Util.instrumentDeclared(thisClass, ctCombatHandler, "getCombatRating", "getBonusForSpellEffect", replace);
            }

            if (WyvernMods.disableLegendaryRegeneration) {
                Util.setReason("Disable natural regeneration on legendary creatures.");
                CtClass ctWound = classPool.get("com.wurmonline.server.bodys.Wound");
                replace = "if(!this.creature.isUnique()){"
                        + "  $_ = $proceed($$);"
                        + "}";
                Util.instrumentDeclared(thisClass, ctWound, "poll", "modifySeverity", replace);
                Util.setReason("Disable natural regeneration on legendary creatures.");
                Util.instrumentDeclared(thisClass, ctWound, "poll", "checkInfection", replace);
                Util.setReason("Disable natural regeneration on legendary creatures.");
                Util.instrumentDeclared(thisClass, ctWound, "poll", "checkPoison", replace);
            }

            /* Disabled for now until it can be fixed for new Priest Update adjustments.

            Util.setReason("Allow Life Transfer to stack with Rotting Touch (Mechanics-Wise).");
            replace = CombatChanges.class.getName()+".doLifeTransfer(this.creature, defender, attWeapon, defdamage, armourMod);"
                    + "$_ = $proceed($$);";
            Util.instrumentDeclared(thisClass, ctCombatHandler, "setDamage", "isWeaponCrush", replace);

            Util.setReason("Reduce Life Transfer power on PvP.");
            replace = "$_ = $proceed($$)*"+CombatChanges.class.getName()+".getLifeTransferModifier(this.creature, defender);";
            Util.instrumentDeclared(thisClass, ctCombatHandler, "setDamage", "getSpellLifeTransferModifier", replace);

            Util.setReason("Reduce Life Transfer healing amount for certain wounds.");
            replace = "$_ = $proceed("+CombatChanges.class.getName()+".getLifeTransferAmountModifier($0, $1));";
            Util.instrumentDeclared(thisClass, ctCombatHandler, "setDamage", "modifySeverity", replace);*/

            /*Util.setReason("Debug attack method");
            CtClass ctAction = classPool.get("com.wurmonline.server.behaviours.Action");
            CtClass[] params4 = {
                    ctCreature,
                    CtClass.intType,
                    CtClass.booleanType,
                    CtClass.floatType,
                    ctAction
            };
            String desc4 = Descriptor.ofMethod(CtClass.booleanType, params4);
            replace = "logger.info(\"opponent = \"+$1.getName()+\", combatCounter = \"+$2+\", opportunity = \"+$3+\", actionCounter = \"+$4);";
            Util.insertBeforeDescribed(thisClass, ctCombatHandler, "attack", desc4, replace);*/

            /*Util.setReason("Debug CreatureAI Poll");
            CtClass ctCreatureAI = classPool.get("com.wurmonline.server.creatures.ai.CreatureAI");
            replace = "if($1.getTemplate().getTemplateId() == 2147483619){" +
                    CombatChanges.class.getName()+".goodLog(\"CreatureAI.pollCreature(\"+$1.getName()+\", \"+$2+\")\");" +
                    "}";
            Util.insertBeforeDeclared(thisClass, ctCreatureAI, "pollCreature", replace);*/

            /*Util.setReason("Debug VolaTile Poll");
            CtClass ctVolaTile = classPool.get("com.wurmonline.server.zones.VolaTile");
            replace = "if($2.getTemplate().getTemplateId() == 2147483619){" +
                    CombatChanges.class.getName()+".goodLog(\"VolaTile.pollOneCreatureOnThisTile(\"+$2.getName()+\")\");" +
                    "}";
            Util.insertBeforeDeclared(thisClass, ctVolaTile, "pollOneCreatureOnThisTile", replace);*/

            /*Util.setReason("Debug Creature Poll");
            replace = "if($0.getTemplate().getTemplateId() == 2147483619){" +
                    CombatChanges.class.getName()+".goodLog(\"Creature.poll(\"+$0.getName()+\")\");" +
                    "}";
            Util.insertBeforeDeclared(thisClass, ctCreature, "poll", replace);*/

            /*Util.setReason("Debug Creatures Poll");
            CtClass ctCreatures = classPool.get("com.wurmonline.server.creatures.Creatures");
            replace = CombatChanges.class.getName()+".goodLog(\"Creatures.pollAllCreatures()\");";
            Util.insertBeforeDeclared(thisClass, ctCreatures, "pollAllCreatures", replace);*/

            // TODO: Enable with new combat rework.
            /*Util.setReason("Poll creature action stacks on every update.");
            CtClass ctZones = classPool.get("com.wurmonline.server.zones.Zones");
            replace = //CombatChanges.class.getName()+".goodLog(\"Zones.pollNextZones(\"+$1+\") [time \"+java.lang.System.currentTimeMillis()+\"]\");" +
                    CombatChanges.class.getName()+".pollCreatureActionStacks();";
            Util.insertBeforeDeclared(thisClass, ctZones, "pollNextZones", replace);*/

            /*replace = "$_ = $proceed($$);" +
                    CombatChanges.class.getName()+".goodLog(\"Zones.pollNextZones(\"+sleepTime+\") call to Creatures.getInstance().pollAllCreatures(\"+$1+\") [time \"+java.lang.System.currentTimeMillis()+\"]\");";
            Util.instrumentDeclared(thisClass, ctZones, "pollNextZones", "pollAllCreatures", replace);*/

            //patchCombatDamageCheckCombatEngine(classPool);
            //patchCombatDamageCheckCombatHandler(classPool);

        } catch ( NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        }
    }
}
