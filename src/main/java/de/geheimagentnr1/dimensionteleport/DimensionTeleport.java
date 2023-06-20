package de.geheimagentnr1.dimensionteleport;

import de.geheimagentnr1.dimensionteleport.elements.commands.ModCommandsRegisterFactory;
import de.geheimagentnr1.minecraft_forge_api.AbstractMod;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;


@Mod( DimensionTeleport.MODID )
public class DimensionTeleport extends AbstractMod {
	
	
	@NotNull
	static final String MODID = "dimensionteleport";
	
	@NotNull
	@Override
	public String getModId() {
		
		return MODID;
	}
	
	@Override
	protected void initMod() {
		
		registerEventHandler( new ModCommandsRegisterFactory() );
	}
}
