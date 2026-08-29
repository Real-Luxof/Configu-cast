package com.luxof.configucast.meth.equationparts;

/** the closing parenthesis on a function. */
public final class FunctionArgumentsEndEP implements EquationPart {
    @Override public Object getValue() { return this; }
    @Override public String strRepr() { return ")"; }
}
