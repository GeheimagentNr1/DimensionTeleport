package de.geheimagentnr1.dimensionteleport.elements.commands.dimension_teleport;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;

import java.util.Collection;


public class DimensionTeleportCommand {
	
	
	public static void register( CommandDispatcher<CommandSource> dispatcher ) {
		
		LiteralArgumentBuilder<CommandSource> tpd = Commands.literal( "tpd" )
			.requires( commandSource -> commandSource.hasPermission( 2 ) );
		tpd.then( Commands.argument( "targets", EntityArgument.entities() )
			.then( Commands.argument( "location", BlockPosArgument.blockPos() )
				.executes( DimensionTeleportCommand::teleportToPos )
				.then( Commands.argument( "dimension", DimensionArgument.dimension() )
					.executes( DimensionTeleportCommand::teleportToPosWithDim ) ) )
			.then( Commands.argument( "destination", EntityArgument.entity() )
				.executes( DimensionTeleportCommand::teleportToEntity ) ) );
		dispatcher.register( tpd );
	}
	
	private static int teleportToPos( CommandContext<CommandSource> context ) throws CommandSyntaxException {
		
		return teleportToPos( context, target -> (ServerWorld)target.getCommandSenderWorld() );
	}
	
	private static int teleportToPosWithDim( CommandContext<CommandSource> context ) throws CommandSyntaxException {
		
		ServerWorld destination_world = DimensionArgument.getDimension( context, "dimension" );
		return teleportToPos( context, target -> destination_world );
	}
	
	private static int teleportToPos( CommandContext<CommandSource> context, TargetListener targetListener )
		throws CommandSyntaxException {
		
		CommandSource source = context.getSource();
		Collection<? extends Entity> targets = EntityArgument.getEntities( context, "targets" );
		BlockPos destinationPos = BlockPosArgument.getOrLoadBlockPos( context, "location" );
		Vector3d destination = new Vector3d(
			destinationPos.getX() + 0.5,
			destinationPos.getY(),
			destinationPos.getZ() + 0.5
		);
		for( Entity target : targets ) {
			teleport(
				target,
				targetListener.getTargetDimension( target ),
				destination.x(),
				destination.y(),
				destination.z(),
				target.yRot,
				target.xRot
			);
		}
		if( targets.size() == 1 ) {
			source.sendSuccess(
				new TranslationTextComponent(
					"commands.teleport.success.location.single",
					targets.iterator().next().getDisplayName(),
					destination.x(),
					destination.y(),
					destination.z()
				),
				true
			);
		} else {
			source.sendSuccess(
				new TranslationTextComponent(
					"commands.teleport.success.location.multiple",
					targets.size(),
					destination.x(),
					destination.y(),
					destination.z()
				),
				true
			);
		}
		return targets.size();
	}
	
	private static int teleportToEntity( CommandContext<CommandSource> context ) throws CommandSyntaxException {
		
		CommandSource source = context.getSource();
		Collection<? extends Entity> targets = EntityArgument.getEntities( context, "targets" );
		Entity destination = EntityArgument.getEntity( context, "destination" );
		BlockPos destinationPos = destination.blockPosition();
		
		for( Entity target : targets ) {
			teleport(
				target,
				(ServerWorld)destination.getCommandSenderWorld(),
				destinationPos.getX(),
				destinationPos.getY(),
				destinationPos.getZ(),
				destination.yRot,
				destination.xRot
			);
		}
		if( targets.size() == 1 ) {
			source.sendSuccess(
				new TranslationTextComponent(
					"commands.teleport.success.entity.single",
					targets.iterator().next().getDisplayName(),
					destination.getDisplayName()
				),
				true
			);
		} else {
			source.sendSuccess(
				new TranslationTextComponent(
					"commands.teleport.success.entity.multiple",
					targets.size(),
					destination.getDisplayName()
				),
				true
			);
		}
		return targets.size();
	}
	
	private static void teleport(
		Entity entity,
		ServerWorld destination_world,
		double x,
		double y,
		double z,
		float yaw,
		float pitch ) {
		
		if( entity instanceof ServerPlayerEntity ) {
			ServerPlayerEntity player = (ServerPlayerEntity)entity;
			destination_world.getChunkSource().addRegionTicket(
				TicketType.POST_TELEPORT,
				new ChunkPos( new BlockPos( x, y, z ) ),
				1,
				entity.getId()
			);
			player.stopRiding();
			if( player.isSleeping() ) {
				player.stopSleepInBed( true, true );
			}
			player.teleportTo( destination_world, x, y, z, yaw, pitch );
			player.setYHeadRot( yaw );
			player.onUpdateAbilities();
		} else {
			yaw = MathHelper.wrapDegrees( yaw );
			pitch = MathHelper.clamp( MathHelper.wrapDegrees( pitch ), -90, 90 );
			entity.moveTo( x, y, z, yaw, pitch );
			entity.setYHeadRot( yaw );
		}
		if( !( entity instanceof LivingEntity ) || !( (LivingEntity)entity ).isFallFlying() ) {
			entity.setDeltaMovement( entity.getDeltaMovement().multiply( 1.0D, 0.0D, 1.0D ) );
			entity.setOnGround( true );
		}
		if( entity instanceof CreatureEntity ) {
			( (CreatureEntity)entity ).getNavigation().stop();
		}
	}
}
