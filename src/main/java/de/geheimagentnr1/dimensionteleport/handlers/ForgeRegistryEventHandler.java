package de.geheimagentnr1.dimensionteleport.handlers;

import de.geheimagentnr1.dimensionteleport.elements.commands.dimension_teleport.DimensionTeleportCommand;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;


@SuppressWarnings( "unused" )
@Mod.EventBusSubscriber( bus = Mod.EventBusSubscriber.Bus.FORGE )
public class ForgeRegistryEventHandler {
	
	
	@SubscribeEvent
	public static void handlerServerStartEvent( FMLServerStartingEvent event ) {
		
		DimensionTeleportCommand.register( event.getCommandDispatcher() );
	}
}
