package mod.sin.creatures;

import org.gotti.wurmunlimited.modsupport.CreatureTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.creatures.ModCreature;

import com.wurmonline.server.bodys.BodyTemplate;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.combat.ArmourTypes;
import com.wurmonline.server.creatures.CreatureTypes;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.skills.SkillList;

public class MacroSlayer implements ModCreature, CreatureTypes {
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
		
		CreatureTemplateBuilder builder = new CreatureTemplateBuilder("mod.creature.macro.slayer", "Blobber the Macro Slayer", 
				"The incarnation of the Macro King. Here to claim the souls of those who challenge his rightful rule.", "model.blob", 
				types, BodyTemplate.TYPE_DRAGON, (short) 5, (byte) 0, (short) 350, (short) 100, (short) 60,
				"sound.death.dragon", "sound.death.dragon", "sound.combat.hit.dragon", "sound.combat.hit.dragon",
				0.001f, 10.0f, 10.0f, 10.0f, 10.0f, 10.0f, 0.5f, 500,
				new int[]{}, 40, 100, Materials.MATERIAL_MEAT_HUMANOID);
		
		builder.skill(SkillList.BODY_STRENGTH, 90.0f);
		builder.skill(SkillList.BODY_STAMINA, 70.0f);
		builder.skill(SkillList.BODY_CONTROL, 60.0f);
		builder.skill(SkillList.MIND_LOGICAL, 35.0f);
		builder.skill(SkillList.MIND_SPEED, 45.0f);
		builder.skill(SkillList.SOUL_STRENGTH, 80.0f);
		builder.skill(SkillList.SOUL_DEPTH, 80.0f);
		builder.skill(SkillList.WEAPONLESS_FIGHTING, 80.0f);
		builder.skill(SkillList.GROUP_FIGHTING, 80.0f);
		
		builder.boundsValues(-0.5f, -1.0f, 0.5f, 1.42f);
		builder.handDamString("macro");
		builder.kickDamString("macro");
		builder.maxAge(200);
		builder.armourType(ArmourTypes.ARMOUR_SCALE_DRAGON);
		builder.baseCombatRating(99.0f);
		builder.combatDamageType(Wound.TYPE_WATER);
		builder.maxGroupAttackSize(100);
		
		templateId = builder.getTemplateId();
		return builder;
	}
	
	@Override
	public void addEncounters() {
		return;
	}
}
