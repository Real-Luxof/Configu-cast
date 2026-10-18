package com.luxof.configucast.meth;

public class MathException extends RuntimeException {
    public MathException(String msg, Object... args) {
        super(String.format(msg, args));
    }
}
