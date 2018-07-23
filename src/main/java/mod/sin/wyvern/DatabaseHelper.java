package mod.sin.wyvern;

import com.wurmonline.server.players.Player;
import org.gotti.wurmunlimited.modsupport.ModSupportDb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseHelper {
    private static Logger logger = Logger.getLogger(DatabaseHelper.class.getName());

    public static void onPlayerLogin(Player p){
        Connection dbcon;
        PreparedStatement ps;
        boolean foundLeaderboardOpt = false;
        try {
            dbcon = ModSupportDb.getModSupportDb();
            ps = dbcon.prepareStatement("SELECT * FROM LeaderboardOpt");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (!rs.getString("name").equals(p.getName())) continue;
                foundLeaderboardOpt = true;
            }
            rs.close();
            ps.close();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (!foundLeaderboardOpt) {
            logger.info("No leaderboard entry for "+p.getName()+". Creating one.");
            try {
                dbcon = ModSupportDb.getModSupportDb();
                ps = dbcon.prepareStatement("INSERT INTO LeaderboardOpt (name) VALUES(?)");
                ps.setString(1, p.getName());
                ps.executeUpdate();
                ps.close();
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        boolean foundPlayerStats = false;
        try {
            dbcon = ModSupportDb.getModSupportDb();
            ps = dbcon.prepareStatement("SELECT * FROM PlayerStats");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (!rs.getString("NAME").equals(p.getName())) continue;
                foundPlayerStats = true;
            }
            rs.close();
            ps.close();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (!foundPlayerStats) {
            logger.info("No player stats entry for "+p.getName()+". Creating one.");
            try {
                dbcon = ModSupportDb.getModSupportDb();
                ps = dbcon.prepareStatement("INSERT INTO PlayerStats (NAME) VALUES(\"" + p.getName() + "\")");
                ps.executeUpdate();
                ps.close();
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void onServerStarted(){
        try {
            Connection con = ModSupportDb.getModSupportDb();
            String sql;
            String tableName = "LeaderboardOpt";
            if (!ModSupportDb.hasTable(con, tableName)) {
                logger.info(tableName+" table not found in ModSupport. Creating table now.");
                sql = "CREATE TABLE "+tableName+" (name VARCHAR(30) NOT NULL DEFAULT 'Unknown', OPTIN INT NOT NULL DEFAULT 0)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.execute();
                ps.close();
            }
            tableName = "SteamIdMap";
            if (!ModSupportDb.hasTable(con, tableName)) {
                logger.info(tableName+" table not found in ModSupport. Creating table now.");
                sql = "CREATE TABLE "+tableName+" (NAME VARCHAR(30) NOT NULL DEFAULT 'Unknown', STEAMID LONG NOT NULL DEFAULT 0)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.execute();
                ps.close();
            }
            tableName = "PlayerStats";
            if (!ModSupportDb.hasTable(con, tableName)) {
                logger.info(tableName+" table not found in ModSupport. Creating table now.");
                sql = "CREATE TABLE "+tableName+" (NAME VARCHAR(30) NOT NULL DEFAULT 'Unknown', KILLS INT NOT NULL DEFAULT 0, DEATHS INT NOT NULL DEFAULT 0, DEPOTS INT NOT NULL DEFAULT 0, HOTAS INT NOT NULL DEFAULT 0, TITANS INT NOT NULL DEFAULT 0, UNIQUES INT NOT NULL DEFAULT 0)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.execute();
                ps.close();
            }else{
                logger.info("Found "+tableName+". Checking if it has a unique column.");
                ResultSet rs = con.getMetaData().getColumns(null, null, tableName, "UNIQUES");
                if(rs.next()){
                    logger.info(tableName+" already has a uniques column.");
                }else{
                    logger.info("Detected no uniques column in "+tableName);
                    sql = "ALTER TABLE "+tableName+" ADD COLUMN UNIQUES INT NOT NULL DEFAULT 0";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.execute();
                    ps.close();
                }
            }
            tableName = "ObjectiveTimers";
            if (!ModSupportDb.hasTable(con, tableName)) {
                logger.info(tableName+" table not found in ModSupport. Creating table now.");
                sql = "CREATE TABLE "+tableName+" (ID VARCHAR(30) NOT NULL DEFAULT 'Unknown', TIMER LONG NOT NULL DEFAULT 0)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.execute();
                ps.close();
                try {
                    Connection dbcon;
                    dbcon = ModSupportDb.getModSupportDb();
                    ps = dbcon.prepareStatement("INSERT INTO ObjectiveTimers (ID, TIMER) VALUES(\"DEPOT\", 0)");
                    ps.executeUpdate();
                    ps.close();
                }
                catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                try {
                    Connection dbcon;
                    dbcon = ModSupportDb.getModSupportDb();
                    ps = dbcon.prepareStatement("INSERT INTO ObjectiveTimers (ID, TIMER) VALUES(\"TITAN\", 0)");
                    ps.executeUpdate();
                    ps.close();
                }
                catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            SupplyDepots.initializeDepotTimer();
            Titans.initializeTitanTimer();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
