package mod.sin.wyvern;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.SpellEffectsEnum;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.Cults;
import com.wurmonline.server.players.Player;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.logging.Logger;

public class MeditationPerks {
    public static Logger logger = Logger.getLogger(MeditationPerks.class.getName());

    public static void sendPassiveBuffs(Cultist cultist){
        byte path = cultist.getPath();
        byte level = cultist.getLevel();
        try {
            Creature cret = Server.getInstance().getCreature(cultist.getWurmId());
            if(path == Cults.PATH_HATE && level >= 4){
                float levelDiff = level-3f;
                cret.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.DEITY_MOVEBONUS, -1, levelDiff);
            }
            if(path == Cults.PATH_INSANITY && level >= 7){
                float levelDiff = level-6f;
                cret.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.INSANITY_SHIELD_GONE, -1, levelDiff*2f);
            }
            if(path == Cults.PATH_KNOWLEDGE && level >= 7){
                float levelDiff = level-6f;
                cret.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.KNOWLEDGE_INCREASED_SKILL_GAIN, -1, levelDiff*5f);
            }
            if(path == Cults.PATH_POWER && level >= 7){
                float levelDiff = level-6f;
                cret.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.POWER_USES_LESS_STAMINA, -1, levelDiff*5f);
            }
        } catch (NoSuchPlayerException | NoSuchCreatureException e) {
            e.printStackTrace();
        }
    }

    public static float getCultistSpeedMultiplier(MovementScheme scheme){
        try {
            Creature cret = ReflectionUtil.getPrivateField(scheme, ReflectionUtil.getField(scheme.getClass(), "creature"));
            if(cret != null){
                if(cret.getCultist() != null){
                    Cultist path = cret.getCultist();
                    if(path.getPath() == Cults.PATH_HATE){
                        byte level = path.getLevel();
                        if(level >= 3){
                            float levelDiff = level-3f;
                            return 1.0f+(levelDiff*0.01f);
                        }
                    }
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return 1.0f;
    }

    public static float newStaminaModifierFor(Creature performer, int staminaNeeded){
        int currstam = performer.getStatus().getStamina();
        float staminaMod = 1.0f;
        if (currstam > 60000) {
            staminaMod = 0.8f;
        }
        if(performer.getCultist() != null){
            Cultist path = performer.getCultist();
            if(path.getPath() == Cults.PATH_INSANITY){
                byte level = path.getLevel();
                if(level >= 7){
                    float levelDiff = level-6f;
                    float insanityMod = 1.0f-(levelDiff*0.02f);
                    staminaMod *= insanityMod;
                }
            }
        }
        staminaMod += 1.0f - (float)currstam / 65535.0f;
        if (currstam < staminaNeeded) {
            float diff = staminaNeeded - currstam;
            staminaMod += diff / (float)staminaNeeded * performer.getStaminaMod();
        }
        return staminaMod;
    }

    public static float getPowerStaminaBonus(Creature creature){
        if(creature instanceof Player){
            Player player = (Player) creature;
            if(player.getCultist() != null){
                Cultist path = player.getCultist();
                if(path.getPath() == Cults.PATH_POWER){
                    byte level = path.getLevel();
                    if(level >= 7){
                        float levelDiff = level-6f;
                        return (levelDiff*0.05f);
                    }
                }
            }
        }
        return 0f;
    }

    public static float getKnowledgeSkillGain(Player player){
        if(player.getCultist() != null){
            Cultist path = player.getCultist();
            if(path.getPath() == Cults.PATH_KNOWLEDGE){
                byte level = path.getLevel();
                if(level >= 7){
                    float levelDiff = level-6f;
                    return 1.0f+(levelDiff*0.05f);
                }
            }
        }
        return 1.0f;
    }

    public static byte getNewPathFor(int tilex, int tiley, int layer){
        if (layer < 0) {
            return Cults.PATH_INSANITY;
        }
        int tile = Server.surfaceMesh.getTile(tilex, tiley);
        byte type = Tiles.decodeType(tile);
        Tiles.Tile theTile = Tiles.getTile(type);
        if (theTile.isGrass() || theTile.isBush() || theTile.isTree()){
            return Cults.PATH_LOVE;
        }
        if (theTile.isMycelium() || theTile.isMyceliumBush() || theTile.isMyceliumTree()){
            return Cults.PATH_HATE;
        }
        if (type == Tiles.TILE_TYPE_SAND){
            return Cults.PATH_KNOWLEDGE;
        }
        if (type == Tiles.TILE_TYPE_ROCK || type == Tiles.TILE_TYPE_CLIFF){
            return Cults.PATH_POWER;
        }
        return Cults.PATH_NONE;
    }

    public static void preInit(){
        try{
            ClassPool classPool = HookManager.getInstance().getClassPool();
            final Class<MeditationPerks> thisClass = MeditationPerks.class;
            String replace;

            Util.setReason("Enable buff icons for meditation.");
            CtClass ctCultist = classPool.get("com.wurmonline.server.players.Cultist");
            replace = MeditationPerks.class.getName()+".sendPassiveBuffs($0);";
            Util.insertBeforeDeclared(thisClass, ctCultist, "sendPassiveBuffs", replace);

            Util.setReason("Make meditating paths more straightforward.");
            CtClass ctCults = classPool.get("com.wurmonline.server.players.Cults");
            replace = "{ return "+MeditationPerks.class.getName()+".getNewPathFor($1, $2, $3); }";
            Util.setBodyDeclared(thisClass, ctCults, "getPathFor", replace);

            Util.setReason("Replace stamina modifier for new Insanity ability.");
            CtClass ctActions = classPool.get("com.wurmonline.server.behaviours.Actions");
            replace = "{ return "+MeditationPerks.class.getName()+".newStaminaModifierFor($1, $2); }";
            Util.setBodyDeclared(thisClass, ctActions, "getStaminaModiferFor", replace);

            Util.setReason("Increase movement speed for Path of Hate users.");
            CtClass ctMovementScheme = classPool.get("com.wurmonline.server.creatures.MovementScheme");
            replace = "if($_ > 0){" +
                    "  $_ = $_ * "+MeditationPerks.class.getName()+".getCultistSpeedMultiplier(this);" +
                    "}";
            Util.insertAfterDeclared(thisClass, ctMovementScheme, "getSpeedModifier", replace);

            Util.setReason("Scale path of power stamina bonus from level 7 onwards.");
            CtClass ctCreatureStatus = classPool.get("com.wurmonline.server.creatures.CreatureStatus");
            replace = "staminaMod += "+MeditationPerks.class.getName()+".getPowerStaminaBonus(this.statusHolder);" +
                    "$_ = false;";
            Util.instrumentDeclared(thisClass, ctCreatureStatus, "modifyStamina", "usesNoStamina", replace);

            Util.setReason("Scale path of knowledge skill gain from level 7 onwards.");
            CtClass ctSkill = classPool.get("com.wurmonline.server.skills.Skill");
            CtClass[] params1 = {
                    CtClass.doubleType,
                    CtClass.booleanType,
                    CtClass.floatType,
                    CtClass.booleanType,
                    CtClass.doubleType
            };
            String desc1 = Descriptor.ofMethod(CtClass.voidType, params1);
            replace = "staminaMod *= "+MeditationPerks.class.getName()+".getKnowledgeSkillGain(player);" +
                    "$_ = false;";
            Util.instrumentDescribed(thisClass, ctSkill, "alterSkill", desc1, "levelElevenSkillgain", replace);

            Util.setReason("Remove shield of the gone effect.");
            replace = "{ return 0.0f; }";
            Util.setBodyDeclared(thisClass, ctCultist, "getHalfDamagePercentage", replace);
            Util.setReason("Remove hate war damage effect.");
            replace = "{ return false; }";
            Util.setBodyDeclared(thisClass, ctCultist, "mayStartDoubleWarDamage", replace);
            Util.setBodyDeclared(thisClass, ctCultist, "doubleWarDamage", replace);

            Util.setReason("Remove artifical tick timer for meditation.");
            replace = "$_ = 0;";
            Util.instrumentDeclared(thisClass, ctCults, "meditate", "getLastMeditated", replace);

            // - Reduce meditation cooldowns - //
            replace = "return this.path == 1 && this.level > 3 && System.currentTimeMillis() - this.cooldown1 > "+(TimeConstants.HOUR_MILLIS*8)+";";
            Util.setBodyDeclared(thisClass, ctCultist, "mayRefresh", replace);
            //ctCultist.getDeclaredMethod("mayRefresh").setBody("return this.path == 1 && this.level > 3 && System.currentTimeMillis() - this.cooldown1 > 28800000;");
            replace = "return this.path == 1 && this.level > 6 && System.currentTimeMillis() - this.cooldown2 > "+(TimeConstants.HOUR_MILLIS*8)+";";
            Util.setBodyDeclared(thisClass, ctCultist, "mayEnchantNature", replace);
            //ctCultist.getDeclaredMethod("mayEnchantNature").setBody("return this.path == 1 && this.level > 6 && System.currentTimeMillis() - this.cooldown2 > 28800000;");
            replace = "return this.path == 1 && this.level > 8 && System.currentTimeMillis() - this.cooldown3 > "+(TimeConstants.HOUR_MILLIS*4)+";";
            Util.setBodyDeclared(thisClass, ctCultist, "mayStartLoveEffect", replace);
            //ctCultist.getDeclaredMethod("mayStartLoveEffect").setBody("return this.path == 1 && this.level > 8 && System.currentTimeMillis() - this.cooldown3 > 14400000;");
            replace = "return this.path == 2 && this.level > 6 && System.currentTimeMillis() - this.cooldown1 > "+(TimeConstants.HOUR_MILLIS*6)+";";
            Util.setBodyDeclared(thisClass, ctCultist, "mayStartDoubleWarDamage", replace);
            //ctCultist.getDeclaredMethod("mayStartDoubleWarDamage").setBody("return this.path == 2 && this.level > 6 && System.currentTimeMillis() - this.cooldown1 > 21600000;");
            replace = "return this.path == 2 && this.level > 3 && System.currentTimeMillis() - this.cooldown2 > "+(TimeConstants.HOUR_MILLIS*4)+";";
            Util.setBodyDeclared(thisClass, ctCultist, "mayStartDoubleStructDamage", replace);
            //ctCultist.getDeclaredMethod("mayStartDoubleStructDamage").setBody("return this.path == 2 && this.level > 3 && System.currentTimeMillis() - this.cooldown2 > 14400000;");
            replace = "return this.path == 2 && this.level > 8 && System.currentTimeMillis() - this.cooldown3 > "+(TimeConstants.HOUR_MILLIS*6)+";";
            Util.setBodyDeclared(thisClass, ctCultist, "mayStartFearEffect", replace);
            //ctCultist.getDeclaredMethod("mayStartFearEffect").setBody("return this.path == 2 && this.level > 8 && System.currentTimeMillis() - this.cooldown3 > 21600000;");
            replace = "return this.path == 5 && this.level > 8 && System.currentTimeMillis() - this.cooldown1 > "+(TimeConstants.HOUR_MILLIS*6)+";";
            Util.setBodyDeclared(thisClass, ctCultist, "mayStartNoElementalDamage", replace);
            //ctCultist.getDeclaredMethod("mayStartNoElementalDamage").setBody("return this.path == 5 && this.level > 8 && System.currentTimeMillis() - this.cooldown1 > 21600000;");
            replace = "return this.path == 5 && this.level > 6 && System.currentTimeMillis() - this.cooldown2 > "+(TimeConstants.HOUR_MILLIS*8)+";";
            Util.setBodyDeclared(thisClass, ctCultist, "maySpawnVolcano", replace);
            //ctCultist.getDeclaredMethod("maySpawnVolcano").setBody("return this.path == 5 && this.level > 6 && System.currentTimeMillis() - this.cooldown2 > 28800000;");
            replace = "return this.path == 5 && this.level > 3 && System.currentTimeMillis() - this.cooldown3 > "+(TimeConstants.HOUR_MILLIS*4)+";";
            Util.setBodyDeclared(thisClass, ctCultist, "mayStartIgnoreTraps", replace);
            //ctCultist.getDeclaredMethod("mayStartIgnoreTraps").setBody("return this.path == 5 && this.level > 3 && System.currentTimeMillis() - this.cooldown3 > 14400000;");
            replace = "return this.path == 3 && this.level > 3 && System.currentTimeMillis() - this.cooldown1 > "+(TimeConstants.HOUR_MILLIS*4)+";";
            Util.setBodyDeclared(thisClass, ctCultist, "mayCreatureInfo", replace);
            //ctCultist.getDeclaredMethod("mayCreatureInfo").setBody("return this.path == 3 && this.level > 3 && System.currentTimeMillis() - this.cooldown1 > 14400000;");
            replace = "return this.path == 3 && this.level > 6 && System.currentTimeMillis() - this.cooldown2 > "+(TimeConstants.HOUR_MILLIS*4)+";";
            Util.setBodyDeclared(thisClass, ctCultist, "mayInfoLocal", replace);
            //ctCultist.getDeclaredMethod("mayInfoLocal").setBody("return this.path == 3 && this.level > 6 && System.currentTimeMillis() - this.cooldown2 > 14400000;");

        } catch ( NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        }
    }
}
