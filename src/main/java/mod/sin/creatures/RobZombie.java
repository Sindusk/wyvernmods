package mod.sin.creatures;

import org.gotti.wurmunlimited.modsupport.CreatureTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.creatures.ModCreature;

import com.wurmonline.server.creatures.CreatureTypes;
import com.wurmonline.server.skills.SkillList;

public class RobZombie implements ModCreature, CreatureTypes {
	@Override
	public CreatureTemplateBuilder createCreateTemplateBuilder() {
		int[] types = {
				CreatureTypes.C_TYPE_MOVE_LOCAL,
				CreatureTypes.C_TYPE_HERBIVORE,
				CreatureTypes.C_TYPE_UNDEAD,
				CreatureTypes.C_TYPE_TRADER,
				CreatureTypes.C_TYPE_NPC_TRADER};
		
		CreatureTemplateBuilder builder = new CreatureTemplateBuilder("mod.creature.zombie.rob", "Rob Zombie",
				"This is Rob Zombie. He is known to eat brains when you do not buy things from him.", "model.creature.humanoid.human.player.zombie",
				types, (byte) 0, (short) 20, (byte) 0, (short) 350, (short) 100, (short) 60,
				"sound.death.zombie", "sound.death.zombie", "sound.combat.hit.zombie", "sound.combat.hit.zombie",
				0.005f, 150.0f, 150.0f, 150.0f, 0.0f, 0.0f, 0.5f, 500, new int[]{868, 867}, 40, 100, (byte) 0);
		
		builder.skill(SkillList.BODY_STRENGTH, 99.0f);
		builder.skill(SkillList.BODY_STAMINA, 99.0f);
		builder.skill(SkillList.BODY_CONTROL, 99.0f);
		builder.skill(SkillList.MIND_LOGICAL, 99.0f);
		builder.skill(SkillList.MIND_SPEED, 99.0f);
		builder.skill(SkillList.SOUL_STRENGTH, 99.0f);
		builder.skill(SkillList.SOUL_DEPTH, 99.0f);
		builder.skill(SkillList.WEAPONLESS_FIGHTING, 99.0f);
		builder.skill(SkillList.GROUP_FIGHTING, 99.0f);
		
		builder.boundsValues(-0.5f, -1.0f, 0.5f, 1.42f);
		builder.handDamString("decimate");
		builder.kickDamString("decimate");
		builder.maxAge(200);
		builder.armourType(10);
		builder.baseCombatRating(99.0f);
		builder.combatDamageType((byte)2);
		builder.maxGroupAttackSize(100);
		
		return builder;
	}
	
	@Override
	public void addEncounters() {
		return;
	}
}
