package de.geheimagentnr1.dimensionteleport.elements.commands.dimension_teleport;


import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;


//package-private
@FunctionalInterface
interface TargetListener {
	
	
	//public
	@NotNull
	ServerLevel getTargetDimension( @NotNull Entity target );
}
