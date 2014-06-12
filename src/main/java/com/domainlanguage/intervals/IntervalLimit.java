/**
 * Copyright (c) 2005 Domain Language, Inc. (http://domainlanguage.com) This
 * free software is distributed under the "MIT" licence. See file licence.txt.
 * For more information, see http://timeandmoney.sourceforge.net.
 */
package com.domainlanguage.intervals;

import java.io.Serializable;

class IntervalLimit<T extends Comparable<T>> implements Comparable<IntervalLimit<T>>, Serializable {

  private static final long serialVersionUID = 1L;
  private final boolean closed;
  private final boolean lower;
  private final T value;

  static <T extends Comparable<T>> IntervalLimit<T> upper(boolean closed, T value) {
    return new IntervalLimit<T>(closed, false, value);
  }

  static <T extends Comparable<T>> IntervalLimit<T> lower(boolean closed, T value) {
    return new IntervalLimit<T>(closed, true, value);
  }

  IntervalLimit(boolean closed, boolean lower, T value) {
    this.closed = closed;
    this.lower = lower;
    this.value = value;
  }

  boolean isLower() {
    return lower;
  }

  boolean isUpper() {
    return !lower;
  }

  boolean isClosed() {
    return closed;
  }

  boolean isOpen() {
    return !closed;
  }

  T getValue() {
    return value;
  }

  @Override
  public int compareTo(IntervalLimit<T> other) {
    T otherValue = other.value;
    if (otherValue == value) {
      return 0;
    }
    if (value == null) {
      return lower ? -1 : 1;
    }
    if (otherValue == null) {
      return other.lower ? 1 : -1;
    }
    return value.compareTo(otherValue);
  }

  @Override
  public String toString() {
    String prefix = lower ? "lower" : "upper";
    return "IntervalLimit[" + prefix + "," + value + "," + closed + "]";
  }

}
