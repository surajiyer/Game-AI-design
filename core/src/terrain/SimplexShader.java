/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 *
 * @author S.S.Iyer
 */
public class SimplexShader implements Shader {
    ShaderProgram shaderProgram;
    Camera camera;
    RenderContext context;
    int u_projViewTrans;
    int u_worldTrans;
    int u_texture;
    
    @Override
    public void init() {
        String vert = Gdx.files.internal("vertex.glsl").readString();//DefaultShader.getDefaultVertexShader();
        String frag = Gdx.files.internal("fragment.glsl").readString();//DefaultShader.getDefaultFragmentShader();
        shaderProgram = new ShaderProgram(vert, frag);
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
    }

    @Override
    public void render(Renderable renderable) {
        shaderProgram.setUniformMatrix(u_worldTrans, renderable.worldTransform);
        shaderProgram.setUniformi(u_texture, context.textureBinder.bind((Texture) renderable.userData));
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
