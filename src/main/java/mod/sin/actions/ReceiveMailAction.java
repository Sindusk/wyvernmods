package mod.sin.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.WurmMail;
import com.wurmonline.server.players.Player;

public class ReceiveMailAction implements ModAction {
	private static Logger logger = Logger.getLogger(ReceiveMailAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;

	public ReceiveMailAction() {
		logger.log(Level.WARNING, "ReceiveMailAction()");

		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId,
			"Receive all mail",
			"receiving",
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
				if(performer instanceof Player && object != null && object.isMailBox() && object.getSpellCourierBonus() > 0f) {
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
				if(performer instanceof Player){
					Player player = (Player) performer;
					if (!target.isMailBox()){
	                    player.getCommunicator().sendSafeServerMessage("You can only receive mail at a mailbox.");
	                    return true;
					}
					if(target.getSpellCourierBonus() <= 0f){
	                    player.getCommunicator().sendSafeServerMessage("The mailbox must be enchanted before receiving mail.");
	                    return true;
					}
					if(!performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 4.0f)){
	                    player.getCommunicator().sendSafeServerMessage("You must be closer to collect mail.");
	                    return true;
					}
					if(!target.isEmpty(false)){
	                    player.getCommunicator().sendSafeServerMessage("Empty the mailbox first.");
	                    return true;
					}
					Set<WurmMail> mailset = WurmMail.getSentMailsFor(performer.getWurmId(), 100);
					if(mailset.isEmpty()){
	                    player.getCommunicator().sendSafeServerMessage("You have no mail to collect.");
	                    return true;
					}
					Iterator<WurmMail> it = mailset.iterator();
					WurmMail m;
					HashSet<Item> itemset = new HashSet<Item>();
					while(it.hasNext()){
						m = it.next();
						if(m.rejected || m.price > 0){
							continue;
						}
						try {
							itemset.add(Items.getItem(m.itemId));
						} catch (NoSuchItemException e) {
							e.printStackTrace();
						}
					}
					if(!itemset.isEmpty()){
						player.getCommunicator().sendSafeServerMessage("The "+itemset.size()+" items that were sent via mail are now available.");
						for (Item item : itemset) {
	                        Item[] contained4 = item.getAllItems(true);
	                        for (int c4 = 0; c4 < contained4.length; ++c4) {
	                            contained4[c4].setMailed(false);
	                            contained4[c4].setLastOwnerId(performer.getWurmId());
	                        }
	                        WurmMail.removeMail(item.getWurmId());
	                        target.insertItem(item, true);
	                        item.setLastOwnerId(performer.getWurmId());
	                        item.setMailed(false);
	                        logger.log(Level.INFO, performer.getName() + " received " + item.getName() + " " + item.getWurmId());
	                    }
					}
				}else{
					logger.info("Somehow a non-player activated an Affinity Orb...");
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