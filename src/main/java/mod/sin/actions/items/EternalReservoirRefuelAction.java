package mod.sin.actions.items;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import mod.sin.items.Soul;
import mod.sin.items.EternalReservoir;

public class EternalReservoirRefuelAction implements ModAction {
	private static Logger logger = Logger.getLogger(EternalReservoirRefuelAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;
	
	public EternalReservoirRefuelAction() {
		logger.log(Level.WARNING, "SoulForgeRefuelAction()");

		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId,
			"Feed soul",
			"feeding", 
			new int[]{
					Actions.ACTION_TYPE_IGNORERANGE
				}
			
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
				if(performer instanceof Player && source != null && object != null && source.getTemplateId() == Soul.templateId && object.getTemplateId() == EternalReservoir.templateId){
					return Arrays.asList(actionEntry);
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
						Player player = (Player) performer;
						if(target.getTemplate().getTemplateId() != EternalReservoir.templateId){
							player.getCommunicator().sendNormalServerMessage("That is not an eternal reservoir.");
							return true;
						}
						if(source.getTemplate().getTemplateId() != Soul.templateId){
							player.getCommunicator().sendNormalServerMessage("You can only use souls to refuel the eternal reservoir.");
							return true;
						}
						if(!performer.isWithinDistanceTo(target, 5)){
							player.getCommunicator().sendNormalServerMessage("You are too far away to fuel the reservoir.");
			                return true;
						}
						Skill stealing = null;
						Skill soulStrength = null;
						int time = 100;
						if(counter == 1.0f){
							performer.getCommunicator().sendNormalServerMessage("You begin to feed the "+source.getName()+" to the "+target.getName()+".");
							Server.getInstance().broadCastAction(performer.getName() + " begins to feed a "+source.getName()+" to the "+target.getName()+".", performer, 5);
							stealing = performer.getStealSkill();
							time = Actions.getSlowActionTime(performer, stealing, source, 0d);
							act.setTimeLeft(time);
							performer.sendActionControl("Feeding", true, act.getTimeLeft());
						}else if(counter * 10f > performer.getCurrentAction().getTimeLeft()){
							soulStrength = performer.getSoulStrength();
							double power = soulStrength.skillCheck(25f-(source.getCurrentQualityLevel()*0.2f), source, 0f, false, 1);
							if(power > 0){
								target.setData1((int) (target.getData1()+power));
								performer.getCommunicator().sendNormalServerMessage("You feed the "+source.getName()+" to the "+target.getName()+".");
								Server.getInstance().broadCastAction(performer.getName() + " feeds a "+source.getName()+" to the "+target.getName()+".", performer, 5);
							}else{
								performer.getCommunicator().sendNormalServerMessage("You fail to feed the "+source.getName()+" to the "+target.getName()+".");
								Server.getInstance().broadCastAction(performer.getName() + " fails to feed a "+source.getName()+" to the "+target.getName()+".", performer, 5);
							}
							Items.destroyItem(source.getWurmId());
							return true;
						}
					}else{
						logger.info("Somehow a non-player activated an Enchant Orb...");
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