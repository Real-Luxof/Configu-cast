package com.luxof.configucast;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.BooleanIota;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.ListIota;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.api.casting.iota.Vec3Iota;

import com.google.gson.JsonObject;

import static com.luxof.configucast.Configucast.LOGGER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import net.minecraft.data.client.BlockStateVariantMap.TriFunction;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import org.jetbrains.annotations.Nullable;

// don't do math kids
/** This place is not a place of honor...
 * no highly esteemed deed is commemorated here...
 * nothing valued is here. */
public class MethInterpreter {
    private static void print(List<EquationPart> equationParts) {
        String acc = "";
        for (EquationPart ep : equationParts) {
            acc += ep.strRepr() + " ";
        }
        LOGGER.info(acc);
    }


    private interface EquationPart {
        public Object getValue();
        public String strRepr();
    }
    private sealed static class NumberEP implements EquationPart permits DefaultEP {
        public double value;
        public NumberEP(double value) { this.value = value; }
        @Override public Object getValue() { return value; }
        @Override public String strRepr() { return String.valueOf(value); }
        public boolean truthy() { return !tolerates(value, 0); }
    }
    /** only produced when indexing NBT fails. consumes indexing. */
    private final static class DefaultEP extends NumberEP {
        public DefaultEP() { super(0); }
    }
    private final static class VariableEP implements EquationPart {
        public String[] variable;
        public VariableEP(String variable) {
            this.variable = variable.substring(1).split(Pattern.quote("."));
        }
        @Override public Object getValue() { return this; }
        @Override public String strRepr() { return "$" + Arrays.toString(variable); }
    }
    private static enum TypeEP implements EquationPart {
        NUMBER,
        STRING,
        VECTOR,
        NBT;

        public static TypeEP of(String type) {
            // TODO: document type names
            String lower = type.toLowerCase();
            if ("number".startsWith(lower)) return NUMBER;
            else if ("string".startsWith(lower)) return STRING;
            else if ("vector".startsWith(lower)) return VECTOR;
            else if ("nbt".startsWith(lower)) return NBT;
            else return null;
        }

        public boolean instanceOf(EquationPart ep) {
            return switch (this) {
                case NUMBER -> ep instanceof NumberEP;
                case STRING -> ep instanceof StringEP;
                case VECTOR -> ep instanceof VecEP;
                case NBT -> ep instanceof NBTEP;
            };
        }

        @Override public Object getValue() { return this; }
        @Override public String strRepr() { return this.toString(); }
    }
    private final static class FunctionEP implements EquationPart {
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
    }
    private final static class StringEP implements EquationPart {
        public String value;
        public StringEP(String value) { this.value = value; }
        @Override public Object getValue() { return value; }
        @Override public String strRepr() { return value; }
    }
    private final static class VecEP implements EquationPart {
        public Vec3d value;
        public VecEP(Vec3d value) { this.value = value; }
        @Override public Object getValue() { return value; }
        @Override public String strRepr() { return value.toString(); }
    }
    private static enum OperatorEP implements EquationPart {
        EXP, // 0
        MUL, // 1 - 3
        DIV,
        MOD,
        ADD, // 4 - 5
        SUB,
        SHIFT_LEFT, // 6 - 7
        SHIFT_RIGHT,
        LESS, // 8 - 9
        GREATER,
        LESS_EQ, // 10 - 11
        GREATER_EQ,
        INSTANCEOF, // 12
        EQ, // 13 - 14
        NEQ,
        AND, // 15 - 17
        XOR,
        OR,
        LOGICAL_AND, // 18 - 19
        LOGICAL_OR,
        TERNARY, // 20
        TERNARY_COLON;
        @Override public Object getValue() { return this; }
        @Override public String strRepr() { return this.toString(); }

