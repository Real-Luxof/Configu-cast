package com.luxof.configucast.meth.equationparts;

public enum TypeEP implements EquationPart {
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
