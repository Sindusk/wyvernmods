package mod.sin.wyvern;

import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;

import java.util.logging.Logger;

public class DeityChanges {
    public static Logger logger = Logger.getLogger(DeityChanges.class.getName());

    public static void onServerStarted(){
        if(Deities.getDeity(101) != null){ // Edit Thelastdab Player God
            Deity thelastdab = Deities.getDeity(101);
            // Add some defining affinities
            thelastdab.metalAffinity = true;
            thelastdab.deathProtector = true;
            thelastdab.mountainGod = true;
            thelastdab.warrior = true;
            // Remove some affinities
            thelastdab.learner = false;
            thelastdab.repairer = false;
            thelastdab.befriendCreature = false;
            thelastdab.healer = false;
            thelastdab.clayAffinity = false;
            thelastdab.waterGod = false;
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
