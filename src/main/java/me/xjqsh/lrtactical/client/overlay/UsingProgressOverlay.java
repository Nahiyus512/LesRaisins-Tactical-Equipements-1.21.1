package me.xjqsh.lrtactical.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.item.ICustomItem;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.api.item.IThrowable;
import me.xjqsh.lrtactical.config.ClientConfig;
import me.xjqsh.lrtactical.init.ModCapabilities;
import me.xjqsh.lrtactical.item.throwable.ThrowableData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class UsingProgressOverlay {
    public static final ResourceLocation ARROW_TEXTURE = ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "textures/gui/arrow.png");

    private static final int IMPACT_DURATION_TICKS = 6;

    private int lastTickCount = -1;
    private int impactTicks = 0;
    private int lastCombatCooldownTick = 0;

    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }
        if (ClientConfig.HUD_STYLE.get() == ClientConfig.HudStyle.LINE) {
            renderLineHud(guiGraphics, deltaTracker, player);
        } else {
            renderRingHud(guiGraphics, deltaTracker, player);
        }
    }

    private void renderRingHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Player player) {
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);

        int tickCount = player.tickCount;
        if (lastTickCount != tickCount) {
            impactTicks = Math.max(0, impactTicks - 1);
            lastTickCount = tickCount;
        }

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        int crosshairX = (screenWidth - 15) / 2;
        int crosshairY = (screenHeight - 15) / 2;
        float cx = crosshairX + 7.5f;
        float cy = crosshairY + 7.5f;

        ItemStack useStack = player.getUseItem();
        float progress = -1f;
        float cookProgress = -1f;

        if (player.isUsingItem() && useStack.getItem() instanceof ICustomItem item) {
            float maxTick = item.getMaxUsingTick(useStack);
            if (maxTick > 0) {
                int usingTick = player.getTicksUsingItem();
                progress = Mth.clamp((usingTick + partialTick) / maxTick, 0f, 1f);
                if (useStack.getItem() instanceof IThrowable throwable) {
                    final float[] cookProgressHolder = new float[]{-1f};
                    throwable.getThrowableIndex(useStack).ifPresent(index -> {
                        ThrowableData data = index.getData();
                        if (data.isCookable() && player.getTicksUsingItem() >= data.getPrepareTime()) {
                            int cookTime = player.getTicksUsingItem() - data.getPrepareTime();
                            float maxCookTime = (float) data.getEntityData().getLifeTime() * 0.9f;
                            if (maxCookTime > 0) {
                                float cook = Mth.clamp((cookTime + partialTick) / maxCookTime, 0f, 1f);
                                cookProgressHolder[0] = cook;
                            }
                        }
                    });
                    cookProgress = cookProgressHolder[0];
                }
            }
        } else {
            var cap = player.getData(ModCapabilities.COMBAT_PROPERTIES);
            int maxTick = cap.getLastMaxTick();
            int coolDownTick = cap.getCoolDownTick();
            ItemStack mainHand = player.getMainHandItem();
            if (coolDownTick > 0 && maxTick > 0 && !cap.isDrawing() && mainHand.getItem() instanceof IMeleeWeapon) {
                if (coolDownTick > lastCombatCooldownTick) {
                    impactTicks = IMPACT_DURATION_TICKS;
                }
                progress = 1f - Mth.clamp((coolDownTick - partialTick) / (float) maxTick, 0f, 1f);
            }
            lastCombatCooldownTick = coolDownTick;
        }

        if (progress < 0f) {
            return;
        }

        float kick = 0f;
        if (impactTicks > 0) {
            kick = Mth.clamp((impactTicks - partialTick) / (float) IMPACT_DURATION_TICKS, 0f, 1f);
        }

        float baseOuterRadius = 9.5f;
        float baseThickness = 2.0f;
        float outerRadius = baseOuterRadius + 1.6f * kick;
        float thickness = baseThickness;
        float innerRadius = Math.max(0.1f, outerRadius - thickness);

        int bgColor = argb(24, 0xFF, 0xFF, 0xFF);
        int fgColor = argb(160, 0xFF, 0xFF, 0xFF);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        drawRingArc(guiGraphics, cx, cy, innerRadius, outerRadius, -Mth.HALF_PI, -Mth.HALF_PI + Mth.TWO_PI, bgColor);

        float start = -Mth.HALF_PI;
        float end = start + progress * Mth.TWO_PI;
        drawRingArc(guiGraphics, cx, cy, innerRadius, outerRadius, start, end, fgColor);

        if (cookProgress >= 0f) {
            float cookOuter = outerRadius;
            float cookInner = innerRadius;
            int cookColor = argb(0xFF, 0xFF, 0x57, 0x57);
            drawRingArc(
                    guiGraphics,
                    cx,
                    cy,
                    cookInner,
                    cookOuter,
                    -Mth.HALF_PI,
                    -Mth.HALF_PI + Mth.TWO_PI * Mth.clamp(cookProgress, 0f, 1f),
                    cookColor
            );
        }

        if (player.isUsingItem() && useStack.getItem() instanceof IThrowable && player.isCrouching()) {
            RenderSystem.setShaderColor(1f, 1f, 1f, 0.7f);
            guiGraphics.blit(ARROW_TEXTURE, (int) cx - 4, (int) cy + (int) (outerRadius + 6), 0, 0, 8, 4, 8, 4);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private void drawRingArc(GuiGraphics guiGraphics, float cx, float cy, float innerRadius, float outerRadius, float startAngle, float endAngle, int argb) {
        if (endAngle <= startAngle || outerRadius <= innerRadius) {
            return;
        }

        float angleSpan = endAngle - startAngle;
        int segments = Math.max(24, (int) (180 * (angleSpan / Mth.TWO_PI)));

        float a = ((argb >>> 24) & 255) / 255f;
        float r = ((argb >>> 16) & 255) / 255f;
        float g = ((argb >>> 8) & 255) / 255f;
        float b = (argb & 255) / 255f;

        Matrix4f matrix = guiGraphics.pose().last().pose();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            float t = i / (float) segments;
            float angle = startAngle + angleSpan * t;
            float cos = Mth.cos(angle);
            float sin = Mth.sin(angle);
            buffer.addVertex(matrix, cx + cos * outerRadius, cy + sin * outerRadius, 0).setColor(r, g, b, a);
            buffer.addVertex(matrix, cx + cos * innerRadius, cy + sin * innerRadius, 0).setColor(r, g, b, a);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static int argb(int alpha, int r, int g, int b) {
        int a = Mth.clamp(alpha, 0, 255);
        return (a << 24) | ((r & 255) << 16) | ((g & 255) << 8) | (b & 255);
    }

    private void renderLineHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Player player) {
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        ItemStack stack = player.getUseItem();
        if (stack.getItem() instanceof ICustomItem item) {
            float maxTick = item.getMaxUsingTick(stack);
            int usingTick = player.getTicksUsingItem();
            float progress = Math.min(1f, usingTick / maxTick);
            int x = screenWidth / 2 - 16;
            int y = screenHeight / 2 + 16;
            int alpha;
            if (progress == 1f) {
                alpha = (int) (80 + 80 * Math.sin(usingTick / 2f));
            } else {
                alpha = 0x80;
            }
            guiGraphics.fill(x, y, (int) (x + progress * 32), y + 4, 0xFFFFFF | (alpha << 24));
            if (stack.getItem() instanceof IThrowable throwable) {
                throwable.getThrowableIndex(stack).ifPresent(index -> {
                    ThrowableData data = index.getData();
                    if (data.isCookable() && player.getTicksUsingItem() >= data.getPrepareTime()) {
                        int cookTime = player.getTicksUsingItem() - data.getPrepareTime();
                        float cookProgress = Math.min(1f, cookTime / (float) data.getEntityData().getLifeTime());
                        guiGraphics.fill(x, y, (int) (x + cookProgress * 32), y + 4, 0xFF0000 | (alpha << 24));
                    }
                });
                if (player.isCrouching()) {
                    RenderSystem.enableBlend();
                    RenderSystem.setShaderColor(1f, 1f, 1f, 0.7f);
                    guiGraphics.blit(ARROW_TEXTURE, x + 12, y + 6, 0, 0, 8, 4, 8, 4);
                    RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                    RenderSystem.disableBlend();
                }
            }
        }

        var cap = player.getData(ModCapabilities.COMBAT_PROPERTIES);
        if (cap.getCoolDownTick() > 0) {
            float maxTick = cap.getLastMaxTick();
            float progress = 1 - Math.min(1f, cap.getCoolDownTick() / maxTick);
            int x = screenWidth / 2 - 16;
            int y = screenHeight / 2 + 16;
            int alpha = 0x80;
            if (progress == 1f) {
                alpha = (int) (80 + 80 * Math.sin(cap.getCoolDownTick() / 2f));
            }
            guiGraphics.fill(x, y, (int) (x + progress * 32), y + 4, 0xFFFFFF | (alpha << 24));
        }
    }
}
