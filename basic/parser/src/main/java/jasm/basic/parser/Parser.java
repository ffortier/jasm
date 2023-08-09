package jasm.basic.parser;

import static jasm.basic.parser.Result.Error;
import static jasm.basic.parser.Result.Ok;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
public interface Parser<T> {
    Result<T> run(Input input);

    default <T1> Parser<T1> map(Function<T, T1> mapper) {
        return input -> run(input).map((input1, value) -> Ok(input1, mapper.apply(value)));
    }

    default <T1> Parser<T1> bind(Function<T, Parser<T1>> mapper) {
        return input -> run(input).map((input1, value) -> mapper.apply(value).run(input1));
    }

    default <R> Parser<R> foldRight(Parser<R> parser) {
        return input -> run(input).map((input1, value) -> parser.run(input1));
    }

    default Parser<T> foldLeft(Parser<?> parser) {
        return input -> run(input).map((input1, value) -> parser.run(input1).map(
                (input2, ignored) -> Ok(input2, value),
                (input2, err) -> Error(input2, err)));
    }

    default <R> Parser<Tuple<T, R>> join(Parser<R> parser) {
        return input -> run(input).map(
                (input1, left) -> parser.run(input1).map(
                        (input2, right) -> Result.Ok(input2, Tuple.of(left, right)),
                        (input2, err) -> Result.Error(input2, err)),
                (input1, err) -> Result.Error(input1, err));
    }

    default Parser<T> or(Parser<T> parser) {
        return input -> run(input).map(Result::Ok, (input1, err) -> parser.run(input));
    }

    default Parser<Optional<T>> optional() {
        return input -> run(input).map(
                (input1, value) -> Ok(input1, Optional.of(value)),
                (input1, err) -> Ok(input1, Optional.empty()));
    }

    default Parser<List<T>> many() {
        return input -> {
            final var list = new ArrayList<T>();

            var res = run(input);

            while (res.isOk()) {
                list.add(res.value());
                res = run(res.input());
            }

            return Ok(res.input(), list);
        };
    }

    default Parser<List<T>> manyExact(int count) {
        return input -> many().run(input).map((input1, value) -> {
            if (value.size() != count) {
                return Error(input, "expected exactly %d elements but found %d".formatted(count, value.size()));
            }

            return Ok(input1, value);
        }, (input1, err) -> Error(input1, "unreachable, many should never fail"));
    }
}
