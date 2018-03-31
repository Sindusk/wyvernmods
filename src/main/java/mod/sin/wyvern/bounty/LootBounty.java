package mod.sin.wyvern.bounty;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import mod.sin.armour.SpectralHide;
import mod.sin.creatures.Reaper;
import mod.sin.creatures.SpectralDrake;
import mod.sin.items.ChaosCrystal;
import mod.sin.items.EnchantersCrystal;
import mod.sin.items.FriyanTablet;
import mod.sin.wyvern.Bounty;
import mod.sin.wyvern.MiscChanges;
import mod.sin.wyvern.arena.Arena;
import mod.sin.wyvern.util.ItemUtil;

public class LootBounty {
	public static final Logger logger = Logger.getLogger(LootBounty.class.getName());
	protected static final Random random = new Random();
	
	public static void displayLootAssistance(Creature mob){
		if(Bounty.dealtDamage.containsKey(mob.getWurmId())){
			logger.info("Found the damageDealt entry, parsing...");
    		ArrayList<String> names = new ArrayList<String>();
    		ArrayList<Double> damages = new ArrayList<Double>();
    		for(long creatureId : Bounty.dealtDamage.get(mob.getWurmId()).keySet()){
    			if(Players.getInstance().getPlayerOrNull(creatureId) != null){
    				names.add(Players.getInstance().getPlayerOrNull(creatureId).getName());
    				damages.add(Bounty.dealtDamage.get(mob.getWurmId()).get(creatureId));
    			}else{
    				if(Creatures.getInstance().getCreatureOrNull(creatureId) != null){
    					logger.info("Skipping creature "+Creatures.getInstance().getCreatureOrNull(creatureId).getName()+" in loot assistance.");
    				}
    			}
    		}
    		logger.info("Names have been added: "+names);
    		String strBuilder = "Loot Assistance <Damagers> ("+mob.getName()+"): ";
    		DecimalFormat formatter = new DecimalFormat("#,###,###");
    		while(names.size() > 0){
    			int index = Server.rand.nextInt(names.size());
    			strBuilder += names.get(index);
    			strBuilder += " ["+formatter.format(Math.round(damages.get(index)))+"]";
    			names.remove(index);
    			damages.remove(index);
    			if(names.size() > 0){
    				strBuilder += ", ";
    			}
    		}
    		MiscChanges.sendServerTabMessage(strBuilder, 0, 128, 255);
    		logger.info("Broadcast loot assistance message success [Damage].");
		}else{
			logger.warning("Powerful creature "+mob.getName()+" died, but no players were credited to its death [Damage].");
		}
	}
	
	public static int doRollingCrystalReward(Creature mob, Item corpse, double cretStr, int templateId, int chance, double reductionPerRoll){
		try {
			double rollingCounter = cretStr;
	    	int addedCrystals = 0;
	    	/*if(mob.isUnique()){ // Uniques will drop 3x as many, and have special properties to enable dropping rare and possibly supreme versions as well.
	    		rollingCounter *= 3;
	    	}else if(Servers.localServer.PVPSERVER){ // Arena gives double the amount of crystals.
	    		rollingCounter *= 2;
	    	}*/
	    	while(rollingCounter > 0){
	    		if(random.nextInt(chance+addedCrystals) == 0){ // Give a chance at a crystal, decreasing with the amount of crystals contained.
	    			// The crystal quality is the cube root of the rolling counter, capped at 100 of course
	    			Item chaosCrystal = ItemFactory.createItem(templateId, (float) (random.nextFloat()*Math.min(100, Math.cbrt(rollingCounter))), "");
	    			if(random.nextInt(40) == 0){
	    				chaosCrystal.setRarity((byte) 1);
	    			}else if(mob.isUnique() && random.nextInt(5) == 0){
	    				if(random.nextInt(5) == 0){
	    					chaosCrystal.setRarity((byte) 2);
	    				}else{
	    					chaosCrystal.setRarity((byte) 1);
	    				}
	    			}
	    			corpse.insertItem(chaosCrystal);
	    			addedCrystals++;
	    		}
	    		rollingCounter -= reductionPerRoll;
	    	}
	    	return addedCrystals;
		} catch (FailedException | NoSuchTemplateException e) {
			e.printStackTrace();
		}
    	return 0;
	}
	
