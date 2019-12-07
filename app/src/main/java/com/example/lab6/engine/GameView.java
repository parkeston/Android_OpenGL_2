package com.example.lab6.engine;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class GameView extends GLSurfaceView {

    private GameRenderer renderer;

    public static float left;
    public static float right;
    public static float top;
    public static float bottom;

    public GameView(Context context){
        super(context);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context,attrs);
        init(context);
    }

    private void init(Context context)
    {
        setEGLContextClientVersion(2);

        renderer = new GameRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);
        // Render the view only when there is a change in the drawing data
    }
}
