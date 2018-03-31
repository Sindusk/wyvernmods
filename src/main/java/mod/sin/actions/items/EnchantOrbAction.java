package mod.sin.actions.items;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.server.Items;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.spells.Spells;

import mod.sin.items.EnchantOrb;

public class EnchantOrbAction implements ModAction {
	private static Logger logger = Logger.getLogger(EnchantOrbAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;
	
	public EnchantOrbAction() {
		logger.log(Level.WARNING, "EnchantOrbAction()");

		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId,
			"Transfer enchant",
			"transferring", 
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
				if(performer instanceof Player && source != null && object != null && source.getTemplateId() == EnchantOrb.templateId && source != object){
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
				if(performer instanceof Player){
					Player player = (Player) performer;
					if(source.getTemplate().getTemplateId() != EnchantOrb.templateId){
						player.getCommunicator().sendNormalServerMessage("You must use an Enchant Orb to transfer enchants.");
						return true;
					}
					if(source.getWurmId() == target.getWurmId()){
						player.getCommunicator().sendNormalServerMessage("You cannot enchant the orb with itself!");
						return true;
					}
					ItemSpellEffects effs = source.getSpellEffects();
					if(effs == null || effs.getEffects().length == 0){
						player.getCommunicator().sendNormalServerMessage("The "+source.getTemplate().getName()+" has no enchants.");
						return true;
					}
					if(!Spell.mayBeEnchanted(target)){
						player.getCommunicator().sendNormalServerMessage("The "+target.getTemplate().getName()+" may not be enchanted.");
					}
					ItemSpellEffects teffs = target.getSpellEffects();
					if(teffs == null){
						teffs = new ItemSpellEffects(target.getWurmId());
					}
					for(SpellEffect eff : effs.getEffects()){
						Spell spell = Spells.getEnchantment(eff.type);
						boolean canEnchant = Spell.mayBeEnchanted(target);
						byte type = eff.type;
						if(spell == null){
							logger.info("Error: Enchant for "+eff.type+" doesn't exist.");
							continue;
						}
						if(canEnchant){
							try {
								Method m = spell.getClass().getDeclaredMethod("precondition", Skill.class, Creature.class, Item.class);
								canEnchant = ReflectionUtil.callPrivateMethod(spell, m, player.getChannelingSkill(), performer, target);
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
								e.printStackTrace();
							}
						}
						if(canEnchant){
							SpellEffect newEff = new SpellEffect(target.getWurmId(), type, eff.getPower(), 20000000);
							teffs.addSpellEffect(newEff);
							Items.destroyItem(source.getWurmId());
							player.getCommunicator().sendSafeServerMessage("The "+eff.getName()+" transfers to the "+target.getTemplate().getName()+".");
						}
					}
				}else{
					logger.info("Somehow a non-player activated an Enchant Orb...");
				}
				return true;
			}
			
	
		}; // ActionPerformer
	}
}