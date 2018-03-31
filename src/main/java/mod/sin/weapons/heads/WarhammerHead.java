package mod.sin.weapons.heads;

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

public class WarhammerHead implements ItemTypes, MiscConstants {
	public static Logger logger = Logger.getLogger(WarhammerHead.class.getName());
	public static int templateId;
	private String name = "warhammer head";
	public void createTemplate() throws IOException{
		/*  ItemTemplateCreator.createItemTemplate(293, 4, 
		 * "maul head", "large maul heads", "excellent", "good", "ok", "poor", 
		 * "The thick, heavy, spiked metal head for a maul.", 
		 * new short[]{44, 22}, 1232, 1, 0, 3024000, 20, 20, 20, -10, MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY, 
		 * "model.weapon.head.large.maul.", 15.0f, 4000, 11, 100, false, -1);
		 */
		ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.warhammer.head");
		itemBuilder.name(name, "warhammer heads", "A warhammer head.");
		itemBuilder.itemTypes(new short[]{ // new short[]{44, 22} - Large Maul Head
				ItemTypes.ITEM_TYPE_NAMED,
				ItemTypes.ITEM_TYPE_METAL
		});
		itemBuilder.imageNumber((short) 1232);
		itemBuilder.behaviourType((short) 35);
		itemBuilder.combatDamage(40);
		itemBuilder.decayTime(Long.MAX_VALUE);
		itemBuilder.dimensions(5, 10, 80);
		itemBuilder.primarySkill(SkillList.WARHAMMER);
		itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
		itemBuilder.modelName("model.artifact.hammerhuge.");
		itemBuilder.difficulty(25.0f);
		itemBuilder.weightGrams(6000);
		itemBuilder.material(Materials.MATERIAL_IRON);
		itemBuilder.value(1000);
		
		ItemTemplate template = itemBuilder.build();
		templateId = template.getTemplateId();
		logger.info(name+" TemplateID: "+templateId);
	}

	public void initCreationEntry(){
		logger.info("initCreationEntry()");
		if(templateId > 0){
			logger.info("Creating "+name+" creation entry, ID = "+templateId);
			CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilLarge, ItemList.ironBar,
					templateId, false, true, 0.0f, false, false, CreationCategories.WEAPON_HEADS);
			CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilLarge, ItemList.steelBar,
					templateId, false, true, 0.0f, false, false, CreationCategories.WEAPON_HEADS);
			CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilLarge, ItemList.adamantineBar,
					templateId, false, true, 0.0f, false, false, CreationCategories.WEAPON_HEADS);
			CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilLarge, ItemList.glimmerSteelBar,
					templateId, false, true, 0.0f, false, false, CreationCategories.WEAPON_HEADS);
			CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilLarge, ItemList.seryllBar,
					templateId, false, true, 0.0f, false, false, CreationCategories.WEAPON_HEADS);
			//final AdvancedCreationEntry entry = CreationEntryCreator.createAdvancedEntry(SkillList.SMITHING_WEAPON_HEADS,
			//		ItemList.ironBand, ItemList.shaft, templateId, false, false, 0f, true, false, CreationCategories.TOOLS);
			//entry.addRequirement(new CreationRequirement(1, ItemList.woodenHandleSword, 2, true));
			//entry.addRequirement(new CreationRequirement(2, ItemList.nailsIronSmall, 1, true));
		}else{
			logger.info(name+" does not have a template ID on creation entry.");
		}
	}
}
