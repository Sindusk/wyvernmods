package mod.sin.wyvern;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.HashMap;
import java.util.logging.Logger;

public class TeleportHandler {
    public static Logger logger = Logger.getLogger(TeleportHandler.class.getName());

    protected static HashMap<Long, Float> teleX = new HashMap<>();
    protected static HashMap<Long, Float> teleY = new HashMap<>();
    protected static void setTeleportLocationRandom(long wurmid){
        boolean found = false;
        while(!found){
            int x = Server.rand.nextInt(Server.surfaceMesh.getSize());
            int y = Server.rand.nextInt(Server.surfaceMesh.getSize());
            short height = Tiles.decodeHeight(Server.surfaceMesh.getTile(x, y));
            if(height > 0 && height < 1000 && Creature.getTileSteepness(x, y, true)[1] < 30){
                Village v = Villages.getVillage(x, y, true);
                if (v == null) {
                    for (int vx = -50; vx < 50; vx += 5) {
                        for (int vy = -50; vy < 50 && (v = Villages.getVillage(x + vx, y + vy, true)) == null; vy += 5) {
                        }
                        if(v != null){
                            break;
                        }
                    }
                }
                if(v != null){
                    continue;
                }
                teleX.put(wurmid, (float) (x*4));
                teleY.put(wurmid, (float) (y*4));
                found = true;
            }
        }
    }
    protected static void setTeleportLocation(long wurmid){
        PlayerInfo pinfo = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
        if(pinfo != null){
            logger.info("Player info exists.");
            boolean hasVillage = false;
            for(Village v : Villages.getVillages()){
                if(v.isCitizen(wurmid)){
                    logger.info("Player is found in village "+v.getName()+", teleporting to token.");
                    teleX.put(wurmid, (float) (v.getTokenX()*4));
                    teleY.put(wurmid, (float) (v.getTokenY()*4));
                    hasVillage = true;
                    break;
                }
            }
            if(!hasVillage){
                if(Servers.localServer.PVPSERVER) {
                    logger.info("Player is not identified as belonging to a village. PvP server detected. Performing random teleport.");
                    setTeleportLocationRandom(wurmid);
                }else{
                    logger.info("Player is not identified as belonging to a village. PvE server detected. Teleporting to JENNX/JENNY.");
                    teleX.put(wurmid, (float) (Servers.localServer.SPAWNPOINTJENNX*4));
                    teleY.put(wurmid, (float) (Servers.localServer.SPAWNPOINTJENNY*4));
                }
            }
        }else{
            if(Servers.localServer.PVPSERVER) {
                logger.info("Player info doesn't exist. PvP server detected. Performing a random teleport.");
                setTeleportLocationRandom(wurmid);
            }else{
                logger.info("Player info doesn't exist. PvE server detected. Teleporting to JENNX/JENNY.");
                teleX.put(wurmid, (float) (Servers.localServer.SPAWNPOINTJENNX*4));
                teleY.put(wurmid, (float) (Servers.localServer.SPAWNPOINTJENNY*4));
            }
        }
    }
    public static float getTeleportPosX(long wurmid){
        setTeleportLocation(wurmid);
        if(teleX.containsKey(wurmid)){
            return teleX.get(wurmid);
        }
        return 4000f;
    }
    public static float getTeleportPosY(long wurmid){
        if(teleY.containsKey(wurmid)){
            return teleY.get(wurmid);
        }
        return 4000f;
    }

    public static void preInit(){
        try{
            ClassPool classPool = HookManager.getInstance().getClassPool();
            final Class<TeleportHandler> thisClass = TeleportHandler.class;
            String replace;

            Util.setReason("Custom teleportation system for Arena teleport/escape.");
            CtClass ctPlayerMetaData = classPool.get("com.wurmonline.server.players.PlayerMetaData");
            replace = "logger.info(\"posx = \"+this.posx+\", posy = \"+this.posy);" +
                    "if(this.posx >= 4000f && this.posx <= 4050f && this.posy >= 4000f && this.posy <= 4050f){" +
                    "  this.posx = "+TeleportHandler.class.getName()+".getTeleportPosX(this.wurmid);" +
                    "  this.posy = "+TeleportHandler.class.getName()+".getTeleportPosY(this.wurmid);" +
                    "}" +
                    "$_ = $proceed($$);";
            Util.instrumentDeclared(thisClass, ctPlayerMetaData, "save", "getPosition", replace);

        } catch ( NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        }
    }
}
