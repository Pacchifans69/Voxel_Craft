package input;

import render.Camera;
import world.World;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class InputHandler implements KeyListener, MouseListener, MouseMotionListener {
    private Camera camera;
    private World world;

    // 按键状态
    private boolean w, a, s, d;
    private boolean space, shift;

    // --- 修改点 1: 大幅降低灵敏度 ---
    // Y轴(Pitch)更低，符合人眼对垂直运动更敏感的习惯
    private float mouseSensitivityX = 0.0010f; // 降低了一半
    private float mouseSensitivityY = 0.0005f; // 降低了原来的3/4

    private float moveSpeed = 0.15f;

    private boolean isMouseLocked = false;
    private Component context;
    private Robot robot;
    private int centerX, centerY;

    public InputHandler(Camera camera, World world) {
        this.camera = camera;
        this.world = world;
        try {
            this.robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public void initMouseLock(Component component) {
        this.context = component;
        if (component.isShowing()) {
            updateCenterPoint();
            setMouseLocked(true);
        }
    }

    private void updateCenterPoint() {
        if (context == null) return;
        Point loc = context.getLocationOnScreen();
        centerX = loc.x + context.getWidth() / 2;
        centerY = loc.y + context.getHeight() / 2;
    }

    private void setMouseLocked(boolean locked) {
        this.isMouseLocked = locked;
        if (locked) {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Cursor blankCursor = toolkit.createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
            context.setCursor(blankCursor);
            centerMouse();
        } else {
            context.setCursor(Cursor.getDefaultCursor());
        }
    }

    private void centerMouse() {
        if (context == null || !context.isShowing()) return;
        robot.mouseMove(centerX, centerY);
    }

    public void update() {
        float dx = 0;
        float dz = 0;
        float dy = 0;

        // 获取当前 Yaw
        float yaw = camera.getRotation().y;

        // --- 修正的核心 ---
        // 之前：sin(yaw) --- 此时正角代表逆时针
        // 现在：-sin(yaw) --- 强行反转X轴分量以适配摄像机矩阵

        float sin = (float) Math.sin(yaw);
        float cos = (float) Math.cos(yaw);

        // 前方(Forward)计算
        // 0度(Z轴)依然取决于 cos (不变，保持完美)
        // 90度(X轴)取决于 sin (变了)
        // 旧逻辑：forwardX = sin; forwardZ = -cos;
        // 新逻辑：forwardX = -sin; forwardZ = -cos;

        float forwardX = -sin;
        float forwardZ = -cos;

        // 右方(Right)计算 (垂直于前方)
        // 右方向量 X 取决于 cos，Z 取决于 sin
        // 新逻辑保证 Right = Forward 顺时针转 90度

        float rightX = cos;
        float rightZ = -sin; // 同时也反转这里的 sin，解决 90度时的平移颠倒

        // 这里的 WASD 逻辑本身不需要变，只需要上面算出的基准向量是对的
        if (w) {
            dx += forwardX * moveSpeed;
            dz += forwardZ * moveSpeed;
        }
        if (s) {
            dx -= forwardX * moveSpeed;
            dz -= forwardZ * moveSpeed;
        }

        if (d) {
            dx += rightX * moveSpeed;
            dz += rightZ * moveSpeed;
        }
        if (a) {
            dx -= rightX * moveSpeed;
            dz -= rightZ * moveSpeed;
        }

        if (space) dy += moveSpeed;
        if (shift) dy -= moveSpeed;

        if (dx != 0 || dy != 0 || dz != 0) {
            camera.moveWithCollision(dx, dy, dz, world);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (isMouseLocked && context.isFocusOwner()) {
            // 这里不调用 updateCenterPoint 以优化性能，假定窗口位置不移动
            // 如果窗口频繁移动，请把 updateCenterPoint() 加回来

            int currentX = e.getXOnScreen();
            int currentY = e.getYOnScreen();

            int deltaX = currentX - centerX;
            int deltaY = currentY - centerY;

            // 防止 robot 回中时的微小抖动
            if (deltaX == 0 && deltaY == 0) return;

            // --- 修改点 2: 关键反转与灵敏度应用 ---

            // 注意这里：mouseSensitivityX 前面加上了 负号 (-)
            // 这是一个修正值。如果之前的移动是“反直觉”的，加个负号通常能直接解决
            // 使得鼠标向右移 -> deltaX正 -> 减去角度 -> 视角向右转 (具体取决于你的 Math 库是左手还是右手系)
            float rotY = -deltaX * mouseSensitivityX;

            // Pitch (上下) 通常保持默认方向（屏幕向下是正Y，所以向下移鼠标 -> 增加Pitch -> 看来像是低头/抬头）
            // 如果你感觉上下也反了（比如想要“飞机拉杆”反转视角），可以在 deltaY 前面加负号
            float rotX = -deltaY * mouseSensitivityY;

            camera.rotate(rotX, rotY, 0);

            centerMouse();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> w = true;
            case KeyEvent.VK_S -> s = true;
            case KeyEvent.VK_A -> a = true;
            case KeyEvent.VK_D -> d = true;
            case KeyEvent.VK_SPACE -> space = true;
            case KeyEvent.VK_SHIFT -> shift = true;
            case KeyEvent.VK_ESCAPE -> setMouseLocked(false);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> w = false;
            case KeyEvent.VK_S -> s = false;
            case KeyEvent.VK_A -> a = false;
            case KeyEvent.VK_D -> d = false;
            case KeyEvent.VK_SPACE -> space = false;
            case KeyEvent.VK_SHIFT -> shift = false;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!isMouseLocked) {
            // 点击重新锁定之前重新计算一下中心点，防止窗口刚才移动过
            initMouseLock(e.getComponent());
        }
    }

    // --- 空实现 ---
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}