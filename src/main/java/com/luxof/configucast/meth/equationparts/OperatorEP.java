package com.luxof.configucast.meth.equationparts;

import java.util.Map;

public enum OperatorEP implements EquationPart {
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

    public static EquationPart multiply(
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
    public static String multiplyString(String string, double times) {
        String newStr = "";
        for (int i = 0; i < Math.floor(times); i++) {
            newStr += string;
        }
        // TODO: DON'T FORGET TO DOCUMENT THIS WEIRD STRING PROCESSING AS WELL
        newStr = newStr + string.substring(0, (int)Math.floor(string.length() * times % 1));
        return newStr;
    }


    public static EquationPart divide(
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

    public static EquationPart modulo(
        EquationPart currEp,
        EquationPart secoEp
    ) {
        return currEp instanceof NumberEP num1 && secoEp instanceof NumberEP num2
            ? new NumberEP(num1.value % num2.value) : null;
    }

    public static EquationPart add(
        EquationPart currEp,
        EquationPart secoEp
    ) {
        return currEp instanceof NumberEP num1 && secoEp instanceof NumberEP num2 ? new NumberEP(num1.value + num2.value)
            : currEp instanceof StringEP str1 && secoEp instanceof StringEP str2 ? new StringEP(str1.value + str2.value)
            : null;
    }

    public static EquationPart sub(
        EquationPart currEp,
        EquationPart secoEp
    ) {
        return currEp instanceof NumberEP num1 && secoEp instanceof NumberEP num2
            ? new NumberEP(num1.value - num2.value) : null;
    }
}
