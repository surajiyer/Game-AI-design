/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import mechanics.GlobalState;

/**
 * Adapted from code posted on StackOverflow http://stackoverflow.com/questions/21884805/libgdx-0-9-9-apply-cubemap-in-environment
 * But heavily modified
 * @author S.S.Iyer
 */
public class EnvironmentCubeMap implements Disposable{

    public final Pixmap[] data = new Pixmap[6];  
    protected ShaderProgram shader;

    public float rotationSpeed = 1f;
    private float rotation = 0f;
    protected int u_worldTrans;
    protected int fogColour;
    protected Mesh quad;
    private Matrix4 worldTrans;
    private Quaternion q;

    protected String vertexShader = " attribute vec3 a_position; \n"+
            " attribute vec3 a_normal; \n"+
            " attribute vec2 a_texCoord0; \n"+          
            " uniform mat4 u_worldTrans; \n"+                   
            " varying vec2 v_texCoord0; \n"+
            " varying vec3 v_cubeMapUV; \n"+
            " void main() { \n"+
            "     v_texCoord0 = a_texCoord0;     \n"+
            "     vec4 g_position = u_worldTrans * vec4(a_position, 1.0); \n"+
            "     v_cubeMapUV = normalize(g_position.xyz); \n"+
            "     gl_Position = vec4(a_position, 1.0); \n"+
            " } \n";

    protected String fragmentShader = "#ifdef GL_ES \n"+
            " precision mediump float; \n"+
            " #endif \n"+           
            " uniform samplerCube u_environmentCubemap; \n"+
            " const float lowerLimit = 0.0;\n"+
            " const float upperLimit = 0.64;\n"+
            " uniform vec4 fogColour; \n"+
            " varying vec2 v_texCoord0; \n"+
            " varying vec3 v_cubeMapUV; \n"+
            " void main() {      \n"+
            "   vec4 finalColour = texture(u_environmentCubemap, v_cubeMapUV);\n"+
            "   float factor = (v_cubeMapUV.y - lowerLimit) / (upperLimit - lowerLimit);\n"+
            "   factor = clamp(factor, 0.0, 1.0);\n"+
            "   gl_FragColor = mix(fogColour, finalColour, factor);\n"+
            " } \n";

    public String getDefaultVertexShader(){
        return vertexShader;
    }

    public String getDefaultFragmentShader(){
        return fragmentShader;
    }

    public EnvironmentCubeMap (Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, 
            Pixmap negativeY, Pixmap positiveZ, Pixmap negativeZ) {
        data[0]=positiveX; data[1]=negativeX;
        data[2]=positiveY; data[3]=negativeY;
        data[4]=positiveZ; data[5]=negativeZ;
        init();   
    }

    public EnvironmentCubeMap (FileHandle positiveX, FileHandle negativeX, FileHandle positiveY,
            FileHandle negativeY, FileHandle positiveZ, FileHandle negativeZ) {
        this(new Pixmap(positiveX), new Pixmap(negativeX), new Pixmap(positiveY), 
                new Pixmap(negativeY), new Pixmap(positiveZ), new Pixmap(negativeZ));
    }

    //IF ALL SIX SIDES ARE REPRESENTED IN ONE IMAGE
    public EnvironmentCubeMap (Pixmap cubemap) {        
        int w = cubemap.getWidth();
        int h = cubemap.getHeight();
        for(int i=0; i<6; i++) data[i] = new Pixmap(w/4, h/3, Format.RGB888);
        for(int x=0; x<w; x++)
            for(int y=0; y<h; y++){
                //-X
                if(x>=0 && x<=w/4 && y>=h/3 && y<=h*2/3) 
                    data[1].drawPixel(x, y-h/3, cubemap.getPixel(x, y));
                //+Y
                if(x>=w/4 && x<=w/2 && y>=0 && y<=h/3) 
                    data[2].drawPixel(x-w/4, y, cubemap.getPixel(x, y));
                //+Z
                if(x>=w/4 && x<=w/2 && y>=h/3 && y<=h*2/3) 
                    data[4].drawPixel(x-w/4, y-h/3, cubemap.getPixel(x, y));
                //-Y
                if(x>=w/4 && x<=w/2 && y>=h*2/3 && y<=h) 
                    data[3].drawPixel(x-w/4, y-h*2/3, cubemap.getPixel(x, y));
                //+X
                if(x>=w/2 && x<=w*3/4 && y>=h/3 && y<=h*2/3) 
                    data[0].drawPixel(x-w/2, y-h/3, cubemap.getPixel(x, y));
                //-Z
                if(x>=w*3/4 && x<=w && y>=h/3 && y<=h*2/3) 
                    data[5].drawPixel(x-w*3/4, y-h/3, cubemap.getPixel(x, y));
            }
        cubemap.dispose();
        init();     
    }

