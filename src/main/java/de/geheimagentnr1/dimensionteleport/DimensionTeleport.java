package de.geheimagentnr1.dimensionteleport;

import de.geheimagentnr1.dimensionteleport.handlers.ForgeEventHandler;
import de.geheimagentnr1.minecraft_forge_api.AbstractMod;
import net.minecraftforge.fml.common.Mod;


@Mod( DimensionTeleport.MODID )
public class DimensionTeleport extends AbstractMod {
	
	
	static final String MODID = "dimensionteleport";
	
	public DimensionTeleport() {
		
		super( MODID, 1, "" );
	}
	
	@Override
	protected void initMod() {
	
		registerForgeEventHandler( new ForgeEventHandler() );
	}
}
