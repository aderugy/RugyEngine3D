package com.aderugy.rugyengine3d.core.gameobjects.components;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public class Transform {
    public static final Vector3f X_AXIS = new Vector3f(1, 0, 0);
    public static final Vector3f Y_AXIS = new Vector3f(0, 1, 0);
    public static final Vector3f Z_AXIS = new Vector3f(0, 0, 1);

    private final Matrix4f transformMatrix;

    public Transform() {
        this.transformMatrix = new Matrix4f();
    }

    public Transform(Matrix4f mat) {
        transformMatrix = mat;
    }

    public void scale(float factor) {
        transformMatrix.scale(factor);
    }

    public void scale(float factor, Vector3f axis) {
        float xFactor = axis == Transform.X_AXIS ? factor : 1;
        float yFactor = axis == Transform.Y_AXIS ? factor : 1;
        float zFactor = axis == Transform.Z_AXIS ? factor : 1;
        transformMatrix.scale(xFactor, yFactor, zFactor);
    }

    public void translate(Vector3f offset) {
        transformMatrix.translate(offset);
    }

    public void rotateDeg(float degrees, Vector3f axis) {
        transformMatrix.rotate(Math.toRadians(degrees), axis);
    }

    public void rotateRad(float radians, Vector3f axis) {
        transformMatrix.rotate(radians, axis);
    }

    public Matrix4f getTransformMatrix() {
        return transformMatrix;
    }

    public FloatBuffer getProjection() {
        FloatBuffer projectionBuffer = BufferUtils.createFloatBuffer(16);
        transformMatrix.get(projectionBuffer);
        return projectionBuffer;
    }
}
