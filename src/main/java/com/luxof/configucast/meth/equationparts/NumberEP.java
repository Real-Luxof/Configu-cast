package com.luxof.configucast.meth.equationparts;

public sealed class NumberEP implements EquationPart permits DefaultEP {
    public double value;
    public NumberEP(double value) { this.value = value; }
    @Override public Object getValue() { return value; }
    @Override public String strRepr() { return String.valueOf(value); }
    public boolean truthy() { return Math.abs(value) > 0.0001; }
}
