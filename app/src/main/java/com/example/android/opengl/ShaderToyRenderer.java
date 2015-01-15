package com.example.android.opengl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Date;

public class ShaderToyRenderer implements GLSurfaceView.Renderer {
    static final int COORDS_PER_VERTEX = 3;
    static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;

    static final String fragmentSrcHeader =
            "precision mediump float;" +
            "uniform vec3      iResolution;" +           // viewport resolution (in pixels)
            "uniform float     iGlobalTime;" +           // shader playback time (in seconds)
            "uniform float     iChannelTime[4];" +       // channel playback time (in seconds)
            "uniform vec3      iChannelResolution[4];" + // channel resolution (in pixels)
            "uniform vec4      iMouse;" +                // mouse pixel coords. xy: current (if MLB down), zw: click
            "uniform sampler2D iChannel0;" +             // input channels x4. TODO: cube maps
            "uniform sampler2D iChannel1;" +
            "uniform sampler2D iChannel2;" +
            "uniform sampler2D iChannel3;" +
            "uniform vec4      iDate;" +                 // (year, month, day, time in seconds)
            "uniform float     iSampleRate;";            // sound sample rate (i.e., 44100)

    private Context context;

    private int program;
    private FloatBuffer vertexBuffer;
    private ShortBuffer drawOrderBuffer;
    private int drawOrderLength;
    private String vertexShaderSrc;
    private String fragmentShaderSrc;
    private int[] textures = new int[4];

    private long startTime;  // time since epoch that we started.
    private int screenWidth, screenHeight;
    private float touchX, touchY;  // last screen pos that was touched

    // Vertex shader inputs.
    private int vPosition;  // vec3 the vertex position

    // ShaderToy fragment shader inputs:
    private int iResolution;
    private int iGlobalTime;
    private int iChannelTime;
    private int iChannelResolution;
    private int iMouse;
    private int[] iChannel = new int[4];
    private int iDate;
    private int iSampleRate;

    public ShaderToyRenderer(Context context, String vSrc, String fSrc) {
        this.context = context;

        startTime = new Date().getTime();
        vertexShaderSrc = vSrc;
        fragmentShaderSrc = fragmentSrcHeader + fSrc;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        program = GLUtils.programFromSrc(vertexShaderSrc, fragmentShaderSrc);

        vPosition = GLES20.glGetAttribLocation(program, "vPosition");
        iResolution = GLES20.glGetUniformLocation(program, "iResolution");
        iGlobalTime = GLES20.glGetUniformLocation(program, "iGlobalTime");
        iChannelTime = GLES20.glGetUniformLocation(program, "iChannelTime");
        iChannelResolution = GLES20.glGetUniformLocation(program, "iChannelResolution");
        iMouse = GLES20.glGetUniformLocation(program, "iMouse");
        for (int i = 0; i < 3; ++i)
            iChannel[i] = GLES20.glGetUniformLocation(program, "iChannel" + Integer.toString(i));
        iDate = GLES20.glGetUniformLocation(program, "iDate");
        iSampleRate = GLES20.glGetUniformLocation(program, "iSampleRate");

        GLES20.glGenTextures(4, textures, 0);

        final float squareCoords[] = {
                -1.0f, -1.0f, 0.0f,
                -1.0f, 1.0f, 0.0f,
                1.0f, 1.0f, 0.0f,
                1.0f, -1.0f, 0.0f,
        };
        final short drawOrder[] = {0, 1, 2, 0, 2, 3};
        vertexBuffer = createBuffer(squareCoords);
        drawOrderBuffer = createBuffer(drawOrder);
        drawOrderLength = drawOrder.length;

        GLUtils.loadGLTexture(context, R.drawable.tex16, textures[0]);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(program);

        for (int i = 0; i < 3; ++i) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i]);
        }

        setShaderVariables();
        drawVertexBuffer(vertexBuffer, drawOrderBuffer, drawOrderLength);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        screenWidth = width;
        screenHeight = height;
    }

    public void onTouchEvent(float x, float y) {
        touchX = x;
        touchY = y;
    }

    public static FloatBuffer createBuffer(float[] array) {
        ByteBuffer bb = ByteBuffer.allocateDirect(array.length * 4);  // 4 bytes per float
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = bb.asFloatBuffer();
        buffer.put(array);
        buffer.position(0);
        return buffer;
    }

    public static ShortBuffer createBuffer(short[] array) {
        ByteBuffer bb = ByteBuffer.allocateDirect(array.length * 2);  // 2 bytes per short
        bb.order(ByteOrder.nativeOrder());
        ShortBuffer buffer = bb.asShortBuffer();
        buffer.put(array);
        buffer.position(0);
        return buffer;
    }

    public void drawVertexBuffer(FloatBuffer vertexBuffer,
                                 ShortBuffer drawOrderBuffer,
                                 int drawOrderLength) {
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(
            vPosition, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, vertexBuffer);
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES, drawOrderLength,
            GLES20.GL_UNSIGNED_SHORT, drawOrderBuffer);
        GLES20.glDisableVertexAttribArray(vPosition);
    }

    private void setShaderVariables() {
        GLES20.glUniform3f(iResolution, screenWidth, screenHeight,
                (float)screenWidth / (float)screenHeight);

        Date date = new Date();
        float time = (float) (date.getTime() - startTime) / 1000.0f;
        GLES20.glUniform1f(iGlobalTime, time);
        GLES20.glUniform4f(iChannelTime, time, time, time, time);  // ???
        GLES20.glUniform3f(iChannelResolution, 0.0f, 0.0f, 0.0f);  // ???
        for (int i = 0; i < 3; ++i)
            GLES20.glUniform1i(iChannel[i], i);
        GLES20.glUniform4f(iMouse, touchX, touchY, touchX, touchY);
        GLES20.glUniform4f(iDate, date.getYear(), date.getMonth(), date.getDate(),
                date.getHours() * 24 * 60 + date.getMinutes() * 60 + date.getSeconds()); // ???
        GLES20.glUniform1f(iSampleRate, 44000.0f);
    }
}
