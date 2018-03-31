package mod.sin.weapons;

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

public class Club implements ItemTypes, MiscConstants {
	public static Logger logger = Logger.getLogger(Club.class.getName());
	public static int templateId;
	private String name = "club";
	public void createTemplate() throws IOException{
		/*  ItemTemplateCreator.createItemTemplate(337, 
		 * "Hammer of Magranon", "hammers of magranon", "excellent", "good", "ok", "poor", 
		 * "A huge brutal warhammer made totally from bronze.", 
		 * new short[]{52, 48, 69, 37, 14, 40, 71}, 
		 * 1339, 35, 80, Long.MAX_VALUE, 5, 10, 80, 10070, MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY, 
		 * "model.artifact.hammerhuge.", 99.0f, 7000, 31, 3000000, false);
		 */
		ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.club");
		itemBuilder.name(name, "clubs", "A blunt weapon.");
		itemBuilder.itemTypes(new short[]{ // new short[]{108, 44, 147, 22, 37, 14, 189} - Large Maul
				ItemTypes.ITEM_TYPE_NAMED,
				ItemTypes.ITEM_TYPE_REPAIRABLE,
				ItemTypes.ITEM_TYPE_WOOD,
				ItemTypes.ITEM_TYPE_WEAPON,
				ItemTypes.ITEM_TYPE_WEAPON_CRUSH
		});
		itemBuilder.imageNumber((short) 1239);
		itemBuilder.behaviourType((short) 35);
		itemBuilder.combatDamage(35);
		itemBuilder.decayTime(Long.MAX_VALUE);
		itemBuilder.dimensions(5, 10, 80);
		itemBuilder.primarySkill(SkillList.CLUB_HUGE);
		itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
		itemBuilder.modelName("model.weapon.club.huge.");
		itemBuilder.difficulty(40.0f);
		itemBuilder.weightGrams(8000);
		itemBuilder.material(Materials.MATERIAL_WOOD_BIRCH);
		itemBuilder.value(1000);
		
		ItemTemplate template = itemBuilder.build();
		templateId = template.getTemplateId();
		logger.info(name+" TemplateID: "+templateId);
	}
	
	public void initCreationEntry(){
		logger.info("initCreationEntry()");
		if(templateId > 0){
			logger.info("Creating "+name+" creation entry, ID = "+templateId);
			CreationEntryCreator.createSimpleEntry(SkillList.CARPENTRY_FINE, ItemList.knifeCarving, ItemList.log,
					templateId, false, true, 0.0f, false, false, CreationCategories.WEAPONS);
			//final AdvancedCreationEntry entry = CreationEntryCreator.createAdvancedEntry(SkillList.SMITHING_WEAPON_HEADS,
			//		ItemList.ironBand, ItemList.shaft, templateId, false, false, 0f, true, false, CreationCategories.TOOLS);
			//entry.addRequirement(new CreationRequirement(1, ItemList.woodenHandleSword, 2, true));
			//entry.addRequirement(new CreationRequirement(2, ItemList.nailsIronSmall, 1, true));
		}else{
			logger.info(name+" does not have a template ID on creation entry.");
		}
	}
}
