package mod.sin.creatures;

import org.gotti.wurmunlimited.modsupport.CreatureTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.creatures.EncounterBuilder;
import org.gotti.wurmunlimited.modsupport.creatures.ModCreature;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;

public class LavaFiend implements ModCreature {

	private int templateId;
	
	@Override
	public CreatureTemplateBuilder createCreateTemplateBuilder() {
		templateId = CreatureTemplateIds.LAVA_CREATURE_CID;
		return new CreatureTemplateBuilder(templateId) {
			@Override
			public CreatureTemplate build() {
				try {
					
					return CreatureTemplateFactory.getInstance().getTemplate(templateId);
				} catch (NoSuchCreatureTemplateException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}
	
	@Override
	public void addEncounters() {
		if (templateId == 0)
			return;

		new EncounterBuilder(Tiles.Tile.TILE_SAND.id)
			.addCreatures(templateId, 1)
			.build(1);
		
	}
}
