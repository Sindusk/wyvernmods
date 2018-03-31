package mod.sin.actions;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.players.Player;

public class VillageTeleportAction implements ModAction {
	private static Logger logger = Logger.getLogger(VillageTeleportAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;

	public VillageTeleportAction() {
		logger.log(Level.WARNING, "VillageTeleportAction()");

		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId, 
			"Village Teleport", 
			"teleporting", 
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
				if(performer instanceof Player && object != null && (object.getTemplateId() == ItemList.bodyBody || object.getTemplateId() == ItemList.bodyHand) && !Servers.localServer.PVPSERVER && performer.getCitizenVillage() != null) {
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
						if(Servers.localServer.PVPSERVER){
							performer.getCommunicator().sendNormalServerMessage("You cannot use Village Teleport on a PvP server.");
							return true;
						}
						if(counter == 1.0f){
							performer.getCommunicator().sendNormalServerMessage("You prepare to transport yourself to another location.");
							act.setTimeLeft(600);
							performer.sendActionControl("Teleporting", true, act.getTimeLeft());
						}else if(performer.isFighting()){
							performer.getCommunicator().sendAlertServerMessage("Your teleport was interrupted by entering combat.");
							return true;
						}else if(counter * 10f > performer.getCurrentAction().getTimeLeft()){
							Player player = (Player) performer;
							if(player.getCitizenVillage() != null){
								act.stop(false);
								int tilex = player.getCitizenVillage().getTokenX()*4;
								int tiley = player.getCitizenVillage().getTokenY()*4;
								player.setTeleportPoints(tilex+2.0f, tiley+2.0f, 0, 0);
								if(player.startTeleporting()){
									player.getCommunicator().sendNormalServerMessage("You feel a slight tingle in your spine.");
									player.getCommunicator().sendTeleport(false);
								}
							}
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