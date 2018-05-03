package mod.sin.wyvern;

import com.wurmonline.server.items.Item;
import com.wurmonline.server.villages.GuardPlan;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import mod.sin.items.SealedMap;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.logging.Logger;

public class EconomicChanges {
    public static Logger logger = Logger.getLogger(EconomicChanges.class.getName());

    public static int getNewVillageTiles(int tiles){
        float power = 2f;
        float changeRate = 1000;
        float maxNumTiles = 50000;
        // =(C2) * (1-POW(C2/$C$16, $A$24)) + (SQRT(C2)*$A$26) * POW(C2/$C$16, $A$24)
        return (int) ((float) tiles * (1-Math.pow((float) tiles /maxNumTiles, power)) + (Math.sqrt((float) tiles)*changeRate) * Math.pow((float) tiles /maxNumTiles, power));
    }

    // Used for full refunds of deeds minus guards.
    public static long getNewDisbandMoney(GuardPlan gp, Village v){
        int tiles = v.getDiameterX() * v.getDiameterY();
        long tileCost = (long)tiles * Villages.TILE_COST;
        long perimeterCost = v.getPerimeterSize() * Villages.PERIMETER_COST;
        return gp.moneyLeft + tileCost + perimeterCost;
    }

    private static final float PRICE_MARKUP = 1f/1.4f;
    public static int getNewValue(Item item){
        if(item.getTemplateId() == SealedMap.templateId){
            float qual = item.getQualityLevel();
            float dam = item.getDamage();
            // =($A$25*A2*A2 / 10000)
            float initialValue = ((float)item.getTemplate().getValue())*qual*qual/10000f;
            float baseCost = 100000f;
            float power = 6.0f;
            // =((10+B2/4.5)*(1-POW(A2/100, $A$27)) + B2*POW(A2/100, $A$27)) * ((100 - $A$29) / 100)
            return (int) (((baseCost+(initialValue/4.5f)) * (1f-Math.pow(qual/100f, power)) + initialValue*Math.pow(qual/100f, power)) * ((100f-dam)/100f) * PRICE_MARKUP);
        }
        return -10;
    }

    public static void preInit(){
        try{
            ClassPool classPool = HookManager.getInstance().getClassPool();
            final Class<EconomicChanges> thisClass = EconomicChanges.class;
            String replace;

            Util.setReason("Increase deed upkeep by modifying the amount of tiles it thinks it has.");
            CtClass ctGuardPlan = classPool.get("com.wurmonline.server.villages.GuardPlan");
            replace = "$_ = "+EconomicChanges.class.getName()+".getNewVillageTiles(vill.getNumTiles());";
            Util.instrumentDeclared(thisClass, ctGuardPlan, "getMonthlyCost", "getNumTiles", replace);

            /*Util.setReason("Allow players to get a full deed refund.");
			replace = "{ return "+EconomicChanges.class.getName()+".getNewDisbandMoney(this, this.getVillage()); }";
			Util.setBodyDeclared(thisClass, ctGuardPlan, "getDisbandMoneyLeft", replace);*/

            Util.setReason("Adjust value for certain items.");
            CtClass ctItem = classPool.get("com.wurmonline.server.items.Item");
            replace = "int newVal = "+EconomicChanges.class.getName()+".getNewValue(this);"
                    + "if(newVal > 0){"
                    + "  return newVal;"
                    + "}";
            Util.insertBeforeDeclared(thisClass, ctItem, "getValue", replace);

    } catch ( NotFoundException | IllegalArgumentException | ClassCastException e) {
        throw new HookException(e);
    }
    }
}
