package mod.sin.wyvern.mastercraft;

import com.wurmonline.server.Server;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Titles;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillList;
import javassist.*;
import javassist.bytecode.BadBytecode;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.Objects;
import java.util.logging.Logger;

public class Mastercraft {
	private static Logger logger = Logger.getLogger(Mastercraft.class.getName());
	public static double getNewDifficulty(Skill skill, double diff, Item item){
		if(skill.affinity > 0){
			diff -= skill.affinity;
		}
		if(skill.getKnowledge() > 99.0d){
			diff -= 2d-((100d-skill.getKnowledge())*2d);
		}
		if(skill.getKnowledge() > 90.0d){
			diff -= 2d-((100d-skill.getKnowledge())*0.2d);
		}
		if(item != null){
			if(item.getRarity() > 0){
				diff -= item.getRarity();
			}
			if(item.getCurrentQualityLevel() > 99.0f){
				diff -= 1d-((100d-item.getCurrentQualityLevel())*1d);
			}
			if(item.getCurrentQualityLevel() > 90.0f){
				diff -= 1d-((100d-item.getCurrentQualityLevel())*0.1d);
			}
		}
		return diff;
	}
	public static float getCastPowerIncrease(Skill skill){
		double addedPower = 0;
		if(skill.affinity > 0){
			addedPower += 2*skill.affinity;
		}
		if(skill.getKnowledge() > 0){
			float lowFloat1 = Math.min(Server.rand.nextFloat(), Server.rand.nextFloat());
			float lowFloat2 = Math.min(Server.rand.nextFloat(), Server.rand.nextFloat());
			addedPower += Math.min(skill.getKnowledge()*lowFloat1, skill.getKnowledge()*lowFloat2);
		}else{
			logger.warning("Error: Some player just tried casting with no channeling skill!");
		}
		return (float) addedPower;
	}
	public static float getFavorCostMultiplier(Skill skill){
		float mult = 1f;
		if(skill.affinity > 0){
			mult -= skill.affinity*0.02f; //2% reduction per affinity
		}
		if(skill.getKnowledge() > 90d){
			mult -= 0.1d-((100d-skill.getKnowledge())*0.01d);
		}
		if(skill.getKnowledge() > 99d){
			mult -= 0.1d-((100-skill.getKnowledge())*0.1d);
		}
		return mult;
	}
	/*public static void addNewTitles(){
        try {
            ExtendTitleEnum.builder("com.wurmonline.server.players.Titles$Title");
            // GM/Developer Titles
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Game_Master", 2500, "Game Master", "Game Master", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Developer", 2501, "Developer", "Developer", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Pet_Me", 2502, "Pet Me", "Pet Me", -1, "NORMAL");
            
            // Troll Titles
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Macro_King", 550, "Macro King", "Macro King", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Drama_Queen", 551, "Drama Queen", "Drama Queen", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Zergling", 552, "Zergling", "Zergling", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Special_Title", 553, "Special Guy", "Special Girl", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Prophet_Ear", 554, "Prophet Ear", "Prophet Ear", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Koza", 555, "Koza", "Koza", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Wyvern_Hunter", 556, "Wyvern Hunter", "Wyvern Hunter", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Overlord", 557, "Overlord", "Overlord", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Troll", 558, "Troll", "Troll", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Beggar", 559, "Beggar", "Beggar", -1, "NORMAL");
            
            // Contest Titles
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Home_Decorator", 600, "Home Decorator", "Home Decorator", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Arena_Champion", 601, "Champion of the Arena", "Champion of the Arena", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Pastamancer", 602, "Pastamancer", "Pastamancer", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Pizzamancer", 603, "Pizzamancer", "Pizzamancer", -1, "NORMAL");
            
            // Special Event Titles
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Titan_Slayer", 700, "Titanslayer", "Titanslayer", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Spectral_Slayer", 701, "Spectral Warrior", "Spectral Warrior", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Holdstrong_Architect", 702, "Holdstrong Architect", "Holdstrong Architect", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Stronghold_Architect", 703, "Stronghold Architect", "Stronghold Architect", -1, "NORMAL");

            // Donation titles
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Donator", 800, "Donator", "Donator", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Pazza_FavoriteGM", 801, "Sindusks Favourite GM", "Sindusks Favourite GM", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Warriorgen_ThatGuy", 802, "That Guy", "That Guy", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Eternallove_WarriorgensWife", 803, "Warriorgens Wife", "Warriorgens Wife", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Bambam_ThornOne", 804, "Thorn One", "Thorn One", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Svenja_CareDependant", 805, "The care-dependent", "The care-dependent", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Alexia_TheTreasuring", 806, "The Treasuring", "The Treasuring", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Reevi_ScienceGuy", 807, "Science Guy", "Science Guy", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Genocide_GrandDesigner", 808, "Grand Designer", "Grand Designer", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Seleas_CrazyCatLord", 809, "The Crazy Cat Lord", "The Crazy Cat Lord", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Piratemax_Slave", 810, "Slave", "Slave", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Eltacolad_TrueTaco", 811, "The One True Taco", "The One True Taco", -1, "NORMAL");

            // Skill Titles (100)
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Archery_Legendary", 1500, "Legendary Marksman", "Legendary Marksman", 1030, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Body_Legendary", 1501, "Hercules", "Hercules", 1, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Axes_Legendary", 1502, "Viking", "Viking", 1003, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Baking_Legendary", 1503, "Patissier", "Patissier", 10039, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Archaeology_Legendary", 1504, "Curator", "Curator", 10069, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("CarvingKnife_Legendary", 1505, "Woodsculptor", "Woodsculptor", 10007, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Taming_Legendary", 1506, "King of the Jungle", "Queen of the Jungle", 10078, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Climbing_Legendary", 1507, "Moonwalker", "Moonwalker", 10073, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Tracking_Legendary", 1508, "Bloodhound", "Bloodhound", 10018, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Clubs_Legendary", 1509, "Bam Bam", "Bam Bam", 1025, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Catapults_Legendary", 1510, "Castle Crasher", "Castle Crasher", 10077, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Firemaking_Legendary", 1511, "Incendiary", "Incendiary", 1010, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Gardening_Legendary", 1512, "Earthbound", "Earthbound", 10045, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Hammers_Legendary", 1513, "Doomhammer", "Doomhammer", 1027, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Locksmithing_Legendary", 1514, "Vault Smith", "Vault Smith", 10034, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Religion_Legendary", 1515, "Chosen", "Chosen", 1026, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Yoyo_Legendary", 1516, "String Theorist", "String Theorist", 10050, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Nature_Legendary", 1517, "Naturalist", "Naturalist", 1019, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Mind_Legendary", 1518, "Enlightened", "Enlightened", 2, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Mauls_Legendary", 1519, "Breaker", "Breaker", 1004, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Shipbuilding_Legendary", 1520, "Naval Engineer", "Naval Engineer", 10082, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("NaturalSubstances_Legendary", 1521, "Biochemist", "Biochemist", 10042, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("WarMachines_Legendary", 1522, "Eradicator", "Eradicator", 1029, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Thievery_Legendary", 1523, "Shadow", "Shadow", 1028, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Swords_Legendary", 1524, "Samurai", "Samurai", 1000, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Forestry_Legendary", 1525, "Silvanus", "Mother Nature", 10048, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().ExtendEnumEntries();

        } catch (BadBytecode | ClassNotFoundException | NotFoundException | CannotCompileException e) {
            logger.warning(e.getMessage());
        }
    }*/

