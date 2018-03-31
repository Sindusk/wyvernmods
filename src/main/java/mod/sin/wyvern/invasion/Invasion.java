package mod.sin.wyvern.invasion;

import java.util.HashSet;
import java.util.Random;

import com.wurmonline.server.Server;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;

public class Invasion {
	public static boolean active = false;
	public static long lastInvasionPoll = 0;
	public static HashSet<InvasionEvent> invasion = new HashSet<>();
	public static void pollInvasions(){
		long now = System.currentTimeMillis();
		if(now - lastInvasionPoll > 360000){ // 1 hour
			Random rand = new Random();
			Village v = null;
			int startx = 0;
			int starty = 0;
			while(v == null){
				startx = rand.nextInt(4000);
				starty = rand.nextInt(4000);
				v = Villages.getVillage(startx, starty, true);
				if(v != null){ continue; }
	            for (int x = -50; x < 50; x += 5) {
	                for (int y = -50; y < 50 && (v = Villages.getVillage(startx + x, starty + y, true)) == null; y += 5) {
	                }
	            }
	        }
			try {
				int minion1Id = 555;
				int minion2Id = 666;
				int bossId = 777;
				String villageName = v.getName();
				InvasionEvent event;
				event = new InvasionEvent(startx, starty, villageName, bossId, minion1Id, minion2Id, rand.nextFloat()*100f);
				invasion.add(event);
				Server.getInstance().broadCastNormal("Whispers of a "+event.getPowerString()+" Necromancer circulate the area around "+villageName+"...");
				//HistoryManager.addHistory("A "+event.getPowerString()+" Necromancer", "invades the area surrounding "+villageName+"!");
				active = true;
				lastInvasionPoll = now;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
