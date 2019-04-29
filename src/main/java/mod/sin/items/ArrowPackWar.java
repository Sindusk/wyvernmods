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

public class ArrowPackWar {
	public static Logger logger = Logger.getLogger(ArrowPackWar.class.getName());
	public static int templateId;
	
	public void createTemplate() throws IOException{
		String name = "war arrow pack";
		ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.arrowpack.war");
		itemBuilder.name(name, "war arrow packs", "A pack of arrows, able to be unpacked into a full quiver.");
		itemBuilder.descriptions("excellent", "good", "ok", "poor");
		itemBuilder.itemTypes(new short[]{ // {108, 146, 44, 21, 147, 113} - War Arrow
				ItemTypes.ITEM_TYPE_NAMED,
				ItemTypes.ITEM_TYPE_REPAIRABLE,
				ItemTypes.ITEM_TYPE_WOOD,
				ItemTypes.ITEM_TYPE_WEAPON
		});
		itemBuilder.imageNumber((short) 760);
		itemBuilder.behaviourType((short) 1);
		itemBuilder.combatDamage(0);
		itemBuilder.decayTime(Long.MAX_VALUE);
		itemBuilder.dimensions(15, 15, 50);
		itemBuilder.primarySkill(-10);
		itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
		itemBuilder.modelName("model.container.quiver.");
		itemBuilder.difficulty(55.0f);
		itemBuilder.weightGrams(4500);
		itemBuilder.material(Materials.MATERIAL_WOOD_BIRCH);
		itemBuilder.value(1000);
		itemBuilder.isTraded(true);
		
		ItemTemplate template = itemBuilder.build();
		templateId = template.getTemplateId();
		logger.info(name+" TemplateID: "+templateId);
	}
	
	public void initCreationEntry(){
		logger.info("initCreationEntry()");
		if(templateId > 0){
	        AdvancedCreationEntry huntingPack = CreationEntryCreator.createAdvancedEntry(SkillList.GROUP_FLETCHING,
	        		ItemList.quiver, ItemList.arrowWar, templateId,
	                false, false, 0.0F, true, false, 0, 50.0D, CreationCategories.FLETCHING);
	
	        huntingPack.addRequirement(new CreationRequirement(1, ItemList.arrowWar, 39, true));
		}
    }
}
