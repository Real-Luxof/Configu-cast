package com.luxof.configucast.meth.equationparts;

import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public final class NBTEP implements EquationPart {
    public NbtCompound nbt;
    public NBTEP(NbtCompound nbt) { this.nbt = nbt; }
    @Override public Object getValue() { return this.nbt; }
    @Override public String strRepr() { return "NBT"; }

    /** all this for a custom fucking integer check... */
    private boolean equals(NbtElement ele1, NbtElement ele2) {
        if (ele1.getType() != ele2.getType()) return false;

        else if (ele1 instanceof AbstractNbtNumber num1)
            return Math.abs(num1.doubleValue() - ((AbstractNbtNumber)ele2).doubleValue()) < 0.0001;

        else if (ele1 instanceof NbtCompound comp1) {
            NbtCompound comp2 = (NbtCompound)ele2;
            for (String key : comp1.getKeys()) {
                if (!comp2.contains(key) || !equals(comp1.get(key), comp2.get(key))) return false;
            }
            return true;

        } else if (ele1 instanceof AbstractNbtList<?> list1) {
            AbstractNbtList<?> list2 = (AbstractNbtList<?>)ele2;
            int list1Size = list1.size();
            if (list1Size != list2.size()) return false;
            for (int idx = 0; idx < list1Size; idx++) {
                if (!equals(list1.get(idx), list2.get(idx))) return false;
            }
            return true;

        } else return ele1.equals(ele2);
    }
    @Override public boolean equals(Object o) {
        return o instanceof NBTEP nbtEp
            ? equals(nbtEp.nbt, nbt)
            : false;
    }
}
