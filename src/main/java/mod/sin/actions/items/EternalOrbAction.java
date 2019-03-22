package mod.sin.actions.items;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.*;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.spells.Spells;
import mod.sin.items.EnchantOrb;
import mod.sin.items.EternalOrb;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EternalOrbAction implements ModAction {
	private static Logger logger = Logger.getLogger(EternalOrbAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;

	public EternalOrbAction() {
		logger.log(Level.WARNING, "EternalOrbAction()");

		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId,
			"Absorb enchants",
			"absorbing",
			new int[0]
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
				if(performer instanceof Player && source != null && object != null && source.getTemplateId() == EternalOrb.templateId && source != object){
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
			
			// With activated object
			@Override
			public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter)
			{
				if(performer instanceof Player){
					Player player = (Player) performer;
					if(source.getTemplate().getTemplateId() != EternalOrb.templateId){
						player.getCommunicator().sendNormalServerMessage("You must use an Eternal Orb to absorb enchants.");
						return true;
					}
					if(source.getWurmId() == target.getWurmId()){
						player.getCommunicator().sendNormalServerMessage("You cannot absorb the orb with itself!");
						return true;
					}
					if(target.getTemplateId() == ItemList.arrowHunting || target.getTemplateId() == ItemList.arrowWar){
						player.getCommunicator().sendNormalServerMessage("You cannot use Eternal Orbs on arrows.");
						return true;
					}
					ItemSpellEffects teffs = target.getSpellEffects();
					if(teffs == null || teffs.getEffects().length == 0){
						player.getCommunicator().sendNormalServerMessage("The "+target.getTemplate().getName()+" has no enchants.");
						return true;
					}
					for(SpellEffect eff : teffs.getEffects()){
					    if(eff.type == 120){
					        player.getCommunicator().sendNormalServerMessage("The "+eff.getName()+" enchant makes this item immune to the effects of the "+source.getName()+".");
					        return true;
                        }
                    }
					try {
						Item enchantOrb = ItemFactory.createItem(EnchantOrb.templateId, source.getCurrentQualityLevel(), "");
						ItemSpellEffects effs = enchantOrb.getSpellEffects();
						if(effs == null){
							effs = new ItemSpellEffects(enchantOrb.getWurmId());
						}
						for(SpellEffect teff : teffs.getEffects()){
							byte type = teff.type;
                            SpellEffect newEff = new SpellEffect(enchantOrb.getWurmId(), type, teff.getPower(), 20000000);
                            effs.addSpellEffect(newEff);
                            teffs.removeSpellEffect(type);
                            player.getCommunicator().sendSafeServerMessage("The "+teff.getName()+" transfers to the "+enchantOrb.getTemplate().getName()+".");
                            if(enchantOrb.getDescription().equals("")){
                                enchantOrb.setDescription(newEff.getName().substring(0,1)+String.format("%d", (int) newEff.getPower()));
                            }else{
                                enchantOrb.setDescription(enchantOrb.getDescription()+" "+newEff.getName().substring(0,1)+String.format("%d", (int) newEff.getPower()));
                            }
						}
						performer.getInventory().insertItem(enchantOrb, true);
						Items.destroyItem(source.getWurmId());
					} catch (FailedException | NoSuchTemplateException e) {
						e.printStackTrace();
					}
				}else{
					logger.info("Somehow a non-player activated an Enchant Orb...");
				}
				return true;
			}
			
	
		}; // ActionPerformer
	}
}