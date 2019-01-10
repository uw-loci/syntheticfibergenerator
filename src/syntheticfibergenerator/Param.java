package syntheticfibergenerator;

import java.text.ParseException;


/**
 * TODO: Change serialization/deserialization to flatten value field
 */
class Param<T extends Comparable<T>> {

    interface Parser<U> {
        U parse(String s) throws ParseException;
    }

    interface Verifier<U extends Comparable<U>> {
        void verify(U value, U bound) throws IllegalArgumentException;
    }


    private T value;
    private transient String name;
    private transient String hint;


    T value() {
        return value;
    }

    String string() {
        return value == null ? "" : value.toString();
    }

    void setName(String name) {
        this.name = name;
    }

    String name() {
        return name == null ? "" : name;
    }

    void setHint(String hint) {
        this.hint = hint;
    }

    String hint() {
        return hint;
    }

    void parse(String s, Parser<T> p) throws IllegalArgumentException {
        if (s.replaceAll("\\s+", "").isEmpty()) {
            throw new IllegalArgumentException("Value of \"" + name() + "\" must be non-empty");
        }
        try {
            value = p.parse(s);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse value \"" + s + "\" for parameter \"" + name() + '\"');
        }
    }

    void verify(T bound, Verifier<T> verifier) throws IllegalArgumentException {
        try {
            verifier.verify(value, bound);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Value of " + name() + " " + e.getMessage() + " " + bound);
        }
    }

    static <U extends Comparable<U>> void less(U value, U max) {
        if (value.compareTo(max) >= 0) {
            throw new IllegalArgumentException("must be less than");
        }
    }

    static <U extends Comparable<U>> void greater(U value, U min) {
        if (value.compareTo(min) <= 0) {
            throw new IllegalArgumentException("must be greater than");
        }
    }

    static <U extends Comparable<U>> void lessEq(U value, U max) {
        if (value.compareTo(max) > 0) {
            throw new IllegalArgumentException("must be less than or equal to");
        }
    }

    static <U extends Comparable<U>> void greaterEq(U value, U min) {
        if (value.compareTo(min) < 0) {
            throw new IllegalArgumentException("must be greater than or equal to");
        }
    }
}


class Optional<T extends Comparable<T>> extends Param<T> {

    boolean use;


    @Override
    void parse(String s, Parser<T> p) {
        if (use) {
            super.parse(s, p);
        }
    }

    void parse(boolean use, String s, Parser<T> p) throws IllegalArgumentException {
        this.use = use;
        parse(s, p);
    }

    @Override
    void verify(T bound, Verifier<T> verifier) throws IllegalArgumentException {
        if (use) {
            super.verify(bound, verifier);
        }
    }
}