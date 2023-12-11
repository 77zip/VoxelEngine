package com.zip.Engine;

import com.zip.Engine.Player.Camera;
import com.zip.Engine.World.Block;
import com.zip.Engine.World.Chunk;
import com.zip.Engine.World.Model;
import com.zip.ShaderProgram;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.*;
import com.zip.Engine.Resource.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static com.zip.Engine.Util.loadResource;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.stb.STBEasyFont.stb_easy_font_print;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Render {

    public long window;
    public ShaderProgram shaderProgram;
    public Camera camera = new Camera();
    //block data
    int bufferID;
    int vertexID;
    int atlasID;
    boolean[] transparent = new boolean[] {
            false, false
    };

    public boolean[] cullCheck(int[] position, Block[][][] data) {
        boolean[] enabled = new boolean[] {true,true,true,true,true,true};

        //left right
        if (position[0]+1 < data.length) {
            int dest = data[position[0] + 1][position[1]][position[2]].type;
            if (dest != 0 && !transparent[0]) {
                enabled[2] = false;
            }
        }
        if (position[0] > 0) {
            int dest = data[position[0] - 1][position[1]][position[2]].type;
            if (dest != 0 && !transparent[0]) {
                enabled[3] = false;
            }
        }
        //forward backward
        if (position[2]+1 < data[0][0].length) {
            int dest = data[position[0]][position[1]][position[2] + 1].type;
            if (dest != 0 && !transparent[0]) {
                enabled[1] = false;
            }
        }
        if (position[2] > 0) {
            int dest = data[position[0]][position[1]][position[2] - 1].type;
            if (dest != 0 && !transparent[0]) {
                enabled[0] = false;
            }
        }

        //top bottom
        if (position[1]+1 < data[0].length) {
            int dest = data[position[0]][position[1] + 1][position[2]].type;
            if (dest != 0 && !transparent[0]) {
                enabled[4] = false;
            }
        }
        if (position[1] > 0) {
            int dest = data[position[0]][position[1] - 1][position[2]].type;
            if (dest != 0 && !transparent[0]) {
                enabled[5] = false;
            }
        }
        if (position[1] == 0) {
            enabled[5] = false;
        }

        return enabled;
    }

    //template cube
    public FloatBuffer BuildChunk(Block[][][] data) {
        if (data == null) {
            Logger.Warning("Critical Chunk Error");
            return BufferUtils.createFloatBuffer(0);
        }
        int size = 0;
        for (int y = 0; y < 255; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    //create actual block
                    if (data[x+1][y][z+1].type != 0) {
                        boolean[] enabled = cullCheck(new int[]{x+1, y, z+1}, data);
                        for (int j = 0; j < Model.templates[data[x+1][y][z+1].type-1].length; j++) {
                            size += Model.templates[data[x+1][y][z+1].type-1][j].arraySize(enabled);
                        }
                    }
                }
            }
        }


        bufferSize = size;
        FloatBuffer buffer = BufferUtils.createFloatBuffer(size);

        buffer.clear();
        for (int y = 1; y < 255; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    //create actual block

                    if (data[x+1][y][z+1].type != 0) {
                        boolean[] enabled = cullCheck(new int[]{x+1, y, z+1}, data);
                        for (int j = 0; j < Model.templates[data[x+1][y][z+1].type-1].length; j++) {
                            int[] lightArr = new int[] {
                                    data[x+1][y][z].light,
                                    data[x+1][y][z+2].light,
                                    data[x+2][y][z+1].light,
                                    data[x][y][z+1].light,
                                    data[x+1][y+1][z+1].light,
                                    data[x+1][y-1][z+1].light,
                            };
                            float[] faceArray = Model.templates[data[x+1][y][z+1].type-1][j].genArray(enabled, new float[]{(x-0.5f) * 0.005f, (y-0.5f) * 0.005f, (z-0.5f) * 0.005f}, data[x+1][y][z+1].type, lightArr);
                            buffer.put(faceArray);
                        }
                    }
                }
            }
        }
        buffer.flip();
        return buffer;
    }

    public void updateVBO(FloatBuffer buffer, int id) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_DYNAMIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }
    int ImageLoc;
    int lightLevelLoc;
    public int bufferSize = 0;
    FloatBuffer fb = BufferUtils.createFloatBuffer(16);
    int modelLoc;
    int viewLoc;
    int lightPosLoc;
    int projectionLoc;

    //main
    public void init() throws Exception {
        //other stuff
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(800, 600, "Voxel Engine", NULL, NULL);
        if ( window == NULL ) {
            throw new RuntimeException("Failed to create the GLFW window");
        }
        GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (videoMode.width()/2)-(400),(videoMode.height()/2)-(300));
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glfwSwapInterval(0);
        glfwShowWindow(window);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        //callbacks
        glfwSetWindowSizeCallback(window, new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                glViewport(0, 0, width,height);
                camera.updateProjection(70, width, height);

            }
        });
        GLFWErrorCallback.createPrint(System.err).set();

        //init shaders
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(loadResource("/shaders/vertex.glsl"));
        shaderProgram.createFragmentShader(loadResource("/shaders/fragment.glsl"));
        shaderProgram.link();
        ImageLoc = glGetUniformLocation(shaderProgram.programId,"tex");
        modelLoc = glGetUniformLocation(shaderProgram.programId,"model");
        viewLoc = glGetUniformLocation(shaderProgram.programId,"view");
        lightPosLoc = glGetUniformLocation(shaderProgram.programId,"lightPos");
        projectionLoc = glGetUniformLocation(shaderProgram.programId,"projection");
        lightLevelLoc = glGetUniformLocation(shaderProgram.programId,"lightLevel");
    }
    public int loadTexture(String path) {
        try {
            Texture texture = new Texture(path);
            ByteBuffer buffer = texture.buffer;
            int width = texture.width;
            int height = texture.height;

            int textureID = glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
            //GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            return textureID;
        } catch (Exception e) {
            Logger.Print(Logger.Level.WARNING,"Error loading texture, Application may not function as intended");
        }
        return -1;
    }
    public Render() {
        //empty
        //todo
    }
}
