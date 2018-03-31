package mod.sin.items;

import java.io.IOException;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;

public class ArenaSupplyDepot implements MiscConstants {
	public static Logger logger = Logger.getLogger(ArenaSupplyDepot.class.getName());
	public static int templateId;
	
	public void createTemplate() throws IOException{
		String name = "arena depot";
		ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.arena.depot");
		itemBuilder.name(name, "arena depots", "Contains a valuable cache of treasures.");
		// {108, 135, 1, 31, 25, 51, 86, 52, 59, 44, 147, 176, 180, 209, 199}
		itemBuilder.itemTypes(new short[]{
				ItemTypes.ITEM_TYPE_NAMED,
				ItemTypes.ITEM_TYPE_WOOD,
				ItemTypes.ITEM_TYPE_NOTAKE,
				ItemTypes.ITEM_TYPE_LOCKABLE,
				ItemTypes.ITEM_TYPE_DECORATION,
				ItemTypes.ITEM_TYPE_ONE_PER_TILE,
				ItemTypes.ITEM_TYPE_OWNER_TURNABLE,
				ItemTypes.ITEM_TYPE_REPAIRABLE,
				ItemTypes.ITEM_TYPE_MISSION,
				ItemTypes.ITEM_TYPE_PLANTABLE
		});
		itemBuilder.imageNumber((short) 462);
		itemBuilder.behaviourType((short) 1);
		itemBuilder.combatDamage(0);
		itemBuilder.decayTime(Long.MAX_VALUE);
		itemBuilder.dimensions(300, 300, 300);
		itemBuilder.primarySkill((int) NOID);
		itemBuilder.bodySpaces(EMPTY_BYTE_PRIMITIVE_ARRAY);
		itemBuilder.modelName("model.structure.war.supplydepot.2.0.");
		itemBuilder.difficulty(5.0f);
		itemBuilder.weightGrams(50000);
		itemBuilder.material(Materials.MATERIAL_WOOD_BIRCH);
		itemBuilder.value(5000);
		
		ItemTemplate template = itemBuilder.build();
		templateId = template.getTemplateId();
		logger.info(name+" TemplateID: "+templateId);
	}
}
