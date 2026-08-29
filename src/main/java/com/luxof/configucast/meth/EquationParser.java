package com.luxof.configucast.meth;

import com.google.gson.JsonObject;

import com.luxof.configucast.meth.equationparts.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import org.jetbrains.annotations.Nullable;

/**
 * <pre>"I fear not the programmer who has written 1000 lines 1 time, I fear the programmer who has written 1 line 1000 times :3"
 * -Jackie-chan
 */
public class EquationParser {
    public static final Map<Identifier, List<EquationPart>> costFormulae = new HashMap<>();

    public static Map<Identifier, List<EquationPart>> loadMathEquationsFromJson(
        JsonObject json
    ) throws Exception {
        Map<Identifier, List<EquationPart>> formulae = new HashMap<>();

        for (String key : json.keySet()) {
            Identifier id = new Identifier(key);

            formulae.put(id, loadMathEquationFromString(json.get(key).getAsString()));
        }

        return formulae;
    }

    public static @Nullable List<EquationPart> getMathFormulaFor(Identifier actionId) {
        return costFormulae.get(actionId);
    }

    public static final Pattern equationRegex = Pattern.compile("(?<number>((?<!\\d)-)?\\d+(\\.\\d+)?)|(?<operator>\\*\\*|[\\^*\\/%+\\-<>&|?:]|<<|>>|<=|>=|==|!=|&&|\\|\\|)|[()\\[\\],]|(?<function>(?:[A-Za-z]+)\\((?=[^\\)]*\\)))|(?<variable>\\$[^ +\\-*\\/^<>&|?,\\[\\]\\(\\)]+)|(?<string>\\\"[^\\\"]*\\\")|(?<type>\\{[^}]+})|\\s+");
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
}
