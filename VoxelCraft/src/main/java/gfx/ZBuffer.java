package gfx;

import java.util.Arrays;

public class ZBuffer {
    private float[] buffer;
    private int width, height;

    public ZBuffer(int width, int height) {
        this.width = width;
        this.height = height;
        this.buffer = new float[width * height];
    }

    public void clear() {
        // 用一个极大值填充，代表“无限远”
        Arrays.fill(buffer, Float.MAX_VALUE);
    }

    /**
     * 深度测试与写入
     *
     * @return 如果新的深度值 z 小于当前缓冲值（更近），则更新缓冲并返回 true（表示可以绘制该像素）
     */
    public boolean testAndSet(int x, int y, float z) {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;

        int index = x + y * width;
        if (z < buffer[index]) {
            buffer[index] = z;
            return true;
        }
        return false;
    }
}