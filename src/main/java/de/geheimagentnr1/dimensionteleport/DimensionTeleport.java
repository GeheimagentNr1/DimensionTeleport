package de.geheimagentnr1.dimensionteleport;

import de.geheimagentnr1.dimensionteleport.elements.commands.dimension_teleport.DimensionTeleportCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.jetbrains.annotations.NotNull;


@Mod( DimensionTeleport.MODID )
public class DimensionTeleport {
	
	
	@NotNull
	public static final String MODID = "dimensionteleport";
	
	public DimensionTeleport() {
		
		NeoForge.EVENT_BUS.register( this );
	}
	
	@SubscribeEvent
	public void onRegisterCommands( @NotNull RegisterCommandsEvent event ) {
		
		event.getDispatcher().register( new DimensionTeleportCommand().build() );
	}
}
