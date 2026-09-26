package com.luxof.configucast.meth.equationparts;

import net.minecraft.util.math.Vec3d;

public final class VecEP implements EquationPart {
    public Vec3d value;
    public VecEP(Vec3d value) { this.value = value; }
    @Override public Object getValue() { return value; }
    @Override public String strRepr() { return value.toString(); }
    @Override public boolean equals(Object o) {
        return o instanceof VecEP vec
            ? Math.abs(vec.value.x - value.x) < 0.0001
            && Math.abs(vec.value.y - value.y) < 0.0001
            && Math.abs(vec.value.z - value.z) < 0.0001
            : super.equals(o);
    }
}