	public static void changeExistingTitles(){
		for (Titles.Title title : Titles.Title.values()) {
		    if (Objects.equals("Pumpkin King", title.getFemaleName())){
		        try {
					ReflectionUtil.setPrivateField(title,  ReflectionUtil.getField(title.getClass(), "femaleName"), "Pumpkin Queen");
				} catch (IllegalArgumentException | IllegalAccessException | ClassCastException | NoSuchFieldException e) {
					e.printStackTrace();
				}
		    }
		}
	}
	public static void preInit(){
    	try {
    		ClassPool classPool = HookManager.getInstance().getClassPool();
    		Class<Mastercraft> thisClass = Mastercraft.class;
    		
    		// - Reduce skill check difficulty with high skills or tools - //
    		CtClass ctSkill = classPool.get("com.wurmonline.server.skills.Skill");
    		
			/*ctSkill.getDeclaredMethod("checkAdvance").insertBefore(""
					+ "$1 = "+Mastercraft.class.getName()+".getNewDifficulty(this, $1, $2);");*/
			Util.setReason("Modify difficulty for skill checks in MasterCraft.");
			String replace = "$1 = "+Mastercraft.class.getName()+".getNewDifficulty(this, $1, $2);";
			Util.insertBeforeDeclared(thisClass, ctSkill, "checkAdvance", replace);
			
			// - Increase spellcasting power for skilled channelers - //
			CtClass ctSpell = classPool.get("com.wurmonline.server.spells.Spell");
			CtMethod[] ctRuns = ctSpell.getDeclaredMethods("run");
			for(CtMethod method : ctRuns){
	            method.instrument(new ExprEditor(){
	                public void edit(MethodCall m) throws CannotCompileException {
	                    if (m.getMethodName().equals("doEffect")) {
	                        m.replace("$2 += "+Mastercraft.class.getName()+".getCastPowerIncrease(castSkill);"
	                        		+ "$_ = $proceed($$);");
	                        logger.info("Instrumented doEffect in run()");
	                    }
	                }
	            });
	            method.instrument(new ExprEditor(){
	                public void edit(MethodCall m) throws CannotCompileException {
	                    if (m.getMethodName().equals("depleteFavor")) {
	                        m.replace("$1 *= "+Mastercraft.class.getName()+".getFavorCostMultiplier(castSkill);"
	                        		+ "$_ = $proceed($$);");
	                        logger.info("Instrumented depleteFavor in run()");
	                    }
	                }
	            });
			}
		} catch (CannotCompileException | NotFoundException e) {
			e.printStackTrace();
		}
	}
}
