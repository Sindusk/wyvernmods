package mod.sin.wyvern;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Logger;

import com.wurmonline.server.skills.SkillList;
import mod.sin.lib.Util;
import mod.sin.wyvern.bounty.PlayerBounty;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.Features;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.ai.NoPathException;
import com.wurmonline.server.creatures.ai.Path;
import com.wurmonline.server.creatures.ai.PathFinder;
import com.wurmonline.server.creatures.ai.PathTile;
import com.wurmonline.server.zones.Water;
import com.wurmonline.server.zones.Zones;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.gotti.wurmunlimited.modsupport.ModSupportDb;

public class AntiCheat {
	private static Logger logger = Logger.getLogger(AntiCheat.class.getName());
	private static final int emptyRock = Tiles.encode((short)-100, (byte)Tiles.Tile.TILE_CAVE_WALL.id, (byte)0);

    public static void mapPlayerSteamId(String name, String steamId){
        Connection dbcon;
        PreparedStatement ps;
        boolean foundSteamIdMap = false;
        try {
            dbcon = ModSupportDb.getModSupportDb();
            ps = dbcon.prepareStatement("SELECT * FROM SteamIdMap WHERE NAME=? AND STEAMID=?");
            ps.setString(1, name);
            ps.setString(2, steamId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if(!foundSteamIdMap) {
                    foundSteamIdMap = true;
                }else{
                    logger.warning(String.format("Player %s has accessed their account from multiple Steam ID's!", name));
                }
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (!foundSteamIdMap) {
            logger.info("No steam id entry for " + name + ". Creating one.");
            try {
                dbcon = ModSupportDb.getModSupportDb();
                ps = dbcon.prepareStatement("INSERT INTO SteamIdMap (NAME, STEAMID) VALUES(?, ?)");
                ps.setString(1, name);
                ps.setString(2, steamId);
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        PlayerBounty.steamIdMap.put(name, Long.valueOf(steamId));
    }

	private static boolean isCaveWall(int xx, int yy){
		if (xx < 0 || xx >= Zones.worldTileSizeX || yy < 0 || yy >= Zones.worldTileSizeY) {
            return true;
        } else if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.data[xx | yy << Constants.meshSize]))) {
        	return true;
        }
		return false;
	}
	private static boolean isSurroundedByCaveWalls(int tilex, int tiley){
		int xx = tilex+1;
		int yy = tiley;
		if(!isCaveWall(xx, yy)){
			return false;
		}
		xx = tilex-1;
		if(!isCaveWall(xx, yy)){
			return false;
		}
		xx = tilex;
		yy = tiley+1;
		if(!isCaveWall(xx, yy)){
			return false;
		}
		yy = tiley-1;
		if(!isCaveWall(xx, yy)){
			return false;
		}
		return true;
	}
	private static boolean playerCanSeeVein(int tilex, int tiley, int distance){
	    int xx = tilex-distance;
	    int yy;
	    while(xx <= tilex+distance){
	        yy = tiley-distance;
	        while(yy <= tiley+distance){
                if(!isCaveWall(xx, yy)){
                    return true;
                }
	            yy++;
            }
            xx++;
        }
	    return false;
    }
	private static int getDummyWallAntiCheat(int tilex, int tiley){
		return Tiles.encode(Tiles.decodeHeight(Server.caveMesh.data[tilex | tiley << Constants.meshSize]), Tiles.Tile.TILE_CAVE_WALL.id, Tiles.decodeData((int)Server.caveMesh.data[tilex | tiley << Constants.meshSize]));
	}
	public static void sendCaveStripAntiCheat(Communicator comm, short xStart, short yStart, int width, int height){
		if (comm.player != null && comm.player.hasLink()) {
            try {
                ByteBuffer bb = comm.getConnection().getBuffer();
                bb.put((byte) 102);
                bb.put((byte) (Features.Feature.CAVEWATER.isEnabled() ? 1 : 0));
                bb.put((byte) (comm.player.isSendExtraBytes() ? 1 : 0));
                bb.putShort(xStart);
                bb.putShort(yStart);
                bb.putShort((short)width);
                bb.putShort((short)height);
                boolean onSurface = comm.player.isOnSurface();
                double prospecting = comm.player.getSkills().getSkill(SkillList.PROSPECT).getKnowledge();
                int distance = (int) prospecting / 18;
                for (int x = 0; x < width; ++x) {
                    for (int y = 0; y < height; ++y) {
                        int xx = xStart + x;
                        int yy = yStart + y;
                        if (xx < 0 || xx >= Zones.worldTileSizeX || yy < 0 || yy >= Zones.worldTileSizeY) {
                            bb.putInt(emptyRock);
                            xx = 0;
                            yy = 0;
                        } else if (!onSurface) {
                        	if(!(Tiles.decodeType(Server.caveMesh.getTile(xx, yy)) == Tiles.Tile.TILE_CAVE_EXIT.id)){
                        	    if(prospecting < 20 && isSurroundedByCaveWalls(xx, yy)){
                                    bb.putInt(getDummyWallAntiCheat(xx, yy));
                                }else if(prospecting > 20 && playerCanSeeVein(xx, yy, distance)){
                                    bb.putInt(Server.caveMesh.data[xx | yy << Constants.meshSize]);
                                }else if(!isSurroundedByCaveWalls(xx, yy)){
                                    bb.putInt(Server.caveMesh.data[xx | yy << Constants.meshSize]);
                                }else {
                                    bb.putInt(getDummyWallAntiCheat(xx, yy));
                                }
                        	}else{
                        		bb.putInt(Server.caveMesh.data[xx | yy << Constants.meshSize]);
                        	}
                        } else if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.data[xx | yy << Constants.meshSize]))) {
                            bb.putInt(getDummyWallAntiCheat(xx, yy));
                        } else {
                            bb.putInt(Server.caveMesh.data[xx | yy << Constants.meshSize]);
                        }
                        if (Features.Feature.CAVEWATER.isEnabled()) {
                            bb.putShort((short)Water.getCaveWater(xx, yy));
                        }
                        if (!comm.player.isSendExtraBytes()) continue;
                        bb.put(Server.getClientCaveFlags(xx, yy));
                    }
                }
                comm.getConnection().flush();
            }
            catch (Exception ex) {
                comm.player.setLink(false);
            }
        }
	}
	public static boolean isVisibleThroughTerrain(Creature performer, Creature defender){
		int trees = 0;
        //int treetilex = -1;
        //int treetiley = -1;
        //int tileArrowDownX = -1;
        //int tileArrowDownY = -1;
        PathFinder pf = new PathFinder(true);
        try {
            Path path = pf.rayCast(performer.getCurrentTile().tilex, performer.getCurrentTile().tiley, defender.getCurrentTile().tilex, defender.getCurrentTile().tiley, performer.isOnSurface(), ((int)Creature.getRange(performer, defender.getPosX(), defender.getPosY()) >> 2) + 5);
            float initialHeight = Math.max(-1.4f, performer.getPositionZ() + performer.getAltOffZ() + 1.4f);
            float targetHeight = Math.max(-1.4f, defender.getPositionZ() + defender.getAltOffZ() + 1.4f);
            double distx = Math.pow(performer.getCurrentTile().tilex - defender.getCurrentTile().tilex, 2.0);
            double disty = Math.pow(performer.getCurrentTile().tiley - defender.getCurrentTile().tiley, 2.0);
            double dist = Math.sqrt(distx + disty);
            double dx = (double)(targetHeight - initialHeight) / dist;
            while (!path.isEmpty()) {
                PathTile p = path.getFirst();
                if(Tiles.getTile(Tiles.decodeType(p.getTile())).isTree()){
                	trees++;
                }
                /*if (Tiles.getTile((byte)Tiles.decodeType((int)p.getTile())).isTree() && treetilex == -1 && Server.rand.nextInt(10) < ++trees) {
                    treetilex = p.getTileX();
                    treetiley = p.getTileY();
                }*/
                distx = Math.pow(p.getTileX() - defender.getCurrentTile().tilex, 2.0);
                disty = Math.pow(p.getTileY() - defender.getCurrentTile().tiley, 2.0);
                double currdist = Math.sqrt(distx + disty);
                float currHeight = Math.max(-1.4f, Zones.getLowestCorner(p.getTileX(), p.getTileY(), performer.getLayer()));
                double distmod = currdist * dx;
                if (dx < 0.0) {
                    if ((double)currHeight > (double)targetHeight - distmod) {
                        return false;
                    }
                } else if ((double)currHeight > (double)targetHeight - distmod) {
                    return false;
                }
                /*if (tileArrowDownX == -1 && Server.rand.nextInt(15) == 0) {
                    tileArrowDownX = p.getTileX();
                    tileArrowDownY = p.getTileY();
                }*/
                path.removeFirst();
            }
            if(trees >= 8){
            	return false;
            }
        }
        catch (NoPathException np) {
            performer.getCommunicator().sendCombatNormalMessage("You fail to get a clear shot.");
            return false;
        }
        return true;
	}
	public static boolean isVisibleToAntiCheat(Creature cret, Creature watcher) {
        if (!cret.isVisible()) {
            return cret.getPower() > 0 && cret.getPower() <= watcher.getPower();
        }
        if (cret.isStealth()) {
            if (cret.getPower() > 0 && cret.getPower() <= watcher.getPower()) {
                return true;
            }
            if (cret.getPower() < watcher.getPower()) {
                return true;
            }
            if (watcher.isUnique() || watcher.isDetectInvis()) {
                return true;
            }
            Set<Long> stealthBreakers = null;
			try {
				stealthBreakers = ReflectionUtil.getPrivateField(cret, ReflectionUtil.getField(cret.getClass(), "stealthBreakers"));
			} catch (IllegalArgumentException | IllegalAccessException | ClassCastException | NoSuchFieldException e) {
				logger.info("Failed to get stealthBreakers for creature "+cret.getName());
				e.printStackTrace();
			}
            if (stealthBreakers != null && stealthBreakers.contains(watcher.getWurmId())) {
                return true;
            }
            return false;
        }
        if(WyvernMods.espCounter && watcher.isPlayer()){
        	if(cret.isPlayer() || cret.getLeader() != null || cret.isRidden() || cret.isUnique()){
		        if(cret.isWithinDistanceTo(watcher, 120)){
		        	return true;
		        }
		        if(isVisibleThroughTerrain(cret, watcher)){
		        	return true;
		        }
		        return false;
        	}
        }
        return true;
    }
	public static void preInit(){
		try{
			ClassPool classPool = HookManager.getInstance().getClassPool();
			Class<AntiCheat> thisClass = AntiCheat.class;
			String replace;

            // - Change the caveStrip method to the custom one, so we can edit what the clients see! - //
			CtClass ctCommunicator = classPool.get("com.wurmonline.server.creatures.Communicator");
			replace = "{ mod.sin.wyvern.AntiCheat.sendCaveStripAntiCheat(this, $$); }";
			Util.setBodyDeclared(thisClass, ctCommunicator, "sendCaveStrip", replace);
        	/*ctCommunicator.getDeclaredMethod("sendCaveStrip").setBody("{"
        			+ "  mod.sin.wyvern.AntiCheat.sendCaveStripAntiCheat(this, $$);"
        			+ "}");*/

            Util.setReason("Map Steam ID's to Modsupport table.");
            CtClass ctLoginHandler = classPool.get("com.wurmonline.server.LoginHandler");
            replace = AntiCheat.class.getName()+".mapPlayerSteamId($1, $2);";
            Util.insertBeforeDeclared(thisClass, ctLoginHandler, "preValidateLogin", replace);

            // - Change the creature isVisibleTo method to the custom one, so we can edit what the clients see! - //
			/*CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");
        	ctCreature.getDeclaredMethod("isVisibleTo").setBody("{"
        			+ "  return mod.sin.wyvern.AntiCheat.isVisibleToAntiCheat(this, $$);"
        			+ "}");
        	
        	// - Edit VirtualZone creature movement so that it removes units that the player cannot see - //
        	CtClass ctVirtualZone = classPool.get("com.wurmonline.server.zones.VirtualZone");
        	ctVirtualZone.getDeclaredMethod("coversCreature").insertBefore(""
        			+ "if(!this.covers($1.getTileX(), $1.getTileY())){"
        			+ "  return false;"
        			+ "}"
        			+ "if(!mod.sin.wyvern.AntiCheat.isVisibleToAntiCheat(this.watcher, $1)){"
        			+ "  return false;"
        			+ "}");*/

        } catch ( NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException((Throwable)e);
        }
	}
}
