package de.jonasb2510.screenshotsharemod2.client;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.component.Component;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ScreenshotDialog extends Screen {
    private final Path file;  // Store it here

    public ScreenshotDialog(Text title, Path file) {
        super(title);  // Only Text goes to super
        this.file = file;  // Save for later use
    }
    @Override
    protected void init() {
        // Horizontal center for all (screen width / 2)
        int centerX = this.width / 2;

        // Upload button: 100x20, centered horizontally, upper position
        int uploadX = centerX - 50;  // Half of width 10
        int uploadY = this.height / 2 - 50;  // Above true center
        ButtonWidget uploadBtn = ButtonWidget.builder(Text.translatable( "button.screenshotsharemod2.upload"), (btn) -> {
            this.client.getToastManager().add(
                    SystemToast.create(this.client, SystemToast.Type.LOW_DISK_SPACE,
                            Text.translatable("toast.screenshotsharemod2.upload"), Text.translatable("toast.screenshotsharemod2.upload_desc"))
            );
            CompletableFuture.supplyAsync(() -> {
                try {
                    return ScreenshotUpload.uploadToCatbox(this.file);  // Your upload method
                } catch (IOException e) {
                    return "Error: " + e.getMessage();
                }
            }).thenAccept((result) -> {
                // Back on main thread - show result
                this.client.execute(() -> {
                    if (result.startsWith("https")) {
                        Identifier id = Identifier.of("screenshotsharemod2", "send_share");
                        ClickEvent event = new ClickEvent.Custom(id, Optional.empty());
                        try {
                            this.client.player.sendMessage(
                                    Text.translatable("text.screenshotsharemod2.uploaded", result).setStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(new URI(result)))), false);
//                            this.client.player.sendMessage(
//                                    Text.translatable("text.screenshotsharemod2.share").setStyle(Style.EMPTY.withClickEvent(event).withColor(Formatting.YELLOW)), false);
                            this.client.player.sendMessage(
                                    Text.translatable("text.screenshotsharemod2.share").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.SuggestCommand("!screenshot " + result)).withColor(Formatting.YELLOW)), false);
                        } catch (URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                        this.client.setScreen(null);
                    } else {
                        this.client.player.sendMessage(
                                Text.literal(result), true);
                    }
                });
            });
        }).size(100, 20).position(uploadX, uploadY).build();

        // Cancel button: Default ~100x20 (adjust if needed), centered horizontally, below Upload
        int cancelX = centerX - 50;  // Half of ~100 width
        int cancelY = uploadY + 50 - 30;  // 100px gap below Upload 30=gap szie
        ButtonWidget cancelBtn = ButtonWidget.builder(Text.translatable("button.screenshotsharemod2.cancel"), (btn) -> {
            this.client.setScreen(null);  // Closes dialog (use this.client)
        }).size(100, 20).position(cancelX, cancelY).build();

        this.addDrawableChild(uploadBtn);
        this.addDrawableChild(cancelBtn);
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int titleX = this.width / 2;
        context.drawCenteredTextWithShadow(this.textRenderer, title,
                titleX, 40, 0xFFFFFFFF);
    }
}