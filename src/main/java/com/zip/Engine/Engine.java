package com.zip.Engine;

import com.zip.Engine.World.Block;
import com.zip.Engine.World.Model;
import com.zip.Engine.World.World;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.EXTFramebufferObject.glGenerateMipmapEXT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBEasyFont.stb_easy_font_print;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Engine {
    Render render;
    int size = 20; //earth would be 2,504,687 chunks
    int seed = 69;
    World world = new World(seed);
    float c = 0;
    float vx = 0;

    //deltaTime variables
    long deltaTime = 0;
    long lastTimeDelta = System.currentTimeMillis();
    long lastTime = System.currentTimeMillis();
    int frames = 0;
    int fps = 0;
    boolean fullscreenToggle = false;
    boolean canJump = false;
    //settings
    int renderDistance = 5;
    Vector3f sunRotation = new Vector3f(0,0,0);
    //TODO
    //devide world coordinates to 1000 to support higher render distance and depth resolution


    boolean fullscreen = false;
    //buffers
    FloatBuffer fb = BufferUtils.createFloatBuffer(16);
    public HashMap<Vector2i, Integer> vertexMap = new HashMap<>();
    public HashMap<Vector2i, Integer> bufferMap = new HashMap<>();
    private class Timer {
        long start = 0;
        long end = 0;
        public void start() {
            this.start = System.currentTimeMillis();
        }
        public long stop(String title) {
            this.end = System.currentTimeMillis();
            return (this.end-this.start);
        }
    }
    Timer timer = new Timer();
    final Object lock = new Object();
    List<Vector2i> pendingChunks = new ArrayList<>();
    List<Vector2i> dirtyChunks = new ArrayList<>();
    int skyboxID;
    int skyboxVID;
    public void drawMultiBuffer(int count, FloatBuffer[] buffers) {
        int size = 0;
        for (int i = 0; i < count; i++) {
            size += buffers[i].capacity();
        }
        FloatBuffer res = BufferUtils.createFloatBuffer(size);
        int[] starts = new int[count];
        int[] sizes = new int[count];
        int offset = 0;
        for (int i = 0; i < count; i++) {
            FloatBuffer current = buffers[i].flip();
            starts[i] = offset;
            sizes[i] = current.capacity()/9;
            offset += current.capacity();
        }
        res.flip();
        glBindVertexArray(69420);
        fb.clear();
        glMultiDrawArrays(GL_TRIANGLE_FAN, starts, sizes);
    }
    HashMap<Vector2i, Block[][][]> finishedChunks;
    public void loop() {
        glClearColor(0.5f,0.5f,0.5f, 1.0f);
        glColor3f(169f / 255f, 183f / 255f, 198f / 255f); // Text color
        render.camera.Position.y = 40;
        Thread.currentThread().setPriority(10);
        while ( !glfwWindowShouldClose(render.window) ) {
            //deltaTime
            Timer timer2 = new Timer();
            timer2.start();

            //main
            c -= 0.1f;

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            vx += 0.098f*(deltaTime/1000f);
            //player.Position.add(new Vector3f(0,vx, 0));

            Vector3f playerMovement = new Vector3f(0,0,0);
            //keyboard movement
            float speed = 1.6f * (deltaTime/1000f);
            if (keys[GLFW_KEY_LEFT_SHIFT]) {
                speed *= 5;
            }
            if (keys[GLFW_KEY_SPACE]) {
                playerMovement.add(new Vector3f(0,-speed,0));
            }
            if (keys[GLFW_KEY_LEFT_CONTROL]) {
                playerMovement.add(new Vector3f(0,speed,0));
            }

            if (keys[GLFW_KEY_W]) {
                Vector3f cameraDirection = render.camera.getForwardDirection(0f);
                playerMovement.add(cameraDirection.mul(speed*2));
            }
            if (keys[GLFW_KEY_A]) {
                Vector3f cameraDirection = render.camera.getForwardDirection(90f);
                playerMovement.add(cameraDirection.mul(speed));
            }
            if (keys[GLFW_KEY_S]) {
                Vector3f cameraDirection = render.camera.getForwardDirection(180f);
                playerMovement.add(cameraDirection.mul(speed));
            }
            if (keys[GLFW_KEY_D]) {
                Vector3f cameraDirection = render.camera.getForwardDirection(-90f);
                playerMovement.add(cameraDirection.mul(speed));
            }

            //update camera
            render.camera.Position.sub(playerMovement);
            //player.Position = render.camera.Position;
            if (keys[GLFW_KEY_ESCAPE]) {
                //glfwDestroyWindow(render.window);
                glfwSetWindowShouldClose(render.window,true);
            }
            if (!keys[GLFW_KEY_ENTER]) {
                fullscreenToggle = true;
            }
            if (keys[GLFW_KEY_LEFT_ALT]) {
                if (keys[GLFW_KEY_ENTER]) { //alt enter for fullscreen
                    if (fullscreenToggle) {
                        fullscreenToggle = false;
                        fullscreen = !fullscreen;
                        if (fullscreen) {
                            //fullscreen code
                            GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
                            glfwSetWindowMonitor(render.window, glfwGetPrimaryMonitor(), 0, 0, videoMode.width(), videoMode.height(), 60);
                        } else {
                            glfwSetWindowMonitor(render.window, NULL, 0, 0, 800, 600, 60);
                        }
                    }
                }
            }

            render.camera.updateFrustum();
            //render skybox
            glUseProgram(render.shaderProgram.programId);
            glUniform1i(render.ImageLoc, 0);
            //glBindVertexArray(vertexMap.get(new Vector2i(0,0)));

            //skybox
            glDisable(GL_CULL_FACE);
            //glDepthMask(false);
            glActiveTexture(1);
            glBindTexture(GL_TEXTURE_2D, skyboxID);
            Vector3f pos = new Vector3f(render.camera.Position.x/500,render.camera.Position.y/500,render.camera.Position.z/500);
            Matrix4f model = new Matrix4f().scale(5,5,5).translate(pos);
            Matrix4f[] MVP = render.camera.getMVP(model);
            glBindVertexArray(skyboxVID);
            fb.clear();
            glUniformMatrix4fv(render.modelLoc, false, MVP[0].get(fb));
            glUniformMatrix4fv(render.viewLoc, false, MVP[1].get(fb));
            glUniformMatrix4fv(render.projectionLoc, false, MVP[2].get(fb));
            glDrawArrays(GL_TRIANGLES, 0, Util.skyboxVerts.length);
            glBindVertexArray(0);
            glClear(GL_DEPTH_BUFFER_BIT);
            //end skybox
            glEnable(GL_CULL_FACE);
            //glDepthMask(true);
            glActiveTexture(0);
            glBindTexture(GL_TEXTURE_2D, render.atlasID);



            float cameraX = (render.camera.Position.x*2);
            //float cameraY = (render.camera.Position.y*2);
            float cameraZ = (render.camera.Position.z*2);

            //Logger.Info("Player Current Block: " + CurrentChunk[Math.round(PlayerX%16)][Math.round(PlayerY)][Math.round(PlayerZ%16)]);
            timer.start();
            int playerChunkX = Math.round(cameraX)/16;
            int playerChunkZ = Math.round(cameraZ)/16;

            for (int x = -renderDistance; x < renderDistance; x++) {
                for (int z = -renderDistance; z < renderDistance; z++) {
                    int chunkX = playerChunkX + x;
                    int chunkZ = playerChunkZ + z;
                    boolean isLoaded = world.hasChunk(chunkX, chunkZ) || pendingChunks.contains(new Vector2i(chunkX,chunkZ));
                    if (!isLoaded) {
                        float xDist = Math.abs((chunkX) - (render.camera.Position.x * 2)/16);
                        float zDist = Math.abs((chunkZ) - (render.camera.Position.z * 2)/16);
                        double distance = Math.sqrt((xDist * xDist) + (zDist * zDist));
                        if (distance < (renderDistance)) {
                            //loadChunk(chunkX, chunkZ);
                            pendingChunks.add(new Vector2i(chunkX, chunkZ));
                        }
                    }
                }
            }

            timer.stop("surrounding chunk loading");

            world.chunkMap.forEach((position, data) -> {
                int x = position.x;
                int y = position.y;
                float xDist = Math.abs((x) - (render.camera.Position.x * 2) / 16);
                float zDist = Math.abs((y) - (render.camera.Position.z * 2) / 16);
                double distance = Math.sqrt((xDist * xDist) + (zDist * zDist));

                float chunkX = (x * 8f) - (render.camera.Position.x);
                float chunkY = (y * 8f) - (render.camera.Position.z);
                if (distance < (renderDistance)) {
                    if (render.camera.isVisible((int) (chunkX - 0f), 64, (int) (chunkY - 0f), 20f)) {
                        Vector3f pos1 = new Vector3f(((x * 0.08f) - 0.00f), 0.0f, ((y * 0.08f) - 0.00f));
                        Matrix4f model1 = new Matrix4f().translate(pos1);
                        Matrix4f[] MVP1 = render.camera.getMVP(model1);
                        //test
                        if (vertexMap.get(new Vector2i(x, y)) != null) {
                            glBindVertexArray(vertexMap.get(new Vector2i(x, y)));
                            fb.clear();
                            glUniformMatrix4fv(render.modelLoc, false, MVP1[0].get(fb));
                            glUniformMatrix4fv(render.viewLoc, false, MVP1[1].get(fb));
                            glUniformMatrix4fv(render.projectionLoc, false, MVP1[2].get(fb));
                            glDrawArrays(GL_TRIANGLES, 0, render.bufferSize);
                        }
                        glBindVertexArray(0);
                    }
                }
                if (distance > (renderDistance)+4) {
                    if (world.hasChunk(x, y)) {
                        if (!dirtyChunks.contains(new Vector2i(x, y))) {
                            dirtyChunks.add(new Vector2i(x, y));
                        }
                    }
                }
            });
            if (!dirtyChunks.isEmpty()) {
                Vector2i lpos = dirtyChunks.get(0);
                world.unloadChunk(lpos.x, lpos.y);
                dirtyChunks.remove(lpos);
            }
            if (!pendingChunks.isEmpty()) {
                Vector2i p = pendingChunks.get(0);
                loadChunk(p.x,p.y);
                pendingChunks.remove(0);
            }

            //free empty space in map
            HashMap<Vector2i, Block[][][]> tmp1 = new HashMap<>(world.chunkMap);
            world.chunkMap = tmp1;
            glClear(GL_DEPTH_BUFFER_BIT);

            //collect debug info
            long totalMem = Runtime.getRuntime().totalMemory()/(1024L * 1024L);
            long freeMem = Runtime.getRuntime().freeMemory()/(1024L * 1024L);
            long usedMem = totalMem - freeMem;
            long percentMem = (long)(((float)usedMem / (float)totalMem)*100);
            if (percentMem > 50) {
                System.gc();
            }
            glUseProgram(0);

            //finish render
            glfwSwapBuffers(render.window);
            glfwPollEvents();

            //calculate how long the gpu and frame took
            long frameTime = timer2.stop("");
            long time = System.currentTimeMillis();
            deltaTime = time-lastTimeDelta;
            lastTimeDelta = time;
            frames++;
            sunRotation.add((deltaTime / 1000f) * 0.016f,0,0); //one cycle every minute

            if (time-lastTime > 1000) {
                fps = frames;
                frames = 0;
                lastTime = time;
                String logString = "FPS: "+fps;
                logString +=", Delta: "+deltaTime+"ms";
                logString +=", Cpu Time: "+frameTime+"ms";
                logString +=", Gpu Time: "+(deltaTime-frameTime)+"ms";
                logString += ", Memory: "+(usedMem)+"/"+totalMem+"mb "+percentMem+"%";
                logString +=", Chunks To Load: "+ pendingChunks.size()+" chunks";
                Logger.Print(Logger.Level.INFO,logString);
            }
        }
    }
    public void generateSpawn() {
        for (int x = -1; x < 1; x++) {
            for (int z = -1; z < 1; z++) {
                loadChunk(x,z);
            }
        }
    }

    public Block[][][] loadChunk(int x, int y) {
        Block[][][] blocks = world.getChunk(x,y);
        FloatBuffer data = render.BuildChunk(blocks);
        if (data.capacity() == 0) {
            return new Block[0][0][0];
        }

        boolean needsUpdate = false;
        int id;
        if (!vertexMap.containsKey(new Vector2i(x,y))) {
            id = glGenVertexArrays();
            vertexMap.put(new Vector2i(x, y), id);
            needsUpdate = true;
        } else {
            id = vertexMap.get(new Vector2i(x,y));
        }
        glBindVertexArray(id);

        int bufferID;
        if (!bufferMap.containsKey(new Vector2i(x,y))) {
            bufferID = glGenBuffers();
            bufferMap.put(new Vector2i(x, y), id);
            needsUpdate = true;
        } else {
            bufferID = bufferMap.get(new Vector2i(x,y));
        }

        glBindBuffer(GL_ARRAY_BUFFER, bufferID);
        glBufferData(GL_ARRAY_BUFFER, data, GL_DYNAMIC_DRAW);


        if (needsUpdate) {
            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);
            glEnableVertexAttribArray(2);

            glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * 4,     0);  //pos
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 6 * 4, 3 * 4);  //uv
            glVertexAttribPointer(2, 1, GL_FLOAT, false, 6 * 4, 5 * 4);  //brightness

            glBindVertexArray(0);
            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);
            glDisableVertexAttribArray(2);
        } else {
            glBindVertexArray(0);
        }
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        return blocks;
    }

    public Engine() {
        //empty
        //world.seed = System.currentTimeMillis();
    }

    boolean[] keys = new boolean[1024];
    public void chunkThread() {
        if (!pendingChunks.isEmpty()) {
            Vector2i lpos = pendingChunks.get(0);
            //don't load the chunk if it is scheduled for deletion
            if (!dirtyChunks.contains(new Vector2i(lpos.x,lpos.y))) {
                Block[][][] data = loadChunk(lpos.x,lpos.y);
                synchronized (lock) {
                    finishedChunks.put(new Vector2i(lpos.x,lpos.y),data);
                }
            }
            pendingChunks.remove(lpos);
        }
    }
    public void loadSkybox() {
        //load cube
        FloatBuffer data = BufferUtils.createFloatBuffer(Util.skyboxVerts.length);
        data.clear();
        data.put(Util.skyboxVerts);
        data.flip();
        skyboxVID = glGenVertexArrays();
        glBindVertexArray(skyboxVID);
        //glBindVertexArray(0);

        int bufferID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, bufferID);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * 4,     0);  //pos
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 6 * 4, 3 * 4);  //uv
        glVertexAttribPointer(2, 1, GL_FLOAT, false, 6 * 4, 5 * 4);  //brightness

        glBindVertexArray(0);
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);

        glBufferData(GL_ARRAY_BUFFER, data, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
    public void run() {
        //render stuff
        render = new Render();
        Model.loadTemplates();
        //lock mouse

        try {
            render.init();
            //generate initial spawn
            //generateSpawn();

            //callbacks
            glfwSetKeyCallback(render.window, (window, key, scancode, action, mods) -> keys[key] = (action == GLFW_PRESS || action == GLFW_REPEAT));
            glfwSetWindowSizeCallback(render.window, new GLFWWindowSizeCallback() {
                @Override
                public void invoke(long window, int width, int height) {
                    glViewport(0, 0, width,height);
                    render.camera.updateProjection(70, width, height);
                }
            });
            glfwSetInputMode(render.window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            glfwSetCursorPosCallback(render.window, new GLFWCursorPosCallback() {
                @Override
                public void invoke(long window, double x, double y) {
                    render.camera.Rotation.add(new Vector3f((float)Math.toRadians(y/10),(float)Math.toRadians(x/10),0));
                    glfwSetCursorPos(render.window, 0, 0);
                    //camera bounds
                    if (render.camera.Rotation.x > Math.toRadians(90)) {
                        render.camera.Rotation.x = (float)Math.toRadians(90);
                    }
                    if (render.camera.Rotation.x < Math.toRadians(-90)) {
                        render.camera.Rotation.x = (float)Math.toRadians(-90);
                    }
                }
            });
            //texture stuff
            //glEnable(GL_TEXTURE_2D);
            //glDisable(GL_TEXTURE_CUBE_MAP);
            //load texture atlas for terrain
            render.atlasID = render.loadTexture("/textures/atlas.png");
            glActiveTexture(GL_TEXTURE0 + 1);
            glBindTexture(GL_TEXTURE_2D, render.atlasID);

            skyboxID = render.loadTexture("/textures/skybox/skybox.png");
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D,skyboxID);
            glGenerateMipmap(skyboxID);
            glGenerateMipmapEXT(GL_TEXTURE_2D);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            //GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
            GL11.glTexParameteri (GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 1);
            //glGenerateMipmap(skyboxID);
            //load skybox cubemap
            loadSkybox();
            System.out.print("Number of available processors are: ");
            System.out.println( Runtime.getRuntime().availableProcessors());
            new Thread(() -> {
                Thread.currentThread().setPriority(1);
                long window = glfwCreateWindow(800, 600, "Voxel Engine", NULL, NULL);
                glfwMakeContextCurrent(window);
                GL.createCapabilities();
                long last_time = System.currentTimeMillis();
                while (!glfwWindowShouldClose(render.window)) {
                    long time = System.currentTimeMillis();
                    if (time > last_time + 500) { //run chunkThread every 200ms
                        last_time = time;
                        chunkThread();
                    }
                }
            });
            loop();
        } catch(Exception e) {
            e.printStackTrace();
            Logger.Print(Logger.Level.Error,"Error Initializing, Cannot proceed.");
            System.exit(1);
        }
    }
}