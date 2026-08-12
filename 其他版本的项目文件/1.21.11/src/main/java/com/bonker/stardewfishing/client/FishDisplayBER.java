package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.gameplay.blocks.FishDisplayBlockEntity;
import com.bonker.stardewfishing.gameplay.ItemUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class FishDisplayBER implements BlockEntityRenderer<@NotNull FishDisplayBlockEntity, FishDisplayBER.@NotNull FishDisplayRenderState> {
    private final ItemModelManager itemModelResolver;

    public FishDisplayBER(BlockEntityRendererFactory.Context context) {
        this.itemModelResolver = context.itemModelManager();
    }

    public static class FishDisplayRenderState extends BlockEntityRenderState {
        public ItemRenderState displayItem;
        public ItemStack item;
        public Direction facing;
        public boolean renderLabel;
        public boolean offsetRender;
    }

    @Override
    public FishDisplayRenderState createRenderState() {
        return new FishDisplayRenderState();
    }

    @Override
    public void updateRenderState(@NotNull FishDisplayBlockEntity blockEntity, @NotNull FishDisplayRenderState renderState, float partialTick, Vec3d cameraPosition, ModelCommandRenderer.@Nullable CrumblingOverlayCommand breakProgress) {
        BlockEntityRenderer.super.updateRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);

        ItemStack stack = blockEntity.getItem();
        if (!stack.isEmpty() && blockEntity.getWorld() != null) {
            BlockPos pos = blockEntity.getPos();
            BlockState state = blockEntity.getCachedState();
            if (!state.contains(HorizontalFacingBlock.FACING)) {
                return;
            }

            renderState.facing = state.get(HorizontalFacingBlock.FACING);
            renderState.displayItem = new ItemRenderState();
            renderState.item = stack;
            HitResult hitResult = MinecraftClient.getInstance().crosshairTarget;
            renderState.renderLabel = hitResult != null && hitResult.getType() == HitResult.Type.BLOCK && ((BlockHitResult) hitResult).getBlockPos().equals(pos);
            int sum = pos.getX() + pos.getY() + pos.getZ();
            renderState.offsetRender = sum % 2 == 0;

            itemModelResolver.clearAndUpdate(renderState.displayItem, stack, ItemDisplayContext.FIXED, blockEntity.getWorld(), null, 0);
        }
    }

    @Override
    public void render(FishDisplayRenderState renderState, @NotNull MatrixStack poseStack, @NotNull OrderedRenderCommandQueue nodeCollector, @NotNull CameraRenderState cameraRenderState) {
        if (renderState.displayItem == null) {
            return;
        }

        ItemStack stack = renderState.item;
        if (stack.isEmpty() || renderState.facing == null) {
            return;
        }

        boolean legendary = ItemUtils.isLegendaryFish(stack);

        poseStack.push();

        double offset = -0.5 + 5/64.0;
        poseStack.translate(0.5 + renderState.facing.getOffsetX() * offset, 0.5, 0.5 + renderState.facing.getOffsetZ() * offset);

        float rot = switch (renderState.facing) {
            case SOUTH -> 180;
            case WEST -> 90;
            case EAST -> -90;
            default -> 0;
        };
        if (rot != 0) {
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rot));
        }

        if (renderState.renderLabel) {
            MutableText component = Text.empty().append(stack.getName());
            if (stack.contains(DataComponentTypes.CUSTOM_NAME)) {
                component.formatted(Formatting.ITALIC);
            }
            if (legendary) {
                component.fillStyle(StardewFishing.LEGENDARY);
            }

            poseStack.push();
            poseStack.translate(0, -0.25F, -0.25F);
            poseStack.scale(-0.025F, 0.025F, 0.025F);
            poseStack.multiply(RotationAxis.POSITIVE_X.rotation(MathHelper.PI));

            nodeCollector.submitText(poseStack, -MinecraftClient.getInstance().textRenderer.getWidth(component) / 2F, 0, component.asOrderedText(), true, TextRenderer.TextLayerType.NORMAL, renderState.lightmapCoordinates, 0xFFFFFFFF, 0x20FFFFFF, 0);

            poseStack.pop();
        }

        float scale;
        if (legendary) {
            scale = 1.19F;
        } else {
            scale = 0.8F;
        }
        poseStack.scale(scale, scale, scale);

        if (renderState.offsetRender) {
            poseStack.translate(0, 0, -1/128F);
        }

        renderState.displayItem.render(poseStack, nodeCollector, renderState.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0);

        poseStack.pop();
    }
}
