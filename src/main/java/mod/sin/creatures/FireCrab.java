package mod.sin.creatures;

import com.wurmonline.server.bodys.BodyTemplate;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.combat.ArmourTypes;
import com.wurmonline.server.creatures.CreatureTypes;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.skills.SkillList;
import org.gotti.wurmunlimited.modsupport.CreatureTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.creatures.ModCreature;

public class FireCrab implements ModCreature, CreatureTypes {
	public static int templateId;
	
	@Override
	public CreatureTemplateBuilder createCreateTemplateBuilder() {
		// {C_TYPE_MOVE_LOCAL, C_TYPE_VEHICLE, C_TYPE_ANIMAL, C_TYPE_LEADABLE, C_TYPE_GRAZER, C_TYPE_OMNIVORE, C_TYPE_DOMINATABLE, C_TYPE_AGG_HUMAN, C_TYPE_NON_NEWBIE, C_TYPE_BURNING}; - Hell Horse
		// int[] types = new int[]{7, 6, 13, 3, 29, 39, 60, 61}; - Spider
		int[] types = {
				CreatureTypes.C_TYPE_MOVE_LOCAL,
				CreatureTypes.C_TYPE_AGG_HUMAN,
				CreatureTypes.C_TYPE_HUNTING,
				CreatureTypes.C_TYPE_ANIMAL,
				CreatureTypes.C_TYPE_CARNIVORE,
				CreatureTypes.C_TYPE_NON_NEWBIE
		};
		
		//public CreatureTemplateBuilder(final String identifier, final String name, final String description,
		//       final String modelName, final int[] types, final byte bodyType, final short vision, final byte sex, final short centimetersHigh, final short centimetersLong, final short centimetersWide,
		//       final String deathSndMale, final String deathSndFemale, final String hitSndMale, final String hitSndFemale,
		//       final float naturalArmour, final float handDam, final float kickDam, final float biteDam, final float headDam, final float breathDam, final float speed, final int moveRate,
		//       final int[] itemsButchered, final int maxHuntDist, final int aggress) {
		CreatureTemplateBuilder builder = new CreatureTemplateBuilder("mod.creature.fire.crab", "Fire crab", "A crab lit on fire. I wonder how...",
				"model.creature.quadraped.crab", types, BodyTemplate.TYPE_DOG, (short) 5, (byte) 0, (short) 85, (short) 50, (short) 85,
				"sound.combat.crab.death", "sound.combat.crab.death", "sound.combat.crab.attack", "sound.combat.crab.attack2",
				0.6f, 9f, 0f, 12.0f, 0.0f, 0.0f, 1.0f, 500,
				new int[]{}, 10, 74, Materials.MATERIAL_MEAT_SEAFOOD);
		
		builder.skill(SkillList.BODY_STRENGTH, 20.0f);
		builder.skill(SkillList.BODY_STAMINA, 30.0f);
		builder.skill(SkillList.BODY_CONTROL, 35.0f);
		builder.skill(SkillList.MIND_LOGICAL, 30.0f);
		builder.skill(SkillList.MIND_SPEED, 30.0f);
		builder.skill(SkillList.SOUL_STRENGTH, 30.0f);
		builder.skill(SkillList.SOUL_DEPTH, 30.0f);
		builder.skill(SkillList.WEAPONLESS_FIGHTING, 35.0f);
		builder.skill(SkillList.GROUP_FIGHTING, 25.0f);
		
		builder.boundsValues(-0.5f, -1.0f, 0.5f, 1.42f);
		builder.handDamString("claw");
		builder.maxAge(100);
		builder.armourType(ArmourTypes.ARMOUR_CLOTH);
		builder.baseCombatRating(7.0f);
		builder.combatDamageType(Wound.TYPE_BURN);
		builder.maxGroupAttackSize(100);
		
		templateId = builder.getTemplateId();
		return builder;
	}
	
	@Override
	public void addEncounters() {
		if (templateId == 0)
			return;
	}
}
