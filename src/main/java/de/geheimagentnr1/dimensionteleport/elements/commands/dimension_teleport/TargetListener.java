package de.geheimagentnr1.dimensionteleport.elements.commands.dimension_teleport;

import net.minecraft.entity.Entity;
import net.minecraft.world.server.ServerWorld;


//package-private
@FunctionalInterface
interface TargetListener {
	
	
	//public
	ServerWorld getTargetDimension( Entity target );
}
