package mod.sin.wyvern;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

import mod.sin.actions.items.EternalReservoirCheckFuelAction;
import mod.sin.actions.items.EternalReservoirRefuelAction;
import mod.sin.actions.items.SoulstealAction;
import mod.sin.items.EternalReservoir;

public class Soulstealing {
	public static final Logger logger = Logger.getLogger(Soulstealing.class.getName());
	public static ArrayList<Item> soulForges = new ArrayList<>();
	public static void pollSoulForge(Item soulForge){
		int tilex = soulForge.getTileX();
		int tiley = soulForge.getTileY();
		int range = (int) (soulForge.getCurrentQualityLevel()/10f);
        int fuel = soulForge.getData1();
		logger.info("Polling eternal reservoir at ("+tilex+", "+tiley+") [range "+range+"] <fuel "+fuel+">");
        if(fuel >= 1){
    		int sx = Zones.safeTileX(tilex - range);
            int sy = Zones.safeTileY(tiley - range);
            int ex = Zones.safeTileX(tilex + range);
            int ey = Zones.safeTileY(tiley + range);
            int x, y;
	        for (x = sx; x <= ex; ++x) {
	            for (y = sy; y <= ey; ++y) {
	                VolaTile t = Zones.getTileOrNull(x, y, soulForge.isOnSurface());
	                if (t == null){
	                	continue;
	                }
	                Creature[] crets2 = t.getCreatures();
	                for (Creature lCret : crets2) {
	                	if(lCret.isBranded() && lCret.isCarnivore()){
	                		int hunger = lCret.getStatus().getHunger();
	                		if(hunger > 10000 && fuel > 50){
		                		logger.info("Detected branded carnivore "+lCret.getName()+" at "+lCret.getTileX()+", "+lCret.getTileY()+" with hunger "+hunger);
	                			lCret.getStatus().modifyHunger(-10000, 1);
	                			Server.getInstance().broadCastAction("The "+lCret.getName()+" is visited by an ethereal creature, and seems less hungry.", lCret, 10);
	                			fuel -= 50;
	                		}
	                	}
	                }
	                Item[] items = t.getItems();
	                for(Item item : items){
	                	if(item.isForgeOrOven()){
	                		if(item.isOnFire()){
		                		if(item.getTemperature() < 20000 && fuel > 15){
			                		logger.info("Found lit container "+item.getName()+" at "+item.getTileX()+", "+item.getTileY()+" with temperature "+item.getTemperature());
		                			item.setTemperature((short) (item.getTemperature()+10000));
		                			Server.getInstance().broadCastMessage("The "+item.getName()+" is visited by an ethereal creature, and is refueled.", item.getTileX(), item.getTileY(), item.isOnSurface(), 10);
		                			fuel -= 15;
		                		}
	                		}
	                	}
	                }
	            }
	        }
	        soulForge.setData1(fuel);
        }else{
        	logger.info("Eternal Reservoir is low on fuel, skipping the poll.");
        }
	}
	public static void pollSoulForges(){
		for(Item item : Items.getAllItems()){
			if(item.getTemplateId() == EternalReservoir.templateId){
				if(!soulForges.contains(item)){
					logger.info("Found eternal reservoir that was not in the list, adding it now...");
					soulForges.add(item);
				} // Need to check for culling after, don't know how
			}
		}
		for(Item soulForge : soulForges){
			pollSoulForge(soulForge);
		}
	}
	public static void registerActions(){
		ModActions.registerAction(new EternalReservoirCheckFuelAction());
		ModActions.registerAction(new EternalReservoirRefuelAction());
		ModActions.registerAction(new SoulstealAction());
	}
}
