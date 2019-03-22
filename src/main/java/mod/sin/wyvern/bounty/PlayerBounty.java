package mod.sin.wyvern.bounty;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Player;
import mod.piddagoras.duskombat.DamageEngine;
import mod.sin.armour.SpectralHide;
import mod.sin.creatures.Reaper;
import mod.sin.creatures.SpectralDrake;
import mod.sin.items.AffinityOrb;
import mod.sin.items.caches.RiftCache;
import mod.sin.items.caches.TitanCache;
import mod.sin.wyvern.*;
import mod.sin.wyvern.util.ItemUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

public class PlayerBounty {
	public static final Logger logger = Logger.getLogger(PlayerBounty.class.getName());
	protected static final Random random = new Random();
	public static HashMap<String, Long> steamIdMap = new HashMap<>();
	public static HashMap<Long, ArrayList<Long>> playersRewarded = new HashMap<>();
	
	public static double getTypeBountyMod(Creature mob, String mobType){
		if(!mob.isUnique()){
    		if (mobType.endsWith("fierce ")){
    			return 1.5;
    		}else if (mobType.endsWith("angry ")){
    			return 1.4;
    		}else if (mobType.endsWith("raging ")){
    			return 1.6;
    		}else if (mobType.endsWith("slow ")){
    			return 0.95;
    		}else if (mobType.endsWith("alert ")){
    			return 1.2;
    		}else if (mobType.endsWith("greenish ")){
    			return 1.7;
    		}else if (mobType.endsWith("lurking ")){
    			return 1.1;
    		}else if (mobType.endsWith("sly ")){
    			return 0.8;
    		}else if (mobType.endsWith("hardened ")){
    			return 1.3;
    		}else if (mobType.endsWith("scared ")){
    			return 0.85;
    		}else if (mobType.endsWith("diseased ")){
    			return 0.9;
    		}else if (mobType.endsWith("champion ")){
    			return 2.0;
    		}
		}
		return 1.0;
	}
	
	public static void rewardPowerfulLoot(Player player, Creature mob){
		try{
			// Affinity Orb:
			Item affinityOrb = ItemFactory.createItem(AffinityOrb.templateId, 99f+(1f*random.nextFloat()), "");
		    player.getInventory().insertItem(affinityOrb);
		    // Enchant Orb:
			float power;
			if(mob.getStatus().isChampion()){
				power = 100f+(random.nextFloat()*20f);
			}else{
				power = 90f+(random.nextFloat()*30f);
			}
			Item enchantOrb = ItemUtil.createEnchantOrb(power);
			player.getInventory().insertItem(enchantOrb);
			player.getCommunicator().sendSafeServerMessage("Libila takes the "+mob.getNameWithoutPrefixes()+"'s soul, but leaves something else behind...");
    	}catch (NoSuchTemplateException | FailedException e) {
			e.printStackTrace();
		}
	}
	
	public static void rewardSpectralLoot(Player player){
		try{
			double fightskill = player.getFightingSkill().getKnowledge();
			Item spectralHide = ItemFactory.createItem(SpectralHide.templateId, 70+(30*random.nextFloat()), ""); // Spectral Hide ID: 22764
    		ItemTemplate itemTemplate = spectralHide.getTemplate();
			int weightGrams = itemTemplate.getWeightGrams();
			spectralHide.setWeight((int)((weightGrams*0.25f)+(weightGrams*0.25f*fightskill/100f*random.nextFloat())), true);
			player.getInventory().insertItem(spectralHide);
			String fightStrength = "strong";
			if(fightskill >= 60){
				fightStrength = "great";
			}
			if(fightskill >= 70){
				fightStrength = "powerful";
			}
			if(fightskill >= 80){
				fightStrength = "master";
			}
			if(fightskill >= 90){
				fightStrength = "legendary";
			}
    		player.getCommunicator().sendSafeServerMessage("The spirit recognizes you as a "+fightStrength+" warrior, and rewards you accordingly.");
			player.addTitle(PlayerTitles.getTitle(PlayerTitles.SPECTRAL));
    	}catch (NoSuchTemplateException | FailedException e) {
			e.printStackTrace();
		}
	}
	
