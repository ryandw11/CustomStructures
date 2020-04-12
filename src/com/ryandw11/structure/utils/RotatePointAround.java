package com.ryandw11.structure.utils;

import com.sk89q.worldedit.math.BlockVector3;

public class RotatePointAround {

    public static BlockVector3 calculate(BlockVector3 point, BlockVector3 center, double angle){
        angle = angle * -1 * (Math.PI/180);
        double rotatedX = Math.cos(angle) * (point.getX() - center.getX()) - Math.sin(angle) * (point.getZ() - center.getZ()) + center.getX();
        double rotatedZ = Math.sin(angle) * (point.getX() - center.getX()) + Math.cos(angle) * (point.getZ() - center.getZ()) + center.getZ();

        return BlockVector3.at(rotatedX, point.getY(), rotatedZ);
    }
}
