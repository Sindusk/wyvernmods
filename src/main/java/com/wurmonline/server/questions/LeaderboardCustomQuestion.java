package com.wurmonline.server.questions;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.SkillSystem;
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

public class LeaderboardCustomQuestion extends Question {
    protected int entryNum;

    public LeaderboardCustomQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget, int entryNum){
        super(aResponder, aTitle, aQuestion, 79, aTarget);
        this.entryNum = entryNum;
    }

    @Override
    public void answer(Properties answer) {
        boolean accepted = answer.containsKey("okay") && answer.get("okay") == "true";
        if (accepted) {
            LeaderboardQuestion lbq = new LeaderboardQuestion(this.getResponder(), "Leaderboard", "Which leaderboard would you like to view?", this.getResponder().getWurmId());
            lbq.sendQuestion();
        }
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

    protected ArrayList<String> names = new ArrayList<>();
    protected ArrayList<Double> values = new ArrayList<>();
    protected ArrayList<String> extra = new ArrayList<>();
    protected void totalSkills(int limit){
        Connection dbcon;
        PreparedStatement ps;
        ResultSet rs;
        String name;
        int skillNum;
        double skill;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement("SELECT players.name, achievements.counter FROM achievements JOIN players ON achievements.player = players.wurmid WHERE achievements.achievement = 371 AND players.power = 0 ORDER BY achievements.counter DESC LIMIT "+limit);
            rs = ps.executeQuery();
            while(rs.next()){
                name = rs.getString(1);
                skill = rs.getDouble(2);
                names.add(name);
                values.add(skill);
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    protected void topAnySkill(int limit){
        Connection dbcon;
        PreparedStatement ps;
        ResultSet rs;
        String name;
        int skillNum;
        double skill;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement("SELECT players.name, skills.number, skills.value FROM skills JOIN players ON skills.owner = players.wurmid WHERE players.power = 0 ORDER BY skills.value DESC LIMIT "+limit);
            rs = ps.executeQuery();
            while(rs.next()){
                name = rs.getString(1);
                skillNum = rs.getInt(2);
                skill = rs.getDouble(3);
                names.add(name);
                values.add(skill);
                extra.add(SkillSystem.getNameFor(skillNum));
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    //select wurmid, count(*) from titles group by wurmid order by count(*) desc;
    protected void totalTitles(int limit){
        Connection dbcon;
        PreparedStatement ps;
        ResultSet rs;
        String name;
        int skillNum;
        double skill;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement("SELECT players.name, COUNT(titles.wurmid) as Count FROM titles JOIN players ON titles.wurmid = players.wurmid WHERE players.power = 0 GROUP BY titles.wurmid ORDER BY Count DESC LIMIT "+limit);
            rs = ps.executeQuery();
            while(rs.next()){
                name = rs.getString(1);
                skill = rs.getDouble(2);
                names.add(name);
                values.add(skill);
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    protected void topPlayerStats(String statName, int limit){
        Connection dbcon;
        PreparedStatement ps;
        ResultSet rs;
        String name;
        int skillNum;
        double stat;
        try {
            dbcon = ModSupportDb.getModSupportDb();
            ps = dbcon.prepareStatement("SELECT name, "+statName+" FROM PlayerStats ORDER BY "+statName+" DESC LIMIT "+limit);
            rs = ps.executeQuery();
            while(rs.next()){
                name = rs.getString(1);
                stat = rs.getDouble(2);
                names.add(name);
                values.add(stat);
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendQuestion() {
        BmlForm f = new BmlForm("");
        f.addHidden("id", String.valueOf(this.id));

        // Populates HashMap with latest opt-in data.
        identifyOptIn();

        // Identify and execute correct list generation.
        boolean format = false;
        boolean ignoreOpt = false;
        int limit = 20;
        switch(entryNum){
            case 0:
                limit = 50;
                totalSkills(limit);
                break;
            case 1:
                limit = 50;
                topAnySkill(limit);
                format = true;
                break;
            case 2:
                limit = 50;
                totalTitles(limit);
                break;
            case 3:
                limit = 10;
                topPlayerStats("kills", limit);
                ignoreOpt = true;
                break;
            case 4:
                limit = 10;
                topPlayerStats("deaths", limit);
                ignoreOpt = true;
                break;
            case 5:
                limit = 10;
                topPlayerStats("depots", limit);
                ignoreOpt = true;
                break;
        }

        f.addBoldText("Top "+limit+" players in "+this.getQuestion(), new String[0]);
        f.addText("\n\n", new String[0]);
        int i = 0;
        DecimalFormat df = new DecimalFormat(".000");
        if(!format){
            df = new DecimalFormat("#");
        }
        String name;
        String line;
        while(i < names.size() && i < values.size()){
            name = names.get(i);
            if(!ignoreOpt) {
                if (!optIn.containsKey(name)) {
                    name = "Unknown";
                } else if (optIn.get(name).equals(0)) {
                    name = "Unknown";
                }
            }
            line = df.format(values.get(i)) + " - " + name;
            if(extra.size() >= i+1){
                line = line + " ("+ extra.get(i)+")";
            }
            if(names.get(i).equals(this.getResponder().getName())){
                f.addBoldText(line);
            }else{
                f.addText(line);
            }
            i++;
        }
        f.addText(" \n", new String[0]);
        f.beginHorizontalFlow();
        f.addButton("Ok", "okay");
        f.endHorizontalFlow();
        f.addText(" \n", new String[0]);
        this.getResponder().getCommunicator().sendBml(400, 500, true, true, f.toString(), 150, 150, 200, this.title);
    }
}