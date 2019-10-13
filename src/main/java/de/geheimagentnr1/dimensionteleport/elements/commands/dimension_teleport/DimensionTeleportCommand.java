package de.geheimagentnr1.dimensionteleport.elements.commands.dimension_teleport;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.*;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;


public class DimensionTeleportCommand {
	
	public static void register( CommandDispatcher<CommandSource> dispatcher) {
		
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
		if (targets.size() == 1) {
			source.sendFeedback( new TranslationTextComponent( "commands.teleport.success.location.single",
				targets.iterator().next().getDisplayName(), destination.x, destination.y, destination.z ), true );
		} else {
			source.sendFeedback( new TranslationTextComponent( "commands.teleport.success.location.multiple",
				targets.size(), destination.x, destination.y, destination.z ), true );
		}
		return targets.size();
	}
	
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
		if (targets.size() == 1) {
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
		if (targets.size() == 1) {
			source.sendFeedback(new TranslationTextComponent("commands.teleport.success.entity.single",
				targets.iterator().next().getDisplayName(), destination.getDisplayName()), true);
		} else {
			source.sendFeedback(new TranslationTextComponent("commands.teleport.success.entity.multiple",
				targets.size(), destination.getDisplayName()), true);
		}
		return targets.size();
	}
	
	private static void teleport( Entity entity, DimensionType dimension, double x, double y,
		double z, Set<SPlayerPositionLookPacket.Flags> relativeList, float yaw, float pitch ) {
		
		entity.changeDimension( dimension );
		ServerWorld world = (ServerWorld)entity.world;
		if( entity instanceof ServerPlayerEntity ) {
			ServerPlayerEntity player = (ServerPlayerEntity)entity;
			world.getChunkProvider().func_217228_a( TicketType.POST_TELEPORT,
				new ChunkPos( new BlockPos( x, y, z ) ), 1, entity.getEntityId() );
			player.stopRiding();
			if( player.isSleeping() ) {
				player.wakeUpPlayer( true, true, false );
			}
			if( world == player.world ) {
				player.connection.setPlayerLocation( x, y, z, yaw, pitch, relativeList );
			} else {
				player.teleport( world, x, y, z, yaw, pitch );
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
	
	/*public static void register2( CommandDispatcher<CommandSource> dispatcher) {
		LiteralCommandNode<CommandSource> literalcommandnode = dispatcher.register( Commands.literal("teleport")
			.requires( p_198816_0_ -> p_198816_0_.hasPermissionLevel(2) )
			.then(Commands.argument("targets", EntityArgument.entities())
				.then(Commands.argument("location", Vec3Argument.vec3())
					.executes( p_198807_0_ -> teleportToPos(p_198807_0_.getSource(), EntityArgument.getEntities(p_198807_0_, "targets"), p_198807_0_.getSource().getWorld(), Vec3Argument.getLocation(p_198807_0_, "location"), null, null ) )
					.then(Commands.argument("rotation", RotationArgument.rotation())
						.executes( p_198811_0_ -> teleportToPos(p_198811_0_.getSource(), EntityArgument.getEntities(p_198811_0_, "targets"), p_198811_0_.getSource().getWorld(), Vec3Argument.getLocation(p_198811_0_, "location"), RotationArgument.getRotation(p_198811_0_, "rotation"), null ) ))
					.then(Commands.literal("facing")
						.then(Commands.literal("entity")
							.then(Commands.argument("facingEntity", EntityArgument.entity())
								.executes( p_198806_0_ -> teleportToPos(p_198806_0_.getSource(), EntityArgument.getEntities(p_198806_0_, "targets"), p_198806_0_.getSource().getWorld(), Vec3Argument.getLocation(p_198806_0_, "location"), (ILocationArgument)null, new Facing(EntityArgument.getEntity(p_198806_0_, "facingEntity"), EntityAnchorArgument.Type.FEET)) )
								.then(Commands.argument("facingAnchor", EntityAnchorArgument.entityAnchor())
									.executes( p_198812_0_ -> teleportToPos(p_198812_0_.getSource(), EntityArgument.getEntities(p_198812_0_, "targets"), p_198812_0_.getSource().getWorld(), Vec3Argument.getLocation(p_198812_0_, "location"), (ILocationArgument)null, new Facing(EntityArgument.getEntity(p_198812_0_, "facingEntity"), EntityAnchorArgument.getEntityAnchor(p_198812_0_, "facingAnchor"))) ))))
						.then(Commands.argument("facingLocation", Vec3Argument.vec3())
							.executes( p_198805_0_ -> teleportToPos(p_198805_0_.getSource(), EntityArgument.getEntities(p_198805_0_, "targets"), p_198805_0_.getSource().getWorld(), Vec3Argument.getLocation(p_198805_0_, "location"), (ILocationArgument)null, new Facing(Vec3Argument.getVec3(p_198805_0_, "facingLocation"))) ))))
				.then(Commands.argument("destination", EntityArgument.entity()).executes( p_198814_0_ -> teleportToEntity(p_198814_0_.getSource(), EntityArgument.getEntities(p_198814_0_, "targets"), EntityArgument.getEntity(p_198814_0_, "destination")) ))).then(Commands.argument("location", Vec3Argument.vec3()).executes( p_200560_0_ -> teleportToPos(p_200560_0_.getSource(), Collections.singleton(p_200560_0_.getSource().assertIsEntity()), p_200560_0_.getSource().getWorld(), Vec3Argument.getLocation(p_200560_0_, "location"), LocationInput.current(), (Facing)null) )).then(Commands.argument("destination", EntityArgument.entity()).executes( p_200562_0_ -> teleportToEntity(p_200562_0_.getSource(), Collections.singleton(p_200562_0_.getSource().assertIsEntity()), EntityArgument.getEntity(p_200562_0_, "destination")) )));
		dispatcher.register(Commands.literal("tp").requires( p_200556_0_ -> p_200556_0_.hasPermissionLevel(2) ).redirect(literalcommandnode));
	}
	
	private static int teleportToEntity(CommandSource source, Collection<? extends Entity> targets, Entity destination) {
		for(Entity entity : targets) {
			teleport(source, entity, (ServerWorld)destination.world, destination.posX, destination.posY, destination.posZ, EnumSet
				.noneOf( SPlayerPositionLookPacket.Flags.class), destination.rotationYaw, destination.rotationPitch,
				null );
		}
		
		if (targets.size() == 1) {
			source.sendFeedback(new TranslationTextComponent("commands.teleport.success.entity.single", targets.iterator().next().getDisplayName(), destination.getDisplayName()), true);
		} else {
			source.sendFeedback(new TranslationTextComponent("commands.teleport.success.entity.multiple", targets.size(), destination.getDisplayName()), true);
		}
		
		return targets.size();
	}
	
	private static int teleportToPos(CommandSource source, Collection<? extends Entity> targets, ServerWorld worldIn, ILocationArgument position, @Nullable
		ILocationArgument rotationIn, @Nullable DimensionTeleportCommand.Facing facing) {
		Vec3d vec3d = position.getPosition(source);
		Vec2f vec2f = rotationIn == null ? null : rotationIn.getRotation(source);
		Set<SPlayerPositionLookPacket.Flags> set = EnumSet.noneOf(SPlayerPositionLookPacket.Flags.class);
		if (position.isXRelative()) {
			set.add(SPlayerPositionLookPacket.Flags.X);
		}
		
		if (position.isYRelative()) {
			set.add(SPlayerPositionLookPacket.Flags.Y);
		}
		
		if (position.isZRelative()) {
			set.add(SPlayerPositionLookPacket.Flags.Z);
		}
		
		if (rotationIn == null) {
			set.add(SPlayerPositionLookPacket.Flags.X_ROT);
			set.add(SPlayerPositionLookPacket.Flags.Y_ROT);
		} else {
			if (rotationIn.isXRelative()) {
				set.add(SPlayerPositionLookPacket.Flags.X_ROT);
			}
			
			if (rotationIn.isYRelative()) {
				set.add(SPlayerPositionLookPacket.Flags.Y_ROT);
			}
		}
		
		for(Entity entity : targets) {
			if (rotationIn == null) {
				teleport(source, entity, worldIn, vec3d.x, vec3d.y, vec3d.z, set, entity.rotationYaw, entity.rotationPitch, facing);
			} else {
				teleport(source, entity, worldIn, vec3d.x, vec3d.y, vec3d.z, set, vec2f.y, vec2f.x, facing);
			}
		}
		
		if (targets.size() == 1) {
			source.sendFeedback(new TranslationTextComponent("commands.teleport.success.location.single", targets.iterator().next().getDisplayName(), vec3d.x, vec3d.y, vec3d.z), true);
		} else {
			source.sendFeedback(new TranslationTextComponent("commands.teleport.success.location.multiple", targets.size(), vec3d.x, vec3d.y, vec3d.z), true);
		}
		
		return targets.size();
	}
	
	private static void teleport(CommandSource source, Entity entityIn, ServerWorld worldIn, double x, double y, double z, Set<SPlayerPositionLookPacket.Flags> relativeList, float yaw, float pitch, @Nullable DimensionTeleportCommand.Facing facing) {
		if (entityIn instanceof ServerPlayerEntity ) {
			ChunkPos chunkpos = new ChunkPos(new BlockPos(x, y, z));
			worldIn.getChunkProvider().func_217228_a( TicketType.POST_TELEPORT, chunkpos, 1, entityIn.getEntityId());
			entityIn.stopRiding();
			if (((ServerPlayerEntity)entityIn).isSleeping()) {
				((ServerPlayerEntity)entityIn).wakeUpPlayer(true, true, false);
			}
			
			if (worldIn == entityIn.world) {
				((ServerPlayerEntity)entityIn).connection.setPlayerLocation(x, y, z, yaw, pitch, relativeList);
			} else {
				((ServerPlayerEntity)entityIn).teleport(worldIn, x, y, z, yaw, pitch);
			}
			
			entityIn.setRotationYawHead(yaw);
		} else {
			float f1 = MathHelper.wrapDegrees(yaw);
			float f = MathHelper.wrapDegrees(pitch);
			f = MathHelper.clamp(f, -90.0F, 90.0F);
			if (worldIn == entityIn.world) {
				entityIn.setLocationAndAngles(x, y, z, f1, f);
				entityIn.setRotationYawHead(f1);
			} else {
				entityIn.detach();
				entityIn.dimension = worldIn.dimension.getType();
				Entity entity = entityIn;
				entityIn = entityIn.getType().create(worldIn);
				if (entityIn == null) {
					return;
				}
				
				entityIn.copyDataFromOld(entity);
				entityIn.setLocationAndAngles(x, y, z, f1, f);
				entityIn.setRotationYawHead(f1);
				worldIn.func_217460_e(entityIn);
			}
		}
		
		if (facing != null) {
			facing.updateLook(source, entityIn);
		}
		
		if (!(entityIn instanceof LivingEntity ) || !((LivingEntity)entityIn).isElytraFlying()) {
			entityIn.setMotion(entityIn.getMotion().mul(1.0D, 0.0D, 1.0D));
			entityIn.onGround = true;
		}
		
	}
	
	static class Facing {
		private final Vec3d position;
		private final Entity entity;
		private final EntityAnchorArgument.Type anchor;
		
		public Facing(Entity entityIn, EntityAnchorArgument.Type anchorIn) {
			
			entity = entityIn;
			anchor = anchorIn;
			position = anchorIn.apply(entityIn);
		}
		
		public Facing(Vec3d positionIn) {
			
			entity = null;
			position = positionIn;
			anchor = null;
		}
		
		public void updateLook(CommandSource source, Entity entityIn) {
			if ( entity != null) {
				if (entityIn instanceof ServerPlayerEntity) {
					((ServerPlayerEntity)entityIn).lookAt(source.getEntityAnchorType(), entity, anchor );
				} else {
					entityIn.lookAt(source.getEntityAnchorType(), position );
				}
			} else {
				entityIn.lookAt(source.getEntityAnchorType(), position );
			}
			
		}
	}*/
}
