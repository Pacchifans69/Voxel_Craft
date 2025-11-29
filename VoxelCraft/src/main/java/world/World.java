package world;

import java.util.ArrayList;
import java.util.List;

public class World {
    // 改用二维数组存储，方便根据坐标快速查找 (假设 World Size 是 8x8)
    private static final int WORLD_SIZE = 8;
    private Chunk[][] chunkMap;
    private List<Chunk> chunkList; // 仅用于 Render 遍历

    public World() {
        chunkMap = new Chunk[WORLD_SIZE][WORLD_SIZE];
        chunkList = new ArrayList<>();

        // 第一步：只生成 Chunk 数据 (Blocks)，不生成 Mesh
        for (int x = 0; x < WORLD_SIZE; x++) {
            for (int z = 0; z < WORLD_SIZE; z++) {
                Chunk c = new Chunk(x, z);
                chunkMap[x][z] = c;
                chunkList.add(c);
            }
        }

        // 第二步：所有 Chunk 数据就绪后，通知它们构建 Mesh (此时可以查找邻居了)
        for (Chunk c : chunkList) {
            c.rebuildMesh(this); // 把自己传进去
        }
    }

    public List<Chunk> getChunks() {
        return chunkList;
    }

    // --- 新增：碰撞检测核心查询接口 ---

    /**
     * 查询世界坐标 (x,y,z) 处是否是实体方块
     * 碰撞箱判定会大量调用此方法
     */
    public boolean isSolid(float x, float y, float z) {
        // 1. 边界检查 (World bounds)
        int blockX = (int) Math.floor(x);
        int blockY = (int) Math.floor(y);
        int blockZ = (int) Math.floor(z);

        // 如果超出世界高度，不算碰撞（允许跳出地图上方）
        if (blockY < 0 || blockY >= Chunk.HEIGHT) return false;

        // 计算所在的 Chunk 索引
        int chunkX = blockX / Chunk.SIZE;
        int chunkZ = blockZ / Chunk.SIZE;

        // 计算在 Chunk 内的局部坐标
        int localX = blockX % Chunk.SIZE;
        int localZ = blockZ % Chunk.SIZE;

        // 处理负坐标 (Java % 负数会有问题，这里简单处理：如果是负数认为没有方块)
        if (blockX < 0 || blockZ < 0) return false;

        // 检查 Chunk 是否存在
        if (chunkX >= WORLD_SIZE || chunkZ >= WORLD_SIZE) return false;

        Chunk chunk = chunkMap[chunkX][chunkZ];
        if (chunk == null) return false;

        // 获取方块 (需要在 Chunk 类加一个 getBlock 方法)
        Block block = chunk.getBlock(localX, blockY, localZ);

        // 只有非空气方块才算 Solid
        return block != null && block.getId() != Block.AIR;
    }
    public void update() {
        // TODO: 暂时留空
    }
}