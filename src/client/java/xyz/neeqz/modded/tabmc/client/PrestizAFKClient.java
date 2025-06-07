package xyz.neeqz.modded.tabmc.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.Entity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import xyz.neeqz.modded.tabmc.client.UpdateStatus.UpdateChecker;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class PrestizAFKClient implements ClientModInitializer {

    private boolean isRunning = false;
    private long interval = 0;
    private long lastPrestizRun = 0;
    private boolean waitForGuiClick = false;

    @Override
    public void onInitializeClient() {
        OwnerNameTag.register();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerCommands(dispatcher);
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.interactionManager == null) return;

            UpdateChecker.checkForUpdate(); // Sprawdzenie wersji co tick (ale w klasie jest limit 30s)

            long now = System.currentTimeMillis();

            if (isRunning) {

                if (now - lastPrestizRun >= interval) {
                    lastPrestizRun = now;
                    waitForGuiClick = true;

                    client.player.networkHandler.sendChatCommand("prestiz");
                    client.player.sendMessage(ChatLogColor.color(" "), false);
                    client.player.sendMessage(ChatLogColor.color("&7[&6LOG&7] &fWysłano komendę /prestiz"), false);
                }



                HitResult target = client.crosshairTarget;
                if (target != null) {
                    if (target.getType() == HitResult.Type.ENTITY) {
                        Entity targetEntity = ((EntityHitResult) target).getEntity();
                        client.interactionManager.attackEntity(client.player, targetEntity);
                        client.player.swingHand(Hand.MAIN_HAND);
                    } else if (target.getType() == HitResult.Type.BLOCK) {
                        BlockHitResult blockHit = (BlockHitResult) target;
                        client.interactionManager.attackBlock(blockHit.getBlockPos(), blockHit.getSide());
                        client.player.swingHand(Hand.MAIN_HAND);
                    }
                }
            }
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (waitForGuiClick && client.player != null && screen instanceof HandledScreen<?>) {
                if (client.player.currentScreenHandler.slots.size() > 22) {
                    client.interactionManager.clickSlot(
                            client.player.currentScreenHandler.syncId,
                            22,
                            0,
                            SlotActionType.PICKUP,
                            client.player
                    );

                    client.player.closeHandledScreen();
                    waitForGuiClick = false;

                    client.player.sendMessage(ChatLogColor.color("&7[&6LOG&7] &fKliknięto slot 22 i zamknięto GUI"), false);
                    client.player.sendMessage(ChatLogColor.color(" "), false);
                }
            }
        });
    }

    private void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("startafk")
                .then(argument("seconds", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            interval = IntegerArgumentType.getInteger(ctx, "seconds") * 1000L;
                            isRunning = true;
                            lastPrestizRun = 0;
                            ctx.getSource().sendFeedback(ChatLogColor.color(" "));
                            ctx.getSource().sendFeedback(ChatLogColor.color("&aWłączyłeś tryb AFK"));
                            ctx.getSource().sendFeedback(ChatLogColor.color(" "));
                            return 1;
                        }))
        );

        dispatcher.register(literal("stopafk")
                .executes(ctx -> {
                    isRunning = false;
                    ctx.getSource().sendFeedback(ChatLogColor.color(" "));
                    ctx.getSource().sendFeedback(ChatLogColor.color("&cWyłączyłeś tryb AFK"));
                    return 1;
                }));
    }
}