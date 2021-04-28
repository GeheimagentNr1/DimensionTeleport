package de.geheimagentnr1.dimensionteleport;

import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;


@SuppressWarnings( "UtilityClassWithPublicConstructor" )
@Mod( DimensionTeleport.MODID )
public class DimensionTeleport {
	
	
	public static final String MODID = "dimensionteleport";
	
	public DimensionTeleport() {
		
		ModLoadingContext.get().registerExtensionPoint(
			ExtensionPoint.DISPLAYTEST,
			() -> Pair.of( () -> FMLNetworkConstants.IGNORESERVERONLY, ( remote, isServer ) -> true )
		);
	}
}