	public static void blessWorldWithMoonVeins(Creature mob){
		int i = 20;
		while(i > 0){
			int x = random.nextInt(Server.surfaceMesh.getSize());
			int y = random.nextInt(Server.surfaceMesh.getSize());
			short height = Tiles.decodeHeight(Server.surfaceMesh.getTile(x, y));
			int type = Tiles.decodeType((int)Server.caveMesh.getTile(x, y));
			if(height >= 100 && (type == Tiles.Tile.TILE_CAVE_WALL.id || type == Tiles.Tile.TILE_CAVE.id)){
				Tiles.Tile tileType = random.nextBoolean() ? Tiles.Tile.TILE_CAVE_WALL_ORE_ADAMANTINE : Tiles.Tile.TILE_CAVE_WALL_ORE_GLIMMERSTEEL;
				Server.caveMesh.setTile(x, y, Tiles.encode(Tiles.decodeHeight(Server.caveMesh.getTile(x, y)), tileType.id, Tiles.decodeData(Server.caveMesh.getTile(x, y))));
				Players.getInstance().sendChangedTile(x, y, false, true);
				Server.setCaveResource(x, y, 400+random.nextInt(600));
				Village v = Villages.getVillage(x, y, true);
		        if (v == null) {
		            for (int vx = -50; vx < 50; vx += 5) {
		                for (int vy = -50; vy < 50 && (v = Villages.getVillage(x + vx, y + vy, true)) == null; vy += 5) {
		                }
		            }
		        }
		        if(v != null){
		        	HistoryManager.addHistory(mob.getTemplate().getName(), "blesses the world with a "+tileType.getName()+" near "+v.getName()+"!");
		        }
				logger.info("Placed a "+tileType.getName()+" at "+x+", "+y+" - "+height+" height");
				i--;
			}
		}
		Server.getInstance().broadCastAlert("The death of the "+mob.getTemplate().getName()+" has blessed the world with valuable ores!");
	}
	
	public static void spawnFriyanTablets(){
		int i = 5+random.nextInt(5);
		while(i > 0){
			int x = random.nextInt(Server.surfaceMesh.getSize());
			int y = random.nextInt(Server.surfaceMesh.getSize());
			short height = Tiles.decodeHeight(Server.surfaceMesh.getTile(x, y));
			if(height > 0 && height < 1000 && Creature.getTileSteepness(x, y, true)[1] < 30){
				try {
					ItemFactory.createItem(FriyanTablet.templateId, 80f+random.nextInt(20), (float)x*4, (float)y*4, random.nextFloat()*360f, true, (byte)0, -10, "Friyanouce");
					logger.info("Created a Tablet of Friyan at "+x+", "+y+".");
				} catch (NoSuchTemplateException | FailedException e) {
					e.printStackTrace();
				}
				i--;
			}
		}
	}
	
