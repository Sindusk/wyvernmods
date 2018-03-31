package mod.sin.wyvern.bounty;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Titles.Title;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.SkillList;
import mod.sin.armour.SpectralHide;
import mod.sin.creatures.Reaper;
import mod.sin.creatures.SpectralDrake;
import mod.sin.items.AffinityOrb;
import mod.sin.wyvern.Bounty;
import mod.sin.wyvern.arena.Arena;
import mod.sin.wyvern.util.ItemUtil;

public class PlayerBounty {
	public static final Logger logger = Logger.getLogger(PlayerBounty.class.getName());
	protected static final Random random = new Random();
	
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
			player.addTitle(Title.getTitle(701));
    	}catch (NoSuchTemplateException | FailedException e) {
			e.printStackTrace();
		}
	}
	
	public static void checkPlayerReward(Player player, Creature mob){
		try{
			int mobTemplateId = mob.getTemplate().getTemplateId();
    		if(Bounty.dealtDamage.containsKey(mob.getWurmId()) && Bounty.dealtDamage.get(mob.getWurmId()).containsKey(player.getWurmId())){
    			// -- Damage Dealt Rewards -- //
				if(mob.isUnique()){
					// Treasure boxes awarded to players who deal damage:
					Item treasureBox = ItemUtil.createTreasureBox();
					if(treasureBox != null){
						player.getInventory().insertItem(treasureBox);
					}else{
						logger.warning("Error: Treasure box was not created properly!");
					}
				}
				if(Arena.isTitan(mob)){
					player.addTitle(Title.getTitle(700));
				}
				double fightskill = player.getFightingSkill().getKnowledge();
		    	if((mobTemplateId == Reaper.templateId || mobTemplateId == SpectralDrake.templateId) && fightskill >= 50){
		    		rewardPowerfulLoot(player, mob); // Reward affinity orb and enchant orb:
		    		if(mob.getTemplate().getTemplateId() == SpectralDrake.templateId){
			    		rewardSpectralLoot(player); // Reward spectral hide for spectral drakes
		    		}
		    		return; // If the player receives powerful loot, break the method completely and skip bounty.
		    	}
		    	// -- End Damage Dealt Rewards -- //
    		}
    		String mobName = mob.getTemplate().getName().toLowerCase();
    		String mobType = mob.getPrefixes();
    		long iron;
		    double cretStr = Bounty.getCreatureStrength(mob);
		    
    		if(Bounty.reward.containsKey(mobName)){
    			iron = Bounty.reward.get(mobName); // Prioritize hardcoded values in the Bounty.reward list first
    		}else{
    		    iron = java.lang.Math.round(cretStr); // Calculate bounty from creature strength if they do not exist in the reward list.
    		}
    		if(Servers.localServer.PVPSERVER){
    			if(!mob.isUnique() && mob.getTemplate().getTemplateId() != SpectralDrake.templateId && mob.getTemplate().getTemplateId() != Reaper.templateId){
    				iron *= 2.5d;
    			}
    			try {
					player.getSkills().getSkill(SkillList.MEDITATING).skillCheck(10, 0, false, 1); // Meditation skill gain
					float faithMod = 1-(player.getFaith()/200f);
					player.modifyFaith((((float)cretStr)*faithMod)/200000f); // Faith skill gain
				} catch (NoSuchSkillException e) {
					e.printStackTrace();
				}
    		}
    		
    		// Multiply bounty based on type
    		iron *= getTypeBountyMod(mob, mobType);
    		/*if(!mob.isUnique()){
	    		if (mobType.endsWith("fierce ")){
	    			iron *= 1.5;
	    		}else if (mobType.endsWith("angry ")){
	    			iron *= 1.4;
	    		}else if (mobType.endsWith("raging ")){
	    			iron *= 1.6;
	    		}else if (mobType.endsWith("slow ")){
	    			iron *= 0.95;
	    		}else if (mobType.endsWith("alert ")){
	    			iron *= 1.2;
	    		}else if (mobType.endsWith("greenish ")){
	    			iron *= 1.7;
	    		}else if (mobType.endsWith("lurking ")){
	    			iron *= 1.1;
	    		}else if (mobType.endsWith("sly ")){
	    			iron *= 0.8;
	    		}else if (mobType.endsWith("hardened ")){
	    			iron *= 1.3;
	    		}else if (mobType.endsWith("scared ")){
	    			iron *= 0.85;
	    		}else if (mobType.endsWith("diseased ")){
	    			iron *= 0.9;
	    		}else if (mobType.endsWith("champion ")){
	    			iron *= 2.0;
	    		}
    		}*/
    		
    		player.addMoney(iron);
		    Item inventory = player.getInventory();
    		String coinMessage = Economy.getEconomy().getChangeFor(iron).getChangeString();
    		String strBuilder = "You are awarded " + coinMessage;
		    if((mob.isAggHuman() || mob.isMonster()) && !mob.isUnique() && !Servers.localServer.PVPSERVER){
	    		Item creatureToken = ItemFactory.createItem(22765, 1+(99*random.nextFloat()), ""); // Creature Token ID: 22765
	    		inventory.insertItem(creatureToken);
	    		strBuilder += " and a "+creatureToken.getTemplate().getName();
		    }
		    strBuilder += " for slaying the "+mob.getName()+".";
			player.getCommunicator().sendSafeServerMessage(strBuilder);
    	}catch (NoSuchTemplateException | FailedException | IOException e) {
			e.printStackTrace();
		}
    } // checkPlayerReward
	
	public static void checkPlayerBounty(Player player, Creature creature){
		try {
			//Map<Long, Long> attackers = ReflectionUtil.getPrivateField(creature, ReflectionUtil.getField(creature.getClass(), "attackers"));
			Map<Long, Long> attackers = Bounty.getAttackers(creature);
			if(!Bounty.isCombatant(attackers, player.getWurmId()) || creature.isPlayer() || creature.isReborn()){
				return;
			}
			logger.info(player.getName()+" killed "+creature.getName());
			checkPlayerReward(player, creature);
		} catch (IllegalArgumentException | ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
