package mod.sin.creatures;

import org.gotti.wurmunlimited.modsupport.CreatureTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.creatures.ModCreature;

import com.wurmonline.server.bodys.BodyTemplate;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.combat.ArmourTypes;
import com.wurmonline.server.creatures.CreatureTypes;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.skills.SkillList;

public class SpectralDrake implements ModCreature {
	public static int templateId;
	@Override
	public CreatureTemplateBuilder createCreateTemplateBuilder() {
		int[] types = {
				CreatureTypes.C_TYPE_AGG_HUMAN,
				CreatureTypes.C_TYPE_MOVE_LOCAL,
				CreatureTypes.C_TYPE_SWIMMING,
				CreatureTypes.C_TYPE_HUNTING,
				CreatureTypes.C_TYPE_MONSTER,
				CreatureTypes.C_TYPE_HERBIVORE,
				CreatureTypes.C_TYPE_FENCEBREAKER,
				CreatureTypes.C_TYPE_NON_NEWBIE,
				CreatureTypes.C_TYPE_NO_REBIRTH
		};
		
		CreatureTemplateBuilder builder = new CreatureTemplateBuilder("mod.creature.spectraldrake", "Spectral drake", 
				"The spectral incarnation of a defeated dragon.", "model.creature.drake.spirit", types, BodyTemplate.TYPE_DRAGON, 
				(short) 20, (byte) 0, (short) 350, (short) 100, (short) 60, "sound.death.dragon", "sound.death.dragon", 
				"sound.combat.hit.dragon", "sound.combat.hit.dragon",
				0.06f, 45.0f, 50.0f, 22.0f, 45.0f, 0.0f, 2.0f, 500, 
				new int[]{ItemList.boneCollar}, 40, 100, Materials.MATERIAL_MEAT_DRAGON);
		
		builder.skill(SkillList.BODY_STRENGTH, 60.0f);
		builder.skill(SkillList.BODY_STAMINA, 70.0f);
		builder.skill(SkillList.BODY_CONTROL, 60.0f);
		builder.skill(SkillList.MIND_LOGICAL, 35.0f);
		builder.skill(SkillList.MIND_SPEED, 45.0f);
		builder.skill(SkillList.SOUL_STRENGTH, 70.0f);
		builder.skill(SkillList.SOUL_DEPTH, 70.0f);
		builder.skill(SkillList.WEAPONLESS_FIGHTING, 85.0f);
		builder.skill(SkillList.GROUP_FIGHTING, 75.0f);
		
		builder.boundsValues(-0.5f, -1.0f, 0.5f, 1.42f);
		builder.handDamString("claw");
		builder.kickDamString("wingbuff");
		builder.maxAge(200);
		builder.armourType(ArmourTypes.ARMOUR_LEATHER_DRAGON);
		builder.baseCombatRating(60.0f);
		builder.combatDamageType(Wound.TYPE_COLD);
		builder.maxGroupAttackSize(100);
		
		templateId = builder.getTemplateId();
		return builder;
	}
	
	@Override
	public void addEncounters() {
		return;
	}
}
