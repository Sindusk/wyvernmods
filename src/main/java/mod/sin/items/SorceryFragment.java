package mod.sin.items;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import java.io.IOException;
import java.util.logging.Logger;

public class SorceryFragment implements ItemTypes, MiscConstants {
	public static Logger logger = Logger.getLogger(SorceryFragment.class.getName());
	public static int templateId;
	
	public void createTemplate() throws IOException{
		/* ItemTemplateCreatorContinued.createItemTemplate(737, "Valrei mission item", "items", "excellent", "good", "ok", "poor",
		 * "A weird item belonging on Valrei.",
		 * new short[]{32, 59, 147, 60},
		 * 462, 1, 0, 86400, 3, 5, 50, -10, MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY,
		 * "model.valrei.", 300.0f, 100, 21, 1, false);
		 */
		String name = "sorcery fragment [1/10]";
		ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.fragment.sorcery");
		itemBuilder.name(name, "sorcery fragments", "A scrap of a tome.");
		itemBuilder.itemTypes(new short[]{
				ItemTypes.ITEM_TYPE_MAGIC,
				ItemTypes.ITEM_TYPE_FULLPRICE,
				ItemTypes.ITEM_TYPE_NOSELLBACK,
				ItemTypes.ITEM_TYPE_SERVERBOUND,
				ItemTypes.ITEM_TYPE_ARTIFACT
		});
		itemBuilder.imageNumber((short) 331);
		itemBuilder.behaviourType((short) 1);
		itemBuilder.combatDamage(0);
		itemBuilder.decayTime(Long.MAX_VALUE);
		itemBuilder.dimensions(1, 1, 1);
		itemBuilder.primarySkill((int) NOID);
		itemBuilder.bodySpaces(EMPTY_BYTE_PRIMITIVE_ARRAY);
		itemBuilder.modelName("model.artifact.scrollbind.paper."); //model.valrei.magic
		itemBuilder.difficulty(5.0f);
		itemBuilder.weightGrams(250);
		itemBuilder.material(Materials.MATERIAL_CRYSTAL);
		itemBuilder.value(5000);
		itemBuilder.isTraded(true);
		
		ItemTemplate template = itemBuilder.build();
		templateId = template.getTemplateId();
		logger.info(name+" TemplateID: "+templateId);
	}
}
