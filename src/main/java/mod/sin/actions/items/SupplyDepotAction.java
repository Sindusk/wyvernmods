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
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.players.Player;

import mod.sin.items.ArenaCache;
import mod.sin.items.ArenaSupplyDepot;
import mod.sin.wyvern.MiscChanges;
import mod.sin.wyvern.arena.SupplyDepots;

public class SupplyDepotAction implements ModAction {
	private static Logger logger = Logger.getLogger(SupplyDepotAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;

	public SupplyDepotAction() {
		logger.log(Level.WARNING, "SupplyDepotAction()");

		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId,
			"Capture depot",
			"capturing",
			new int[] { 6 /* ACTION_TYPE_NOMOVE */ }	// 6 /* ACTION_TYPE_NOMOVE */, 48 /* ACTION_TYPE_ENEMY_ALWAYS */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */
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
				if(performer instanceof Player && object != null && object.getTemplateId() == ArenaSupplyDepot.templateId) {
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
			
			// Without activated object
			@Override
			public boolean action(Action act, Creature performer, Item target, short action, float counter)
			{
				try{
					if(performer instanceof Player){
						if(target.getTemplate().getTemplateId() != ArenaSupplyDepot.templateId){
							performer.getCommunicator().sendNormalServerMessage("That is not a supply depot.");
							return true;
						}
						if(!performer.isWithinDistanceTo(target, 5)){
							performer.getCommunicator().sendNormalServerMessage("You must be closer to capture the depot.");
							return true;
						}
						if(!Items.exists(target)){
							performer.getCommunicator().sendNormalServerMessage("The supply depot has already been captured.");
							return true;
						}
						if(performer.getFightingSkill().getKnowledge() < 25f){
							performer.getCommunicator().sendNormalServerMessage("You must have at least 25 fighting skill to capture a depot.");
							return true;
						}
						if(counter == 1.0f){
							performer.getCommunicator().sendNormalServerMessage("You begin to capture the depot.");
							Server.getInstance().broadCastAction(performer.getName() + " begins capturing the depot.", performer, 50);
							act.setTimeLeft(2400);
							performer.sendActionControl("Capturing", true, act.getTimeLeft());
							SupplyDepots.maybeBroadcastOpen(performer);
						}else if(counter * 10f > performer.getCurrentAction().getTimeLeft()){
							Item inv = performer.getInventory();
							Item cache = ItemFactory.createItem(ArenaCache.templateId, 90+(10*Server.rand.nextFloat()), "");
							inv.insertItem(cache, true);
							performer.getCommunicator().sendSafeServerMessage("You have successfully captured the depot!");
							Server.getInstance().broadCastAction(performer.getName() + " successfully captures the depot!", performer, 50);
							MiscChanges.sendImportantMessage(performer, performer.getName()+" has claimed an Arena depot!", 255, 128, 0);
							SupplyDepots.removeSupplyDepot(target);
							Items.destroyItem(target.getWurmId());
							return true;
						}
					}else{
						logger.info("Somehow a non-player activated a Arrow Pack Unpack...");
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