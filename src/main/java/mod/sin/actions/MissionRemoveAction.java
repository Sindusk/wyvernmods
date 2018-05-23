package mod.sin.actions;

import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.epic.EpicMission;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.players.Player;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MissionRemoveAction implements ModAction {
	private static Logger logger = Logger.getLogger(MissionRemoveAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;

	public MissionRemoveAction() {
		logger.log(Level.WARNING, "UnequipAllAction()");

		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId,
			"Remove Epic Mission",
			"removing",
			new int[] { 0 }
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
				return this.getBehavioursFor(performer, object);
			}

			// Menu without activated object
			@Override
			public List<ActionEntry> getBehavioursFor(Creature performer, Item object)
			{
				if(performer instanceof Player && object != null && (object.getTemplateId() == ItemList.bodyBody || object.getTemplateId() == ItemList.bodyHand) && performer.getPower() >= 5) {
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
					if(performer.getPower() < 5){
						performer.getCommunicator().sendNormalServerMessage("You do not have permission to do that.");
						return true;
					}
					Player player = (Player) performer;
					EpicMission[] missions = EpicServerStatus.getCurrentEpicMissions();
					int i = 0;
					while(i < missions.length){
						if(missions[i].isCurrent()){
                            try {
                                int entityId = missions[i].getEpicEntityId();
                                logger.info("Removing mission for "+Deities.getDeityName(entityId)+".");
                                ReflectionUtil.callPrivateMethod(EpicServerStatus.class, ReflectionUtil.getMethod(EpicServerStatus.class, "destroyLastMissionForEntity"), entityId);
                                break;
                            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                                e.printStackTrace();
                            }
						}
						i++;
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