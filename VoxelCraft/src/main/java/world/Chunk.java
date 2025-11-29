package world;

import render.math.Vector3f;
import util.NoiseGenerator;

import java.util.ArrayList;
import java.util.List;

public class Chunk {
    public static final int SIZE = 16;
    public static final int HEIGHT = 32;
    private static final NoiseGenerator noiseGen = new NoiseGenerator(12345L);
    private Block[][][] blocks;
    private List<RenderFace> mesh; // 修改类型
    private int chunkX, chunkZ;

    public Chunk(int startX, int startZ) {
        this.chunkX = startX;
        this.chunkZ = startZ;
        this.blocks = new Block[SIZE][HEIGHT][SIZE];
        this.mesh = new ArrayList<>();

        generateTerrain();
    }

    private void generateTerrain() {
        // (保持原来的噪声生成逻辑不变...)
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                int worldX = chunkX * SIZE + x;
                int worldZ = chunkZ * SIZE + z;

                // 地形平缓一点，方便测试光照
                double frequency = 0.05;
                double amplitude = 8.0;
                double noiseValue = noiseGen.noise2D(worldX * frequency, worldZ * frequency);
                int surfaceHeight = (int) (10 + noiseValue * amplitude);

                if (surfaceHeight < 1) surfaceHeight = 1;
                if (surfaceHeight >= HEIGHT) surfaceHeight = HEIGHT - 1;

                for (int y = 0; y < HEIGHT; y++) {
                    if (y == surfaceHeight) blocks[x][y][z] = new Block(Block.GRASS);
                    else if (y < surfaceHeight && y > surfaceHeight - 3) blocks[x][y][z] = new Block(Block.DIRT);
                    else if (y <= surfaceHeight - 3) blocks[x][y][z] = new Block(Block.STONE);
                    else blocks[x][y][z] = new Block(Block.AIR);
                }
            }
        }
    }

    // 辅助方法 (和之前一样)
    public Block getBlock(int x, int y, int z) {
        if (x < 0 || x >= SIZE || y < 0 || y >= HEIGHT || z < 0 || z >= SIZE) return null;
        return blocks[x][y][z];
    }

    // (World isSolid 会用到这个逻辑的变体，这里保留给 Chunk 内部使用)
    private boolean isVoid(int x, int y, int z) {
        if (x < 0 || x >= SIZE || y < 0 || y >= HEIGHT || z < 0 || z >= SIZE) return true;
        return !blocks[x][y][z].isActive();
    }

    // 新的 Mesh 构建方法，接收 World 以查询邻居
    public void rebuildMesh(World world) {
        mesh.clear();
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z < SIZE; z++) {
                    if (!blocks[x][y][z].isActive()) continue;

                    // 获取方块 ID，以便查询颜色
                    byte blockID = blocks[x][y][z].getId();
                    // 计算全局坐标
                    int worldX = chunkX * SIZE + x;
                    int worldY = y;
                    int worldZ = chunkZ * SIZE + z;

                    // 这里直接问 World：我这个面的邻居是实心的吗？
                    // 如果不是实心(是空气)，我就画出这个面
                    // 将 blockID 传给 addFace
                    if (!world.isSolid(worldX, worldY, worldZ + 1)) addFace(0, worldX, worldY, worldZ, blockID);
                    if (!world.isSolid(worldX, worldY, worldZ - 1)) addFace(1, worldX, worldY, worldZ, blockID);
                    if (!world.isSolid(worldX - 1, worldY, worldZ)) addFace(2, worldX, worldY, worldZ, blockID);
                    if (!world.isSolid(worldX + 1, worldY, worldZ)) addFace(3, worldX, worldY, worldZ, blockID);
                    if (!world.isSolid(worldX, worldY + 1, worldZ)) addFace(4, worldX, worldY, worldZ, blockID);
                    if (!world.isSolid(worldX, worldY - 1, worldZ)) addFace(5, worldX, worldY, worldZ, blockID);
                }
            }
        }
    }

    // --- 修改点：存 RenderFace 而不是 Vector3f[] ---
    private void addFace(int dir, float x, float y, float z, byte id) {
        // 调用 Block.getColor 获取正确的颜色 (例如：草的顶面绿，侧面褐)
        int color = Block.getColor(id, dir);
        mesh.add(new RenderFace(Block.getFaceVertices(dir, x, y, z), dir, color));
    }

    public List<RenderFace> getMesh() {
        return mesh;
    }

    // --- 新增：简单的数据结构用于传输渲染数据 ---
    public static class RenderFace {
        public Vector3f[] vertices;
        public Vector3f normal;
        public int color;

        public RenderFace(Vector3f[] vertices, int dirIndex, int color) {
            this.vertices = vertices;
            this.color = color;
            // 根据朝向索引快速生成法线向量 (简化计算)
            switch (dirIndex) {
                case 0:
                    this.normal = new Vector3f(0, 0, 1);
                    break;  // Front
                case 1:
                    this.normal = new Vector3f(0, 0, -1);
                    break; // Back
                case 2:
                    this.normal = new Vector3f(-1, 0, 0);
                    break; // Left
                case 3:
                    this.normal = new Vector3f(1, 0, 0);
                    break;  // Right
                case 4:
                    this.normal = new Vector3f(0, 1, 0);
                    break;  // Top
                case 5:
                    this.normal = new Vector3f(0, -1, 0);
                    break; // Bottom
            }
        }
    }
}