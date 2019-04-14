package mod.sin.wyvern;

import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;

import java.util.logging.Logger;

public class DeityChanges {
    public static Logger logger = Logger.getLogger(DeityChanges.class.getName());

    public static void onServerStarted(){
        if(Deities.getDeity(101) != null){ // Edit Thelastdab Player God
            Deity thelastdab = Deities.getDeity(101);
            // Set template deity
            thelastdab.setTemplateDeity(Deities.DEITY_MAGRANON);
            thelastdab.setMountainGod(true);
            thelastdab.setHateGod(false); // Rolled Libila
            // Add some defining affinities
            thelastdab.setMetalAffinity(true);
            thelastdab.setDeathProtector(true);
            thelastdab.setWarrior(true);
            // Remove some affinities
            thelastdab.setLearner(false);
            thelastdab.setRepairer(false);
            thelastdab.setBefriendCreature(false);
            thelastdab.setHealer(false);
            thelastdab.setClayAffinity(false);
            thelastdab.setWaterGod(false);
        }
        if(Deities.getDeity(102) != null){
            Deity reevi = Deities.getDeity(102);
            // Set template deity
            reevi.setTemplateDeity(Deities.DEITY_MAGRANON);
            reevi.setMountainGod(true);
            reevi.setWaterGod(false); // Rolled Vynora
        }
        /*if(Deities.getDeity(102) != null){ // Edit Cyberhusky player god
            Deity cyberhusky = Deities.getDeity(102);
            // Add some defining affinities
            cyberhusky.hateGod = true;
            cyberhusky.allowsButchering = true;
            cyberhusky.warrior = true;
            // Remove some affinities
            cyberhusky.woodAffinity = false;
            cyberhusky.befriendCreature = false;
        }*/
    }
}