	public static void checkPlayerReward(Player player, Creature mob){
		try{
			if(mob.isReborn() || mob.isBred()){
				return;
			}
			int mobTemplateId = mob.getTemplate().getTemplateId();
			long mobWurmId = mob.getWurmId();
			if(playersRewarded.containsKey(mobWurmId)){
				ArrayList<Long> steamArray = playersRewarded.get(mobWurmId);
				if(steamArray.contains(steamIdMap.get(player.getName()))){
					player.getCommunicator().sendSafeServerMessage("Another character has claimed the reward from this bounty.");
					return;
				}
			}
    		if(DamageEngine.dealtDamage.containsKey(mobWurmId) && DamageEngine.dealtDamage.get(mobWurmId).containsKey(player.getWurmId())){
    			// -- Damage Dealt Rewards -- //
				/*if(mob.isUnique()){
					// Treasure boxes awarded to players who deal damage:
					Item treasureBox = ItemUtil.createTreasureBox();
					if(treasureBox != null){
						player.getInventory().insertItem(treasureBox);
					}else{
						logger.warning("Error: Treasure box was not created properly!");
					}
				}*/
				if(mob.isUnique()){
					MiscChanges.addPlayerStat(player.getName(), "UNIQUES");
				}
				if(RareSpawns.isRareCreature(mob)){
                    Item riftCache = ItemFactory.createItem(RiftCache.templateId, 50f+(30f*Server.rand.nextFloat()), mob.getName());
                    player.getInventory().insertItem(riftCache, true);
                }
				if(Titans.isTitan(mob)){
					player.addTitle(PlayerTitles.getTitle(PlayerTitles.TITAN_SLAYER));
					Item affinityOrb = ItemFactory.createItem(AffinityOrb.templateId, 99f, mob.getName());
					player.getInventory().insertItem(affinityOrb, true);
					Item titanCache = ItemFactory.createItem(TitanCache.templateId, 99f, mob.getName());
					player.getInventory().insertItem(titanCache, true);
					MiscChanges.addPlayerStat(player.getName(), "TITANS");
					return;
				}
				//double fightskill = player.getFightingSkill().getKnowledge();
		    	/*if((mobTemplateId == Reaper.templateId || mobTemplateId == SpectralDrake.templateId) && fightskill >= 50){
		    		rewardPowerfulLoot(player, mob); // Reward affinity orb and enchant orb:
		    		if(mob.getTemplate().getTemplateId() == SpectralDrake.templateId){
			    		rewardSpectralLoot(player); // Reward spectral hide for spectral drakes
		    		}
		    		return; // If the player receives powerful loot, break the method completely and skip bounty.
		    	}*/
		    	// -- End Damage Dealt Rewards -- //
    		}
    		String mobName = mob.getTemplate().getName().toLowerCase();
    		String mobType = mob.getPrefixes();
    		long iron;
		    double cretStr = Bounty.getCreatureStrength(mob);
		    
    		if(Bounty.reward.containsKey(mobName)){
    			iron = Bounty.reward.get(mobName); // Prioritize hardcoded values in the Bounty.reward list first
    		}else{
    		    iron = Math.round(cretStr); // Calculate bounty from creature strength if they do not exist in the reward list.
    		}
    		if(Servers.localServer.PVPSERVER){
    			if(!mob.isUnique() && mob.getTemplate().getTemplateId() != SpectralDrake.templateId && mob.getTemplate().getTemplateId() != Reaper.templateId){
    				iron *= 1.2d;
    			}
    			/*try {
					player.getSkills().getSkill(SkillList.MEDITATING).skillCheck(10, 0, false, 1); // Meditation skill gain
					float faithMod = 1-(player.getFaith()/200f);
					player.modifyFaith((((float)cretStr)*faithMod)/200000f); // Faith skill gain
				} catch (NoSuchSkillException e) {
					e.printStackTrace();
				}*/
    		}
    		
    		// Multiply bounty based on type
			//if(mob.isAggHuman() || mob.getBaseCombatRating() > 10) {
			iron *= getTypeBountyMod(mob, mobType);

			player.addMoney(iron);
			Item inventory = player.getInventory();
			String coinMessage = Economy.getEconomy().getChangeFor(iron).getChangeString();
			String strBuilder = "You are awarded " + coinMessage;
			strBuilder += " for slaying the " + mob.getName() + ".";
			player.getCommunicator().sendSafeServerMessage(strBuilder);
			long playerSteamId = steamIdMap.get(player.getName());
			if(playersRewarded.containsKey(mobWurmId)){
				playersRewarded.get(mobWurmId).add(playerSteamId);
			}else{
				ArrayList<Long> steamArray = new ArrayList<>();
				steamArray.add(playerSteamId);
				playersRewarded.put(mobWurmId, steamArray);
			}
			//}
    	}catch (IOException | FailedException | NoSuchTemplateException e) {
			e.printStackTrace();
		}
	} // checkPlayerReward
	
	public static void checkPlayerBounty(Player player, Creature creature){
		try {
			//Map<Long, Long> attackers = ReflectionUtil.getPrivateField(creature, ReflectionUtil.getField(creature.getClass(), "attackers"));
			Map<Long, Long> attackers = Bounty.getAttackers(creature);
			if((attackers != null && !Bounty.isCombatant(attackers, player.getWurmId())) || creature.isPlayer() || creature.isReborn()){
				return;
			}
			logger.info(player.getName()+" killed "+creature.getName());
			checkPlayerReward(player, creature);
		} catch (IllegalArgumentException | ClassCastException e) {
			e.printStackTrace();
		}
	}
}
