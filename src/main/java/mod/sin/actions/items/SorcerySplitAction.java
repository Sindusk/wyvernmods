package mod.sin.actions.items;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.BookConversionQuestion;
import mod.sin.items.BookOfConversion;
import mod.sin.wyvern.util.ItemUtil;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class SorcerySplitAction implements ModAction {
	private static Logger logger = Logger.getLogger(SorcerySplitAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;

	public SorcerySplitAction() {
		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId,
			"Split sorcery",
			"splitting",
			new int[] { 0 /* ACTION_TYPE_NOMOVE */ }	// 6 /* ACTION_TYPE_NOMOVE */, 48 /* ACTION_TYPE_ENEMY_ALWAYS */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */
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
				if(performer instanceof Player && object != null && ItemUtil.isSorcery(object) && object.getAuxData() < 2) {
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
				if(performer instanceof Player){
					Player player = (Player) performer;
					if(!ItemUtil.isSorcery(target)){
						player.getCommunicator().sendNormalServerMessage("You can only split a sorcery.");
						return true;
					}
					if(target.getAuxData() >= 2){
						player.getCommunicator().sendNormalServerMessage("The sorcery must have at least two charges to split.");
						return true;
					}
                    if(target.getOwnerId() != player.getWurmId()){
					    player.getCommunicator().sendNormalServerMessage("You must be holding the sorcery to split it.");
                        return true;
                    }
					while(target.getAuxData() < 2){
                        try {
                            Item newSorcery = ItemFactory.createItem(target.getTemplateId(), target.getCurrentQualityLevel(), null);
                            newSorcery.setAuxData((byte) 2);
                            player.getInventory().insertItem(newSorcery, true);
                            target.setAuxData((byte) (target.getAuxData()+1));
                        } catch (FailedException | NoSuchTemplateException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
				}else{
					logger.info("Somehow a non-player activated a "+target.getName()+"...");
				}
				return true;
			}
			
			@Override
			public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter)
			{
				return this.action(act, performer, target, action, counter);
			}
			
	
		}; // ActionPerformer
	}
}