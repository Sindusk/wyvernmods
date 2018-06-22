package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Affinities;
import com.wurmonline.server.skills.Affinity;
import com.wurmonline.server.skills.SkillSystem;
import net.coldie.tools.BmlForm;

import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

public class AffinityOrbQuestion extends Question {
    protected Item affinityOrb;

    public AffinityOrbQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget, Item orb){
        super(aResponder, aTitle, aQuestion, 79, aTarget);
        this.affinityOrb = orb;
    }

    public static HashMap<Integer, Integer> affinityMap = new HashMap<>();

    @Override
    public void answer(Properties answer) {
        boolean accepted = answer.containsKey("accept") && answer.get("accept") == "true";
        if (accepted) {
            logger.info("Accepted AffinityOrb");
            int entry = Integer.parseInt(answer.getProperty("affinity"));
            int skillNum = affinityMap.get(entry);
            if(affinityOrb == null || affinityOrb.getOwnerId() != this.getResponder().getWurmId()){
                this.getResponder().getCommunicator().sendNormalServerMessage("You must own an affinity orb to obtain an affinity.");
            }else{
                if(this.getResponder() instanceof Player) {
                    Player player = (Player) this.getResponder();
                    //logger.info("Converting "+player.getName()+" to " + Deities.getDeityName(deity));
                    String skillName = SkillSystem.getNameFor(skillNum);
                    logger.info("Adding affinity for skill "+skillName+" to "+player.getName());
                    Items.destroyItem(affinityOrb.getWurmId());

                    Affinity[] affs = Affinities.getAffinities(player.getWurmId());
                    boolean found = false;
                    for (Affinity affinity : affs) {
                        if (affinity.getSkillNumber() != skillNum) continue;
                        if (affinity.getNumber() >= 5){
                            player.getCommunicator().sendSafeServerMessage("You already have the maximum amount of affinities for "+skillName);
                            return;
                        }
                        Affinities.setAffinity(player.getWurmId(), skillNum, affinity.getNumber() + 1, false);
                        found = true;
                        Items.destroyItem(affinityOrb.getWurmId());
                        player.getCommunicator().sendSafeServerMessage("Vynora infuses you with an affinity for " + skillName + "!");
                        break;
                    }
                    if (!found) {
                        Affinities.setAffinity(player.getWurmId(), skillNum, 1, false);
                        Items.destroyItem(affinityOrb.getWurmId());
                        player.getCommunicator().sendSafeServerMessage("Vynora infuses you with an affinity for " + skillName + "!");
                    }
                }else{
                    logger.info("Non-player used a "+affinityOrb.getName()+"?");
                }
            }
        }
    }

    public String getAffinities(){
        String builder = "";
        Random rand = new Random();
        if(affinityOrb.getAuxData() == 0){
            logger.info("Orb has no affinity set, creating random seed now.");
            affinityOrb.setAuxData((byte) ((1+Server.rand.nextInt(120))*(Server.rand.nextBoolean() ? 1 : -1)));
        }
        rand.setSeed(affinityOrb.getAuxData());
        logger.info("Seed set to "+affinityOrb.getAuxData());
        affinityMap.clear();
        int i = 0;
        while(i < 10){
            int num = rand.nextInt(SkillSystem.getNumberOfSkillTemplates());
            if(!affinityMap.containsValue(num)) {
                builder = builder + SkillSystem.getSkillTemplateByIndex(num).getName();
                affinityMap.put(i, SkillSystem.getSkillTemplateByIndex(num).getNumber());
                i++;
                if (i < 10) {
                    builder = builder + ",";
                }
            }
        }
        return builder;
    }

    @Override
    public void sendQuestion() {
        if(affinityOrb == null || affinityOrb.getOwnerId() != this.getResponder().getWurmId()){
            this.getResponder().getCommunicator().sendNormalServerMessage("You must own an affinity orb before being infused.");
            return;
        }
        BmlForm f = new BmlForm("");
        f.addHidden("id", String.valueOf(this.id));
        f.addBoldText("Select the affinity you would like to obtain\n", new String[0]);
        f.addRaw("harray{label{text='Select Affinity:'}dropdown{id='affinity';options='");
        f.addRaw(getAffinities());
        f.addRaw("'}}");
        f.addText("\n\n", new String[0]);
        f.beginHorizontalFlow();
        f.addButton("Accept", "accept");
        f.endHorizontalFlow();
        this.getResponder().getCommunicator().sendBml(400, 300, true, true, f.toString(), 255, 255, 255, this.title);
    }
}
