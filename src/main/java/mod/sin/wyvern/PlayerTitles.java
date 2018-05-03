package mod.sin.wyvern;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Titles;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerTitles {
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
        playerTitles.put("Sindusk", "Phenomenal Feline");

        customTitles.put("Sindawn", 501); // Developer
        playerTitles.put("Sindawn", "Pet Me");

        donatorTitles.add("Pazza");
        customTitles.put("Pazza", 801); // Sindusks Favorite GM
        donatorTitles.add("Warriorgen");
        customTitles.put("Warriorgen", 802);
        donatorTitles.add("Eternallove");
        customTitles.put("Eternallove", 803);
        donatorTitles.add("Bambam");
        customTitles.put("Bambam", 804);
        donatorTitles.add("Svenja");
        customTitles.put("Svenja", 805);
        playerTitles.put("Svenja", "Akuma");
    }
}
