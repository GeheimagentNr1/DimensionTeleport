package de.geheimagentnr1.dimensionteleport.handlers;

import de.geheimagentnr1.dimensionteleport.elements.commands.dimension_teleport.DimensionTeleportCommand;
import de.geheimagentnr1.minecraft_forge_api.events.ForgeEventHandlerInterface;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class ForgeEventHandler implements ForgeEventHandlerInterface {
	
	
	@SubscribeEvent
	@Override
	public void handlerRegisterCommandsEvent( RegisterCommandsEvent event ) {
		
		DimensionTeleportCommand.register( event.getDispatcher() );
	}
}
