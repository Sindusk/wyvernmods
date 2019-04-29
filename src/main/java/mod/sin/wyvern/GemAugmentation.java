package mod.sin.wyvern;

import com.wurmonline.server.items.Item;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.logging.Logger;

public class GemAugmentation {
    public static Logger logger = Logger.getLogger(GemAugmentation.class.getName());

    public static boolean setGemmedQuality(Item target, double power, float maxGain, float modifier){
        logger.info(String.format("Item %s [QL %.2f] improved with power %.1f, max gain %.1f, modifier %.1f",
                target.getName(), target.getQualityLevel(), power, maxGain, modifier));
        return target.setQualityLevel(Math.min(999.9f, (float)((double)target.getQualityLevel() + power * (double)maxGain * (double) (modifier * target.getMaterialImpBonus()))));
    }

    public static void preInit(){
        try {
            ClassPool classPool = HookManager.getInstance().getClassPool();
            Class<GemAugmentation> thisClass = GemAugmentation.class;
            String replace;

            Util.setReason("Disable Gem Augmentation skill from converting.");
            CtClass ctMethodsReligion = classPool.get("com.wurmonline.server.behaviours.MethodsReligion");
            replace = "$_ = $proceed($1, $2, true, $4);";
            Util.instrumentDeclared(thisClass, ctMethodsReligion, "listen", "skillCheck", replace);

            Util.setReason("Primary Gem Augmentation Hook.");
            CtClass ctMethodsItems = classPool.get("com.wurmonline.server.behaviours.MethodsItems");
            replace = "$_ = "+GemAugmentation.class.getName()+".setGemmedQuality($0, power, maxGain, modifier);";
            Util.instrumentDeclared(thisClass, ctMethodsItems, "improveItem", "setQualityLevel", replace);
            Util.instrumentDeclared(thisClass, ctMethodsItems, "polishItem", "setQualityLevel", replace);
            Util.instrumentDeclared(thisClass, ctMethodsItems, "temper", "setQualityLevel", replace);

            Util.setReason("Prevent action power from being diluted.");
            replace = "$_ = $proceed((float)power);";
            Util.instrumentDeclared(thisClass, ctMethodsItems, "improveItem", "setPower", replace);
            Util.instrumentDeclared(thisClass, ctMethodsItems, "polishItem", "setPower", replace);
            Util.instrumentDeclared(thisClass, ctMethodsItems, "temper", "setPower", replace);

            CtClass ctDbItem = classPool.get("com.wurmonline.server.items.DbItem");
            replace = //"logger.info(\"qlevel = \"+qlevel);" +
                    "$_ = $proceed(9999.9f, $2);";
            Util.instrumentDeclared(thisClass, ctDbItem, "setQualityLevel", "min", replace);

        }catch (NotFoundException e) {
            throw new HookException(e);
        }
    }
}
