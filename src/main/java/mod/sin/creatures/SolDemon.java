package mod.sin.creatures;

import com.wurmonline.server.Servers;
import org.gotti.wurmunlimited.modsupport.CreatureTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.creatures.EncounterBuilder;
import org.gotti.wurmunlimited.modsupport.creatures.ModCreature;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;

public class SolDemon implements ModCreature {

	private int templateId;
	
	@Override
	public CreatureTemplateBuilder createCreateTemplateBuilder() {
		templateId = CreatureTemplateIds.DEMON_SOL_CID;
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

		if(Servers.localServer.PVPSERVER) {
			new EncounterBuilder(Tiles.Tile.TILE_SAND.id)
					.addCreatures(templateId, 2)
					.build(1);
		}

		new EncounterBuilder(Tiles.Tile.TILE_MYCELIUM.id)
			.addCreatures(templateId, 2)
			.build(3);
	}
}
