package derp.immersivehotbar.mixin.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Gui.class)
public interface GuiOverlayAccessor {
    @Accessor("overlayMessageTime")
    int immersiveHotbar$getOverlayMessageTime();

    @Accessor("overlayMessageString")
    Component immersiveHotbar$getOverlayMessageString();
}