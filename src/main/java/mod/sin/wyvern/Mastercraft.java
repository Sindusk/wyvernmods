package mod.sin.wyvern;

import com.wurmonline.server.Server;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Titles;
import com.wurmonline.server.skills.Skill;
import javassist.*;
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
		if(WyvernMods.affinityDifficultyBonus && skill.affinity > 0){
			diff -= skill.affinity;
		}
		if(WyvernMods.legendDifficultyBonus && skill.getKnowledge() > 99.0d){
			diff -= 2d-((100d-skill.getKnowledge())*2d);
		}
		if(WyvernMods.masterDifficultyBonus && skill.getKnowledge() > 90.0d){
			diff -= 2d-((100d-skill.getKnowledge())*0.2d);
		}
		if(item != null){
			if(WyvernMods.itemRarityDifficultyBonus && item.getRarity() > 0){
				diff -= item.getRarity();
			}
			if(WyvernMods.legendItemDifficultyBonus && item.getCurrentQualityLevel() > 99.0f){
				diff -= 1d-((100d-item.getCurrentQualityLevel())*1d);
			}
			if(WyvernMods.masterItemDifficultyBonus && item.getCurrentQualityLevel() > 90.0f){
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

	public static void preInit(){
    	try {
    		ClassPool classPool = HookManager.getInstance().getClassPool();
    		Class<Mastercraft> thisClass = Mastercraft.class;
    		
    		// - Reduce skill check difficulty with high skills or tools - //
    		CtClass ctSkill = classPool.get("com.wurmonline.server.skills.Skill");

    		if (WyvernMods.enableDifficultyAdjustments) {
				Util.setReason("Modify difficulty for skill checks in MasterCraft.");
				String replace = "$1 = " + Mastercraft.class.getName() + ".getNewDifficulty(this, $1, $2);";
				Util.insertBeforeDeclared(thisClass, ctSkill, "checkAdvance", replace);
			}
			
			// - Increase spellcasting power for skilled channelers - //
			CtClass ctSpell = classPool.get("com.wurmonline.server.spells.Spell");
			CtMethod[] ctRuns = ctSpell.getDeclaredMethods("run");
			for(CtMethod method : ctRuns){
				if (WyvernMods.empoweredChannelers) {
					method.instrument(new ExprEditor() {
						public void edit(MethodCall m) throws CannotCompileException {
							if (m.getMethodName().equals("doEffect")) {
								m.replace("$2 += " + Mastercraft.class.getName() + ".getCastPowerIncrease(castSkill);"
										+ "$_ = $proceed($$);");
								logger.info("Instrumented doEffect in run()");
							}
						}
					});
				}
				if (WyvernMods.channelSkillFavorReduction) {
					method.instrument(new ExprEditor() {
						public void edit(MethodCall m) throws CannotCompileException {
							if (m.getMethodName().equals("depleteFavor")) {
								m.replace("$1 *= " + Mastercraft.class.getName() + ".getFavorCostMultiplier(castSkill);"
										+ "$_ = $proceed($$);");
								logger.info("Instrumented depleteFavor in run()");
							}
						}
					});
				}
			}
		} catch (CannotCompileException | NotFoundException e) {
			e.printStackTrace();
		}
	}
}
