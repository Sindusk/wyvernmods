package mod.sin.wyvern;

import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateCreator;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.NoSuchTemplateException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import mod.sin.items.AffinityOrb;

public class TreasureChests {
	public static void doItemSpawn(Item inventory, int[] templateTypes, float startQl, float qlValRange, int maxNums) {
		/*if (item.getOwnerId() != -10) {
			return;
		}*/
		/*Item[] currentItems = item.getAllItems(true);
		boolean[] hasTypes = new boolean[templateTypes.length];
		block2 : for (Item it : currentItems) {
			for (int x = 0; x < templateTypes.length; ++x) {
				if (templateTypes[x] != it.getTemplateId()) continue;
				hasTypes[x] = true;
				continue block2;
			}
		}
		for (int x = 0; x < hasTypes.length; ++x) {*/
		for (int x = 0; x < templateTypes.length; ++x){
			//if (hasTypes[x]) continue; // Disabled so it doesn't mess with my things
			for (int nums = 0; nums < maxNums; ++nums) {
				try{
					byte rrarity;
					int templateType = templateTypes[x];
					/*if (onGround) {
						ItemFactory.createItem(templateType, startQl + Server.rand.nextFloat() * qlValRange, item.getPosX() + 0.3f, item.getPosY() + 0.3f, 65.0f, item.isOnSurface(), (byte) 0, -10, "");
						continue;
					}*/
					boolean isBoneCollar = templateType == 867;
					rrarity = (byte) (Server.rand.nextInt(100) == 0 || isBoneCollar ? 1 : 0);
					if (rrarity > 0) {
						rrarity = (byte) (Server.rand.nextInt(100) == 0 && isBoneCollar ? 2 : 1);
					}
					if (rrarity > 1) {
						rrarity = (byte) (Server.rand.nextInt(100) == 0 && isBoneCollar ? 3 : 2);
					}
					float newql = startQl + Server.rand.nextFloat() * qlValRange;
					
					Item toInsert = ItemFactory.createItem(templateType, newql, rrarity, "");
					
					if (templateType == ItemList.statueHota){
						toInsert.setAuxData((byte)Server.rand.nextInt(10));
						toInsert.setWeight(50000, true);
					}
					if (templateType == ItemList.eggLarge) {
						toInsert.setData1(CreatureTemplateCreator.getRandomDragonOrDrakeId());
					}
					if (templateType == ItemList.drakeHide) {
						int colorId = CreatureTemplateCreator.getRandomDrakeId();
						toInsert.setData1(colorId);
						CreatureTemplate cTemplate = CreatureTemplateFactory.getInstance().getTemplate(colorId);
						String creatureName = cTemplate.getName().toLowerCase();
						if (!toInsert.getName().contains(creatureName)){
							toInsert.setName(creatureName.toLowerCase() + " " + toInsert.getTemplate().getName());
						}
						toInsert.setWeight(50+Server.rand.nextInt(100), true);
					}
					if (templateType == ItemList.dragonScale) {
						int[] dragonIds = new int[]{CreatureTemplateIds.DRAGON_BLACK_CID, CreatureTemplateIds.DRAGON_BLUE_CID, CreatureTemplateIds.DRAGON_GREEN_CID,
								CreatureTemplateIds.DRAGON_RED_CID, CreatureTemplateIds.DRAGON_WHITE_CID};
						int colorId = dragonIds[Server.rand.nextInt(dragonIds.length)];
						toInsert.setData1(colorId);
						CreatureTemplate cTemplate = CreatureTemplateFactory.getInstance().getTemplate(colorId);
						String creatureName = cTemplate.getName().toLowerCase();
						if (!toInsert.getName().contains(creatureName)){
							toInsert.setName(creatureName.toLowerCase() + " " + toInsert.getTemplate().getName());
						}
						toInsert.setWeight(100+Server.rand.nextInt(150), true);
					} // if
					if (templateType == ItemList.riftCrystal || templateType == ItemList.riftWood || templateType == ItemList.riftStone) {
						toInsert.setHasNoDecay(true);
					} // if
					inventory.insertItem(toInsert, true);
					continue;
				} catch (NoSuchTemplateException | FailedException | NoSuchCreatureTemplateException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void newFillTreasureChest(Item item, int auxdata) {
		// AuxData = random value 0-99
		// 0-59 = rare, 60-89 = supreme, 90-99 = fantastic
		int[] normalGems = new int[]{ItemList.emerald, ItemList.ruby, ItemList.opal, ItemList.diamond, ItemList.sapphire};
		int[] starGems = new int[]{375, 377, 379, 381, 383};
		int[] lumps = new int[]{44, 45, 46, 47, 48, 49, 205, 220, 221, 223, 694, 698, 837};
		int[] potions = new int[]{871, 874, 875, 876, 877, 878, 879, 881, 883};
		
		/*int[] usefulItems = {ItemList.axeSmall, ItemList.shieldMedium, ItemList.hatchet, ItemList.knifeCarving,
		ItemList.pickAxe, ItemList.swordLong, ItemList.saw, ItemList.shovel, ItemList.rake, ItemList.hammerMetal,
		ItemList.hammerWood, ItemList.anvilSmall, ItemList.cheeseDrill, ItemList.swordShort, ItemList.swordTwoHander,
		ItemList.shieldSmallWood, ItemList.shieldSmallMetal, ItemList.shieldMediumWood, ItemList.shieldLargeWood,
		ItemList.shieldLargeMetal, ItemList.axeHuge, ItemList.axeMedium, ItemList.knifeButchering,
		ItemList.fishingRodIronHook, ItemList.stoneChisel, ItemList.leatherGlove, ItemList.leatherJacket,
		ItemList.leatherBoot, ItemList.leatherSleeve, ItemList.leatherCap, ItemList.leatherHose, ItemList.clothGlove,
		ItemList.clothShirt, ItemList.clothSleeve, ItemList.clothJacket, ItemList.clothHose, ItemList.clothShoes,
		ItemList.studdedLeatherSleeve, ItemList.studdedLeatherBoot, ItemList.studdedLeatherCap,
		ItemList.studdedLeatherHose, ItemList.studdedLeatherGlove, ItemList.studdedLeatherJacket,
		ItemList.spindle, ItemList.flintSteel, ItemList.fishingRodWoodenHook, ItemList.stoneOven, ItemList.forge,
		ItemList.anvilLarge, ItemList.cartSmall, ItemList.needleIron, ItemList.loom, ItemList.sickle, ItemList.scythe,
		ItemList.gloveSteel, ItemList.chainBoot, ItemList.chainHose, ItemList.chainJacket, ItemList.chainSleeve,
		ItemList.chainGlove, ItemList.chainCoif, ItemList.plateBoot, ItemList.plateHose, ItemList.plateJacket,
		ItemList.plateSleeve, ItemList.plateGauntlet, ItemList.helmetBasinet, ItemList.helmetGreat, ItemList.helmetOpen,
		ItemList.maulLarge, ItemList.maulSmall, ItemList.maulMedium, ItemList.whetStone, ItemList.pelt, ItemList.ropeTool,
		ItemList.guardTower, ItemList.file, ItemList.awl, ItemList.leatherKnife, ItemList.scissors, ItemList.clayShaper,
		ItemList.spatula, ItemList.bowShort, ItemList.bowMedium, ItemList.bowLong, ItemList.dragonLeatherSleeve,
		ItemList.dragonLeatherBoot, ItemList.dragonLeatherCap, ItemList.dragonLeatherHose, ItemList.dragonLeatherGlove,
		ItemList.dragonLeatherJacket, ItemList.dragonScaleBoot, ItemList.dragonScaleHose, ItemList.dragonScaleJacket,
		ItemList.dragonScaleSleeve, ItemList.dragonScaleGauntlet, ItemList.boatRowing, ItemList.boatSailing,
		ItemList.trowel, ItemList.statuetteFo, ItemList.statuetteLibila, ItemList.statuetteMagranon,
		ItemList.statuetteVynora, ItemList.cartLarge, ItemList.cog, ItemList.corbita, ItemList.knarr, ItemList.caravel,
		ItemList.saddle, ItemList.horseShoe, ItemList.meditationRugOne, ItemList.meditationRugTwo,
		ItemList.meditationRugThree, ItemList.meditationRugFour, ItemList.groomingBrush, ItemList.spearLong,
		ItemList.halberd, ItemList.spearSteel, ItemList.clothHood, ItemList.wagon, ItemList.boneCollar,
		ItemList.spinningWheel, ItemList.smelter, ItemList.halterRope};*/
		
		// Generate Rare Chest
		if (auxdata < 60) {
			if(item.getTemplateId() == ItemList.treasureChest){
				item.setRarity((byte)1);
			}
			// Generate source, addy / glimmer, gem
			int[] templateTypes = new int[]{ItemList.sourceCrystal, ItemList.adamantineBar, 
					ItemList.glimmerSteelBar, normalGems[Server.rand.nextInt(5)]};
			doItemSpawn(item, templateTypes, 70.0f, 30.0f, 1);

			// Generate seryll
			if (Server.rand.nextBoolean()) {
				doItemSpawn(item, new int[]{ItemList.seryllBar}, 60.0f, 20.0f, 1);
			} // if

			/*// Generate meal
			if (Server.rand.nextBoolean()) {
				this.doItemSpawn(item, new int[]{ItemList.steak}, (float)auxdata, 60.0f-(float)auxdata, 1, false);
			} // if*/

			// Generate random lump
			if (Server.rand.nextBoolean()) {
				doItemSpawn(item, new int[]{lumps[Server.rand.nextInt(13)]}, 80.0f, 20.0f, 1);
			} // if

			// Generate yellow potion
			if (Server.rand.nextInt(5) == 0) {
				doItemSpawn(item, new int[]{ItemList.potionIllusion}, 10.0f, 90.0f, 1);
			} // if

			// Generate fireworks
			if (Server.rand.nextInt(20) == 0) {
				doItemSpawn(item, new int[]{ItemList.fireworks}, (float)auxdata, 60.0f-(float)auxdata, 1);
			} // if

			// Generate affinity orb
			if (Server.rand.nextInt(200-auxdata) == 0){
				doItemSpawn(item, new int[]{22767}, 80.0f, 10.0f, 1); // Affinity Orb
			}

			/*// Generate random tool
			if (Server.rand.nextInt(4) == 0) {
				// generate random tool thingy
				this.doItemSpawn(item, templateTypes6, 25.0f, 50.0f, 1, false);
			} // if*/

			/*// Generate source
			if (Server.rand.nextInt(4) == 0) {
				int[] templateTypes7 = new int[]{ItemList.skinWater};
				// put 0.1-0.2kg of source in the water skin
				this.doItemSpawn(item, templateTypes7, 25.0f, 50.0f, 1, false);
			} // if*/

			// Generate random potion
			if (Server.rand.nextInt(10) == 0) {
				doItemSpawn(item, new int[]{potions[Server.rand.nextInt(potions.length)]}, 50.0f, 50.0f, 1);
			} // if

			// Generate rift items
			switch (Server.rand.nextInt(3)) {
			case 0: 
				doItemSpawn(item, new int[]{ItemList.riftStone}, 90.0f, 10.0f, 1);
				break;
			case 1:	
				doItemSpawn(item, new int[]{ItemList.riftCrystal}, 90.0f, 10.0f, 1);
				break;
			case 2: 
				doItemSpawn(item, new int[]{ItemList.riftWood}, 90.0f, 10.0f, 1);
				break;
			} // switch

			// Generate Supreme Chest
		} else if (auxdata < 90) {
			if(item.getTemplateId() == ItemList.treasureChest){
				item.setRarity((byte)2);
			}
			// Generate source, gem
			int[] templateTypes = new int[]{ItemList.sourceCrystal, 374 + Server.rand.nextInt(10)};
			// vessel the gem
			doItemSpawn(item, templateTypes, 80.0f, 20.0f, 1);

			// Spawn addy / glimmer
			int[] templateTypes2 = new int[]{ItemList.adamantineBar, ItemList.glimmerSteelBar};
			doItemSpawn(item, templateTypes2, 80.0f, 20.0f, 2 + Server.rand.nextInt(2));

			// Generate seryll
			int[] templateTypes3 = new int[]{ItemList.seryllBar};
			doItemSpawn(item, templateTypes3, 80.0f, 20.0f, 1 + Server.rand.nextInt(3));

			/*// Generate meal
			if (Server.rand.nextBoolean()) {
				this.doItemSpawn(item, new int[]{ItemList.steak}, (float)auxdata, 90.0f-(float)auxdata, 2, false);
			} // if*/

			// Generate fireworks
			if (Server.rand.nextInt(10) == 0) {
				doItemSpawn(item, new int[]{ItemList.fireworks}, (float)auxdata, 90.0f-(float)auxdata, 1);
			} // if

			// Generate affinity orb
			if (Server.rand.nextInt(150-auxdata) == 0){
				doItemSpawn(item, new int[]{22767}, 90.0f, 5.0f, 1); // Affinity Orb
			}

			/*// Generate source
			if (Server.rand.nextBoolean()) {
				int[] templateTypes6 = new int[]{ItemList.skinWater};
				// put 0.2-0.3kg of source in the water skin
				this.doItemSpawn(item, templateTypes6, 25.0f, 50.0f, 1, false);
			} // if*/

			// Generate dragon hide / scale
			if (Server.rand.nextInt(10) == 0) {
				doItemSpawn(item, new int[]{371 + (Server.rand.nextBoolean() ? 0 : 1)}, 80.0f, 20.0f, 1);
			} // if

			// Generate rift items
			doItemSpawn(item, new int[]{ItemList.riftStone}, 90.0f, 10.0f, 1);
			doItemSpawn(item, new int[]{ItemList.riftCrystal}, 90.0f, 10.0f, 1);
			doItemSpawn(item, new int[]{ItemList.riftWood}, 90.0f, 10.0f, 1);

			// Generate Fantastic Chest
		} else {
			if(item.getTemplateId() == ItemList.treasureChest){
				item.setRarity((byte)3);
			}
			// Generate source, gem
			int[] templateTypes = new int[]{ItemList.sourceCrystal, starGems[Server.rand.nextInt(starGems.length)]};
			doItemSpawn(item, templateTypes, 90.0f, 10.0f, 1);

			// Generate addy / glimmer
			int[] templateTypes2 = new int[]{ItemList.adamantineBar, ItemList.glimmerSteelBar};
			doItemSpawn(item, templateTypes2, 80.0f, 20.0f, 3 + Server.rand.nextInt(3));

			// Generate seryll
			int[] templateTypes3 = new int[]{ItemList.seryllBar};
			doItemSpawn(item, templateTypes3, 80.0f, 20.0f, 2 + Server.rand.nextInt(3));

			// Generate affinity orb
			if(Server.rand.nextBoolean()){
				doItemSpawn(item, new int[]{AffinityOrb.templateId}, 99.0f, 1.0f, 1); // Affinity Orb
			}

			/*// Generate source
			if (Server.rand.nextInt(4) != 0) {
				int[] templateTypes5 = new int[]{ItemList.skinWater};
				// put 0.5-0.7kg of source in the water skin
				this.doItemSpawn(item, templateTypes5, 25.0f, 50.0f, 1, false);
			} // if*/

			// Generate fireworks
			if (Server.rand.nextInt(5) == 0) {
				doItemSpawn(item, new int[]{ItemList.fireworks}, (float)auxdata, 100.0f-(float)auxdata, 1);
			} // if

			/*
			// Generate inscriber
			if (Server.rand.nextInt(4) == 0) {
				int[] templateTypes7 = new int[]{ItemList.inscriber};
				this.doItemSpawn(item, templateTypes7, 99.0f, 1.0f, 1, false);
			} // if
			 */

			// Generate dragon hide / scale
			if (Server.rand.nextInt(10) == 0) {
				doItemSpawn(item, new int[]{371 + (Server.rand.nextBoolean() ? 0 : 1)}, 90.0f, 10.0f, 1);
			} // if

			// Generate spyglass
			if (Server.rand.nextInt(100) == 0) {
				doItemSpawn(item, new int[]{ItemList.spyglass}, 99.0f, 1.0f, 1);
			} // if

			// Generate BoK
			if (Server.rand.nextInt(100) == 0) {
				doItemSpawn(item, new int[]{ItemList.bagKeeping}, 99.0f, 1.0f, 1);
			} // if

			// Generate rift items
			doItemSpawn(item, new int[]{ItemList.riftStone}, 90.0f, 10.0f, 1 + Server.rand.nextInt(3));
			doItemSpawn(item, new int[]{ItemList.riftCrystal}, 90.0f, 10.0f, 1 + Server.rand.nextInt(3));
			doItemSpawn(item, new int[]{ItemList.riftWood}, 90.0f, 10.0f, 1 + Server.rand.nextInt(3));

			// Generate OP items
			int rand = Server.rand.nextInt(500);
			int[] fantasticLoot = {};
			int amount = 1;
			if(rand < 249){
				fantasticLoot = new int[]{ItemList.drakeHide};
				amount = 3;
				// Three chunks of random color dragon scale.
			} else if(rand < 349){
				fantasticLoot = new int[]{ItemList.dragonScale};
				amount = 3;
				// Random hota statue.
			} else if(rand < 414){
				fantasticLoot = new int[]{ItemList.statueHota};
				// Rare Bone & Helmet
			} else if (rand < 464) {
				fantasticLoot = new int[]{ItemList.boneCollar/*, Server.rand.nextBoolean() ? ItemList.helmetBasinet : ItemList.helmetGreat*/};
				// 1/100 chance of supreme, 1/10,000 of fantastic
				// Sorcery
			} else if (rand < 490) {
				fantasticLoot = new int[]{795 + Server.rand.nextInt(16)};
				// Dragon Egg
			} else {
				fantasticLoot = new int[]{ItemList.eggLarge};
			} // else
			doItemSpawn(item, fantasticLoot, 99.0f, 1.0f, amount);
		}
	} // fillTreasureChest

	public static void preInit() throws NotFoundException, CannotCompileException{
		ClassPool classPool = HookManager.getInstance().getClassPool();

		CtClass ctZone = classPool.get("com.wurmonline.server.zones.Zone");
		CtMethod ctCreateTreasureChest = ctZone.getDeclaredMethod("createTreasureChest");
		// Increase treasure chest AuxData by 2.
		ctCreateTreasureChest.instrument(new ExprEditor(){
			public void edit(MethodCall m) throws CannotCompileException {
				if (m.getMethodName().equals("setAuxData")) {
					m.replace("$_ = $proceed((byte)(com.wurmonline.server.Server.rand.nextInt(100)));");
					return;
				}
			}
		});

		// New treasure chest fill.
		CtClass ctItem = classPool.get("com.wurmonline.server.items.Item");
		ctItem.getDeclaredMethod("fillTreasureChest").setBody("{"
				+ "  mod.sin.wyvern.TreasureChests.newFillTreasureChest(this, this.getAuxData());"
				+ "  logger.info(\"Spawned treasure chest level \"+this.getAuxData()+\" at \"+(this.getPosX()/4)+\", \"+(this.getPosY()/4));"
				+ "}");

		// Add an Affinity Orb to the chest.
		/*ctItem.getDeclaredMethod("fillTreasureChest").insertAfter(""
        		+ "if(this.getAuxData() >= 8){"
        		+ "  int[] templateTypes8 = new int[]{22767};"
        		+ "  this.spawnItemSpawn(templateTypes8, 99.0f, 1.0f, 1, false);"
        		+ "}"
        		+ "logger.info(\"Spawned treasure chest level \"+this.getAuxData()+\" at \"+(this.getPosX()/4)+\", \"+(this.getPosY()/4));");*/
	}
}
