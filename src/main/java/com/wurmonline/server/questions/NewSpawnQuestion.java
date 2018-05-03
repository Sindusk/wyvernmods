package com.wurmonline.server.questions;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.*;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Spawnpoint;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import net.coldie.tools.BmlForm;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Properties;

public class NewSpawnQuestion extends Question {

    public NewSpawnQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget){
        super(aResponder, aTitle, aQuestion, 79, aTarget);
    }

    public void spawn(Player p, Spawnpoint spawnpoint) {
        if (p.isDead()) {
            p.addNewbieBuffs();
            p.setLayer(0, false);
            boolean found = false;
            if (p.isUndead()) {
                float[] txty = Player.findRandomSpawnX(false, false);
                float posX = txty[0];
                float posY = txty[1];
                p.setTeleportPoints(posX, posY, 0, 0);
                p.startTeleporting();
                p.getCommunicator().sendNormalServerMessage("You are cast back into the horrible light.");
            } else {
                p.setTeleportPoints(spawnpoint.tilex, spawnpoint.tiley, spawnpoint.surfaced ? 0 : -1, 0);
                p.startTeleporting();
                p.getCommunicator().sendNormalServerMessage("You are cast back into the light.");
            }
            p.getCommunicator().sendTeleport(false);
            try { //p.setDead(false);
                ReflectionUtil.callPrivateMethod(p, ReflectionUtil.getMethod(p.getClass(), "setDead"), false);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            p.spawnpoints = null;
        }
    }

    @Override
    public void answer(Properties answer) {
        boolean accepted = answer.containsKey("accept") && answer.get("accept") == "true";
        if (accepted) {
            logger.info("Accepted NewSpawnQuestion");
            int entry = Integer.parseInt(answer.getProperty("spawnpoint"));
            Spawnpoint spawn = spawns.get(entry);
            this.spawn((Player) this.getResponder(), spawn);
        }else{
            boolean transfer = answer.containsKey("transfer") && answer.get("transfer") == "true";
            if(transfer) {
                try {
                    ServerEntry targetServer = Servers.localServer.serverSouth;
                    Player player = Players.getInstance().getPlayer(this.getResponder().getWurmId());
                    if(targetServer == null){
                        player.getCommunicator().sendNormalServerMessage("Error: Something went wrong [TARGETSERVER=NULL].");
                    }
                    if (!targetServer.isAvailable(player.getPower(), true)) {
                        player.getCommunicator().sendNormalServerMessage(targetServer.name + " is not currently available.");
                    } else {
                        player.sendTransfer(Server.getInstance(), targetServer.EXTERNALIP, Integer.parseInt(targetServer.EXTERNALPORT), targetServer.INTRASERVERPASSWORD, targetServer.getId(), -1, -1, true, false, player.getKingdomId());
                    }
                } catch (NoSuchPlayerException e) {
                    logger.info("Could not find player for WurmId " + this.getResponder().getWurmId() + " [" + this.getResponder().getName() + "]");
                    e.printStackTrace();
                }
            }else {
                this.getResponder().getCommunicator().sendNormalServerMessage("You can bring the spawn question back by typing /respawn in a chat window.");
            }
        }
    }

    protected static HashMap<Integer, Spawnpoint> spawns = new HashMap<>();

    public Spawnpoint getRandomSpawnpoint(byte spawnNums){
        int i = 1000;
        while(i > 0){
            i--;
            int x = Server.rand.nextInt(Server.surfaceMesh.getSize());
            int y = Server.rand.nextInt(Server.surfaceMesh.getSize());
            short height = Tiles.decodeHeight(Server.surfaceMesh.getTile(x, y));
            if(height > 0 && height < 1000 && Creature.getTileSteepness(x, y, true)[1] < 30){
                Village v = Villages.getVillage(x, y, true);
                if (v == null) {
                    for (int vx = -50; vx < 50; vx += 5) {
                        for (int vy = -50; vy < 50 && (v = Villages.getVillage(x + vx, y + vy, true)) == null; vy += 5) {
                        }
                    }
                }
                if(v != null){
                    continue;
                }
                return new Spawnpoint(spawnNums, "Random location", (short) x, (short) y, true);
            }
        }
        return null;
    }
    public String getSpawnpointOptions(){
        String builder = "";
        byte spawnNums = 0;
        if(this.getResponder().citizenVillage != null){
            Village v = this.getResponder().citizenVillage;
            short tpx = (short)v.getTokenX();
            short tpy = (short)v.getTokenY();
            Spawnpoint home = new Spawnpoint(spawnNums, "Token of " + v.getName(), tpx, tpy, v.isOnSurface());
            spawns.put((int) spawnNums, home);
            builder += home.description+",";
            spawnNums++;
        }
        Spawnpoint random = getRandomSpawnpoint(spawnNums);
        spawns.put((int) spawnNums, random);
        builder += random.description;
        return builder;
    }

    @Override
    public void sendQuestion() {
        BmlForm f = new BmlForm("");
        f.addHidden("id", String.valueOf(this.id));
        f.addBoldText("Where would you like to spawn?\n\n", new String[0]);
        f.addText("\n\n", new String[0]);
        f.addRaw("harray{label{text='Respawn Point:'}dropdown{id='spawnpoint';options='");
        f.addRaw(getSpawnpointOptions());
        f.addRaw("'}}");
        f.addText("\n\n", new String[0]);
        f.beginHorizontalFlow();
        f.addButton("Accept", "accept");
        f.endHorizontalFlow();
        f.addText("\n\n", new String[0]);
        f.addBoldText("Or transfer back to the PvE server:");
        f.addText("\n\n", new String[0]);
        f.beginHorizontalFlow();
        f.addButton("Transfer to PvE", "transfer");
        f.endHorizontalFlow();
        this.getResponder().getCommunicator().sendBml(400, 300, true, true, f.toString(), 128, 50, 50, this.title);
    }
}
