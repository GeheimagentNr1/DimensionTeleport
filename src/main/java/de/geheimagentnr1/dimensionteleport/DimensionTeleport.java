package de.geheimagentnr1.dimensionteleport;

import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmllegacy.network.FMLNetworkConstants;


@SuppressWarnings( "UtilityClassWithPublicConstructor" )
@Mod( DimensionTeleport.MODID )
public class DimensionTeleport {
	
	
	public static final String MODID = "dimensionteleport";
	
	public DimensionTeleport() {
		
		ModLoadingContext.get().registerExtensionPoint(
			IExtensionPoint.DisplayTest.class,
			() -> new IExtensionPoint.DisplayTest(
				() -> FMLNetworkConstants.IGNORESERVERONLY,
				( remote, isServer ) -> true
			)
		);
	}
}
