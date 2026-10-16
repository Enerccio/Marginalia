package com.github.enerccio.marginalia.domain.service.impl;

/**
 * Conversion of the arguments of a command to the types of its parameters: the types they already have, numbers to
 * other number types when nothing is lost, and text (what the user typed) to numbers, booleans, characters and enums.
 */
final class CommandValues {

    /** The converted value and how far it is from the argument: 0 it already had the type, 1 "any", 2 converted. */
    record Converted(Object value, int cost) {

    }

    private CommandValues() {
    }

    /**
     * @return the converted argument, null when it can not be converted to the type (a null argument converts to
     * {@code Converted(null)} to any type that is not a primitive)
     */
    static Converted convert(Object argument, Class<?> type) {
        if (type == Object.class) {
            return new Converted(argument, 1);
        }
        Class<?> boxed = box(type);
        if (argument == null) {
            return type.isPrimitive() ? null : new Converted(null, 1);
        }
        if (boxed.isInstance(argument)) {
            return new Converted(argument, 0);
        }
        if (argument instanceof Number number) {
            Object converted = convertNumber(number, boxed);
            return converted == null ? null : new Converted(converted, 2);
        }
        if (argument instanceof String text) {
            Object converted = parse(text, boxed);
            return converted == null ? null : new Converted(converted, 2);
        }
        return null;
    }

    static Class<?> box(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        return switch (type.getName()) {
            case "int" -> Integer.class;
            case "long" -> Long.class;
            case "double" -> Double.class;
            case "float" -> Float.class;
            case "short" -> Short.class;
            case "byte" -> Byte.class;
            case "boolean" -> Boolean.class;
            case "char" -> Character.class;
            default -> type;
        };
    }

    /** The number as the wanted number type when it fits without loss, null otherwise. */
    static Object convertNumber(Number number, Class<?> boxed) {
        double d = number.doubleValue();
        if (boxed == Double.class) {
            return d;
        }
        if (boxed == Float.class) {
            return number.floatValue();
        }
        if (d != Math.rint(d) || Double.isInfinite(d)) {
            return null;
        }
        long l = number.longValue();
        if (boxed == Long.class) {
            return l;
        } else if (boxed == Integer.class) {
            return l == (int) l ? (Object) (int) l : null;
        } else if (boxed == Short.class) {
            return l == (short) l ? (Object) (short) l : null;
        } else if (boxed == Byte.class) {
            return l == (byte) l ? (Object) (byte) l : null;
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object parse(String text, Class<?> boxed) {
        try {
            if (boxed == Integer.class) {
                return Integer.valueOf(text.trim());
            } else if (boxed == Long.class) {
                return Long.valueOf(text.trim());
            } else if (boxed == Double.class) {
                return Double.valueOf(text.trim());
            } else if (boxed == Float.class) {
                return Float.valueOf(text.trim());
            } else if (boxed == Short.class) {
                return Short.valueOf(text.trim());
            } else if (boxed == Byte.class) {
                return Byte.valueOf(text.trim());
            } else if (boxed == Boolean.class) {
                if (text.equalsIgnoreCase("true")) {
                    return Boolean.TRUE;
                }
                return text.equalsIgnoreCase("false") ? Boolean.FALSE : null;
            } else if (boxed == Character.class) {
                return text.length() == 1 ? (Object) text.charAt(0) : null;
            } else if (boxed.isEnum()) {
                for (Object constant : boxed.getEnumConstants()) {
                    if (((Enum) constant).name().equalsIgnoreCase(text)) {
                        return constant;
                    }
                }
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

}
