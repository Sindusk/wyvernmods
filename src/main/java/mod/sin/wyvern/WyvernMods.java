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

import com.wurmonline.server.Message;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.*;
import mod.sin.actions.items.SorcerySplitAction;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import org.gotti.wurmunlimited.modsupport.creatures.ModCreatures;
import org.gotti.wurmunlimited.modsupport.vehicles.ModVehicleBehaviours;

import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.players.Player;

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
implements WurmServerMod, Configurable, PreInitable, Initable, ItemTemplatesCreatedListener, ServerStartedListener, ServerPollListener, PlayerLoginListener, ChannelMessageListener {
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

    public static void handleExamine(Creature performer, Item target) {
        // Im just not a smart man.
        /*if(target.isContainerLiquid()){
            boolean found = false;
            for(Item i : Items.getAllItems()){
                if(i == target){
                    found = true;
                }
            }
            if(found){
                logger.info("Item exists!");
            }else{
                logger.info("Item not found.");
            }
        }*/
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
            SkillChanges.preInit();
            MeditationPerks.preInit();
            MountedChanges.preInit();
            EconomicChanges.preInit();
            QualityOfLife.preInit();
            Bloodlust.preInit();
            AntiCheat.preInit();
            Mastercraft.preInit();
            //Mastercraft.addNewTitles();
            SupplyDepots.preInit();
            KeyEvent.preInit();
            //GemAugmentation.preInit();

            Class<WyvernMods> thisClass = WyvernMods.class;
			ClassPool classPool = HookManager.getInstance().getClassPool();

            Util.setReason("Insert examine method.");
            CtClass ctItemBehaviour = classPool.get("com.wurmonline.server.behaviours.ItemBehaviour");
            String replace = WyvernMods.class.getName() + ".handleExamine($2, $3);";
            Util.insertAfterDeclared(thisClass, ctItemBehaviour, "examine", replace);

            // - Enable custom command handler - //
			CtClass ctCommunicator = classPool.get("com.wurmonline.server.creatures.Communicator");
        	ctCommunicator.getDeclaredMethod("reallyHandle").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("reallyHandle_CMD_MESSAGE")) {
                        m.replace("java.nio.ByteBuffer tempBuffer = $1.duplicate();"
                        		+ "if(!mod.sin.wyvern.WyvernMods.customCommandHandler($1, this.player)){"
                        		+ "  $_ = $proceed(tempBuffer);"
                        		+ "}");
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
        ModCreatures.addCreature(new FireCrab());
		ModCreatures.addCreature(new ForestSpider());
		ModCreatures.addCreature(new Giant());
        ModCreatures.addCreature(new Charger());
        ModCreatures.addCreature(new HornedPony());
		ModCreatures.addCreature(new LargeBoar());
		ModCreatures.addCreature(new SpiritTroll());

		// Event Mobs:
        logger.info("Registering Event creatures.");
        ModCreatures.addCreature(new IceCat());
        ModCreatures.addCreature(new FireGiant());
        ModCreatures.addCreature(new GuardianMagranon());
		
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
		logger.info("Initiating Title changes.");
		PlayerTitles.onItemTemplatesCreated();
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
        DatabaseHelper.onPlayerLogin(p);
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
			ModActions.registerAction(new AddSubGroupAction());
			ModActions.registerAction(new SorcerySplitAction());
			ModActions.registerAction(new LeaderboardSkillAction());
			logger.info("Registering Arena actions.");
			ModActions.registerAction(new SorceryCombineAction());
			//ModActions.registerAction(new VillageTeleportAction()); // [3/28/18] Disabled - Highway Portals added instead.
			ModActions.registerAction(new ArenaTeleportAction());
			ModActions.registerAction(new ArenaEscapeAction());
			logger.info("Registering Dev actions.");
			ModActions.registerAction(new MissionAddAction());
			ModActions.registerAction(new MissionRemoveAction());
			ModActions.registerAction(new CreatureReportAction());
			ModActions.registerAction(new SmoothTerrainAction());
			logger.info("Setting custom creature corpse models.");
			MethodsBestiary.setTemplateVariables();
			logger.info("Setting up Achievement templates.");
			AchievementChanges.onServerStarted();

			DeityChanges.onServerStarted();
			
			//espCounter = Servers.localServer.PVPSERVER; // Enables on PvP server by default.
			//espCounter = false;

            SkillChanges.onServerStarted();

			CreationEntry lockpicks = CreationMatrix.getInstance().getCreationEntry(ItemList.lockpick);
			try {
				ReflectionUtil.setPrivateField(lockpicks, ReflectionUtil.getField(lockpicks.getClass(), "hasMinimumSkillRequirement"), false);
				ReflectionUtil.setPrivateField(lockpicks, ReflectionUtil.getField(lockpicks.getClass(), "minimumSkill"), 0.0);
			} catch (IllegalAccessException | NoSuchFieldException e) {
				logger.info("Failed to set lockpick creation entry changes!");
				e.printStackTrace();
			}

		} catch (IllegalArgumentException | ClassCastException e) {
			e.printStackTrace();
		}
		DatabaseHelper.onServerStarted();
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
    public static long lastPolledBloodlust = 0;
    public static final long pollBloodlustTime = TimeConstants.MINUTE_MILLIS;
    public static long lastPolledUniqueRegeneration = 0;
    public static final long pollUniqueRegenerationTime = TimeConstants.SECOND_MILLIS;
    public static long lastPolledUniqueCollection = 0;
    public static final long pollUniqueCollectionTime = TimeConstants.MINUTE_MILLIS*5;
    public static long lastPolledTerrainSmooth = 0;
    public static final long pollTerrainSmoothTime = TimeConstants.SECOND_MILLIS*5;
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
            if(lastPolledBloodlust + pollBloodlustTime < System.currentTimeMillis()){
                Bloodlust.pollLusts();
                lastPolledBloodlust += pollBloodlustTime;
            }
            if(lastPolledUniqueRegeneration + pollUniqueRegenerationTime < System.currentTimeMillis()){
                CombatChanges.pollUniqueRegeneration();
                lastPolledUniqueRegeneration += pollUniqueRegenerationTime;
            }
            if(lastPolledUniqueCollection + pollUniqueCollectionTime < System.currentTimeMillis()){
                CombatChanges.pollUniqueCollection();
                lastPolledUniqueCollection += pollUniqueCollectionTime;
            }
            if(lastPolledTerrainSmooth + pollTerrainSmoothTime < System.currentTimeMillis()){
                SmoothTerrainAction.onServerPoll();
                lastPolledTerrainSmooth += pollTerrainSmoothTime;
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
				lastPolledBloodlust = System.currentTimeMillis();
				lastPolledUniqueRegeneration = System.currentTimeMillis();
				lastPolledUniqueCollection = System.currentTimeMillis();
				lastPolledTerrainSmooth = System.currentTimeMillis();
			}
		}
	}

	@Override
	public MessagePolicy onKingdomMessage(Message message) {
		String window = message.getWindow();
		if(window.startsWith("GL-Freedom") && KeyEvent.isActive()){
			KeyEvent.handlePlayerMessage(message);
		}

		return MessagePolicy.PASS;
	}

}