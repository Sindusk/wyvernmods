package mod.sin.items;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.*;
import com.wurmonline.server.skills.SkillList;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import java.io.IOException;
import java.util.logging.Logger;

public class TitaniumLump {
	private static Logger logger = Logger.getLogger(TitaniumLump.class.getName());
	public static int templateId;
	private String name = "lump, titanium";
	public void createTemplate() throws IOException{
		ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.lump.titanium");
		itemBuilder.name(name, "titanium lumps", "A lightweight lump of glistening titanium.");
		itemBuilder.itemTypes(new short[]{ // {22, 146, 46, 113, 157} - Addy Lump
				ItemTypes.ITEM_TYPE_METAL,
				ItemTypes.ITEM_TYPE_BULK,
				ItemTypes.ITEM_TYPE_COMBINE
		});
		itemBuilder.imageNumber((short) 638);
		itemBuilder.behaviourType((short) 1);
		itemBuilder.combatDamage(0);
		itemBuilder.decayTime(Long.MAX_VALUE);
		itemBuilder.dimensions(3, 3, 3);
		itemBuilder.primarySkill(-10);
		itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
		itemBuilder.modelName("model.resource.lump.");
		itemBuilder.difficulty(40.0f);
		itemBuilder.weightGrams(400);
		itemBuilder.material(Materials.MATERIAL_UNDEFINED);
		itemBuilder.value(200);
		
		ItemTemplate template = itemBuilder.build();
		templateId = template.getTemplateId();
		logger.info(name+" TemplateID: "+templateId);
	}
	
	public void initCreationEntry(){
		logger.info("initCreationEntry()");
		if(templateId > 0){
			logger.info("Creating "+name+" creation entry, ID = "+templateId);
			// CreationEntryCreator.createSimpleEntry(10041, 220, 47, 223, true, true, 0.0f, false, false, CreationCategories.RESOURCES);
			CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_METALLURGY, ItemList.adamantineBar, ItemList.glimmerSteelBar,
					templateId, true, true, 0.0f, true, false, CreationCategories.RESOURCES);
		}else{
			logger.info(name+" does not have a template ID on creation entry.");
		}
	}
}
