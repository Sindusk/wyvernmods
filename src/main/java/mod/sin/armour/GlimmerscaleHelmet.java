package mod.sin.armour;

import java.io.IOException;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.combat.ArmourTypes;
import com.wurmonline.server.items.CreationCategories;
import com.wurmonline.server.items.CreationEntryCreator;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.skills.SkillList;

public class GlimmerscaleHelmet implements ItemTypes, MiscConstants {
	public static Logger logger = Logger.getLogger(GlimmerscaleHelmet.class.getName());
	public static int templateId;
	private String name = "glimmerscale helmet";
	public void createTemplate() throws IOException{
		/*ItemTemplateCreator.createItemTemplate(473, 3, "drake hide jacket", "drake hide jackets", "excellent", "good", "ok", "poor",
		 * "A jacket made from finest drake hide with brass husks.",
		 * new short[]{108, 44, 23, 4, 99},
		 * 1060, 1, 0, 29030400, 2, 40, 40, -10, new byte[]{2},
		 * "model.armour.torso.dragon.", 70.0f, 600, 16, 100000, true, 9);
		 */
		ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.glimmerscale.helmet");
		itemBuilder.name(name, "glimmerscale helmets", "A horned helmet made of glimmerscale.");
		itemBuilder.itemTypes(new short[]{ // {108, 44, 23, 4, 99} - Drake hide jacket
				ItemTypes.ITEM_TYPE_NAMED,
				ItemTypes.ITEM_TYPE_REPAIRABLE,
				ItemTypes.ITEM_TYPE_METAL,
				ItemTypes.ITEM_TYPE_ARMOUR,
				ItemTypes.ITEM_TYPE_DRAGONARMOUR
		});
		itemBuilder.imageNumber((short) 968);
		itemBuilder.behaviourType((short) 1);
		itemBuilder.combatDamage(0);
		itemBuilder.decayTime(Long.MAX_VALUE);
		itemBuilder.dimensions(2, 40, 40);
		itemBuilder.primarySkill(-10);
		itemBuilder.bodySpaces(new byte[]{1, 28});
		itemBuilder.modelName("model.armour.head.greathelmHornedOfGold.");
		itemBuilder.difficulty(76.0f);
		itemBuilder.weightGrams(2500);
		itemBuilder.material(Materials.MATERIAL_GLIMMERSTEEL);
		itemBuilder.value(1000000);
		itemBuilder.armourType(ArmourTypes.ARMOUR_SCALE_DRAGON);
		
		ItemTemplate template = itemBuilder.build();
		templateId = template.getTemplateId();
		logger.info(name+" TemplateID: "+templateId);
	}
	
	public void initCreationEntry(){
		logger.info("initCreationEntry()");
		if(templateId > 0){
			logger.info("Creating "+name+" creation entry, ID = "+templateId);
			CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_ARMOUR_PLATE, ItemList.anvilLarge, Glimmerscale.templateId,
					templateId, false, true, 0.0f, false, false, CreationCategories.ARMOUR);
			//final AdvancedCreationEntry entry = CreationEntryCreator.createAdvancedEntry(SkillList.SMITHING_WEAPON_HEADS,
			//		ItemList.ironBand, ItemList.shaft, templateId, false, false, 0f, true, false, CreationCategories.TOOLS);
			//entry.addRequirement(new CreationRequirement(1, ItemList.woodenHandleSword, 2, true));
			//entry.addRequirement(new CreationRequirement(2, ItemList.nailsIronSmall, 1, true));
		}else{
			logger.info(name+" does not have a template ID on creation entry.");
		}
	}
}
