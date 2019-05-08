package mod.sin.wyvern;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.shared.constants.BodyPartConstants;
import com.wurmonline.shared.constants.Enchants;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class MountedChanges {
    public static float newCalcHorseShoeBonus(Creature creature){
        float factor = 1.0f;
        ArrayList<Item> gear = new ArrayList<>();
        try {
            Item leftFoot = creature.getEquippedItem(BodyPartConstants.LEFT_FOOT);
            if (leftFoot != null) {
                leftFoot.setDamage(leftFoot.getDamage() + (leftFoot.getDamageModifier() * 0.002f));
                gear.add(leftFoot);
            }
        } catch (NoSpaceException ignored) {
        }
        try {
            Item rightFoot = creature.getEquippedItem(BodyPartConstants.RIGHT_FOOT);
            if (rightFoot != null) {
                rightFoot.setDamage(rightFoot.getDamage() + (rightFoot.getDamageModifier() * 0.002f));
                gear.add(rightFoot);
            }
        } catch (NoSpaceException ignored) {
        }
        try {
            Item leftHand = creature.getEquippedItem(BodyPartConstants.LEFT_HAND);
            if (leftHand != null) {
                leftHand.setDamage(leftHand.getDamage() + (leftHand.getDamageModifier() * 0.002f));
                gear.add(leftHand);
            }
        } catch (NoSpaceException ignored) {
        }
        try {
            Item rightHand = creature.getEquippedItem(BodyPartConstants.RIGHT_HAND);
            if (rightHand != null) {
                rightHand.setDamage(rightHand.getDamage() + (rightHand.getDamageModifier() * 0.002f));
                gear.add(rightHand);
            }
        } catch (NoSpaceException ignored) {
        }
        for(Item shoe : gear){
            factor += Math.max(10f, shoe.getCurrentQualityLevel()) / 2000f;
            factor += shoe.getSpellSpeedBonus() / 2000f;
            factor += shoe.getRarity() * 0.03f;
        }
        return factor;
    }
    public static float newMountSpeedMultiplier(Creature creature, boolean mounting){
        float hunger = creature.getStatus().getHunger()/65535f;
        float damage = creature.getStatus().damage/65535f;
        float factor = ((((1f-damage*damage)*(1f-damage)+(1f-2f*damage)*damage)*(1f-damage)+(1f-damage)*damage)*(1f-0.4f*hunger*hunger));
        try {
            float traitMove = ReflectionUtil.callPrivateMethod(creature, ReflectionUtil.getMethod(creature.getClass(), "getTraitMovePercent"), mounting);
            factor += traitMove;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        if(creature.isHorse() || creature.isUnicorn()) {
            factor *= newCalcHorseShoeBonus(creature);
        }
        /*if(creature.isHorse()){
            try {
                Item barding = creature.getArmour(BodyPartConstants.TORSO);
                if(barding != null){
                    if(barding.getTemplateId() == ItemList.clothBarding){
                        factor *= 0.9f;
                    }else if(barding.getTemplateId() == ItemList.leatherBarding){
                        factor *= 0.82f;
                    }else if(barding.getTemplateId() == ItemList.chainBarding){
                        factor *= 0.75f;
                    }
                }
            } catch (NoArmourException | NoSpaceException ignored) {
            }
        }*/
        if (creature.getBonusForSpellEffect(Enchants.CRET_OAKSHELL) > 0.0f) {
            factor *= 1f - (0.3f * (creature.getBonusForSpellEffect(Enchants.CRET_OAKSHELL) / 100.0f));
        }
        if(creature.isRidden()){
            try {
                float saddleFactor = 1.0f;
                Item saddle = creature.getEquippedItem(BodyPartConstants.TORSO);
                if(saddle != null) {
                    saddle.setDamage(saddle.getDamage()+(saddle.getDamageModifier()*0.001f));
                    saddleFactor += Math.max(10f, saddle.getCurrentQualityLevel()) / 2000f;
                    saddleFactor += saddle.getSpellSpeedBonus() / 2000f;
                    saddleFactor += saddle.getRarity() * 0.03f;
                    factor *= saddleFactor;
                }
            } catch (NoSpaceException ignored) {
            }
            factor *= creature.getMovementScheme().getSpeedModifier();
        }
        return factor;
    }

    public static void preInit(){
        try{
            ClassPool classPool = HookManager.getInstance().getClassPool();
            final Class<MountedChanges> thisClass = MountedChanges.class;
            String replace;

            CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");

            if (WyvernMods.newMountSpeedScaling) {
                Util.setReason("New mount speed scaling.");
                replace = "{ return " + MountedChanges.class.getName() + ".newMountSpeedMultiplier(this, $1); }";
                Util.setBodyDeclared(thisClass, ctCreature, "getMountSpeedPercent", replace);
            }

            if (WyvernMods.updateMountSpeedOnDamage) {
                Util.setReason("Force mount speed change check on damage.");
                replace = "forceMountSpeedChange();";
                Util.insertBeforeDeclared(thisClass, ctCreature, "setWounded", replace);
            }

        } catch ( NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        }

    }
}