	public static void handleDragonLoot(Creature mob, Item corpse){
		try{
			int mTemplate = mob.getTemplate().getTemplateId();
			int lootTemplate = ItemList.drakeHide;
			byte ctype = 0;
			if(mTemplate == CreatureTemplateFactory.DRAGON_BLACK_CID || mTemplate == CreatureTemplateFactory.DRAGON_BLUE_CID || mTemplate == CreatureTemplateFactory.DRAGON_GREEN_CID
					|| mTemplate == CreatureTemplateFactory.DRAGON_RED_CID || mTemplate == CreatureTemplateFactory.DRAGON_WHITE_CID){
			//if(mTemplate == 16 || mTemplate == 89 || mTemplate == 91 || mTemplate == 90 || mTemplate == 92){
				ctype = 99; // Champion creature type
				lootTemplate = ItemList.dragonScale;
				//lootTemplate = 372;
			}else{
				ctype = (byte)Math.max(0, Server.rand.nextInt(17) - 5);
			}
			
			float x = mob.getPosX();
			float y = mob.getPosY();
			
			// Spawn the spectral drake.
			//logger.info("Spawning a spectral drake.");
			CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(SpectralDrake.templateId); // Spectral Drake ID: 2147483646
			Creature spectralDrake = Creature.doNew(template.getTemplateId(), true, x, y, random.nextFloat()*360.0f, mob.getLayer(),
					template.getName(), (byte)0, mob.getKingdomId(), ctype, false, (byte)150);
			Server.getInstance().broadCastAction("The spirit of the "+mob.getTemplate().getName()+" is released into the world!", mob, 20);
			Server.getInstance().broadCastAlert(spectralDrake.getName()+" is released from the soul of the "+mob.getTemplate().getName()+", seeking vengeance for its physical form!");
			
			// Insert extra hide / scale
	
			logger.info("Generating extra hide & scale to insert on the corpse of "+mob.getName()+".");
			ItemTemplate itemTemplate = ItemTemplateFactory.getInstance().getTemplate(lootTemplate);
			for(int i = 0; i < 2; i++){
				Item loot = ItemFactory.createItem(lootTemplate, 80+(15*random.nextFloat()), "");
	            String creatureName = mob.getTemplate().getName().toLowerCase();
	            if (!loot.getName().contains(creatureName)){
	                loot.setName(creatureName.toLowerCase() + " " + itemTemplate.getName());
	            }
				loot.setData2(mTemplate);
				int weightGrams = itemTemplate.getWeightGrams() * (lootTemplate == 371 ? 3 : 1);
				loot.setWeight((int)((weightGrams*0.1f)+(weightGrams*0.1f*random.nextFloat())), true);
				corpse.insertItem(loot);
			}
			for(int i = 0; i < 4; i++){
				Item loot = ItemFactory.createItem(lootTemplate, 80+(15*random.nextFloat()), "");
	            String creatureName = mob.getTemplate().getName().toLowerCase();
	            if (!loot.getName().contains(creatureName)){
	                loot.setName(creatureName.toLowerCase() + " " + itemTemplate.getName());
	            }
				loot.setData2(mTemplate);
				int weightGrams = itemTemplate.getWeightGrams() * (lootTemplate == 371 ? 3 : 1);
				loot.setWeight((int)((weightGrams*0.05f)+(weightGrams*0.05f*random.nextFloat())), true);
				spectralDrake.getInventory().insertItem(loot);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void handleChampionLoot(Item corpse){
		try{
			corpse.insertItem(ItemUtil.createRandomLootTool());
			if(random.nextInt(100) < 75){
				corpse.insertItem(ItemFactory.createItem((random.nextBoolean() ? 694 : 698), 30+(30*random.nextFloat()), ""));
			}
			if(random.nextInt(100) < 5){
				//int[] maskTemplates = {973, 974, 975, 976, 977, 978, 1099};
				int[] maskTemplates = {
						ItemList.maskEnlightended,
						ItemList.maskRavager,
						ItemList.maskPale,
						ItemList.maskShadow,
						ItemList.maskChallenge,
						ItemList.maskIsles,
						ItemList.maskOfTheReturner
				};
				corpse.insertItem(ItemFactory.createItem(maskTemplates[random.nextInt(maskTemplates.length)], 90+(9*random.nextFloat()), ""));
			}
			if(random.nextInt(100) < 1){
				Item bone = ItemFactory.createItem(867, 90+(10*random.nextFloat()), "");
		        bone.setRarity((byte)1);
		        if(random.nextInt(100) < 1){
		        	bone.setRarity((byte)2);
		        }
		        corpse.insertItem(bone);
			}
		} catch (FailedException | NoSuchTemplateException e) {
			e.printStackTrace();
		}
	}
	
	public static void checkLootTable(Creature mob, Item corpse){
	    double cretStr = Bounty.getCreatureStrength(mob);
	    int numCrystals = 0;
    	double crystalStr = cretStr;
    	if(mob.isUnique()){ // Uniques will drop 3x as many, and have special properties to enable dropping rare and possibly supreme versions as well.
    		crystalStr *= 3;
    	}else if(Servers.localServer.PVPSERVER){ // Arena gives double the amount of crystals.
    		crystalStr *= 2.5;
    	}
	    // Award chaos crystals if the strength is high enough:
	    if(crystalStr > 3000){ // 30 copper
	    	numCrystals += doRollingCrystalReward(mob, corpse, crystalStr, ChaosCrystal.templateId, 4, 5000);
	    	/*double rollingCounter = cretStr;
	    	int chance = 4;
	    	int addedCrystals = 0;
	    	if(mob.isUnique()){ // Uniques will drop 3x as many, and have special properties to enable dropping rare and possibly supreme versions as well.
	    		rollingCounter *= 3;
	    	}else if(Servers.localServer.PVPSERVER){
	    		rollingCounter *= 2;
	    	}
	    	while(rollingCounter > 0){
		    	// For every 50,000 creature strength, give a 1/8 chance at a chaos crystal
	    		if(random.nextInt(chance+addedCrystals) == 0){
	    			// The crystal quality is the cube root of the rolling counter, capped at 100 of course
	    			Item chaosCrystal = ItemFactory.createItem(ChaosCrystal.templateId, (float) (random.nextFloat()*Math.min(100, Math.cbrt(rollingCounter))), "");
	    			if(random.nextInt(40) == 0){
	    				chaosCrystal.setRarity((byte) 1);
	    			}else if(mob.isUnique() && random.nextInt(5) == 0){
	    				if(random.nextInt(5) == 0){
	    					chaosCrystal.setRarity((byte) 2);
	    				}else{
	    					chaosCrystal.setRarity((byte) 1);
	    				}
	    			}
	    			corpse.insertItem(chaosCrystal);
	    			addedCrystals++;
	    		}
	    		rollingCounter -= 5000;
	    	}
	    	if(addedCrystals > 0){
	    		hasCrystals = true;
	    	}*/
	    }
	    if(crystalStr > 10000){ // 1 silver
	    	numCrystals += doRollingCrystalReward(mob, corpse, crystalStr, EnchantersCrystal.templateId, 5, 20000);
	    	/*double rollingCounter = cretStr;
	    	int chance = 5;
	    	int addedCrystals = 0;
	    	if(mob.isUnique()){ // Uniques will drop 3x as many, and have special properties to enable dropping rare and possibly supreme versions as well.
	    		rollingCounter *= 3;
	    	}else if(Servers.localServer.PVPSERVER){
	    		rollingCounter *= 2;
	    	}
	    	while(rollingCounter > 0){
		    	// For every 200,000 creature strength, give a 1/8 chance at a enchanters crystal
	    		if(random.nextInt(chance+addedCrystals) == 0){
	    			// The crystal quality is the cube root of the rolling counter, capped at 100 of course
	    			Item enchantersCrystal = ItemFactory.createItem(EnchantersCrystal.templateId, (float) (random.nextFloat()*Math.min(100, Math.cbrt(rollingCounter))), "");
	    			if(random.nextInt(40) == 0){
	    				enchantersCrystal.setRarity((byte) 1);
	    			}else if(mob.isUnique() && random.nextInt(5) == 0){
	    				if(random.nextInt(5) == 0){
	    					enchantersCrystal.setRarity((byte) 2);
	    				}else{
	    					enchantersCrystal.setRarity((byte) 1);
	    				}
	    			}
	    			corpse.insertItem(enchantersCrystal);
	    			addedCrystals++;
	    		}
	    		rollingCounter -= 20000;
	    	}
	    	if(addedCrystals > 0){
	    		hasCrystals = true;
	    	}*/
	    }
	    boolean sendLootHelp = false;
	    // Begin loot table drops
    	if(mob.getTemplate().getTemplateId() == Reaper.templateId){
    		Server.getInstance().broadCastNormal("The "+mob.getName()+" has been slain.");
    		sendLootHelp = true;
    	}else if(mob.getTemplate().getTemplateId() == SpectralDrake.templateId){
    		try{
	    		logger.info("Generating spectral hide for the corpse of the "+mob.getName()+".");
	    		for(int i = 0; i < 2; i++){
		    		Item spectralHide = ItemFactory.createItem(SpectralHide.templateId, 50+(50*random.nextFloat()), "");
		    		ItemTemplate itemTemplate = spectralHide.getTemplate();
					int weightGrams = itemTemplate.getWeightGrams();
					spectralHide.setWeight((int)((weightGrams*0.5f)+(weightGrams*0.5f*random.nextFloat())), true);
					corpse.insertItem(spectralHide);
					if(!mob.getStatus().isChampion()){
						break;
					}
		    	}
			} catch (FailedException | NoSuchTemplateException e) {
				e.printStackTrace();
			}
    		Server.getInstance().broadCastNormal("The "+mob.getName()+" has been slain.");
    		sendLootHelp = true;
    	}else if(Arena.isTitan(mob)){
    		Server.getInstance().broadCastAlert("The Titan "+mob.getName()+" has been defeated!");
    		MiscChanges.sendGlobalFreedomChat(mob, "The Titan "+mob.getName()+" has been defeated!", 255, 105, 180);
    		MiscChanges.sendServerTabMessage("The Titan "+mob.getName()+" has been defeated!", 255, 105, 180);
    		Arena.removeTitan(mob);
    		sendLootHelp = true;
    	}
    	if(mob.getTemplate().getTemplateId() == CreatureTemplateFactory.GOBLIN_CID){
    		// Random lump of metal from goblins.
    		try{
        		int[] lumpIds = {
        				//44, 45, 46, 47, 48, 49, 205, 221, 223, 220
        				ItemList.adamantineBar,
        				ItemList.brassBar,
        				ItemList.bronzeBar,
        				ItemList.copperBar,
        				ItemList.glimmerSteelBar,
        				ItemList.goldBar,
        				ItemList.ironBar,
        				ItemList.leadBar,
        				ItemList.silverBar,
        				ItemList.steelBar,
        				ItemList.zincBar
				};
	    		Item randomLump = ItemFactory.createItem(lumpIds[random.nextInt(lumpIds.length)], 20+(60*random.nextFloat()), "");
	    		corpse.insertItem(randomLump);
			} catch (FailedException | NoSuchTemplateException e) {
				e.printStackTrace();
			}
    	}
    	if(mob.isUnique()){
    		// Spawn random addy/glimmer veins throughout the world
    		blessWorldWithMoonVeins(mob);
			/*int i = 20;
			while(i > 0){
				int x = random.nextInt(Server.surfaceMesh.getSize());
				int y = random.nextInt(Server.surfaceMesh.getSize());
				short height = Tiles.decodeHeight(Server.surfaceMesh.getTile(x, y));
				int type = Tiles.decodeType((int)Server.caveMesh.getTile(x, y));
				if(height >= 100 && (type == Tiles.Tile.TILE_CAVE_WALL.id || type == Tiles.Tile.TILE_CAVE.id)){
					Tiles.Tile tileType = random.nextBoolean() ? Tiles.Tile.TILE_CAVE_WALL_ORE_ADAMANTINE : Tiles.Tile.TILE_CAVE_WALL_ORE_GLIMMERSTEEL;
					Server.caveMesh.setTile(x, y, Tiles.encode(Tiles.decodeHeight(Server.caveMesh.getTile(x, y)), tileType.id, Tiles.decodeData(Server.caveMesh.getTile(x, y))));
					Players.getInstance().sendChangedTile(x, y, false, true);
					Server.setCaveResource(x, y, 400+random.nextInt(600));
					Village v = Villages.getVillage(x, y, true);
			        if (v == null) {
			            for (int vx = -50; vx < 50; vx += 5) {
			                for (int vy = -50; vy < 50 && (v = Villages.getVillage(x + vx, y + vy, true)) == null; vy += 5) {
			                }
			            }
			        }
			        if(v != null){
			        	HistoryManager.addHistory(mob.getTemplate().getName(), "blesses the world with a "+tileType.getName()+" near "+v.getName()+"!");
			        }
					logger.info("Placed a "+tileType.getName()+" at "+x+", "+y+" - "+height+" height");
					i--;
				}
			}
			Server.getInstance().broadCastAlert("The death of the "+mob.getTemplate().getName()+" has blessed the world with valuable ores!");*/
			// Spawn 5-10 friyan tablets throughout the world.
    		spawnFriyanTablets();
			/*i = 5+random.nextInt(5);
			while(i > 0){
				int x = random.nextInt(Server.surfaceMesh.getSize());
				int y = random.nextInt(Server.surfaceMesh.getSize());
				short height = Tiles.decodeHeight(Server.surfaceMesh.getTile(x, y));
				if(height > 0 && height < 1000 && Creature.getTileSteepness(x, y, true)[1] < 30){
					ItemFactory.createItem(FriyanTablet.templateId, 80f+random.nextInt(20), (float)x*4, (float)y*4, random.nextFloat()*360f, true, (byte)0, -10, "Friyanouce");
					logger.info("Created a Tablet of Friyan at "+x+", "+y+".");
					i--;
				}
			}*/
			
			// Spawn Spectral Drake
    		if (mob.isDragon()) { // Spawn the spectral drake and add extra hide/scale
    			handleDragonLoot(mob, corpse);
    			/*int mTemplate = mob.getTemplate().getTemplateId();
    			int lootTemplate = ItemList.drakeHide;
    			byte ctype = 0;
    			if(mTemplate == CreatureTemplateFactory.DRAGON_BLACK_CID || mTemplate == CreatureTemplateFactory.DRAGON_BLUE_CID || mTemplate == CreatureTemplateFactory.DRAGON_GREEN_CID
    					|| mTemplate == CreatureTemplateFactory.DRAGON_RED_CID || mTemplate == CreatureTemplateFactory.DRAGON_WHITE_CID){
    			//if(mTemplate == 16 || mTemplate == 89 || mTemplate == 91 || mTemplate == 90 || mTemplate == 92){
    				ctype = 99; // Champion creature type
    				lootTemplate = ItemList.dragonScale;
    				//lootTemplate = 372;
    			}else{
    				ctype = (byte)Math.max(0, Server.rand.nextInt(17) - 5);
    			}
    			
    			float x = mob.getPosX();
    			float y = mob.getPosY();
				
				// Spawn the spectral drake.
    			//logger.info("Spawning a spectral drake.");
    			CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(SpectralDrake.templateId); // Spectral Drake ID: 2147483646
    			Creature spectralDrake = Creature.doNew(template.getTemplateId(), true, x, y, random.nextFloat()*360.0f, mob.getLayer(),
    					template.getName(), (byte)0, mob.getKingdomId(), ctype, false, (byte)150);
    			Server.getInstance().broadCastAction("The spirit of the "+mob.getTemplate().getName()+" is released into the world!", mob, 20);
	    		Server.getInstance().broadCastAlert(spectralDrake.getName()+" is released from the soul of the "+mob.getTemplate().getName()+", seeking vengeance for its physical form!");
	    		
	    		// Insert extra hide / scale

    			logger.info("Generating extra hide & scale to insert on the corpse of "+mob.getName()+".");
    			ItemTemplate itemTemplate = ItemTemplateFactory.getInstance().getTemplate(lootTemplate);
    			for(int i = 0; i < 2; i++){
	    			Item loot = ItemFactory.createItem(lootTemplate, 80+(15*random.nextFloat()), "");
	                String creatureName = mob.getTemplate().getName().toLowerCase();
	                if (!loot.getName().contains(creatureName)){
	                    loot.setName(creatureName.toLowerCase() + " " + itemTemplate.getName());
	                }
	    			loot.setData2(mTemplate);
	    			int weightGrams = itemTemplate.getWeightGrams() * (lootTemplate == 371 ? 3 : 1);
	    			loot.setWeight((int)((weightGrams*0.1f)+(weightGrams*0.1f*random.nextFloat())), true);
	    			corpse.insertItem(loot);
    			}
    			for(int i = 0; i < 4; i++){
	    			Item loot = ItemFactory.createItem(lootTemplate, 80+(15*random.nextFloat()), "");
	                String creatureName = mob.getTemplate().getName().toLowerCase();
	                if (!loot.getName().contains(creatureName)){
	                    loot.setName(creatureName.toLowerCase() + " " + itemTemplate.getName());
	                }
	    			loot.setData2(mTemplate);
	    			int weightGrams = itemTemplate.getWeightGrams() * (lootTemplate == 371 ? 3 : 1);
	    			loot.setWeight((int)((weightGrams*0.05f)+(weightGrams*0.05f*random.nextFloat())), true);
	    			spectralDrake.getInventory().insertItem(loot);
    			}*/
    		} else { // Spawn the reaper
				try {
	    			byte ctype = (byte)Math.max(0, Server.rand.nextInt(17) - 5);
		    		CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(Reaper.templateId); // Reaper ID: 2147483647
		    		Creature reaper = Creature.doNew(template.getTemplateId(), true, mob.getPosX(), mob.getPosY(), random.nextFloat()*360.0f, mob.getLayer(), 
								template.getName(), (byte)0, mob.getKingdomId(), ctype, false, (byte)150);
		    		Server.getInstance().broadCastAction("The death of the "+mob.getTemplate().getName()+" attracts a powerful being from below, seeking to claim it's soul.", mob, 20);
		    		Server.getInstance().broadCastAlert(reaper.getName()+" is released from the underworld, seeking the soul of a powerful creature!");
				} catch (Exception e) {
					e.printStackTrace();
				}
    		}
    		sendLootHelp = true;
    	}
    	if(mob.getStatus().isChampion()){
    		// Champion mob loot
    		handleChampionLoot(corpse);
    		/*corpse.insertItem(ItemUtil.createRandomLootTool());
    		if(random.nextInt(100) < 75){
    			corpse.insertItem(ItemFactory.createItem((random.nextBoolean() ? 694 : 698), 30+(30*random.nextFloat()), ""));
    		}
    		if(random.nextInt(100) < 5){
    			int[] maskTemplates = {973, 974, 975, 976, 977, 978, 1099};
    			corpse.insertItem(ItemFactory.createItem(maskTemplates[random.nextInt(maskTemplates.length)], 90+(9*random.nextFloat()), ""));
    		}
    		if(random.nextInt(100) < 1){
    			Item bone = ItemFactory.createItem(867, 90+(10*random.nextFloat()), "");
		        bone.setRarity((byte)1);
		        if(random.nextInt(100) < 1){
		        	bone.setRarity((byte)2);
		        }
		        corpse.insertItem(bone);
    		}*/
    	}
    	if(sendLootHelp){
			logger.info("Beginning loot assistance message generation...");
			displayLootAssistance(mob);
			/*ArrayList<String> atkNames = new ArrayList<String>();
			Map<Long, Long> attackers = Bounty.getAttackers(mob);
			if(attackers != null){
				for(Long wid : attackers.keySet()){
					Creature cret = Creatures.getInstance().getCreatureOrNull(wid);
					if(cret != null && cret.isPlayer()){
						atkNames.add(Players.getInstance().getPlayer(wid).getName());
					}
				}
				if(atkNames.size() > 0){
					String atkStrBuilder = "Loot Assistance <Attackers> ("+mob.getName()+"): ";
		    		while(atkNames.size() > 0){
		    			int index = Server.rand.nextInt(atkNames.size());
		    			atkStrBuilder += atkNames.get(index);
		    			atkNames.remove(index);
		    			if(atkNames.size() > 0){
		    				atkStrBuilder += ", ";
		    			}
		    		}
		    		MiscChanges.sendServerTabMessage(atkStrBuilder, 0, 128, 255);
		    		logger.info("Broadcast loot assistance message success [Attackers].");
				}else{
	    			logger.warning("Powerful creature "+mob.getName()+" died, but no players were credited to its death [Attackers].");
				}
			}else{
				logger.warning("Attackers was null for creature "+mob.getName()+" [Attackers].");
			}*/
    		/*if(Bounty.dealtDamage.containsKey(mob.getWurmId())){
    			logger.info("Found the damageDealt entry, parsing...");
	    		ArrayList<String> names = new ArrayList<String>();
	    		ArrayList<Double> damages = new ArrayList<Double>();
	    		for(long creatureId : Bounty.dealtDamage.get(mob.getWurmId()).keySet()){
	    			if(Players.getInstance().getPlayerOrNull(creatureId) != null){
	    				names.add(Players.getInstance().getPlayerOrNull(creatureId).getName());
	    				damages.add(Bounty.dealtDamage.get(mob.getWurmId()).get(creatureId));
	    			}else{
	    				if(Creatures.getInstance().getCreatureOrNull(creatureId) != null){
	    					logger.info("Skipping creature "+Creatures.getInstance().getCreatureOrNull(creatureId).getName()+" in loot assistance.");
	    				}
	    			}
	    		}
	    		logger.info("Names have been added: "+names);
	    		String strBuilder = "Loot Assistance <Damagers> ("+mob.getName()+"): ";
	    		DecimalFormat formatter = new DecimalFormat("#,###,###");
	    		while(names.size() > 0){
	    			int index = Server.rand.nextInt(names.size());
	    			strBuilder += names.get(index);
	    			strBuilder += " ["+formatter.format(Math.round(damages.get(index)))+"]";
	    			names.remove(index);
	    			damages.remove(index);
	    			if(names.size() > 0){
	    				strBuilder += ", ";
	    			}
	    		}
	    		MiscChanges.sendServerTabMessage(strBuilder, 0, 128, 255);
	    		logger.info("Broadcast loot assistance message success [Damage].");
    		}else{
    			logger.warning("Powerful creature "+mob.getName()+" died, but no players were credited to its death [Damage].");
    		}*/
    	}
    	if(numCrystals > 0){
    		Server.getInstance().broadCastAction(mob.getName()+" had something of interest...", mob, 5);
    	}
    }
}
