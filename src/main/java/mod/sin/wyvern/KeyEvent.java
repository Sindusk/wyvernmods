package mod.sin.wyvern;

import com.wurmonline.server.Message;
import com.wurmonline.server.Server;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class KeyEvent {
    public static Logger logger = Logger.getLogger(KeyEvent.class.getName());

    protected static class Response{
        protected int index;
        protected long startTime;
        protected long endTime;
        protected String response = "";
        public Response(int index, int startSecond, int endSecond){
            this.index = index;
            this.startTime = startSecond * TimeConstants.SECOND_MILLIS;
            this.endTime = endSecond * TimeConstants.SECOND_MILLIS;
        }
        public void setResponse(String response){
            this.response = response;
        }
        public String getResponse(){
            return response;
        }
    }

    protected static ArrayList<Response> responses = new ArrayList<>();
    protected static void resetResponses(){
        responses.clear();
        responses.add(new Response(0, 40, 60)); // Desire
        responses.add(new Response(1, 120, 150)); // Fo's Power
        responses.add(new Response(2, 170, 200)); // Magranon's Power
        responses.add(new Response(3, 220, 250)); // Vynora's Power
        responses.add(new Response(4, 270, 300)); // Liblia's Power
        responses.add(new Response(5, 340, 370)); // Ascend Template

        // Reset booleans
        hasWeaponEnchant = false;
        hasCreatureEnchant = false;
        hasIndustryEnchant = false;
        hasHeal = false;
        hasTame = false;
    }
    public static String getResponse(int index){
        for(Response r : responses){
            if(r.index == index){
                return r.getResponse();
            }
        }
        return "";
    }

    public static boolean hasWeaponEnchant = false;
    public static boolean hasCreatureEnchant = false;
    public static boolean hasIndustryEnchant = false;
    public static boolean hasHeal = false;
    public static boolean hasTame = false;

    public static String foPower = "";
    public static String magranonPower = "";
    public static String vynoraPower = "";
    public static String libilaPower = "";
    public static String ascendTemplate = "";

    public static String getFoPowers(){
        return "I offer the following: Life Transfer, Oakshell, Light of Fo, Charm";
    }
    public static boolean isValidFo(){
        return isValidFo(getResponse(1).toLowerCase());
    }
    public static boolean isValidFo(String response){
        if(response.contains("life transfer") || response.contains("lifetransfer") || response.equals("lt")){
            foPower = "Life Transfer";
            hasWeaponEnchant = true;
            return true;
        }else if(response.contains("oakshell") || response.contains("oak shell")){
            foPower = "Oakshell";
            hasCreatureEnchant = true;
            return true;
        }else if(response.contains("light of fo") || response.contains("lof") || response.contains("light fo")){
            foPower = "Light of Fo";
            hasHeal = true;
            return true;
        }else if(response.contains("charm") || response.contains("tame")){
            foPower = "Charm";
            hasTame = true;
            return true;
        }
        return false;
    }
    public static String getMagranonPowers(){
        String builder = "I offer the following: ";
        boolean started = false;
        if(!hasWeaponEnchant){
            builder += "Flaming Aura";
            started = true;
        }
        if(!hasCreatureEnchant){
            if(started){
                builder += ", Frantic Charge";
            }else{
                builder += "Frantic Charge";
                started = true;
            }
        }
        if(!hasIndustryEnchant){
            if(started){
                builder += ", Efficiency";
            }else{
                builder += "Efficiency";
                started = true;
            }
        }
        if(!hasHeal){
            if(started){
                builder += ", Mass Stamina";
            }else{
                builder += "Mass Stamina";
                started = true;
            }
        }
        if(!hasTame){
            if(started){
                builder += ", Dominate";
            }else{
                builder += "Dominate";
            }
        }
        builder += ", Strongwall";
        return builder;
    }
    public static void setRandomMagranonPower(){
        if(!hasWeaponEnchant){
            magranonPower = "Flaming Aura";
            hasWeaponEnchant = true;
        }else if(!hasCreatureEnchant){
            magranonPower = "Frantic Charge";
            hasCreatureEnchant = true;
        }else if(!hasIndustryEnchant){
            magranonPower = "Efficiency";
            hasIndustryEnchant = true;
        }else{
            magranonPower = "Strongwall";
        }
    }
    public static boolean isValidMagranon(){
        return isValidMagranon(getResponse(2).toLowerCase());
    }
    public static boolean isValidMagranon(String response){
        if((response.contains("flaming aura") || response.contains("flamingaura") || response.contains("flame aura") || response.equals("fa"))){
            magranonPower = "Flaming Aura";
            hasWeaponEnchant = true;
            return true;
        }else if((response.contains("frantic") || response.contains("charge"))){
            magranonPower = "Frantic Charge";
            hasCreatureEnchant = true;
            return true;
        }else if((response.contains("mass stam") || response.contains("stamina"))){
            magranonPower = "Mass Stamina";
            hasHeal = true;
            return true;
        }else if((response.contains("effic") || response.contains("effec"))){
            magranonPower = "Efficiency";
            hasIndustryEnchant = true;
            return true;
        }else if((response.contains("dominate") || response.contains("dom"))){
            magranonPower = "Dominate";
            hasTame = true;
            return true;
        }else if(response.contains("wall") || response.contains("strong")){
            magranonPower = "Strongwall";
            return true;
        }
        return false;
    }
    public static String getVynoraPowers(){
        String builder = "I offer the following: ";
        builder += "Wind of Ages, Circle of Cunning, Aura of Shared Pain";
        if(!hasWeaponEnchant){
            builder += ", Frostbrand, Nimbleness, Mind Stealer";
        }
        if(!hasCreatureEnchant){
            builder += ", Excel";
        }
        builder += ", Opulence";
        return builder;
    }
    public static void setRandomVynoraPower(){
        if(!hasWeaponEnchant){
            vynoraPower = "Nimbleness";
            hasWeaponEnchant = true;
        }else if(!hasCreatureEnchant){
            vynoraPower = "Excel";
            hasCreatureEnchant = true;
        }else{
            vynoraPower = "Wind of Ages";
            hasIndustryEnchant = true;
        }
    }
    public static boolean isValidVynora(){
        return isValidVynora(getResponse(3).toLowerCase());
    }
    public static boolean isValidVynora(String response){
        if((response.contains("frost"))){
            vynoraPower = "Frostbrand";
            hasWeaponEnchant = true;
            return true;
        }else if((response.contains("nimb"))){
            vynoraPower = "Nimbleness";
            hasWeaponEnchant = true;
            return true;
        }else if((response.contains("mind stealer") || response.contains("mindstealer"))){
            vynoraPower = "Mind Stealer";
            hasWeaponEnchant = true;
            return true;
        }else if((response.contains("excel"))){
            vynoraPower = "Excel";
            hasCreatureEnchant = true;
            return true;
        }else if(response.contains("wind") || response.equals("woa")){
            vynoraPower = "Wind of Ages";
            hasIndustryEnchant = true;
            return true;
        }else if(response.contains("circle") || response.contains("cunning") || response.equals("coc")){
            vynoraPower = "Circle of Cunning";
            hasIndustryEnchant = true;
            return true;
        }else if(response.contains("aura") || response.contains("shared") || response.equals("aosp")){
            vynoraPower = "Aura of Shared Pain";
            return true;
        }else if(response.contains("opulence")){
            vynoraPower = "Opulence";
            return true;
        }
        return false;
    }
    public static String getLibilaPowers(){
        String builder = "I offer the following: ";
        builder += "Web Armour";
        if(!hasWeaponEnchant){
            builder += ", Bloodthirst, Rotting Touch";
        }
        if(!hasCreatureEnchant){
            builder += ", Truehit";
        }
        if(!hasHeal){
            builder += ", Scorn of Libila";
        }
        if(!hasIndustryEnchant){
            builder += ", Blessings of the Dark";
        }
        if(!hasTame){
            builder += ", Rebirth";
        }
        builder += ", Drain Health, Drain Stamina";
        return builder;
    }
    public static void setRandomLibilaPower(){
        if(!hasWeaponEnchant){
            libilaPower = "Rotting Touch";
            hasWeaponEnchant = true;
        }else if(!hasCreatureEnchant){
            libilaPower = "Truehit";
            hasCreatureEnchant = true;
        }else if(!hasTame){
            libilaPower = "Rebirth";
            hasTame = true;
        }else{
            libilaPower = "Drain Health";
        }
    }
    public static boolean isValidLibila(){
        return isValidLibila(getResponse(4).toLowerCase());
    }
    public static boolean isValidLibila(String response){
        if((response.contains("bloodthirst") || response.contains("blood thirst"))){
            libilaPower = "Bloodthirst";
            hasWeaponEnchant = true;
            return true;
        }else if((response.contains("rotting") || response.contains("touch"))){
            libilaPower = "Rotting Touch";
            hasWeaponEnchant = true;
            return true;
        }else if((response.contains("truehit") || response.contains("truhit"))){
            libilaPower = "Truehit";
            hasCreatureEnchant = true;
            return true;
        }else if((response.contains("scorn"))){
            libilaPower = "Scorn of Libila";
            hasHeal = true;
            return true;
        }else if((response.contains("blessing") || response.contains("dark") || response.equals("botd"))){
            libilaPower = "Blessings of the Dark";
            hasIndustryEnchant = true;
            return true;
        }else if((response.contains("rebirth"))){
            libilaPower = "Rebirth";
            hasTame = true;
            return true;
        }else if(response.contains("health")){
            libilaPower = "Drain Health";
            return true;
        }else if(response.contains("stam")){
            libilaPower = "Drain Stamina";
            return true;
        }else if(response.contains("web") || response.contains("armour")){
            libilaPower = "Web Armour";
            return true;
        }
        return false;
    }
    public static void setRandomAscendTemplate(){
        if(Server.rand.nextBoolean()) {
            ascendTemplate = "Fo";
        }else{
            ascendTemplate = "Vynora";
        }
    }
    public static boolean isValidAscendTemplate(){
        return isValidAscendTemplate(getResponse(5).toLowerCase());
    }
    public static boolean isValidAscendTemplate(String response){
        if(response.equals("fo")){
            ascendTemplate = "Fo";
            return true;
        }else if(response.contains("mag")){
            ascendTemplate = "Magranon";
            return true;
        }else if(response.contains("vyn")){
            ascendTemplate = "Vynora";
            return true;
        }else if(response.contains("lib")){
            ascendTemplate = "Libila";
            return true;
        }
        return false;
    }

    protected static boolean isValidResponse(int index, String message){
        if (index == 0){ // Desire
            return true;
        }else if(index == 1){ // Fo Power
            return isValidFo(message);
        }else if(index == 2){ // Magranon Power
            return isValidMagranon(message);
        }else if(index == 3) { // Vynora Power
            return isValidVynora(message);
        }else if(index == 4){ // Libila Power
            return isValidLibila(message);
        }else if(index == 5){ // Ascend Template
            return isValidAscendTemplate(message);
        }
        return true;
    }

    protected static boolean active = false;
    protected static long startTime = 0;
    protected static Creature performer = null;
    public static boolean isActive(){
        return active;
    }

    public static void setActive(long time, Creature performer){
        KeyEvent.active = true;
        KeyEvent.startTime = time;
        KeyEvent.performer = performer;
        resetResponses();
    }

    public static void handlePlayerMessage(Message message){
        if(performer == message.getSender()){
            long currentTime = System.currentTimeMillis() - startTime;
            //logger.info(String.format("Current timer: %s", currentTime));
            for(Response r : responses){
                //logger.info(String.format("Checking if index %s is valid at time %s (%s to %s)", r.index, currentTime, r.startTime, r.endTime));
                if(r.startTime < currentTime && r.endTime > currentTime){
                    String response = message.getMessage().substring(message.getSender().getName().length()+3).toLowerCase();
                    //logger.info(String.format("Response at index %s is valid (%s to %s)", r.index, r.startTime, r.endTime));
                    if (isValidResponse(r.index, response)) {
                        r.setResponse(response);
                    }
                }
            }
        }
    }

    public static void preInit(){
        resetResponses();
    }
}
