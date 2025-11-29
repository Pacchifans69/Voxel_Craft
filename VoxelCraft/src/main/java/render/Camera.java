package render;

import render.math.Matrix4f;
import render.math.Vector3f;
import world.World;

public class Camera {
    // 玩家的碰撞箱尺寸
    private final float PLAYER_WIDTH = 0.4f; // 半径
    private final float PLAYER_HEIGHT = 1.8f;
    private Vector3f position;
    private Vector3f rotation;

    public Camera() {
        this.position = new Vector3f(16.0f, 40.0f, 16.0f); // 默认高处
        this.rotation = new Vector3f(0.0f, 0.0f, 0.0f);
    }

    // ... getViewMatrix, getPosition, Setter 等保持不变 ...

    public Matrix4f getViewMatrix() {
        // ... (保持之前的实现)
        Matrix4f negativeTranslation = Matrix4f.translation(-position.x, -position.y, -position.z);
        Matrix4f negativeRotX = Matrix4f.rotationX(-rotation.x);
        Matrix4f negativeRotY = Matrix4f.rotationY(-rotation.y);
        return negativeRotX.mul(negativeRotY).mul(negativeTranslation);
    }

    /**
     * 解决问题 2：增加 Pitch 限制
     */
    public void rotate(float dPitch, float dYaw, float dRoll) {
        this.rotation.x += dPitch;
        this.rotation.y += dYaw;
        this.rotation.z += dRoll;

        // 限制 Pitch (X轴旋转) 在 -90度 到 90度之间 (弧度 -PI/2 到 PI/2)
        // 留一点余量(0.01)防止 Gimbal Lock
        float maxPitch = (float) (Math.PI / 2.0f - 0.01f);
        if (rotation.x > maxPitch) rotation.x = maxPitch;
        if (rotation.x < -maxPitch) rotation.x = -maxPitch;
    }

    /**
     * 解决问题 1：带碰撞检测的移动
     * 采用“分离轴”逻辑：先尝试X移动，如果不撞则应用；再尝试Z...
     */
    public void moveWithCollision(float dx, float dy, float dz, World world) {
        // 1. 尝试 X 轴移动
        if (dx != 0) {
            float nextX = position.x + dx;
            if (!checkCollision(world, nextX, position.y, position.z)) {
                position.x = nextX;
            }
        }

        // 2. 尝试 Z 轴移动
        if (dz != 0) {
            float nextZ = position.z + dz;
            if (!checkCollision(world, position.x, position.y, nextZ)) {
                position.z = nextZ;
            }
        }

        // 3. 尝试 Y 轴移动 (飞行模式)
        if (dy != 0) {
            float nextY = position.y + dy;
            if (!checkCollision(world, position.x, nextY, position.z)) {
                position.y = nextY;
            }
        }
    }

    /**
     * 检测以 (x,y,z) 为中心的玩家包围盒是否碰到 World 中的方块
     */
    private boolean checkCollision(World world, float x, float y, float z) {
        // 简化的 AABB 检测：检查玩家包围盒的 4个角 + 头部中心 + 脚部中心
        // 实际上只要检查: (x±r, y, z±r) 和 (x±r, y-h, z±r)

        float r = PLAYER_WIDTH;

        // 检查身体四周（比如脚踝高度）
        // 偏移量 -1.5f 大概是眼睛高度减去身体高度，或者我们简化，认为Camera是头顶，往下检查
        // 假设 Position 是眼睛位置。脚底是 y - 1.6
        float footY = y - 1.6f;
        float headY = y - 0.1f;

        // 我们需要检查脚底和头顶所在的层
        // 这里的逻辑：如果在 target 位置的【任何一个角】碰到了 solid block，就返回 true

        if (isBlocked(world, x - r, footY, z - r)) return true;
        if (isBlocked(world, x + r, footY, z - r)) return true;
        if (isBlocked(world, x - r, footY, z + r)) return true;
        if (isBlocked(world, x + r, footY, z + r)) return true;

        // 也要检查头部高度（防止穿墙）
        if (isBlocked(world, x - r, headY, z - r)) return true;
        if (isBlocked(world, x + r, headY, z - r)) return true;
        if (isBlocked(world, x - r, headY, z + r)) return true;
        if (isBlocked(world, x + r, headY, z + r)) return true;

        return false;
    }

    private boolean isBlocked(World world, float x, float y, float z) {
        return world.isSolid(x, y, z);
    }

    // Getter/Setter 补充
    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(Vector3f rot) {
        this.rotation = rot;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f pos) {
        this.position = pos;
    }
}