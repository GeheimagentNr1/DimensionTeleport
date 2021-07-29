package de.geheimagentnr1.dimensionteleport.elements.commands.dimension_teleport;


import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;


//package-private
@FunctionalInterface
interface TargetListener {
	
	
	//public
	ServerLevel getTargetDimension( Entity target );
}
