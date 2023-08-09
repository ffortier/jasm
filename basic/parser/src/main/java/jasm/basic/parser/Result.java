package jasm.basic.parser;

import java.util.Objects;

public record Result<T>(Input input, T value, String error) {

    public Result {
        Objects.requireNonNull(input, "input");
    }

    public <T1> Result<T1> map(Mapper<T, T1> success, Mapper<String, T1> error) {
        if (isOk()) {
            return success.apply(input, value);
        }

        return error.apply(input, this.error);
    }

    public <T1> Result<T1> map(Mapper<T, T1> success) {
        if (isOk()) {
            return success.apply(input, value);
        }

        return Error(input, error);
    }

    public boolean isOk() {
        return error == null;
    }

    public boolean isError() {
        return error != null;
    }

    public static <T> Result<T> Ok(Input input, T value) {
        return new Result<>(input, value, null);
    }

    public static <T> Result<T> Error(Input input, String error) {
        return new Result<>(input, null, error);
    }

    public interface Mapper<T, T1> {
        Result<T1> apply(Input input, T value);
    }

    public T unwrap() {
        if (isError()) {
            throw new ParserException(error, input);
        }

        return value;
    }
}
