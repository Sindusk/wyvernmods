package mod.sin.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.players.Player;

public class ArenaTeleportAction implements ModAction {
	private static Logger logger = Logger.getLogger(ArenaTeleportAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;

	public ArenaTeleportAction() {
		logger.log(Level.WARNING, "ArenaTeleport()");

		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId, 
			"Teleport to Arena", 
			"preparing for combat", 
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
				if(performer instanceof Player && object != null && (object.getTemplateId() == ItemList.bodyBody || object.getTemplateId() == ItemList.bodyHand) && !Servers.localServer.PVPSERVER) {
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
						if(Servers.localServer.PVPSERVER){
							performer.getCommunicator().sendNormalServerMessage("You cannot enter the arena from here.");
							return true;
						}
						if(counter == 1.0f){
							performer.getCommunicator().sendNormalServerMessage("You sit and begin to transfer your mind.");
							performer.playAnimation("meditate", false);
							act.setTimeLeft(600);
							performer.sendActionControl("Transferring", true, act.getTimeLeft());
						}else if(act.currentSecond() == 10){
							performer.getCommunicator().sendAlertServerMessage("You are about to enter a full PvP environment.", (byte) 3);
						}else if(act.currentSecond() == 20){
							performer.getCommunicator().sendNormalServerMessage("Death will not drop your items. Instead they will be damaged heavily and returned to you upon respawn.", (byte) 3);
						}else if(act.currentSecond() == 30){
							performer.getCommunicator().sendNormalServerMessage("If you are part of a deed, you will arrive at the token. Otherwise, you will be placed in a random location.", (byte) 3);
						}else if(act.currentSecond() == 40){
							performer.getCommunicator().sendNormalServerMessage("You can equip creatures such as horses by simply leading them. Taming is not required in the Arena.", (byte) 3);
						}else if(act.currentSecond() == 55){
							performer.getCommunicator().sendNormalServerMessage("It appears you have accepted these conditions. Transferring to the Revenant Arena. Good luck.", (byte) 3);
						}else if(counter * 10f > performer.getCurrentAction().getTimeLeft()){
							ServerEntry targetserver = Servers.localServer.serverNorth;
							if(targetserver == null){
			                    performer.getCommunicator().sendNormalServerMessage("Error: Something went wrong [TARGETSERVER=NULL].");
			                    return true;
							}
							if (!targetserver.isAvailable(performer.getPower(), true)) {
			                    performer.getCommunicator().sendNormalServerMessage(targetserver.name + " is not currently available.");
			                    return true;
			                }
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