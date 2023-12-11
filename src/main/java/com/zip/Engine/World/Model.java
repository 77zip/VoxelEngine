package com.zip.Engine.World;

import com.zip.Engine.Resource;
import com.zip.Engine.Util;
import org.json.JSONArray;
import org.json.JSONObject;
import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;


import static com.zip.Engine.Util.loadResource;

public class Model {
    //enum of all blocks and their data
    public static String[] TemplatePaths = {
            "stairs.json",
            "log.json",
            "dirt.json",
            "grass.json",
            "water.json",
            "sand.json",
            "stone.json",
            "bedrock.json",
            "torch.json"
    };
    //this cube is used as a template for all other voxels
    public static float[][] cube = {
            //north
            {-0.005f,  0.005f, -0.005f},
            { 0.005f,  0.005f, -0.005f},
            { 0.005f, -0.005f, -0.005f},
            {-0.005f, -0.005f, -0.005f},

            //south
            { 0.005f,  0.005f,  0.005f},
            {-0.005f,  0.005f,  0.005f},
            {-0.005f, -0.005f,  0.005f},
            { 0.005f, -0.005f,  0.005f},

            { 0.005f,  0.005f, -0.005f},
            { 0.005f,  0.005f,  0.001095f},
            { 0.005f, -0.005f,  0.005f},
            { 0.005f, -0.005f, -0.005f},

            {-0.005f,  0.005f,  0.005f},
            {-0.005f,  0.005f, -0.005f},
            {-0.005f, -0.005f, -0.005f},
            {-0.005f, -0.005f,  0.005f},

            { 0.005f,  0.005f,  0.005f},
            { 0.005f,  0.005f, -0.005f},
            {-0.005f,  0.005f, -0.005f},
            {-0.005f,  0.005f,  0.005f},

            {-0.005f, -0.005f,  0.005f},
            {-0.005f, -0.005f, -0.005f},
            { 0.005f, -0.005f, -0.005f},
            { 0.005f, -0.005f,  0.005f},
    };
    //vertex manipulation functions
    public static float[] createFace(float[][] vertices) {
        return new float[]{
                vertices[0][0], vertices[0][1], vertices[0][2], vertices[0][3], vertices[0][4], 1,
                vertices[1][0], vertices[1][1], vertices[1][2], vertices[1][3], vertices[1][4], 1,
                vertices[2][0], vertices[2][1], vertices[2][2], vertices[2][3], vertices[2][4], 1,

                vertices[2][0], vertices[2][1], vertices[2][2], vertices[2][3], vertices[2][4], 1,
                vertices[3][0], vertices[3][1], vertices[3][2], vertices[3][3], vertices[3][4], 1,
                vertices[0][0], vertices[0][1], vertices[0][2], vertices[0][3], vertices[0][4], 1,
        };
    }
    public static float[] parseVSO(float[] v, float[] uv, float[] from, float[] to) {
        float[] scale = {(to[0] - from[0])/20, (to[1] - from[1])/20, (to[2] - from[2])/20};
        return new float[] {(v[0]+from[0]/500)*scale[0], (v[1]+from[1]/500)*scale[1], (v[2]+from[2]/500)*scale[2], uv[0], uv[1]};
    }
    public static float[] getUV(JSONObject obj, String key) {
        try {
            float[] tmp = Util.fillData(obj.getJSONObject(key).getJSONArray("uv"));
            return new float[] {(tmp[0]/16)*10,(tmp[1]/16)*10, (tmp[2]/16)*10, (tmp[3]/16)*10};
        } catch(Exception e) {
            return null;
        }
    }
    static class Voxel {
        public float[] from;
        public float[] to;
        public float[][] uvs;
        boolean[] cull;
        public Voxel(float[] from, float[] to, float[][] uvs, boolean[] cull) {
            this.from = from;
            this.to = to;
            this.uvs = uvs;
            this.cull = cull;
        }
        public float[] addFloatArray(float[] a, float[] b) {
            for (int i = 0; i < b.length; i++) {
                a[i] += b[i];
            }
            return a;
        }
        public int arraySize(boolean[] enabled) {
            int count = 0;
            for (int j = 0; j < 6; j++) {
                if ( (enabled[j] || !cull[j]) && (uvs[j] != null)) {
                    count += 1;
                }
            }
            return count*30;
        }
        public float[] genArray(boolean[] enabled, float[] position, int id) {
            int count = 0;
            for (int j = 0; j < 6; j++) {
                if ( (enabled[j] || !cull[j]) && (uvs[j] != null)) {
                    count += 1;
                }
            }
            float[] res = new float[count*30];
            int index = 0;
            for (int j = 0; j < 6; j++) {
                if ( (enabled[j] || !cull[j]) && (uvs[j] != null)) {
                    float[] v1 = cube[(j * 4) + 0];
                    float[] v2 = cube[(j * 4) + 1];
                    float[] v3 = cube[(j * 4) + 2];
                    float[] v4 = cube[(j * 4) + 3];

                    float[] uv1 = getuvAtlas(304, 288, uvs[j][2] / 10, uvs[j][1] / 10, id);
                    float[] uv2 = getuvAtlas(304, 288, uvs[j][0] / 10, uvs[j][1] / 10, id);
                    float[] uv3 = getuvAtlas(304, 288, uvs[j][0] / 10, uvs[j][3] / 10, id);
                    float[] uv4 = getuvAtlas(304, 288, uvs[j][2] / 10, uvs[j][3] / 10, id);

                    float[] res1 = addFloatArray(parseVSO(v1, uv1, from, to), position);
                    float[] res2 = addFloatArray(parseVSO(v2, uv2, from, to), position);
                    float[] res3 = addFloatArray(parseVSO(v3, uv3, from, to), position);
                    float[] res4 = addFloatArray(parseVSO(v4, uv4, from, to), position);

                    float[] face = createFace(new float[][]{res1, res2, res3, res4});
                    //int count = 0;
                    for (int k = 0; k < 30; k++) {
                        res[(index * 30) + k] = face[k];
                    }
                    index += 1;
                }
            }
            return res;
        }
        public float[] getuvAtlas(int width, int height, float u, float v, int id) {
            int x = (id) % 19;
            int y = (id) / 19;
            float scaleX = (float) 1 / (width/16);
            float scaleY = (float) 1 / (height/16);
            float offsetX = x * scaleX;
            float offsetY = y * scaleY;
            float[] res = new float[]{
                    (u * scaleX)+offsetX,
                    (v * scaleY)+offsetY
            };
            return res;
        }
    }
    public static boolean checkCull(JSONObject obj, String key) {
        try {
            return obj.getJSONObject(key).has("cullface");
        } catch (Exception e){
            return false;
        }
    }
    public static Voxel[] parseModel(String path) {
        try {
            String ModelStr = loadResource(path);
            JSONObject obj = new JSONObject(ModelStr);

            String texturePath = obj.getString("texture");
            JSONArray model = obj.getJSONArray("model");
            FloatBuffer buffer = BufferUtils.createFloatBuffer(model.length()*5*6*6);
            buffer.clear();
            //apply buffer data here (this is horrific code)
            Voxel[] voxels = new Voxel[model.length()];
            for (int i = 0; i < model.length(); i++) {
                JSONObject element = model.getJSONObject(i);
                JSONArray fromJson = element.getJSONArray("from");
                JSONArray toJson = element.getJSONArray("to");
                JSONObject facesJson = element.getJSONObject("faces");

                float[] tmp = Util.fillData(fromJson);
                float[] from = {(tmp[0]/16)*10,(tmp[1]/16)*10, (tmp[2]/16)*10};
                tmp = Util.fillData(toJson);
                float[] to = {(tmp[0]/16)*10,(tmp[1]/16)*10, (tmp[2]/16)*10};

                boolean[] cull = new boolean[] {
                        checkCull(facesJson, "north"),
                        checkCull(facesJson, "south"),
                        checkCull(facesJson, "east"),
                        checkCull(facesJson, "west"),
                        checkCull(facesJson, "up"),
                        checkCull(facesJson, "down"),
                };

                float[][] uvs = {
                        getUV(facesJson,"north"),
                        getUV(facesJson,"south"),
                        getUV(facesJson,"east"),
                        getUV(facesJson,"west"),
                        getUV(facesJson,"up"),
                        getUV(facesJson,"down")
                };
                Voxel vox = new Voxel(from, to, uvs, cull);

                voxels[i] = vox;
            }
            return voxels;
        } catch (Exception e){
            return null;
        }
    }
    public static Resource.Voxel[][] templates = new Resource.Voxel[TemplatePaths.length][];

    public static void loadTemplates() {

        for (int i = 0; i < TemplatePaths.length; i++) {
            //templates[i] = parseModel("/models/"+TemplatePaths[i]);
            templates[i] = new Resource.ModelData("/models/"+TemplatePaths[i]).voxels;
        }
    }
}
