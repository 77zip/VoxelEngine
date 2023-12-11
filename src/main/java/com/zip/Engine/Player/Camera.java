package com.zip.Engine.Player;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    public Vector3f Position;
    public Vector3f Rotation;
    public Matrix4f Projection;
    public Matrix4f CullProjection;
    private Vector3f lookAt;
    private Vector3f up;

    //testing culling
    FrustumIntersection frustumInt;
    public Camera() {
        setPosition(0,0,0);
        setRotation(0,0,0);
        updateProjection(70, 800, 600);
        this.frustumInt = new FrustumIntersection();
    }
    public void updateProjection(int fov, int width, int height) {
        this.Projection = new Matrix4f();
        this.Projection.setPerspective((float)Math.toRadians(fov), (float)width/height, 0.001f, 1f);
        this.CullProjection = new Matrix4f();
        this.CullProjection.setPerspective((float)Math.toRadians(fov), (float)width/height, 0.001f, 1000f);

        //culling
    }
    //public boolean isVisible(int x, int y, int z, float radius) {
    //    return frustumInt.testSphere(x, y, z, radius);
    //}
    public boolean isVisible(int x, int y, int z, float scale) {
        float minX = x - scale;// / 2f;
        float minY = y - (scale*4);// / 2f;
        float minZ = z - scale;// / 2f;
        float maxX = x + scale;// / 2f;
        float maxY = y + (scale*4);// / 2f;
        float maxZ = z + scale;// / 2f;
        Vector3f min = new Vector3f(minX,minY,minZ);
        Vector3f max = new Vector3f(maxX,maxY,maxZ);
        return frustumInt.testAab(min,max);
    }
    //position
    public void setPosition(float x, float y, float z) {
        this.Position = new Vector3f(x,y,z);
    }
    public void setRotation(float x, float y, float z) {
        this.Rotation = new Vector3f(x,y,z);
    }
    public void updateFrustum() {
        Matrix4f tmp = new Matrix4f(this.CullProjection);
        //tmp.mul(this.getViewMatrix());
        if (this.frustumInt == null) {
            this.frustumInt = new FrustumIntersection();
        }
        //tmp.setPerspective((float)Math.toRadians(70), (float)800/600, 0.001f, 100f);
        tmp.rotateX(this.Rotation.x);
        tmp.rotateY(this.Rotation.y);
        tmp.rotateZ(this.Rotation.z);
        this.frustumInt.set(tmp);
    }

    //utility
    public Matrix4f getViewMatrix() {
        Vector3f pos = new Vector3f(this.Position).mul(-1,-1,-1).add(0,-0.7f,0);
        Vector3f rot = new Vector3f(this.Rotation);
        return new Matrix4f().rotateXYZ(rot).translate(pos.div(100,100,100));
    }

    public Matrix4f[] getMVP(Matrix4f Model) {
        return new Matrix4f[] {Model, this.getViewMatrix(), this.Projection};
    }
    public Vector3f getDirection(float offset) {
        Matrix4f rotationMatrix = new Matrix4f();
        //rotationMatrix.rotateX(Rotation.x);
        rotationMatrix.rotateY((float)Math.toRadians(offset));
        rotationMatrix.rotateZ(Rotation.z);

        Vector3f forwardDirection = new Vector3f(0.0f, 0.0f, 1.0f);
        rotationMatrix.transformDirection(forwardDirection);
        return forwardDirection.normalize();
    }
    public Vector3f getForwardDirection(float offset) {
        Matrix4f rotationMatrix = new Matrix4f();
        //rotationMatrix.rotateX(Rotation.x);
        rotationMatrix.rotateY(-Rotation.y + (float)Math.toRadians(offset));
        rotationMatrix.rotateZ(Rotation.z);

        Vector3f forwardDirection = new Vector3f(0.0f, 0.0f, 1.0f);
        rotationMatrix.transformDirection(forwardDirection);
        return forwardDirection.normalize();
    }

    public void setPosition(Vector3f position) {
        this.Position = new Vector3f(position);
    }
}
