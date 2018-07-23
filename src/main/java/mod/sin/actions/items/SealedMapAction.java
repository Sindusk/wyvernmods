package mod.sin.actions.items;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mod.sin.items.KeyFragment;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.pveplands.treasurehunting.Treasuremap;
import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;

import mod.sin.items.SealedMap;

public class SealedMapAction implements ModAction {
	private static Logger logger = Logger.getLogger(SealedMapAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;

	public SealedMapAction() {
		logger.log(Level.WARNING, "TreasureCacheOpenAction()");

		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId,
			"Unseal",
			"unsealing",
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
				if(performer instanceof Player && object != null && object.getTemplateId() == SealedMap.templateId) {
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
						if(target.getTemplateId() != SealedMap.templateId){
							performer.getCommunicator().sendNormalServerMessage("That is not a sealed map.");
							return true;
						}
						if(target.getLastOwnerId() != performer.getWurmId() && target.getOwnerId() != performer.getWurmId()){
							performer.getCommunicator().sendNormalServerMessage("You must own the "+target.getName()+" to open it.");
							return true;
						}
						if(counter == 1.0f){
							performer.getCommunicator().sendNormalServerMessage("You begin to unseal the "+target.getName()+".");
							Server.getInstance().broadCastAction(performer.getName() + " begins unsealing "+performer.getHisHerItsString()+" "+target.getName()+".", performer, 5);
							act.setTimeLeft(50);
							performer.sendActionControl("Unsealing", true, act.getTimeLeft());
						}else if(counter * 10f > performer.getCurrentAction().getTimeLeft()){
							performer.getCommunicator().sendNormalServerMessage("You unseal your "+target.getName()+".");
							Server.getInstance().broadCastAction(performer.getName() + " unseals "+performer.getHisHerItsString()+" "+target.getName()+".", performer, 5);
							logger.info("Player "+performer.getName()+" unsealed "+target.getName()+" at quality "+target.getCurrentQualityLevel()+" and rarity "+target.getRarity());
							target.setData1(100);
							Item map = Treasuremap.CreateTreasuremap(performer, target, null, null, true);
							performer.getInventory().insertItem(map, true);
							Items.destroyItem(target.getWurmId());
							return true;
						}
					}else{
						logger.info("Somehow a non-player activated a Treasure Box...");
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