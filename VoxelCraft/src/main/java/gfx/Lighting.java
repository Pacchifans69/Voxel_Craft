package gfx;

import render.math.Vector3f;

public class Lighting {
    // 修改光源方向：调高 Y 轴 (1.0 -> 1.8)，使其更接近头顶的太阳，减少过长的侧面阴影
    // x=0.4, z=0.5: 给一点角度，保证前面和右面依然比后面亮，方便辨识方向
    private static final Vector3f LIGHT_DIR = new Vector3f(0.4f, 1.8f, 0.5f).normalize();

    // 环境光基础强度 (范围 0.0 ~ 1.0)
    // 提高此值可以让暗部更明亮柔和
    private static final float MIN_BRIGHTNESS = 0.55f;

    /**
     * 计算 Half-Lambert 光照强度 (更加柔和，保留阴影细节)
     */
    public static float getShadingFactor(Vector3f normal) {
        float dot = normal.dot(LIGHT_DIR);

        // --- Half-Lambert 核心算法 ---
        // 将 [-1, 1] 的余弦值线性映射到 [0, 1]
        // 这样背光面(dot < 0) 也是有梯度的，不再是一片死黑
        float halfLambert = dot * 0.5f + 0.5f;

        // 二次幂曲线 (Square)：虽然线性映射好了，但有时太灰，平方一下能增加少许对比度，让亮部更亮，暗部更有层次
        // 你可以根据喜好保留或删除这行
        halfLambert = halfLambert * halfLambert;

        // 混合：最终亮度 = 基础环境光 + (可变光照范围 * 强度)
        // (1.0f - MIN_BRIGHTNESS) 确保亮度不会超过 1.0
        return MIN_BRIGHTNESS + halfLambert * (1.0f - MIN_BRIGHTNESS);
    }

    /**
     * 应用光照到颜色 (包含简单的 RGB 钳制保护)
     */
    public static int applyLighting(int hexColor, float factor) {
        int r = (hexColor >> 16) & 0xFF;
        int g = (hexColor >> 8) & 0xFF;
        int b = hexColor & 0xFF;

        r = (int) (r * factor);
        g = (int) (g * factor);
        b = (int) (b * factor);

        // 钳制 RGB，防止数值溢出变色
        if (r > 255) r = 255;
        if (g > 255) g = 255;
        if (b > 255) b = 255;

        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }
}