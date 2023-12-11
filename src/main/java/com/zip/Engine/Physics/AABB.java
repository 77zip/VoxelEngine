package com.zip.Engine.Physics;
public class AABB {

    private final double epsilon = 0.0F;

    public double minX;
    public double minY;
    public double minZ;
    public double maxX;
    public double maxY;
    public double maxZ;
    public AABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }
    public AABB clone() {
        return new AABB(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }
    public AABB expand(double x, double y, double z) {
        double minX = this.minX;
        double minY = this.minY;
        double minZ = this.minZ;
        double maxX = this.maxX;
        double maxY = this.maxY;
        double maxZ = this.maxZ;

        // Handle expanding of min/max x
        if (x < 0.0F) {
            minX += x;
        } else {
            maxX += x;
        }

        // Handle expanding of min/max y
        if (y < 0.0F) {
            minY += y;
        } else {
            maxY += y;
        }

        // Handle expanding of min/max z
        if (z < 0.0F) {
            minZ += z;
        } else {
            maxZ += z;
        }

        // Create new bounding box
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }
    public AABB grow(double x, double y, double z) {
        return new AABB(this.minX - x, this.minY - y,
                this.minZ - z, this.maxX + x,
                this.maxY + y, this.maxZ + z);
    }
    public double clipXCollide(AABB otherBoundingBox, double x) {
        // Check if the boxes are colliding on the Y axis
        if (otherBoundingBox.maxY <= this.minY || otherBoundingBox.minY >= this.maxY) {
            return x;
        }
        // Check if the boxes are colliding on the Z axis
        if (otherBoundingBox.maxZ <= this.minZ || otherBoundingBox.minZ >= this.maxZ) {
            return x;
        }
        // Check for collision if the X axis of the current box is bigger
        if (x > 0.0F && otherBoundingBox.maxX <= this.minX) {
            double max = this.minX - otherBoundingBox.maxX - this.epsilon;
            if (max < x) {
                x = max;
            }
        }
        // Check for collision if the X axis of the current box is smaller
        if (x < 0.0F && otherBoundingBox.minX >= this.maxX) {
            double max = this.maxX - otherBoundingBox.minX + this.epsilon;
            if (max > x) {
                x = max;
            }
        }
        return x;
    }

    public double clipYCollide(AABB otherBoundingBox, double y) {
        // Check if the boxes are colliding on the X axis
        if (otherBoundingBox.maxX <= this.minX || otherBoundingBox.minX >= this.maxX) {
            return y;
        }
        // Check if the boxes are colliding on the Z axis
        if (otherBoundingBox.maxZ <= this.minZ || otherBoundingBox.minZ >= this.maxZ) {
            return y;
        }
        // Check for collision if the Y axis of the current box is bigger
        if (y > 0.0F && otherBoundingBox.maxY <= this.minY) {
            double max = this.minY - otherBoundingBox.maxY - this.epsilon;
            if (max < y) {
                y = max;
            }
        }
        // Check for collision if the Y axis of the current box is bigger
        if (y < 0.0F && otherBoundingBox.minY >= this.maxY) {
            double max = this.maxY - otherBoundingBox.minY + this.epsilon;
            if (max > y) {
                y = max;
            }
        }

        return y;
    }
    public double clipZCollide(AABB otherBoundingBox, double z) {
        // Check if the boxes are colliding on the X axis
        if (otherBoundingBox.maxX <= this.minX || otherBoundingBox.minX >= this.maxX) {
            return z;
        }
        // Check if the boxes are colliding on the Y axis
        if (otherBoundingBox.maxY <= this.minY || otherBoundingBox.minY >= this.maxY) {
            return z;
        }
        // Check for collision if the Z axis of the current box is bigger
        if (z > 0.0F && otherBoundingBox.maxZ <= this.minZ) {
            double max = this.minZ - otherBoundingBox.maxZ - this.epsilon;
            if (max < z) {
                z = max;
            }
        }
        if (z < 0.0F && otherBoundingBox.minZ >= this.maxZ) {
            double max = this.maxZ - otherBoundingBox.minZ + this.epsilon;
            if (max > z) {
                z = max;
            }
        }
        return z;
    }
    public boolean intersects(AABB otherBoundingBox) {
        if (otherBoundingBox.maxX <= this.minX || otherBoundingBox.minX >= this.maxX) {
            return false;
        }
        if (otherBoundingBox.maxY <= this.minY || otherBoundingBox.minY >= this.maxY) {
            return false;
        }
        return (!(otherBoundingBox.maxZ <= this.minZ)) && (!(otherBoundingBox.minZ >= this.maxZ));
    }

    public void move(double x, double y, double z) {
        this.minX += x;
        this.minY += y;
        this.minZ += z;
        this.maxX += x;
        this.maxY += y;
        this.maxZ += z;
    }

    public AABB offset(double x, double y, double z) {
        return new AABB(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }
}