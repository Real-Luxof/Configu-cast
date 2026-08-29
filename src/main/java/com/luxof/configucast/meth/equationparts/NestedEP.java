package com.luxof.configucast.meth.equationparts;

import java.util.List;

public abstract sealed class NestedEP implements EquationPart permits ParenNEP, SquareBracketNEP, CommaNEP {
    public List<EquationPart> nested;
    public NestedEP(List<EquationPart> nested) { this.nested = nested; }
    @Override public Object getValue() { return this; }
}
