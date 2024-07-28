package result;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.NoSuchElementException;

/**
 * A result type is a container object much like an {@link Optional}, but it 
 * can also contain an error. If a value is present, {@link #isOk()} returns
 * {@code true} and the result is an {@link Ok} containing the value. The
 * contained value can be accessed with {@link #get()}. If an error is
 * present, {@link #isOk()} returns {@code false} and the result is an
 * {@link Err} containing the error. The error message can be accessed with
 * {@link #error()}, {@link #getErrorMessage()} or {@link #printErrorMessage()}.
 * The contained value or error are guaranteed to be non-{@code null}.
 * 
 * <p>Additional methods that depend on the presence or absence of a contained
 * value are provided, such as {@link #getOr(Object)} or {@link #getOr(Function)}.
 * 
 * <p>{@code Result} is primarily intended for use as a method return type for
 * methods that can fail and would traditionally throw an error. This reduces 
 * the need for {@code try/catch} blocks which leads to cleaner, more readable 
 * and maintainable code with better control flow.
 * 
 * @param <T> the type of value
 * @since 1.2
 */
public sealed interface Result<T> {
    /**
     * Returns true if the result is {@link Ok}, otherwise false if it is an
     * {@link Err}.
     * 
     * @return true if the result is {@link Ok}, otherwise false.
     */
    boolean isOk();

    /**
     * Returns true if the result is {@link Err}, otherwise false if it is an
     * {@link Ok}.
     * 
     * @return true if the result is {@link Err}, otherwise false.
     */
    boolean isErr();

    /**
     * Returns the contained {@link Ok} value.
     * Throws if the value is an {@link Err}, with an error message provided by the
     * {@link Err}.
     * Only call this method if {@link #isOk()} returns true.
     * 
     * @return the contained {@link Ok} value
     * @throws NoSuchElementException if the result is an {@link Err}
     */
    T get();
    
    /**
     * If a the result is {@link Ok}, returns the value, otherwise returns
     * {@code other}.
     * 
     * @param other the value to be returned, if the result is {@link Err}.
     * @return the value if the result is {@link Ok}, otherwise {@code other}
     * @throws NullPointerException if {@code other} is {@code null}
     */
    T getOr(T other);

    /**
     * If the result is an {@link Ok}, returns the value, otherwise returns the result
     * produced by the supplying function.
     *
     * @param function the supplying function that produces a value to be returned
     * @return the value, if present, otherwise the result produced by the
     *         supplying function
     * @throws NullPointerException if the supplying function or the result produced 
     *         by the supplying function is {@code null}
     */
    T getOr(Function<Throwable, T> function);

    /**
     * Returns the error message if the result is an {@link Err}.
     * 
     * @return the error message if the result is an {@link Err}
     * @throws NoSuchElementException if the result is an {@link Ok}
     */
    Throwable error();

    /**
     * Returns the error message if the result is an {@link Err}, otherwise throws
     * an exception.
     * 
     * @return the error message if the result is an {@link Err}
     * @throws NoSuchElementException if the result is an {@link Ok}
     */
    String getErrorMessage();

    /**
     * Prints the error message if the result is an {@link Err}.
     * 
     * @throws NoSuchElementException if the result is an {@link Ok}
     */
    void printErrorMessage();

    /**
     * If the result is an {@link Ok}, performs the given action with the value,
     * otherwise performs the given action with the error.
     * 
     * @param okConsumer the action to be performed, if the result is an {@link Ok}
     * @param errConsumer the action to be performed, if the result is an {@link Err}
     * @return the result itself
     */
    Result<T> on(Consumer<T> okConsumer, Consumer<Throwable> errConsumer);
    
    /**
     * If the result is an {@link Ok}, performs the given action with the value,
     * otherwise does nothing.
     * 
     * @param okConsumer the action to be performed, if the result is an {@link Ok}
     * @return the result itself
     */
    Result<T> onOk(Consumer<T> okConsumer);

    /**
     * If the result is an {@link Err}, performs the given action with the error,
     * otherwise does nothing.
     * 
     * @param errConsumer the action to be performed, if the result is an {@link Err}
     * @return the result itself
     */
    Result<T> onErr(Consumer<Throwable> errConsumer);

    /**
     * If the result is an {@link Ok}, performs the given action with the value,
     * returning a new Result, otherwise does nothing.
     * 
     * @param <R> the type of the value of the new {@link Result}
     * @param okMapper the action to be performed, if the result is an {@link Ok}
     * @return the mapped result
     */
    <R> Result<R> map(Function<T, Result<R>> okMapper);

    /**
     * If the result is an {@link Ok}, performs the given action with the value,
     * otherwise performs the given action with the error.
     * 
     * @param <R> the type of the value of the new {@link Result}
     * @param okMapper the action to be performed, if the result is an {@link Ok}
     * @param errMapper the action to be performed, if the result is an {@link Err}
     * @return the mapped result
     */
    <R> R mapOr(Function<T, R> okMapper, Function<Throwable, R> errMapper);

    /**
     * Returns an {@link Ok} with the specified value.
     * 
     * @param <T> the type of the value
     * @param value the value to be present in the {@link Ok}
     * @return an {@link Ok} with the specified value, {@link Err} if the value is null
     */
    public static <T> Result<T> Ok(T value) {
        return new Ok<>(value);
    }

    /**
     * Returns an {@link Ok} with a value of {@code true}.
     * 
     * @return an {@link Ok} with a value of {@code true}
     */
    public static Result<Void> Ok() {
        return new Ok<>(new Void());
    }

    /**
     * Returns an {@link Err} with the specified error.
     * 
     * @param <T> the type of the value
     * @param error the error to be present in the {@link Err}
     * @return an {@link Err} with the specified error or {@link Err} holding a NullPointerException if the error is null
     */
    public static <T> Result<T> Err(Throwable error) {
        return new Err<>(error);
    }

