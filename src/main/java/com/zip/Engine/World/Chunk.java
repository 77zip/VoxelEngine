package com.zip.Engine.World;

import java.util.Random;

public class Chunk {
    public Block[][][] data = new Block[18][256][18];
    int cx;
    int cz;
    World world;

    public Chunk(int seed, World world, int cx, int cz) {
        this.world = world;
        this.cx = cx;
        this.cz = cz;
        Random random = new Random();
        random.setSeed(seed);
        //fill chunk
        SimplexNoiseOctave simplex = new SimplexNoiseOctave(seed);

        for (int z = 0; z < 18; z++) {
            float worldZ = ((z+1) + (cz * 16)) / 50f;
            for (int x = 0; x < 18; x++) {
                float worldX = ((x+1) + (cx * 16)) / 50f;
                double baseLevel = (simplex.noise(worldX/20,worldZ/20,0)*15)+5;
                double squishFactor = 1;//+(Math.abs(simplex.noise(worldX,worldZ,2))*1.5f);
                //baseLevel = Math.clamp(baseLevel, 5, 32);
                int highest = 0;
                int highestDirt = 0;
                for (int y = 0; y < 256; y++) {
                    double densityMod = (baseLevel - (y/10f) ) * squishFactor;
                    double density = simplex.noise(worldX,(y / 25f),worldZ);
                    //density += simplex.noise(worldX*2f,(y / 15f),worldZ*2f);
                    this.data[x][y][z] = new Block(0,0);
                    this.data[x][y][z].light = 16;
                    if (density + densityMod > 0.1) {
                        if (baseLevel > 10) {
                            this.data[x][y][z].type = 7; //stone
                        } else {
                            this.data[x][y][z].type = 4; //grass
                            if (y > 0) {
                                this.data[x][y - 1][z].type = 3; //dirt
                            }
                            if (y > highestDirt) {
                                highestDirt = y;
                            }
                        }
                        if (y > highest) {
                            highest = y;
                        }
                        if (y < 66) {
                            if (density + densityMod > 0.5) {
                                this.data[x][y][z].type = 6; //sand
                            }
                        }
                    } else {
                        if (y < 64) {
                            this.data[x][y][z].type = 5; //water
                        }
                    }
                }
                if (highest < highestDirt) {
                    if (this.data[x][highest+1][z].type == 0) {
                        this.data[x][highest][z].type = 4; //grass
                    }
                    for (int i = 1; i < 3; i++) {
                        if (highest > i) {
                            if (this.data[x][highest - i][z].type != 0) {
                                this.data[x][highest - i][z].type = 3; //dirt
                            }
                        }
                    }
                }
            }
        }
    }
}