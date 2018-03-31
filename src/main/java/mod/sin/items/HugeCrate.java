package mod.sin.items;

import java.io.IOException;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.AdvancedCreationEntry;
import com.wurmonline.server.items.CreationCategories;
import com.wurmonline.server.items.CreationEntryCreator;
import com.wurmonline.server.items.CreationRequirement;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.skills.SkillList;

public class HugeCrate implements ItemTypes, MiscConstants {
	public static Logger logger = Logger.getLogger(HugeCrate.class.getName());
	public static int templateId;
	public void createTemplate() throws IOException{
		String name = "huge crate";
		ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.crate.huge");
		itemBuilder.name(name, "huge crates", "A huge crate made from planks, primarily used to transport goods.");
		itemBuilder.itemTypes(new short[]{ // {108, 135, 167, 21, 51, 52, 44, 1, 92, 145, 176} - large crate
				ItemTypes.ITEM_TYPE_NAMED,
				ItemTypes.ITEM_TYPE_OWNER_DESTROYABLE,
				ItemTypes.ITEM_TYPE_WOOD,
				ItemTypes.ITEM_TYPE_TURNABLE,
				ItemTypes.ITEM_TYPE_DECORATION,
				ItemTypes.ITEM_TYPE_REPAIRABLE,
				ItemTypes.ITEM_TYPE_HOLLOW,
				ItemTypes.ITEM_TYPE_COLORABLE,
				ItemTypes.ITEM_TYPE_BULKCONTAINER,
				ItemTypes.ITEM_TYPE_TRANSPORTABLE
		});
		itemBuilder.imageNumber((short) 311);
		itemBuilder.behaviourType((short) 1);
		itemBuilder.combatDamage(0);
		itemBuilder.decayTime(9072000);
		itemBuilder.dimensions(140, 140, 140);
		itemBuilder.primarySkill((int) NOID);
		itemBuilder.bodySpaces(EMPTY_BYTE_PRIMITIVE_ARRAY);
		itemBuilder.modelName("model.container.crate.large.");
		itemBuilder.difficulty(70.0f);
		itemBuilder.weightGrams(20000);
		itemBuilder.material(Materials.MATERIAL_WOOD_BIRCH);
		itemBuilder.value(10000);
		itemBuilder.isTraded(false);
		
		ItemTemplate template = itemBuilder.build();
		templateId = template.getTemplateId();
		logger.info(name+" TemplateID: "+ templateId);
	}
	
	public void initCreationEntry() {
		logger.info("initCreationEntry()");
		if (templateId > 0) {
			logger.info("Creating Huge Crate creation entry, ID = "+templateId);
			final AdvancedCreationEntry entry = CreationEntryCreator.createAdvancedEntry(SkillList.CARPENTRY_FINE,
					ItemList.plank, ItemList.nailsIronLarge, templateId, false, false, 0f, true, false, CreationCategories.TOOLS);
			entry.addRequirement(new CreationRequirement(1, ItemList.plank, 20, true));
		} else {
			logger.info("Huge Crate does not have a template ID on creation entry.");
		}
	}
}
