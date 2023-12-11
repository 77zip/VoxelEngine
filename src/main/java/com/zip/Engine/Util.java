package com.zip.Engine;

import com.zip.Main;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Util {
    public static String loadResource(String path) {
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
    public static float[] fillData(JSONArray jsonArray){
        float[] fData = new float[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                fData[i] =(jsonArray.getInt(i));
            } catch (Exception e) {
                return new float[] {};
            }
        }
        return fData;
    }
    private static float[] calculateFaceNormal(float[] vertex1, float[] vertex2, float[] vertex3) {
        // Calculate the two edge vectors of the face.
        float[] edge1 = new float[3];
        for (int i = 0; i < 3; i++) {
            edge1[i] = vertex2[i] - vertex1[i];
        }

        float[] edge2 = new float[3];
        for (int i = 0; i < 3; i++) {
            edge2[i] = vertex3[i] - vertex1[i];
        }

        // Calculate the cross product of the two edge vectors to get the face normal.
        float[] faceNormal = new float[3];
        faceNormal[0] = edge1[1] * edge2[2] - edge1[2] * edge2[1];
        faceNormal[1] = edge1[2] * edge2[0] - edge1[0] * edge2[2];
        faceNormal[2] = edge1[0] * edge2[1] - edge1[1] * edge2[0];

        // Normalize the face normal.
        float magnitude = (float) Math.sqrt(faceNormal[0] * faceNormal[0] + faceNormal[1] * faceNormal[1] + faceNormal[2] * faceNormal[2]);
        for (int i = 0; i < 3; i++) {
            faceNormal[i] /= magnitude;
        }

        return faceNormal;
    }
    public static float[] createFace(float[][] vertices) {
        return new float[]{
                vertices[0][0], vertices[0][1], vertices[0][2], vertices[0][3], vertices[0][4], vertices[0][5],
                vertices[1][0], vertices[1][1], vertices[1][2], vertices[1][3], vertices[1][4], vertices[1][5],
                vertices[2][0], vertices[2][1], vertices[2][2], vertices[2][3], vertices[2][4], vertices[2][5],

                vertices[2][0], vertices[2][1], vertices[2][2], vertices[2][3], vertices[2][4], vertices[2][5],
                vertices[3][0], vertices[3][1], vertices[3][2], vertices[3][3], vertices[3][4], vertices[3][5],
                vertices[0][0], vertices[0][1], vertices[0][2], vertices[0][3], vertices[0][4], vertices[0][5],
        };
    }
    public static float[] parseVSO(float[] v, float[] uv, float[] from, float[] to, int light) {
        float[] scale = {(to[0] - from[0])/20, (to[1] - from[1])/20, (to[2] - from[2])/20};
        return new float[] {(v[0]+from[0]/500)*scale[0],(v[1]+from[1]/500)*scale[1],(v[2]+from[2]/500)*scale[2], uv[0],uv[1], light};
    }
    public static float[] getuvAtlas(int width, int height, float u, float v, int id) {
        int x = (id) % 19;
        int y = (id) / 19;
        float scaleX = (float) 1 / (width/16f);
        float scaleY = (float) 1 / (height/16f);
        float offsetX = x * scaleX;
        float offsetY = y * scaleY;
        //Logger.Info("ID: "+id+", XY:"+x+","+y);
        return new float[]{
                (u * scaleX)+offsetX,
                (v * scaleY)+offsetY
        };
    }
    public static float[] addFloatArray(float[] a, float[] b) {
        for (int i = 0; i < b.length; i++) {
            a[i] += b[i];
        }
        return a;
    }
    public static float[] skyboxVerts = new float[] { //n,s,e,w,u,d
            -0.1f,  0.1f, -0.1f, 1,0.5f, 16,
             0.1f,  0.1f, -0.1f, 0.66666666666f,0.5f, 16,
             0.1f, -0.1f, -0.1f, 0.66666666666f,0f, 16,
             0.1f, -0.1f, -0.1f, 0.66666666666f,0f, 16,
            -0.1f, -0.1f, -0.1f, 1,0f, 16,
            -0.1f,  0.1f, -0.1f, 1,0.5f, 16,

            //south
             0.1f,  0.1f,  0.1f,  1f,1, 16,
            -0.1f,  0.1f,  0.1f,  0.66666666666f,1, 16,
            -0.1f, -0.1f,  0.1f,  0.66666666666f,0.5f, 16,
            -0.1f, -0.1f,  0.1f,  0.66666666666f,0.5f, 16,
             0.1f, -0.1f,  0.1f,  1,0.5f, 16,
             0.1f,  0.1f,  0.1f,  1,1, 16,

             0.1f,  0.1f, -0.1f,  0.33333333333f,1, 16,
             0.1f,  0.1f,  0.1f,  0,1, 16,
             0.1f, -0.1f,  0.1f,  0,0.5f, 16,
             0.1f, -0.1f,  0.1f,  0,0.5f, 16,
             0.1f, -0.1f, -0.1f,  0.33333333333f,0.5f, 16,
             0.1f,  0.1f, -0.1f,  0.33333333333f,1, 16,

            -0.1f,  0.1f,  0.1f, 0.33333333333f,0.5f, 16,
            -0.1f,  0.1f, -0.1f, 0,0.5f, 16,
            -0.1f, -0.1f, -0.1f, 0,0f, 16,
            -0.1f, -0.1f, -0.1f, 0,0f, 16,
            -0.1f, -0.1f,  0.1f, 0.33333333333f,0f, 16,
            -0.1f,  0.1f,  0.1f, 0.33333333333f,0.5f, 16,

             0.1f,  0.1f,  0.1f, 0.66666666666f,0.5f, 16,
             0.1f,  0.1f, -0.1f, 0.33333333333f,0.5f, 16,
            -0.1f,  0.1f, -0.1f, 0.33333333333f,0, 16,
            -0.1f,  0.1f, -0.1f, 0.33333333333f,0, 16,
            -0.1f,  0.1f,  0.1f, 0.66666666666f,0, 16,
             0.1f,  0.1f,  0.1f, 0.66666666666f,0.5f, 16,

            -0.1f, -0.1f,  0.1f, 0.66666666666f,1f, 16,
            -0.1f, -0.1f, -0.1f, 0.33333333333f,1f, 16,
             0.1f, -0.1f, -0.1f, 0.33333333333f,0.5f, 16,
             0.1f, -0.1f, -0.1f, 0.33333333333f,0.5f, 16,
             0.1f, -0.1f,  0.1f, 0.66666666666f,0.5f, 16,
            -0.1f, -0.1f,  0.1f, 0.66666666666f,1f, 16,
    };
}
