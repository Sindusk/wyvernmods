package com.wurmonline.server.questions;

import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.AchievementTemplate;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.SkillTemplate;
import mod.sin.wyvern.AchievementChanges;
import net.coldie.tools.BmlForm;
import org.gotti.wurmunlimited.modsupport.ModSupportDb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class LeaderboardQuestion extends Question {

    public LeaderboardQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget){
        super(aResponder, aTitle, aQuestion, 79, aTarget);
    }

    protected void setPlayerOptStatus(String name, int opt){
        Connection dbcon;
        PreparedStatement ps;
        try {
            dbcon = ModSupportDb.getModSupportDb();
            ps = dbcon.prepareStatement("UPDATE LeaderboardOpt SET OPTIN = " + opt + " WHERE name = \"" + name + "\"");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void answer(Properties answer) {
        boolean skill = answer.containsKey("accept") && answer.get("accept") == "true";
        boolean achievements = answer.containsKey("achievements") && answer.get("achievements") == "true";
        boolean custom = answer.containsKey("custom") && answer.get("custom") == "true";
        if (skill) {
            int entry = Integer.parseInt(answer.getProperty("leaderboard"));
            String val = skillMap.get(entry);
            int skillNum = skillIdMap.get(entry);
            LeaderboardSkillQuestion lbsq = new LeaderboardSkillQuestion(this.getResponder(), "Leaderboard", val, this.getResponder().getWurmId(), skillNum);
            lbsq.sendQuestion();
        }else if(achievements){
            int entry = Integer.parseInt(answer.getProperty("achievementboard"));
            String val = achievementMap.get(entry);
            int achievementNum = achievementIdMap.get(entry);
            LeaderboardAchievementQuestion lbaq = new LeaderboardAchievementQuestion(this.getResponder(), "Leaderboard", val, this.getResponder().getWurmId(), achievementNum);
            lbaq.sendQuestion();
        }else if(custom){
            int entry = Integer.parseInt(answer.getProperty("customboard"));
            String val = customMap.get(entry);
            LeaderboardCustomQuestion lbcq = new LeaderboardCustomQuestion(this.getResponder(), "Leaderboard", val, this.getResponder().getWurmId(), entry);
            lbcq.sendQuestion();
        }else{
            String name = this.getResponder().getName();
            if(answer.containsKey("optin") && answer.get("optin") == "true"){
                logger.info("Player "+name+" has opted into Leaderboard system.");
                setPlayerOptStatus(name, 1);
                this.getResponder().getCommunicator().sendNormalServerMessage("You have opted into the Leaderboard system!");
            }else if(answer.containsKey("optout") && answer.get("optout") == "true"){
                logger.info("Player "+name+" has opted out of the Leaderboard system.");
                setPlayerOptStatus(name, 0);
                this.getResponder().getCommunicator().sendNormalServerMessage("You have opted out of the Leaderboard system.");
            }
        }
    }

    protected HashMap<Integer, String> skillMap = new HashMap<>();
    protected HashMap<Integer, Integer> skillIdMap = new HashMap<>();
    protected HashMap<Integer, String> customMap = new HashMap<>();

    public String getSkillOptions(){
        String builder = "";
        SkillTemplate[] skillTemplates = SkillSystem.getAllSkillTemplates();
        Arrays.sort(skillTemplates, new Comparator<SkillTemplate>(){
            public int compare(SkillTemplate o1, SkillTemplate o2){
                return o1.getName().compareTo(o2.getName());
            }
        });
        int i = 0;
        int index = 0;
        skillMap.clear();
        while(i < skillTemplates.length){
            builder = builder + skillTemplates[i].getName();
            skillMap.put(index, skillTemplates[i].getName());
            skillIdMap.put(index, skillTemplates[i].getNumber());
            i++;
            index++;
            if(i < skillTemplates.length){
                builder = builder + ",";
            }
        }
        return builder;
    }

    protected HashMap<Integer, String> achievementMap = new HashMap<>();
    protected HashMap<Integer, Integer> achievementIdMap = new HashMap<>();

    public String getAchievementOptions(){
        String builder = "";
        Collection<AchievementTemplate> achievements = AchievementChanges.goodAchievements.values();
        List<AchievementTemplate> sortedAchievements = new ArrayList<>(achievements);
        sortedAchievements.sort(new Comparator<AchievementTemplate>() {
            public int compare(AchievementTemplate o1, AchievementTemplate o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        int i = 0;
        int index = 0;
        achievementMap.clear();
        while(i < sortedAchievements.size()){
            builder = builder + sortedAchievements.get(i).getName();
            achievementMap.put(i, sortedAchievements.get(i).getName());
            achievementIdMap.put(i, sortedAchievements.get(i).getNumber());
            i++;
            if(i < sortedAchievements.size()){
                builder = builder + ",";
            }
        }
        builder = builder.replaceAll("'", "");
        return builder;
    }

    public String getCustomOptions(){
        String builder = "Total Skill";
        customMap.put(0, "Total Skill");
        builder = builder + ",High Skills";
        customMap.put(1, "High Skills");
        builder = builder + ",Most Titles";
        customMap.put(2, "Most Titles");
        builder = builder + ",Uniques Slain";
        customMap.put(3, "Uniques Slain");
        builder = builder + ",Titans Slain";
        customMap.put(4, "Titans Slain");
        builder = builder + ",Most Affinities";
        customMap.put(5, "Most Affinities");
        builder = builder + ",Most Unique Achievements";
        customMap.put(6, "Most Unique Achievements");
        builder = builder + ",Largest Structures";
        customMap.put(7, "Largest Structures");
        builder = builder + ",Most Populated Villages";
        customMap.put(8, "Most Populated Villages");
        if(Servers.localServer.PVPSERVER || this.getResponder().getPower() >= 5){
            builder = builder + ",PvP Kills";
            customMap.put(9, "PvP Kills");
            builder = builder + ",PvP Deaths";
            customMap.put(10, "PvP Deaths");
            builder = builder + ",Depots Captured";
            customMap.put(11, "PvP Depots Captured");
        }
        return builder;
    }

    @Override
    public void sendQuestion() {
        BmlForm f = new BmlForm("");
        f.addHidden("id", String.valueOf(this.id));
        int opted;
        Connection dbcon;
        PreparedStatement ps;
        ResultSet rs;
        try {
            dbcon = ModSupportDb.getModSupportDb();
            ps = dbcon.prepareStatement("SELECT * FROM LeaderboardOpt WHERE name = \"" + this.getResponder().getName() + "\"");
            rs = ps.executeQuery();
            opted = rs.getInt("OPTIN");
            rs.close();
            ps.close();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        f.addBoldText("You are currently " + (opted == 0 ? "not " : "") + "opted into the leaderboard system.\n\n", new String[0]);
        f.addBoldText("Skill Leaderboards");
        f.addRaw("harray{label{text='View leaderboard:'}dropdown{id='leaderboard';options='");
        f.addRaw(getSkillOptions());
        f.addRaw("'}}");
        f.beginHorizontalFlow();
        f.addButton("Accept", "accept");
        f.endHorizontalFlow();
        f.addText(" \n\n", new String[0]);
        f.addBoldText("Achievement Leaderboards");
        f.addRaw("harray{label{text='View leaderboard:'}dropdown{id='achievementboard';options='");
        f.addRaw(getAchievementOptions());
        f.addRaw("'}}");
        f.beginHorizontalFlow();
        f.addButton("Accept", "achievements");
        f.endHorizontalFlow();
        f.addText(" \n\n", new String[0]);
        f.addBoldText("Special Leaderboards");
        f.addRaw("harray{label{text='View leaderboard:'}dropdown{id='customboard';options='");
        f.addRaw(getCustomOptions());
        f.addRaw("'}}");
        f.beginHorizontalFlow();
        f.addButton("Accept", "custom");
        f.endHorizontalFlow();
        f.addText(" \n\n", new String[0]);
        f.addBoldText("Opt into or out of the Leaderboard system.");
        f.beginHorizontalFlow();
        f.addButton("Opt In", "optin");
        f.addButton("Opt Out", "optout");
        f.endHorizontalFlow();
        this.getResponder().getCommunicator().sendBml(400, 500, true, true, f.toString(), 150, 150, 200, this.title);
    }
}
