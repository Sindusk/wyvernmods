package mod.sin.items;

import java.io.IOException;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.vehicles.ModVehicleBehaviours;

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

public class MassStorageUnit implements ItemTypes {
	private static Logger logger = Logger.getLogger(MassStorageUnit.class.getName());
	public static int templateId;
	private String name = "mass storage unit";

    public void createTemplate() throws IOException{
    	ModVehicleBehaviours.init();
        ItemTemplateBuilder builder = new ItemTemplateBuilder("mod.item.mass.storage");
        builder.name(name, "mass storage units", "A massive storage unit able to be loaded with containers.");
        builder.descriptions("almost full", "somewhat occupied", "half-full", "emptyish");
        builder.itemTypes(new short[] { 
				ITEM_TYPE_WOOD,
				ITEM_TYPE_NOTAKE,
				ITEM_TYPE_REPAIRABLE,
				ITEM_TYPE_TURNABLE,
				ITEM_TYPE_DECORATION,
				ITEM_TYPE_DESTROYABLE,
				ITEM_TYPE_ONE_PER_TILE,
				ITEM_TYPE_LOCKABLE,
				ITEM_TYPE_HOLLOW,
				ITEM_TYPE_VEHICLE,
				ITEM_TYPE_IMPROVEITEM,
				ITEM_TYPE_OWNER_DESTROYABLE,
				ItemTypes.ITEM_TYPE_USES_SPECIFIED_CONTAINER_VOLUME,
				ITEM_TYPE_OWNER_TURNABLE,
				ITEM_TYPE_CART,
				ITEM_TYPE_OWNER_MOVEABLE
		});
		builder.imageNumber((short) 60);
		builder.behaviourType((short) 41);
        builder.combatDamage(0);
        builder.decayTime(9072000L);
		builder.dimensions(400, 300, 1000);
        builder.primarySkill(-10);
		builder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
        builder.modelName("model.furniture.wooden.storageunit.");
        //builder.size(3);

        builder.difficulty(80.0F);
        builder.weightGrams(300000);
        builder.material(Materials.MATERIAL_WOOD_BIRCH);
        
        ItemTemplate template = builder.build();
		templateId = template.getTemplateId();
        template.setContainerSize(300, 300, 600);
        
        MassStorageBehaviour massStorageBehaviour = new MassStorageBehaviour();
        ModVehicleBehaviours.addItemVehicle(templateId, massStorageBehaviour);
        //KingdomWagonBehaviour kingdomWagonBehaviour = new KingdomWagonBehaviour();
        //ModVehicleBehaviours.addItemVehicle(resultTemplate.getTemplateId(), kingdomWagonBehaviour);

		logger.info(name+" TemplateID: "+templateId);
    }
    
    public void initCreationEntry(){
		logger.info("initCreationEntry()");
		if(templateId > 0){
	        AdvancedCreationEntry massStorage = CreationEntryCreator.createAdvancedEntry(SkillList.CARPENTRY_FINE,
	        		ItemList.woodBeam, ItemList.woodBeam, templateId,
	                false, false, 0.0F, true, true, 0, 70.0D, CreationCategories.STORAGE);
	
	        massStorage.addRequirement(new CreationRequirement(1, ItemList.plank, 500, true));
	        massStorage.addRequirement(new CreationRequirement(2, ItemList.shaft, 200, true));
	        massStorage.addRequirement(new CreationRequirement(3, ItemList.ironBand, 50, true));
	        massStorage.addRequirement(new CreationRequirement(4, ItemList.nailsIronLarge, 100, true));
		}
    }
}
