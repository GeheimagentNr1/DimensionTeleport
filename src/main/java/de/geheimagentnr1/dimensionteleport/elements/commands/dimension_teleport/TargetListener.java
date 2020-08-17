package de.geheimagentnr1.dimensionteleport.elements.commands.dimension_teleport;

import net.minecraft.entity.Entity;
import net.minecraft.world.dimension.DimensionType;


//package-private
@FunctionalInterface
interface TargetListener {
	
	
	//public
	DimensionType getTargetDimension( Entity target );
}
