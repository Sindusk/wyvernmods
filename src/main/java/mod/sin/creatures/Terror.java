package mod.sin.creatures;

import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.bodys.BodyTemplate;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.combat.ArmourTypes;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.shared.constants.CreatureTypes;
import org.gotti.wurmunlimited.modsupport.CreatureTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.creatures.ModCreature;
import org.gotti.wurmunlimited.modsupport.vehicles.ModVehicleBehaviour;
import org.gotti.wurmunlimited.modsupport.vehicles.VehicleFacade;

public class Terror implements ModCreature, CreatureTypes {
	public static int templateId;
	@Override
	public CreatureTemplateBuilder createCreateTemplateBuilder() {
		// {C_TYPE_MOVE_LOCAL, C_TYPE_VEHICLE, C_TYPE_ANIMAL, C_TYPE_LEADABLE, C_TYPE_GRAZER, C_TYPE_OMNIVORE, C_TYPE_DOMINATABLE, C_TYPE_AGG_HUMAN, C_TYPE_NON_NEWBIE, C_TYPE_BURNING}; - Hell Horse
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
		
		//public CreatureTemplateBuilder(final String identifier, final String name, final String description,
		//       final String modelName, final int[] types, final byte bodyType, final short vision, final byte sex, final short centimetersHigh, final short centimetersLong, final short centimetersWide,
		//       final String deathSndMale, final String deathSndFemale, final String hitSndMale, final String hitSndFemale,
		//       final float naturalArmour, final float handDam, final float kickDam, final float biteDam, final float headDam, final float breathDam, final float speed, final int moveRate,
		//       final int[] itemsButchered, final int maxHuntDist, final int aggress) {
		CreatureTemplateBuilder builder = new CreatureTemplateBuilder("mod.creature.terror", "Terror", "Run.",
				"model.creature.dragon.red", types, BodyTemplate.TYPE_DRAGON, (short) 10, (byte) 0, (short) 350, (short) 100, (short) 60,
				"sound.death.dragon", "sound.death.dragon", "sound.combat.hit.dragon", "sound.combat.hit.dragon",
				1.0f, 3.0f, 3.0f, 0.0f, 0.0f, 0.0f, 2.5f, 2500,
				new int[]{}, 7, 70, Materials.MATERIAL_MEAT_DRAGON);
		
		builder.skill(SkillList.BODY_STRENGTH, 22.0f);
		builder.skill(SkillList.BODY_STAMINA, 20.0f);
		builder.skill(SkillList.BODY_CONTROL, 20.0f);
		builder.skill(SkillList.MIND_LOGICAL, 20.0f);
		builder.skill(SkillList.MIND_SPEED, 20.0f);
		builder.skill(SkillList.SOUL_STRENGTH, 20.0f);
		builder.skill(SkillList.SOUL_DEPTH, 20.0f);
		builder.skill(SkillList.WEAPONLESS_FIGHTING, 25.0f);
		builder.skill(SkillList.GROUP_FIGHTING, 25.0f);

		builder.boundsValues(-0.5f, -1.0f, 0.5f, 1.42f);
		builder.handDamString("bite");
		builder.kickDamString("wingbuff");
		builder.maxAge(200);
		builder.armourType(ArmourTypes.ARMOUR_SCALE_DRAGON);
		builder.baseCombatRating(3.0f);
		builder.combatDamageType(Wound.TYPE_BURN);
		builder.maxGroupAttackSize(10);

		templateId = builder.getTemplateId();
		return builder;
	}
	
	public ModVehicleBehaviour getVehicleBehaviour() {

		return new ModVehicleBehaviour() {

			@Override
			public void setSettingsForVehicle(Item item, Vehicle vehicle) {
			}

			@Override
			public void setSettingsForVehicle(Creature creature, Vehicle v) {
				VehicleFacade vehicle = wrap(v);

				vehicle.createPassengerSeats(0);
				vehicle.setSeatFightMod(0, 0.8f, 1.1f);
				vehicle.setSeatOffset(0, 0.2f, 0.0f, 0.0f);
				vehicle.setCreature(true);
				vehicle.setSkillNeeded(95f);
				vehicle.setName(creature.getName());
				vehicle.setMaxHeightDiff(0.10f);
				vehicle.setMaxDepth(-50f);
				vehicle.setMaxSpeed(90.0f);
				vehicle.setCommandType((byte) 3);
				vehicle.setCanHaveEquipment(true);
			}
		};
	}
	
	@Override
	public void addEncounters() {
		if (templateId == 0)
			return;
	}
}
