package mod.sin.actions.items;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;

import mod.sin.items.EternalReservoir;

public class EternalReservoirCheckFuelAction implements ModAction {
	private static Logger logger = Logger.getLogger(EternalReservoirCheckFuelAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;

	public EternalReservoirCheckFuelAction() {
		logger.log(Level.WARNING, "EternalReservoirFuelCheckAction()");

		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId,
			"Check fuel",
			"checking",
			new int[0]
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
				if(performer instanceof Player && object != null && object.getTemplateId() == EternalReservoir.templateId) {
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
						if(target.getTemplateId() != EternalReservoir.templateId){
							performer.getCommunicator().sendNormalServerMessage("That is not an eternal reservoir.");
							return true;
						}
						if(!performer.isWithinDistanceTo(target, 9)){
							performer.getCommunicator().sendNormalServerMessage("You are too far away to check the fuel.");
			                return true;
						}
						int fuel = target.getData1();
						if(fuel < 30){
							performer.getCommunicator().sendNormalServerMessage("The "+target.getName()+" has no souls, and is inactive.");
						}else if(fuel < 1000){
							performer.getCommunicator().sendNormalServerMessage("The "+target.getName()+" is very low on souls.");
						}else if(fuel < 5000){
							performer.getCommunicator().sendNormalServerMessage("The "+target.getName()+" has some souls, but yearns for more.");
						}else if(fuel < 10000){
							performer.getCommunicator().sendNormalServerMessage("The "+target.getName()+" has a good amount of souls.");
						}else if(fuel < 50000){
							performer.getCommunicator().sendNormalServerMessage("The "+target.getName()+" has plenty of souls.");
						}else{
							performer.getCommunicator().sendNormalServerMessage("The "+target.getName()+" is absolutely flooded with souls, and will last a long time.");
						}
						return true;
					}else{
						logger.info("Somehow a non-player activated a Eternal Reservoir...");
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