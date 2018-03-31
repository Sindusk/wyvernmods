package mod.sin.actions.items;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Items;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.TileRockBehaviour;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;

import mod.sin.items.DisintegrationRod;

public class DisintegrationRodAction implements ModAction {
	private static Logger logger = Logger.getLogger(DisintegrationRodAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;
	
	public DisintegrationRodAction() {
		logger.log(Level.WARNING, "DisintegrationRodAction()");

		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId,
			"Use",
			"disintegrating",
			new int[] { // {1, 4, 25, 9, 18, 21, 41, 43, 5, 49, 50} - Mining
				Actions.ACTION_TYPE_SHOW_ON_SELECT_BAR,
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
			public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, int tilex, int tiley, boolean onSurface, int tile, int dir)
			{
				if(performer instanceof Player && subject != null && subject.getTemplateId() == DisintegrationRod.templateId && Tiles.isSolidCave(Tiles.decodeType(tile))){
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
			public boolean action(Action act, Creature performer, Item source, int tilex, int tiley, boolean onSurface, int heightOffset, int tile, short action, float counter)
			{
				try{
					if(performer instanceof Player){
						int newTile = Server.caveMesh.getTile(tilex, tiley);
						if(source.getTemplate().getTemplateId() != DisintegrationRod.templateId){
							performer.getCommunicator().sendSafeServerMessage("You must use a Disintegration Rod to do this.");
							return true;
						}
						byte type = Tiles.decodeType(newTile);
						if (Tiles.isSolidCave(type)) {
							int resource = Server.getCaveResource(tilex, tiley);
							int dir = (int)(act.getTarget() >> 48) & 255;
							boolean destroyRod = true;
							
							// Don't allow disintegration of reinforced caves
							if ((type & 0xFF) == Tiles.TILE_TYPE_CAVE_WALL_REINFORCED) {
								destroyRod = false;
								performer.getCommunicator().sendSafeServerMessage("The reinforced wall is too strong and doesn't get affected by the rod.");
							} else if (resource <= 1000 && TileRockBehaviour.createInsideTunnel(tilex, tiley, newTile, performer, 145, dir, true, act)) {
								//Server.caveMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_CAVE.id, Tiles.decodeData(tile)));
			                    Players.getInstance().sendChangedTile(tilex, tiley, false, false);
			                    performer.getCommunicator().sendSafeServerMessage("You use the " + source.getTemplate().getName()+" on the "+Tiles.getTile(Tiles.decodeType(newTile)).tiledesc.toLowerCase()+" and it breaks!");
							} else if ((type & 0xFF) != Tiles.TILE_TYPE_CAVE_WALL && resource > 1000){
								Server.setCaveResource(tilex, tiley, resource-1000);
			                    Players.getInstance().sendChangedTile(tilex, tiley, false, false);
			                    performer.getCommunicator().sendSafeServerMessage("You use the " + source.getTemplate().getName()+" on the "+Tiles.getTile(Tiles.decodeType(newTile)).tiledesc.toLowerCase()+", but it is only weakened!");
							} else if ((type & 0xFF) == Tiles.TILE_TYPE_CAVE_WALL && TileRockBehaviour.createInsideTunnel(tilex, tiley, newTile, performer, 145, dir, true, act)) {
			                    Players.getInstance().sendChangedTile(tilex, tiley, false, false);
			                    performer.getCommunicator().sendSafeServerMessage("The rock is powerless against the "+source.getTemplate().getName()+"!");
							} else if (!TileRockBehaviour.createInsideTunnel(tilex, tiley, newTile, performer, 145, dir, true, act)) {
			                    performer.getCommunicator().sendSafeServerMessage("The wall is not stable enough.");
			                    destroyRod = false;
							} // else if
							if (destroyRod) {
								Items.destroyItem(source.getWurmId());
							}
						}else{
							logger.info("Disintegration rod attempted to be used on non-solid cave at "+tilex+", "+tiley);
						}
					} else {
						logger.info("Somehow a non-player activated a "+source.getTemplate().getName()+".");
					}
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return true;
				} // catch
			}
		}; // ActionPerformer
	}
}