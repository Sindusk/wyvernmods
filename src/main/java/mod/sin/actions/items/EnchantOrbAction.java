package mod.sin.actions.items;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wurmonline.server.spells.ItemEnchantment;
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
					if(source.getTemplate().getTemplateId() != EnchantOrb.templateId){
						player.getCommunicator().sendNormalServerMessage("You must use an Enchant Orb to transfer enchants.");
						return true;
					}
					if(target.getTemplate().getTemplateId() == EnchantOrb.templateId){
						player.getCommunicator().sendNormalServerMessage("You cannot enchant an Enchant Orb with another.");
						return true;
					}
					ItemSpellEffects effs = source.getSpellEffects();
					if(effs == null || effs.getEffects().length == 0){
						player.getCommunicator().sendNormalServerMessage("The "+source.getTemplate().getName()+" has no enchants.");
						return true;
					}
					/*if(!Spell.mayBeEnchanted(target)){
						player.getCommunicator().sendNormalServerMessage("The "+target.getTemplate().getName()+" may not be enchanted.");
					}*/
					ItemSpellEffects teffs = target.getSpellEffects();
					if(teffs == null){
						teffs = new ItemSpellEffects(target.getWurmId());
					}
					for(SpellEffect eff : effs.getEffects()){
						Spell spell = Spells.getEnchantment(eff.type);
						boolean canEnchant = false;// = Spell.mayBeEnchanted(target);
						byte type = eff.type;
						if(spell == null){
							if(eff.type < -60){ // It's a rune
							    if(teffs.getNumberOfRuneEffects() > 0){
							        teffs.getRandomRuneEffect();
							        player.getCommunicator().sendAlertServerMessage("The "+target.getTemplate().getName()+" already has a rune attached and resists the application of the "+eff.getName()+".");
							        continue;
                                }else{
							        canEnchant = true;
                                }
                            }else{
                                if(teffs.getSpellEffect(type) != null){
                                    float power = teffs.getSpellEffect(type).getPower();
                                    if(power >= 100f){
                                        player.getCommunicator().sendAlertServerMessage("The "+target.getTemplate().getName()+" already has the maximum power for "+eff.getName()+", and refuses to accept more.");
                                        continue;
                                    }else if(power + eff.getPower() > 100){
                                        float difference = 100-power;
                                        eff.setPower(eff.getPower()-difference);
                                        teffs.getSpellEffect(type).setPower(100);
                                        player.getCommunicator().sendSafeServerMessage("The "+eff.getName()+" transfers some of its power to the "+target.getTemplate().getName()+".");
                                        continue;
                                    }else{
                                        teffs.getSpellEffect(type).setPower(effs.getSpellEffect(type).getPower()+power);
                                        effs.removeSpellEffect(type);
                                        player.getCommunicator().sendSafeServerMessage("The "+eff.getName()+" fully transfers to the "+target.getTemplate().getName()+".");
                                        continue;
                                    }
                                }else{
                                    canEnchant = true;
                                }
                            }
						}else {
                            try {
                            	Method m;
                            	if (spell instanceof ItemEnchantment){
                            		m = ItemEnchantment.class.getDeclaredMethod("precondition", Skill.class, Creature.class, Item.class);
								}else {
									m = spell.getClass().getDeclaredMethod("precondition", Skill.class, Creature.class, Item.class);
								}
                                canEnchant = ReflectionUtil.callPrivateMethod(spell, m, player.getChannelingSkill(), performer, target);
                            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
                                e.printStackTrace();
                            }
                        }
						if(canEnchant){
						    if(teffs.getSpellEffect(type) != null){
						        if(teffs.getSpellEffect(type).getPower() >= eff.getPower()) {
                                    player.getCommunicator().sendAlertServerMessage("The " + target.getTemplate().getName() + " already has a more powerful " + eff.getName() + " and resists the transfer.");
                                    continue;
                                }else{
						            teffs.getSpellEffect(type).setPower(eff.getPower());
						            effs.removeSpellEffect(type);
						            player.getCommunicator().sendSafeServerMessage("The "+eff.getName()+" replaces the existing enchant.");
						            continue;
                                }
                            }
							SpellEffect newEff = new SpellEffect(target.getWurmId(), type, eff.getPower(), 20000000);
							teffs.addSpellEffect(newEff);
							effs.removeSpellEffect(type);
							player.getCommunicator().sendSafeServerMessage("The "+eff.getName()+" transfers to the "+target.getTemplate().getName()+".");
						}
					}
					if(effs.getEffects().length == 0){
					    player.getCommunicator().sendSafeServerMessage("The "+source.getTemplate().getName()+" exhausts the last of its magic and vanishes.");
					    Items.destroyItem(source.getWurmId());
                    }
				}else{
					logger.info("Somehow a non-player activated an Enchant Orb...");
				}
				return true;
			}
			
	
		}; // ActionPerformer
	}
}