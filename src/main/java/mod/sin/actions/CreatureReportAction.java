package mod.sin.actions;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.*;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.players.Player;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CreatureReportAction implements ModAction {
	private static Logger logger = Logger.getLogger(CreatureReportAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;

	public CreatureReportAction() {
		logger.log(Level.WARNING, "CreatureReportAction()");

		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId,
			"Creature Report",
			"reporting",
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
	private static void creatureReport(Communicator comm) {
		HashMap<CreatureTemplate, Integer> counts = new HashMap<>();
		Arrays.stream(CreatureTemplateFactory.getInstance().getTemplates()).forEach(ct -> counts.put(ct, 0));
		Arrays.stream(Creatures.getInstance().getCreatures()).forEach(cr -> counts.put(cr.getTemplate(), counts.get(cr.getTemplate()) + 1));
		Map<Boolean, List<Map.Entry<CreatureTemplate, Integer>>> tmp = counts.entrySet().stream().collect(Collectors.partitioningBy(e -> e.getKey().isMonster() || e.getKey().isAggHuman()));
		List<Map.Entry<CreatureTemplate, Integer>> aggro = tmp.get(true);
		List<Map.Entry<CreatureTemplate, Integer>> passive = tmp.get(false);
		aggro.sort((o1, o2) -> -Integer.compare(o1.getValue(), o2.getValue()));
		passive.sort((o1, o2) -> -Integer.compare(o1.getValue(), o2.getValue()));
		int aggroTotal = aggro.stream().mapToInt(Map.Entry::getValue).sum();
		int passiveTotal = passive.stream().mapToInt(Map.Entry::getValue).sum();
		int sum = aggroTotal + passiveTotal;
		comm.sendSystemMessage(String.format("Passive: %d (%.1f) Aggro: %d (%.1f) Total: %d", passiveTotal, 100.0 * passiveTotal / sum, aggroTotal, 100.0 * aggroTotal / sum, sum));
		comm.sendSystemMessage("== Aggressive ==");
		aggro.forEach(e -> comm.sendSystemMessage(String.format("%s: %d (%.1f)", e.getKey().getName(), e.getValue(), 100.0 * e.getValue() / aggroTotal)));
		comm.sendSystemMessage("== Passive ==");
		passive.forEach(e -> comm.sendSystemMessage(String.format("%s: %d (%.1f)", e.getKey().getName(), e.getValue(), 100.0 * e.getValue() / passiveTotal)));
		comm.sendSystemMessage("=============");
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
					creatureReport(performer.getCommunicator());
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