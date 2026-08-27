package com.luxof.configucast;

public class MathException extends RuntimeException {
    public MathException(String msg, Object... args) {
        super(String.format(msg, args));
    }
}
