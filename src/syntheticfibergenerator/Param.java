package syntheticfibergenerator; // TODO: Cleaned up

import java.text.ParseException;


class Param<Type extends Comparable<Type>> { // TODO: Change serialization/deserialization to flatten value field

    interface Parser<Type> {
        Type parse(String s) throws ParseException;
    }


    private Type value;
    private transient String name;


    Type getValue() {
        return value;
    }

    String getString() {
        return value == null ? "" : value.toString();
    }

    void setName(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    @SuppressWarnings("unused")
    void verifyLess(Type max) {
        if (getValue().compareTo(max) >= 0) {
            throw new IllegalArgumentException("Error: " + name + " must be less than " + max);
        }
    }

    void verifyGreater(Type min) {
        if (getValue().compareTo(min) <= 0) {
            throw new IllegalArgumentException("Error: " + name + " must be greater than than " + min);
        }
    }

    void verifyLessEq(Type max) {
        if (getValue().compareTo(max) > 0) {
            throw new IllegalArgumentException("Error: " + name + " must be less than or equal to " + max);
        }
    }

    void verifyGreaterEq(@SuppressWarnings("SameParameterValue") Type min) {
        if (getValue().compareTo(min) < 0) {
            throw new IllegalArgumentException("Error: " + name + " must be greater than or equal to " + min);
        }
    }

    void parse(String s, Parser<Type> p) throws ParseException {
        if (s.replaceAll("\\s+","").isEmpty()) {
            throw new ParseException("Value of \"" + name + "\" must be non-empty", 0);
        }
        try {
            value = p.parse(s);
        } catch (ParseException e) {
            throw new ParseException("Unable to parse value \"" + s + "\" for parameter \"" + name + '\"',
                    e.getErrorOffset());
        }
    }
}


class Optional<Type extends Comparable<Type>> extends Param<Type> {

    boolean use;


    void parse(boolean use, String s, Parser<Type> p) throws ParseException {
        this.use = use;
        if (use) {
            super.parse(s, p);
        }
    }
}