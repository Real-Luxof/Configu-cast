package com.luxof.configucast.meth.equationparts;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;

import com.luxof.configucast.meth.MathException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class FunctionEP implements EquationPart {
    public String function;
    public FunctionEP(String function) { this.function = function; }
    @Override public Object getValue() { return this; }
    @Override public String strRepr() { return function + "("; }

    public static Map<String, Pair<Integer, Integer>> functionArgumentRanges = Map.ofEntries(
        Map.entry("floor", new Pair<>(1, 2)),
        Map.entry("ceil", new Pair<>(1, 2)),
        Map.entry("round", new Pair<>(1, 2)),
        Map.entry("max", new Pair<>(2, 1000)),
        Map.entry("min", new Pair<>(2, 1000)),
        Map.entry("log", new Pair<>(1, 3)),
        Map.entry("sqrt", new Pair<>(1, 2)),
        Map.entry("abs", new Pair<>(1, 2)),
        Map.entry("signum", new Pair<>(1, 2)),
        Map.entry("degrees", new Pair<>(1, 2)),
        Map.entry("radians", new Pair<>(1, 2)),
        Map.entry("root", new Pair<>(2, 3)),
        Map.entry("random", new Pair<>(0, 1)),

        Map.entry("sin", new Pair<>(1, 2)),
        Map.entry("sinh", new Pair<>(1, 2)),
        Map.entry("arcsin", new Pair<>(1, 2)),
        
        Map.entry("cos", new Pair<>(1, 2)),
        Map.entry("cosh", new Pair<>(1, 2)),
        Map.entry("arccos", new Pair<>(1, 2)),

        Map.entry("tan", new Pair<>(1, 2)),
        Map.entry("tanh", new Pair<>(1, 2)),
        Map.entry("arctan", new Pair<>(1, 2)),
        Map.entry("atan2", new Pair<>(2, 3)),

        Map.entry("csc", new Pair<>(1, 2)),
        Map.entry("csch", new Pair<>(1, 2)),
        Map.entry("arccsc", new Pair<>(1, 2)),

        Map.entry("sec", new Pair<>(1, 2)),
        Map.entry("sech", new Pair<>(1, 2)),
        Map.entry("arcsec", new Pair<>(1, 2)),

        Map.entry("cot", new Pair<>(1, 2)),
        Map.entry("coth", new Pair<>(1, 2)),
        Map.entry("arccot", new Pair<>(1, 2)),

        Map.entry("axial", new Pair<>(1, 2)),
        Map.entry("len", new Pair<>(1, 2)),
        Map.entry("lenSqr", new Pair<>(1, 2)),
        Map.entry("hadamard", new Pair<>(2, 3)),

        Map.entry("num", new Pair<>(1, 2)),
        Map.entry("str", new Pair<>(1, 2)),
        Map.entry("vec", new Pair<>(3, 4)),

        Map.entry("loop", new Pair<>(5, 6))
    );

    public static EquationPart computeFunction(
        String fn,
        List<CommaNEP> args,
        long originalAmount,
        CastingEnvironment env,
        CastingImage img
    ) {
        Object data = switch (fn) {
            case "neg" -> ~((long)args.get(0).getNum(fn, originalAmount, env, img));
            case "floor" -> Math.floor(args.get(0).getNum(fn, originalAmount, env, img));
            case "ceil" -> Math.ceil(args.get(0).getNum(fn, originalAmount, env, img));
            case "round" -> Math.round(args.get(0).getNum(fn, originalAmount, env, img));
            case "max" -> args.stream().map(a -> a.getNum(fn, originalAmount, env, img)).reduce(Math::max);
            case "min" -> args.stream().map(a -> a.getNum(fn, originalAmount, env, img)).reduce(Math::min);
            case "log" -> Math.log(args.get(0).getNum(fn, originalAmount, env, img)) / Math.log(args.get(1).getNum(fn, originalAmount, env, img));
            case "sqrt" -> Math.sqrt(args.get(0).getNum(fn, originalAmount, env, img));
            case "abs" -> Math.abs(args.get(0).getNum(fn, originalAmount, env, img));
            case "signum" -> Math.signum(args.get(0).getNum(fn, originalAmount, env, img));
            case "degrees" -> Math.toDegrees(args.get(0).getNum(fn, originalAmount, env, img));
            case "radians" -> Math.toRadians(args.get(0).getNum(fn, originalAmount, env, img));
            case "root" -> Math.pow(args.get(0).getNum(fn, originalAmount, env, img), 1.0 / args.get(1).getNum(fn, originalAmount, env, img));
            case "random" -> Math.random();

            case "sin" -> Math.sin(args.get(0).getNum(fn, originalAmount, env, img));
            case "sinh" -> Math.sinh(args.get(0).getNum(fn, originalAmount, env, img));
            case "arcsin" -> Math.asin(args.get(0).getNum(fn, originalAmount, env, img));
            case "cos" -> Math.cos(args.get(0).getNum(fn, originalAmount, env, img));
            case "cosh" -> Math.cosh(args.get(0).getNum(fn, originalAmount, env, img));
            case "arccos" -> Math.acos(args.get(0).getNum(fn, originalAmount, env, img));
            case "tan" -> Math.tan(args.get(0).getNum(fn, originalAmount, env, img));
            case "tanh" -> Math.tanh(args.get(0).getNum(fn, originalAmount, env, img));
            case "arctan" -> Math.atan(args.get(0).getNum(fn, originalAmount, env, img));
            case "atan2" -> Math.atan2(args.get(0).getNum(fn, originalAmount, env, img), args.get(1).getNum(fn, originalAmount, env, img));
            case "csc" -> 1.0 / Math.sin(args.get(0).getNum(fn, originalAmount, env, img));
            case "csch" -> 1.0 / Math.sinh(args.get(0).getNum(fn, originalAmount, env, img));
            case "arccsc" -> 1.0 / Math.asin(args.get(0).getNum(fn, originalAmount, env, img));
            case "sec" -> 1.0 / Math.cos(args.get(0).getNum(fn, originalAmount, env, img));
            case "sech" -> 1.0 / Math.cosh(args.get(0).getNum(fn, originalAmount, env, img));
            case "arcsec" -> 1.0 / Math.acos(args.get(0).getNum(fn, originalAmount, env, img));
            case "cot" -> 1.0 / Math.tan(args.get(0).getNum(fn, originalAmount, env, img));
            case "coth" -> 1.0 / Math.tanh(args.get(0).getNum(fn, originalAmount, env, img));
            case "arccot" -> 1.0 / Math.atan(args.get(0).getNum(fn, originalAmount, env, img));

            case "len" -> args.get(0).getVec(fn, originalAmount, env, img).length();
            case "lenSqr" -> args.get(0).getVec(fn, originalAmount, env, img).lengthSquared();
            case "axial" -> Vec3d.of(getFacing(args.get(0).getVec(fn, originalAmount, env, img)).getVector());
            case "hadamard" -> args.get(0).getVec(fn, originalAmount, env, img).multiply(args.get(1).getVec(fn, originalAmount, env, img));

            case "num" -> getDoubleOutOf(args.get(0).get(fn, originalAmount, env, img));
            case "str" -> args.get(0).get(fn, originalAmount, env, img).getValue().toString();
            case "vec" -> new Vec3d(
                args.get(0).getNum(fn, originalAmount, env, img),
                args.get(1).getNum(fn, originalAmount, env, img),
                args.get(2).getNum(fn, originalAmount, env, img)
            );

            // TODO: DOCUMENT THIS (IN CASE I FORGET): the "loop" function is like creating an iterator and then using reduce() on it.
            case "loop" -> loopFn(
                args.get(0).get(fn, originalAmount, env, img),
                args.get(1),
                args.get(2),
                args.get(3),
                args.get(4),
                originalAmount,
                env,
                img
            );

            default -> throw new MathException("Oops! Big fucky wucky, a wittle fucko boingo. Function doesn't exist! Contact Luxof: " + fn);
        };

        // Fuck DRY at this point get me my mod already man
        data = data instanceof Number num ? new NumberEP((double)num)
            : data instanceof Vec3d vec ? new VecEP(vec)
            : data instanceof NbtCompound nbt ? new NBTEP(nbt)
            : data instanceof String string ? new StringEP(string)
            : data instanceof Boolean bool ? new NumberEP(bool ? 1 : 0)
            : data instanceof EquationPart ep ? ep
            : new MathException("Oops! Big fucky wucky, a wittle fucko boingo. Function returned an unsupported type! Contact Luxof: " + data.getClass().toString());
        if (data instanceof RuntimeException e) throw e;
        return (EquationPart)data;
    }
    private static Direction getFacing(Vec3d vec) {
        return Direction.getFacing(vec.x, vec.y, vec.z);
    }
    private static double getDoubleOutOf(EquationPart ep) {
        if (ep instanceof NumberEP numEp) return numEp.value;
        if (!(ep instanceof StringEP stringEp)) throw new MathException("Error interpreting math equation in function processing: \"num\" requires either a number or a string as input!");
        try {
            return Double.parseDouble(stringEp.value);
        } catch (NumberFormatException e) {
            throw new MathException("Error interpreting math equation in function processing: \"num\" requires a string that may be a valid number, but was provided \"" + stringEp.value + "\"!");
        }
    }
    private static EquationPart loopFn(
        EquationPart first,
        CommaNEP predicate,
        CommaNEP step,
        CommaNEP mapper,
        CommaNEP reducer,
        long og,
        CastingEnvironment env,
        CastingImage img
    ) {
        EquationPart i = first;
        int iCap = 1 + recursivelySearchEquationAndFindHowManyArgsAreUsed(mapper.nested, "$i");
        int oCap = 1 + recursivelySearchEquationAndFindHowManyArgsAreUsed(mapper.nested, "$o");
        EquationPart o = null;
        Map<String, Object> variables = new HashMap<>(Map.of("i0", i, "iterCount", 0));
        List<EquationPart> allOs = new ArrayList<>(10); // i don't reckon many will do more than 10
        for (
            ;
            Math.abs(predicate.getNum("loop", og, env, img, variables)) > 0.0001;
            i = stepMap(variables, step.get("loop", og, env, img, variables), o, iCap, oCap)
        ) {
            o = mapper.get("loop", og, env, img, variables);
            allOs.add(o);
        }

        EquationPart accumulator = null;
        int windowSize = recursivelySearchEquationAndFindHowManyArgsAreUsed(
            reducer.nested,
            "\\$arg"
        );
        if (windowSize == 0)
            windowSize = 1;
        for (
            int windowStart = 0;
            windowStart < allOs.size();
            windowStart += windowSize
        ) {
            variables.put("arg0", accumulator);
            for (
                int windowIndex = windowStart + 1;
                windowIndex < windowStart + windowSize;
                windowIndex++
            ) {
                variables.put("arg" + String.valueOf(windowIndex), allOs.get(windowIndex));
            }
            accumulator = reducer.get("loop", og, env, img, variables);
        }

        return accumulator;
    }
    private static int recursivelySearchEquationAndFindHowManyArgsAreUsed(
        List<EquationPart> equation,
        String prefix
    ) {
        Pattern regex = Pattern.compile(Pattern.quote(prefix)+ "\\d+");
        return equation.stream().reduce(
            0,
            (highest, ep) -> {
                if (ep instanceof NestedEP nested)
                    return Math.max(
                        highest,
                        recursivelySearchEquationAndFindHowManyArgsAreUsed(nested.nested, prefix)
                    );
                else if (ep instanceof VariableEP var)
                    return Math.max(
                        highest,
                        Stream.of(var.variable)
                            .flatMap(str -> regex.splitAsStream(str))
                            .reduce(
                                0,
                                (num, match) -> Math.max(
                                    num,
                                    Integer.parseInt(match.substring(prefix.length()))
                                ),
                                Math::max
                            )
                    );
                return highest;
            },
            Math::max
        );
    }
    private static EquationPart stepMap(
        Map<String, Object> map,
        EquationPart currI,
        EquationPart currO,
        int iCap,
        int oCap
    ) {
        int iterCount = (int)map.get("iterCount") + 1;
        map.put("iterCount", iterCount);
        for (int i = 1; i < Math.max(iCap, oCap); i--) {
            String iStr = String.valueOf(i);
            String prevIStr = String.valueOf(i - 1);
            if (i < iCap) {
                map.put(
                    "i" + iStr,
                    map.get("i" + prevIStr)
                );
            }
            if (i < oCap) {
                map.put(
                    "o" + iStr,
                    map.get("o" + prevIStr)
                );
            }
        }
        map.put("i0", currI);
        map.put("o0", currO);
        return currI;
    }
}
