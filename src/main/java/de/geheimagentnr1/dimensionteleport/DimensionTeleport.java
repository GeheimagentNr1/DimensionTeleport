package de.geheimagentnr1.dimensionteleport;

import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;


@SuppressWarnings( "UtilityClassWithPublicConstructor" )
@Mod( DimensionTeleport.MODID )
public class DimensionTeleport {
	
	
	public static final String MODID = "dimensionteleport";
	
	public DimensionTeleport() {
		
		ModLoadingContext.get().registerExtensionPoint(
			IExtensionPoint.DisplayTest.class,
			() -> new IExtensionPoint.DisplayTest(
				() -> NetworkConstants.IGNORESERVERONLY,
				( remote, isServer ) -> true
			)
		);
	}
}
