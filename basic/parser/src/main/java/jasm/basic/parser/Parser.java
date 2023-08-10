package jasm.basic.parser;

import static jasm.basic.parser.Result.error;
import static jasm.basic.parser.Result.ok;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

@FunctionalInterface
public interface Parser<T> {
    Result<T> run(Input input);

    default <T1> Parser<T1> map(Function<T, T1> mapper) {
        return input -> run(input).map((input1, value) -> ok(input1, mapper.apply(value)));
    }

    default <T1> Parser<T1> bind(Function<T, Parser<T1>> mapper) {
        return input -> run(input).map((input1, value) -> mapper.apply(value).run(input1));
    }

    default <R> Parser<R> foldRight(Parser<R> parser) {
        return input -> run(input).map((input1, value) -> parser.run(input1));
    }

    default Parser<T> foldLeft(Parser<?> parser) {
        return input -> run(input).map((input1, value) -> parser.run(input1).map(
                (input2, ignored) -> ok(input2, value),
                (input2, err) -> error(input2, err)));
    }

    default <R> Parser<Tuple<T, R>> join(Parser<R> parser) {
        return input -> run(input).map(
                (input1, left) -> parser.run(input1).map(
                        (input2, right) -> Result.ok(input2, Tuple.of(left, right)),
                        (input2, err) -> Result.error(input2, err)),
                (input1, err) -> Result.error(input1, err));
    }

    default Parser<T> or(Parser<T> parser) {
        return input -> run(input).map(Result::ok, (input1, err) -> parser.run(input));
    }

    default Parser<Optional<T>> optional() {
        return input -> run(input).map(
                (input1, value) -> ok(input1, Optional.of(value)),
                (input1, err) -> ok(input1, Optional.empty()));
    }

    default Parser<List<T>> many() {
        return input -> {
            final var list = new ArrayList<T>();

            var res = run(input);

            while (res instanceof Result.Ok<T> ok) {
                list.add(ok.value());
                res = run(ok.input());
            }

            return ok(res.input(), list);
        };
    }

    default Parser<T> check(Predicate<T> predicate, Function<T, String> error) {
        return input -> run(input).map((input1, value) -> predicate.test(value)
                ? ok(input1, value)
                : error(input, error.apply(value)));
    }
}
