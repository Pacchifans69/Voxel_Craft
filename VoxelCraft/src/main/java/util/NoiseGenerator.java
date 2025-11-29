package util;

import java.util.Random;

public class NoiseGenerator {
    private final int[] p = new int[512];
    private final int[] permutation = new int[256];

    public NoiseGenerator() {
        this(new Random().nextLong());
    }

    public NoiseGenerator(long seed) {
        Random random = new Random(seed);

        // 初始化排列表
        for (int i = 0; i < 256; i++) {
            permutation[i] = i;
        }

        // 简单的洗牌算法
        for (int i = 0; i < 256; i++) {
            int j = random.nextInt(256);
            int temp = permutation[i];
            permutation[i] = permutation[j];
            permutation[j] = temp;
        }

        // 复制一份以处理溢出
        for (int i = 0; i < 512; i++) {
            p[i] = permutation[i % 256];
        }
    }

    public double noise(double x, double y, double z) {
        // 找到包含点的单位立方体
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        int Z = (int) Math.floor(z) & 255;

        // 找出立方体中的相对 x, y, z
        x -= Math.floor(x);
        y -= Math.floor(y);
        z -= Math.floor(z);

        // 计算缓和曲线 (Fade curves) 对于 x, y, z
        double u = fade(x);
        double v = fade(y);
        double w = fade(z);

        // 哈希每个立方体顶点的坐标
        int A = p[X] + Y, AA = p[A] + Z, AB = p[A + 1] + Z;
        int B = p[X + 1] + Y, BA = p[B] + Z, BB = p[B + 1] + Z;

        // 混合结果
        return lerp(w, lerp(v, lerp(u, grad(p[AA], x, y, z),
                                grad(p[BA], x - 1, y, z)),
                        lerp(u, grad(p[AB], x, y - 1, z),
                                grad(p[BB], x - 1, y - 1, z))),
                lerp(v, lerp(u, grad(p[AA + 1], x, y, z - 1),
                                grad(p[BA + 1], x - 1, y, z - 1)),
                        lerp(u, grad(p[AB + 1], x, y - 1, z - 1),
                                grad(p[BB + 1], x - 1, y - 1, z - 1))));
    }

    // 生成简单的 2D 噪声 (y通常用于高程图)
    public double noise2D(double x, double z) {
        return noise(x, 0, z);
    }

    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}