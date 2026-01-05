package com.lemonclient.api.util.misc;

public class EnumUtils {
   public static <T extends Enum<T>> T next(T value) {
      T[] enumValues = (T[])value.getDeclaringClass().getEnumConstants();
      return enumValues[(value.ordinal() + 1) % enumValues.length];
   }
}
