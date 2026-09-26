package com.luxof.configucast.meth.equationparts;

public final class StringEP implements EquationPart {
    public String value;
    public StringEP(String value) { this.value = value; }
    @Override public Object getValue() { return value; }
    @Override public String strRepr() { return value; }
    @Override public boolean equals(Object o) {
        return o instanceof StringEP str ? str.getValue().equals(value) : super.equals(o);
    }
}