        public static Map<String, OperatorEP> stringToOperatorMap = Map.ofEntries(
            Map.entry("**", EXP),
            Map.entry("*", MUL),
            Map.entry("/", DIV),
            Map.entry("%", MOD),
            Map.entry("+", ADD),
            Map.entry("-", SUB),
            Map.entry("<<", SHIFT_LEFT),
            Map.entry(">>", SHIFT_RIGHT),
            Map.entry("<", LESS),
            Map.entry(">", GREATER),
            Map.entry("<=", LESS_EQ),
            Map.entry(">=", GREATER_EQ),
            Map.entry("instanceof", INSTANCEOF),
            Map.entry("==", EQ),
            Map.entry("!=", NEQ),
            Map.entry("&", AND),
            Map.entry("^", XOR),
            Map.entry("|", OR),
            Map.entry("&&", LOGICAL_AND),
            Map.entry("||", LOGICAL_OR),
            Map.entry("?", TERNARY),
            Map.entry(":", TERNARY_COLON)
        );
    }
    private abstract static sealed class NestedEP implements EquationPart permits ParenNEP, SquareBracketNEP, CommaNEP {
        public List<EquationPart> nested;
        public NestedEP(List<EquationPart> nested) { this.nested = nested; }
        @Override public Object getValue() { return this; }
    }
    private static final class ParenNEP extends NestedEP {
        public ParenNEP(List<EquationPart> nested) { super(nested); }
        @Override public String strRepr() { return "PAREN"; }
    }
    private static final class SquareBracketNEP extends NestedEP {
        public SquareBracketNEP(List<EquationPart> nested) { super(nested); }
        @Override public String strRepr() { return "SQB"; }
    }
    private static final class NBTEP implements EquationPart {
        public NbtCompound nbt;
        public NBTEP(NbtCompound nbt) { this.nbt = nbt; }
        @Override public Object getValue() { return this.nbt; }
        @Override public String strRepr() { return "NBT"; }
    }
    private static final class CommaNEP extends NestedEP {
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
            List<EquationPart> terms = interpretMathInner(this.nested, og, env, img, variables);
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
    private static final class FunctionArgumentsEndEP implements EquationPart {
        @Override public Object getValue() { return this; }
        @Override public String strRepr() { return ")"; }
    }


    private static final Map<Identifier, List<EquationPart>> costFormulae = new HashMap<>();


    protected static void loadMathEquationsFromJson(JsonObject json) throws Exception {
        Map<Identifier, List<EquationPart>> formulaeBuffer = new HashMap<>();

        for (String key : json.keySet()) {
            Identifier id = new Identifier(key);
            if (costFormulae.containsKey(id)) throw new MathException("%s is already modified to another cost by another datapack!", key);

            formulaeBuffer.put(id, loadMathEquationFromString(json.get(key).getAsString()));
        }

        costFormulae.putAll(formulaeBuffer);
    }

    private static final Pattern equationRegex = Pattern.compile("(?<number>((?<!\\d)-)?\\d+(\\.\\d+)?)|(?<operator>\\*\\*|[\\^*\\/%+\\-<>&|?:]|<<|>>|<=|>=|==|!=|&&|\\|\\|)|[()\\[\\],]|(?<function>(?:[A-Za-z]+)\\((?=[^\\)]*\\)))|(?<variable>\\$[^ +\\-*\\/^<>&|?,\\[\\]\\(\\)]+)|(?<string>\\\"[^\\\"]*\\\")|(?<type>\\{[^}]+})|\\s+");
    public static List<EquationPart> loadMathEquationFromString(String raw) {
        // [(equation, state)] where state=1 = square bracket-confined, and state=2 = function argument
        Stack<Pair<List<EquationPart>, Integer>> equationStack = new Stack<>();
        // comments? why would you ever need another comment to read this code?
        List<EquationPart> topEquation = new ArrayList<>();
        equationStack.add(new Pair<>(topEquation, 0));

        Matcher matcher = equationRegex.matcher(raw);
        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) throw new MathException("\"%s\" is not a valid equation!", raw);
            lastEnd = matcher.end();
            String match = matcher.group();
            if (match.isBlank()) continue;

            if (matcher.group("number") != null)
                topEquation.add(new NumberEP(Double.valueOf(match)));

            else if (matcher.group("operator") != null)
                topEquation.add(OperatorEP.stringToOperatorMap.get(match));

            else if (matcher.group("variable") != null)
                topEquation.add(new VariableEP(match));

            else if (matcher.group("function") != null) {
                String functionName = match.substring(0, match.length() - 1);
                if (!FunctionEP.functionArgumentRanges.containsKey(functionName)) throw new MathException("\"%s\" is not a valid equation: unknown function (%s) used!", raw, functionName);
                topEquation.add(new FunctionEP(functionName));
                topEquation = new ArrayList<>();
                equationStack.push(new Pair<>(topEquation, 2));

            } else if (matcher.group("string") != null)
                //if (equationStack.peek().getRight() != 1) throw new MathException("\"%s\" is not a valid equation: string found outside of square brackets!", raw));
                topEquation.add(new StringEP(match.substring(1, match.length() - 1)));

            else if (matcher.group("type") != null) {
                TypeEP type = TypeEP.of(match.substring(1, match.length() - 1));
                if (type == null) throw new MathException("\"%s\" is not a valid equation: \"%s\" is not a valid type!");
                topEquation.add(type);
            }

