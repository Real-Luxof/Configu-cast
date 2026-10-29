package com.luxof.configucast.meth.equationparts;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;

import com.luxof.configucast.meth.MathException;
import com.luxof.configucast.meth.MethInterpreter;

import static com.luxof.configucast.Configucast.prepareVariables;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;

/** function argument. */
public final class CommaNEP extends NestedEP {
    public CommaNEP(List<EquationPart> nested) { super(nested); }

    public EquationPart get(String fn, long og, CastingEnvironment env, CastingImage img) {
        return this.get(fn, og, env, img, new HashMap<>());
    }
    public EquationPart get(String fn, long og, CastingEnvironment env, CastingImage img, Map<String, Object> variables) {
        prepareVariables(og, env, img, variables);
        return this.get(fn, variables);
    }
    public double getNum(String fn, long og, CastingEnvironment env, CastingImage img) {
        return this.getNum(fn, og, env, img, new HashMap<>());
    }
    public double getNum(String fn, long og, CastingEnvironment env, CastingImage img, Map<String, Object> variables) {
        prepareVariables(og, env, img, variables);
        return this.getNum(fn, variables);
    }
    public String getStr(String fn, long og, CastingEnvironment env, CastingImage img) {
        return this.getStr(fn, og, env, img, new HashMap<>());
    }
    public String getStr(String fn, long og, CastingEnvironment env, CastingImage img, Map<String, Object> variables) {
        prepareVariables(og, env, img, variables);
        return this.getStr(fn, variables);
    }
    public Vec3d getVec(String fn, long og, CastingEnvironment env, CastingImage img) {
        return this.getVec(fn, og, env, img, new HashMap<>());
    }
    public Vec3d getVec(String fn, long og, CastingEnvironment env, CastingImage img, Map<String, Object> variables) {
        prepareVariables(og, env, img, variables);
        return this.getVec(fn, variables);
    }
    public NbtCompound getNBT(String fn, long og, CastingEnvironment env, CastingImage img) {
        return this.getNBT(fn, og, env, img, new HashMap<>());
    }
    public NbtCompound getNBT(String fn, long og, CastingEnvironment env, CastingImage img, Map<String, Object> variables) {
        prepareVariables(og, env, img, variables);
        return this.getNBT(fn, variables);
    }
    public EquationPart get(String fn, Map<String, Object> variables) {
        List<EquationPart> terms = MethInterpreter.simplify(this.nested, variables);
        if (terms.size() > 1) throw new MathException("Error interpreting math equation in function %s: a function argument returned more than one term!", fn);
        return terms.get(0);
    }
    public double getNum(String fn, Map<String, Object> variables) {
        if (!(get(fn, variables) instanceof NumberEP term)) throw new MathException("Error interpreting math equation in function %s: expected a number!", fn);
        return term.value;
    }
    public String getStr(String fn, Map<String, Object> variables) {
        if (!(get(fn, variables) instanceof StringEP term)) throw new MathException("Error interpreting math equation in function %s: expected a string!", fn);
        return term.value;
    }
    public Vec3d getVec(String fn, Map<String, Object> variables) {
        if (!(get(fn, variables) instanceof VecEP term)) throw new MathException("Error interpreting math equation in function %s: expected a vector!", fn);
        return term.value;
    }
    public NbtCompound getNBT(String fn, Map<String, Object> variables) {
        if (!(get(fn, variables) instanceof NBTEP term)) throw new MathException("Error interpreting math equation in function %s: expected NBT!", fn);
        return term.nbt;
    }
    @Override public String strRepr() { return "COMMA"; }
}
