package com.luxof.configucast.meth.equationparts;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.iota.NullIota;

import com.luxof.configucast.meth.MathException;

import static com.luxof.configucast.Configucast.prepareVariables;
import static com.luxof.configucast.Configucast.runningServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

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
        Map.entry("nbt", new Pair<>(2, 999)),
        Map.entry("iota", new Pair<>(1, 2)),

        Map.entry("loop", new Pair<>(5, 6)),
        Map.entry("sum", new Pair<>(2, 3)),
        Map.entry("prod", new Pair<>(2, 3))
    );

    public static EquationPart computeFunction(
        String fn,
        List<CommaNEP> args,
        long originalAmount,
        CastingEnvironment env,
        CastingImage img
    ) {
        Map<String, Object> variables = new HashMap<>();
        prepareVariables(originalAmount, env, img, variables);
        return computeFunction(fn, args, variables);
    }

    public static EquationPart computeFunction(
        String fn,
        List<CommaNEP> args,
        Map<String, Object> variables
    ) {
        Object data = switch (fn) {
            case "neg" -> ~((long)args.get(0).getNum(fn, variables));
            case "floor" -> Math.floor(args.get(0).getNum(fn, variables));
            case "ceil" -> Math.ceil(args.get(0).getNum(fn, variables));
            case "round" -> Math.round(args.get(0).getNum(fn, variables));
            case "max" -> args.stream().map(a -> a.getNum(fn, variables)).reduce(Math::max);
            case "min" -> args.stream().map(a -> a.getNum(fn, variables)).reduce(Math::min);
            case "log" -> Math.log(args.get(0).getNum(fn, variables)) / Math.log(args.get(1).getNum(fn, variables));
            case "sqrt" -> Math.sqrt(args.get(0).getNum(fn, variables));
            case "abs" -> Math.abs(args.get(0).getNum(fn, variables));
            case "signum" -> Math.signum(args.get(0).getNum(fn, variables));
            case "degrees" -> Math.toDegrees(args.get(0).getNum(fn, variables));
            case "radians" -> Math.toRadians(args.get(0).getNum(fn, variables));
            case "root" -> Math.pow(args.get(0).getNum(fn, variables), 1.0 / args.get(1).getNum(fn, variables));
            case "random" -> Math.random();

            case "sin" -> Math.sin(args.get(0).getNum(fn, variables));
            case "sinh" -> Math.sinh(args.get(0).getNum(fn, variables));
            case "arcsin" -> Math.asin(args.get(0).getNum(fn, variables));
            case "cos" -> Math.cos(args.get(0).getNum(fn, variables));
            case "cosh" -> Math.cosh(args.get(0).getNum(fn, variables));
            case "arccos" -> Math.acos(args.get(0).getNum(fn, variables));
            case "tan" -> Math.tan(args.get(0).getNum(fn, variables));
            case "tanh" -> Math.tanh(args.get(0).getNum(fn, variables));
            case "arctan" -> Math.atan(args.get(0).getNum(fn, variables));
            case "atan2" -> Math.atan2(args.get(0).getNum(fn, variables), args.get(1).getNum(fn, variables));
            case "csc" -> 1.0 / Math.sin(args.get(0).getNum(fn, variables));
            case "csch" -> 1.0 / Math.sinh(args.get(0).getNum(fn, variables));
            case "arccsc" -> 1.0 / Math.asin(args.get(0).getNum(fn, variables));
            case "sec" -> 1.0 / Math.cos(args.get(0).getNum(fn, variables));
            case "sech" -> 1.0 / Math.cosh(args.get(0).getNum(fn, variables));
            case "arcsec" -> 1.0 / Math.acos(args.get(0).getNum(fn, variables));
            case "cot" -> 1.0 / Math.tan(args.get(0).getNum(fn, variables));
            case "coth" -> 1.0 / Math.tanh(args.get(0).getNum(fn, variables));
            case "arccot" -> 1.0 / Math.atan(args.get(0).getNum(fn, variables));

            case "len" -> args.get(0).getVec(fn, variables).length();
            case "lenSqr" -> args.get(0).getVec(fn, variables).lengthSquared();
            case "axial" -> Vec3d.of(getFacing(args.get(0).getVec(fn, variables)).getVector());
            case "hadamard" -> args.get(0).getVec(fn, variables).multiply(args.get(1).getVec(fn, variables));

            case "num" -> getDoubleOutOf(args.get(0).getStr(fn, variables));
            case "str" -> args.get(0).get(fn, variables).getValue().toString();
            case "vec" -> new Vec3d(
                args.get(0).getNum(fn, variables),
                args.get(1).getNum(fn, variables),
                args.get(2).getNum(fn, variables)
            );
            case "nbt" -> nbtFn(args, variables);
            case "iota" -> VariableEP.deIotaThisIotaIfPossible(deserializeIotaNbt(
                args.get(0).getNBT(fn, variables)
            ));

            case "loop" -> loopFn(
                args.get(0).get(fn, variables),
                args.get(1),
                args.get(2),
                args.get(3),
                args.get(4),
                variables
            );
            case "sum" -> sumFn(
                args.get(0),
                args.get(1),
                variables
            );
            case "prod" -> prodFn(
                args.get(0),
                args.get(1),
                variables
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
            : (fn.equals("iota")
                ? new MathException("Error interpreting math equation in function processing: \"iota\" returned an unsupported type: " + data.getClass().toString())
                : new MathException("Oops! Big fucky wucky, a wittle fucko boingo. Function returned an unsupported type! Contact Luxof: " + data.getClass().toString()));
        if (data instanceof RuntimeException e) throw e;
        return (EquationPart)data;
    }
    private static Direction getFacing(Vec3d vec) {
        return Direction.getFacing(vec.x, vec.y, vec.z);
    }
    private static double getDoubleOutOf(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            throw new MathException("Error interpreting math equation in function processing: \"num\" requires a string that may be a valid number, but was provided \"" + str + "\"!");
        }
    }

    private static NbtCompound nbtFn(
        List<CommaNEP> args,
        Map<String, Object> variables
    ) {
        NbtCompound nbt = new NbtCompound();

        if (args.size() % 2 == 1)
            throw new MathException("Error interpreting math equation in function processing: \"nbt\" function requires an even number of arguments.");
        for (int i = 0; i < args.size(); i += 2) {
            String key = args.get(i).getStr("nbt", variables);
            EquationPart value = args.get(i + 1).get("nbt", variables);

            if (value instanceof NumberEP numEp)
                nbt.putDouble(key, numEp.value);

            else if (value instanceof VecEP vecEp) {
                Vec3d vector = vecEp.value;
                NbtCompound vectorNbt = new NbtCompound();
                vectorNbt.putDouble("x", vector.x);
                vectorNbt.putDouble("y", vector.y);
                vectorNbt.putDouble("z", vector.z);
                nbt.put(key, vectorNbt);

            } else if (value instanceof StringEP strEp)
                nbt.putString(key, strEp.value);

            else if (value instanceof NBTEP nbtEp)
                nbt.put(key, nbtEp.nbt);
        }

        return nbt;
    }

    private static Iota deserializeIotaNbt(
        NbtCompound nbt
    ) {
        if (runningServer == null)
            throw new MathException("Error interpreting math equation in function processing: \"iota\" function requires being ran while a server is active. Hi mod dev!");

        // couldn't be arsed, i'll just pull the server from a global
        for (RegistryKey<World> worldKey : runningServer.getWorldRegistryKeys()) {
            Iota iota = IotaType.deserialize(nbt, runningServer.getWorld(worldKey));
            if (!(iota instanceof NullIota)) return iota;
        }
        return new NullIota();
    }


    private static EquationPart loopFn(
        EquationPart first,
        CommaNEP predicate,
        CommaNEP step,
        CommaNEP mapper,
        CommaNEP reducer,
        Map<String, Object> outerVariables
    ) {
        EquationPart i = first;
        int iCap = 1 + recursivelySearchEquationAndFindHowManyArgsAreUsed(mapper.nested, "$i");
        int oCap = 1 + recursivelySearchEquationAndFindHowManyArgsAreUsed(mapper.nested, "$o");
        EquationPart o = null;

        Map<String, Object> variables = new HashMap<>(outerVariables);
        variables.put("i0", i);
        variables.put("iterCount", 0);

        List<EquationPart> allOs = new ArrayList<>(10); // i don't reckon many will do more than 10
        for (
            ;
            Math.abs(predicate.getNum("loop", variables)) > 0.0001;
            i = stepMap(variables, step.get("loop", variables), o, iCap, oCap)
        ) {
            o = mapper.get("loop", variables);
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
                windowIndex < windowStart + windowSize && windowIndex < allOs.size();
                windowIndex++
            ) {
                variables.put("arg" + String.valueOf(windowIndex), allOs.get(windowIndex));
            }
            accumulator = reducer.get("loop", variables);
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

    private static EquationPart sumFn(
        CommaNEP condition,
        CommaNEP selector,
        Map<String, Object> variables
    ) {
        double sum = 0.0;
        for (
            int i = 0;
            Math.abs(condition.getNum("sum", variables)) > 0.0001;
            i++
        ) {
            variables.put("i", i);
            sum += selector.getNum("sum", variables);
        }
        return new NumberEP(sum);
    }

    private static EquationPart prodFn(
        CommaNEP condition,
        CommaNEP selector,
        Map<String, Object> variables
    ) {
        double prod = 0.0;
        for (
            int i = 0;
            Math.abs(condition.getNum("prod", variables)) > 0.0001;
            i++
        ) {
            variables.put("i", i);
            prod *= selector.getNum("prod", variables);
        }
        return new NumberEP(prod);
    }
}