            else if (match.equals("(")) {
                topEquation = new ArrayList<>();
                equationStack.add(new Pair<>(topEquation, 0));

            } else if (match.equals(")")) {
                var top = equationStack.peek();
                int topMode = top.getRight();
                if (equationStack.size() == 1 || topMode == 1) throw new MathException("\"%s\" is not a valid equation: parenthesis and square bracket error!", raw);

                equationStack.pop();
                topEquation = equationStack.peek().getLeft();
                topEquation.add(
                    topMode == 0 ? new ParenNEP(top.getLeft()) : new CommaNEP(top.getLeft())
                );
                if (topMode != 2) continue;

                for (int i = topEquation.size() - 1; i > 0; i--) {
                    if (!(topEquation.get(i) instanceof FunctionEP fep)) continue;
                    int args = topEquation.size() - 1 - i;
                    Pair<Integer, Integer> range = FunctionEP.functionArgumentRanges.get(fep.function);

                    if (args < range.getLeft()) throw new MathException("\"%s\" is not a valid equation: function \"%s\" is given too few arguments (%d), must have at least %d and at most %d.", raw, fep.function, args, range.getLeft(), range.getRight() - 1);
                    else if (args > range.getLeft()) throw new MathException("\"%s\" is not a valid equation: function \"%s\" is given too many arguments (%d), must have at least %d and at most %d.", raw, fep.function, args, range.getLeft(), range.getRight() - 1);
                }
                topEquation.add(new FunctionArgumentsEndEP());

            } else if (match.equals("[")) {
                topEquation = new ArrayList<>();
                equationStack.add(new Pair<>(topEquation, 1));

            } else if (match.equals("]")) {
                var top = equationStack.peek();
                if (equationStack.size() == 1 || top.getRight() != 1) throw new MathException("\"%s\" is not a valid equation: parenthesis and square bracket error!", raw);
                equationStack.pop();
                topEquation = equationStack.peek().getLeft();
                topEquation.add(new SquareBracketNEP(top.getLeft()));

            } else if (match.equals(",")) {
                var top = equationStack.peek();
                if (equationStack.size() == 1 || top.getRight() != 2) throw new MathException("\"%s\" is not a valid equation: comma found inside anything other than a function's arguments!", raw);
                equationStack.pop();
                equationStack.peek().getLeft().add(new CommaNEP(topEquation));
                topEquation = new ArrayList<>();
                equationStack.push(new Pair<>(topEquation, 2));

            }
        }

        if (equationStack.size() != 1) throw new MathException("\"%s\" is not a valid equation: unclosed parenthesis or square brackets!", raw);

