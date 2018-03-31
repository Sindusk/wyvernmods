package mod.sin.actions.items;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.SkillList;

import mod.sin.items.DepthDrill;

public class DepthDrillAction implements ModAction {
	private static Logger logger = Logger.getLogger(DepthDrillAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;
	
	public DepthDrillAction() {
		logger.log(Level.WARNING, "DepthDrillAction()");

		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId,
			"Drill",
			"drilling",
			new int[] { Actions.ACTION_TYPE_NOMOVE }
		);
		ModActions.registerAction(actionEntry);
	}


	@Override
	public BehaviourProvider getBehaviourProvider()
	{
		return new BehaviourProvider() {
			// Menu with activated object
			@Override
			public List<ActionEntry> getBehavioursFor(Creature performer, Item object, int tilex, int tiley, boolean onSurface, boolean corner, int tile)
			{
				if(performer instanceof Player && object != null && object.getTemplateId() == DepthDrill.templateId){
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
			public boolean action(Action action, Creature performer, Item source, int tilex, int tiley, boolean onSurface, boolean corner, int tile, short num, float counter)
			{
				try{
					if(performer instanceof Player){
						if (source.getTemplate().getTemplateId() != DepthDrill.templateId){
							performer.getCommunicator().sendNormalServerMessage("You must use a depth drill to drill.");
							return true;
						}
						if (!performer.isWithinDistanceTo(tilex * 4, tiley * 4, performer.getPositionZ(), 4)) {
							performer.getCommunicator().sendNormalServerMessage("You are too far away to drill.");
							action.stop(true);
							return true;
						}
	
						int surfaceHeight = Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex, tiley));
						int rockHeight = Tiles.decodeHeight(Server.rockMesh.getTile(tilex, tiley));
						int delta = surfaceHeight - rockHeight;
						if (counter == 1.0f) {
							if (delta == 0) {
								performer.getCommunicator().sendNormalServerMessage("The ground here is too hard and the drill makes no progress.");
								action.stop(false);
								return true;
							}
							performer.getCommunicator().sendNormalServerMessage("You start to drill into the ground.");
							Server.getInstance().broadCastAction(performer.getName() + " starts to drill into the ground.", performer, 5);
							action.setTimeLeft(Actions.getSlowActionTime(performer, performer.getSkills().getSkill(SkillList.DIGGING), source, 1));
							performer.sendActionControl("Drilling", true, action.getTimeLeft());
						} else if (counter * 10.0f > performer.getCurrentAction().getTimeLeft()) {
							performer.getCommunicator().sendNormalServerMessage("The rock here is " + delta + " deep.");
							performer.getSkills().getSkill(SkillList.DIGGING).skillCheck(10d, 0d, false, 1);
							performer.getSkills().getSkill(SkillList.PROSPECT).skillCheck(10d, 0d, false, 1);
							sendOres(performer, tilex, tiley);
							return true;
						}
					}else{
						logger.info("Somehow a non-player activated a "+source.getTemplate().getName()+".");
					}
					return false;
				}catch(Exception e){
					e.printStackTrace();
					return true;
				}
			}
			
			private void sendOres(Creature performer, int x, int y) throws NoSuchSkillException {
				double prospecting = performer.getSkills().getSkill(SkillList.PROSPECT).getRealKnowledge();
				if (prospecting < 30) return;
				double mining = performer.getSkills().getSkill(SkillList.MINING).getRealKnowledge();

				Set<String> ores = new HashSet<>();
				int distance = (int) Math.floor(Math.pow(prospecting+mining, 0.38D));

				for (int dx = distance*-1; dx < distance; dx++) {
					for (int dy = distance*-1; dy < distance; dy++) {
						int type = Tiles.decodeType(Server.caveMesh.getTile(x + dx, y + dy)) & 0xFF;

						if (type == Tiles.TILE_TYPE_CAVE_WALL_SLATE && prospecting > 40) {
							ores.add("slate");
						} else if (type == Tiles.TILE_TYPE_CAVE_WALL_MARBLE && prospecting > 40) {
							ores.add("marble");
						} else if (type == Tiles.TILE_TYPE_CAVE_WALL_ORE_GOLD && prospecting > 60) {
							ores.add("gold");
						} else if (type == Tiles.TILE_TYPE_CAVE_WALL_ORE_SILVER && prospecting > 50) {
							ores.add("silver");
						} else if (type == Tiles.TILE_TYPE_CAVE_WALL_ORE_ADAMANTINE && prospecting > 70) {
							ores.add("adamantine");
						} else if (type == Tiles.TILE_TYPE_CAVE_WALL_ORE_GLIMMERSTEEL && prospecting > 80) {
							ores.add("glimmersteel");
						} else if (type == Tiles.TILE_TYPE_CAVE_WALL_ORE_IRON) {
							ores.add("iron");
						} else if (type == Tiles.TILE_TYPE_CAVE_WALL_ORE_COPPER) {
							ores.add("copper");
						} else if (type == Tiles.TILE_TYPE_CAVE_WALL_ORE_LEAD) {
							ores.add("lead");
						} else if (type == Tiles.TILE_TYPE_CAVE_WALL_ORE_ZINC) {
							ores.add("zinc");
						} else if (type == Tiles.TILE_TYPE_CAVE_WALL_ORE_TIN) {
							ores.add("tin");
						}
					}
				}
				logger.info("Player "+performer.getName()+" uses depth drill at "+x+", "+y+" and prospects "+ores.toString()+" in the ground.");

				Iterator<String> it = ores.iterator();
				if (ores.size() == 1) {
					performer.getCommunicator().sendNormalServerMessage("You find traces of " + it.next() + " in the dirt.");
				} else if (ores.size() > 1) {
					String s = "You find traces of ";
					for (int i = 0; i < ores.size() - 1; i++) {
						if (i == ores.size() - 2) {
							s += it.next();
						} else {
							s += it.next() + ", ";
						}
					}
					s += " and " + it.next() + " in the dirt.";

					performer.getCommunicator().sendNormalServerMessage(s);
				}
			}
		}; // ActionPerformer
	}
}