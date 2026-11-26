package com.bonker.stardewfishing.common.block;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.item.FishingItemSupport;
import com.bonker.stardewfishing.registry.SFParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

@SuppressWarnings("deprecation") // Mojang marks the 1.20.1 BlockBehaviour override surface as deprecated.
public class FishDisplayBlock extends HorizontalDirectionalBlock implements EntityBlock, SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape SHAPE_NORTH = box(0, 2, 0, 1, 14, 16);
    private static final VoxelShape SHAPE_SOUTH = box(15, 2, 0, 16, 14, 16);
    private static final VoxelShape SHAPE_WEST = box(0, 2, 15, 16, 14, 16);
    private static final VoxelShape SHAPE_EAST = box(0, 2, 0, 16, 14, 1);
    private static final Component TOOLTIP = Component.translatable("item.stardew_fishing.fish_display.tooltip")
            .withStyle(StardewFishing.LIGHTER_COLOR);

    public FishDisplayBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
    }

    public static boolean canBeDisplayed(ItemStack stack) {
        return stack.is(StardewFishing.IN_FISH_DISPLAY);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof FishDisplayBlockEntity display)) return InteractionResult.PASS;

        ItemStack displayed = display.getItem();
        if (displayed.isEmpty()) {
            if (!canBeDisplayed(held)) return InteractionResult.PASS;
            if (!level.isClientSide) {
                display.setItem(held.copyWithCount(1));
                if (!player.getAbilities().instabuild || held.hasTag()) held.shrink(1);
            }
            level.playSound(player, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else {
            if (!level.isClientSide) {
                if (!player.getAbilities().instabuild || displayed.hasTag()) {
                    level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, displayed.copy()));
                }
                display.setItem(ItemStack.EMPTY);
            }
            level.playSound(player, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        player.swing(hand);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof FishDisplayBlockEntity display)
                || !FishingItemSupport.isLegendaryFish(display.getItem()) || random.nextFloat() >= 0.25F) return;

        Direction facing = state.getValue(FACING);
        Vector3f spawn = new Vector3f(pos.getX() + 0.5F - facing.getStepX() * 0.3F,
                pos.getY(), pos.getZ() + 0.5F - facing.getStepZ() * 0.3F);
        if (facing.getAxis() == Direction.Axis.X) spawn.z += random.nextFloat() - 0.5F;
        else spawn.x += random.nextFloat() - 0.5F;
        spawn.y += random.nextFloat();
        level.addParticle(SFParticles.SPARKLE, spawn.x, spawn.y, spawn.z, 0, 0, 0);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(TOOLTIP);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof FishDisplayBlockEntity display) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), display.getItem());
            }
            super.onRemove(state, level, pos, newState, moving);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_WEST;
            case SOUTH -> SHAPE_EAST;
            case WEST -> SHAPE_SOUTH;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        if (context.isSecondaryUseActive() || facing.getAxis() == Direction.Axis.Y) {
            facing = context.getHorizontalDirection().getOpposite();
        }
        Fluid fluid = context.getLevel().getFluidState(context.getClickedPos()).getType();
        return defaultBlockState().setValue(FACING, facing).setValue(WATERLOGGED, fluid == Fluids.WATER);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FishDisplayBlockEntity(pos, state);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FishDisplayBlockEntity display && !display.getItem().isEmpty()) {
            return display.getItem().copyWithCount(1);
        }
        return super.getCloneItemStack(level, pos, state);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level,
                                  BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }
}
