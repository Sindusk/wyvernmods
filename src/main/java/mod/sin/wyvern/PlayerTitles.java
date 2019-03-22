package mod.sin.wyvern;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.Titles;
import com.wurmonline.server.utils.DbUtilities;
import javassist.CtClass;
import javassist.CtPrimitiveType;
import javassist.bytecode.Descriptor;
import mod.enumbuster.EnumBuster;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerTitles {
    public static Logger logger = Logger.getLogger(PlayerTitles.class.getName());
    private static Titles.Title[] titleArray;

    // Player Title Maps
    protected static ArrayList<String> donatorTitles = new ArrayList<>();
    protected static ArrayList<String> patronTitles = new ArrayList<>();
    protected static HashMap<String,Integer> customTitles = new HashMap<>();
    protected static HashMap<String,String> playerTitles = new HashMap<>();

    // Event Title ID's
    public static int TITAN_SLAYER = 10000;
    public static int SPECTRAL = 10001;
    public static int PASTAMANCER = 10002;

    // Player Donation Title ID's
    public static int PATRON = 19999;
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
    public static int ATTICUS_THE_GREAT_ILLUMINATY = 20012;

    public static void onItemTemplatesCreated(){
        interceptLoadTitles();
        EnumBuster<Titles.Title> buster = new EnumBuster<>(Titles.Title.class, Titles.Title.class);
        // Random titles for fun
        //PlayerTitles.createTitle(buster, "Game_Master", 2500, "Game Master", "Game Master", -1, Titles.TitleType.NORMAL);
        //PlayerTitles.createTitle(buster, "Developer", 2501, "Developer", "Developer", -1, Titles.TitleType.NORMAL);
        //PlayerTitles.createTitle(buster, "Pet_Me", 2502, "Pet Me", "Pet Me", -1, Titles.TitleType.NORMAL);

        // Event Titles
        createTitle(buster, "Titan_Slayer", TITAN_SLAYER, "Titanslayer", "Titanslayer", -1, Titles.TitleType.NORMAL);
        createTitle(buster, "Spectral", SPECTRAL, "Spectral", "Spectral", -1, Titles.TitleType.NORMAL);
        //PlayerTitles.createTitle(buster, "Holdstrong_Architect", 702, "Holdstrong Architect", "Holdstrong Architect", -1, Titles.TitleType.NORMAL);
        //PlayerTitles.createTitle(buster, "Stronghold_Architect", 703, "Stronghold Architect", "Stronghold Architect", -1, Titles.TitleType.NORMAL);
        createTitle(buster, "Pastamancer", PASTAMANCER, "Pastamancer", "Pastamancer", -1, Titles.TitleType.NORMAL);

        // Donation Titles
        createTitle(buster, "Patron", PATRON, "Patron", "Patron", -1, Titles.TitleType.NORMAL);
        createTitle(buster, "Donator", DONATOR, "Donator", "Donator", -1, Titles.TitleType.NORMAL);
        createTitle(buster, "Pazza_FavoriteGM", PAZZA_FAVORITE_GM, "Sindusks Favourite GM", "Sindusks Favourite GM", -1, Titles.TitleType.NORMAL);
        createTitle(buster, "Warriorgen_ThatGuy", WARRIORGEN_THAT_GUY, "That Guy", "That Guy", -1, Titles.TitleType.NORMAL);
        createTitle(buster, "Eternallove_WarriorgensWife", ETERNALLOVE_WARRIORGENS_WIFE, "Warriorgens Wife", "Warriorgens Wife", -1, Titles.TitleType.NORMAL);
        createTitle(buster, "Bambam_ThornOne", BAMBAM_THORN_ONE, "Thorn One", "Thorn One", -1, Titles.TitleType.NORMAL);
        createTitle(buster, "Svenja_CareDependant", SVENJA_CARE_DEPENDANT, "The care-dependent", "The care-dependent", -1, Titles.TitleType.NORMAL);
        createTitle(buster, "Alexia_TheTreasuring", ALEXIA_THE_TREASURING, "The Treasuring", "The Treasuring", -1, Titles.TitleType.NORMAL);
        createTitle(buster, "Reevi_ScienceGuy", REEVI_SCIENCE_GUY, "Science Guy", "Science Guy", -1, Titles.TitleType.NORMAL);
        createTitle(buster, "Genocide_GrandDesigner", GENOCIDE_GRAND_DESIGNER, "Grand Designer", "Grand Designer", -1, Titles.TitleType.NORMAL);
        createTitle(buster, "Seleas_CrazyCatLord", SELEAS_CRAZY_CAT_LORD, "The Crazy Cat Lord", "The Crazy Cat Lord", -1, Titles.TitleType.NORMAL);
        createTitle(buster, "Piratemax_Slave", PIRATEMAX_SLAVE, "Slave", "Slave", -1, Titles.TitleType.NORMAL);
        createTitle(buster, "Eltacolad_TrueTaco", ELTACOLAD_TRUE_TACO, "The One True Taco", "The One True Taco", -1, Titles.TitleType.NORMAL);
        createTitle(buster, "Atticus_The_Great_Illuminaty", ATTICUS_THE_GREAT_ILLUMINATY, "The Great Illuminaty", "The Great Illuminaty", -1, Titles.TitleType.NORMAL);

        // Supporter titles
        logger.info(Arrays.toString(Titles.Title.values()));
        titleArray = Titles.Title.values();
    }

    private static void createTitle(EnumBuster<Titles.Title> buster, String enumName, int id, String titleMale, String titleFemale, int skillId, Titles.TitleType type) {
        Titles.Title testTitle = buster.make(enumName, 0, new Class[]{Integer.TYPE, String.class, String.class, Integer.TYPE, Titles.TitleType.class}, new Object[]{id, titleMale, titleFemale, skillId, type});
        buster.addByValue(testTitle);
        logger.log(Level.INFO, String.format("Created new title with ID #%d: %s [\"%s\", \"%s\"]", id, enumName, titleMale, titleFemale));
    }

    public static boolean hasTitle(Creature c, int titleId) {
        if (c.isPlayer()) {
            Titles.Title[] titles;
            Titles.Title[] arrtitle = titles = ((Player)c).getTitles();
            int n = arrtitle.length;
            int n2 = 0;
            while (n2 < n) {
                Titles.Title title = arrtitle[n2];
                if (title == null) {
                    throw new RuntimeException("We have NULL in titles collection, that is not nice at all!");
                }
                if (title.getTitleId() == titleId) {
                    return true;
                }
                ++n2;
            }
        } else {
            return true;
        }
        return false;
    }

    private static void interceptLoadTitles() {
        String descriptor = Descriptor.ofMethod((CtClass)CtPrimitiveType.voidType, (CtClass[])new CtClass[]{CtClass.longType});
        HookManager.getInstance().registerHook("com.wurmonline.server.players.DbPlayerInfo", "loadTitles", descriptor, new InvocationHandlerFactory(){

            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler(){

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Object result;
                        block8 : {
                            ResultSet rs;
                            Connection dbcon;
                            PreparedStatement ps;
                            result = method.invoke(proxy, args);
                            PlayerInfo pi = (PlayerInfo)proxy;
                            Set<Titles.Title> titles = ReflectionUtil.getPrivateField(pi, ReflectionUtil.getField(pi.getClass(), "titles"));
                            titles.remove(null);
                            dbcon = null;
                            ps = null;
                            rs = null;
                            try {
                                try {
                                    dbcon = DbConnector.getPlayerDbCon();
                                    ps = dbcon.prepareStatement("select TITLEID from TITLES where WURMID=?");
                                    ps.setLong(1, pi.getPlayerId());
                                    rs = ps.executeQuery();
                                    while (rs.next()) {
                                        if (Titles.Title.getTitle(rs.getInt("TITLEID")) != null){
                                            continue;
                                        }
                                        titles.add(PlayerTitles.getTitle(rs.getInt("TITLEID")));
                                    }
                                }
                                catch (SQLException ex) {
                                    logger.log(Level.INFO, "Failed to load titles for  " + pi.getPlayerId(), ex);
                                    DbUtilities.closeDatabaseObjects(ps, rs);
                                    DbConnector.returnConnection(dbcon);
                                    break block8;
                                }
                            }
                            catch (Throwable throwable) {
                                DbUtilities.closeDatabaseObjects(ps, rs);
                                DbConnector.returnConnection(dbcon);
                                throw throwable;
                            }
                            DbUtilities.closeDatabaseObjects(ps, rs);
                            DbConnector.returnConnection(dbcon);
                        }
                        return result;
                    }
                };
            }

        });
    }

    public static Titles.Title getTitle(int titleAsInt) {
        int i = 0;
        while (i < titleArray.length) {
            if (titleAsInt == titleArray[i].getTitleId()) {
                return titleArray[i];
            }
            ++i;
        }
        throw new RuntimeException("Could not find title: " + titleAsInt);
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
        if(donatorTitles.contains(name)){
            Titles.Title donator = getTitle(DONATOR);
            p.addTitle(donator);
        }
        if(patronTitles.contains(name)){
            Titles.Title patron = getTitle(PATRON);
            p.addTitle(patron);
        }
        if(customTitles.containsKey(name)){
            Titles.Title customTitle = getTitle(customTitles.get(name));
            p.addTitle(customTitle);
        }
    }
    public static void preInit(){
        // Donations
        donatorTitles.add("Pazza");
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
        customTitles.put("Critias", PASTAMANCER);
    }
}
