package render;

import gfx.Lighting;
import gfx.ZBuffer;
import render.math.Matrix4f;
import render.math.Vector3f;
import world.Chunk;
import world.World;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.List;

public class Renderer {
    private int width, height;
    private BufferedImage buffer;
    private int[] pixels;
    private ZBuffer zBuffer;
    private Matrix4f projectionMatrix;

    public Renderer(int width, int height) {
        this.width = width;
        this.height = height;
        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) buffer.getRaster().getDataBuffer()).getData();
        zBuffer = new ZBuffer(width, height);
        updateProjectionMatrix();
    }

    private void updateProjectionMatrix() {
        float fov = (float) Math.toRadians(70.0f);
        float aspectRatio = (float) width / height;
        // 近平面稍微调大一点点防止Z-fighting
        float zNear = 0.1f;
        float zFar = 1000.0f;
        this.projectionMatrix = Matrix4f.perspective(fov, aspectRatio, zNear, zFar);
    }

    public void render(Graphics g, World world, Camera camera) {
        // 天空色设为淡淡的蓝色
        clear(0xFF87CEEB);

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f mvp = projectionMatrix.mul(viewMatrix);
        float halfWidth = width * 0.5f;
        float halfHeight = height * 0.5f;

        for (Chunk chunk : world.getChunks()) {
            // 获取带法线的网格
            List<Chunk.RenderFace> mesh = chunk.getMesh();

            for (Chunk.RenderFace face : mesh) {

                // 1. 光照计算 (使用上一轮优化过的柔和光照)
                float shadeFactor = Lighting.getShadingFactor(face.normal);

                // 2. 颜色获取 [核心修改]
                // 之前：基于法线猜测颜色 if (normal.y > 0.5) ...
                // 现在：直接读取 Chunk 生成好的正确颜色
                int baseColor = face.color;

                // 应用光照
                int finalColor = Lighting.applyLighting(baseColor, shadeFactor);

                // 2. 顶点变换流水线
                Vertex[] verts = new Vertex[4];
                boolean safe = true;

                for (int i = 0; i < 4; i++) {
                    Vector3f ndc = mvp.transform(face.vertices[i]);
                    if (ndc.z < -1.0f || ndc.z > 1.0f) {
                        safe = false;
                        break;
                    }

                    int screenX = (int) ((ndc.x + 1.0f) * halfWidth);
                    // 屏幕Y坐标修正
                    int screenY = (int) ((1.0f - ndc.y) * halfHeight);
                    verts[i] = new Vertex(screenX, screenY, ndc.z);
                }

                if (!safe) continue;

                // 3. 绘制两个三角形并进行 [背面剔除 Optimization]
                // 顺序: 0-1-2 和 2-3-0
                drawTriangleOptimized(verts[0], verts[1], verts[2], finalColor);
                drawTriangleOptimized(verts[0], verts[2], verts[3], finalColor);
            }
        }
        g.drawImage(buffer, 0, 0, null);
    }

    private void clear(int color) {
        for (int i = 0; i < pixels.length; i++) pixels[i] = color;
        zBuffer.clear();
    }

    /**
     * 带背面剔除 (Back-face Culling) 的三角形绘制
     */
    private void drawTriangleOptimized(Vertex v1, Vertex v2, Vertex v3, int color) {
        // --- 核心优化: 背面剔除 ---
        // 计算三角形在屏幕空间的二维叉积 (z分量)
        // (x2-x1)(y3-y1) - (y2-y1)(x3-x1)
        float crossProduct = (v2.x - v1.x) * (v3.y - v1.y) - (v2.y - v1.y) * (v3.x - v1.x);

        // 如果叉积 < 0 (或 > 0，取决于坐标系和顶点绕序)，说明是背面
        // 通常 NDC 坐标系下，顺时针(CW) 可能为正，逆时针(CCW)为负
        // 按照我们的 Vertex 生成顺序，如果不渲染，请反转这个符号检测
        // 反转剔除逻辑 (如果顶面出现了，说明你的三角形定义是另一方向的绕序)
        if (crossProduct <= 0) {
            return;
        }

        // --- 以下是常规光栅化逻辑 ---
        int minX = Math.max(0, Math.min(v1.x, Math.min(v2.x, v3.x)));
        int maxX = Math.min(width - 1, Math.max(v1.x, Math.max(v2.x, v3.x)));
        int minY = Math.max(0, Math.min(v1.y, Math.min(v2.y, v3.y)));
        int maxY = Math.min(height - 1, Math.max(v1.y, Math.max(v2.y, v3.y)));

        float area = crossProduct; // 刚好这就是上面的叉积，就是面积

        // 简单的优化：如果面积太小，也不画
        if (area == 0) return;

        // 包围盒遍历
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                // 重心坐标算法优化：将公共项提取
                // P = (x,y)
                // W0 edge from v2 to v3
                float w0 = (v3.x - v2.x) * (y - v2.y) - (v3.y - v2.y) * (x - v2.x);
                // W1 edge from v3 to v1
                float w1 = (v1.x - v3.x) * (y - v3.y) - (v1.y - v3.y) * (x - v3.x);
                // W2 edge from v1 to v2
                float w2 = (v2.x - v1.x) * (y - v1.y) - (v2.y - v1.y) * (x - v1.x);

                // 检查是否在三角形内
                // 注意：如果剔除了背面，这里的符号应该是一致的，不需要都 check >=0
                // 这里的符号判断要和剔除方向一致
                if (w0 >= 0 && w1 >= 0 && w2 >= 0) {
                    w0 /= area;
                    w1 /= area;
                    w2 /= area;

                    float z = w0 * v1.z + w1 * v2.z + w2 * v3.z;
                    if (zBuffer.testAndSet(x, y, z)) {
                        pixels[x + y * width] = color;
                    }
                }
            }
        }
    }

    private static class Vertex {
        int x, y;
        float z;

        public Vertex(int x, int y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}