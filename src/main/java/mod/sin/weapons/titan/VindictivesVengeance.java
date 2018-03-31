package mod.sin.weapons.titan;

import java.io.IOException;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.skills.SkillList;

public class VindictivesVengeance implements ItemTypes, MiscConstants {
	public static Logger logger = Logger.getLogger(VindictivesVengeance.class.getName());
	public static int templateId;
	private String name = "Vindictive's Vengeance";
	public void createTemplate() throws IOException{
		/*  ItemTemplateCreator.createItemTemplate(337, 
		 * "Hammer of Magranon", "hammers of magranon", "excellent", "good", "ok", "poor", 
		 * "A huge brutal warhammer made totally from bronze.", 
		 * new short[]{52, 48, 69, 37, 14, 40, 71}, 
		 * 1339, 35, 80, Long.MAX_VALUE, 5, 10, 80, 10070, MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY, 
		 * "model.artifact.hammerhuge.", 99.0f, 7000, 31, 3000000, false);
		 */
		ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.titan.vindictive.vengeance");
		itemBuilder.name(name, "Vindictive Vengeances", "A sickle as light as a feather, capable of swinging as swiftly as the user can wield it.");
		itemBuilder.itemTypes(new short[]{ // new short[]{108, 44, 147, 22, 37, 14, 189} - Large Maul
				ItemTypes.ITEM_TYPE_NAMED,
				ItemTypes.ITEM_TYPE_METAL,
				ItemTypes.ITEM_TYPE_REPAIRABLE,
				ItemTypes.ITEM_TYPE_WEAPON,
				ItemTypes.ITEM_TYPE_WEAPON_SLASH
		});
		itemBuilder.imageNumber((short) 752);
		itemBuilder.behaviourType((short) 35);
		itemBuilder.combatDamage(40);
		itemBuilder.decayTime(Long.MAX_VALUE);
		itemBuilder.dimensions(5, 10, 80);
		itemBuilder.primarySkill(SkillList.SICKLE);
		itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
		itemBuilder.modelName("model.weapon.sickle.");
		itemBuilder.difficulty(90.0f);
		itemBuilder.weightGrams(100);
		itemBuilder.material(Materials.MATERIAL_ADAMANTINE);
		itemBuilder.value(1000000);
		
		ItemTemplate template = itemBuilder.build();
		templateId = template.getTemplateId();
		logger.info(name+" TemplateID: "+templateId);
	}
}
