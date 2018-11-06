package com.github.the20login.trivial.bankng.util;

import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Result<R, E> {
    private Result(){}

    public abstract R getValue();
    public abstract E getError();

    public abstract boolean isError();
    public abstract R orElse(R defaultValue);
    public abstract R orElseGet(Supplier<R> supplier);
    public abstract R orElseCompute(Function<E, R> handler);


    public static <R, E> Result<R, E> value(R value) {
        return new Value<>(value);
    }

    public static <R, E> Result<R, E> error(E error) {
        return new Error<>(error);
    }

    private static class Value<R, E> extends Result<R, E> {
        private final R value;

        private Value(R value) {
            this.value = value;
        }

        @Override
        public R getValue() {
            return value;
        }

        @Override
        public E getError() {
            return null;
        }

        @Override
        public boolean isError() {
            return false;
        }

        @Override
        public R orElse(R defaultValue) {
            return value;
        }

        @Override
        public R orElseGet(Supplier<R> supplier) {
            return value;
        }

        @Override
        public R orElseCompute(Function<E, R> handler) {
            return value;
        }
    }

    private static class Error<R, E> extends Result<R, E> {
        private final E error;

        private Error(E error) {
            this.error = error;
        }

        @Override
        public R getValue() {
            return null;
        }

        @Override
        public E getError() {
            return error;
        }

        @Override
        public boolean isError() {
            return true;
        }

        @Override
        public R orElse(R defaultValue) {
            return defaultValue;
        }

        @Override
        public R orElseGet(Supplier<R> supplier) {
            return supplier.get();
        }

        @Override
        public R orElseCompute(Function<E, R> handler) {
            return handler.apply(error);
        }
    }
}
