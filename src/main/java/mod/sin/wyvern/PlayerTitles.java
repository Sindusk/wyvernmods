package mod.sin.wyvern;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Titles;
import net.bdew.wurm.tools.server.ModTitles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerTitles {
    public static Logger logger = Logger.getLogger(PlayerTitles.class.getName());

    // Player Title Maps
    protected static ArrayList<String> donatorTitles = new ArrayList<>();
    protected static ArrayList<String> patronTitles = new ArrayList<>();
    protected static HashMap<String,Integer> customTitles = new HashMap<>();
    protected static HashMap<String,String> playerTitles = new HashMap<>();

    // Event Title ID's
    public static int TITAN_SLAYER = 10000;
    public static int SPECTRAL = 10001;

    public static void init(){
        for (WyvernMods.CustomTitle title : WyvernMods.customTitles){
            createTitle(title.getTitleId(), title.getMaleTitle(), title.getFemaleTitle(), title.getSkillId(), title.getType());
        }
        // Event Titles
        createTitle(TITAN_SLAYER, "Titanslayer", "Titanslayer", -1, "NORMAL");
        createTitle(SPECTRAL, "Spectral", "Spectral", -1, "NORMAL");

        // Display all existing titles
        logger.info(Arrays.toString(Titles.Title.values()));
    }

    private static void createTitle(int id, String titleMale, String titleFemale, int skillId, String type) {
        ModTitles.addTitle(id, titleMale, titleFemale, skillId, type);
        logger.log(Level.INFO, String.format("Created new title with ID #%d: [\"%s\", \"%s\"]", id, titleMale, titleFemale));
    }

    public static boolean hasCustomTitle(Creature creature){
        if(creature instanceof Player){
            Player p = (Player) creature;
            return playerTitles.containsKey(p.getName());
        }
        return false;
    }
    public static String getCustomTitle(Creature creature){
        if(creature instanceof Player){
            Player p = (Player) creature;
            return " <"+playerTitles.get(p.getName())+">";
        }
        return "";
    }
    public static void awardCustomTitles(Player p){
        String name = p.getName();
        for (int titleId : WyvernMods.awardTitles.keySet()){
            try {
                Titles.Title theTitle = Titles.Title.getTitle(titleId);
                ArrayList<String> playerList = WyvernMods.awardTitles.get(titleId);
                if (playerList.contains(name)){
                    p.addTitle(theTitle);
                }
            }catch(Exception e){
                logger.warning("Failed to get title with ID "+titleId);
            }
        }
    }
}
