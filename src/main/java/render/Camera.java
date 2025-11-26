package render;

import render.math.Vector3f;

public class Camera {
    public Vector3f position = new Vector3f(0, 0, -5);
    public float pitch = 0;
    public float yaw = 0;

    public void move(float dx, float dy, float dz) {
        position.x += dx;
        position.y += dy;
        position.z += dz;
    }
}
