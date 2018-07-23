package com.wurmonline.server.questions;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.server.utils.DbUtilities;
import net.coldie.tools.BmlForm;
import org.gotti.wurmunlimited.modsupport.ModSupportDb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class LeaderboardSkillQuestion extends Question {
    protected int skillNum;

    public LeaderboardSkillQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget, int skillNum){
        super(aResponder, aTitle, aQuestion, 79, aTarget);
        this.skillNum = skillNum;
    }

    @Override
    public void answer(Properties answer) {
        boolean accepted = answer.containsKey("okay") && answer.get("okay") == "true";
        if (accepted) {
            LeaderboardQuestion lbq = new LeaderboardQuestion(this.getResponder(), "Leaderboard", "Which leaderboard would you like to view?", this.getResponder().getWurmId());
            lbq.sendQuestion();
        }
    }

    public int[] getSkilLevelColors(double skill){
        int[] colors = new int[3];
        colors[0] = 0; // No red value
        if(skill >= 90){
            double percentTowards100 = 1-((100-skill)*0.1); // Division by 10
            double greenPower = 128 + (128*percentTowards100);
            colors[1] = (int) Math.min(255, greenPower);
            colors[2] = (int) Math.max(0, 255-greenPower);
        }else if(skill >= 50){
            double percentTowards90 = 1-((90-skill)*0.025); // Division by 40
            double greenPower = percentTowards90*128;
            colors[1] = (int) Math.max(128, greenPower);
            colors[2] = (int) Math.min(255, 255-greenPower);
        }else{
            double percentTowards50 = 1-((50-skill)*0.02); // Division by 50
            double otherPower = 255 - (percentTowards50*255);
            colors[0] = (int) Math.min(255, otherPower);
            colors[1] = (int) Math.min(255, Math.max(128, otherPower));
            colors[2] = 255;
        }
        return colors;
    }

    protected HashMap<String, Integer> optIn = new HashMap<>();
    protected void identifyOptIn(){
        String name;
        int opted;
        Connection dbcon;
        PreparedStatement ps;
        ResultSet rs;
        try {
            dbcon = ModSupportDb.getModSupportDb();
            ps = dbcon.prepareStatement("SELECT * FROM LeaderboardOpt");
            rs = ps.executeQuery();
            while (rs.next()) {
                name = rs.getString("name");
                opted = rs.getInt("OPTIN");
                optIn.put(name, opted);
            }
            rs.close();
            ps.close();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendQuestion() {
        BmlForm f = new BmlForm("");
        f.addHidden("id", String.valueOf(this.id));
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Double> skills = new ArrayList<>();
        ArrayList<Integer> deities = new ArrayList<>();
        String name;
        double skill;
        int deity;
        String extra = "";

        // Populates HashMap with latest opt-in data.
        identifyOptIn();

        Connection dbcon;
        PreparedStatement ps;
        ResultSet rs;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement("SELECT players.name, skills.value, players.deity FROM skills JOIN players ON skills.owner = players.wurmid WHERE skills.number = " + skillNum + " AND (players.power = 0) ORDER BY skills.value DESC LIMIT 20");
            rs = ps.executeQuery();
            while(rs.next()){
                name = rs.getString(1);
                skill = rs.getDouble(2);
                deity = rs.getInt(3);
                names.add(name);
                skills.add(skill);
                deities.add(deity);
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        f.addBoldText("Top 20 players in "+this.getQuestion());
        f.addText("\n\n");
        int i = 0;
        DecimalFormat df = new DecimalFormat(".000");
        while(i < names.size() && i < skills.size()){
            name = names.get(i);
            if(!optIn.containsKey(name)){
                name = "Unknown";
            }else if(optIn.get(name).equals(0)){
                name = "Unknown";
            }
            if(skillNum == SkillList.CHANNELING){
                extra = " ("+ Deities.getDeityName(deities.get(i))+")";
            }
            int[] color = getSkilLevelColors(skills.get(i));
            if(names.get(i).equals(this.getResponder().getName())){
                name = names.get(i);
                f.addBoldColoredText(df.format(skills.get(i)) + " - " + name + extra, color[0], color[1], color[2]);
            }else{
                f.addColoredText(df.format(skills.get(i)) + " - " + name + extra, color[0], color[1], color[2]);
            }
            i++;
        }
        f.addText(" \n");
        f.beginHorizontalFlow();
        f.addButton("Ok", "okay");
        f.endHorizontalFlow();
        f.addText(" \n");
        this.getResponder().getCommunicator().sendBml(400, 500, true, true, f.toString(), 150, 150, 200, this.title);
    }
}
