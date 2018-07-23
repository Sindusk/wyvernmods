package mod.sin.actions.items;

import com.wurmonline.server.Items;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.AffinityOrbQuestion;
import com.wurmonline.server.skills.Affinities;
import com.wurmonline.server.skills.Affinity;
import mod.sin.items.AffinityCatcher;
import mod.sin.items.AffinityOrb;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AffinityCatcherConsumeAction implements ModAction {
	private static Logger logger = Logger.getLogger(AffinityCatcherConsumeAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;

	public AffinityCatcherConsumeAction() {
		logger.log(Level.WARNING, "AffinityCatcherConsumeAction()");

		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId,
			"Gain affinity",
			"infusing",
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
				if(performer instanceof Player && object != null && object.getTemplateId() == AffinityCatcher.templateId && object.getData() > 0) {
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
					if (target.getTemplate().getTemplateId() != AffinityCatcher.templateId){
	                    player.getCommunicator().sendSafeServerMessage("You must use a captured affinity.");
	                    return true;
					}
					if(target.getData() <= 0){
						player.getCommunicator().sendSafeServerMessage("The catcher needs to have an affinity captured before being consumed.");
						return true;
					}
					int skillNum = (int) target.getData();
					Affinity[] affs = Affinities.getAffinities(performer.getWurmId());
                    for (Affinity affinity : affs) {
                        if (affinity.getSkillNumber() != skillNum) continue;
                        if (affinity.getNumber() >= 5){
                            player.getCommunicator().sendSafeServerMessage("You already have the maximum amount of affinities for that skill.");
                            return true;
                        }
                        Affinities.setAffinity(player.getWurmId(), skillNum, affinity.getNumber() + 1, false);
                        player.getCommunicator().sendSafeServerMessage("Your affinity grows stronger.");
                        Items.destroyItem(target.getWurmId());
                        return true;
                    }
                    // Has no affinity in this, so should give them one.
					Affinities.setAffinity(player.getWurmId(), skillNum, 1, false);
                    player.getCommunicator().sendSafeServerMessage("You obtain a new affinity.");
					Items.destroyItem(target.getWurmId());
					return true;
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