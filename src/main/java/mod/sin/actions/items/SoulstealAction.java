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
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import mod.sin.items.Soul;

public class SoulstealAction implements ModAction {
	private static Logger logger = Logger.getLogger(SoulstealAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;
	
	public SoulstealAction() {
		logger.log(Level.WARNING, "SoulstealAction()");

		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId,
			"Soulsteal",
			"soulstealing", 
			new int[]{
					Actions.ACTION_TYPE_IGNORERANGE
				}
			
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
				if(performer instanceof Player && source != null && object != null && source.getTemplateId() == ItemList.sacrificialKnife && object.getTemplateId() == ItemList.corpse){
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
			
			// With activated object
			@Override
			public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter)
			{
				try{
					if(performer instanceof Player){
						Player player = (Player) performer;
						if(source.getTemplate().getTemplateId() != ItemList.sacrificialKnife){
							player.getCommunicator().sendNormalServerMessage("You must use a sacrifical knife to steal souls.");
							return true;
						}
						if(target.getTemplate().getTemplateId() != ItemList.corpse){
							player.getCommunicator().sendNormalServerMessage("You can only steal the soul from a corpse.");
							return true;
						}
						if(target.getData1() == 1 && (target.getLastOwnerId() != performer.getWurmId()) && !Servers.isThisAPvpServer()) {
							player.getCommunicator().sendNormalServerMessage(Action.NOT_ALLOWED_ACTION_ON_FREEDOM_MESSAGE);
							return true;
						}
						if(target.isButchered()){
							player.getCommunicator().sendNormalServerMessage("The corpse has been butchered and there is no soul left.");
							return true;
						}
						if (target.getTopParentOrNull() != performer.getInventory() && !Methods.isActionAllowed(performer, (short) 120, target)) {
							player.getCommunicator().sendNormalServerMessage("You are not allowed to soulsteal that.");
			                return true;
			            }
						if(!performer.isWithinDistanceTo(target, 5)){
							player.getCommunicator().sendNormalServerMessage("You are too far away to steal that soul.");
			                return true;
						}
						Skill stealing = null;
						int time = 100;
						CreatureTemplate template = null;
						if(counter == 1.0f){
							template = CreatureTemplateFactory.getInstance().getTemplate(target.getData1());
							performer.getCommunicator().sendNormalServerMessage("You begin to steal the soul of the "+template.getName()+".");
							Server.getInstance().broadCastAction(performer.getName() + " begins to steal the "+template.getName()+" soul.", performer, 5);
							stealing = performer.getStealSkill();
							time = Actions.getStandardActionTime(performer, stealing, source, 0d);
							act.setTimeLeft(time);
							performer.sendActionControl("Soulstealing", true, act.getTimeLeft());
						}else if(counter * 10f > performer.getCurrentAction().getTimeLeft()){
							template = CreatureTemplateFactory.getInstance().getTemplate(target.getData1());
							stealing = performer.getStealSkill();
							double power = stealing.skillCheck(40f-(template.getBaseCombatRating()*0.4f), source, 0f, false, 1);
							if(power > 0){
								Item soul = ItemFactory.createItem(Soul.templateId, (float) power, performer.getName());
								soul.setName("Soul of "+template.getName());
								performer.getInventory().insertItem(soul, true);
								performer.getCommunicator().sendNormalServerMessage("You obtain the soul of the "+template.getName()+".");
								Server.getInstance().broadCastAction(performer.getName() + " obtains the soul of the "+template.getName()+".", performer, 5);
							}else{
								performer.getCommunicator().sendNormalServerMessage("You fail to steal the soul of the "+template.getName()+".");
								Server.getInstance().broadCastAction(performer.getName() + " fails to steal the soul of the "+template.getName()+".", performer, 5);
							}
							source.setDamage(source.getDamage()+(0.001f*source.getDamageModifier()));
							Items.destroyItem(target.getWurmId());
							return true;
						}
					}else{
						logger.info("Somehow a non-player activated an Enchant Orb...");
					}
					return false;
				}catch(Exception e){
					e.printStackTrace();
					return true;
				}
			}
			
	
		}; // ActionPerformer
	}
}