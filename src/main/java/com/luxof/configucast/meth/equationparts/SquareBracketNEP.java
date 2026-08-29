package com.luxof.configucast.meth.equationparts;

import java.util.List;

public final class SquareBracketNEP extends NestedEP {
    public SquareBracketNEP(List<EquationPart> nested) { super(nested); }
    @Override public String strRepr() { return "SQB"; }
}
