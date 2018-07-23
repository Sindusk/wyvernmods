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
	public static void addNewTitles(){
        try {
            ExtendTitleEnum.builder("com.wurmonline.server.players.Titles$Title");
            // GM/Developer Titles
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Game_Master", 500, "Game Master", "Game Master", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Developer", 501, "Developer", "Developer", -1, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Pet_Me", 502, "Pet Me", "Pet Me", -1, "NORMAL");
            
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

            // Characteristic Titles
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("MindLogic_Normal", 1000, "Logical", "Logical", 100, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("MindLogic_Minor", 1001, "Intelligent", "Intelligent", 100, "MINOR");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("MindLogic_Master", 1002, "Brilliant", "Brilliant", 100, "MASTER");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("MindLogic_Legendary", 1003, "Mentalist", "Mentalist", 100, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("MindSpeed_Normal", 1004, "Keen", "Keen", 101, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("MindSpeed_Minor", 1005, "Thinker", "Thinker", 101, "MINOR");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("MindSpeed_Master", 1006, "Clever", "Clever", 101, "MASTER");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("MindSpeed_Legendary", 1007, "Mind Over Matter", "Mind Over Matter", 101, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("BodyStrength_Normal", 1008, "Strong", "Strong", 102, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("BodyStrength_Minor", 1009, "Fortified", "Fortified", 102, "MINOR");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("BodyStrength_Master", 1010, "Unyielding", "Unyielding", 102, "MASTER");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("BodyStrength_Legendary", 1011, "Force of Nature", "Force of Nature", 102, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("BodyStamina_Normal", 1012, "Enduring", "Enduring", 103, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("BodyStamina_Minor", 1013, "Resilient", "Resilient", 103, "MINOR");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("BodyStamina_Master", 1014, "Vigorous", "Vigorous", 103, "MASTER");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("BodyStamina_Legendary", 1015, "Unstoppable", "Unstoppable", 103, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("BodyControl_Normal", 1016, "Nimble", "Nimble", 104, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("BodyControl_Minor", 1017, "Deft", "Deft", 104, "MINOR");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("BodyControl_Master", 1018, "Skillful", "Skillful", 104, "MASTER");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("BodyControl_Legendary", 1019, "Manipulator", "Manipulator", 104, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("SoulStrength_Normal", 1020, "Spirited", "Spirited", 105, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("SoulStrength_Minor", 1021, "Diviner", "Diviner", 105, "MINOR");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("SoulStrength_Master", 1022, "Anima", "Anima", 105, "MASTER");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("SoulStrength_Legendary", 1023, "Prophet", "Prophet", 105, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("SoulDepth_Normal", 1024, "Sensible", "Sensible", 106, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("SoulDepth_Minor", 1025, "Medium", "Medium", 106, "MINOR");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("SoulDepth_Master", 1026, "Spiritual", "Spiritual", 106, "MASTER");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("SoulDepth_Legendary", 1027, "Planewalker", "Planewalker", 106, "LEGENDARY");

            // Skill Titles (Full)
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Staff_Normal", 1100, "Acolyte", "Acolyte", 10090, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Staff_Minor", 1101, "Disciple", "Disciple", 10090, "MINOR");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Staff_Master", 1102, "Monk", "Monk", 10090, "MASTER");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Staff_Legendary", 1103, "Sensei", "Sensei", 10090, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Scythe_Normal", 1104, "Mower", "Mower", 10047, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Scythe_Minor", 1105, "Harvester", "Harvester", 10047, "MINOR");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Scythe_Master", 1106, "Scythian", "Scythian", 10047, "MASTER");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Scythe_Legendary", 1107, "Reaper", "Reaper", 10047, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Defensive_Normal", 1108, "Resistant", "Resistant", 10054, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Defensive_Minor", 1109, "Guardian", "Guardian", 10054, "MINOR");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Defensive_Master", 1110, "Bulwark", "Bulwark", 10054, "MASTER");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Defensive_Legendary", 1111, "Unbreakable", "Unbreakable", 10054, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Aggressive_Normal", 1112, "Angry", "Angry", 10053, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Aggressive_Minor", 1113, "Violent", "Violent", 10053, "MINOR");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Aggressive_Master", 1114, "Battleborn", "Battleborn", 10053, "MASTER");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Aggressive_Legendary", 1115, "Warmonger", "Warmonger", 10053, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Normal_Normal", 1116, "Infantry", "Infantry", 10055, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Normal_Minor", 1117, "Marauder", "Marauder", 10055, "MINOR");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Normal_Master", 1118, "Gladiator", "Gladiator", 10055, "MASTER");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Normal_Legendary", 1119, "Templar", "Templar", 10055, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Weaponless_Normal", 1120, "Scrapper", "Scrapper", 10052, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Weaponless_Minor", 1121, "Brawler", "Brawler", 10052, "MINOR");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Weaponless_Master", 1122, "Boxer", "Boxer", 10052, "MASTER");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("Weaponless_Legendary", 1123, "Martial Artist", "Martial Artist", 10052, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("BladesSmithing_Normal", 1124, "Bladesmith", "Bladesmith", SkillList.SMITHING_WEAPON_BLADES, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("BladesSmithing_Minor", 1125, "Renowned Bladesmith", "Renowned Bladesmith", SkillList.SMITHING_WEAPON_BLADES, "MINOR");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("BladesSmithing_Master", 1126, "Master Bladesmith", "Master Bladesmith", SkillList.SMITHING_WEAPON_BLADES, "MASTER");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("BladesSmithing_Legendary", 1127, "Legendary Bladesmith", "Legendary Bladesmith", SkillList.SMITHING_WEAPON_BLADES, "LEGENDARY");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("HeadSmithing_Normal", 1128, "Headsmither", "Headsmither", SkillList.SMITHING_WEAPON_HEADS, "NORMAL");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("HeadSmithing_Minor", 1129, "Renowned Headsmither", "Renowned Headsmither", SkillList.SMITHING_WEAPON_HEADS, "MINOR");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("HeadSmithing_Master", 1130, "Master Headsmither", "Master Headsmither", SkillList.SMITHING_WEAPON_HEADS, "MASTER");
            ExtendTitleEnum.getSingletonInstance().addExtendEntry("HeadSmithing_Legendary", 1131, "Legendary Headsmither", "Legendary Headsmither", SkillList.SMITHING_WEAPON_HEADS, "LEGENDARY");
            
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
    }
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
