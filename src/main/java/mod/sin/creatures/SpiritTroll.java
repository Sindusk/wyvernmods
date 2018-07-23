package mod.sin.creatures;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Servers;
import com.wurmonline.server.bodys.BodyTemplate;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.combat.ArmourTypes;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.shared.constants.CreatureTypes;
import org.gotti.wurmunlimited.modsupport.CreatureTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.creatures.EncounterBuilder;
import org.gotti.wurmunlimited.modsupport.creatures.ModCreature;

public class SpiritTroll implements ModCreature, CreatureTypes {
	public static int templateId;
	
	@Override
	public CreatureTemplateBuilder createCreateTemplateBuilder() {
		// {C_TYPE_MOVE_LOCAL, C_TYPE_VEHICLE, C_TYPE_ANIMAL, C_TYPE_LEADABLE, C_TYPE_GRAZER, C_TYPE_OMNIVORE, C_TYPE_DOMINATABLE, C_TYPE_AGG_HUMAN, C_TYPE_NON_NEWBIE, C_TYPE_BURNING}; - Hell Horse
		// new int[]{7, 6, 40, 13, 16, 18, 29, 30, 32, 36, 39, 45, 60, 61}; - Troll
		int[] types = {
				CreatureTypes.C_TYPE_MOVE_LOCAL,
				CreatureTypes.C_TYPE_AGG_HUMAN,
				CreatureTypes.C_TYPE_FENCEBREAKER,
				CreatureTypes.C_TYPE_HUNTING,
				CreatureTypes.C_TYPE_MONSTER,
				CreatureTypes.C_TYPE_REGENERATING,
				CreatureTypes.C_TYPE_CARNIVORE,
				CreatureTypes.C_TYPE_CLIMBER,
				CreatureTypes.C_TYPE_UNDEAD,
				CreatureTypes.C_TYPE_DETECTINVIS,
				CreatureTypes.C_TYPE_NON_NEWBIE,
				CreatureTypes.C_TYPE_MISSION_OK,
				CreatureTypes.C_TYPE_MISSION_TRAITOR_OK
		};
		int[] pvpTypes = {
				CreatureTypes.C_TYPE_MOVE_LOCAL,
				CreatureTypes.C_TYPE_AGG_HUMAN,
				CreatureTypes.C_TYPE_FENCEBREAKER,
				CreatureTypes.C_TYPE_HUNTING,
				CreatureTypes.C_TYPE_MONSTER,
				CreatureTypes.C_TYPE_REGENERATING,
				CreatureTypes.C_TYPE_CARNIVORE,
				CreatureTypes.C_TYPE_CLIMBER,
				CreatureTypes.C_TYPE_UNDEAD,
				CreatureTypes.C_TYPE_DETECTINVIS,
				CreatureTypes.C_TYPE_NON_NEWBIE,
				CreatureTypes.C_TYPE_NO_REBIRTH,
				CreatureTypes.C_TYPE_MISSION_OK,
				CreatureTypes.C_TYPE_MISSION_TRAITOR_OK
		};

		//public CreatureTemplateBuilder(final String identifier, final String name, final String description,
		//       final String modelName, final int[] types, final byte bodyType, final short vision, final byte sex, final short centimetersHigh, final short centimetersLong, final short centimetersWide,
		//       final String deathSndMale, final String deathSndFemale, final String hitSndMale, final String hitSndFemale,
		//       final float naturalArmour, final float handDam, final float kickDam, final float biteDam, final float headDam, final float breathDam, final float speed, final int moveRate,
		//       final int[] itemsButchered, final int maxHuntDist, final int aggress) {
		CreatureTemplateBuilder builder = new CreatureTemplateBuilder("mod.creature.spirit.troll", "Spirit troll", "A spirit troll.",
				"model.creature.humanoid.troll.king", Servers.localServer.PVPSERVER ? pvpTypes : types, BodyTemplate.TYPE_HUMAN, (short) 5, (byte) 0, (short) 85, (short) 50, (short) 85,
				"sound.death.troll", "sound.death.troll", "sound.combat.hit.troll", "sound.combat.hit.troll", 
				0.15f, 15f, 17f, 19.0f, 0.0f, 0.0f, 1.2f, 500,
				new int[]{}, 10, 74, Materials.MATERIAL_MEAT_HUMANOID);
		
		builder.skill(SkillList.BODY_STRENGTH, 45.0f);
		builder.skill(SkillList.BODY_STAMINA, 40.0f);
		builder.skill(SkillList.BODY_CONTROL, 45.0f);
		builder.skill(SkillList.MIND_LOGICAL, 30.0f);
		builder.skill(SkillList.MIND_SPEED, 30.0f);
		builder.skill(SkillList.SOUL_STRENGTH, 30.0f);
		builder.skill(SkillList.SOUL_DEPTH, 30.0f);
		builder.skill(SkillList.WEAPONLESS_FIGHTING, 50.0f);
		builder.skill(SkillList.GROUP_FIGHTING, 30.0f);
		
		builder.boundsValues(-0.5f, -1.0f, 0.5f, 1.42f);
		builder.handDamString("maul");
		builder.maxAge(100);
		builder.armourType(ArmourTypes.ARMOUR_LEATHER);
		builder.baseCombatRating(26.0f);
		builder.combatDamageType(Wound.TYPE_COLD);
		builder.denMaterial(Materials.MATERIAL_WOOD_BIRCH);
		builder.denName("spirit troll lair");
		builder.maxPercentOfCreatures(0.001f);
		builder.maxGroupAttackSize(100);
		
		templateId = builder.getTemplateId();
		return builder;
	}
	
	@Override
	public void addEncounters() {
		if (templateId == 0)
			return;

		new EncounterBuilder(Tiles.Tile.TILE_SNOW.id)
			.addCreatures(templateId, 1)
			.build(1);
	}
}
