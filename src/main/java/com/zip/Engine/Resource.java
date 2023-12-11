package com.zip.Engine;

import com.zip.Main;
import com.zip.ShaderProgram;
import org.joml.Vector3f;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.zip.Engine.Util.*;

public class Resource {
    public static float[][] cube = {
            //north
            {-0.00f,  0.01f, -0.00f},
            { 0.01f,  0.01f, -0.00f},
            { 0.01f, -0.00f, -0.00f},
            {-0.00f, -0.00f, -0.00f},

            //south
            { 0.01f,  0.01f,  0.01f},
            {-0.00f,  0.01f,  0.01f},
            {-0.00f, -0.00f,  0.01f},
            { 0.01f, -0.00f,  0.01f},

            { 0.01f,  0.01f, -0.00f},
            { 0.01f,  0.01f,  0.01f},
            { 0.01f, -0.00f,  0.01f},
            { 0.01f, -0.00f, -0.00f},

            {-0.00f,  0.01f,  0.01f},
            {-0.00f,  0.01f, -0.00f},
            {-0.00f, -0.00f, -0.00f},
            {-0.00f, -0.00f,  0.01f},

            { 0.01f,  0.01f,  0.01f},
            { 0.01f,  0.01f, -0.00f},
            {-0.00f,  0.01f, -0.00f},
            {-0.00f,  0.01f,  0.01f},

            {-0.00f, -0.00f,  0.01f},
            {-0.00f, -0.00f, -0.00f},
            { 0.01f, -0.00f, -0.00f},
            { 0.01f, -0.00f,  0.01f},
    };

