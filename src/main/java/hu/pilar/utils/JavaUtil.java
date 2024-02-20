/*
 * Copyright 2009-2024 Pilar Internet Consulting Kft.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package hu.pilar.utils;

import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import hu.pilar.utils.exceptions.UncheckedException;
import hu.pilar.utils.exceptions.ValidationException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.json.JSONFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaUtil {

  private JavaUtil() {}

  private static final Logger LOGGER = LoggerFactory.getLogger(JavaUtil.class);

  public static <T> Optional<T> some(T object) {
    return Optional.of(object);
  }

  public static void unchecked(CheckedRunnable<?> runnable) {
    try {
      runnable.runThrows();
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new UncheckedException(ex);
    }
  }

  public static <T> T unchecked(Callable<T> supplier) {
    try {
      return supplier.call();
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new UncheckedException(ex);
    }
  }

  public static <T> T unchecked(Callable<T> supplier, String message) {
    try {
      return supplier.call();
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new UncheckedException(message, ex);
    }
  }

  public static <T> T uncheckedTrace(Callable<T> supplier, Supplier<T> defaultValue) {
    try {
      return supplier.call();
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ex) {
      LOGGER.trace("Unchecked logged exception", ex);
      return defaultValue == null ? null : defaultValue.get();
    }
  }

  public static <T> T uncheckedLog(Callable<T> supplier, Supplier<T> defaultValue) {
    try {
      return supplier.call();
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ex) {
      LOGGER.debug("Unchecked logged exception", ex);
      return defaultValue == null ? null : defaultValue.get();
    }
  }

  public static void uncheckedFeedback(CheckedRunnable<?> runnable, Component component) {
    try {
      runnable.run();
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ex) {
      LOGGER.debug("Unchecked logged exception", ex);
      component.error(ex.getMessage());
    }
  }

  public static void uncheckedLog(CheckedRunnable<?> runnable) {
    try {
      runnable.run();
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ex) {
      LOGGER.debug("Unchecked logged exception", ex);
    }
  }

  public static <T> T unchecked(Callable<T> supplier, Supplier<T> defaultValue) {
    try {
      return supplier.call();
    } catch (Exception ex) {
      LOGGER.error("Error occured, returning default value", ex);
      return defaultValue.get();
    }
  }

  public static <T> Optional<T> tryLog(Callable<T> supplier) {
    try {
      return ofNullable(supplier.call());
    } catch (Exception ex) {
      LOGGER.error("", ex);
      return empty();
    }
  }

  public static <T> Optional<T> tryLog(Callable<T> supplier, String message) {
    try {
      return ofNullable(supplier.call());
    } catch (Exception ex) {
      LOGGER.error(message, ex);
      return empty();
    }
  }

  public static void tryLog(CheckedRunnable runnable) {
    try {
      runnable.run();
    } catch (Exception ex) {
      LOGGER.error("", ex);
    }
  }

  public static <T> Optional<T> tryDebugLog(Callable<T> supplier) {
    try {
      return ofNullable(supplier.call());
    } catch (Exception ex) {
      LOGGER.debug("", ex);
      return empty();
    }
  }

  public static <T> T uncheckedNoLog(Callable<T> supplier, T defaultValue) {
    try {
      return supplier.call();
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ex) {
      return defaultValue;
    }
  }

  public static <T> T checkThat(Predicate<T> check, T object, String validationMessage) {
    if (!check.test(object)) {
      throw new ValidationException(validationMessage);
    }
    return object;
  }

  public static <T> T checkThat(Predicate<T> check, T object, Supplier<RuntimeException> ex) {
    if (!check.test(object)) {
      throw ex.get();
    }
    return object;
  }

  public static <T> T checkNotNull(T object, String validationMessage) {
    if (object == null) {
      throw new ValidationException(validationMessage);
    }
    return object;
  }

  public static <S, T> Map<S, T> orderPreservedMap(final Map.Entry<S, T>... entries) {
    final var map = new LinkedHashMap<S, T>();
    stream(entries).forEach(e -> map.put(e.getKey(), e.getValue()));
    return unmodifiableMap(map);
  }

  public static JSONFunction jsonFunction(final String s) {
    return new JSONFunction(s);
  }
}
