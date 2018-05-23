package mod.sin.creatures;

import com.wurmonline.server.bodys.BodyTemplate;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.combat.ArmourTypes;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.shared.constants.CreatureTypes;
import org.gotti.wurmunlimited.modsupport.CreatureTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.creatures.ModCreature;

public class Reaper implements ModCreature, CreatureTypes {
	public static int templateId;
	@Override
	public CreatureTemplateBuilder createCreateTemplateBuilder() {
		int[] types = {
				CreatureTypes.C_TYPE_AGG_HUMAN,
				CreatureTypes.C_TYPE_MOVE_LOCAL,
				CreatureTypes.C_TYPE_SWIMMING,
				CreatureTypes.C_TYPE_HUNTING,
				CreatureTypes.C_TYPE_MONSTER,
				CreatureTypes.C_TYPE_CARNIVORE,
				//CreatureTypes.C_TYPE_FENCEBREAKER,
				CreatureTypes.C_TYPE_NON_NEWBIE,
				CreatureTypes.C_TYPE_NO_REBIRTH,
				CreatureTypes.C_TYPE_REGENERATING
		};
		
		CreatureTemplateBuilder builder = new CreatureTemplateBuilder("mod.creature.reaper", "Reaper", 
				"The reaper, here to claim the souls of the living.", "model.creature.gmdark",
				types, BodyTemplate.TYPE_HUMAN, (short) 20, (byte) 0, (short) 350, (short) 100, (short) 60, "sound.death.dragon", 
				"sound.death.dragon", "sound.combat.hit.dragon", "sound.combat.hit.dragon",
				0.10f, 35.0f, 45.0f, 22.0f, 40.0f, 0.0f, 1.0f, 500,
				new int[]{}, 20, 70, Materials.MATERIAL_MEAT_HUMANOID);

		builder.skill(SkillList.BODY_STRENGTH, 60.0f);
		builder.skill(SkillList.BODY_STAMINA, 70.0f);
		builder.skill(SkillList.BODY_CONTROL, 60.0f);
		builder.skill(SkillList.MIND_LOGICAL, 35.0f);
		builder.skill(SkillList.MIND_SPEED, 45.0f);
		builder.skill(SkillList.SOUL_STRENGTH, 80.0f);
		builder.skill(SkillList.SOUL_DEPTH, 80.0f);
		builder.skill(SkillList.WEAPONLESS_FIGHTING, 80.0f);
		builder.skill(SkillList.GROUP_FIGHTING, 80.0f);
		
		builder.boundsValues(-0.5f, -1.0f, 0.5f, 1.42f);
		builder.handDamString("slice");
		builder.kickDamString("reap");
		builder.maxAge(200);
		builder.armourType(ArmourTypes.ARMOUR_SCALE_DRAGON);
		builder.baseCombatRating(55.0f);
		builder.combatDamageType(Wound.TYPE_PIERCE);
		builder.maxGroupAttackSize(100);
		
		templateId = builder.getTemplateId();
		return builder;
	}
	
	@Override
	public void addEncounters() {
		return;
	}
}
