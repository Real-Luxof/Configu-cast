package com.luxof.configucast.meth;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;

import com.luxof.configucast.meth.equationparts.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.data.client.BlockStateVariantMap.TriFunction;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;

// don't do math kids
public class MethInterpreter {
    public static long simplifyToNum(
        List<EquationPart> formula,
        long originalAmount,
        CastingEnvironment env,
        CastingImage image
    ) {
        var result = simplify(formula, originalAmount, env, image);
        if (result.size() > 1 || !(result.get(0) instanceof NumberEP nep))
            throw new MathException("Desired one number term, equation returned multiple terms or a non-number result upon simplification.");
        return (long)(nep.value * 10000);
    }

    public static List<EquationPart> simplify(
        List<EquationPart> formula,
        long originalAmount,
        CastingEnvironment env,
        CastingImage image
    ) {
        return simplify(formula, originalAmount, env, image, Map.of());
    }

    public static List<EquationPart> simplify(
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
                    EquationPart ret = FunctionEP.computeFunction(
                        ((FunctionEP)fnArgs.remove(0).nested.get(0)).function,
                        fnArgs,
                        originalAmount,
                        env,
                        image
                    );
                    fnArgs.clear();
                    return Stream.of(ret);
                }

                if (!fnArgs.isEmpty()) return Stream.of();

                return ep instanceof NestedEP nep
                    ? Stream.of(
                        ep instanceof SquareBracketNEP
                            ? new EquationPart[] { new SquareBracketNEP(simplify(nep.nested, originalAmount, env, image)) }
                            : simplify(nep.nested, originalAmount, env, image, variables).toArray(new EquationPart[0])
                    )
                    : Stream.of(
                        ep instanceof VariableEP vep
                            ? VariableEP.dereferenceVariable(vep, originalAmount, env, image, variables)
                            : ep
                    );
            })
            .toList()
        );

        List<EquationPart> prevEquation;
        do {
            prevEquation = List.copyOf(equation);
            stepThroughEquationOnce(equation);
        } while (!prevEquation.equals(equation));

        return equation;
    }

    public static void stepThroughEquationOnce(
        List<EquationPart> equation
    ) {
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

        if (equation.size() == 1) return; // optimize
        operate(
            equation,
            1, 3,
            (curr, op, seco) -> op == OperatorEP.MUL ? OperatorEP.multiply(curr, seco)
                : op == OperatorEP.DIV ? OperatorEP.divide(curr, seco)
                : op == OperatorEP.MOD ? OperatorEP.modulo(curr, seco)
                : null
        );

        if (equation.size() == 1) return;
        operate(
            equation,
            4, 5,
            (curr, op, seco) -> op == OperatorEP.ADD ? OperatorEP.add(curr, seco)
                : op == OperatorEP.SUB ? OperatorEP.sub(curr, seco)
                : null
        );

        if (equation.size() == 1) return;
        operateOnInts(
            equation,
            6, 7,
            (curr, op, seco) -> new NumberEP(
                op == OperatorEP.SHIFT_LEFT ? curr << seco : curr >> seco
            )
        );

        if (equation.size() == 1) return;
        operateOnNums(
            equation,
            8, 9,
            (curr, op, seco) -> new NumberEP(
                op == OperatorEP.LESS ? curr < seco ? 1 : 0 : curr > seco ? 1 : 0
            )
        );

        if (equation.size() == 1) return;
        operateOnNums(
            equation,
            10, 11,
            (curr, op, seco) -> new NumberEP(
                op == OperatorEP.LESS_EQ ? curr <= seco ? 1 : 0 : curr >= seco ? 1 : 0
            )
        );

        if (equation.size() == 1) return;
        operate(
            equation,
            12, 12,
            (curr, op, seco) -> seco instanceof TypeEP type
                ? new NumberEP(type.instanceOf(curr) ? 1 : 0) : null
        );

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
            (num1, op, num2) -> isInteger(num1) && isInteger(num2)
                ? operator.apply(num1.longValue(), op, num2.longValue()) : null
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

    
    private static boolean isInteger(double num) {
        return Math.abs(num - Math.floor(num)) < 0.0001;
    }
}
