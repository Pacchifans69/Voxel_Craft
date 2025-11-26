package input;

import render.Camera;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class InputHandler implements KeyListener {
    private Camera camera;
    private boolean w, a, s, d;

    public InputHandler(Camera camera) {
        this.camera = camera;
    }

    public void update() {
        float speed = 0.1f;
        if (w) camera.move(0, 0, speed);
        if (s) camera.move(0, 0, -speed);
        if (a) camera.move(speed, 0, 0);
        if (d) camera.move(-speed, 0, 0);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> w = true;
            case KeyEvent.VK_S -> s = true;
            case KeyEvent.VK_A -> a = true;
            case KeyEvent.VK_D -> d = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> w = false;
            case KeyEvent.VK_S -> s = false;
            case KeyEvent.VK_A -> a = false;
            case KeyEvent.VK_D -> d = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
