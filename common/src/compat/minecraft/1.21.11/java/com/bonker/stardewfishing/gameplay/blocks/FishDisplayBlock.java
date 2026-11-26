package com.bonker.stardewfishing.gameplay.blocks;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.gameplay.ItemUtils;
import com.bonker.stardewfishing.registry.SFParticles;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/** Wall-mounted display for a single fish; the fish is stored in {@link FishDisplayBlockEntity}. */
public class FishDisplayBlock extends HorizontalFacingBlock implements BlockEntityProvider, Waterloggable {
    private static final MapCodec<FishDisplayBlock> CODEC = createCodec(FishDisplayBlock::new);
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    private static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(0, 2, 0, 1, 14, 16);
    private static final VoxelShape SHAPE_SOUTH = Block.createCuboidShape(15, 2, 0, 16, 14, 16);
    private static final VoxelShape SHAPE_WEST = Block.createCuboidShape(0, 2, 15, 16, 14, 16);
    private static final VoxelShape SHAPE_EAST = Block.createCuboidShape(0, 2, 0, 16, 14, 1);
    public static final net.minecraft.text.Text TOOLTIP =
            net.minecraft.text.Text.translatable("item.stardew_fishing.fish_display.tooltip")
                    .setStyle(StardewFishing.LIGHTER_COLOR);

    public FishDisplayBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    public static boolean canBeDisplayed(ItemStack stack) {
        return stack.isIn(StardewFishing.IN_FISH_DISPLAY);
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
                                         PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof FishDisplayBlockEntity fishDisplay) {
            ItemStack displayed = fishDisplay.getItem();

            if (displayed.isEmpty()) {
                if (canBeDisplayed(stack) && !displayed.isOf(stack.getItem())) {
                    fishDisplay.setItem(stack.copyWithCount(1));
                    if (!player.getAbilities().creativeMode) {
                        stack.decrement(1);
                    }
                    world.playSound(player, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    player.swingHand(hand);
                    return ActionResult.SUCCESS;
                }
            } else {
                if (!player.getAbilities().creativeMode) {
                    world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, displayed));
                }
                fishDisplay.setItem(ItemStack.EMPTY);
                world.playSound(player, pos, SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
                player.swingHand(hand);
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof FishDisplayBlockEntity fishDisplay) {
            ItemStack stack = fishDisplay.getItem();
            if (ItemUtils.isLegendaryFish(stack) && world.random.nextFloat() < 0.25F) {
                Direction facing = state.get(FACING);
                Vector3f spawnPos = new Vector3f(
                        pos.getX() + 0.5F - facing.getOffsetX() * 0.3F,
                        pos.getY(),
                        pos.getZ() + 0.5F - facing.getOffsetZ() * 0.3F);
                if (facing.getAxis() == Direction.Axis.X) {
                    spawnPos.z += world.random.nextFloat() - 0.5F;
                } else {
                    spawnPos.x += world.random.nextFloat() - 0.5F;
                }
                spawnPos.y += world.random.nextFloat();
                world.addParticleClient(SFParticles.SPARKLE, spawnPos.x, spawnPos.y, spawnPos.z, 0, 0, 0);
            }
        }
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case NORTH -> SHAPE_WEST;
            case SOUTH -> SHAPE_EAST;
            case WEST -> SHAPE_SOUTH;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction facing = context.getSide();
        if (context.shouldCancelInteraction() || facing.getAxis() == Direction.Axis.Y) {
            facing = context.getHorizontalPlayerFacing().getOpposite();
        }
        Fluid fluid = context.getWorld().getFluidState(context.getBlockPos()).getFluid();
        return getDefaultState().with(FACING, facing).with(WATERLOGGED, fluid == Fluids.WATER);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FishDisplayBlockEntity(pos, state);
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof FishDisplayBlockEntity fishDisplay) {
            ItemStack stack = fishDisplay.getItem();
            if (!stack.isEmpty()) {
                return stack.copyWithCount(1);
            }
        }
        return super.getPickStack(world, pos, state, includeData);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView,
                                                   BlockPos pos, Direction direction, BlockPos neighborPos,
                                                   BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }
}
