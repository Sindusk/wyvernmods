package mod.sin.items;

import java.io.IOException;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;

public class AffinityOrb implements ItemTypes, MiscConstants {
	public static Logger logger = Logger.getLogger(AffinityOrb.class.getName());
	public static int templateId;
	
	public void createTemplate() throws IOException{
		String name = "affinity orb";
		ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("wyvern.affinityorb");
		itemBuilder.name(name, "affinity orbs", "A valuable orb that infuses the user with hidden knowledge.");
		itemBuilder.descriptions("brilliantly glowing", "strongly glowing", "faintly glowing", "barely glowing");
		itemBuilder.itemTypes(new short[]{
				ItemTypes.ITEM_TYPE_MAGIC,
				ItemTypes.ITEM_TYPE_FULLPRICE,
				ItemTypes.ITEM_TYPE_NOSELLBACK,
				ItemTypes.ITEM_TYPE_ALWAYS_BANKABLE
		});
		itemBuilder.imageNumber((short) 919);
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
		itemBuilder.value(1000000);
		itemBuilder.isTraded(true);
		
		ItemTemplate template = itemBuilder.build();
		templateId = template.getTemplateId();
		logger.info(name+" TemplateID: "+templateId);
	}
}
