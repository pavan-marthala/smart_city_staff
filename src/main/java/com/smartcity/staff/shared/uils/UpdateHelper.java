package com.smartcity.staff.shared.uils;

import java.util.Optional;
import java.util.function.Consumer;

public class UpdateHelper {
    public static <T> void updateIfNotNull(Consumer<T> setter, T value) {
        Optional.ofNullable(value).ifPresent(setter);
    }
}
