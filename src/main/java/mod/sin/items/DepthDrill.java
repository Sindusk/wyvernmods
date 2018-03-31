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

public class DepthDrill implements ItemTypes, MiscConstants {
	public static Logger logger = Logger.getLogger(DepthDrill.class.getName());
	public static int templateId;
	private String name = "depth drill";
	public void createTemplate() throws IOException{
		ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("wyvern.depthdrill");
		itemBuilder.name(name, "depth drills", "A tool for determining dirt depth.");
		itemBuilder.itemTypes(new short[]{
				ItemTypes.ITEM_TYPE_NAMED,
				ItemTypes.ITEM_TYPE_REPAIRABLE,
				ItemTypes.ITEM_TYPE_TOOL,
				ItemTypes.ITEM_TYPE_WEAPON_PIERCE
		});
		itemBuilder.imageNumber((short) 60);
		itemBuilder.behaviourType((short) 1);
		itemBuilder.combatDamage(0);
		itemBuilder.decayTime(Long.MAX_VALUE);
		itemBuilder.dimensions(6, 6, 96);
		itemBuilder.primarySkill(SkillList.CARPENTRY_FINE);
		itemBuilder.bodySpaces(EMPTY_BYTE_PRIMITIVE_ARRAY);
		itemBuilder.modelName("model.resource.shaft.");
		itemBuilder.difficulty(30.0f);
		itemBuilder.weightGrams(1100);
		itemBuilder.material(Materials.MATERIAL_IRON);
		itemBuilder.value(100);
		
		ItemTemplate template = itemBuilder.build();
		templateId = template.getTemplateId();
		logger.info(name+" TemplateID: "+templateId);
	}
	
	public void initCreationEntry(){
		logger.info("initCreationEntry()");
		if(templateId > 0){
			logger.info("Creating "+name+" creation entry, ID = "+templateId);
			final AdvancedCreationEntry entry = CreationEntryCreator.createAdvancedEntry(SkillList.CARPENTRY_FINE,
					ItemList.ironBand, ItemList.shaft, templateId, false, false, 0f, true, false, CreationCategories.TOOLS);
			entry.addRequirement(new CreationRequirement(1, ItemList.woodenHandleSword, 2, true));
			entry.addRequirement(new CreationRequirement(2, ItemList.nailsIronSmall, 1, true));
		}else{
			logger.info("Depth Drill does not have a template ID on creation entry.");
		}
	}
}
