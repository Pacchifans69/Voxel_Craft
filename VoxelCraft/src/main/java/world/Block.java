package world;

import render.math.Vector3f;

public class Block {
    public static final byte AIR = 0;
    public static final byte GRASS = 1;
    public static final byte DIRT = 2;
    public static final byte STONE = 3;

    private byte id;
    // --- 更加自然的调色板 (Minecraft 风格) ---
    // 1. 草顶绿色：稍微暗淡一点的森林绿，不要太荧光
    private static final int COLOR_GRASS_TOP = 0xFF5D9438;
    // 2. 泥土褐色：带一点灰度的棕色
    private static final int COLOR_DIRT = 0xFF866043;
    // 3. 石头灰色：标准的灰
    private static final int COLOR_STONE = 0xFF7D7D7D;

    public Block(byte id) {
        this.id = id;
    }

    public static int getColor(byte id, int dir) {
        switch (id) {
            case GRASS:
                // 只有顶面是绿色，底面和侧面都是泥土色
                if (dir == 4) return COLOR_GRASS_TOP;
                return COLOR_DIRT;

            case DIRT:
                return COLOR_DIRT;

            case STONE:
                return COLOR_STONE;

            default:
                return 0xFFFFFFFF; // 白色作为缺省错误色
        }
    }

    public boolean isActive() {
        return id != AIR;
    }

    public byte getId() {
        return id;
    }
    /**
     * 获取面的顶点。
     * 关键修正：统一所有面的绕序（Winding Order），确保所有朝外的面都被算作“正面”。
     */
    public static Vector3f[] getFaceVertices(int faceDir, float x, float y, float z) {
        float size = 0.5f;

        switch (faceDir) {
            case 0: // Front (Z+)
                // 之前导致透明是因为逆时针/顺时针反了，现在调整顺序
                return new Vector3f[]{
                        new Vector3f(x - size, y + size, z + size), // 左上
                        new Vector3f(x + size, y + size, z + size), // 右上
                        new Vector3f(x + size, y - size, z + size), // 右下
                        new Vector3f(x - size, y - size, z + size)  // 左下
                };

            case 1: // Back (Z-)
                // 同样反转顺序
                return new Vector3f[]{
                        new Vector3f(x + size, y + size, z - size), // 右上 (相对Back视角的左上)
                        new Vector3f(x - size, y + size, z - size),
                        new Vector3f(x - size, y - size, z - size),
                        new Vector3f(x + size, y - size, z - size)
                };

            case 2: // Left (X-)
                return new Vector3f[]{
                        new Vector3f(x - size, y + size, z - size),
                        new Vector3f(x - size, y + size, z + size),
                        new Vector3f(x - size, y - size, z + size),
                        new Vector3f(x - size, y - size, z - size)
                };

            case 3: // Right (X+)
                return new Vector3f[]{
                        new Vector3f(x + size, y + size, z + size),
                        new Vector3f(x + size, y + size, z - size),
                        new Vector3f(x + size, y - size, z - size),
                        new Vector3f(x + size, y - size, z + size)
                };

            case 4: // Top (Y+)
                // 你反馈这个面之前是能看到的，所以保持你现在的代码逻辑
                // 这里用的是我推测能让你看到的那个正确版本
                return new Vector3f[]{
                        new Vector3f(x - size, y + size, z + size), // 左前
                        new Vector3f(x - size, y + size, z - size), // 左后
                        new Vector3f(x + size, y + size, z - size), // 右后
                        new Vector3f(x + size, y + size, z + size)  // 右前
                };

            case 5: // Bottom (Y-)
                return new Vector3f[]{
                        new Vector3f(x - size, y - size, z - size),
                        new Vector3f(x - size, y - size, z + size),
                        new Vector3f(x + size, y - size, z + size),
                        new Vector3f(x + size, y - size, z - size)
                };

            default:
                return null;
        }
    }

}