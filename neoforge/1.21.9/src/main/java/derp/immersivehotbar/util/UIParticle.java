package derp.immersivehotbar.util;

import net.minecraft.client.Minecraft;

public class UIParticle {
    public float x, y;
    public float vx, vy;
    public float alpha = 1.0f;


    public float tintR = 1.0f;
    public float tintG = 1.0f;
    public float tintB = 1.0f;

    public UIParticle(float x, float y) {
        this.x = x;
        this.y = y;

        this.vx = (float)(Math.random() - 0.5) * 1.2f;
        this.vy = (float)(Math.random() - 1.2) * 2.2f;
    }

    public boolean tick() {
        x += vx;
        y += vy;
        vy += 0.02f;

        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        return x >= -10 && x <= width + 10 && y <= height + 10 && alpha > 0f;
    }
}
