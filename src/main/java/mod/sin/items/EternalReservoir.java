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

public class EternalReservoir implements ItemTypes, MiscConstants {
	protected static Logger logger = Logger.getLogger(EternalReservoir.class.getName());
	public static int templateId;
	public void createTemplate() throws IOException{
		String name = "eternal reservoir";
		ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.eternal.reservoir");
		itemBuilder.name(name, "eternal reservoir", "Fueled by the souls of the fallen, this black magic device stores souls that tend to nearby creatures and fires.");
		itemBuilder.itemTypes(new short[]{ // {25, 49, 31, 52, 40} Stone of Soulfall {108, 31, 25, 194, 52, 44, 195, 67, 49, 123, 178, 157} // Colossus
				ItemTypes.ITEM_TYPE_STONE,
				ItemTypes.ITEM_TYPE_REPAIRABLE,
				ItemTypes.ITEM_TYPE_NOTAKE,
				ItemTypes.ITEM_TYPE_DECORATION,
				ItemTypes.ITEM_TYPE_USE_GROUND_ONLY,
				ItemTypes.ITEM_TYPE_HASDATA,
				ItemTypes.ITEM_TYPE_NEVER_SHOW_CREATION_WINDOW_OPTION,
				ItemTypes.ITEM_TYPE_NOT_MISSION
		});
		itemBuilder.imageNumber((short) 60);
		itemBuilder.behaviourType((short) 1);
		itemBuilder.combatDamage(0);
		itemBuilder.decayTime(Long.MAX_VALUE);
		itemBuilder.dimensions(500, 500, 1000);
		itemBuilder.primarySkill((int)MiscConstants.NOID);
		itemBuilder.bodySpaces(EMPTY_BYTE_PRIMITIVE_ARRAY);
		itemBuilder.modelName("model.structure.rift.altar.1.");
		itemBuilder.difficulty(40.0f);
		itemBuilder.weightGrams(200000);
		itemBuilder.material(Materials.MATERIAL_STONE);
		itemBuilder.value(10000);
		
		ItemTemplate template = itemBuilder.build();
		templateId = template.getTemplateId();
		logger.info(name+" TemplateID: "+templateId);
	}

    public void initCreationEntry(){
		logger.info("initCreationEntry()");
		if(templateId > 0){
	        AdvancedCreationEntry massStorage = CreationEntryCreator.createAdvancedEntry(SkillList.POTTERY,
	        		ItemList.diamondStar, ItemList.dirtPile, templateId,
	                false, false, 0.0F, true, true, 0, 0.0D, CreationCategories.ALTAR);

	        massStorage.addRequirement(new CreationRequirement(1, ItemList.dirtPile, 99, true));
	        massStorage.addRequirement(new CreationRequirement(2, ItemList.brickPottery, 200, true));
	        massStorage.addRequirement(new CreationRequirement(3, ChaosCrystal.templateId, 30, true));
	        massStorage.addRequirement(new CreationRequirement(4, ItemList.heart, 20, true));
		}
    }
}
