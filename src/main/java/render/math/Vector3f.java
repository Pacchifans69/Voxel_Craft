package render.math;

public class Vector3f {
    public float x, y, z;

    public Vector3f() {
        this(0.0f, 0.0f, 0.0f);
    }

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f add(Vector3f other) {
        return new Vector3f(x + other.x, y + other.y, z + other.z);
    }

    public Vector3f sub(Vector3f other) {
        return new Vector3f(x - other.x, y - other.y, z - other.z);
    }

    public Vector3f mul(float scalar) {
        return new Vector3f(x * scalar, y * scalar, z * scalar);
    }

    public Vector3f div(float scalar) {
        return new Vector3f(x / scalar, y / scalar, z / scalar);
    }

    public float dot(Vector3f v) {
        return x * v.x + y * v.y + z * v.z; 
    }

    public Vector3f cross(Vector3f v) {
        return new Vector3f(
          y * v.z - z * v.y,
          z * v.x - x * v.z,
          x * v.y - y * v.x
        );
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3f normalize() {
        float len = length();
        return len == 0 ? new Vector3f(0, 0, 0) : div(len);
    }

    public static Vector3f lerp(Vector3f a, Vector3f b, float t) {
        return a.add(b.sub(a).mul(t));
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f, %.2f)", x, y, z);
    }
}
