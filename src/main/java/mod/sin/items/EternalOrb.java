package mod.sin.items;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import java.io.IOException;
import java.util.logging.Logger;

public class EternalOrb implements ItemTypes, MiscConstants {
	public static Logger logger = Logger.getLogger(EternalOrb.class.getName());
	public static int templateId;
	
	public void createTemplate() throws IOException{
		String name = "eternal orb";
		ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.eternal.orb");
		itemBuilder.name(name, "eternal orbs", "Legends say it consumes magic from an object, and moves it to another.");
		itemBuilder.itemTypes(new short[]{
				ItemTypes.ITEM_TYPE_FULLPRICE,
				ItemTypes.ITEM_TYPE_NOSELLBACK,
				ItemTypes.ITEM_TYPE_ALWAYS_BANKABLE
		});
		itemBuilder.imageNumber((short) 819);
		itemBuilder.behaviourType((short) 1);
		itemBuilder.combatDamage(0);
		itemBuilder.decayTime(Long.MAX_VALUE);
		itemBuilder.dimensions(1, 1, 1);
		itemBuilder.primarySkill((int) NOID);
		itemBuilder.bodySpaces(EMPTY_BYTE_PRIMITIVE_ARRAY);
		itemBuilder.modelName("model.artifact.orbdoom");
		itemBuilder.difficulty(5.0f);
		itemBuilder.weightGrams(500);
		itemBuilder.material(Materials.MATERIAL_CRYSTAL);
		itemBuilder.value(200000);
		itemBuilder.isTraded(true);
		
		ItemTemplate template = itemBuilder.build();
		templateId = template.getTemplateId();
		logger.info(name+" TemplateID: "+templateId);
	}
}
