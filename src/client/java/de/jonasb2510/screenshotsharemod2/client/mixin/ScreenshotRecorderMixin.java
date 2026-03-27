//package de.jonasb2510.screenshotsharemod2.client.mixin;
//
//import com.mojang.blaze3d.buffers.GpuBuffer;
//import com.mojang.blaze3d.systems.CommandEncoder;
//import com.mojang.blaze3d.systems.RenderSystem;
//import com.mojang.blaze3d.textures.GpuTexture;
//import net.minecraft.client.gl.Framebuffer;
//import net.minecraft.client.texture.NativeImage;
//import net.minecraft.client.util.ScreenshotRecorder;
//import net.minecraft.util.math.ColorHelper;
//import org.slf4j.Logger;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.function.Consumer;
//
//@Mixin(ScreenshotRecorder.class)
//public class ScreenshotRecorderMixin {
//    @Shadow @Final private static Logger LOGGER;
//
//    @Inject(method = "takeScreenshot(Lnet/minecraft/client/gl/Framebuffer;ILjava/util/function/Consumer;)V", at = @At("TAIL"))
//    private static void onTakeScreenshot(Framebuffer framebuffer, int downscaleFactor, Consumer<NativeImage> callback, CallbackInfo ci) throws IOException {
//        File file= new File("C:\\Users\\ASRock\\Desktop\\mc screenshotsharemod2", "screenshots");
//        int width = framebuffer.textureWidth;
//        int height = framebuffer.textureHeight;
//        GpuTexture gpuTexture = framebuffer.getColorAttachment();
//        GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Screenshot buffer", 9, (long)width * height * gpuTexture.getFormat().pixelSize());
//        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
//        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(gpuBuffer, true, false)) {
//            int widthScale = width / downscaleFactor;
//            int heightScale = height / downscaleFactor;
//            NativeImage nativeImage = new NativeImage(widthScale, heightScale, false);
//
//            for (int n = 0; n < heightScale; n++) {
//                for (int o = 0; o < widthScale; o++) {
//                    if (downscaleFactor == 1) {
//                        int p = mappedView.data().getInt((o + n * width) * gpuTexture.getFormat().pixelSize());
//                        nativeImage.setColor(o, height - n - 1, p | 0xFF000000);
//                    } else {
//                        int red = 0;
//                        int green = 0;
//                        int blue = 0;
//
//                        for (int s = 0; s < downscaleFactor; s++) {
//                            for (int t = 0; t < downscaleFactor; t++) {
//                                int u = mappedView.data().getInt((o * downscaleFactor + s + (n * downscaleFactor + t) * width) * gpuTexture.getFormat().pixelSize());
//                                red += ColorHelper.getRed(u);
//                                green += ColorHelper.getGreen(u);
//                                blue += ColorHelper.getBlue(u);
//                            }
//                        }
//
//                        int s = downscaleFactor * downscaleFactor;
//                        nativeImage.setColor(o, heightScale - n - 1, ColorHelper.getArgb(255, red / s, green / s, blue / s));
//                    }
//                }
//            }
//
//            callback.accept(nativeImage);
//            nativeImage.writeTo(file);
//        }
//        gpuBuffer.close();
//    }
//}
package de.jonasb2510.screenshotsharemod2.client.mixin;

import de.jonasb2510.screenshotsharemod2.Screenshotsharemod2;
import de.jonasb2510.screenshotsharemod2.client.Screenshotsharemod2Client;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.net.URI;
import java.util.UUID;

@Mixin(NativeImage.class)
public class ScreenshotRecorderMixin {
    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "writeTo(Ljava/io/File;)V", at = @At("TAIL"))
    public void writeTo(File path, CallbackInfo ci) {
        if (path.toString().contains("screenshots")) {
            MinecraftClient.getInstance().execute(() -> {
                UUID uuid = UUID.randomUUID();
                Screenshotsharemod2Client.fileMap.put(uuid, path.toPath());
                MinecraftClient.getInstance().player.sendMessage(Text.translatable("text.screenshotsharemod2.onclick").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/uploadscreenshot " + uuid.toString())).withColor(Formatting.YELLOW)), false);
            });
        }
    }
}