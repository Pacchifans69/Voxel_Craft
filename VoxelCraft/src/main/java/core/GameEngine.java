package core;

import input.InputHandler;
import render.Camera;
import render.Renderer;
import world.World;

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
        input = new InputHandler(camera, world); // 创建

        // --- 核心变更开始 ---
        addKeyListener(input);            // 监听键盘
        addMouseListener(input);          // 监听点击
        addMouseMotionListener(input);    // 监听移动

        setFocusable(true);
        requestFocusInWindow(); // 确保启动时获得焦点

        window = new JFrame("VoxelCraft");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.add(this);
        window.pack();
        window.setVisible(true);

        // 初始化鼠标锁定 (窗口显示后调用)
        input.initMouseLock(this);
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
