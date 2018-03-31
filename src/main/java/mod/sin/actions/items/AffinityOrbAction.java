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
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Affinities;
import com.wurmonline.server.skills.Affinity;
import com.wurmonline.server.skills.SkillSystem;

import mod.sin.items.AffinityOrb;

public class AffinityOrbAction implements ModAction {
	private static Logger logger = Logger.getLogger(AffinityOrbAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;

	public AffinityOrbAction() {
		logger.log(Level.WARNING, "AffinityOrbAction()");

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
				if(performer instanceof Player && object != null && object.getTemplateId() == AffinityOrb.templateId) {
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
					if (target.getTemplate().getTemplateId() != AffinityOrb.templateId){
	                    player.getCommunicator().sendSafeServerMessage("You must use an Affinity Orb to be infused.");
	                    return true;
					}
					int skillNum = SkillSystem.getRandomSkillNum();
		            Affinity[] affs = Affinities.getAffinities(player.getWurmId());
		            boolean found = false;
		            while (!found) {
		                boolean hasAffinity = false;
		                for (Affinity affinity : affs) {
		                    if (affinity.getSkillNumber() != skillNum) continue;
		                    hasAffinity = true;
		                    if (affinity.getNumber() >= 5) break;
		                    Affinities.setAffinity(player.getWurmId(), skillNum, affinity.getNumber() + 1, false);
		                    String skillString = SkillSystem.getNameFor(skillNum);
		                    found = true;
		                    Items.destroyItem(target.getWurmId());
		                    player.getCommunicator().sendSafeServerMessage("Vynora infuses you with an affinity for " + skillString + "!");
		                    break;
		                }
		                if (!found && !hasAffinity) {
		                	String skillString = SkillSystem.getNameFor(skillNum);
		                    Affinities.setAffinity(player.getWurmId(), skillNum, 1, false);
		                    Items.destroyItem(target.getWurmId());
		                    player.getCommunicator().sendSafeServerMessage("Vynora infuses you with an affinity for " + skillString + "!");
		                    found = true;
		                }
		                skillNum = SkillSystem.getRandomSkillNum();
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