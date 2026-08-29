package com.luxof.configucast.meth.equationparts;

import net.minecraft.nbt.NbtCompound;

public final class NBTEP implements EquationPart {
    public NbtCompound nbt;
    public NBTEP(NbtCompound nbt) { this.nbt = nbt; }
    @Override public Object getValue() { return this.nbt; }
    @Override public String strRepr() { return "NBT"; }
}
