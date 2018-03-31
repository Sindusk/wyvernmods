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

public class CoinDecoration implements ItemTypes, MiscConstants {
	public static Logger logger = Logger.getLogger(CoinDecoration.class.getName());
	public static int templateId;
	private String name = "coin pile";
	public void createTemplate() throws IOException{
		ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.coin.pile");
		itemBuilder.name(name, "coin piles", "A pile of decorative coins.");
		itemBuilder.itemTypes(new short[]{ // {108, 21, 135, 86, 31, 51, 52, 157, 44, 92, 176} - Table
				ItemTypes.ITEM_TYPE_NAMED,
				ItemTypes.ITEM_TYPE_OWNER_DESTROYABLE,
				ItemTypes.ITEM_TYPE_DESTROYABLE,
				ItemTypes.ITEM_TYPE_TURNABLE,
				ItemTypes.ITEM_TYPE_DECORATION,
				ItemTypes.ITEM_TYPE_NOT_MISSION,
				ItemTypes.ITEM_TYPE_REPAIRABLE,
				ItemTypes.ITEM_TYPE_COLORABLE
		});
		itemBuilder.imageNumber((short) 572);
		itemBuilder.behaviourType((short) 1);
		itemBuilder.combatDamage(0);
		itemBuilder.decayTime(Long.MAX_VALUE);
		itemBuilder.dimensions(5, 5, 5);
		itemBuilder.primarySkill(SkillList.MISCELLANEOUS);
		itemBuilder.bodySpaces(EMPTY_BYTE_PRIMITIVE_ARRAY);
		itemBuilder.modelName("model.pile.coin.");
		itemBuilder.difficulty(70.0f);
		itemBuilder.weightGrams(1000);
		itemBuilder.material(Materials.MATERIAL_COPPER);
		itemBuilder.value(100);
		
		ItemTemplate template = itemBuilder.build();
		templateId = template.getTemplateId();
		logger.info(name+" TemplateID: "+templateId);
	}
	
	public void initCreationEntry(){
		logger.info("initCreationEntry()");
		if(templateId > 0){
			logger.info("Creating "+name+" creation entry, ID = "+templateId);
			final AdvancedCreationEntry entry = CreationEntryCreator.createAdvancedEntry(SkillList.MISCELLANEOUS,
					ItemList.coinCopper, ItemList.coinCopper, templateId, false, false, 0f, true, false, CreationCategories.DECORATION);
			entry.addRequirement(new CreationRequirement(1, ItemList.coinCopper, 3, true));
		}else{
			logger.info(name+" does not have a template ID on creation entry.");
		}
	}
}