    //texture loader
    public static class Texture {
        public int width;
        public int height;
        public ByteBuffer buffer;
        public Texture(String path) {
            Object[] data = this.load(path);
            if (data != null) {
                this.buffer = (ByteBuffer) data[0];
                this.width = (int) data[1];
                this.height = (int) data[2];
            }
        }
        public static Object[] load(String path) {
            try {
                InputStream in = Main.class.getResourceAsStream(path);
                assert in != null;
                BufferedImage img = ImageIO.read(in);
                int width = img.getWidth();
                int height = img.getHeight();
                ByteBuffer buffer = BufferUtils.createByteBuffer(img.getWidth() * img.getHeight() * 4); //4 for RGBA, 3 for RGB
                int[] pixels = new int[img.getWidth() * img.getHeight()];
                img.getRGB(0, 0, img.getWidth(), img.getHeight(), pixels, 0, img.getWidth());
                for(int y = 0; y < img.getHeight(); y++){
                    for(int x = 0; x < img.getWidth(); x++){
                        int pixel = pixels[y * img.getWidth() + x];
                        buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
                        buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
                        buffer.put((byte) (pixel & 0xFF));	            // Blue component
                        buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
                    }
                }
                buffer.flip();
                return new Object[] {buffer, width, height};
            } catch (Exception e) {
                return null;
            }
        }
    }
    //Shader Loader
    //file loader
    public static String loadFile(String path) {
        try (InputStream in = Main.class.getResourceAsStream(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            // Use resource
            String line;
            String message = new String();
            while ((line = reader.readLine()) != null) {
                message += line + "\n";
            }
            in.close();
            return message;
        } catch (Exception e){}
        return "";
    }
    public static class Voxel {
        public Vector3f scale;
        public Vector3f from;
        public Vector3f to;
        public Vector3f anchor;
        public boolean[] cull;
        public boolean[] hidden;
        public int[] textures;
        public float[][] uvs;
        public float[] lightLevel; //6, one for each face
        public Voxel(Vector3f scale, Vector3f from, Vector3f to, Vector3f anchor, boolean[] cull,  int[] textures, float[][] uvs) {
            this.scale = scale;
            this.from = from;
            this.to = to;
            this.anchor = anchor;
            this.cull = cull;
            this.cull = cull;
            this.textures = textures;
            this.uvs = uvs;
        }
        public int arraySize(boolean[] enabled) {
            int count = 0;
            for (int j = 0; j < 6; j++) {
                if ( (enabled[j] || !cull[j]) && (uvs[j] != null)) {
                    count += 1;
                }
            }
            return count*36;
        }
        public float[] genArray(boolean[] enabled, float[] position, int id, int[] light) {
            int count = 0;
            for (int j = 0; j < 6; j++) {
                if ( (enabled[j] || !cull[j]) && (uvs[j] != null)) {
                    count += 1;
                }
            }

            float[] res = new float[count*36];
            int index = 0;
            for (int j = 0; j < 6; j++) {
                if ( (enabled[j] || !cull[j]) && (uvs[j] != null)) {

                    float[] v1 = cube[(j * 4) + 0];
                    float[] v2 = cube[(j * 4) + 1];
                    float[] v3 = cube[(j * 4) + 2];
                    float[] v4 = cube[(j * 4) + 3];


                    float[] uv1 = getuvAtlas(304, 288, uvs[j][2] * 0.1f, uvs[j][1] * 0.1f, this.textures[j]);
                    float[] uv2 = getuvAtlas(304, 288, uvs[j][0] * 0.1f, uvs[j][1] * 0.1f, this.textures[j]);
                    float[] uv3 = getuvAtlas(304, 288, uvs[j][0] * 0.1f, uvs[j][3] * 0.1f, this.textures[j]);
                    float[] uv4 = getuvAtlas(304, 288, uvs[j][2] * 0.1f, uvs[j][3] * 0.1f, this.textures[j]);

                    float[] res1 = addFloatArray(parseVSO(v1, uv1, new float[]{from.x,from.y,from.z}, new float[]{to.x,to.y,to.z}, light[j]), position);
                    float[] res2 = addFloatArray(parseVSO(v2, uv2, new float[]{from.x,from.y,from.z}, new float[]{to.x,to.y,to.z}, light[j]), position);
                    float[] res3 = addFloatArray(parseVSO(v3, uv3, new float[]{from.x,from.y,from.z}, new float[]{to.x,to.y,to.z}, light[j]), position);
                    float[] res4 = addFloatArray(parseVSO(v4, uv4, new float[]{from.x,from.y,from.z}, new float[]{to.x,to.y,to.z}, light[j]), position);

                    float[] face = createFace(new float[][]{res1, res2, res3, res4});

                    //int count = 0;
                    for (int k = 0; k < 36; k++) {
                        res[(index * 36) + k] = face[k];
                    }
                    index += 1;
                }
            }
            return res;
        }
    }
    public static float[] jsonReadFloatArr(JSONArray arr) throws JSONException {
        float[] res = new float[arr.length()];
        for (int i = 0; i < arr.length(); i++) {
            res[i] = (float)arr.getDouble(i);
        }
        return res;
    }
    public static class ModelData {
        public String type;
        public Vector3f scale;
        public Voxel[] voxels;
        public ModelData(String path) {
            try {
                String ModelStr = loadFile(path);
                JSONObject obj = new JSONObject(ModelStr);
                this.type = obj.getString("Type");
                this.scale = new Vector3f(jsonReadFloatArr(obj.getJSONArray("Scale")));
                JSONArray model = obj.getJSONArray("Model");
                this.voxels = new Voxel[model.length()];
                for (int i = 0; i < model.length(); i++) {
                    JSONObject current = model.getJSONObject(i);
                    Vector3f scale = new Vector3f(jsonReadFloatArr(current.getJSONArray("Scale")));
                    Vector3f from = new Vector3f(jsonReadFloatArr(current.getJSONArray("From")));
                    Vector3f to = new Vector3f(jsonReadFloatArr(current.getJSONArray("To")));
                    from.x = (from.x / 16) * 10;
                    from.y = (from.x / 16) * 10;
                    from.z = (from.x / 16) * 10;
                    to.x = (to.x / 16) * 10;
                    to.y = (to.y / 16) * 10;
                    to.z = (to.z / 16) * 10;

                    Vector3f anchor = new Vector3f(jsonReadFloatArr(current.getJSONArray("Anchor")));
                    JSONObject faces = current.getJSONObject("Faces");
                    //check for each face
                    String[] faceNames = new String[] {"north","south","east","west","up","down"};
                    boolean[] cull = new boolean[6];
                    int[] textures = new int[6];
                    float[][] uvs = new float[6][4];
                    //String faceString = "";
                    for (int j = 0; j < 6; j++) {
                        if (faces.has(faceNames[j])) {
                            cull[j] = faces.getJSONObject(faceNames[j]).getBoolean("cull");
                            textures[j] = faces.getJSONObject(faceNames[j]).getInt("Texture");
                            uvs[j] = jsonReadFloatArr(faces.getJSONObject(faceNames[j]).getJSONArray("uv"));

                            //correct uv from 0-16 to 0-1
                            for (int k = 0; k < uvs[j].length; k++) {
                                uvs[j][k] = (uvs[j][k] / 16) * 10;
                            }
                        } else {
                            System.out.println("Culled");
                            cull[i] = true;
                            textures[i] = 10;
                            uvs[i] = new float[]{0,0,0,0};
                        }
                    }
                    voxels[i] = new Voxel(scale, from, to, anchor, cull, textures, uvs);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.Error("Failed to load Model: "+path);
            }
        }
    }
}