    /**
     * Represents an empty result, i.e. a result that contains no value.
     */
    public static record Void() {}

    /**
     * A Result type that holds an error message.
     * 
     * @param <T> the type of the value
     * @param error the error to be present in the {@link Err}
     */
    public record Err<T> (Throwable error) implements Result<T> {
        /**
         * Constructs an {@link Err} with the specified error.
         * 
         * @param error the error to be present in the {@link Err}
         */
        public Err {
            Objects.requireNonNull(error);
        }

        @Override
        public boolean isOk() {
            return false;
        }

        @Override
        public boolean isErr() {
            return true;
        }

        @Override
        public T get() {
            throw new NoSuchElementException("No value present: " + error.getMessage());
        }

        @Override
        public T getOr(T other) {
            return Objects.requireNonNull(other);
        }

        @Override
        public T getOr(Function<Throwable, T> errMapper) {
            return Objects.requireNonNull(errMapper.apply(error));
        }

        @Override
        public Throwable error() {
            return error;
        }

        @Override
        public String getErrorMessage() {
            return error.getMessage();
        }

        @Override
        public void printErrorMessage() {
            System.out.println(error.getMessage());
        }

        @Override
        public Result<T> onOk(Consumer<T> okConsumer) {
            return this;
        }

        @Override
        public Result<T> on(Consumer<T> okConsumer, Consumer<Throwable> errConsumer) {
            errConsumer.accept(error);
            return this;
        }

        @Override
        public Result<T> onErr(Consumer<Throwable> errConsumer) {
            errConsumer.accept(error);
            return this;
        }

        @Override
        public <R> Result<R> map(Function<T, Result<R>> okMapper) {
            return new Err<R>(error);
        }

        @Override
        public <R> R mapOr(Function<T, R> okMapper, Function<Throwable, R> errMapper) {
            return Objects.requireNonNull(errMapper.apply(error));
        }
        
        /**
         * Indicates whether some other object is "equal to" this {@code Err}.
         * The other object is considered equal if:
         * <ul>
         * <li>it is also an {@code Err} and;
         * <li>both instances have no error present or;
         * <li>the present errors are "equal to" each other via {@code equals()}.
         * </ul>
         *
         * @param obj an object to be tested for equality
         * @return {@code true} if the other object is "equal to" this object
         *         otherwise {@code false}
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            return obj instanceof Err<?> other && Objects.equals(error, other.error);
        }

        /**
         * Returns the hash code of the error, if present, otherwise {@code 0}
         * (zero) if no error is present.
         *
         * @return hash code value of the present error or {@code 0} if no error is
         *         present
         */
        @Override
        public int hashCode() {
            return Objects.hashCode(error);
        }

        /**
         * Returns a string representation of this {@code Err}
         * suitable for debugging.
         *
         * @return the string representation of this instance
         */
        @Override
        public String toString() {
            return "Err[" + error + "]";
        }
    }

    /**
     * A Result type that holds a value of type T.
     * 
     * @param <T> the type of the value
     * @param value the value to be present in the {@link Ok}
     */
    public record Ok<T>(T value) implements Result<T> {
        /**
         * Constructs an {@link Ok} with the specified value.
         * 
         * @param value the value to be present in the {@link Ok}
         */
        public Ok {
            Objects.requireNonNull(value);
        }

        @Override
        public boolean isOk() {
            return true;
        }

        @Override
        public boolean isErr() {
            return false;
        }

        @Override
        public T get() {
            return value;
        }
        
        @Override
        public T getOr(T other) {
            return value;
        }

        @Override
        public T getOr(Function<Throwable, T> function) {
            return value;
        }

        @Override
        public Throwable error() {
            throw new NoSuchElementException("No error present");
        }

        @Override
        public String getErrorMessage() {
            throw new NoSuchElementException("No error present");
        }

        @Override
        public void printErrorMessage() {
            throw new NoSuchElementException("No error present");
        }

        @Override
        public Result<T> onOk(Consumer<T> okConsumer) {
            okConsumer.accept(value);
            return this;
        }

        @Override
        public Result<T> on(Consumer<T> okConsumer, Consumer<Throwable> errConsumer) {
            okConsumer.accept(value);
            return this;
        }
        
        @Override
        public Result<T> onErr(Consumer<Throwable> errConsumer) {
            return this;
        }

        @Override
        public <R> Result<R> map(Function<T, Result<R>> okMapper) {
            return Objects.requireNonNull(okMapper.apply(value));
        }

        @Override
        public <R> R mapOr(Function<T, R> okMapper, Function<Throwable, R> errMapper) {
            return Objects.requireNonNull(okMapper.apply(value));
        }

        /**
         * Indicates whether some other object is "equal to" this {@code Ok}.
         * The other object is considered equal if:
         * <ul>
         * <li>it is also an {@code Ok} and;
         * <li>both instances have no value present or;
         * <li>the present values are "equal to" each other via {@code equals()}.
         * </ul>
         *
         * @param obj an object to be tested for equality
         * @return {@code true} if the other object is "equal to" this object
         *         otherwise {@code false}
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            return obj instanceof Ok<?> other && Objects.equals(value, other.value);
        }

        /**
         * Returns the hash code of the value, if present, otherwise {@code 0}
         * (zero) if no value is present.
         *
         * @return hash code value of the present value or {@code 0} if no value is
         *         present
         */
        @Override
        public int hashCode() {
            return Objects.hashCode(value);
        }

        /**
         * Returns a string representation of this {@code Ok}
         * suitable for debugging.
         *
         * @return the string representation of this instance
         */
        @Override
        public String toString() {
            return "Ok[" + value + "]";
        }
    }
}
