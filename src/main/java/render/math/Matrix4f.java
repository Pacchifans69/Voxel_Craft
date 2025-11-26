package render.math;

public class Matrix4f {
    public float[][] m = new float[4][4];

    //构造方法和单位矩阵
    public Matrix4f() {
        identity();
    }

    public Matrix4f identity() {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                m[i][j] = (i == j) ? 1f : 0f;
        return this;
    }

    // 矩阵乘法
    public Matrix4f mul(Matrix4f other) {
        Matrix4f result = new Matrix4f();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.m[i][j] =
                        m[i][0] * other.m[0][j] +
                        m[i][1] * other.m[1][j] +
                        m[i][2] * other.m[2][j] +
                        m[i][3] * other.m[3][j];
            }
        }
        return result;
    }

    //旋转矩阵
    public static Matrix4f rotationX(float angle) {
        Matrix4f mat = new Matrix4f();
        float c = (float)Math.cos(angle);
        float s = (float)Math.sin(angle);
        mat.m[1][1] = c;
        mat.m[1][2] = -s;
        mat.m[2][1] = s;
        mat.m[2][2] = c;
        return mat;
    }

    public static Matrix4f rotationY(float angle) {
        Matrix4f mat = new Matrix4f();
        float c = (float)Math.cos(angle);
        float s = (float)Math.sin(angle);
        mat.m[0][0] = c;
        mat.m[0][2] = s;
        mat.m[2][0] = -s;
        mat.m[2][2] = c;
        return mat;
    }

    public static Matrix4f rotationZ(float angle) {
        Matrix4f mat = new Matrix4f();
        float c = (float)Math.cos(angle);
        float s = (float)Math.sin(angle);
        mat.m[0][0] = c;
        mat.m[0][1] = -s;
        mat.m[1][0] = s;
        mat.m[1][1] = c;
        return mat;
    }

    //平移 / 缩放矩阵
    public static Matrix4f translation(float x, float y, float z) {
        Matrix4f mat = new Matrix4f();
        mat.m[0][3] = x;
        mat.m[1][3] = y;
        mat.m[2][3] = z;
        return mat;
    }

    public static Matrix4f scale(float sx, float sy, float sz) {
        Matrix4f mat = new Matrix4f();
        mat.m[0][0] = sx;
        mat.m[1][1] = sy;
        mat.m[2][2] = sz;
        return mat;
    }

    //透视投影矩阵
    public static Matrix4f perspective(float fov, float aspect, float near, float far) {
        Matrix4f mat = new Matrix4f();
        float f = 1.0f / (float)Math.tan(fov / 2.0f);
        mat.m[0][0] = f / aspect;
        mat.m[1][1] = f;
        mat.m[2][2] = (far + near) / (near - far);
        mat.m[2][3] = (2 * far * near) / (near - far);
        mat.m[3][2] = -1f;
        mat.m[3][3] = 0f;
        return mat;
    }

    //向量变换
    public Vector3f transform(Vector3f v) {
        float x = v.x * m[0][0] + v.y * m[0][1] + v.z * m[0][2] + m[0][3];
        float y = v.x * m[1][0] + v.y * m[1][1] + v.z * m[1][2] + m[1][3];
        float z = v.x * m[2][0] + v.y * m[2][1] + v.z * m[2][2] + m[2][3];
        float w = v.x * m[3][0] + v.y * m[3][1] + v.z * m[3][2] + m[3][3];

        if (w != 0f) {
            x /= w; y /= w; z /= w;
        }
        return new Vector3f(x, y, z);
    }
}
