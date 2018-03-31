 package mod.sin.armour;

import java.io.IOException;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;

public class SpectralHide implements ItemTypes, MiscConstants {
	private static Logger logger = Logger.getLogger(SpectralHide.class.getName());
	public static int templateId;
	public void createTemplate() throws IOException{
		String name = "spectral hide";
		ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("wyvern.spectral.hide");
		itemBuilder.name(name, "spectral hides", "Lightweight and transparent, this ethereal leather comes from another plane of existance. It is stronger than natural drake hide.");
		itemBuilder.descriptions("excellent", "good", "ok", "poor");
		itemBuilder.itemTypes(new short[]{
				ITEM_TYPE_LEATHER,
				ITEM_TYPE_HASDATA,
				ITEM_TYPE_COMBINE
		});
		itemBuilder.imageNumber((short) 602);
		itemBuilder.behaviourType((short) 1);
		itemBuilder.combatDamage(0);
		itemBuilder.decayTime(Long.MAX_VALUE);
		itemBuilder.dimensions(10, 30, 30);
		itemBuilder.primarySkill((int) NOID);
		itemBuilder.bodySpaces(EMPTY_BYTE_PRIMITIVE_ARRAY);
		itemBuilder.modelName("model.resource.leather.dragon.");
		itemBuilder.difficulty(20.0f);
		itemBuilder.weightGrams(200);
		itemBuilder.material(Materials.MATERIAL_LEATHER);
		itemBuilder.value(200000);
		
		ItemTemplate template = itemBuilder.build();
		templateId = template.getTemplateId();
		logger.info(name+" TemplateID: "+templateId);
	}
}
