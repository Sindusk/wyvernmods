package mod.sin.actions;

import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.players.Player;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArenaEscapeAction implements ModAction {
	private static Logger logger = Logger.getLogger(ArenaEscapeAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;

	public ArenaEscapeAction() {
		logger.log(Level.WARNING, "ArenaEscapeAction()");

		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId, 
			"Escape the Arena", 
			"escaping", 
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
				if(performer instanceof Player && object != null && (object.getTemplateId() == ItemList.bodyBody || object.getTemplateId() == ItemList.bodyHand) && Servers.localServer.PVPSERVER) {
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
						if(!Servers.localServer.PVPSERVER){
							performer.getCommunicator().sendNormalServerMessage("You must be in the Arena in order to escape it!");
							return true;
						}
						if(performer.isStealth()){
							performer.getCommunicator().sendNormalServerMessage("You cannot escape while stealthed.");
							return true;
						}
						if(performer.getEnemyPresense() > 0 || performer.isFighting()){
							performer.getCommunicator().sendNormalServerMessage("Nearby enemies prevent your escape. Clear the area and try again.");
							return true;
						}
						if(counter == 1.0f){
							performer.getCommunicator().sendNormalServerMessage("You prepare your body and mind to transfer to another realm.");
							act.setTimeLeft(1800);
							performer.sendActionControl("Preparing", true, act.getTimeLeft());
						}else if(counter * 10f > performer.getCurrentAction().getTimeLeft()){
							ServerEntry targetserver = Servers.localServer.serverSouth;
							if(targetserver == null){
			                    performer.getCommunicator().sendNormalServerMessage("Error: Something went wrong [TARGETSERVER=NULL].");
			                    return true;
							}
							if (!targetserver.isAvailable(performer.getPower(), true)) {
			                    performer.getCommunicator().sendNormalServerMessage(targetserver.name + " is not currently available.");
			                    return true;
			                }
							performer.getCommunicator().sendNormalServerMessage("You successfully escape the arena.");
			                performer.getCommunicator().sendNormalServerMessage("You transfer to " + targetserver.name + ".");
			                Server.getInstance().broadCastAction(performer.getName() + " transfers to " + targetserver.name + ".", performer, 5);
			                int tilex = 1010;
			                int tiley = 1010;
			                ((Player)performer).sendTransfer(Server.getInstance(), targetserver.INTRASERVERADDRESS, Integer.parseInt(targetserver.INTRASERVERPORT), targetserver.INTRASERVERPASSWORD, targetserver.id, tilex, tiley, true, false, performer.getKingdomId());
			                ((Player)performer).transferCounter = 30;
							return true;
						}
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