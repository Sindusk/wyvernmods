package mod.sin.wyvern.invasion;

import java.util.HashSet;
import java.util.Random;

import com.wurmonline.server.creatures.Creature;

public class InvasionEvent{
	protected Random rand = new Random();
	protected String villageName;
	protected float power;
	protected Creature invasionBoss;
	protected HashSet<Creature> minions = new HashSet<>();

	public InvasionEvent(int x, int y, String villageName, int bossId, int templateId1, int templateId2, float power) throws Exception{
		this.villageName = villageName;
		this.power = power;
		this.invasionBoss = Creature.doNew(bossId, x, y, rand.nextFloat()*360f, 0, "Necromancer", (rand.nextBoolean() ? (byte)0 : (byte)1));
		float halfPower = power / 2f;
		int minionCount = (int) ((halfPower+(rand.nextFloat()*halfPower))/5f);
		for(int i = 0; i < minionCount; i++){
			minions.add(Creature.doNew((rand.nextBoolean() ? templateId1 : templateId2), x, y, rand.nextFloat()*360f, 0, "Minion", (rand.nextBoolean() ? (byte)0 : (byte)1)));
		}
	}

	public String getPowerString(){
		if(power > 95){
			return "Legendary";
		}else if(power > 80){
			return "Powerful";
		}else if(power > 60){
			return "Strong";
		}else if(power > 40){
			return "Mediocre";
		}else if(power > 20){
			return "Weak";
		}else{
			return "Pathetic";
		}
	}
}
