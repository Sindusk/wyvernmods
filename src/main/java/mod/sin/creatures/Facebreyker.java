package mod.sin.creatures;

import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.Materials;
import com.wurmonline.shared.constants.CreatureTypes;
import org.gotti.wurmunlimited.modsupport.CreatureTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.creatures.ModCreature;

import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.skills.SkillList;

public class Facebreyker implements ModCreature, CreatureTypes {
	public static int templateId;
	
	@Override
	public CreatureTemplateBuilder createCreateTemplateBuilder() {
		int[] types = {
				CreatureTypes.C_TYPE_MOVE_LOCAL,
				CreatureTypes.C_TYPE_UNIQUE,
				CreatureTypes.C_TYPE_REGENERATING,
				CreatureTypes.C_TYPE_AGG_HUMAN,
				CreatureTypes.C_TYPE_MONSTER,
				CreatureTypes.C_TYPE_FENCEBREAKER,
				CreatureTypes.C_TYPE_CARNIVORE,
				CreatureTypes.C_TYPE_NON_NEWBIE,
				CreatureTypes.C_TYPE_NO_REBIRTH
		};
		
		//public CreatureTemplateBuilder(final String identifier, final String name, final String description,
		//       final String modelName, final int[] types, final byte bodyType, final short vision, final byte sex, final short centimetersHigh, final short centimetersLong, final short centimetersWide,
		//       final String deathSndMale, final String deathSndFemale, final String hitSndMale, final String hitSndFemale,
		//       final float naturalArmour, final float handDam, final float kickDam, final float biteDam, final float headDam, final float breathDam, final float speed, final int moveRate,
		//       final int[] itemsButchered, final int maxHuntDist, final int aggress) {
		CreatureTemplateBuilder builder = new CreatureTemplateBuilder("mod.creature.unique.facebreyker", "Facebreyker", "A bold warrior corrupted by darkness. You feel the presence of Libila.",
				"model.creature.humanoid.ogre.rift", types, (byte) 0, (short) 20, (byte) 0, (short) 350, (short) 100, (short) 60,
				"sound.death.troll", "sound.death.troll", "sound.combat.hit.troll", "sound.combat.hit.troll",
				0.05f, 50.0f, 50.0f, 0.0f, 0.0f, 0.0f, 1.6f, 1100,
				new int[]{ItemList.boneCollar}, 40, 100, Materials.MATERIAL_MEAT_HUMANOID);
		
		builder.skill(SkillList.BODY_STRENGTH, 90.0f);
		builder.skill(SkillList.BODY_STAMINA, 90.0f);
		builder.skill(SkillList.BODY_CONTROL, 90.0f);
		builder.skill(SkillList.MIND_LOGICAL, 30.0f);
		builder.skill(SkillList.MIND_SPEED, 30.0f);
		builder.skill(SkillList.SOUL_STRENGTH, 70.0f);
		builder.skill(SkillList.SOUL_DEPTH, 60.0f);
		builder.skill(SkillList.WEAPONLESS_FIGHTING, 80.0f);
		builder.skill(SkillList.GROUP_FIGHTING, 80.0f);
		builder.skill(SkillList.GROUP_CLUBS, 99.0f);
		builder.skill(SkillList.CLUB_HUGE, 99.0f);
		
		builder.boundsValues(-0.5f, -1.0f, 0.5f, 1.42f);
		builder.handDamString("slashe");
		builder.kickDamString("eviscerate");
		builder.maxAge(200);
		builder.armourType(10);
		builder.baseCombatRating(80.0f);
		builder.combatDamageType(Wound.TYPE_CRUSH);
		builder.maxGroupAttackSize(100);
		
		//builder.usesNewAttacks(true);
		// float baseDamage, float criticalChance, float baseSpeed, int attackReach, int weightGroup, byte damageType, boolean usesWeapon, int rounds, float waitUntilNextAttack
		//builder.addPrimaryAttack(new AttackAction("slashe", AttackIdentifier.STRIKE, new AttackValues(70f, 0.05f, 5f, 3, 1, Wound.TYPE_SLASH, false, 1, 1.0f)));
		//builder.addPrimaryAttack(new AttackAction("eviscerate", AttackIdentifier.STRIKE, new AttackValues(100f, 0.5f, 30f, 3, 1, Wound.TYPE_INFECTION, false, 4, 5.0f)));
		//builder.addSecondaryAttack(new AttackAction("annihilate", AttackIdentifier.KICK, new AttackValues(200f, 0.3f, 60f, 2, 1, Wound.TYPE_ACID, false, 7, 8.0f)));
		
		templateId = builder.getTemplateId();
		return builder;
	}
	
	@Override
	public void addEncounters() {
		return;
	}
}
