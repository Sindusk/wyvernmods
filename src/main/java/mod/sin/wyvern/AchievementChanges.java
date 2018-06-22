package mod.sin.wyvern;

import com.wurmonline.server.players.Achievement;
import com.wurmonline.server.players.AchievementTemplate;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class AchievementChanges {
    private static Logger logger = Logger.getLogger(AchievementChanges.class.getName());
    public static HashMap<Integer, AchievementTemplate> goodAchievements = new HashMap<>();
    public static ArrayList<Integer> blacklist = new ArrayList<>();

    protected static int getNumber(String name){
        AchievementTemplate temp = Achievement.getTemplate(name);
        if(temp != null){
            return temp.getNumber();
        }
        return -1;
    }

    protected static void blacklist(String name){
        blacklist.add(getNumber(name));
    }
    protected static void blacklist(int number){
        blacklist.add(number);
    }

    private static AchievementTemplate addAchievement(int id, String name, String description, String requirement, boolean isInvisible, int triggerOn, byte achievementType, boolean playUpdateSound, boolean isOneTimer) {
        AchievementTemplate ach = new AchievementTemplate(id, name, isInvisible, triggerOn, achievementType, playUpdateSound, isOneTimer, requirement);
        ach.setDescription(description);
        try {
            ReflectionUtil.callPrivateMethod(null, ReflectionUtil.getMethod(Achievement.class, "addTemplate", new Class<?>[]{AchievementTemplate.class}), ach);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return ach;
    }

    protected static void generateBlacklist(){
        blacklist("Adulterator");
        blacklist("All Hell");
        blacklist("Ambitious");
        blacklist("Angry Sailor");
        blacklist("Arachnophile");
        blacklist("Arch Mage");
        blacklist("Ascended");
        blacklist("Backstabber");
        blacklist("Barbarian");
        blacklist("Braaains");
        blacklist(299); // Brilliant!
        blacklist(364); // Brilliant!
        blacklist("Bumble Bee");
        blacklist("Burglar");
        blacklist("Cap'n");
        blacklist("Caravel sailor");
        blacklist("Chief Mate");
        blacklist("Cog sailor");
        blacklist("Corbita sailor");
        blacklist("Cowboy");
        blacklist("Deforestation");
        blacklist("Demolition");
        blacklist("Diabolist");
        blacklist("Die by the Rift");
        blacklist("Die by the Rift a gazillion times");
        blacklist("Drake Spirits");
        blacklist("Eagle Spirits");
        blacklist("Environmental Hero");
        blacklist("Epic finalizer");
        blacklist(298); // Exquisite Gem
        blacklist(363); // Exquisite Gem
        blacklist("Fast Learner");
        blacklist("Fine titles");
        blacklist("Fo's Favourite");
        blacklist("Ghost of the Rift Warmasters");
        blacklist("Hedgehog");
        blacklist("High Spirits");
        blacklist("Hippie");
        blacklist("Humanss!");
        blacklist("Hunter Apprentice");
        blacklist("Incarnated To Hell");
        blacklist("Investigating the Rift");
        blacklist("Jackal Hunter");
        blacklist("Janitor");
        blacklist("Johnny Appleseed");
        blacklist("Joyrider");
        blacklist("Juggernaut's demise");
        blacklist("Kingdom Assault");
        blacklist("Knarr sailor");
        blacklist("Last Rope");
        blacklist("Lord of War");
        blacklist("Mage");
        blacklist("Magician");
        blacklist("Magus");
        blacklist("Manifested No More");
        blacklist("Master Bridgebuilder");
        blacklist("Master Shipbuilder");
        blacklist("Master Winemaker");
        blacklist("Miner on Strike");
        blacklist("Moby Dick");
        blacklist("Mountain Goat");
        blacklist("Moved a Mountain");
        blacklist("Muffin Maker");
        blacklist("Mussst kill");
        blacklist("No Fuel for the Flame Of Udun");
        blacklist("On the Way to the Moon");
        blacklist("Out At Sea");
        blacklist("Out, out, brief candle!");
        blacklist("Own The Rift");
        blacklist("Pasta maker");
        blacklist("Pasta master");
        blacklist("Peace of Mind");
        blacklist("Pirate");
        blacklist("Pizza maker");
        blacklist("Pizza master");
        blacklist("Planeswalker");
        blacklist("Rider of the Apocalypse");
        blacklist("Rift Beast Nemesis");
        blacklist("Rift Ogre Hero");
        blacklist("Rift Opener");
        blacklist("Rift Specialist");
        blacklist("Rift Surfer");
        blacklist("Rowboat sailor");
        blacklist("Ruler");
        blacklist("Sailboat sailor");
        blacklist("Settlement Assault");
        blacklist("Sisyphos Says Hello");
        blacklist("Shadow");
        blacklist("Shadowmage");
        blacklist("Shutting Down");
        blacklist("Singing While Eating");
        blacklist("Sneaky");
        blacklist("Tastes like Chicken");
        blacklist("Tears of the Unicorn");
        blacklist("The Path of Vynora");
        blacklist("The Smell of Freshly Baked Bread");
        blacklist("The Smell of Freshly Made Muffins");
        blacklist("Thin Air");
        blacklist("Tree Hugger");
        blacklist("Trucker");
        blacklist("Truffle Pig");
        blacklist("Vynora commands you");
        blacklist("Waller");
        blacklist("Wanderer");
        blacklist("Went up a Hill");
        blacklist("Wet Feet");
        blacklist("You Beauty");
        blacklist("You Cannot Pass");
        blacklist("Zombie Hunter");
        blacklist("around the world");
        blacklist("brewed 1000 liters");
        blacklist("brewed liters");
        blacklist("distilled 1000 liters");
        blacklist("distilled liters");
    }

    protected static void setRequirement(AchievementTemplate temp, String req){
        try {
            ReflectionUtil.setPrivateField(temp, ReflectionUtil.getField(temp.getClass(), "requirement"), req);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    protected static void addRequirements(AchievementTemplate temp){
        if(temp.getName().equals("Meoww!")){
            setRequirement(temp, "Kill a Wild Cat");
        }
        if(temp.getName().equals("Treasure Hunter")){
            setRequirement(temp, "Open a treasure chest");
        }
        if(temp.getName().equals("Mercykiller")){
            setRequirement(temp, "Slay a Diseased creature");
        }
        if(temp.getName().equals("Slayer of the Meek")){
            setRequirement(temp, "Slay a Scared creature");
        }
        if(temp.getName().equals("Willbreaker")){
            setRequirement(temp, "Slay a Hardened creature");
        }
        if(temp.getName().equals("Crocodiles Killed")){
            setRequirement(temp, "Kill a Crocodile");
        }
        if(temp.getName().equals("Tower Builder")){
            setRequirement(temp, "Build a Guard Tower");
        }
        if(temp.getName().equals("Invisible:UseResStone")){
            setRequirement(temp, "Use a Resurrection Stone");
        }
        if(temp.getName().equals("Invisible:ShakerOrbing")){
            setRequirement(temp, "Use a Shaker Orb");
        }
        if(temp.getName().equals("Invisible:CutTree")){
            setRequirement(temp, "Cut down a tree");
        }
        if(temp.getName().equals("Invisible:CatchFish")){
            setRequirement(temp, "Catch a fish");
        }
        if(temp.getName().equals("Invisible:PlantFlower")){
            setRequirement(temp, "Plant a flower");
        }
        if(temp.getName().equals("Invisible:Planting")){
            setRequirement(temp, "Plant a tree");
        }
        if(temp.getName().equals("Invisible:PickLock")){
            setRequirement(temp, "Pick a lock");
        }
        if(temp.getName().equals("Invisible:Stealth")){
            setRequirement(temp, "Successfully hide");
        }
        if(temp.getName().equals("Invisible:ItemInTrash")){
            setRequirement(temp, "Trash an item");
        }
        if(temp.getName().equals("Invisible:BulkBinDeposit")){
            setRequirement(temp, "Deposit a bulk item");
        }
        if(temp.getName().equals("Invisible:Distancemoved")){
            setRequirement(temp, "Walk one tile");
        }
        if(temp.getName().equals("Invisible:Hedges")){
            setRequirement(temp, "Plant a hedge or flower bed");
        }
        if(temp.getName().equals("Invisible:MeditatingAction")){
            setRequirement(temp, "Meditate");
        }
        if(temp.getName().equals("Invisible:PickMushroom")){
            setRequirement(temp, "Pick a mushroom");
        }
        if(temp.getName().equals("Maintenance")){
            setRequirement(temp, "Repair a fence, floor, or wall");
        }
        if(temp.getName().equals("Be Gentle Please")){
            setRequirement(temp, "Win a spar");
        }
        if(temp.getName().equals("Rarity")){
            setRequirement(temp, "Make the best quality of an item");
        }
    }

    protected static void fixName(AchievementTemplate temp){
        if(temp.getName().contains("Invisible:")){
            temp.setName(temp.getName().replaceAll("Invisible:", ""));
        }
        if(temp.getName().equals("PlayerkillBow")){
            temp.setName("Arrow To The Knee");
        }
        if(temp.getName().equals("PlayerkillSword")){
            temp.setName("Pointing The Right Direction");
        }
        if(temp.getName().equals("PlayerkillMaul")){
            temp.setName("Trip To The Maul");
        }
        if(temp.getName().equals("PlayerkillAxe")){
            temp.setName("Can I Axe You A Question?");
        }
        if(temp.getName().equals("OuchThatHurt")){
            temp.setName("Ouch That Hurt");
        }
        if(temp.getName().equals("UseResStone")){
            temp.setName("No Risk No Reward");
        }
        if(temp.getName().equals("ShakerOrbing")){
            temp.setName("This Mine Is Busted");
        }
        if(temp.getName().equals("CutTree")){
            temp.setName("Getting Wood");
        }
        if(temp.getName().equals("CatchFish")){
            temp.setName("Delicious Fish");
        }
        if(temp.getName().equals("PlantFlower")){
            temp.setName("Gardening");
        }
        if(temp.getName().equals("Planting")){
            temp.setName("Forester");
        }
        if(temp.getName().equals("PickLock")){
            temp.setName("Vault Hunter");
        }
        if(temp.getName().equals("ItemInTrash")){
            temp.setName("Another Mans Trash");
        }
        if(temp.getName().equals("Stealth")){
            temp.setName("The Invisible Man");
        }
        if(temp.getName().equals("BulkBinDeposit")){
            temp.setName("Bulk Hoarder");
        }
        if(temp.getName().equals("Distancemoved")){
            temp.setName("Explorer");
        }
        if(temp.getName().equals("Hedges")){
            temp.setName("Hedging Your Bets");
        }
        if(temp.getName().equals("MeditatingAction")){
            temp.setName("One With The World");
        }
        if(temp.getName().equals("PickMushroom")){
            temp.setName("Mushroom Collector");
        }
    }

    public static void onServerStarted(){
        try {
            ConcurrentHashMap<Integer, AchievementTemplate> templates = ReflectionUtil.getPrivateField(Achievement.class, ReflectionUtil.getField(Achievement.class, "templates"));
            generateBlacklist();
            for(int i : templates.keySet()){
                AchievementTemplate temp = templates.get(i);
                addRequirements(temp);
                if(!temp.getRequirement().equals("") && !temp.isForCooking() && !blacklist.contains(i)){
                    fixName(temp);
                    goodAchievements.put(i, temp);
                    logger.info(temp.getNumber()+": "+temp.getName()+" - "+temp.getDescription()+" ("+temp.getRequirement()+")");
                }
            }
            logger.info("Total achievements loaded into system: "+goodAchievements.size());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
