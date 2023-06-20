package de.geheimagentnr1.dimensionteleport.elements.commands.dimension_teleport;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.geheimagentnr1.minecraft_forge_api.elements.commands.CommandInterface;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;


public class DimensionTeleportCommand implements CommandInterface {
	
	
	@NotNull
	private static final SimpleCommandExceptionType INVALID_POSITION =
		new SimpleCommandExceptionType( Component.translatable( "commands.teleport.invalidPosition" ) );
	
	@NotNull
	@Override
	public LiteralArgumentBuilder<CommandSourceStack> build() {
		
		LiteralArgumentBuilder<CommandSourceStack> tpd = Commands.literal( "tpd" )
			.requires( source -> source.hasPermission( 2 ) );
		tpd.then( Commands.argument( "targets", EntityArgument.entities() )
			.then( Commands.argument( "location", BlockPosArgument.blockPos() )
				.executes( this::teleportToPos )
				.then( Commands.argument( "dimension", DimensionArgument.dimension() )
					.executes( this::teleportToPosWithDim ) ) )
			.then( Commands.argument( "destination", EntityArgument.entity() )
				.executes( this::teleportToEntity ) ) );
		return tpd;
	}
	
	private int teleportToPos( @NotNull CommandContext<CommandSourceStack> context ) throws CommandSyntaxException {
		
		return teleportToPos( context, target -> (ServerLevel)target.getCommandSenderWorld() );
	}
	
	private int teleportToPosWithDim( @NotNull CommandContext<CommandSourceStack> context )
		throws CommandSyntaxException {
		
		ServerLevel destination_level = DimensionArgument.getDimension( context, "dimension" );
		return teleportToPos( context, target -> destination_level );
	}
	
	private int teleportToPos(
		@NotNull CommandContext<CommandSourceStack> context,
		@NotNull TargetListener targetListener )
		throws CommandSyntaxException {
		
		CommandSourceStack source = context.getSource();
		Collection<? extends Entity> targets = EntityArgument.getEntities( context, "targets" );
		BlockPos destinationPos = BlockPosArgument.getSpawnablePos( context, "location" );
		Vec3 destination = new Vec3( destinationPos.getX() + 0.5, destinationPos.getY(), destinationPos.getZ() + 0.5 );
		for( Entity target : targets ) {
			teleport(
				target,
				targetListener.getTargetDimension( target ),
				destination.x(),
				destination.y(),
				destination.z(),
				target.getYRot(),
				target.getXRot()
			);
		}
		if( targets.size() == 1 ) {
			source.sendSuccess(
				() -> Component.translatable(
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
				() -> Component.translatable(
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
	
	private int teleportToEntity( @NotNull CommandContext<CommandSourceStack> context ) throws CommandSyntaxException {
		
		CommandSourceStack source = context.getSource();
		Collection<? extends Entity> targets = EntityArgument.getEntities( context, "targets" );
		Entity destination = EntityArgument.getEntity( context, "destination" );
		BlockPos destinationPos = destination.blockPosition();
		
		for( Entity target : targets ) {
			teleport(
				target,
				(ServerLevel)destination.getCommandSenderWorld(),
				destinationPos.getX(),
				destinationPos.getY(),
				destinationPos.getZ(),
				destination.getYRot(),
				destination.getXRot()
			);
		}
		if( targets.size() == 1 ) {
			source.sendSuccess(
				() -> Component.translatable(
					"commands.teleport.success.entity.single",
					targets.iterator().next().getDisplayName(),
					destination.getDisplayName()
				),
				true
			);
		} else {
			source.sendSuccess(
				() -> Component.translatable(
					"commands.teleport.success.entity.multiple",
					targets.size(),
					destination.getDisplayName()
				),
				true
			);
		}
		return targets.size();
	}
	
	private void teleport(
		@NotNull Entity entity,
		@NotNull ServerLevel destination_level,
		double x,
		double y,
		double z,
		float yaw,
		float pitch ) throws CommandSyntaxException {
		
		EntityTeleportEvent.TeleportCommand event = ForgeEventFactory.onEntityTeleportCommand( entity, x, y, z );
		if( event.isCanceled() ) {
			return;
		}
		x = event.getTargetX();
		y = event.getTargetY();
		z = event.getTargetZ();
		BlockPos blockpos = BlockPos.containing( x, y, z );
		if( Level.isInSpawnableBounds( blockpos ) ) {
			yaw = Mth.wrapDegrees( yaw );
			pitch = Mth.wrapDegrees( pitch );
			if( entity instanceof ServerPlayer player ) {
				ChunkPos chunkpos = new ChunkPos( blockpos );
				destination_level.getChunkSource().addRegionTicket(
					TicketType.POST_TELEPORT,
					chunkpos,
					1,
					player.getId()
				);
				player.stopRiding();
				if( player.isSleeping() ) {
					player.stopSleepInBed( true, true );
				}
				if( destination_level == player.level() ) {
					player.connection.teleport( x, y, z, yaw, pitch );
				} else {
					player.teleportTo( destination_level, x, y, z, yaw, pitch );
				}
				player.setYHeadRot( yaw );
				player.onUpdateAbilities();
			} else {
				pitch = Mth.clamp( pitch, -90.0F, 90.0F );
				if( destination_level == entity.level() ) {
					entity.moveTo( x, y, z, yaw, pitch );
					entity.setYHeadRot( yaw );
				} else {
					entity.unRide();
					Entity newEntity = entity.getType().create( destination_level );
					if( newEntity == null ) {
						return;
					}
					
					newEntity.restoreFrom( entity );
					newEntity.moveTo( x, y, z, yaw, pitch );
					newEntity.setYHeadRot( yaw );
					newEntity.setRemoved( Entity.RemovalReason.CHANGED_DIMENSION );
					destination_level.addDuringTeleport( entity );
				}
			}
			if( !( entity instanceof LivingEntity ) || !( (LivingEntity)entity ).isFallFlying() ) {
				entity.setDeltaMovement( entity.getDeltaMovement().multiply( 1.0D, 0.0D, 1.0D ) );
				entity.setOnGround( true );
			}
			if( entity instanceof PathfinderMob ) {
				( (PathfinderMob)entity ).getNavigation().stop();
			}
		} else {
			throw INVALID_POSITION.create();
		}
	}
}
