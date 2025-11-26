package render;

import world.World;

import java.awt.*;

public class Renderer {
    private int width, height;

    public Renderer(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void render(Graphics g, World world, Camera camera) {
        g.setColor(Color.GREEN);
        g.drawString("VoxelCraft - Rendering Placeholder", 20, 20);

        // TODO: 在这里调用 world 渲染逻辑（例如绘制立方体或线框）
    }
}
