/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.espersoftworks.shadertoy.watchface;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.espersoftworks.shadertoy.watchface.opengl.MyGLSurfaceView;
import com.espersoftworks.shadertoy.watchface.util.ShaderSpec;

public class OpenGLES20Activity extends Activity {
    private ShaderSpec spec = new ShaderSpec("Slipstream", R.raw.slipstream, new int[]{R.drawable.tex08, R.drawable.tex03, R.drawable.tex09});
    private GLSurfaceView mGLView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLView = new MyGLSurfaceView(this, spec.load(this));
        setContentView(mGLView);
    }
}
