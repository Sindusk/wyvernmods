package mod.sin.wyvern;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.wurmonline.server.Servers;
import com.wurmonline.server.items.CreationEntry;
import com.wurmonline.server.items.CreationMatrix;
import com.wurmonline.server.items.ItemList;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.ModSupportDb;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import org.gotti.wurmunlimited.modsupport.creatures.ModCreatures;
import org.gotti.wurmunlimited.modsupport.vehicles.ModVehicleBehaviours;

import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.SkillTemplate;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import mod.sin.actions.*;
import mod.sin.creatures.*;
import mod.sin.creatures.titans.*;
import mod.sin.wyvern.bestiary.MethodsBestiary;
import mod.sin.wyvern.mastercraft.Mastercraft;

public class WyvernMods
implements WurmServerMod, Configurable, PreInitable, Initable, ItemTemplatesCreatedListener, ServerStartedListener, ServerPollListener, PlayerLoginListener {
	private static Logger logger = Logger.getLogger(WyvernMods.class.getName());
	public static boolean espCounter = false;
	public static boolean enableDepots = false;
	
    boolean bDebug = false;
    
    public static boolean customCommandHandler(ByteBuffer byteBuffer, Player player) throws UnsupportedEncodingException{
    	byte[] tempStringArr = new byte[byteBuffer.get() & 255];
        byteBuffer.get(tempStringArr);
        String message = new String(tempStringArr, "UTF-8");
        tempStringArr = new byte[byteBuffer.get() & 255];
        byteBuffer.get(tempStringArr);
        //String title = new String(tempStringArr, "UTF-8");
        if(player.mayMute() && message.startsWith("!")){
    		logger.info("Player "+player.getName()+" used custom WyvernMods command: "+message);
    		if(message.startsWith("!toggleESP") && player.getPower() >= 5){
                espCounter = !espCounter;
                player.getCommunicator().sendSafeServerMessage("ESP counter for this server is now = "+espCounter);
    		}else if(message.startsWith("!toggleDepots") && player.getPower() >= 5){
                enableDepots = !enableDepots;
                player.getCommunicator().sendSafeServerMessage("Arena depots for this server is now = "+enableDepots);
    		}else{
    			player.getCommunicator().sendSafeServerMessage("Custom command not found: "+message);
    		}
    		return true;
        }
        return false;
    }

    public void configure(Properties properties) {
        this.bDebug = Boolean.parseBoolean(properties.getProperty("debug", Boolean.toString(this.bDebug)));
        try {
            String logsPath = Paths.get("mods", new String[0]) + "/logs/";
            File newDirectory = new File(logsPath);
            if (!newDirectory.exists()) {
                newDirectory.mkdirs();
            }
            FileHandler fh = new FileHandler(String.valueOf(String.valueOf(logsPath)) + this.getClass().getSimpleName() + ".log", 10240000, 200, true);
            if (this.bDebug) {
                fh.setLevel(Level.INFO);
            } else {
                fh.setLevel(Level.WARNING);
            }
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
        }
        catch (IOException ie) {
            System.err.println(String.valueOf(this.getClass().getName()) + ": Unable to add file handler to logger");
        }
        //this.logger.log(Level.INFO, "Property: " + this.somevalue);
        this.Debug("Debugging messages are enabled.");
    }

    private void Debug(String x) {
        if (this.bDebug) {
            System.out.println(String.valueOf(this.getClass().getSimpleName()) + ": " + x);
            System.out.flush();
            logger.log(Level.INFO, x);
        }
    }

    public void preInit() {
    	logger.info("Pre-Initializing.");
        try {
        	ModActions.init();
        	//Bounty.preInit(this);
        	TreasureChests.preInit();
            MiscChanges.preInit();
            Arena.preInit();
            Titans.preInit();
            RareSpawns.preInit();
            PlayerTitles.preInit();
            TeleportHandler.preInit();
            MethodsBestiary.preInit();
            MissionCreator.preInit();
            CombatChanges.preInit();
            MeditationPerks.preInit();
            MountedChanges.preInit();
            EconomicChanges.preInit();
            QualityOfLife.preInit();
            AntiCheat.preInit();
            Mastercraft.preInit();
            Mastercraft.addNewTitles();
            SupplyDepots.preInit();
            
			ClassPool classPool = HookManager.getInstance().getClassPool();

            // - Enable custom command handler - //
			CtClass ctCommunicator = classPool.get("com.wurmonline.server.creatures.Communicator");
        	ctCommunicator.getDeclaredMethod("reallyHandle").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("reallyHandle_CMD_MESSAGE")) {
                        m.replace("java.nio.ByteBuffer tempBuffer = $1.duplicate();"
                        		+ "if(!mod.sin.wyvern.WyvernMods.customCommandHandler($1, this.player)){"
                        		+ "  $_ = $proceed(tempBuffer);"
                        		+ "}");
                        return;
                    }
                }
            });
        } catch (CannotCompileException | NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        }
    }
    
	@Override
	public void init() {
		logger.info("Initializing.");
		ModCreatures.init();
		ModVehicleBehaviours.init();
		
		// Vanilla:
		logger.info("Registering Vanilla creature changes.");
		ModCreatures.addCreature(new Bison());
		
		// Epic:
		logger.info("Registering Epic creatures.");
		ModCreatures.addCreature(new LavaFiend());
		ModCreatures.addCreature(new SolDemon());
		ModCreatures.addCreature(new Worg());
		
		// Wyverns:
        logger.info("Registering Wyverns.");
        ModCreatures.addCreature(new WyvernBlack());
        ModCreatures.addCreature(new WyvernGreen());
        ModCreatures.addCreature(new WyvernRed());
        ModCreatures.addCreature(new WyvernWhite());
        ModCreatures.addCreature(new WyvernBlue());
		
		// Flavor Mobs:
        logger.info("Registering Flavor creatures.");
		ModCreatures.addCreature(new Avenger());
		ModCreatures.addCreature(new ForestSpider());
		ModCreatures.addCreature(new Giant());
        ModCreatures.addCreature(new Charger());
        ModCreatures.addCreature(new HornedPony());
		ModCreatures.addCreature(new LargeBoar());
		ModCreatures.addCreature(new SpiritTroll());

		// Event Mobs:
        logger.info("Registering Event creatures.");
        ModCreatures.addCreature(new IceCat());
        ModCreatures.addCreature(new FireCrab());
        ModCreatures.addCreature(new FireGiant());
		
		// Bosses:
		logger.info("Registering Custom Boss creatures.");
		ModCreatures.addCreature(new Reaper());
		ModCreatures.addCreature(new SpectralDrake());
		// Uniques:
		ModCreatures.addCreature(new Facebreyker());
		
		// Titans:
		ModCreatures.addCreature(new Ifrit());
		ModCreatures.addCreature(new Lilith());
		// Titan Spawns:
		ModCreatures.addCreature(new IfritFiend());
		ModCreatures.addCreature(new IfritSpider());
		ModCreatures.addCreature(new LilithWraith());
		ModCreatures.addCreature(new LilithZombie());
		
		// NPC's
		logger.info("Registering Custom NPC creatures.");
		//ModCreatures.addCreature(new RobZombie());
		//ModCreatures.addCreature(new MacroSlayer());
        ModCreatures.addCreature(new Terror());
		
		Bounty.init();
		
		Mastercraft.changeExistingTitles();
	}

	@Override
	public void onItemTemplatesCreated() {
		logger.info("Creating Item Mod items.");
		ItemMod.createItems();
		logger.info("Creating Cache items.");
		Caches.createItems();
		try {
			logger.info("Editing existing item templates.");
			ItemMod.modifyItems();
			logger.info("Registering permissions hook for custom items.");
			ItemMod.registerPermissionsHook();
		} catch (NoSuchTemplateException | IllegalArgumentException | IllegalAccessException | ClassCastException | NoSuchFieldException e) {
			e.printStackTrace();
		}
	}

	@Override
    public void onPlayerLogin(Player p) {
        Connection dbcon;
        PreparedStatement ps;
        boolean foundLeaderboardOpt = false;
        try {
            dbcon = ModSupportDb.getModSupportDb();
            ps = dbcon.prepareStatement("SELECT * FROM LeaderboardOpt");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (!rs.getString("name").equals(p.getName())) continue;
                foundLeaderboardOpt = true;
            }
            rs.close();
            ps.close();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (!foundLeaderboardOpt) {
            logger.info("No leaderboard entry for "+p.getName()+". Creating one.");
            try {
                dbcon = ModSupportDb.getModSupportDb();
                ps = dbcon.prepareStatement("INSERT INTO LeaderboardOpt (name) VALUES(\"" + p.getName() + "\")");
                ps.executeUpdate();
                ps.close();
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        boolean foundPlayerStats = false;
        try {
            dbcon = ModSupportDb.getModSupportDb();
            ps = dbcon.prepareStatement("SELECT * FROM PlayerStats");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (!rs.getString("NAME").equals(p.getName())) continue;
                foundPlayerStats = true;
            }
            rs.close();
            ps.close();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (!foundPlayerStats) {
            logger.info("No player stats entry for "+p.getName()+". Creating one.");
            try {
                dbcon = ModSupportDb.getModSupportDb();
                ps = dbcon.prepareStatement("INSERT INTO PlayerStats (NAME) VALUES(\"" + p.getName() + "\")");
                ps.executeUpdate();
                ps.close();
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        PlayerTitles.awardCustomTitles(p);
    }

	@Override
	public void onServerStarted() {
		try {
			logger.info("Registering Item Mod creation entries.");
			ItemMod.initCreationEntries();
			logger.info("Registering Item Mod actions.");
			ItemMod.registerActions();
			logger.info("Registering Cache actions.");
			Caches.registerActions();
			logger.info("Registering Soulstealer actions.");
			Soulstealing.registerActions();
			logger.info("Registering Custom actions.");
			ModActions.registerAction(new UnequipAllAction());
			ModActions.registerAction(new ReceiveMailAction());
			ModActions.registerAction(new LeaderboardAction());
			logger.info("Registering Arena actions.");
			ModActions.registerAction(new SorceryCombineAction());
			//ModActions.registerAction(new VillageTeleportAction()); // [3/28/18] Disabled - Highway Portals added instead.
			ModActions.registerAction(new ArenaTeleportAction());
			ModActions.registerAction(new ArenaEscapeAction());
			logger.info("Registering Dev actions.");
			ModActions.registerAction(new MissionAction());
			logger.info("Setting custom creature corpse models.");
			MethodsBestiary.setTemplateVariables();
			/*if(Deities.getDeity(101) != null){ // Edit Breyk player god
				Deity breyk = Deities.getDeity(101);
				// Add some defining affinities
				breyk.repairer = true;
				breyk.learner = true;
				breyk.deathProtector = true;
				breyk.befriendCreature = true;
				// Remove some affinities
				breyk.warrior = false;
				breyk.healer = false;
				breyk.clayAffinity = false;
			}*/
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
			
			//espCounter = Servers.localServer.PVPSERVER; // Enables on PvP server by default.
			//espCounter = false;
			
            SkillTemplate stealing = SkillSystem.templates.get(SkillList.STEALING);
            try {
				ReflectionUtil.setPrivateField(stealing, ReflectionUtil.getField(stealing.getClass(), "tickTime"), 0);
			} catch (IllegalAccessException | NoSuchFieldException e) {
				logger.info("Failed to set tickTime for stealing!");
				e.printStackTrace();
			}
            SkillTemplate meditating = SkillSystem.templates.get(SkillList.MEDITATING);
            try {
                ReflectionUtil.setPrivateField(meditating, ReflectionUtil.getField(meditating.getClass(), "tickTime"), TimeConstants.HOUR_MILLIS);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                logger.info("Failed to set tickTime for meditating!");
                e.printStackTrace();
            }
            meditating.setDifficulty(300f);

			CreationEntry lockpicks = CreationMatrix.getInstance().getCreationEntry(ItemList.lockpick);
			try {
				ReflectionUtil.setPrivateField(lockpicks, ReflectionUtil.getField(lockpicks.getClass(), "hasMinimumSkillRequirement"), false);
				ReflectionUtil.setPrivateField(lockpicks, ReflectionUtil.getField(lockpicks.getClass(), "minimumSkill"), 0.0);
			} catch (IllegalAccessException | NoSuchFieldException e) {
				logger.info("Failed to set lockpick creation entry changes!");
				e.printStackTrace();
			}

			// Set mining difficulty down to be equivalent to digging.
			SkillTemplate mining = SkillSystem.templates.get(SkillList.MINING);
			mining.setDifficulty(3000f);
			// Triple lockpicking skill
            SkillTemplate lockpicking = SkillSystem.templates.get(SkillList.LOCKPICKING);
            lockpicking.setDifficulty(700f);
		} catch (IllegalArgumentException | ClassCastException e) {
			e.printStackTrace();
		}
		try {
			Connection con = ModSupportDb.getModSupportDb();
			String sql;
            if (!ModSupportDb.hasTable(con, "LeaderboardOpt")) {
                sql = "CREATE TABLE LeaderboardOpt (\t\tname\t\t\t\tVARCHAR(30)\t\t\tNOT NULL DEFAULT 'Unknown',\t\tOPTIN\t\t\t\t\tINT\t\tNOT NULL DEFAULT 0)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.execute();
                ps.close();
            }
            if (!ModSupportDb.hasTable(con, "SteamIdMap")) {
                sql = "CREATE TABLE SteamIdMap (\t\tNAME\t\t\t\tVARCHAR(30)\t\t\tNOT NULL DEFAULT 'Unknown',\t\tSTEAMID\t\t\t\t\tLONG\t\tNOT NULL DEFAULT 0)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.execute();
                ps.close();
            }
            if (!ModSupportDb.hasTable(con, "PlayerStats")) {
                sql = "CREATE TABLE PlayerStats (NAME VARCHAR(30) NOT NULL DEFAULT 'Unknown', KILLS INT NOT NULL DEFAULT 0, DEATHS INT NOT NULL DEFAULT 0, DEPOTS INT NOT NULL DEFAULT 0, HOTAS INT NOT NULL DEFAULT 0, TITANS INT NOT NULL DEFAULT 0)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.execute();
                ps.close();
            }
			if (!ModSupportDb.hasTable(con, "ObjectiveTimers")) {
				sql = "CREATE TABLE ObjectiveTimers (\t\tID\t\t\t\tVARCHAR(30)\t\t\tNOT NULL DEFAULT 'Unknown',\t\tTIMER\t\t\t\t\tLONG\t\tNOT NULL DEFAULT 0)";
				PreparedStatement ps = con.prepareStatement(sql);
				ps.execute();
				ps.close();
				try {
					Connection dbcon;
					dbcon = ModSupportDb.getModSupportDb();
					ps = dbcon.prepareStatement("INSERT INTO ObjectiveTimers (ID, TIMER) VALUES(\"DEPOT\", 0)");
					ps.executeUpdate();
					ps.close();
				}
				catch (SQLException e) {
					throw new RuntimeException(e);
				}
				try {
					Connection dbcon;
					dbcon = ModSupportDb.getModSupportDb();
					ps = dbcon.prepareStatement("INSERT INTO ObjectiveTimers (ID, TIMER) VALUES(\"TITAN\", 0)");
					ps.executeUpdate();
					ps.close();
				}
				catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}else{
                SupplyDepots.initializeDepotTimer();
                Titans.initializeTitanTimer();
            }
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static long lastSecondPolled = 0;
	public static long lastPolledTitanSpawn = 0;
	public static final long pollTitanSpawnTime = TimeConstants.MINUTE_MILLIS*2;
	public static long lastPolledTitans = 0;
	public static final long pollTitanTime = TimeConstants.SECOND_MILLIS;
	public static long lastPolledDepots = 0;
	public static final long pollDepotTime = TimeConstants.MINUTE_MILLIS;
	public static long lastPolledRareSpawns = 0;
	public static final long pollRareSpawnTime = TimeConstants.MINUTE_MILLIS*5;
	public static long lastPolledEternalReservoirs = 0;
	public static final long pollEternalReservoirTime = TimeConstants.MINUTE_MILLIS*10;
	public static long lastPolledMissionCreator = 0;
	public static final long pollMissionCreatorTime = TimeConstants.HOUR_MILLIS*4;
	@Override
	public void onServerPoll() {
		if((lastSecondPolled + TimeConstants.SECOND_MILLIS) < System.currentTimeMillis()){
			if(lastPolledDepots + pollDepotTime < System.currentTimeMillis()){
				SupplyDepots.pollDepotSpawn();
				lastPolledDepots += pollDepotTime;
			}
			if(lastPolledTitanSpawn + pollTitanSpawnTime < System.currentTimeMillis()){
				Titans.pollTitanSpawn();
				lastPolledTitanSpawn += pollTitanSpawnTime;
			}
			if(lastPolledTitans + pollTitanTime < System.currentTimeMillis()){
				Titans.pollTitans();
				lastPolledTitans += pollTitanTime;
			}
			if(lastPolledRareSpawns + pollRareSpawnTime < System.currentTimeMillis()){
			    RareSpawns.pollRareSpawns();
			    lastPolledRareSpawns += pollRareSpawnTime;
            }
			if(lastPolledEternalReservoirs + pollEternalReservoirTime < System.currentTimeMillis()){
				Soulstealing.pollSoulForges();
				lastPolledEternalReservoirs += pollEternalReservoirTime;
			}
			if(lastPolledMissionCreator + pollMissionCreatorTime < System.currentTimeMillis()){
				MissionCreator.pollMissions();
				lastPolledMissionCreator += pollMissionCreatorTime;
			}
			
			// Update counter
			if(lastSecondPolled + TimeConstants.SECOND_MILLIS*10 > System.currentTimeMillis()){
				lastSecondPolled += TimeConstants.SECOND_MILLIS;
			}else{
				logger.info("Time between last poll was greater than 10 seconds. Resetting all poll counters...");
				lastSecondPolled = System.currentTimeMillis();
				lastPolledTitanSpawn = System.currentTimeMillis();
				lastPolledTitans = System.currentTimeMillis();
				lastPolledDepots = System.currentTimeMillis();
				lastPolledRareSpawns = System.currentTimeMillis();
				lastPolledEternalReservoirs = System.currentTimeMillis();
				lastPolledMissionCreator = System.currentTimeMillis();
			}
		}
	}

}