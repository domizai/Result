package result;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

import result.Result.Err;
import result.Result.Ok;
import result.Result.Void;
import static result.Result.Ok;
import static result.Result.Err;

class ResultTest {
    /* 
     * A simple example
     */
    Result<Double> divide(double a, double b) {
        if (b == 0) {
            return Err(new IllegalArgumentException("Cannot divide by zero"));
        }
        return Ok(a / b);
    }

    /*
     * Example of propagating an Err to the caller.
     *
     * I would like to write the following, but Java is not quite there yet.
     * (Return within switch expressions not permitted)
     *
     * Double result = switch (divide(10, 2)) {
     *     case Ok(Double v) -> v;
     *     case Err(Throwable e) -> { return Err(e); }
     * };
     */
    Result<Void> propagateErr() {
        Double result; switch (divide(10, 0)) {
            case Ok(Double v) -> result = v;
            case Err(Throwable e) -> { return Err(e); }
        }
        result *= 1; 
        return Ok();
    }

    @Test void testIsMethods() {
        assertTrue(Ok().isOk());
        assertFalse(Ok().isErr());
        assertTrue(Err(new IllegalArgumentException()).isErr());
        assertFalse(Err(new IllegalArgumentException()).isOk());
    }

    @Test void testOnMethods() {
        AtomicBoolean err = new AtomicBoolean(false);
        AtomicBoolean ok = new AtomicBoolean(false);
        divide(10, 0)
            .onOk(v -> ok.set(true))    // not called
            .onErr(e -> err.set(true)); // called
        assertFalse(ok.get());
        assertTrue(err.get());

        err.set(false);
        ok.set(false);
        divide(10, 2)
            .onOk(v -> ok.set(true))    // called
            .onErr(e -> err.set(true)); // not called
        assertTrue(ok.get());
        assertFalse(err.get());

        err.set(false);
        ok.set(false);
        divide(10, 0).on(
            v -> ok.set(true),   // not called
            e -> err.set(true)); // called
        assertFalse(ok.get());
        assertTrue(err.get());

        err.set(false);
        ok.set(false);
        divide(10, 2).on(
            v -> ok.set(true),   // called
            e -> err.set(true)); // not called
        assertTrue(ok.get());
        assertFalse(err.get());
    }

    @Test void testGetMethods() {
        var result = divide(10, 2);
        assertEquals(result.get(), 5d);
        assertEquals(result.getOr(0d), 5d);
        assertThrows(NoSuchElementException.class, () -> divide(10, 2).error());
        
        result = divide(10, 0);
        assertEquals(result.getOr(0d), 0d);
        assertEquals(result.getOr(e -> 1d), 1d);
        assertThrows(NoSuchElementException.class, () -> divide(10, 0).get());
    }

    @Test void testMapMethods() {
        var result = divide(10, 2);
        assertEquals(result.map(v -> Ok(v * 3d)), Ok(15d));

        AtomicBoolean err = new AtomicBoolean(false);
        AtomicBoolean ok = new AtomicBoolean(false);
        var mapped = result.<Result<Double>>mapOr(
            v -> { ok.set(true); return Ok(v * 3d); }, // called
            e -> { err.set(true); return Ok(4d); });   // not called
        assertTrue(ok.get());
        assertFalse(err.get());
        assertEquals(mapped, Ok(15d));

        result = divide(10, 0);
        assertTrue(result.map(v -> Ok(v)).isErr());

        err.set(false);
        ok.set(false);
        mapped = result.<Result<Double>>mapOr(
            v -> { ok.set(true); return Ok(v * 3d); }, // not called
            e -> { err.set(true); return Ok(4d); });   // called
        assertTrue(err.get());
        assertFalse(ok.get());
        assertEquals(mapped, Ok(4d));
    }

    @Test void testNull() {
        Class<NullPointerException> clazz = NullPointerException.class;
        assertThrows(clazz, () -> Ok(null));
        assertThrows(clazz, () -> Err(null));
        assertThrows(clazz, () -> divide(10, 2).map(null));
        assertThrows(clazz, () -> divide(10, 2).mapOr(null, null));
        assertThrows(clazz, () -> divide(10, 0).mapOr(null, null));
        assertThrows(clazz, () -> divide(10, 2).on(null, null));
        assertThrows(clazz, () -> divide(10, 0).on(null, null));
        assertThrows(clazz, () -> divide(10, 2).onOk(null));
        assertThrows(clazz, () -> divide(10, 0).onErr(null));
    }

    @Test void testPropagateErr() {
        Result<Void> result = propagateErr();
        assertTrue(result.isErr());
        assertEquals(IllegalArgumentException.class, result.error().getClass());
    }

    @Test void testChaining() {
        AtomicBoolean err1 = new AtomicBoolean(false);
        AtomicBoolean err2 = new AtomicBoolean(false);

        String result = divide(10, 0)
            .onErr(e -> err1.set(true)) // called
            .mapOr(
                v -> Err(new Error()),  // not called
                e -> Ok(4))            // called
            .onErr(e -> err2.set(true)) // not called
            .map(v -> Ok(v.toString()))
            .get();

        assertEquals(result, "4");
        assertTrue(err1.get());
        assertFalse(err2.get());
    }
}
