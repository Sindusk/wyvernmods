package mod.sin.wyvern;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Titles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class PlayerTitles {
    public static Logger logger = Logger.getLogger(PlayerTitles.class.getName());

    protected static ArrayList<String> donatorTitles = new ArrayList<>();
    protected static HashMap<String,Integer> customTitles = new HashMap<>();
    protected static HashMap<String,String> playerTitles = new HashMap<>();

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
        if(donatorTitles.contains(name)){
            Titles.Title donator = Titles.Title.getTitle(800);
            p.addTitle(donator);
        }
        if(customTitles.containsKey(name)){
            Titles.Title customTitle = Titles.Title.getTitle(customTitles.get(name));
            p.addTitle(customTitle);
        }
    }
    public static void preInit(){
        // Donations
        playerTitles.put("Sindusk", "Phenomenal Feline");

        customTitles.put("Sindawn", 501); // Developer
        playerTitles.put("Sindawn", "Pet Me");

        donatorTitles.add("Pazza");
        customTitles.put("Pazza", 801); // Sindusks Favorite GM

        donatorTitles.add("Warriorgen");
        customTitles.put("Warriorgen", 802); // That Guy

        donatorTitles.add("Eternallove");
        customTitles.put("Eternallove", 803); // Warriorgens Wife

        donatorTitles.add("Bambam");
        customTitles.put("Bambam", 804); // Thorn One

        donatorTitles.add("Svenja");
        customTitles.put("Svenja", 805); // The care-dependent
        playerTitles.put("Svenja", "Akuma");

        donatorTitles.add("Alexiaselena");
        customTitles.put("Alexiaselena", 806); // The Treasuring
        playerTitles.put("Alexiaselena", "Kami");

        donatorTitles.add("Reevi");
        customTitles.put("Reevi", 807); // Science Guy

        customTitles.put("Genocide", 808); // Grand Designer

        donatorTitles.add("Seleas");
        customTitles.put("Seleas", 809); // The Crazy Cat Lord
        playerTitles.put("Seleas", "No, Really");

        donatorTitles.add("Piratemax");
        customTitles.put("Piratemax", 810); // Slave
        playerTitles.put("Piratemax", "Boy Next Door");

        donatorTitles.add("Eltacolad");
        customTitles.put("Eltacolad", 811); // The One True Taco

        // Other rewards
        customTitles.put("Critias", 602);
    }
}
