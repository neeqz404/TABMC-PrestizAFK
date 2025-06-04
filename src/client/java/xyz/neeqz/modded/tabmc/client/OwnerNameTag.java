package xyz.neeqz.modded.tabmc.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;


    public class OwnerNameTag {

        public static void register() {
            WorldRenderEvents.AFTER_ENTITIES.register(context -> {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player == null || client.world == null) return;

                for (PlayerEntity player : client.world.getPlayers()) {
                    if (!"Neeqz".equals(player.getName().getString())) continue;

                    MatrixStack matrices = context.matrixStack();
                    Vec3d pos = player.getLerpedPos(context.tickDelta());
                    double x = pos.x;
                    double y = pos.y + player.getHeight() + 1.2;
                    double z = pos.z;

                    Camera camera = context.camera();
                    Vec3d camPos = camera.getPos();

                    matrices.push();
                    matrices.translate(x - camPos.x, y - camPos.y, z - camPos.z);
                    matrices.multiply(camera.getRotation());
                    matrices.scale(-0.025f, -0.025f, 0.025f);

                    TextRenderer textRenderer = client.textRenderer;
                    String text = "§aOWNER MOD: §6TabMC-PrestizAFK";
                    float width = textRenderer.getWidth(text) / 2f;

                    VertexConsumerProvider.Immediate bufferSource = client.getBufferBuilders().getEntityVertexConsumers();
                    textRenderer.draw(
                            text,
                            -width,
                            0,
                            0xFFFFFF,
                            false,
                            matrices.peek().getPositionMatrix(),
                            bufferSource,
                            TextRenderer.TextLayerType.NORMAL,
                            0,
                            LightmapTextureManager.MAX_LIGHT_COORDINATE
                    );
                    bufferSource.draw();
                    matrices.pop();
                }
            });
        }
    }