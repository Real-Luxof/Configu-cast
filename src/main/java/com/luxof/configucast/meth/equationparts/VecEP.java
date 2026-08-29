package com.luxof.configucast.meth.equationparts;

import net.minecraft.util.math.Vec3d;

public final class VecEP implements EquationPart {
    public Vec3d value;
    public VecEP(Vec3d value) { this.value = value; }
    @Override public Object getValue() { return value; }
    @Override public String strRepr() { return value.toString(); }
}
