package mod.sin.armour;

import java.io.IOException;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.CreationCategories;
import com.wurmonline.server.items.CreationEntryCreator;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.skills.SkillList;

public class Glimmerscale {
	private static Logger logger = Logger.getLogger(Glimmerscale.class.getName());
	public static int templateId;
	private String name = "glimmerscale";
	public void createTemplate() throws IOException{
		ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("item.mod.glimmerscale.scale");
		itemBuilder.name(name, "glimmerscales", "Legendary scales, made from the careful combination of dragon scales and glimmersteel.");
		itemBuilder.descriptions("excellent", "good", "ok", "poor");
		itemBuilder.itemTypes(new short[]{ // {22, 146, 46, 113, 157} - Addy Lump
				ItemTypes.ITEM_TYPE_METAL,
				ItemTypes.ITEM_TYPE_BULK,
				ItemTypes.ITEM_TYPE_COMBINE,
				ItemTypes.ITEM_TYPE_NOT_MISSION
		});
		itemBuilder.imageNumber((short) 554);
		itemBuilder.behaviourType((short) 1);
		itemBuilder.combatDamage(0);
		itemBuilder.decayTime(Long.MAX_VALUE);
		itemBuilder.dimensions(10, 30, 30);
		itemBuilder.primarySkill(-10);
		itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
		itemBuilder.modelName("model.resource.scales.dragon.");
		itemBuilder.difficulty(60.0f);
		itemBuilder.weightGrams(400);
		itemBuilder.material(Materials.MATERIAL_UNDEFINED);
		itemBuilder.value(200000);
		
		ItemTemplate template = itemBuilder.build();
		templateId = template.getTemplateId();
		logger.info(name+" TemplateID: "+templateId);
	}
	
	public void initCreationEntry(){
		logger.info("initCreationEntry()");
		if(templateId > 0){
			logger.info("Creating "+name+" creation entry, ID = "+templateId);
			// CreationEntryCreator.createSimpleEntry(10041, 220, 47, 223, true, true, 0.0f, false, false, CreationCategories.RESOURCES);
			CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_METALLURGY, ItemList.dragonScale, ItemList.glimmerSteelBar,
					templateId, true, true, 0.0f, true, false, CreationCategories.RESOURCES);
		}else{
			logger.info(name+" does not have a template ID on creation entry.");
		}
	}
}
