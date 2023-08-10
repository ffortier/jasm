package jasm.basic.parser;

import java.util.Objects;

public sealed interface Result<T> permits Result.Ok, Result.Error {

    record Ok<T>(Input input, T value) implements Result<T> {

        public Ok {
            Objects.requireNonNull(input, "input");
        }

        @Override
        public <T1> Result<T1> map(Mapper<T, T1> success, Mapper<String, T1> error) {
            return map(success);
        }

        @Override
        public <T1> Result<T1> map(Mapper<T, T1> success) {
            return success.apply(input, value);
        }

        @Override
        public T unwrap() {
            return value;
        }
    }

    record Error<T>(Input input, String message) implements Result<T> {

        public Error {
            Objects.requireNonNull(input, "input");
        }

        @Override
        public <T1> Result<T1> map(Mapper<T, T1> success, Mapper<String, T1> error) {
            return error.apply(input, message);
        }

        @Override
        public <T1> Result<T1> map(Mapper<T, T1> success) {
            return new Error<>(input, message);
        }

        @Override
        public T unwrap() {
            throw new ParserException(input, message);
        }

    }

    <T1> Result<T1> map(Mapper<T, T1> success, Mapper<String, T1> error);

    <T1> Result<T1> map(Mapper<T, T1> success);

    T unwrap();

    Input input();

    static <T> Ok<T> ok(Input input, T value) {
        return new Ok<>(input, value);
    }

    static <T> Error<T> error(Input input, String message) {
        return new Error<>(input, message);
    }

    public interface Mapper<T, T1> {
        Result<T1> apply(Input input, T value);
    }
}
