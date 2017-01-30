package com.espersoftworks.shadertoy.watchface.util;

import android.content.Context;

import com.espersoftworks.shadertoy.watchface.opengl.ShaderToyRenderer;

public class ShaderSpec {
    private String name;
    private int fragmentSrcResource;
    private int[] textureResources;

    public ShaderSpec(String name, int fragmentSrcResource, int[] textureResources) {
        this.name = name;
        this.fragmentSrcResource = fragmentSrcResource;
        this.textureResources = textureResources;
    }

    public ShaderToyRenderer.Shader load(Context context) {
        String fragmentSrc = GLUtils.loadText(context, fragmentSrcResource);
        return new ShaderToyRenderer.Shader(fragmentSrc, textureResources);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
