package mod.sin.wyvern;

import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.Trade;
import com.wurmonline.server.villages.GuardPlan;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
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

    public static long getNewShopDiff(Trade trade, long money, long shopDiff){
        Shop shop = null;
        Village citizenVillage = null;
        if (trade.creatureOne.isNpcTrader()) {
            shop = Economy.getEconomy().getShop(trade.creatureOne);
        }
        if (trade.creatureTwo.isNpcTrader()) {
            shop = Economy.getEconomy().getShop(trade.creatureTwo);
        }
        if(shop == null){
            logger.info("Something went horribly wrong and the shop is null.");
            return 0;
        }
        logger.info("Money = "+money+", shopDiff = "+shopDiff);
        if(!shop.isPersonal() && money > 0){
            logger.info("We're adding money. Testing to see how much difference there is.");
            if(money + shopDiff > 0){
                logger.info("Player actually purchased something. Reducing the income.");
                long newDiff = money + shopDiff;
                logger.info("Actual difference in currency: "+Economy.getEconomy().getChangeFor(newDiff).getChangeString());
                newDiff *= 0.2;
                logger.info("After 80% void: "+Economy.getEconomy().getChangeFor(newDiff).getChangeString());
                logger.info("Returning the following amount of money to incur the change: "+(-shopDiff+newDiff));
                return -shopDiff+newDiff;
            }
            //return (long) (money*0.2);
        }
        return money;
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

            Util.setReason("Disable upkeep on arena for now until a fix can be found.");
            replace = "if(com.wurmonline.server.Servers.localServer.PVPSERVER){"
                    + "  $_ = false;"
                    + "}else{" +
                    "  $_ = $proceed($$);" +
                    "}";
            //replace = "$_ = false;";
            Util.instrumentDeclared(thisClass, ctGuardPlan, "getMonthlyCost", "isUpkeep", replace);

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

            Util.setReason("Remove trader refilling off kings coffers.");
            CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");
            replace = "$_ = 1;";
            Util.instrumentDeclared(thisClass, ctCreature, "removeRandomItems", "nextInt", replace);

            Util.setReason("Void 80% of all currency put into traders.");
            CtClass ctTrade = classPool.get("com.wurmonline.server.items.Trade");
            replace = "$1 = "+EconomicChanges.class.getName()+".getNewShopDiff($0, $1, $0.shopDiff);";
            Util.insertBeforeDeclared(thisClass, ctTrade, "addShopDiff", replace);

        } catch ( NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        }
    }
}
