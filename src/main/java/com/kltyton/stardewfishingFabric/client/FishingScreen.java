package com.kltyton.stardewfishingFabric.client;

import com.kltyton.stardewfishingFabric.StardewfishingFabric;
import com.kltyton.stardewfishingFabric.client.util.Animation;
import com.kltyton.stardewfishingFabric.client.util.RenderUtil;
import com.kltyton.stardewfishingFabric.client.util.Shake;
import com.kltyton.stardewfishingFabric.common.FishBehavior;
import com.kltyton.stardewfishingFabric.common.networking.C2SCompleteMinigamePacket;
import com.kltyton.stardewfishingFabric.common.networking.SFNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public class FishingScreen extends Screen {
    private static final Text TITLE = Text.literal("钓鱼小游戏");
    private static final Identifier TEXTURE = Identifier.of(StardewfishingFabric.MODID, "textures/minigame.png");

    // GUI尺寸常量
    private static final int GUI_WIDTH = 38;
    private static final int GUI_HEIGHT = 152;
    private static final int HIT_WIDTH = 73;
    private static final int HIT_HEIGHT = 29;
    private static final int PERFECT_WIDTH = 41;
    private static final int PERFECT_HEIGHT = 12;

    // 透明度变化速率
    private static final float ALPHA_PER_TICK = 1F / 10;
    // 手柄旋转速度
    private static final float HANDLE_ROT_FAST = MathHelper.PI / 3;
    private static final float HANDLE_ROT_SLOW = MathHelper.PI / -7F;

    // 卷线声音计时器长度
    private static final int REEL_FAST_LENGTH = 30;
    private static final int REEL_SLOW_LENGTH = 20;
    private static final int CREAK_LENGTH = 6;

    // GUI位置变量
    private int leftPos, topPos;
    // 小游戏逻辑
    private final FishingMinigame minigame;
    // 预览物品
    private final ItemStack previewItem;
    // 小游戏状态
    public Status status = Status.HIT_TEXT;
    // 钓鱼准确度
    public double accuracy = -1;
    // 鼠标按下状态
    private boolean mouseDown = false;
    // 动画计时器
    private int animationTimer = 0;

    // 动画对象
    private final Animation textSize = new Animation(0);
    private final Animation progressBar;
    private final Animation bobberPos = new Animation(0);
    private final Animation bobberAlpha = new Animation(1);
    private final Animation fishPos = new Animation(0);
    private final Animation handleRot = new Animation(0);

    // 屏幕震动效果
    private final Shake shake = new Shake(0.75F, 1);

    // 卷线声音计时器
    public int reelSoundTimer = -1;
    // 吱嘎声计时器
    private int creakSoundTimer = 0;

    // 构造函数，初始化小游戏
    public FishingScreen(FishBehavior behavior, ItemStack previewItem) {
        super(TITLE);
        this.minigame = new FishingMinigame(this, behavior);
        this.previewItem = previewItem == null ? ItemStack.EMPTY : previewItem.copy();
        this.progressBar = new Animation(minigame.getProgress());
    }

    // 渲染方法
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        final float partialTick = delta;
        var matrices = context.getMatrices();

        if (!shouldPause()) {
            // render HIT!
            float scale = textSize.getInterpolated(partialTick) * 1.5F;
            float x = (width - HIT_WIDTH * scale) / 2;
            float y = (height - HIT_HEIGHT * scale) / 3;

            matrices.pushMatrix();
            matrices.scale(scale, scale);
            RenderUtil.blitF(context, TEXTURE, x * (1 / scale), y * (1 / scale), 71, 0, HIT_WIDTH, HIT_HEIGHT);
            matrices.popMatrix();
        } else {
            // 变暗 screen, avoid Screen.renderBackground() blur on 1.21.11
            context.fill(0, 0, width, height, 0xB0000000);

            RenderUtil.drawWithShake(matrices, shake, partialTick, status == Status.SUCCESS || status == Status.FAILURE, () -> {
                RenderUtil.drawWithBlend(() -> {
                    // draw 钓鱼 GUI
                    RenderUtil.blitF(context, TEXTURE, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT);

                    // draw 浮标
                    RenderUtil.drawWithAlpha(bobberAlpha.getInterpolated(partialTick), () -> {
                        float bobberY = 4 - 36 + (142 - bobberPos.getInterpolated(partialTick));
                        RenderUtil.blitF(context, TEXTURE, leftPos + 18, topPos + bobberY, 38, 0, 9, 36);
                    });
                });

                RenderUtil.drawWithShake(matrices, shake, partialTick, minigame.isBobberOnFish() && status == Status.MINIGAME, () -> {
                    // draw 鱼
                    float fishY = 4 - 16 + (142 - fishPos.getInterpolated(partialTick));
                    RenderUtil.blitF(context, TEXTURE, leftPos + 14, topPos + fishY, 55, 0, 16, 15);
                });

                // draw 进度条
                float progress = progressBar.getInterpolated(partialTick);
                int color = MathHelper.hsvToRgb(progress / 3.0F, 1.0F, 1.0F) | 0xFF000000;
                RenderUtil.fillF(context, leftPos + 33, topPos + 148, leftPos + 37, topPos + 148 - progress * 145, 0, color);

                // draw 处理
                RenderUtil.drawRotatedAround(matrices, handleRot.getInterpolated(partialTick), leftPos + 6.5F, topPos + 130.5F, () -> RenderUtil.blitF(context, TEXTURE, leftPos + 5, topPos + 129, 47, 0, 8, 3));

                // render 完美!
                if (status == Status.SUCCESS && accuracy == 1) {
                    float scale = textSize.getInterpolated(partialTick);
                    float x = leftPos + 2 + (PERFECT_WIDTH - PERFECT_WIDTH * scale) / 2;
                    float y = topPos - PERFECT_HEIGHT * scale;

                    matrices.pushMatrix();
                    matrices.scale(scale, scale);
                    RenderUtil.blitF(context, TEXTURE, x * (1 / scale), y * (1 / scale), 144, 0, PERFECT_WIDTH, PERFECT_HEIGHT);
                    matrices.popMatrix();
                }
            });
        }

        renderPreviewItem(context);
    }

    private void renderPreviewItem(DrawContext context) {
        if (previewItem.isEmpty()) {
            return;
        }

        int slotX = width / 2 - 9;
        int slotY = topPos - 28;

        context.fill(slotX - 3, slotY - 3, slotX + 19, slotY + 19, 0x66000000);
        context.drawItem(previewItem, slotX, slotY);
    }
    // 初始化方法
    @Override
    protected void init() {
        leftPos = (width - GUI_WIDTH) / 2;
        topPos = (height - GUI_HEIGHT) / 2;
    }
    //每帧更新
    @Override
    public void tick() {
        shake.tick();

        switch (status) {
            case HIT_TEXT -> {
                if (animationTimer < 20) {
                    if (++animationTimer == 20) {
                        status = Status.MINIGAME;
                    } else if (animationTimer <= 5) {
                        textSize.addValue(0.2F);
                    } else if (animationTimer <= 15) {
                        textSize.addValue(-0.013F);
                    } else {
                        textSize.addValue(-0.16F);
                    }
                }
            }
            case MINIGAME -> {
                minigame.tick(mouseDown);

                boolean onFish = minigame.isBobberOnFish();

                progressBar.setValue(minigame.getProgress());
                bobberPos.setValue(minigame.getBobberPos());
                bobberAlpha.addValue(onFish ? ALPHA_PER_TICK : -ALPHA_PER_TICK, 0.4F, 1);
                fishPos.setValue(minigame.getFishPos());
                handleRot.addValue(onFish ? HANDLE_ROT_FAST : HANDLE_ROT_SLOW);

                if (reelSoundTimer == -1 || --reelSoundTimer == 0) {
                    reelSoundTimer = onFish ? REEL_FAST_LENGTH : REEL_SLOW_LENGTH;
                    playSound(onFish ? StardewfishingFabric.REEL_FAST : StardewfishingFabric.REEL_SLOW);
                }

                if (creakSoundTimer > 0) {
                    creakSoundTimer--;
                }
                if (mouseDown && creakSoundTimer == 0) {
                    creakSoundTimer = CREAK_LENGTH;
                    playSound(StardewfishingFabric.REEL_CREAK);
                }
            }
            case SUCCESS, FAILURE -> {
                if (--animationTimer == 0) {
                    close();
                } else if (animationTimer >= 15) {
                    textSize.addValue(0.2F);
                } else if (animationTimer >= 5) {
                    textSize.addValue(-0.013F);
                } else {
                    textSize.addValue(-0.16F);
                }
            }
        }
    }
    // 鼠标点击事件
    @Override
    public boolean mouseClicked(Click click, boolean handled) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_1 || click.button() == GLFW.GLFW_MOUSE_BUTTON_2) {
            if (status == Status.MINIGAME && !mouseDown) {
                playSound(StardewfishingFabric.REEL_CREAK);
                mouseDown = true;
            }
            return true;
        } else {
            return super.mouseClicked(click, handled);
        }
    }
    // 鼠标释放事件
    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_1 || click.button() == GLFW.GLFW_MOUSE_BUTTON_2) {
            if (status == Status.MINIGAME && mouseDown) {
                mouseDown = false;
            }
            return true;
        } else {
            return super.mouseReleased(click);
        }
    }
    // 关闭屏幕时发送完成包
    @Override
    public void close() {
        super.close();

        // 发送完成结果给服务器
        SFNetworking.sendToServer(new C2SCompleteMinigamePacket(status == Status.SUCCESS, accuracy));

        stopReelingSounds();
    }
    // 是否在按下Esc时关闭屏幕
    @Override
    public boolean shouldCloseOnEsc() {
        return status == Status.MINIGAME;
    }
    // 是否是暂停屏幕
    @Override
    public boolean shouldPause() {
        return status != Status.HIT_TEXT;
    }
    // 设置结果和状态
    public void setResult(boolean success, double accuracy) {
        status = success ? Status.SUCCESS : Status.FAILURE;
        this.accuracy = accuracy;
        animationTimer = 40;
        textSize.reset(0.0F);

        progressBar.freeze();
        bobberPos.freeze();
        bobberAlpha.freeze();
        fishPos.freeze();
        handleRot.freeze();

        playSound(success ? StardewfishingFabric.COMPLETE : StardewfishingFabric.FISH_ESCAPE);
        shake.setValues(2.0F, 1);
    }
    // 播放声音
    public void playSound(SoundEvent soundEvent) {
        client.getSoundManager().play(PositionedSoundInstance.ui(soundEvent, 1.0F));
    }
    // 停止卷线声音
    public void stopReelingSounds() {
        client.getSoundManager().stopSounds(StardewfishingFabric.REEL_FAST.id(), null);
        client.getSoundManager().stopSounds(StardewfishingFabric.REEL_SLOW.id(), null);
    }
    // 小游戏状态枚举
    public enum Status {
        HIT_TEXT, MINIGAME, SUCCESS, FAILURE
    }
}
