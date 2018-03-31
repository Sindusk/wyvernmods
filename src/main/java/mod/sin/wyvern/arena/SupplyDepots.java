package mod.sin.wyvern.arena;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.zones.Zones;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import mod.sin.items.ArenaSupplyDepot;
import mod.sin.items.caches.*;
import mod.sin.wyvern.MiscChanges;
import mod.sin.wyvern.WyvernMods;
import mod.sin.wyvern.util.ItemUtil;

public class SupplyDepots {
	private static Logger logger = Logger.getLogger(SupplyDepots.class.getName());
	public static ArrayList<Item> depots = new ArrayList<Item>();
	public static Creature host = null;
	public static final long depotRespawnTime = TimeConstants.HOUR_MILLIS*7L;
	public static long lastSpawnedDepot = 0;
	public static void sendDepotEffect(Player player, Item depot){
		player.getCommunicator().sendAddEffect(depot.getWurmId(), (byte) 25, depot.getPosX(), depot.getPosY(), depot.getPosZ(), (byte) 0);
	}
	public static void sendDepotEffectsToPlayer(Player player){
		logger.info("Sending depot effects to player "+player.getName());
		for(Item depot : depots){
			sendDepotEffect(player, depot);
		}
	}
	public static void sendDepotEffectsToPlayers(Item depot){
		for(Player p : Players.getInstance().getPlayers()){
			sendDepotEffect(p, depot);
		}
	}
	public static void removeDepotEffect(Item depot){
		for(Player player : Players.getInstance().getPlayers()){
			player.getCommunicator().sendRemoveEffect(depot.getWurmId());
		}
	}
	public static void removeSupplyDepot(Item depot){
		if(depots.contains(depot)){
			depots.remove(depot);
		}
		removeDepotEffect(depot);
	}
	private static boolean isSupplyDepot(Item item){
		return item.getTemplateId() == ArenaSupplyDepot.templateId;
	}
	public static void pollDepotSpawn(){
		if(!Servers.localServer.PVPSERVER && !WyvernMods.enableDepots){
			return;
		}
		for(int i = 0; i < depots.size(); i++){
			Item depot = depots.get(i);
			if(!Items.exists(depot)){
				logger.info("Supply depot was destroyed, removing from list.");
				depots.remove(depot);
				removeDepotEffect(depot);
			}
		}
		for(Item item : Items.getAllItems()){
			if(isSupplyDepot(item) && !depots.contains(item)){
				logger.info("Found existing supply depots, adding to list and sending data to players.");
				depots.add(item);
				sendDepotEffectsToPlayers(item);
			}
		}
		if(depots.isEmpty()){
			if(host == null){
				ArrayList<Creature> uniques = new ArrayList<Creature>();
				for(Creature c : Creatures.getInstance().getCreatures()){
					if(c.isUnique()){
						uniques.add(c);
					}
				}
				if(uniques.size() > 0){
					host = uniques.get(Server.rand.nextInt(uniques.size()));
					MiscChanges.sendImportantMessage(host, "Greetings! I'll be your host, informing you of the next depot to appear over here on the Arena!", 255, 128, 0);
				}
			}
			if(System.currentTimeMillis() > lastSpawnedDepot + depotRespawnTime){
				logger.info("No Depots were found, and the timer has expired. Spawning a new one.");
				boolean spawned = false;
				int i = 0;
				while(!spawned && i < 20){
					float worldSizeX = Zones.worldTileSizeX;
					float worldSizeY = Zones.worldTileSizeY;
					float minX = worldSizeX*0.25f;
					float minY = worldSizeY*0.25f;
					int tilex = (int) (minX+(minX*2*Server.rand.nextFloat()));
					int tiley = (int) (minY+(minY*2*Server.rand.nextFloat()));
					int tile = Server.surfaceMesh.getTile(tilex, tiley);
					try {
						if(Tiles.decodeHeight((int)tile) > 0){
							Item depot = ItemFactory.createItem(ArenaSupplyDepot.templateId, 50+Server.rand.nextFloat()*40f, (float)(tilex << 2) + 2.0f, (float)(tiley << 2) + 2.0f, Server.rand.nextFloat() * 360.0f, true, (byte) 0, -10, null);
							depots.add(depot);
							sendDepotEffectsToPlayers(depot);
							if(host != null){
								MiscChanges.sendImportantMessage(host, "A new depot has appeared on the Arena!", 255, 128, 0);
							}
							logger.info("New supply depot being placed at "+tilex+", "+tiley);
							spawned = true;
							host = null;
							lastSpawnedDepot = System.currentTimeMillis();
						}else{
							logger.info("Position "+tilex+", "+tiley+" was invalid, attempting another spawn...");
							i++;
						}
					} catch (Exception e) {
						logger.severe("Failed to create Arena Depot.");
						e.printStackTrace();
					}
				}
				if(i >= 20){
					logger.warning("Could not find a valid location within 20 tries for a supply depot.");
				}
			}else if(host != null){
				long timeleft = (lastSpawnedDepot + depotRespawnTime) - System.currentTimeMillis();
				long minutesLeft = timeleft/TimeConstants.MINUTE_MILLIS;
				if(minutesLeft > 0){
					if(minutesLeft == 1){
						MiscChanges.sendImportantMessage(host, "Come quickly! The next Arena depot will appear in 5 minutes!", 255, 128, 0);
					}else if(minutesLeft == 19){
						MiscChanges.sendImportantMessage(host, "Best start heading over, the next Arena depot will appear in 20 minutes!", 255, 128, 0);
					}else if(minutesLeft == 59){
						MiscChanges.sendImportantMessage(host, "Heads up! The next Arena depot will appear in 60 minutes!", 255, 128, 0);
					}
				}
			}
		}
	}
	
