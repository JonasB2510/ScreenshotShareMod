package de.jonasb2510.screenshotsharemod2.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import de.jonasb2510.screenshotsharemod2.Screenshotsharemod2;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.Component;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Screenshotsharemod2Client implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("ScreenshotShareMod2");
    public static Map<UUID, Path> fileMap = new HashMap<>();
    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing ScreenshotShareMod2");
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("uploadscreenshot")
                            .executes(context -> {
                                context.getSource().sendFeedback(
                                        Text.translatable("text.screenshotsharemod2.warn.provide_uuid")
                                );
                                return 1;
                            })
                            .then(argument("uuid", StringArgumentType.word())
                                    .executes(context -> {
                                        String uuidStr = StringArgumentType.getString(context, "uuid");
                                        try {
                                            UUID uuid = UUID.fromString(uuidStr);
                                            Path file = fileMap.get(uuid);
                                            if (file != null) {
                                                MinecraftClient.getInstance().setScreen(new ScreenshotDialog(Text.translatable("gui.screenshotsharemod2.title"), file));
                                            } else {
                                                MinecraftClient.getInstance().player.sendMessage(
                                                        Text.translatable("text.screenshotsharemod2.error.nofile", uuidStr), false
                                                );
                                            }
                                        } catch (IllegalArgumentException e) {
                                            context.getSource().sendFeedback(Text.translatable("text.screenshotsharemod2.error.invalid_uuid", uuidStr));
                                        }
                                        return 1;
                                    })
                            )
            );
        });
//        ClientReceiveMessageEvents.CHAT.register((message, signed_message, sender, params, timestamp) -> {
//            LOGGER.info("CHAT Message from {}: {} with params {}", sender, message, params);
//        });
//        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
//            LOGGER.info("GAME Message: {}, overlay: {}", message, overlay);
//        });
//        https://www.reddit.com/r/fabricmc/comments/1b5g5vi/onchat_event_in_fabric/
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signed, sender, params, timestamp) -> {
            String fullMsg = message.getString();

            if (fullMsg.contains("!screenshot")) {
                String[] parts = fullMsg.split(" ", 3);
                if (parts.length < 3) {
                    return true;
                }
                String arg = parts[2];

                if (arg.startsWith("http")) {
                    if (isValidUrl(arg)) {
                        try {
                            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
                                    Text.translatable("text.screenshotsharemod2.shared", arg).setStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(new URI(arg))).withColor(Formatting.BLUE))
                            );
                            return false;
                        } catch (URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return true;
                }

                return true;
            }
            return true;
        });
    }
    private boolean isValidUrl(String urlStr) {
        try {
            new URL(urlStr).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
