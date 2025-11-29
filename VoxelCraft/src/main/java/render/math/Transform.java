package render.math;

public class Transform {
    private Vector3f position;
    private Vector3f rotation;
    private Vector3f scale;

    public Transform() {
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = new Vector3f(1, 1, 1);
    }

    public Matrix4f getMatrix() {
        Matrix4f translation = Matrix4f.translation(position.x, position.y, position.z);
        Matrix4f rotX = Matrix4f.rotationX(rotation.x);
        Matrix4f rotY = Matrix4f.rotationY(rotation.y);
        Matrix4f rotZ = Matrix4f.rotationZ(rotation.z);
        Matrix4f scaleMat = Matrix4f.scale(scale.x, scale.y, scale.z);

        // 组合顺序：平移 * 旋转Z * 旋转Y * 旋转X * 缩放
        return translation.mul(rotZ).mul(rotY).mul(rotX).mul(scaleMat);
    }


    public Vector3f getPosition() { return position; }
    public void setPosition(Vector3f pos) { this.position = pos; }

    public Vector3f getRotation() { return rotation; }
    public void setRotation(Vector3f rot) { this.rotation = rot; }

    public Vector3f getScale() { return scale; }
    public void setScale(Vector3f scale) { this.scale = scale; }
}
