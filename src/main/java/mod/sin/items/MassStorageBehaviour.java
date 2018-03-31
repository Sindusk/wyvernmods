package mod.sin.items;

import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.vehicles.ModVehicleBehaviour;
import org.gotti.wurmunlimited.modsupport.vehicles.VehicleFacade;

import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;

public class MassStorageBehaviour extends ModVehicleBehaviour {
	public static Logger logger = Logger.getLogger(MassStorageBehaviour.class.getName());
	public void setSettingsForVehicle(final Creature creature, final Vehicle vehicle){
		// Empty
	}
	/*public void setSettingsForVehicle(final Item item, final Vehicle v){
		logger.info("Setting vehicle behaviour for item "+item.getTemplate().getTemplateId());

		// Vehicle facade to access:
		VehicleFacade vehicle = wrap(v);
        vehicle.createPassengerSeats(1);
        vehicle.setCommandType((byte)1);
        vehicle.setCreature(false);
        vehicle.setSeatFightMod(0, 0.9f, 0.9f);
        vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.246f);
        vehicle.setSkillNeeded(19.0f);
	}*/
	public void setSettingsForVehicle(final Item item, final Vehicle v) {
		logger.info("Setting vehicle behaviour for item "+item.getTemplate().getTemplateId());
		/*
		 * Vehicle has some protected fields. The facade will deal with those.
		 */
		VehicleFacade vehicle = wrap(v);
		vehicle.createPassengerSeats(0);
		vehicle.setSeatFightMod(0, 0.7f, 0.4f);
		vehicle.setSeatOffset(0, 0f, 1.5f, -0.2f);
		vehicle.setCreature(false);
		vehicle.setEmbarkString("enter");
		vehicle.setName(item.getName());
		vehicle.setMaxDepth(9000f);
		vehicle.setMaxHeightDiff(0.00f);
		vehicle.setCommandType((byte)2);
		
		/*
		 * The Seat constructor is protected too. createSeat() will handle that
		 */
		final Seat[] hitches = { createSeat(Seat.TYPE_HITCHED) };
		hitches[0].offx = 3.0f;
		hitches[0].offy = 0.0f;
		vehicle.addHitchSeats(hitches);
	}
}
