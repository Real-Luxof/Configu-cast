package com.luxof.configucast.meth.equationparts;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;

import com.luxof.configucast.meth.MathException;
import com.luxof.configucast.meth.MethInterpreter;

import java.util.List;
import java.util.Map;

import net.minecraft.util.math.Vec3d;

/** function argument. */
public final class CommaNEP extends NestedEP {
    public CommaNEP(List<EquationPart> nested) { super(nested); }

    public EquationPart get(String fn, long og, CastingEnvironment env, CastingImage img) {
        return this.get(fn, og, env, img, Map.of());
    }
    public double getNum(String fn, long og, CastingEnvironment env, CastingImage img) {
        return this.getNum(fn, og, env, img, Map.of());
    }
    /*public String getStr(String fn, long og, CastingEnvironment env, CastingImage img) {
        return this.getStr(fn, og, env, img, Map.of());
    }*/
    public Vec3d getVec(String fn, long og, CastingEnvironment env, CastingImage img) {
        return this.getVec(fn, og, env, img, Map.of());
    }
    /*public NbtCompound getNBT(String fn, long og, CastingEnvironment env, CastingImage img) {
        return this.getNBT(fn, og, env, img, Map.of());
    }*/
    public EquationPart get(String fn, long og, CastingEnvironment env, CastingImage img, Map<String, Object> variables) {
        List<EquationPart> terms = MethInterpreter.simplify(this.nested, og, env, img, variables);
        if (terms.size() > 1) throw new MathException("Error interpreting math equation in function %s: a function argument returned more than one term!", fn);
        return terms.get(0);
    }
    public double getNum(String fn, long og, CastingEnvironment env, CastingImage img, Map<String, Object> variables) {
        if (!(get(fn, og, env, img, variables) instanceof NumberEP term)) throw new MathException("Error interpreting math equation in function %s: expected a number!", fn);
        return term.value;
    }
    /*public String getStr(String fn, long og, CastingEnvironment env, CastingImage img, Map<String, Object> variables) {
        if (!(get(fn, og, env, img, variables) instanceof StringEP term)) throw new MathException("Error interpreting math equation in function %s: expected a string!", fn);
        return term.value;
    }*/
    public Vec3d getVec(String fn, long og, CastingEnvironment env, CastingImage img, Map<String, Object> variables) {
        if (!(get(fn, og, env, img, variables) instanceof VecEP term)) throw new MathException("Error interpreting math equation in function %s: expected a vector!", fn);
        return term.value;
    }
    /*public NbtCompound getNBT(String fn, long og, CastingEnvironment env, CastingImage img, Map<String, Object> variables) {
        if (!(get(fn, og, env, img, variables) instanceof NBTEP term)) throw new MathException("Error interpreting math equation in function %s: expected NBT!", fn);
        return term.nbt;
    }*/
    @Override public String strRepr() { return "COMMA"; }
}
