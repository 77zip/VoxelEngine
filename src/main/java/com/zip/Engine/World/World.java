package com.zip.Engine.World;

import com.zip.Engine.Logger;
import org.joml.Vector2i;

import java.io.*;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class World {
    public HashMap<Vector2i, Block[][][]> chunkMap = new HashMap<>();
    public int seed;
    public World(int seed) {
        this.seed = seed;
    }
    public void saveChunk(int x, int z) {
        try {
            DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new FileOutputStream("world/level"+x+"_"+z+".dat")));
            Block[][][] blocks = chunkMap.get(new Vector2i(x,z));
            byte[] data = new byte[18 * 256 * 18];
            for (int bz = 0; bz < 18; bz++) {
                for (int by = 0; by < 256; by++) {
                    for (int bx = 0; bx < 18; bx++) {
                        data[bx + by * 18 + bz * 18 * 256] = (byte) blocks[bx][by][bz].type;
                    }
                }
            }
            dos.write(data);
            dos.close();
        } catch (Exception e) {
            System.out.println("SaveError");
            e.printStackTrace();
        }
    }
    public boolean chunkFileExists(int x, int z) {
        return new File("world/level"+x+"_"+z+".dat").exists();
    }
    public void loadChunk(int x, int z) {
        try {
            DataInputStream dis = new DataInputStream(new GZIPInputStream(new FileInputStream("world/level"+x+"_"+z+".dat")));
            Block[][][] blocks = new Block[18][256][18];
            byte[] data = new byte[18 * 256 * 18];
            dis.readFully(data);
            dis.close();

            for (int bz = 0; bz < 18; bz++) {
                for (int by = 0; by < 256; by++) {
                    for (int bx = 0; bx < 18; bx++) {
                        blocks[bx][by][bz] = new Block(data[bx + by * 18 + bz * 18 * 256],16);
                    }
                }
            }
            chunkMap.put(new Vector2i(x,z),blocks);

        } catch (Exception e) {
            //file may be corrupt delete it and mark it for regeneration
            Logger.Warning("Chunk may be corrupt at: "+x+","+z+" deleting...");
            if (!new File("world/level"+x+"_"+z+".dat").delete()) {
                //if failed to delete file, something is seriously fucked if this fails
                Logger.Error("Something is seriously wrong with chunk loading...");
            }
            chunkMap.remove(new Vector2i(x,z));
        }
    }
    public Block[][][] getChunk(int x, int z) {
        if (!chunkFileExists(x,z)) {
            if (!chunkMap.containsKey(new Vector2i(x, z))) {
                Chunk chunk = new Chunk(this.seed, this, x, z);
                chunkMap.put(new Vector2i(x, z), chunk.data);
                saveChunk(x, z);
            }
        } else {
            //chunk exists
            loadChunk(x,z);
        }
        return chunkMap.get(new Vector2i(x,z));
    }
    public void setChunk(int x, int z, Block[][][] data) {
        chunkMap.put(new Vector2i(x,z), data);
    }
    public int getLight(int x, int y, int z) {
        int chunkX = x/16;
        int chunkZ = z/16;
        int blockX = x%16;
        int blockZ = z%16;
        return this.chunkMap.get(new Vector2i(chunkX,chunkZ))[blockX][y][blockZ].light;
    }
    public void setLight(int x, int y, int z, int data) {
        int chunkX = x/16;
        int chunkZ = z/16;
        int blockX = x%16;
        int blockZ = z%16;
        Block[][][] light = this.chunkMap.get(new Vector2i(chunkX,chunkZ));
        light[blockX][y][blockZ].light = data;
        this.chunkMap.put(new Vector2i(chunkX,chunkZ),light);
    }
    public Block getBlock(int x, int y, int z) {
        int chunkX = x/16;
        int chunkZ = z/16;
        int blockX = x%16;
        int blockZ = z%16;
        return this.chunkMap.get(new Vector2i(chunkX,chunkZ))[blockX][y][blockZ];
    }
    public void setBlock(int x, int y, int z, int data) {
        int chunkX = x/16;
        int chunkZ = z/16;
        int blockX = x%16;
        int blockZ = z%16;
        Block[][][] block = this.chunkMap.get(new Vector2i(chunkX,chunkZ));
        block[blockX][y][blockZ].type = data;
        this.chunkMap.put(new Vector2i(chunkX,chunkZ),block);
    }

    public boolean hasChunk(int x, int z) {
        return chunkMap.containsKey(new Vector2i(x, z));
    }
    public void unloadChunk(int x, int z) {
        chunkMap.remove(new Vector2i(x,z));
    }
    public void shrinkChunkMap() {
        HashMap<Vector2i, Block[][][]> tmpMap = new HashMap<>();
        tmpMap.putAll(this.chunkMap);
        this.chunkMap = tmpMap;
    }

}