    private void init(){        
        shader = new ShaderProgram(vertexShader, fragmentShader);
        if (!shader.isCompiled())
            throw new GdxRuntimeException(shader.getLog());
        
        u_worldTrans = shader.getUniformLocation("u_worldTrans");
        fogColour = shader.getUniformLocation("fogColour");
        quad = createQuad();      
        worldTrans = new Matrix4();  
        q = new Quaternion();
        
        initCubemap();
    }
    
    public void reset() {
        worldTrans = new Matrix4();  
        q = new Quaternion();
    }

    private void initCubemap(){
        // Bind cube map
        Gdx.gl20.glBindTexture(GL20.GL_TEXTURE_CUBE_MAP, 1);
        Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL20.GL_RGB, data[0].getWidth(), data[0].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[0].getPixels());
        Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL20.GL_RGB, data[1].getWidth(), data[1].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[1].getPixels());

        Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GL20.GL_RGB, data[2].getWidth(), data[2].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[2].getPixels());
        Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GL20.GL_RGB, data[3].getWidth(), data[3].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[3].getPixels());

        Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GL20.GL_RGB, data[4].getWidth(), data[4].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[4].getPixels());
        Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GL20.GL_RGB, data[5].getWidth(), data[5].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[5].getPixels());

        //Gdx.gl20.glGenerateMipmap(GL20.GL_TEXTURE_CUBE_MAP);
        //Gdx.gl20.glTexParameteri(GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR);

        Gdx.gl20.glTexParameteri ( GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_MIN_FILTER,GL20.GL_LINEAR_MIPMAP_LINEAR );     
        Gdx.gl20.glTexParameteri ( GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_MAG_FILTER,GL20.GL_LINEAR );
        Gdx.gl20.glTexParameteri ( GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE );
        Gdx.gl20.glTexParameteri ( GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE );   

        Gdx.gl20.glGenerateMipmap(GL20.GL_TEXTURE_CUBE_MAP);
    }

    public void render(Camera camera) {
        worldTrans.idt();
        camera.view.getRotation(q, true);
        q.conjugate();
        worldTrans.rotate(q);
//        rotation += (rotationSpeed * Gdx.graphics.getDeltaTime());
//        rotation %= 360;
//        worldTrans.rotate(Vector3.Y, rotation);

        shader.begin();     
        shader.setUniformMatrix(u_worldTrans, worldTrans.translate(0, 0, -1));
        shader.setUniform4fv(fogColour, GlobalState.fogColour, 0, 4);
        quad.render(shader, GL20.GL_TRIANGLES);
        shader.end();
    }

    public Mesh createQuad(){
        Mesh mesh = new Mesh(true, 4, 6, VertexAttribute.Position(), 
                VertexAttribute.ColorUnpacked(), VertexAttribute.TexCoords(0));
        mesh.setVertices(new float[] 
        {-1f, -1f, 0, 1, 1, 1, 1, 0, 1,
        1f, -1f, 0, 1, 1, 1, 1, 1, 1,
        1f, 1f, 0, 1, 1, 1, 1, 1, 0,
        -1f, 1f, 0, 1, 1, 1, 1, 0, 0});
        mesh.setIndices(new short[] {0, 1, 2, 2, 3, 0});
        return mesh;
    }
    
    public void setScale(float scale) {
        if(worldTrans != null) {
            worldTrans.scl(scale / worldTrans.getScaleX());
        }
    }

    @Override
    public void dispose() {
        shader.dispose();
        quad.dispose();
        for(int i=0; i<6; i++) 
            data[i].dispose();
    }

}
