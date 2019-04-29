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
    //public static int PASTAMANCER = 10002;

    // Player Donation Title ID's
    /*public static int PATRON = 19999;
    public static int DONATOR = 20000;
    public static int PAZZA_FAVORITE_GM = 20001;
    public static int WARRIORGEN_THAT_GUY = 20002;
    public static int ETERNALLOVE_WARRIORGENS_WIFE = 20003;
    public static int BAMBAM_THORN_ONE = 20004;
    public static int SVENJA_CARE_DEPENDANT = 20005;
    public static int ALEXIA_THE_TREASURING = 20006;
    public static int REEVI_SCIENCE_GUY = 20007;
    public static int GENOCIDE_GRAND_DESIGNER = 20008;
    public static int SELEAS_CRAZY_CAT_LORD = 20009;
    public static int PIRATEMAX_SLAVE = 20010;
    public static int ELTACOLAD_TRUE_TACO = 20011;
    public static int ATTICUS_THE_GREAT_ILLUMINATY = 20012;*/

    public static void init(){
        for (WyvernMods.CustomTitle title : WyvernMods.customTitles){
            createTitle(title.getTitleId(), title.getMaleTitle(), title.getFemaleTitle(), title.getSkillId(), title.getType());
        }
        // Event Titles
        createTitle(TITAN_SLAYER, "Titanslayer", "Titanslayer", -1, "NORMAL");
        createTitle(SPECTRAL, "Spectral", "Spectral", -1, "NORMAL");
        //PlayerTitles.createTitle(buster, "Holdstrong_Architect", 702, "Holdstrong Architect", "Holdstrong Architect", -1, Titles.TitleType.NORMAL);
        //PlayerTitles.createTitle(buster, "Stronghold_Architect", 703, "Stronghold Architect", "Stronghold Architect", -1, Titles.TitleType.NORMAL);
        //createTitle(PASTAMANCER, "Pastamancer", "Pastamancer", -1, "NORMAL");

        // Donation Titles
        /* Moved to configuration
        createTitle(PATRON, "Patron", "Patron", -1, "NORMAL");
        createTitle(DONATOR, "Donator", "Donator", -1, "NORMAL");
        createTitle(PAZZA_FAVORITE_GM, "Sindusks Favourite GM", "Sindusks Favourite GM", -1, "NORMAL");
        createTitle(WARRIORGEN_THAT_GUY, "That Guy", "That Guy", -1, "NORMAL");
        createTitle(ETERNALLOVE_WARRIORGENS_WIFE, "Warriorgens Wife", "Warriorgens Wife", -1, "NORMAL");
        createTitle(BAMBAM_THORN_ONE, "Thorn One", "Thorn One", -1, "NORMAL");
        createTitle(SVENJA_CARE_DEPENDANT, "The care-dependent", "The care-dependent", -1, "NORMAL");
        createTitle(ALEXIA_THE_TREASURING, "The Treasuring", "The Treasuring", -1, "NORMAL");
        createTitle(REEVI_SCIENCE_GUY, "Science Guy", "Science Guy", -1, "NORMAL");
        createTitle(GENOCIDE_GRAND_DESIGNER, "Grand Designer", "Grand Designer", -1, "NORMAL");
        createTitle(SELEAS_CRAZY_CAT_LORD, "The Crazy Cat Lord", "The Crazy Cat Lord", -1, "NORMAL");
        createTitle(PIRATEMAX_SLAVE, "Slave", "Slave", -1, "NORMAL");
        createTitle(ELTACOLAD_TRUE_TACO, "The One True Taco", "The One True Taco", -1, "NORMAL");
        createTitle(ATTICUS_THE_GREAT_ILLUMINATY, "The Great Illuminaty", "The Great Illuminaty", -1, "NORMAL");*/

        // Supporter titles
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
        /*if(donatorTitles.contains(name)){
            Titles.Title donator = Titles.Title.getTitle(DONATOR);
            p.addTitle(donator);
        }
        if(patronTitles.contains(name)){
            Titles.Title patron = Titles.Title.getTitle(PATRON);
            p.addTitle(patron);
        }
        if(customTitles.containsKey(name)){
            Titles.Title customTitle = Titles.Title.getTitle(customTitles.get(name));
            p.addTitle(customTitle);
        }*/
    }
    public static void preInit(){
        // Donations
        /*donatorTitles.add("Pazza");
        customTitles.put("Pazza", PAZZA_FAVORITE_GM); // Sindusks Favorite GM

        donatorTitles.add("Warriorgen");
        customTitles.put("Warriorgen", WARRIORGEN_THAT_GUY); // That Guy

        donatorTitles.add("Eternallove");
        customTitles.put("Eternallove", ETERNALLOVE_WARRIORGENS_WIFE); // Warriorgens Wife

        donatorTitles.add("Bambam");
        customTitles.put("Bambam", BAMBAM_THORN_ONE); // Thorn One

        donatorTitles.add("Svenja");
        customTitles.put("Svenja", SVENJA_CARE_DEPENDANT); // The care-dependent
        playerTitles.put("Svenja", "Akuma");

        donatorTitles.add("Alexiaselena");
        customTitles.put("Alexiaselena", ALEXIA_THE_TREASURING); // The Treasuring
        playerTitles.put("Alexiaselena", "Kami");

        donatorTitles.add("Reevi");
        customTitles.put("Reevi", REEVI_SCIENCE_GUY); // Science Guy

        customTitles.put("Genocide", GENOCIDE_GRAND_DESIGNER); // Grand Designer

        donatorTitles.add("Seleas");
        customTitles.put("Seleas", SELEAS_CRAZY_CAT_LORD); // The Crazy Cat Lord
        playerTitles.put("Seleas", "No, Really");

        donatorTitles.add("Piratemax");
        customTitles.put("Piratemax", PIRATEMAX_SLAVE); // Slave
        playerTitles.put("Piratemax", "Boy Next Door");

        donatorTitles.add("Eltacolad");
        customTitles.put("Eltacolad", ELTACOLAD_TRUE_TACO); // The One True Taco

        patronTitles.add("Atticus");
        customTitles.put("Atticus", ATTICUS_THE_GREAT_ILLUMINATY); // The Great Illuminaty

        // Other rewards
        customTitles.put("Critias", PASTAMANCER);*/
    }
}
