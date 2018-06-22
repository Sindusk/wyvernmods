package mod.sin.actions;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SmoothTerrainAction implements ModAction {
	private static Logger logger = Logger.getLogger(SmoothTerrainAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;

	public SmoothTerrainAction() {
		logger.log(Level.WARNING, "SmoothTerrainAction()");

		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId,
			"Smooth Terrain",
			"smoothing",
			new int[] {
                    Actions.ACTION_TYPE_QUICK,
                    Actions.ACTION_TYPE_IGNORERANGE
            }
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
            public List<ActionEntry> getBehavioursFor(Creature performer, Item source, int tilex, int tiley, boolean onSurface, boolean corner, int tile, int heightOffset) {
                return this.getBehavioursFor(performer, tilex, tiley, onSurface, corner, tile, heightOffset);
            }

			@Override
			public List<ActionEntry> getBehavioursFor(Creature performer, int tilex, int tiley, boolean onSurface, boolean corner, int tile, int heightOffset) {
				if(performer instanceof Player && performer.getPower() >= 5){
					return Collections.singletonList(actionEntry);
				}
				return null;
			}
		};
	}

	static boolean isValidSmoothTile(byte type){
	    return Tiles.isTree(type) || Tiles.isBush(type) || type == Tiles.Tile.TILE_SAND.id || type == Tiles.Tile.TILE_GRASS.id || type == Tiles.Tile.TILE_TUNDRA.id || type == Tiles.Tile.TILE_STEPPE.id || type == Tiles.Tile.TILE_SNOW.id;
    }
    /*static boolean isImmutableTile(byte type) {
        return Tiles.isTree(type) || Tiles.isBush(type) || type == Tiles.Tile.TILE_CLAY.id || type == Tiles.Tile.TILE_MARSH.id || type == Tiles.Tile.TILE_PEAT.id || type == Tiles.Tile.TILE_TAR.id || type == Tiles.Tile.TILE_HOLE.id || type == Tiles.Tile.TILE_MOSS.id || type == Tiles.Tile.TILE_LAVA.id || Tiles.isMineDoor(type);
    }
    static boolean isRockTile(byte type) {
        return Tiles.isSolidCave(type) || type == Tiles.Tile.TILE_CAVE.id || type == Tiles.Tile.TILE_CAVE_EXIT.id || type == Tiles.Tile.TILE_CLIFF.id || type == Tiles.Tile.TILE_ROCK.id || type == Tiles.Tile.TILE_CAVE_FLOOR_REINFORCED.id;
    }*/

    public static void smooth(int tilex, int tiley, MeshIO mesh) {
        int currentTileRock;
        short currentRockHeight;
        boolean insta;
        if(tilex < 50 || tiley < 50 || tilex > Server.surfaceMesh.getSize()-50 || tiley > Server.surfaceMesh.getSize()-50){
            return;
        }
        if (tilex > 1 << Constants.meshSize || tiley > 1 << Constants.meshSize) {
            return;
        }
        int digTile = mesh.getTile(tilex, tiley);
        byte digTileType = Tiles.decodeType(digTile);
        if(!isValidSmoothTile(digTileType)){
            return;
        }
        short digTileHeight = Tiles.decodeHeight(digTile);
        if (digTileHeight <= Tiles.decodeHeight(Server.rockMesh.getTile(tilex, tiley))) {
            return;
        }
        Village village = Villages.getVillageWithPerimeterAt(tilex, tiley, true);
        if(village != null){
            return;
        }
        Structure structure = Structures.getStructureForTile(tilex, tiley, true);
        if(structure != null){
            return;
        }
        short minHeight = -300;
        short maxHeight = 20000;

        // Check for slopes nearby
        int minDiff = -5;
        int xMinDiff = 0;
        int yMinDiff = 0;
        int lNewTile = mesh.getTile(tilex, tiley - 1);
        short height = Tiles.decodeHeight(lNewTile);
        int difference = height - digTileHeight;
        int northDiff = difference;
        if(northDiff < minDiff){
            minDiff = northDiff;
            xMinDiff = 0;
            yMinDiff = -1;
        }

        lNewTile = mesh.getTile(tilex + 1, tiley);
        height = Tiles.decodeHeight(lNewTile);
        difference = height - digTileHeight;
        int eastDiff = difference;
        if(eastDiff < minDiff){
            minDiff = eastDiff;
            xMinDiff = 1;
            yMinDiff = 0;
        }

        lNewTile = mesh.getTile(tilex, tiley + 1);
        height = Tiles.decodeHeight(lNewTile);
        difference = height - digTileHeight;
        int southDiff = difference;
        if(southDiff < minDiff){
            minDiff = southDiff;
            xMinDiff = 0;
            yMinDiff = 1;
        }

        lNewTile = mesh.getTile(tilex - 1, tiley);
        height = Tiles.decodeHeight(lNewTile);
        difference = height - digTileHeight;
        int westDiff = difference;
        if(westDiff < minDiff){
            //minDiff = westDiff;
            xMinDiff = -1;
            yMinDiff = 0;
        }

        boolean bump = false;
        boolean pit = false;
        boolean slanted = false;
        if(westDiff < 0 && eastDiff < 0){
            bump = true;
        }else if(westDiff > 0 && eastDiff > 0){
            pit = true;
        }else if(northDiff < 0 && southDiff < 0){
            bump = true;
        }else if(northDiff > 0 && southDiff > 0){
            pit = true;
        }
        if(pit){
            return;
        }
        if(!bump){
            if(westDiff + eastDiff > 10 || westDiff + eastDiff < -10){
                slanted = true;
            }else if(northDiff + southDiff > 10 || northDiff + southDiff < -10){
                slanted = true;
            }
        }

        if(!bump && !slanted){
            return;
        }

        for(int x = tilex-2; x < tilex+2; x++){
            for(int y = tiley-2; y < tiley+2; y++){
                lNewTile = mesh.getTile(x, y);
                byte lNewTileType = Tiles.decodeType(lNewTile);
                if(!isValidSmoothTile(lNewTileType)){
                    return;
                }
                structure = Structures.getStructureForTile(x, y, true);
                if(structure != null){
                    return;
                }
            }
        }

        if (digTileHeight > minHeight && digTileHeight < maxHeight) {
            short maxdifference = 0;
            boolean hitRock = false;
            boolean allCornersRock;
            for (int x = 1; x >= -1; --x) {
                for (int y = 1; y >= -1; --y) {
                    boolean lChanged = false;
                    lNewTile = mesh.getTile(tilex + x, tiley + y);
                    byte type = Tiles.decodeType(lNewTile);
                    short newTileHeight = Tiles.decodeHeight(lNewTile);
                    int rockTile = Server.rockMesh.getTile(tilex + x, tiley + y);
                    short rockHeight = Tiles.decodeHeight(rockTile);
                    if (x == xMinDiff && y == yMinDiff){
                        lChanged = true;
                        newTileHeight = (short)Math.max(newTileHeight + 1, rockHeight);
                        //logger.info("Tile " + (tilex + x) + ", " + (tiley + y) + " now at " + newTileHeight + ", rock at " + rockHeight + ".");
                        mesh.setTile(tilex + x, tiley + y, Tiles.encode(newTileHeight, type, Tiles.decodeData(lNewTile)));
                    }
                    if (x == 0 && y == 0) {
                        if (newTileHeight > rockHeight) {
                            lChanged = true;
                            newTileHeight = (short)Math.max(newTileHeight - 1, rockHeight);
                            //logger.info("Tile " + (tilex + x) + ", " + (tiley + y) + " now at " + newTileHeight + ", rock at " + rockHeight + ".");
                            mesh.setTile(tilex + x, tiley + y, Tiles.encode(newTileHeight, type, Tiles.decodeData(lNewTile)));
                        }
                    }
                    allCornersRock = Terraforming.allCornersAtRockLevel(tilex + x, tiley + y, mesh);
                    if(allCornersRock) {
                        int theTile = mesh.getTile(tilex + x, tiley + y);
                        float oldTileHeight = Tiles.decodeHeightAsFloat(theTile);
                        Server.modifyFlagsByTileType(tilex + x, tiley + y, Tiles.Tile.TILE_ROCK.id);
                        mesh.setTile(tilex + x, tiley + y, Tiles.encode(oldTileHeight, Tiles.Tile.TILE_ROCK.id, (byte) 0));
                        Players.getInstance().sendChangedTile(tilex + x, tiley + y, true, true);
                    }else if(type == Tiles.TILE_TYPE_ROCK){
                        int theTile = mesh.getTile(tilex + x, tiley + y);
                        float oldTileHeight = Tiles.decodeHeightAsFloat(theTile);
                        Server.modifyFlagsByTileType(tilex + x, tiley + y, Tiles.Tile.TILE_DIRT.id);
                        mesh.setTile(tilex + x, tiley + y, Tiles.encode(oldTileHeight, Tiles.Tile.TILE_DIRT.id, (byte) 0));
                        Players.getInstance().sendChangedTile(tilex + x, tiley + y, true, true);
                    }
                    if (lChanged) {
                        Players.getInstance().sendChangedTile(tilex + x, tiley + y, true, true);
                        try {
                            Zone toCheckForChange = Zones.getZone(tilex + x, tiley + y, true);
                            toCheckForChange.changeTile(tilex + x, tiley + y);
                        }
                        catch (NoSuchZoneException nsz) {
                            logger.log(Level.INFO, "no such zone?: " + tilex + ", " + tiley, nsz);
                        }
                    }
                }
            }
        }
    }

    public static void smoothArea(int tilex, int tiley){
        long start = System.currentTimeMillis();
        int x = tilex-10;
        int y;
        while(x < tilex+10){
            y = tiley-10;
            while(y < tiley+10){
                SmoothTerrainAction.smooth(x, y, Server.surfaceMesh);
                y++;
            }
            x++;
        }
        if(System.currentTimeMillis()-start > 500) {
            logger.info(String.format("Smoothing terrain at [%s, %s] took %s milliseconds.", tilex, tiley, System.currentTimeMillis() - start));
        }
    }
    public static void onServerPoll(){
        if(Servers.localServer.PVPSERVER){
            return;
        }
        smoothArea(Server.rand.nextInt(Server.surfaceMesh.getSize()), Server.rand.nextInt(Server.surfaceMesh.getSize()));
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
            public boolean action(Action action, Creature performer, Item source, int tilex, int tiley, boolean onSurface, boolean corner, int tile, int heightOffset, short num, float counter)
            {
                return this.action(action, performer, tilex, tiley, onSurface, corner, tile, heightOffset, num, counter);
            }

			// Without activated object
			@Override
			public boolean action(Action action, Creature performer, int tilex, int tiley, boolean onSurface, boolean corner, int tile, int heightOffset, short num, float counter)
			{
				if(performer instanceof Player){
					if(performer.getPower() < 5){
						performer.getCommunicator().sendNormalServerMessage("You do not have permission to do that.");
						return true;
					}
					smoothArea(tilex, tiley);
					//SmoothTerrainAction.smooth(tilex, tiley, Server.surfaceMesh);
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