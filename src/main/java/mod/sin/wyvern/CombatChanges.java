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
import javassist.*;
import javassist.bytecode.*;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

public class CombatChanges {
    public static Logger logger = Logger.getLogger(CombatChanges.class.getName());

    // Added to CombatHandled
    public static int getWeaponType(Item weapon){
        if(weapon.enchantment == Enchants.ACID_DAM){
            return Wound.TYPE_ACID;
        }else if(weapon.enchantment == Enchants.FROST_DAM){
            return Wound.TYPE_COLD;
        }else if(weapon.enchantment == Enchants.FIRE_DAM){
            return Wound.TYPE_BURN;
        }
        return -1;
    }

    public static float combatRatingAdditive(float combatRating, Creature cret, Creature opponent){
        //logger.info("Checking additive ("+cret.getName()+" vs "+opponent.getName()+"), combatRating = "+combatRating);
        float add = 0.0f;
        if(cret != null && cret.isPlayer() && (opponent != null && !opponent.isPlayer())){
            if(cret.isRoyalExecutioner()){
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
            if(cret.isDominated() && cret.getDominator() != null) {
                if (cret.getDominator() instanceof Player) {
                    Player owner = (Player) cret.getDominator();
                    double depth = owner.getSoulDepth().getKnowledge();
                    //logger.info("Multiplying combat rating by "+(depth*0.02d)+" due to owner Soul Depth.");
                    mult *= depth * 0.02d;
                } else {
                    logger.info("Somehow a pet is dominated by a non-player? (" + cret.getDominator().getName() + ")");
                }
            }
            if(QualityOfLife.getVehicleSafe(cret) != null){
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

    // Added (kind of) to CombatHandled
    public static float getAdjustedOakshell(Creature defender, Item armour, float armourMod){
        if(defender != null && armour == null){
            float oakshellPower = defender.getBonusForSpellEffect(Enchants.CRET_OAKSHELL);
            if(oakshellPower > 0f){
                return (float) (1-(0.8f*Math.pow((oakshellPower/(oakshellPower+80)), 0.5d)));
            }
        }
        return armourMod; // Returns previous armourMod if the target has armour.
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

    static void patchCombatDamageCheckCombatEngine(ClassPool classPool) throws NotFoundException, BadBytecode {
        CtClass cls = classPool.getCtClass("com.wurmonline.server.combat.CombatEngine");
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
        CtMethod method = cls.getMethod("addWound", desc1);
        //CtMethod method = cls.getMethod("setDamage", "(Lcom/wurmonline/server/creatures/Creature;Lcom/wurmonline/server/items/Item;DBB)Z");
        MethodInfo methodInfo = method.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        ConstPool constPool = codeAttribute.getConstPool();
        CodeIterator codeIterator = codeAttribute.iterator();

        // Scan through all the bytecode - look for a multiplication followed by comparing
        while (codeIterator.hasNext()) {
            int pos = codeIterator.next();
            int op = codeIterator.byteAt(pos);
            if (op != CodeIterator.DMUL) continue; // not multiplication - continue
            op = codeIterator.byteAt(++pos);
            if (op == CodeIterator.LDC2_W && codeIterator.byteAt(pos + 3) == CodeIterator.DCMPL) {
                // found the pattern, check the value it's comparing to
                int ref = codeIterator.u16bitAt(pos + 1);
                double val = constPool.getDoubleInfo(ref);
                if (val == 500.0) {
                    // here it is, generate new code to insert
                    // We'll be calling canDoDamage, the first parameter (damage) is already on the stack, prepare the rest
                    Bytecode newCode = new Bytecode(constPool);

                    newCode.add(Bytecode.ALOAD_0); // performer - first parameter of addWound
                    newCode.add(Bytecode.ALOAD_1); // defender - first parameter of addWound

                    // call our methor, result is left on the stack
                    newCode.addInvokestatic(
                            CombatChanges.class.getName(), "canDoDamage",
                            "(DLcom/wurmonline/server/creatures/Creature;Lcom/wurmonline/server/creatures/Creature;)Z");

                    // The code we're replacing is 4 bytes - LDC2_W, 2byte reference and DCMPL
                    // Insert a gap for to match the size of the new code
                    codeIterator.insertGap(pos, newCode.getSize() - 4);

                    // And put the new code
                    codeIterator.write(newCode.get(), pos);
                }
            }
        }
    }

    static void patchCombatDamageCheckCombatHandler(ClassPool classPool) throws NotFoundException, BadBytecode {
        CtClass cls = classPool.getCtClass("com.wurmonline.server.creatures.CombatHandler");
        CtMethod method = cls.getMethod("setDamage", "(Lcom/wurmonline/server/creatures/Creature;Lcom/wurmonline/server/items/Item;DBB)Z");
        MethodInfo methodInfo = method.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        ConstPool constPool = codeAttribute.getConstPool();
        CodeIterator codeIterator = codeAttribute.iterator();

        // Scan through all the bytecode - look for a multiplication followed by comparing
        while (codeIterator.hasNext()) {
            int pos = codeIterator.next();
            int op = codeIterator.byteAt(pos);
            if (op != CodeIterator.DMUL) continue; // not multiplication - continue
            op = codeIterator.byteAt(++pos);
            if (op == CodeIterator.LDC2_W && codeIterator.byteAt(pos + 3) == CodeIterator.DCMPL) {
                // found the pattern, check the value it's comparing to
                int ref = codeIterator.u16bitAt(pos + 1);
                double val = constPool.getDoubleInfo(ref);
                if (val == 500.0) {
                    // here it is, generate new code to insert
                    // We'll be calling canDoDamage, the first parameter (damage) is already on the stack, prepare the rest
                    Bytecode newCode = new Bytecode(constPool);
                    newCode.add(Bytecode.ALOAD_0); // this
                    newCode.addGetfield(cls, "creature", "Lcom/wurmonline/server/creatures/Creature;"); // this.creature
                    newCode.add(Bytecode.ALOAD_1); // defender - first parameter of setDamage

                    // call our methor, result is left on the stack
                    newCode.addInvokestatic(
                            CombatChanges.class.getName(), "canDoDamage",
                            "(DLcom/wurmonline/server/creatures/Creature;Lcom/wurmonline/server/creatures/Creature;)Z");

                    // The code we're replacing is 4 bytes - LDC2_W, 2byte reference and DCMPL
                    // Insert a gap for to match the size of the new code
                    codeIterator.insertGap(pos, newCode.getSize() - 4);

                    // And put the new code
                    codeIterator.write(newCode.get(), pos);
                }
            }
        }
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
                    "}" +
                    "if($2.isUnique() && $1 != null && $1.isDominated()){" +
                    "  logger.info(\"Detected pet hit on a unique. Reducing damage.\");" +
                    "  $5 = $5 * 0.5d;" +
                    "}";
            Util.insertBeforeDescribed(thisClass, ctCombatEngine, "addWound", desc1, replace);

            Util.setReason("Adjust weapon damage type based on the potion/salve applied.");
            replace = "int wt = "+CombatChanges.class.getName()+".getWeaponType($1);"
                    + "if(wt != -1){"
                    + "  type = wt;"
                    + "  return wt;"
                    + "}";
            Util.insertBeforeDeclared(thisClass, ctCombatHandler, "getType", replace);

            Util.setReason("Adjust bloodthirst to epic settings.");
            CtClass ctItem = classPool.get("com.wurmonline.server.items.Item");
            CtClass[] params2 = {
                    ctCreature,
                    ctItem,
                    ctCreature
            };
            String desc2 = Descriptor.ofMethod(CtClass.doubleType, params2);
            replace = "$_ = true;";
            Util.instrumentDescribed(thisClass, ctCombatHandler, "getDamage", desc2, "isThisAnEpicOrChallengeServer", replace);

            Util.setReason("Fix magranon damage bonus stacking.");
            replace = "if(mildStack){" +
                    "  $_ = $proceed($$) * 8 / 5;" +
                    "}else{" +
                    "  $_ = $proceed($$);" +
                    "}";
            Util.instrumentDescribed(thisClass, ctCombatHandler, "getDamage", desc2, "getModifiedFloatEffect", replace);

            Util.setReason("Adjust bloodthirst to epic settings.");
            CtClass ctAttackAction = classPool.get("com.wurmonline.server.creatures.AttackAction");
            CtClass[] params3 = {
                    ctCreature,
                    ctAttackAction,
                    ctCreature
            };
            String desc3 = Descriptor.ofMethod(CtClass.doubleType, params3);
            replace = "$_ = true;";
            Util.instrumentDescribed(thisClass, ctCombatHandler, "getDamage", desc3, "isThisAnEpicOrChallengeServer", replace);

            Util.setReason("Fix magranon damage bonus stacking.");
            replace = "if(mildStack){" +
                    "  $_ = $proceed($$) * 8 / 5;" +
                    "}else{" +
                    "  $_ = $proceed($$);" +
                    "}";
            Util.instrumentDescribed(thisClass, ctCombatHandler, "getDamage", desc3, "getModifiedFloatEffect", replace);

            Util.setReason("Nerf truehit/excel.");
            replace = "$_ = $proceed($$) * 0.5f;";
            Util.instrumentDeclared(thisClass, ctCombatHandler, "getCombatRating", "getBonusForSpellEffect", replace);

            Util.setReason("Nerf oakshell.");
            replace = "if(defender.isPlayer() && defender.getBonusForSpellEffect((byte)22) > 0f){" +
                    "  armourMod = "+CombatChanges.class.getName()+".getAdjustedOakshell(defender, armour, armourMod);" +
                    "}" +
                    "$_ = $proceed($$);";
            Util.instrumentDeclared(thisClass, ctCombatHandler, "setDamage", "getDamReductionBonusFor", replace);

            Util.setReason("Disable natural regeneration on uniques.");
            CtClass ctWound = classPool.get("com.wurmonline.server.bodys.Wound");
            replace = "if(!this.creature.isUnique()){"
                    + "  $_ = $proceed($$);"
                    + "}";
            Util.instrumentDeclared(thisClass, ctWound, "poll", "modifySeverity", replace);
            Util.instrumentDeclared(thisClass, ctWound, "poll", "checkInfection", replace);
            Util.instrumentDeclared(thisClass, ctWound, "poll", "checkPoison", replace);

            Util.setReason("Allow Life Transfer to stack with Rotting Touch (Mechanics-Wise).");
            replace = CombatChanges.class.getName()+".doLifeTransfer(this.creature, defender, attWeapon, defdamage, armourMod);"
                    + "$_ = $proceed($$);";
            Util.instrumentDeclared(thisClass, ctCombatHandler, "setDamage", "isWeaponCrush", replace);

            Util.setReason("Reduce Life Transfer power on PvP.");
            replace = "$_ = $proceed($$)*"+CombatChanges.class.getName()+".getLifeTransferModifier(this.creature, defender);";
            Util.instrumentDeclared(thisClass, ctCombatHandler, "setDamage", "getSpellLifeTransferModifier", replace);

            Util.setReason("Reduce Life Transfer healing amount for certain wounds.");
            replace = "$_ = $proceed("+CombatChanges.class.getName()+".getLifeTransferAmountModifier($0, $1));";
            Util.instrumentDeclared(thisClass, ctCombatHandler, "setDamage", "modifySeverity", replace);

            Util.setReason("Make AOSP not disgustingly broken.");
            replace = "if(!this.creature.isPlayer()){" +
                    "  $_ = $proceed($$)*this.creature.addSpellResistance((short) 278);" +
                    "}else{" +
                    "  $_ = $proceed($$);" +
                    "}";
            Util.instrumentDeclared(thisClass, ctCombatHandler, "setDamage", "getSpellPainShare", replace);

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

            patchCombatDamageCheckCombatEngine(classPool);
            patchCombatDamageCheckCombatHandler(classPool);

        } catch ( NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        } catch (BadBytecode badBytecode) {
            badBytecode.printStackTrace();
        }
    }
}
