package com.example.lab6.geometry;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.example.lab6.engine.GameRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Tank {

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
                     0.220861f ,0.146243f ,-0.196792f,      0.3f, 1.0f, 0.0f,
                     0.500000f, -0.146243f ,-0.777076f,     1.0f, 1.0f, 1.0f,
                     0.053734f, 0.232617f, 0.489712f,       1.0f, 0.0f, 1.0f,
                     0.500000f ,-0.146243f, 0.777076f,      1.0f, 1.0f, 1.0f,
                     -0.220861f ,0.146243f ,-0.196792f,     0.3f, 1.0f, 0.0f,
                     -0.500000f, -0.146243f ,-0.777076f,    1.0f, 1.0f, 1.0f,
                     -0.053734f, 0.232617f, 0.489712f,      1.0f, 0.0f, 1.0f,
                     -0.500000f ,-0.146243f, 0.777076f,     1.0f, 1.0f, 1.0f,
                     0.500000f, 0.146243f, 0.777076f,       0.3f, 1.0f, 0.0f,
                     -0.500000f, 0.146243f, 0.777076f,      0.3f, 1.0f, 0.0f,
                     0.500000f ,0.146243f ,-0.777076f,      0.3f, 1.0f, 0.0f,
                     -0.500000f ,0.146243f ,-0.777076f,     0.3f, 1.0f, 0.0f,
                     0.053734f, 0.328372f, 0.489712f,       1.0f, 0.0f, 1.0f,
                     -0.053734f, 0.328372f, 0.489712f,      1.0f, 0.0f, 1.0f,
                     0.220861f ,0.414745f ,-0.196792f,      1.0f, 0.0f, 1.0f,
                     -0.220861f ,0.414745f ,-0.196792f,     1.0f, 0.0f, 1.0f,
                     0.220861f, 0.146243f, 0.489712f,       0.3f, 1.0f, 0.0f,
                     -0.220861f, 0.146243f, 0.489712f,      0.3f, 1.0f, 0.0f,
                     0.220861f, 0.414745f, 0.489712f,       1.0f, 0.0f, 1.0f,
                     -0.220861f, 0.414745f, 0.489712f,      1.0f, 0.0f, 1.0f,
                     0.053734f, 0.232617f, 1.162435f,       1.0f, 0.0f, 1.0f,
                     -0.053734f, 0.232617f, 1.162435f,      1.0f, 0.0f, 1.0f,
                     0.053734f, 0.328372f, 1.162435f,       1.0f, 0.0f, 1.0f,
                     -0.053734f, 0.328372f, 1.162435f,      1.0f, 0.0f, 1.0f,
            };

    private short drawOrder[] = {

            18, 16, 5,
            9, 8, 4,
            10, 6, 8,
            2, 8, 6,
            11, 4, 2,
            12, 2, 6,
            17, 10, 9,
            1, 9, 11,
            5, 10, 18,
            5, 11, 12,
            16, 19, 15,
            14, 23, 13,
            5, 15, 1,
            1, 19, 17,
            7, 17, 3,
            13, 20, 14,
            14, 18, 7,
            3, 19, 13,
            21, 24, 22,
            7, 24, 14,
            3, 22, 7,
            13, 21, 3,
            18, 20, 16,
            9,10, 8,
            10, 12, 6,
            2, 4, 8,
            11, 9, 4,
            12, 11, 2,
            17, 18, 10,
            1, 17, 9,
            5, 12, 10,
            5, 1, 11,
            16, 20, 19,
            14, 24, 23,
            5, 16, 15,
            1, 15, 19,
            7, 18, 17,
            13, 19, 20,
            14, 20, 18,
            3, 17, 19,
            21, 23, 24,
            7, 22, 24,
            3, 21, 22,
            13, 23, 21,
    }; // order to draw vertices

    public Tank()
    {

        for (int i =0;i<drawOrder.length;i++)
        {
            drawOrder[i]--;
        }
        ByteBuffer bb = ByteBuffer.allocateDirect(
                coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(coords);
        vertexBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);


        int vertexShader = GameRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = GameRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);

        Matrix.setIdentityM(modelMatrix,0);
    }

    public void draw(float[] vPMatrix)
    {
        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int) time);
        Matrix.setRotateM(modelMatrix, 0, angle, 0, 1, 0);
        //Matrix.translateM(modelMatrix,0,2,0,0);

        Matrix.multiplyMM(mVPMatrix,0,vPMatrix,0,modelMatrix,0);

        GLES20.glUseProgram(mProgram);


        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(positionHandle, 3,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);


        colorHandle = GLES20.glGetAttribLocation(mProgram, "a_Color");
        vertexBuffer.position(3);
        GLES20.glVertexAttribPointer(colorHandle,3,GLES20.GL_FLOAT,false,vertexStride,vertexBuffer);
        GLES20.glEnableVertexAttribArray(colorHandle);


        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mVPMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES,drawOrder.length,GLES20.GL_UNSIGNED_SHORT,drawListBuffer);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(colorHandle);
    }
}
