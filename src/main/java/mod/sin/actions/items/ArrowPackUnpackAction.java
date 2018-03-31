package mod.sin.actions.items;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.spells.SpellEffect;

import mod.sin.items.ArrowPackHunting;
import mod.sin.items.ArrowPackWar;

public class ArrowPackUnpackAction implements ModAction {
	private static Logger logger = Logger.getLogger(ArrowPackUnpackAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;

	public ArrowPackUnpackAction() {
		logger.log(Level.WARNING, "ArrowPackUnpackAction()");

		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId,
			"Unpack arrows",
			"unpacking",
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
				if(performer instanceof Player && object != null && (object.getTemplateId() == ArrowPackHunting.templateId || object.getTemplateId() == ArrowPackWar.templateId)) {
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
						if(target.getTemplate().getTemplateId() != ArrowPackHunting.templateId && target.getTemplate().getTemplateId() != ArrowPackWar.templateId){
							performer.getCommunicator().sendNormalServerMessage("That is not an arrow pack.");
							return true;
						}
						if(target.getLastOwnerId() != performer.getWurmId() && target.getOwnerId() != performer.getWurmId()){
							performer.getCommunicator().sendNormalServerMessage("You must own the arrow pack to unpack it.");
							return true;
						}
						if(counter == 1.0f){
							performer.getCommunicator().sendNormalServerMessage("You begin to unpack your arrows.");
							Server.getInstance().broadCastAction(performer.getName() + " begins unpacking "+performer.getHisHerItsString()+" arrows.", performer, 5);
							act.setTimeLeft(50);
							performer.sendActionControl("Unpacking", true, act.getTimeLeft());
						}else if(counter * 10f > performer.getCurrentAction().getTimeLeft()){
							int arrowTemplate = ItemList.arrowHunting;
							if(target.getTemplate().getTemplateId() == ArrowPackWar.templateId){
								arrowTemplate = ItemList.arrowWar;
							}
							float quality = target.getCurrentQualityLevel();
							if(target.getRarity() > 0){
								quality = Math.min(100f, target.getCurrentQualityLevel()+target.getRarity());
							}
							HashMap<Byte, Float> spellEffects = new HashMap<Byte, Float>();
							ItemSpellEffects effs = target.getSpellEffects();
							if(effs != null){
								for(SpellEffect eff : effs.getEffects()){
									if(eff.getPower() > 0){
										spellEffects.put(eff.type, eff.getPower());
									}
								}
							}
							Item quiver = ItemFactory.createItem(ItemList.quiver, quality, performer.getName());
							performer.getInventory().insertItem(quiver, true);
							Item arrow;
							int i = 0;
							while(i < 40){
								arrow = ItemFactory.createItem(arrowTemplate, quality, performer.getName());
								arrow.setRarity(target.getRarity());
								if(!spellEffects.isEmpty()){
									for(byte b : spellEffects.keySet()){
										ItemSpellEffects arrowEffs = arrow.getSpellEffects();
								        if (arrowEffs == null) {
								            arrowEffs = new ItemSpellEffects(arrow.getWurmId());
								        }
								        SpellEffect arrowEff = new SpellEffect(arrow.getWurmId(), b, spellEffects.get(b), 20000000);
								        arrowEffs.addSpellEffect(arrowEff);
									}
								}
								quiver.insertItem(arrow);
								i++;
							}
							performer.getCommunicator().sendNormalServerMessage("You unpack your arrows into a usable quiver.");
							Server.getInstance().broadCastAction(performer.getName() + " unpacks "+performer.getHisHerItsString()+" arrows into a quiver.", performer, 5);
							Items.destroyItem(target.getWurmId());
							return true;
						}
					}else{
						logger.info("Somehow a non-player activated a Arrow Pack Unpack...");
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