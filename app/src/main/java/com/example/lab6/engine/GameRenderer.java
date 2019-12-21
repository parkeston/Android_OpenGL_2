package com.example.lab6.engine;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.lab6.geometry.Tank;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class GameRenderer implements GLSurfaceView.Renderer {

    private final float[] vPMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];


    private float ratioY;
    private float ratioX;

    private Tank tank;

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glFrontFace(GLES20.GL_FRONT);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        tank = new Tank();
    }

    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.setLookAtM(viewMatrix, 0, 0, 2, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        tank.draw(vPMatrix);
    }


    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        ratioY = 1.0f;
        ratioX = 1.0f;
        float left = -1;
        float right = 1;
        float bottom = -1;
        float top = 1;
        float near = 1.0f;
        float far = 8.0f;
        if (width > height) {
            ratioX = (float) width / height;
            left *= ratioX;
            right *= ratioX;
        } else {
            ratioY = (float) height / width;
            bottom *= ratioY;
            top *= ratioY;
        }

        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);
        GameView.left = left;
        GameView.right = right;
        GameView.top = top;
        GameView.bottom = bottom;
    }

    public static int loadShader(int type, String shaderCode){

        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
