package mod.sin.items;

import java.io.IOException;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;

public class DisintegrationRod implements ItemTypes, MiscConstants {
	public static Logger logger = Logger.getLogger(DisintegrationRod.class.getName());
	public static int templateId;
	public void createTemplate() throws IOException{
		String name = "Rod of Disintegration";
		ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("item.mod.rod.disintegration");
		itemBuilder.name(name, "rods of disintegration", "A rod designed for removal of ore veins and cave walls.");
		itemBuilder.itemTypes(new short[]{ // {42, 53, 127, 155} - Rod of Transmutation {43, 42, 5, 76, 53, 127} - Sleep Powder
				ItemTypes.ITEM_TYPE_FULLPRICE,
				ItemTypes.ITEM_TYPE_NOSELLBACK,
				ItemTypes.ITEM_TYPE_ALWAYS_BANKABLE
		});
		itemBuilder.imageNumber((short) 1259);
		itemBuilder.behaviourType((short) 1);
		itemBuilder.combatDamage(0);
		itemBuilder.decayTime(Long.MAX_VALUE);
		itemBuilder.dimensions(5, 10, 60);
		itemBuilder.primarySkill((int) NOID);
		itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
		itemBuilder.modelName("model.tool.rodtrans.");
		itemBuilder.difficulty(300.0f);
		itemBuilder.weightGrams(1000);
		itemBuilder.material(Materials.MATERIAL_STONE);
		itemBuilder.value(50000);
		itemBuilder.isTraded(true);
		
		ItemTemplate template = itemBuilder.build();
		templateId = template.getTemplateId();
		logger.info(name+" TemplateID: "+templateId);
	}
}
