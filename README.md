# A Java Result Type for Error Handling

The `Result` monad offers a structured way to handle potential errors in your methods, replacing the need to throw exceptions. A `Result` can be either `Ok`, containing a successful return value, or ?`Err`, containing an error message if the method call fails.

## Motivation

Exceptions are generally considered expensive. In addition, `try-catch` clauses are combersome and unsuitable for flow control. The `Result` type avoids this overhead while enforcing error handling, resulting in more robust and maintainable code.

## Basic Example

```java
import static result.Result.Ok;
import static result.Result.Err;

Result<Double> divide(double a, double b) {
    if (b == 0) {
        return Err(new IllegalArgumentException("Cannot divide by zero"));
    }
    return Ok(a / b);
}

// Example usage
String str = divide(10, 0)
    .onErr(System.out::println) // Called due to division by zero
    .mapOr(
        v -> Err(new Error()),  // Not called
        e -> Ok(4))             // Called
    .onErr(System.out::println) // Not called
    .map(v -> Ok(v.toString()))
    .get();

assertEquals(str, "4");
```

### Result\<Void>

Sometimes, there is no value to return. In such cases, use `Result.Void`.

```java
import result.Result.Void;

Result<Void> func() {
    return Ok();
}
```

### Error Propagation

Example of propagating an `Err` to the caller using pattern matching.

```java
Result<Double> func() {
    Double result; 
    switch (divide(10, 0)) {
        case Ok(Double v) -> result = v;
        case Err(Throwable e) -> { return Err(e); }
    }
    // Additional processing
    return Ok(result);
}
```

## Methods

The `Result` type offers a range of methods that simplify further processing and enable the encapsulation of operations into a logical data flow. For more details, see the documentation: [Result](doc/result/Result.html).

### is-Methods
- `isOk()`: Checks if the result is Ok.
- `isErr()`: Checks if the result is Err.

### get-Methods
- `get()`: Returns the contained Ok value or throws if it is Err.
- `getOr(other)`: Returns the contained Ok value or a fallback value.
- `getOr(function)`: Returns the contained Ok value or applies the function to the error.

### on-Methods
- `onOk(okConsumer)`: Executes an action if the result is Ok.
- `onErr(errConsumer)`: Executes an action if the result is Err.
- `on(okConsumer, errConsumer)`: Executes the appropriate action based on the result type. Effectively the same as calling `.onOk(okConsumer).onErr(errConsumer)`.

### map-Methods
- `map(okMapper)`: Transforms the Ok value to a new Result.
- `mapOr(okMapper, errMapper)`: Transforms the result based on its type. `.mapOr(a -> b, c)` is effectively the same as `.map(a -> Ok(b)).getOr(c)`.

## Contribute
We welcome feedback and contributions from the community. If you have suggestions for improvement or discover issues, please check our contribution guidelines and consider making a contribution.
