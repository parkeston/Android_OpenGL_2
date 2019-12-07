package com.example.lab6.geometry;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.example.lab6.engine.GameRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Star
{
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec4 a_Color;"+
                    "varying vec4 v_Color;"+
                    "void main() {" +
                    "  gl_Position = uMVPMatrix*vPosition;" +
                    "v_Color = a_Color;"+
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 v_Color;" +
                    "void main() {" +
                    "  gl_FragColor = v_Color;" +
                    "}";

    private int mProgram;

    private float[] rotationAxis;
    private float rotationAngle;
    private float[] translation;



    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    private int vPMatrixHandle;
    private int positionHandle;
    private int colorHandle;

    private float[] modelMatrix = new float[16];
    private float[] mVPMatrix = new float[16];

    static final int COORDS_PER_VERTEX = 6;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private float[] coords = new float[]
    {
            -0.5f, 0.0f, 0.0f,      1.0f, 1.0f, 1.0f,
            0.0f, 0.5f, -0.15f,     1.0f, 0.0f, 1.0f,
            0.0f, 0.2f, 0.0f,       0.0f, 1.0f, 1.0f,
            -0.35f, 0.4f, 0.0f,     0.0f, 1.0f, 1.0f,
            0.0f, 0.5f, 0.15f,      1.0f, 0.0f, 1.0f,
            0.5f, 0.0f, 0.0f,       1.0f, 1.0f, 1.0f,
            0.35f, 0.4f, 0.0f,      0.0f, 1.0f, 1.0f,
            -0.75f, 0.7f, 0.0f,     1.0f, 1.0f, 1.0f,
            -0.2f, 0.7f, 0.0f,      0.0f, 1.0f, 1.0f,
            0.75f, 0.7f, 0.0f,      1.0f, 1.0f, 1.0f,
            0.22f, 0.7f, 0.0f,      0.0f, 1.0f, 1.0f,
            0.0f, 1.1f, 0.0f,        1.0f, 1.0f, 1.0f
    };

    private short drawOrder[] = {
            0,1,2,
            0,3,1,
            0,2,4,
            0,4,3,

            5,2,1,
            5,1,6,
            5,4,2,
            5,6,4,

            7,1,3,
            7,8,1,
            7,3,4,
            7,4,8,

            9,6,1,
            9,1,10,
            9,4,6,
            9,10,4,

            11,1,8,
            11,10,1,
            11,8,4,
            11,4,10, }; // order to draw vertices

    private final int vertexCount = coords.length/COORDS_PER_VERTEX;

    public Star()
    {
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(coords);
        vertexBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);


        int vertexShader = GameRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = GameRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);

        Matrix.setIdentityM(modelMatrix,0);
    }

    public void setRotationAxis(float[] rotationAxis) {
        this.rotationAxis = rotationAxis;
    }

    public void setRotationAngle(float rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public void setTranslation(float[] translation) {
        this.translation = translation;
    }

    public void draw(float[] vPMatrix)
    {
        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = rotationAngle * ((int) time);
        Matrix.setRotateM(modelMatrix, 0, angle, rotationAxis[0], rotationAxis[1], rotationAxis[2]);
        Matrix.translateM(modelMatrix,0,translation[0],translation[1],translation[2]);
        Matrix.rotateM(modelMatrix,0,angle,0,1,0);

        Matrix.multiplyMM(mVPMatrix,0,vPMatrix,0,modelMatrix,0);

        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        vertexBuffer.position(0);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(positionHandle, 3,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(positionHandle);

        // get handle to fragment shader's vColor member
        colorHandle = GLES20.glGetAttribLocation(mProgram, "a_Color");
        vertexBuffer.position(3);
        GLES20.glVertexAttribPointer(colorHandle,3,GLES20.GL_FLOAT,false,vertexStride,vertexBuffer);
        GLES20.glEnableVertexAttribArray(colorHandle);

        // get handle to shape's transformation matrix
        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mVPMatrix, 0);

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,drawOrder.length,GLES20.GL_UNSIGNED_SHORT,drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(colorHandle);
    }
}