	public static long lastAttemptedDepotCapture = 0;
	public static final long captureMessageInterval = TimeConstants.MINUTE_MILLIS*3L;
	public static void maybeBroadcastOpen(Creature performer){
		if(System.currentTimeMillis() > lastAttemptedDepotCapture + captureMessageInterval){
			MiscChanges.sendImportantMessage(performer, performer.getName()+" is begnning to capture an Arena depot!", 255, 128, 0);
			lastAttemptedDepotCapture = System.currentTimeMillis();
		}
	}
	public static void giveCacheReward(Creature performer){
		Item inv = performer.getInventory();
		Item enchantOrb = ItemUtil.createEnchantOrb(130f+(Server.rand.nextFloat()*20));
		inv.insertItem(enchantOrb);
		try {
			// Add a special caches as a reward.
			int[] cacheIds = {
					ArmourCache.templateId,
					ArtifactCache.templateId,
					CrystalCache.templateId,
					DragonCache.templateId,
					GemCache.templateId,
					MoonCache.templateId,
					RiftCache.templateId,
					TreasureMapCache.templateId
			};
			int i = 1+Server.rand.nextInt(3); // 2-4 caches extra.
			while(i >= 0){
				Item cache = ItemFactory.createItem(cacheIds[Server.rand.nextInt(cacheIds.length)], 80f+(20f*Server.rand.nextFloat()), "");
				inv.insertItem(cache, true);
				i--;
			}
			if(Server.rand.nextFloat()*100f <= 10f){
				Item hotaStatue = ItemFactory.createItem(ItemList.statueHota, 80f+(20f*Server.rand.nextFloat()), "");
				hotaStatue.setAuxData((byte)Server.rand.nextInt(10));
				hotaStatue.setWeight(50000, true);
				inv.insertItem(hotaStatue, true);
			}
			if(Server.rand.nextFloat()*100f <= 3f){
				Item sorcery = ItemFactory.createItem(ItemUtil.sorceryIds[Server.rand.nextInt(ItemUtil.sorceryIds.length)], 80f+(20f*Server.rand.nextFloat()), "");
				sorcery.setAuxData((byte)2);
				inv.insertItem(sorcery, true);
			}
		} catch (FailedException | NoSuchTemplateException e) {
			e.printStackTrace();
		}
	}
	
	public static void preInit(){
		try{
			ClassPool classPool = HookManager.getInstance().getClassPool();
			
            // - Add light effects for the supply depots, since they are unique - //
			CtClass ctPlayers = classPool.get("com.wurmonline.server.Players");
            ctPlayers.getDeclaredMethod("sendAltarsToPlayer").insertBefore("mod.sin.wyvern.arena.SupplyDepots.sendDepotEffectsToPlayer($1);");
			
		}catch (CannotCompileException | NotFoundException e) {
			throw new HookException((Throwable)e);
        }
	}
}
