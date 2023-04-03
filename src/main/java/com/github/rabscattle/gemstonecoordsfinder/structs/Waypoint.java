package com.github.rabscattle.gemstonecoordsfinder.structs;

import com.google.gson.annotations.Expose;

import java.util.Objects;

public class Waypoint {
    @Expose
    private final int x;
    @Expose
    private final int y;
    @Expose
    private final int z;

    public Waypoint(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }


    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Waypoint waypoint)) return false;
        return x == waypoint.x && y == waypoint.y && z == waypoint.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
