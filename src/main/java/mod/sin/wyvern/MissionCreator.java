package mod.sin.wyvern;

import com.wurmonline.server.Server;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.epic.EpicMission;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.players.PlayerInfo;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

public class MissionCreator {
    private static Logger logger = Logger.getLogger(MissionCreator.class.getName());
    public static void pollMissions(){
        int[] deityNums = {
                1, 2, 3, 4, // Original Gods
                6, 7, 8, 9, 10, 11, 12 // Valrei Entities
        };
        EpicServerStatus es = new EpicServerStatus();
        EpicMission[] missions = EpicServerStatus.getCurrentEpicMissions();
        int i = 0;
        while(i < missions.length){
            if(missions[i].isCurrent()){
                if(missions[i].isCompleted() || missions[i].getEndTime() >= System.currentTimeMillis()){
                    try {
                        int entityId = missions[i].getEpicEntityId();
                        logger.info("Detected an existing current mission for "+Deities.getDeityName(entityId)+" that was completed or expired. Removing now.");
                        ReflectionUtil.callPrivateMethod(EpicServerStatus.class, ReflectionUtil.getMethod(EpicServerStatus.class, "destroyLastMissionForEntity"), entityId);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
            }
            i++;
        }
        if(EpicServerStatus.getCurrentEpicMissions().length >= deityNums.length){
            logger.info("All entities already have a mission. Aborting.");
            return;
        }
        i = 10;
        int number = 1;
        while(i > 0) {
            number = deityNums[Server.rand.nextInt(deityNums.length)];
            logger.info("Testing number "+number);
            if(EpicServerStatus.getEpicMissionForEntity(number) == null){
                logger.info("Has no mission, breaking loop.");
                break;
            }else{
                logger.info("Has mission, finding new number.");
            }
            i++;
            if(i == 0){
                logger.info("Ran through 10 possible entities and could not find empty mission. Cancelling.");
                return;
            }
        }
        logger.info("Entity number = "+number);
        String entityName = Deities.getDeityName(number);
        logger.info("Entity name = "+entityName);
        int time = 604800;
        logger.info("Current epic missions: "+EpicServerStatus.getCurrentEpicMissions().length);
        if (EpicServerStatus.getCurrentScenario() != null) {
            es.generateNewMissionForEpicEntity(number, entityName, -1, time, EpicServerStatus.getCurrentScenario().getScenarioName(), EpicServerStatus.getCurrentScenario().getScenarioNumber(), EpicServerStatus.getCurrentScenario().getScenarioQuest(), true);
        }
    }

    public static void awardMissionBonus(PlayerInfo info){
        try {
            info.setMoney(info.money + 2000 + Server.rand.nextInt(2000));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void preInit(){
        try{
            ClassPool classPool = HookManager.getInstance().getClassPool();
            final Class<MissionCreator> thisClass = MissionCreator.class;
            String replace;

            CtClass ctTriggerEffect = classPool.get("com.wurmonline.server.tutorial.TriggerEffect");
            Util.setReason("Give players currency for completing a mission.");
            replace = "$_ = $proceed($$);" +
                    MissionCreator.class.getName()+".awardMissionBonus($0);";
            Util.instrumentDeclared(thisClass, ctTriggerEffect, "effect", "addToSleep", replace);

        } catch ( NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        }
    }
}
