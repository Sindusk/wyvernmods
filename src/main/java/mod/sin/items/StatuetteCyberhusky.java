package mod.sin.items;

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

public class StatuetteCyberhusky implements ItemTypes, MiscConstants {
	public static Logger logger = Logger.getLogger(StatuetteCyberhusky.class.getName());
	public static int templateId;
	private String name = "statuette of Cyberhusky";
	public void createTemplate() throws IOException{
		ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.statuette.cyberhusky");
		itemBuilder.name(name, "statuettes", "A statuette resembling the artists interpretation of the deity Cyberhusky.");
		itemBuilder.itemTypes(new short[]{ // {108, 52, 22, 44, 87, 92, 147} - Statuette
				ItemTypes.ITEM_TYPE_NAMED,
				ItemTypes.ITEM_TYPE_DECORATION,
				ItemTypes.ITEM_TYPE_METAL,
				ItemTypes.ITEM_TYPE_REPAIRABLE,
				ItemTypes.ITEM_TYPE_MATERIAL_PRICEEFFECT,
				ItemTypes.ITEM_TYPE_COLORABLE,
				ItemTypes.ITEM_TYPE_MISSION
		});
		itemBuilder.imageNumber((short) 282);
		itemBuilder.behaviourType((short) 35);
		itemBuilder.combatDamage(40);
		itemBuilder.decayTime(Long.MAX_VALUE);
		itemBuilder.dimensions(3, 5, 20);
		itemBuilder.primarySkill(-10);
		itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
		itemBuilder.modelName("model.decoration.statuette.magranon.");
		itemBuilder.difficulty(40.0f);
		itemBuilder.weightGrams(1000);
		itemBuilder.material(Materials.MATERIAL_SILVER);
		itemBuilder.value(20000);
		itemBuilder.isTraded(true);
		
		ItemTemplate template = itemBuilder.build();
		templateId = template.getTemplateId();
		logger.info(name+" TemplateID: "+templateId);
	}

	public void initCreationEntry(){
		logger.info("initCreationEntry()");
		if(templateId > 0){
			logger.info("Creating "+name+" creation entry, ID = "+templateId);
			CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_GOLDSMITHING, ItemList.anvilSmall, ItemList.silverBar,
					templateId, false, true, 0.0f, false, false, CreationCategories.STATUETTES);
			CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_GOLDSMITHING, ItemList.anvilSmall, ItemList.goldBar,
					templateId, false, true, 0.0f, false, false, CreationCategories.STATUETTES);
			//final AdvancedCreationEntry entry = CreationEntryCreator.createAdvancedEntry(SkillList.SMITHING_WEAPON_HEADS,
			//		ItemList.ironBand, ItemList.shaft, templateId, false, false, 0f, true, false, CreationCategories.TOOLS);
			//entry.addRequirement(new CreationRequirement(1, ItemList.woodenHandleSword, 2, true));
			//entry.addRequirement(new CreationRequirement(2, ItemList.nailsIronSmall, 1, true));
		}else{
			logger.info(name+" does not have a template ID on creation entry.");
		}
	}
}
