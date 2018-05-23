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
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;

import mod.sin.items.FriyanTablet;

public class FriyanTabletAction implements ModAction {
	private static Logger logger = Logger.getLogger(FriyanTabletAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;

	public FriyanTabletAction() {
		logger.log(Level.WARNING, "FriyanTabletAction()");

		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId,
			"Read",
			"reading",
			new int[] { Actions.ACTION_TYPE_NOMOVE }	// 6 /* ACTION_TYPE_NOMOVE */, 48 /* ACTION_TYPE_ENEMY_ALWAYS */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */
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
				return this.getBehavioursFor(performer, object);
			}

			// Menu without activated object
			@Override
			public List<ActionEntry> getBehavioursFor(Creature performer, Item object)
			{
				if(performer instanceof Player && object != null && object.getTemplateId() == FriyanTablet.templateId) {
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
			
			// Without activated object
			@Override
			public boolean action(Action act, Creature performer, Item target, short action, float counter)
			{
				try{
					if(performer instanceof Player){
						if (target.getTemplate().getTemplateId() != FriyanTablet.templateId){
							performer.getCommunicator().sendNormalServerMessage("You cannot read that.");
							return true;
						}
						if (!performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), performer.getPositionZ(), 4)) {
							performer.getCommunicator().sendNormalServerMessage("You are too far away to read.");
							act.stop(true);
							return true;
						}
						if (counter == 1.0f) {
							performer.getCommunicator().sendNormalServerMessage("You start to read the "+target.getName()+".");
							Server.getInstance().broadCastAction(performer.getName() + " begins reading the "+target.getName()+".", performer, 5);
							act.setTimeLeft(200);
							performer.sendActionControl("Reading", true, act.getTimeLeft());
						} else if (counter * 10.0f > performer.getCurrentAction().getTimeLeft()) {
							if(performer.getDeity() != null && performer.getFaith() > 0){
								performer.modifyFaith(Math.max(0.1f, Server.rand.nextFloat()/2f));
								performer.getCommunicator().sendNormalServerMessage("You are enthralled by the knowledge of the Kaen scholar. You feel closer to god than ever!");
								Items.destroyItem(target.getWurmId());
							}else{
								performer.getCommunicator().sendNormalServerMessage("You don't seem to learn anything. You don't believe in the gods.");
							}
							return true;
						}
					}else{
						logger.info("Somehow a non-player activated a "+target.getTemplate().getName()+".");
					}
					return false;
				}catch(Exception e){
					e.printStackTrace();
					return true;
				}
			}
			
			@Override
			public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter)
			{
				return this.action(act, performer, target, action, counter);
			}
			
	
		}; // ActionPerformer
	}
}