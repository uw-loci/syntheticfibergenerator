package syntheticfibergenerator;

// TODO: Change serialization/deserialization to flatten value field
class Param<Type extends Comparable<Type>> {

    private transient String name;
    private Type value;

    Type getValue() {
        return value;
    }

    String getString() {
        return value == null ? "" : value.toString();
    }

    void setName(String name) {
        this.name = name;
    }

    void setValue(Type value) {
        this.value = value;
    }

    String getName() {
        return name;
    }

    void verifyGreater(Type min) {
        if (getValue().compareTo(min) <= 0) {
            throw new IllegalArgumentException("Error: " + name + " must be greater than than " + min); // TODO: Use better exception
        }
    }

    void verifyLess(Type max) {
        if (getValue().compareTo(max) >= 0) {
            throw new IllegalArgumentException("Error: " + name + " must be less than " + max);
        }
    }

    void verifyGreaterEq(Type min) {
        if (getValue().compareTo(min) < 0) {
            throw new IllegalArgumentException("Error: " + name + " must be greater than or equal to " + min);
        }
    }

    void verifyLessEq(Type max) {
        if (getValue().compareTo(max) > 0) {
            throw new IllegalArgumentException("Error: " + name + " must be less than or equal to " + max);
        }
    }

    void parse(String s, Parser<Type> p) {
        if (s.replaceAll("\\s+","").isEmpty()) {
            throw new IllegalArgumentException("Value of \"" + name + "\" must be non-empty");
        }
        try {
            value = p.parse(s);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse value \"" + s + "\" for parameter \"" + name + '\"'); // TODO: Use better exception
        }
    }
}

interface Parser<Type> {
    Type parse(String s);
}

class Optional<Type extends Comparable<Type>> extends Param<Type> {

    boolean use;

    void parse(boolean use, String s, Parser<Type> p) {
        this.use = use;
        if (use) {
            super.parse(s, p);
        }
    }
}