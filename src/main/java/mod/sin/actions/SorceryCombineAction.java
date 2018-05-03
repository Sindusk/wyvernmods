package mod.sin.actions;

import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import mod.sin.items.SorceryFragment;
import mod.sin.wyvern.MiscChanges;
import mod.sin.wyvern.util.ItemUtil;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SorceryCombineAction implements ModAction {
	private static Logger logger = Logger.getLogger(SorceryCombineAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;

	public SorceryCombineAction() {
		logger.log(Level.WARNING, "ArenaTeleport()");

		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId, 
			"Combine sorcery",
			"combining sorcery",
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
				if(performer instanceof Player && object != null && object.isHugeAltar() && Servers.localServer.PVPSERVER) {
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
				try{
					if(performer instanceof Player){
						if(!target.isHugeAltar()){
							performer.getCommunicator().sendNormalServerMessage("You must combine at a huge altar.");
							return true;
						}
						int fragments = 0;
						Set<Item> inventory = performer.getInventory().getItems();
						for(Item i : inventory){
						    if(i.getTemplateId() == SorceryFragment.templateId){
						        fragments++;
                            }
                        }
                        if(fragments < 2){
                            performer.getCommunicator().sendNormalServerMessage("You must have at least two sorcery fragments to combine.");
						    return true;
                        }
						if(counter == 1.0f){
							performer.getCommunicator().sendSafeServerMessage("You begin to combine sorcery fragments.");
							MiscChanges.sendServerTabMessage("arena", performer.getName()+" is beginning to combine sorcery fragments at the "+target.getName()+"!", 52, 152, 219);
							act.setTimeLeft(3000);
							performer.sendActionControl("Combining sorcery", true, act.getTimeLeft());
						}else if(counter * 10f > performer.getCurrentAction().getTimeLeft()){
						    Set<Item> items = performer.getInventory().getItems();
						    Item first = null;
						    Item second = null;
						    for(Item i : items){
						        if(i.getTemplateId() == SorceryFragment.templateId){
						            if(first == null){
						                first = i;
                                    }else if(second == null){
						                second = i;
                                    }
                                }
                            }
                            if(first == null || second == null){
						        performer.getCommunicator().sendNormalServerMessage("Something went wrong with the combination.");
						        return true;
                            }
                            byte firstAux = first.getAuxData();
						    byte secondAux = (byte) (second.getAuxData()+1);
						    if(firstAux + secondAux >= 9){
                                byte newAux = (byte) (firstAux+secondAux-10);
                                performer.getCommunicator().sendSafeServerMessage("A new sorcery has been created!");
                                MiscChanges.sendServerTabMessage("arena", performer.getName()+" has created a new sorcery!", 52, 152, 219);
                                Item sorcery = ItemUtil.createRandomSorcery((byte) (2+Server.rand.nextInt(1)));
                                logger.info("Player "+performer.getName()+" created a "+sorcery.getName()+" with "+(3-sorcery.getAuxData())+" charges.");
                                performer.getInventory().insertItem(sorcery, true);
                                if(newAux >= 0) {
                                    first.setAuxData(newAux);
                                    first.setName("sorcery fragment [" + (first.getAuxData() + 1) + "/10]");
                                }else{
                                    Items.destroyItem(first.getWurmId());
                                }
                            }else{
                                performer.getCommunicator().sendSafeServerMessage("You combine fragments.");
                                MiscChanges.sendServerTabMessage("arena", performer.getName()+" has combined sorcery fragments, and is closer to creating a new sorcery.", 52, 152, 219);
						        first.setAuxData((byte) (firstAux+secondAux));
						        first.setName("sorcery fragment ["+(first.getAuxData()+1)+"/10]");
                            }
                            Items.destroyItem(second.getWurmId());
			                return true;
						}
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