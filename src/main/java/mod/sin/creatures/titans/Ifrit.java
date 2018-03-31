package mod.sin.creatures.titans;

import org.gotti.wurmunlimited.modsupport.CreatureTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.creatures.ModCreature;

import com.wurmonline.server.bodys.BodyTemplate;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.combat.ArmourTypes;
import com.wurmonline.server.creatures.CreatureTypes;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.skills.SkillList;

public class Ifrit implements ModCreature, CreatureTypes {
	public static int templateId;
	@Override
	public CreatureTemplateBuilder createCreateTemplateBuilder() {
		int[] types = {
			CreatureTypes.C_TYPE_MOVE_LOCAL,
			CreatureTypes.C_TYPE_AGG_HUMAN,
			CreatureTypes.C_TYPE_CARNIVORE,
			CreatureTypes.C_TYPE_HUNTING,
			CreatureTypes.C_TYPE_NON_NEWBIE
		};
		
		//public CreatureTemplateBuilder(final String identifier, final String name, final String description,
		//       final String modelName, final int[] types, final byte bodyType, final short vision, final byte sex, final short centimetersHigh, final short centimetersLong, final short centimetersWide,
		//       final String deathSndMale, final String deathSndFemale, final String hitSndMale, final String hitSndFemale,
		//       final float naturalArmour, final float handDam, final float kickDam, final float biteDam, final float headDam, final float breathDam, final float speed, final int moveRate,
		//       final int[] itemsButchered, final int maxHuntDist, final int aggress) {
		CreatureTemplateBuilder builder = new CreatureTemplateBuilder("mod.creature.raid.ifrit", "Ifrit", "A valiant warrior of the flame. You feel the presence of Magranon.",
				"model.creature.humanoid.giant.juggernaut", types, BodyTemplate.TYPE_HUMAN, (short) 5, (byte) 0, (short) 350, (short) 100, (short) 60,
				"sound.death.magranon.juggernaut", "sound.death.magranon.juggernaut", "sound.combat.hit.magranon.juggernaut", "sound.combat.hit.magranon.juggernaut",
				0.014f, 10.0f, 13.0f, 0.0f, 0.0f, 0.0f, 0.5f, 400,
				new int[]{}, 40, 100, Materials.MATERIAL_MEAT_HUMANOID);
		
		builder.skill(SkillList.BODY_STRENGTH, 99.0f);
		builder.skill(SkillList.BODY_STAMINA, 99.0f);
		builder.skill(SkillList.BODY_CONTROL, 99.0f);
		builder.skill(SkillList.MIND_LOGICAL, 99.0f);
		builder.skill(SkillList.MIND_SPEED, 99.0f);
		builder.skill(SkillList.SOUL_STRENGTH, 99.0f);
		builder.skill(SkillList.SOUL_DEPTH, 99.0f);
		builder.skill(SkillList.WEAPONLESS_FIGHTING, 99.0f);
		builder.skill(SkillList.GROUP_FIGHTING, 99.0f);
		builder.skill(SkillList.SCYTHE, 99.0f);
		
		builder.boundsValues(-0.5f, -1.0f, 0.5f, 1.42f);
		builder.handDamString("burn");
		builder.kickDamString("ignite");
		builder.maxAge(200);
		builder.armourType(ArmourTypes.ARMOUR_SCALE_DRAGON);
		builder.baseCombatRating(99.0f);
		builder.combatDamageType(Wound.TYPE_BURN);
		builder.maxGroupAttackSize(150);
		
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