        return equationStack.pop().getLeft();
    }

    @Nullable
    public static List<EquationPart> getMathFormulaFor(Identifier actionId) {
        return costFormulae.get(actionId);
    }
    public static long interpretMath(
        List<EquationPart> formula,
        long originalAmount,
        CastingEnvironment env,
        CastingImage image
    ) {
        var result = interpretMathInner(formula, originalAmount, env, image);
        if (result.size() > 1 || !(result.get(0) instanceof NumberEP nep))
            throw new MathException("Desired one number term, equation returned multiple terms or a non-number result upon simplification.");
        return (long)(nep.value * 10000);
    }

    public static List<EquationPart> interpretMathInner(
        List<EquationPart> formula,
        long originalAmount,
        CastingEnvironment env,
        CastingImage image
    ) {
        return interpretMathInner(formula, originalAmount, env, image, Map.of());
    }

    public static List<EquationPart> interpretMathInner(
        List<EquationPart> formula,
        long originalAmount,
        CastingEnvironment env,
        CastingImage image,
        Map<String, Object> variables
    ) {
        // i will make "5 (random() < 0.5 ? + : -) 2" into valid syntax
        List<CommaNEP> fnArgs = new ArrayList<>();
        // TODO: profile this (instanceof scares me, also 12 for-loops :sob:)
        List<EquationPart> equation = new ArrayList<>(formula.stream()
            .flatMap(ep -> {
                if (!fnArgs.isEmpty() && !(ep instanceof FunctionArgumentsEndEP)) fnArgs.add((CommaNEP)ep);

                if (ep instanceof FunctionEP) {
                    fnArgs.add(new CommaNEP(List.of(ep)));
                } else if (ep instanceof FunctionArgumentsEndEP) {
                    EquationPart ret = computeFunction(((FunctionEP)fnArgs.remove(0).nested.get(0)).function, fnArgs, originalAmount, env, image);
                    fnArgs.clear();
                    return Stream.of(ret);
                }

                if (!fnArgs.isEmpty()) return Stream.of();

                return ep instanceof NestedEP nep
                    ? Stream.of(
                        ep instanceof SquareBracketNEP
                            ? new EquationPart[] { new SquareBracketNEP(interpretMathInner(nep.nested, originalAmount, env, image)) }
                            : interpretMathInner(nep.nested, originalAmount, env, image, variables).toArray(new EquationPart[0])
                    )
                    : Stream.of(
                        ep instanceof VariableEP vep
                            ? dereferenceVariable(vep, originalAmount, env, image, variables)
                            : ep
                    );
            })
            .toList()
        );

        List<EquationPart> prevEquation;
        do {
            prevEquation = List.copyOf(equation);
            walkEquation(equation);
        } while (!prevEquation.equals(equation));

        return equation;
    }
    private static boolean isInteger(double num) {
        return tolerates(num, Math.floor(num));
    }
    private static boolean tolerates(double A, double B) {
        return Math.abs(A - B) < 0.0001;
    }

    private static EquationPart computeFunction(
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
            !tolerates(predicate.getNum("loop", og, env, img, variables), 0);
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

    private static EquationPart dereferenceVariable(VariableEP vep, long originalAmount, CastingEnvironment env, CastingImage img) {
        return dereferenceVariable(vep, originalAmount, env, img, Map.of());
    }
    private static EquationPart dereferenceVariable(VariableEP vep, long originalAmount, CastingEnvironment env, CastingImage img, Map<String, Object> variables) {
        // initialData... like, INITIAL-D, THE ARCADE GAMES?!?!?!??!?!?!?!?!
        Object data;
        String first = vep.variable[0];
        try {
            List<Iota> stack = img.getStack();
            data = stack.get(stack.size() - 1 - Integer.parseInt(first));
        } catch (NumberFormatException e) {
            data = variables.containsKey(first) ? variables.get(first) : switch (first) {
                case "originalAmount" -> (double)originalAmount / 10000.0;
                case "casterExists" -> env.getCastingEntity() != null ? 1.0 : 0.0;
                case "caster" -> env.getCastingEntity();
                case "mishapSprayPos" -> env.mishapSprayPos();
                case "enlightened" -> env.isEnlightened() ? 1.0 : 0.0;
                case "totalMedia" -> (Long.MAX_VALUE - env.extractMedia(Long.MAX_VALUE, true)) / 10000.0;
                case "castingHand" -> env.getCastingHand().ordinal();
                case "otherHand" -> env.getOtherHand().ordinal();
                case "parenCount" -> img.getParenCount();
                case "parenthesized" -> img.getParenthesized();
                // unrequired, i think
                //case "escapeNext" -> img.getEscapeNext() ? 1.0 : 0.0;
                case "userData" -> img.getUserData();
                default -> throw new MathException("Failed to interpret math equation due to unknown variable: \"" + first + "\"");
            };
        }

        for (String attribute : vep.variable) {
            if (attribute == first) continue;

            data = deIotaThisIotaIfPossible(data);


            if (data instanceof LivingEntity living)
                data = switch (attribute) {
                    case "width" -> living.getWidth();
                    case "height" -> living.getHeight();
                    case "pos" -> living.getPos();
                    case "eyepos" -> living.getEyePos();
                    case "lookdir" -> living.getRotationVector();
                    case "age" -> living.age;
                    case "uuid" -> living.getUuidAsString();
                    case "preferredHand" -> living.preferredHand.ordinal();
                    case "maxHealth" -> living.getMaxHealth();
                    case "health" -> living.getHealth();
                    case "armorAttrib" -> living.getArmor();
                    case "equipped" -> getEquipped(living);
                    case "velocity" -> living.getVelocity();
                    case "fallDistance" -> living.fallDistance;
                    case "noClip" -> living.noClip ? 1.0 : 0.0;
                    case "statusEffects" -> List.copyOf(living.getStatusEffects());
                    case "airTicks" -> living.getAir();
                    case "frozenTicks" -> living.getFrozenTicks();
                    case "inPowderSnow" -> living.inPowderSnow;
                    case "isOnFire" -> living.isOnFire();
                    case "isGlowing" -> living.isGlowing();
                    case "isBlocking" -> living.isBlocking();
                    case "isSwimming" -> living.isSwimming();
                    case "isCrawling" -> living.isCrawling();
                    case "isSneaking" -> living.isSneaking();
                    case "isFallFlying" -> living.isFallFlying();
                    case "isFireImmune" -> living.isFireImmune();
                    case "isInLava" -> living.isInLava();
                    case "isInvisible" -> living.isInvisible();
                    case "isOnGround" -> living.isOnGround();
                    case "isOnRail" -> living.isOnRail();
                    case "isSleeping" -> living.isSleeping();
                    case "isSprinting" -> living.isSprinting();
                    case "isTouchingWater" -> living.isTouchingWater();
                    // NOTE: private methods, for some reason
                    //case "isBeingRainedOn" -> living.isBeingRainedOn();
                    //case "isInsideBubbleColumn" -> living.isInsideBubbleColumn();
                    case "isTouchingWaterOrRain" -> living.isTouchingWaterOrRain();
                    case "isInsideWaterOrBubbleColumn" -> living.isInsideWaterOrBubbleColumn();
                    case "isSubmergedInWater" -> living.isSubmergedInWater();
                    case "isSubmergedInLiquid" -> living.isSubmergedInWater() || living.isInLava();
                    case "isWet" -> living.isWet();
                    default -> throw new MathException("Failed to interpret math equation in variable-accessing: entities have no attribute named \"" + attribute + "\".");
                };

            else if (data instanceof Vec3d vec)
                data = switch (attribute) {
                    case "x" -> vec.x;
                    case "y" -> vec.y;
                    case "z" -> vec.z;
                    case "normal" -> vec.normalize();
                    case "negate" -> vec.negate();
                    default -> throw new MathException("Failed to interpret math equation in variable-accessing: vectors have no attribute named \"" + attribute + "\".");
                };

            else if (data instanceof List<?> list) {
                try {
                    int index;
                    if (attribute.startsWith("$")) {
                        EquationPart varValue = dereferenceVariable(
                            new VariableEP(attribute),
                            originalAmount,
                            env,
                            img
                        );
                        if (varValue instanceof NumberEP num && isInteger(num.value)) index = (int)Math.floor(num.value);
                        else throw new MathException(String.format("Failed to interpret math equation in variable-accessing: mini variable-access for a list may NOT return anything but an integer."));

                    } else index = Integer.parseInt(attribute);

                    // to support modulus without actually supporting modulus, i loop around
                    data = list.get(index % list.size());

                } catch (NumberFormatException e) {
                    if (attribute.equals("size")) data = list.size();
                    else throw new MathException("Failed to interpret math equation in variable-accessing: lists have no attribute named \"" + attribute + "\".");
                }

            } else if (data instanceof ItemStack itemStack)
                data = switch (attribute) {
                    case "item" -> itemStack.getItem().getName().toString();
                    case "count" -> itemStack.getCount();
                    case "damage" -> itemStack.getDamage();
                    case "durability" -> itemStack.getMaxDamage() - itemStack.getDamage();
                    case "maxDurability" -> itemStack.getMaxDamage();
                    case "hasCustomName" -> itemStack.hasCustomName();
                    case "hasEnchantments" -> itemStack.hasEnchantments();
                    case "hasGlint" -> itemStack.hasGlint();
                    case "hasDurability" -> itemStack.isDamageable();
                    case "isDamaged" -> itemStack.isDamaged();
                    case "isEmpty" -> itemStack.isEmpty();
                    case "isEnchantable" -> itemStack.isEnchantable();
                    case "isFood" -> itemStack.isFood();
                    case "isStackable" -> itemStack.isStackable();
                    default -> throw new MathException("Failed to interpret math equation in variable-accessing: item stacks have no attribute named \"" + attribute + "\".");
                };

            else if (data instanceof StatusEffectInstance statusEffect)
                data = switch (attribute) {
                    case "duration" -> statusEffect.getDuration();
                    case "level" -> statusEffect.getAmplifier();
                    // "is this given by a beacon or conduit?" (semi-transparent particles)
                    case "isAmbient" -> statusEffect.isAmbient();
                    case "showIcon" -> statusEffect.shouldShowIcon();
                    case "showParticles" -> statusEffect.shouldShowParticles();
                    default -> throw new MathException("Failed to interpret math equation in variable-accessing: status effects have no attribute named \"" + attribute + "\".");
                };

            else
                throw new MathException("Failed to interpret math equation in variable-accessing: this data type has no attributes to access, so you may not access \"" + attribute + "\" within it.");
        }

        data = deIotaThisIotaIfPossible(data);
        data = data instanceof Number num ? new NumberEP((double)num)
            : data instanceof LivingEntity ? new MathException("Failed to interpret math equation in variable-accessing: variable accessing may NOT return entities (no support).")
            : data instanceof Vec3d vec ? new VecEP(vec)
            : data instanceof List<?> ? new MathException("Failed to interpret math equation in variable-accessing: variable accessing may NOT return lists (no support).")
            : data instanceof NbtCompound nbt ? new NBTEP(nbt)
            : data instanceof ItemStack ? new MathException("Failed to interpret math equation in variable-accessing: variable accessing may NOT return item stacks (no support).")
            : data instanceof String string ? new StringEP(string)
            : data instanceof StatusEffectInstance ? new MathException("Failed to interpret math equation in variable-accessing: variable accessing may NOT return status effects (no support).")
            : data == null ? new NumberEP(0) // yes, the default case for non-existent variables (null) is 0.
            : new MathException("Oops! Big fucky wucky, a wittle fucko boingo. Contact Luxof: " + data.getClass().toString());
        if (data instanceof RuntimeException e) throw e;
        return (EquationPart)data;
    }
    private static Object deIotaThisIotaIfPossible(Object data) {
        return data instanceof EquationPart ep
            ? (ep instanceof NumberEP numEp ? numEp.value
            : ep instanceof StringEP stringEp ? stringEp.value
            : ep instanceof VecEP vecEp ? vecEp.value
            : ep instanceof NBTEP nbtEp ? nbtEp.nbt : data)
            : data instanceof Iota
            ? (data instanceof NullIota ? null
            : data instanceof DoubleIota i ? i.getDouble()
            : data instanceof BooleanIota i ? i.getBool() ? 1.0 : 0.0
            : data instanceof EntityIota i && i.getEntity() instanceof LivingEntity living ? living
            : data instanceof ListIota i ? getList(i)
            : data instanceof Vec3Iota i ? i.getVec3() : data)
            : data;
    }
    private static List<Iota> getList(ListIota listIota) {
        List<Iota> list = new ArrayList<>();
        listIota.getList().forEach(list::add);
        return List.copyOf(list);
    }
    private static List<ItemStack> getEquipped(LivingEntity living) {
        EquipmentSlot[] armorSlots = EquipmentSlot.values();
        ItemStack[] armor = new ItemStack[armorSlots.length];
        for (int i = 0; i < armorSlots.length; i++) {
            armor[i] = living.getEquippedStack(armorSlots[i]);
        }
        return List.of(armor);
    }

    private static void walkEquation(
        List<EquationPart> equation
    ) {
        print(equation);
        // first off, kill all the square brackets. Also do exponentiation.
        for (int i = 0; i < equation.size() - 1; i++) {
            EquationPart currEp = equation.get(i);
            EquationPart nextEp = equation.get(i + 1);

            while (nextEp instanceof OperatorEP op && op == OperatorEP.EXP) {
                if (i < equation.size() - 2) continue;
                EquationPart expon = equation.get(i + 2);
                if (!(currEp instanceof NumberEP num1) || !(expon instanceof NumberEP num2)) continue;
                equation.remove(i + 1);
                equation.remove(i + 1);
                equation.set(i, new NumberEP(Math.pow(num1.value, num2.value)));
                nextEp = i + 1 < equation.size() ? equation.get(i) : null;
            }

            while (nextEp instanceof SquareBracketNEP squareBrackets) {
                currEp = equation.get(i);
                EquationPart index = squareBrackets.nested.get(0);

                if (currEp instanceof NBTEP nbt) {
                    if (!(index instanceof StringEP string)) throw new MathException("Error while interpreting math equation in indexing NBT: NBT may only be accessed by strings, not numbers.");
                    NbtElement element = nbt.nbt.get(string.value);
                    equation.remove(i + 1);

                    equation.set(
                        i,
                        element == null ? new DefaultEP()
                        : switch (element.getType()) {
                            case NbtElement.COMPOUND_TYPE -> new NBTEP((NbtCompound)element);
                            case NbtElement.NUMBER_TYPE -> new NumberEP(((AbstractNbtNumber)element).doubleValue());
                            case NbtElement.BYTE_TYPE -> new NumberEP(((NbtByte)element).doubleValue());
                            case NbtElement.STRING_TYPE -> new StringEP(((NbtString)element).asString());
                            default -> throw new MathException("Error while interpreting math equation in indexing NBT: returned a type the interpreter does not support (expected NBT, a number, or a string).");
                        }
                    );

                } else if (currEp instanceof StringEP string) {
                    if (
                        !(index instanceof NumberEP num1) ||
                        !isInteger(num1.value) ||
                        // TODO: DON'T FORGET TO DOCUMENT THIS EVIL BULLSHIT
                        squareBrackets.nested.size() != 2 ||
                        !(squareBrackets.nested.get(1) instanceof NumberEP num2) ||
                        !isInteger(num2.value)
                    )
                        throw new MathException("Error while interpreting math equation in indexing a String: Strings may only be indexed by a pair of integers (inclusive and exclusive).");
                    equation.set(
                        i,
                        new StringEP(string.value.substring((int)num1.value, (int)num2.value))
                    );
                    equation.remove(i + 1);

                } else if (currEp instanceof DefaultEP) equation.remove(i + 1);
                else break;

                nextEp = i + 1 < equation.size() ? equation.get(i + 1) : null;
            }
        }

        print(equation);
        if (equation.size() == 1) return; // optimize
        operate(
            equation,
            1, 3,
            (curr, op, seco) -> op == OperatorEP.MUL ? multiply(curr, seco)
                : op == OperatorEP.DIV ? divide(curr, seco)
                : op == OperatorEP.MOD ? modulo(curr, seco)
                : null
        );

        print(equation);
        if (equation.size() == 1) return;
        operate(
            equation,
            4, 5,
            (curr, op, seco) -> op == OperatorEP.ADD ? add(curr, seco)
                : op == OperatorEP.SUB ? sub(curr, seco)
                : null
        );

        print(equation);
        if (equation.size() == 1) return;
        operateOnInts(
            equation,
            6, 7,
            (curr, op, seco) -> new NumberEP(
                op == OperatorEP.SHIFT_LEFT ? curr << seco : curr >> seco
            )
        );

        print(equation);
        if (equation.size() == 1) return;
        operateOnNums(
            equation,
            8, 9,
            (curr, op, seco) -> new NumberEP(
                op == OperatorEP.LESS ? curr < seco ? 1 : 0 : curr > seco ? 1 : 0
            )
        );

        print(equation);
        if (equation.size() == 1) return;
        operateOnNums(
            equation,
            10, 11,
            (curr, op, seco) -> new NumberEP(
                op == OperatorEP.LESS_EQ ? curr <= seco ? 1 : 0 : curr >= seco ? 1 : 0
            )
        );

        print(equation);
        if (equation.size() == 1) return;
        operate(
            equation,
            12, 12,
            (curr, op, seco) -> seco instanceof TypeEP type
                ? new NumberEP(type.instanceOf(curr) ? 1 : 0) : null
        );

        print(equation);
        if (equation.size() == 1) return;
        operate(
            equation,
            13, 14,
            (curr, op, seco) -> new NumberEP(
                op == OperatorEP.EQ
                    ? curr.getValue().equals(seco.getValue()) ? 1 : 0
                    : !curr.getValue().equals(seco.getValue()) ? 1 : 0
            )
        );

        print(equation);
        if (equation.size() == 1) return;
        operateOnInts(
            equation,
            15, 17,
            (curr, op, seco) -> new NumberEP(
                op == OperatorEP.AND ? curr & seco
                : op == OperatorEP.OR ? curr | seco
                : op == OperatorEP.XOR ? curr ^ seco
                : null
            )
        );

        print(equation);
        if (equation.size() == 1) return;
        operateOnNumEPs(
            equation,
            18, 19,
            (curr, op, seco) -> new NumberEP(
                (op == OperatorEP.LOGICAL_AND
                    ? curr.truthy() && seco.truthy() : curr.truthy() || seco.truthy())
                    ? 1 : 0
            )
        );

        print(equation);
        if (equation.size() == 1) return;
        int inc = 0;
        for (int i = 0; i < equation.size() - 1; i += inc) {
            inc = 1;
            EquationPart here = equation.get(i);
            EquationPart next = equation.get(i + 1);
            if (!(
                next instanceof OperatorEP op && op == OperatorEP.TERNARY &&
                here instanceof NumberEP bool
            )) continue;
            int colon1 = 0;
            int colon2 = 0;
            int colonCount = 0;

            for (int idx = i + 2; idx < equation.size(); idx++) {
                EquationPart comp = equation.get(idx);
                if (comp instanceof OperatorEP possibly && possibly == OperatorEP.TERNARY_COLON) {
                    colonCount++;
                    if (colonCount == 2) break;
                    continue;
                }

                if (colonCount == 0) colon1++;
                else colon2++;
            }

            if (colonCount != 2) break;

            equation.remove(i);
            equation.remove(i);
            equation.remove(i + colon1 + colon2 + 1);
            if (bool.truthy()) {
                for (int I = 0; I <= colon2; I++) {
                    equation.remove(i + colon1);
                }
            } else {
                for (int I = 0; I <= colon1; I++) {
                    equation.remove(i);
                }
            }
            inc = 0;
        }
        print(equation);
    }

    private static void operateOnNumEPs(
        List<EquationPart> equation,
        int lowerBoundOnOperator,
        int upperBoundOnOperator,
        TriFunction<NumberEP, OperatorEP, NumberEP, EquationPart> operator
    ) {
        operate(
            equation,
            lowerBoundOnOperator, upperBoundOnOperator,
            (curr, op, seco) -> curr instanceof NumberEP num1 && seco instanceof NumberEP num2
                ? operator.apply(num1, op, num2) : null
        );
    }
    private static void operateOnNums(
        List<EquationPart> equation,
        int lowerBoundOnOperator,
        int upperBoundOnOperator,
        TriFunction<Double, OperatorEP, Double, EquationPart> operator
    ) {
        operateOnNumEPs(
            equation,
            lowerBoundOnOperator, upperBoundOnOperator,
            (num1, op, num2) -> operator.apply(num1.value, op, num2.value)
        );
    }
    private static void operateOnInts(
        List<EquationPart> equation,
        int lowerBoundOnOperator,
        int upperBoundOnOperator,
        TriFunction<Long, OperatorEP, Long, EquationPart> operator
    ) {
        operateOnNums(
            equation,
            lowerBoundOnOperator, upperBoundOnOperator,
            (num1, op, num2) -> tolerates(num1, (long)(double)num1) && tolerates(num2, (long)(double)num2)
                ? operator.apply((long)(double)num1, op, (long)(double)num2) : null
        );
    }
    private static void operate(
        List<EquationPart> equation,
        int lowerBoundOnOperator,
        int upperBoundOnOperator,
        TriFunction<EquationPart, OperatorEP, EquationPart, EquationPart> operator
    ) {
        int inc = 0;
        for (int i = 0; i < equation.size() - 2; i += inc) {
            inc = 1;
            EquationPart currEp = equation.get(i);
            EquationPart nextEp = equation.get(i + 1);
            EquationPart secoEp = equation.get(i + 2);

            if (!(
                nextEp instanceof OperatorEP op &&
                op.ordinal() >= lowerBoundOnOperator && op.ordinal() <= upperBoundOnOperator
            ))
                continue;

            EquationPart result = operator.apply(currEp, op, secoEp);
            if (result != null) {
                equation.set(i, result);
                equation.remove(i + 1);
                equation.remove(i + 1);
            }
            inc = 0; // incase the next "nextEp" is an eligible operator
        }
    }

    private static EquationPart multiply(
        EquationPart currEp,
        EquationPart secoEp
    ) {
        // ensure there's a number for the first arg
        EquationPart first = currEp instanceof NumberEP ? currEp : secoEp;
        EquationPart secon = currEp instanceof NumberEP ? secoEp : currEp;
        if (first instanceof NumberEP num1) {
            if (secon instanceof NumberEP num2)
                return new NumberEP(num1.value * num2.value);
            if (secon instanceof StringEP str2)
                return new StringEP(multiplyString(str2.value, num1.value));
            else if (secon instanceof VecEP vec2)
                return new VecEP(vec2.value.multiply(num1.value));
            else
                return null;

        } else if (first instanceof VecEP vec1) {
            if (!(secon instanceof VecEP vec2)) return null;
            return new NumberEP(vec1.value.dotProduct(vec2.value));

        } else return null;
    }
    private static String multiplyString(String string, double times) {
        String newStr = "";
        for (int i = 0; i < Math.floor(times); i++) {
            newStr += string;
        }
        // TODO: DON'T FORGET TO DOCUMENT THIS WEIRD STRING PROCESSING AS WELL
        newStr = newStr + string.substring(0, (int)Math.floor(string.length() * times % 1));
        return newStr;
    }

    private static EquationPart divide(
        EquationPart currEp,
        EquationPart secoEp
    ) {
        // ensure there's a number for the second arg
        EquationPart first = secoEp instanceof NumberEP ? currEp : secoEp;
        EquationPart secon = secoEp instanceof NumberEP ? secoEp : currEp;
        if (first instanceof NumberEP num1) {
            if (!(secon instanceof NumberEP num2)) return null;
            return new NumberEP(num1.value / num2.value);

        } else if (first instanceof VecEP vec1) {
            if (secon instanceof NumberEP num2)
                return new VecEP(vec1.value.multiply(1.0 / num2.value));
            else if (secon instanceof VecEP vec2)
                return new VecEP(vec1.value.crossProduct(vec2.value));
            else
                return null;

        } else return null;
    }
    private static EquationPart modulo(
        EquationPart currEp,
        EquationPart secoEp
    ) {
        return currEp instanceof NumberEP num1 && secoEp instanceof NumberEP num2
            ? new NumberEP(num1.value % num2.value) : null;
    }
    private static EquationPart add(
        EquationPart currEp,
        EquationPart secoEp
    ) {
        return currEp instanceof NumberEP num1 && secoEp instanceof NumberEP num2 ? new NumberEP(num1.value + num2.value)
            : currEp instanceof StringEP str1 && secoEp instanceof StringEP str2 ? new StringEP(str1.value + str2.value)
            : null;
    }
    private static EquationPart sub(
        EquationPart currEp,
        EquationPart secoEp
    ) {
        return currEp instanceof NumberEP num1 && secoEp instanceof NumberEP num2
            ? new NumberEP(num1.value - num2.value) : null;
    }
}
