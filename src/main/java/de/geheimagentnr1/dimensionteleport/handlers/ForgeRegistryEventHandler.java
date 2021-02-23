package de.geheimagentnr1.dimensionteleport.handlers;

import de.geheimagentnr1.dimensionteleport.elements.commands.dimension_teleport.DimensionTeleportCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber( bus = Mod.EventBusSubscriber.Bus.FORGE )
public class ForgeRegistryEventHandler {
	
	
	@SubscribeEvent
	public static void handlerRegisterCommandsEvent( RegisterCommandsEvent event ) {
		
		DimensionTeleportCommand.register( event.getDispatcher() );
	}
}
