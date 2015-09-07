package main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.Iterator;

public class Main extends ApplicationAdapter {
    public static final int V_WIDTH = 720;
    public static final int V_HEIGHT = 1280;
    
    SpriteBatch batch;
    private Texture dropImage;
    private Texture backgroundImage;
    private Texture bucketImage;
    private Sound dropSound;
    private Music rainMusic;
    private OrthographicCamera camera;
    private Rectangle bucket;
    Vector3 touchPos;
    private Array<Rectangle> raindrops;
    private long lastDropTime;
    private BitmapFont font;
    private int score = 0;
    
    @Override
    public void create () {
        
        // create a new sprite batch
        batch = new SpriteBatch();
        
        // load the images for the droplet and the bucket, 64x64 pixels each
        backgroundImage = new Texture("background.jpg");
        dropImage = new Texture("droplet.png");
        bucketImage = new Texture("bucket.png");

        // load the drop sound effect and the rain background "music"
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

        // start the playback of the background music immediately
        rainMusic.setLooping(true);
        rainMusic.play();
        
        // Load the font for the score
        font = new BitmapFont();
        
        // Set up a virtual camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, V_WIDTH, V_HEIGHT);
        
        // load the bucket texture onto a rectangle object
        bucket = new Rectangle();
        bucket.width = 64f;
        bucket.height = 64f;
        bucket.x = (V_WIDTH - 64) / 2f;
        bucket.y = 20f;
        
        // Create a 3D vector to store mouse click location
        touchPos = new Vector3();
        
        // Generate a raindrop
        raindrops = new Array<Rectangle>();
        spawnRaindrop();
    }
    
    /**
     * Create a new raindrop
     */
    private void spawnRaindrop() {
        Rectangle raindrop = new Rectangle();
        raindrop.width = 64f;
        raindrop.height = 64f;
        raindrop.x = MathUtils.random(0, V_WIDTH - 64f);
        raindrop.y = V_HEIGHT;
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }

    @Override
    public void render () {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // update the camera once every frame
        camera.update();
        
        // draw stuff on the screen
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.disableBlending();
        batch.draw(backgroundImage, 0, 0);
        batch.enableBlending();
        batch.draw(bucketImage, bucket.x, bucket.y);
        for(Rectangle raindrop: raindrops) {
            batch.draw(dropImage, raindrop.x, raindrop.y);
        }
        font.draw(batch, "Score: " + score, 24, V_HEIGHT - 24);
        batch.end();
        
        // Drag/Move the bucket horizontally
        if(Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = touchPos.x - bucket.width / 2;
        }
        
        // Spawn a new raindrop every 1 second
        if(TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();
        
        // Move the raindrops and remove them if below the screen
        Iterator<Rectangle> iter = raindrops.iterator();
        while(iter.hasNext()) {
            Rectangle raindrop = iter.next();
            raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
            if(raindrop.y + raindrop.width < 0) {
                score -= 5;
                iter.remove();
            }
            // Check if raindrop has fallen into the bucket
            if(raindrop.overlaps(bucket)) {
                dropSound.play();
                score += 10;
                iter.remove();
            }
        }
        
        // Prevent the bucket from going off the screen
        if(bucket.x < 0) bucket.x = 0;
        if(bucket.x > V_WIDTH - bucket.width) bucket.x = V_WIDTH - bucket.width;
    }
    
    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, V_HEIGHT*width/ (float)height, V_HEIGHT);
        batch.setProjectionMatrix(camera.combined);
    }
    
    @Override
    public void dispose() {
        backgroundImage.dispose();
        dropImage.dispose();
        bucketImage.dispose();
        dropSound.dispose();
        rainMusic.dispose();
        batch.dispose();
    }
}
