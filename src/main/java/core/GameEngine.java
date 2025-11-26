package core;

import render.Renderer;
import render.Camera;
import world.World;
import input.InputHandler;

import javax.swing.*;
import java.awt.*;

public class GameEngine extends JPanel implements Runnable {
    private final int WIDTH = Config.SCREEN_WIDTH;
    private final int HEIGHT = Config.SCREEN_HEIGHT;

    private Renderer renderer;
    private World world;
    private Camera camera;
    private InputHandler input;
    private JFrame window;

    private boolean running = true;

    public GameEngine() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);

        renderer = new Renderer(WIDTH, HEIGHT);
        world = new World();
        camera = new Camera();
        input = new InputHandler(camera);

        addKeyListener(input);
        setFocusable(true);

        window = new JFrame("VoxelCraft");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.add(this);
        window.pack();
        window.setVisible(true);
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double nsPerFrame = 1e9 / Config.TARGET_FPS;

        while (running) {
            long now = System.nanoTime();
            if (now - lastTime < nsPerFrame) continue;
            lastTime = now;

            input.update();
            world.update();
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderer.render(g, world, camera);
    }
}
