/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package terrain;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import utils.Cube;

/**
 *
 * @author S.S.Iyer
 */
public class CubeShader implements Shader {
    ShaderProgram shaderProgram;
    Camera camera;
    RenderContext context;
    int u_projViewTrans;
    int u_worldTrans;
    int u_texture;
    
    @Override
    public void init() {
        String vertexShader = "attribute vec3 a_position;\n" +
            "attribute vec2 a_texCoord0;\n" +
            "uniform mat4 u_worldTrans;\n" +
            "uniform mat4 u_projViewTrans;\n" +
            "varying vec2 v_texCoords;\n" +
            "void main() {\n" +
            "    gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0);\n" +
            "	 v_texCoords = a_texCoord0;\n" +
            "}";
        String fragmentShader = "#ifdef GL_ES \n" +
            "precision mediump float;\n" +
            "#endif\n" +
            "uniform sampler2D u_texture;\n" +
            "varying vec3 v_position;\n" +
            "varying vec2 v_texCoords;\n" +
            "void main() {\n" +
            "	gl_FragColor = texture2D(u_texture, v_texCoords);\n" +
            "}";
        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        if (!shaderProgram.isCompiled())
            throw new GdxRuntimeException(shaderProgram.getLog());
        u_projViewTrans = shaderProgram.getUniformLocation("u_projViewTrans");
        u_worldTrans = shaderProgram.getUniformLocation("u_worldTrans");
        u_texture = shaderProgram.getUniformLocation("u_texture");
    }

    @Override
    public int compareTo(Shader other) {
        return 0;
    }

    @Override
    public boolean canRender(Renderable instance) {
        return true;
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.camera = camera;
        this.context = context;
        shaderProgram.begin();
        shaderProgram.setUniformMatrix(u_projViewTrans, camera.combined);
        context.setDepthTest(GL20.GL_LEQUAL);
        context.setCullFace(GL20.GL_BACK);
        if(Cube.texture != null)
            shaderProgram.setUniformi(u_texture, context.textureBinder.bind(Cube.texture));
    }

    @Override
    public void render(Renderable renderable) {
        shaderProgram.setUniformMatrix(u_worldTrans, renderable.worldTransform);
        renderable.mesh.render(shaderProgram,
            renderable.primitiveType,
            renderable.meshPartOffset,
            renderable.meshPartSize);
    }

    @Override
    public void end() {
        shaderProgram.end();
    }

    @Override
    public void dispose() {
        shaderProgram.dispose();
    }
    
}