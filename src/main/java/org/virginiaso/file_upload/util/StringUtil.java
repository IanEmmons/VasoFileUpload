package org.virginiaso.file_upload.util;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class StringUtil {
	private static final List<String> FALSE_STRINGS = List.of(
		"false", "f", "no", "n", "0");

	private StringUtil() {}	// prevent instantiation

	public static <E extends Enum<E>> E convertEnumerator(Class<E> enumClass, String str) {
		Objects.requireNonNull(enumClass, "enumClass");
		try {
			return Enum.valueOf(enumClass, safeTrim(str));
		} catch (NullPointerException | IllegalArgumentException ex) {
			try {
				var valuesMethod = enumClass.getMethod("values");
				var enumValues = (Object[]) valuesMethod.invoke(null);
				var enumerators = Stream.of(enumValues)
					.map(Object::toString)
					.collect(Collectors.joining(", "));
				throw new ValidationException(
					"Unrecognized %1$s value '%2$s' (should be one of %3$s)",
					enumClass.getSimpleName(), str, enumerators);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException nestedEx) {
				throw new ValidationException(nestedEx, "Unrecognized %1$s value '%2$s'",
					enumClass.getSimpleName(), str);
			}
		}
	}

	public static int convertInteger(String str) {
		try {
			return Integer.parseUnsignedInt(safeTrim(str));
		} catch (NullPointerException | NumberFormatException ex) {
			throw new ValidationException("Ill-formed integer: '%1$s'", str);
		}
	}

	public static BigDecimal convertDecimal(String str) {
		try {
			return new BigDecimal(safeTrim(str), MathContext.UNLIMITED);
		} catch (NullPointerException | NumberFormatException ex) {
			throw new ValidationException("Ill-formed decimal number: '%1$s'", str);
		}
	}

	public static String safeTrim(String str) {
		return isBlank(str) ? null : str.trim();
	}

	public static boolean isBlank(String str) {
		return str == null || str.isBlank();
	}

	public static boolean interpretOptReqParam(Optional<String> paramValue) {
		if (paramValue.isEmpty()) {
			// Parameter is not present in the query string:
			return false;
		} else if (paramValue.get().isBlank()) {
			// Parameter is present but blank, i.e., no value specified:
			return true;
		} else {
			// Parameter is present with a specified value:
			var value = paramValue.get().trim();
			return !FALSE_STRINGS.stream().anyMatch(
				falseStr -> value.equalsIgnoreCase(falseStr));
		}
	}
}
