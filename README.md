# Result Type for Error Handling

The `Result` type, which is returned instead of thrown exceptions, offers a structured way to handle potential errors in your methods. A `Result` can either be of the concrete type `Ok` (containing a successful return value) or `Err` (containing an error message if the method call has failed).


## Motivation

Using unchecked exceptions poses the risk of errors remaining unhandled and potentially causing a program crash, as `try-catch` clauses are optional and can easily be ignored or forgotten. The `Result` type enforces handling of potential errors, leading to more robust and maintainable code.


## Examples

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
    .onErr(e -> err1.set(true)) // Called due to division by zero
    .mapOr(
        v -> Err(new Error()),  // Not called
        e -> Ok(4))             // Called and returns Ok(4)
    .onErr(e -> err2.set(true)) // Not called
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

### Propagate an Err

Example of propagating an `Err` to the caller.

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

The `Result` type offers a range of methods to simplify the processing of return values and enhance control flow. These methods create a decoupled and logical chain of data processing, enabling a clearer structure of the code without relying on `try-catch` and `if-else` clauses.

For more details see the documentation: [Result](doc/result/Result.html).

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
- `on(okConsumer, errConsumer)`: Executes the appropriate action based on the result type.

### map-Methods
- `map(okMapper)`: Transforms the Ok value to a new Result.
- `mapOr(okMapper, errMapper)`: Transforms the result based on its type.


## Contribute
We welcome feedback and contributions from the community. If you have suggestions for improvement or discover issues, please check our contribution guidelines and consider making a contribution.
