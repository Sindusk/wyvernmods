package mod.sin.wyvern;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.ItemTemplatesCreatedListener;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.ServerPollListener;
import org.gotti.wurmunlimited.modloader.interfaces.ServerStartedListener;
import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;
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
import mod.sin.wyvern.arena.Arena;
import mod.sin.wyvern.arena.SupplyDepots;
import mod.sin.wyvern.bestiary.MethodsBestiary;
import mod.sin.wyvern.mastercraft.Mastercraft;

public class WyvernMods
implements WurmServerMod, Configurable, PreInitable, Initable, ItemTemplatesCreatedListener, ServerStartedListener, ServerPollListener {
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
            throw new HookException((Throwable)e);
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
		ModCreatures.addCreature(new WyvernBlack());
		ModCreatures.addCreature(new WyvernGreen());
		ModCreatures.addCreature(new WyvernRed());
		ModCreatures.addCreature(new WyvernWhite());
		
		// Flavor Mobs:
		ModCreatures.addCreature(new Avenger());
		ModCreatures.addCreature(new Charger());
		ModCreatures.addCreature(new ForestSpider());
		ModCreatures.addCreature(new Giant());
		ModCreatures.addCreature(new HornedPony());
		ModCreatures.addCreature(new LargeBoar());
		ModCreatures.addCreature(new SpiritTroll());
		
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
		ModCreatures.addCreature(new RobZombie());
		ModCreatures.addCreature(new MacroSlayer());
		
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
			logger.info("Registering Arena actions.");
			//ModActions.registerAction(new VillageTeleportAction()); // [3/28/18] Disabled - Highway Portals added instead.
			ModActions.registerAction(new ArenaTeleportAction());
			ModActions.registerAction(new ArenaEscapeAction());
			logger.info("Setting custom creature corpse models.");
			MethodsBestiary.setTemplateVariables();
			if(Deities.getDeity(101) != null){ // Edit Breyk player god
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
			}
			if(Deities.getDeity(102) != null){ // Edit Cyberhusky player god
				Deity cyberhusky = Deities.getDeity(102);
				// Add some defining affinities
				cyberhusky.hateGod = true;
				cyberhusky.allowsButchering = true;
				cyberhusky.warrior = true;
				// Remove some affinities
				cyberhusky.woodAffinity = false;
				cyberhusky.befriendCreature = false;
			}
			
			//espCounter = Servers.localServer.PVPSERVER; // Enables on PvP server by default.
			//espCounter = false;
			
            SkillTemplate stealing = SkillSystem.templates.get(SkillList.STEALING);
            try {
				ReflectionUtil.setPrivateField(stealing, ReflectionUtil.getField(stealing.getClass(), "tickTime"), 0);
			} catch (IllegalAccessException | NoSuchFieldException e) {
				logger.info("Failed to set tickTime for stealing!");
				e.printStackTrace();
			}
            
		} catch (IllegalArgumentException | ClassCastException e) {
			e.printStackTrace();
		}
	}
	
	public static long lastSecondPolled = 0;
	public static long lastPolledTitanSpawn = 0;
	public static final long pollTitanSpawnTime = TimeConstants.MINUTE_MILLIS*10;
	public static long lastPolledTitans = 0;
	public static final long pollTitanTime = TimeConstants.SECOND_MILLIS;
	public static long lastPolledDepots = 0;
	public static final long pollDepotTime = TimeConstants.MINUTE_MILLIS;
	public static long lastPolledEternalReservoirs = 0;
	public static final long pollEternalReservoirTime = TimeConstants.MINUTE_MILLIS*10;
	@Override
	public void onServerPoll() {
		if((lastSecondPolled + TimeConstants.SECOND_MILLIS) < System.currentTimeMillis()){
			if(lastPolledDepots + pollDepotTime < System.currentTimeMillis()){
				SupplyDepots.pollDepotSpawn();
				lastPolledDepots += pollDepotTime;
			}
			if(lastPolledTitanSpawn + pollTitanSpawnTime < System.currentTimeMillis()){
				Arena.pollTitanSpawn();
				lastPolledTitanSpawn += pollTitanSpawnTime;
			}
			if(lastPolledTitans + pollTitanTime < System.currentTimeMillis()){
				Arena.pollTitans();
				lastPolledTitans += pollTitanTime;
			}
			if(lastPolledEternalReservoirs + pollEternalReservoirTime < System.currentTimeMillis()){
				Soulstealing.pollSoulForges();
				lastPolledEternalReservoirs += pollEternalReservoirTime;
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
				lastPolledEternalReservoirs = System.currentTimeMillis();
			}
		}
	}

}