package mod.sin.actions.items;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillList;

import mod.sin.wyvern.Crystals;

public class CrystalCombineAction implements ModAction {
	private static Logger logger = Logger.getLogger(CrystalCombineAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;
	
	public CrystalCombineAction() {
		logger.log(Level.WARNING, "CrystalCombineAction()");

		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId,
			"Combine",
			"combining", 
			new int[]{ Actions.ACTION_TYPE_NOMOVE }
			//new int[] { 6 /* ACTION_TYPE_NOMOVE */ }	// 6 /* ACTION_TYPE_NOMOVE */, 48 /* ACTION_TYPE_ENEMY_ALWAYS */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */
		);
		ModActions.registerAction(actionEntry);
	}


	@Override
	public BehaviourProvider getBehaviourProvider()
	{
		return new BehaviourProvider() {
			// Menu with activated object
			@Override
			public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item object)
			{
				if(performer instanceof Player && source != null && object != null && Crystals.isCrystal(source) && Crystals.isCrystal(object) && source != object && source.getTemplateId() == object.getTemplateId()){
					return Collections.singletonList(actionEntry);
				}
				return null;
			}
		};
	}

	@Override
	public ActionPerformer getActionPerformer()
	{
		return new ActionPerformer() {
			
			@Override
			public short getActionId() {
				return actionId;
			}
			
			// With activated object
			@Override
			public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter)
			{
				try{
					if(performer instanceof Player){
						if(Crystals.shouldCancelCombine(performer, source, target)){
							return true;
						}
						if(counter == 1.0f){
							performer.getCommunicator().sendNormalServerMessage("You begin to combine the crystals together.");
							Server.getInstance().broadCastAction(performer.getName() + " begins combining crystals.", performer, 5);
							Skill combineSkill = performer.getSkills().getSkill(SkillList.MIND_LOGICAL);
							int time = Actions.getStandardActionTime(performer, combineSkill, source, 0d);
							act.setTimeLeft(time);
							performer.sendActionControl("Combining", true, act.getTimeLeft());
						}else if(counter * 10f > performer.getCurrentAction().getTimeLeft()){
							double diff = (source.getCurrentQualityLevel()+target.getCurrentQualityLevel())*0.3d;
							diff += source.getRarity()*20;
							diff -= performer.getSkills().getSkill(SkillList.MIND).getKnowledge();
							double power = performer.getSkills().getSkill(SkillList.SOUL).skillCheck(diff, source, 0d, false, 1);
							if(power > 0){
								performer.getCommunicator().sendNormalServerMessage("You successfully combine the crystals!");
								Server.getInstance().broadCastAction(performer.getName() + " successfully combines the crystals!", performer, 5);
								if(source.getCurrentQualityLevel() + target.getCurrentQualityLevel() > 100f){
									float newQuality = (source.getCurrentQualityLevel() + target.getCurrentQualityLevel()) - 100f;
									performer.getCommunicator().sendNormalServerMessage("The crystals combine and change, creating a more powerful component.");
									Items.destroyItem(source.getWurmId());
									target.setQualityLevel(newQuality);
									target.setDamage(0);
									target.setRarity((byte) (target.getRarity()+1));
								}else{
									performer.getCommunicator().sendNormalServerMessage("The crystals combine together and reinforce.");
									target.setQualityLevel(source.getCurrentQualityLevel() + target.getCurrentQualityLevel());
									target.setDamage(0);
									Items.destroyItem(source.getWurmId());
								}
								return true;
							}else{
								performer.getCommunicator().sendNormalServerMessage("You fail to combine the crystals, damaging them both.");
								float dam1 = (float) (Server.rand.nextFloat()*power*0.2f);
								float dam2 = (float) (Server.rand.nextFloat()*power*0.2f);
								source.setDamage((float) (source.getDamage()-dam1));
								target.setDamage((float) (target.getDamage()-dam2));
							}
							return true;
						}
					}else{
						logger.info("Somehow a non-player activated a custom item ("+source.getTemplateId()+")...");
					}
					return false;
				}catch(Exception e){
					e.printStackTrace();
					return true;
				}
			}
			
	
		}; // ActionPerformer
	}
}