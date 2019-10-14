package de.geheimagentnr1.dimensionteleport.elements.commands.dimension_teleport;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ILocationArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;


public class DimensionTeleportCommand {
	
	
	@SuppressWarnings( "SameReturnValue" )
	public static void register( CommandDispatcher<CommandSource> dispatcher ) {
		
		LiteralArgumentBuilder<CommandSource> tpd = Commands.literal( "tpd" ).requires(
			commandSource -> commandSource.hasPermissionLevel( 2 )
		);
		tpd.executes( context -> {
			context.getSource().sendFeedback( new StringTextComponent( "/tpd <targets> <destination> [<dimension>]" ),
				true );
			return 1;
		} );
		tpd.then( Commands.argument( "targets", EntityArgument.entities() )
			.then( Commands.argument( "location", Vec3Argument.vec3() )
				.executes(
					DimensionTeleportCommand::teleportToPos
				).then( Commands.argument( "dimension", DimensionArgument.getDimension() ).executes(
					DimensionTeleportCommand::teleportToPosWithDim
				) ) ).then( Commands.argument( "destination", EntityArgument.entity() ).executes(
				DimensionTeleportCommand::teleportToEntity
			) ) );
		dispatcher.register( tpd );
	}
	
	@SuppressWarnings( { "DuplicatedCode", "MismatchedQueryAndUpdateOfCollection" } )
	private static int teleportToPos( CommandContext<CommandSource> context ) throws CommandSyntaxException {
		
		CommandSource source = context.getSource();
		Collection<? extends Entity> targets = EntityArgument.getEntities( context, "targets" );
		ILocationArgument destinationPos = Vec3Argument.getLocation( context, "location" );
		Vec3d destination = destinationPos.getPosition( source );
		Set<SPlayerPositionLookPacket.Flags> relativeList = EnumSet.noneOf( SPlayerPositionLookPacket.Flags.class );
		if( destinationPos.isXRelative() ) {
			relativeList.add( SPlayerPositionLookPacket.Flags.X );
		}
		if( destinationPos.isYRelative() ) {
			relativeList.add( SPlayerPositionLookPacket.Flags.Y );
		}
		if( destinationPos.isZRelative() ) {
			relativeList.add( SPlayerPositionLookPacket.Flags.Z );
		}
		for( Entity target : targets ) {
			teleport( target, target.dimension, destination.x, destination.y, destination.z,
				EnumSet.noneOf( SPlayerPositionLookPacket.Flags.class ), target.rotationYaw,
				target.rotationPitch );
		}
		if( targets.size() == 1 ) {
			source.sendFeedback( new TranslationTextComponent( "commands.teleport.success.location.single",
				targets.iterator().next().getDisplayName(), destination.x, destination.y, destination.z ), true );
		} else {
			source.sendFeedback( new TranslationTextComponent( "commands.teleport.success.location.multiple",
				targets.size(), destination.x, destination.y, destination.z ), true );
		}
		return targets.size();
	}
	
	@SuppressWarnings( { "DuplicatedCode", "MismatchedQueryAndUpdateOfCollection" } )
	private static int teleportToPosWithDim( CommandContext<CommandSource> context ) throws CommandSyntaxException {
		
		CommandSource source = context.getSource();
		Collection<? extends Entity> targets = EntityArgument.getEntities( context, "targets" );
		ILocationArgument destinationPos = Vec3Argument.getLocation( context, "location" );
		DimensionType destination_dimension = DimensionArgument.func_212592_a( context, "dimension" );
		Vec3d destination = destinationPos.getPosition( source );
		Set<SPlayerPositionLookPacket.Flags> relativeList = EnumSet.noneOf( SPlayerPositionLookPacket.Flags.class );
		if( destinationPos.isXRelative() ) {
			relativeList.add( SPlayerPositionLookPacket.Flags.X );
		}
		if( destinationPos.isYRelative() ) {
			relativeList.add( SPlayerPositionLookPacket.Flags.Y );
		}
		if( destinationPos.isZRelative() ) {
			relativeList.add( SPlayerPositionLookPacket.Flags.Z );
		}
		for( Entity target : targets ) {
			teleport( target, destination_dimension, destination.x, destination.y, destination.z,
				EnumSet.noneOf( SPlayerPositionLookPacket.Flags.class ), target.rotationYaw,
				target.rotationPitch );
		}
		if( targets.size() == 1 ) {
			source.sendFeedback( new TranslationTextComponent( "commands.teleport.success.location.single",
				targets.iterator().next().getDisplayName(), destination.x, destination.y, destination.z ), true );
		} else {
			source.sendFeedback( new TranslationTextComponent( "commands.teleport.success.location.multiple",
				targets.size(), destination.x, destination.y, destination.z ), true );
		}
		return targets.size();
	}
	
	private static int teleportToEntity( CommandContext<CommandSource> context ) throws CommandSyntaxException {
		
		CommandSource source = context.getSource();
		Collection<? extends Entity> targets = EntityArgument.getEntities( context, "targets" );
		Entity destination = EntityArgument.getEntity( context, "destination" );
		
		for( Entity target : targets ) {
			teleport( target, destination.dimension, destination.posX, destination.posY, destination.posZ,
				EnumSet.noneOf( SPlayerPositionLookPacket.Flags.class ), destination.rotationYaw,
				destination.rotationPitch );
		}
		if( targets.size() == 1 ) {
			source.sendFeedback( new TranslationTextComponent( "commands.teleport.success.entity.single",
				targets.iterator().next().getDisplayName(), destination.getDisplayName() ), true );
		} else {
			source.sendFeedback( new TranslationTextComponent( "commands.teleport.success.entity.multiple",
				targets.size(), destination.getDisplayName() ), true );
		}
		return targets.size();
	}
	
	private static void teleport( Entity entity, DimensionType dimension, double x, double y,
		double z, Set<SPlayerPositionLookPacket.Flags> relativeList, float yaw, float pitch ) {
		
		ServerWorld destination_world = Objects.requireNonNull( entity.getServer() ).getWorld( dimension );
		if( entity instanceof ServerPlayerEntity ) {
			ServerPlayerEntity player = (ServerPlayerEntity)entity;
			destination_world.getChunkProvider().func_217228_a( TicketType.POST_TELEPORT,
				new ChunkPos( new BlockPos( x, y, z ) ), 1, entity.getEntityId() );
			player.stopRiding();
			if( player.isSleeping() ) {
				player.wakeUpPlayer( true, true, false );
			}
			if( destination_world == player.world ) {
				player.connection.setPlayerLocation( x, y, z, yaw, pitch, relativeList );
			} else {
				player.teleport( destination_world, x, y, z, yaw, pitch );
			}
			player.setRotationYawHead( yaw );
		} else {
			yaw = MathHelper.wrapDegrees( yaw );
			pitch = MathHelper.clamp( MathHelper.wrapDegrees( pitch ), -90, 90 );
			entity.setLocationAndAngles( x, y, z, yaw, pitch );
			entity.setRotationYawHead( yaw );
		}
		if( !( entity instanceof LivingEntity ) || !( (LivingEntity)entity ).isElytraFlying() ) {
			entity.setMotion( entity.getMotion().mul( 1.0D, 0.0D, 1.0D ) );
			entity.onGround = true;
		}
	}
}
