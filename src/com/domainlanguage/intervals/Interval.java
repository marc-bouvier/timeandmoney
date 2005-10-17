/**
 * Copyright (c) 2004 Domain Language, Inc. (http://domainlanguage.com)
 * This free software is distributed under the "MIT" licence. See file licence.txt. 
 * For more information, see http://timeandmoney.sourceforge.net.
 */

package com.domainlanguage.intervals;

import java.io.*;
import java.util.*;

/**
 * The rules of this class are consistent with the common mathematical
 * definition of "interval". For a simple explanation, see
 * http://en.wikipedia.org/wiki/Interval_(mathematics)
 * 
 * Interval (and its "ConcreteInterval" subclass) can be used for any objects
 * that have a natural ordering reflected by implementing the Comparable
 * interface. For example, Integer implements Comparable, so if you want to
 * check if an Integer is within a range, make an Interval. Any class of yours
 * which implements Comparable can have intervals defined this way.
 */
public abstract class Interval implements Comparable, Serializable {

    public static Interval closed(Comparable lower, Comparable upper) {
        return new ConcreteInterval(lower, true, upper, true);
    }

    public static Interval open(Comparable lower, Comparable upper) {
        return new ConcreteInterval(lower, false, upper, false);
    }

    public static Interval over(Comparable lower, boolean lowerIncluded, Comparable upper, boolean upperIncluded) {
        return new ConcreteInterval(lower, lowerIncluded, upper, upperIncluded);
    }

    public abstract Comparable upperLimit();

    //Warning: This method should generally be used for display
    //purposes and interactions with closely coupled classes.
    //Look for (or add) other methods to do computations.

    public abstract Comparable lowerLimit();

    //Warning: This method should generally be used for display
    //purposes and interactions with closely coupled classes.
    //Look for (or add) other methods to do computations.

    public abstract boolean includesLowerLimit();

    //Warning: This method should generally be used for display
    //purposes and interactions with closely coupled classes.
    //Look for (or add) other methods to do computations.

    public abstract boolean includesUpperLimit();

    //Warning: This method should generally be used for display
    //purposes and interactions with closely coupled classes.
    //Look for (or add) other methods to do computations.

    public abstract Interval newOfSameType(Comparable lower, boolean isLowerClosed, Comparable upper, boolean isUpperClosed);

    public Interval emptyOfSameType() {
        return newOfSameType(lowerLimit(), false, lowerLimit(), false);
    }

    public boolean includes(Comparable value) {
        return !this.isBelow(value) && !this.isAbove(value);
    }

    public boolean covers(Interval other) {
        int lowerComparison = lowerLimit().compareTo(other.lowerLimit());
        boolean lowerPass = this.includes(other.lowerLimit()) || (lowerComparison == 0 && !other.includesLowerLimit());
        int upperComparison = upperLimit().compareTo(other.upperLimit());
        boolean upperPass = this.includes(other.upperLimit()) || (upperComparison == 0 && !other.includesUpperLimit());
        return lowerPass && upperPass;
    }

    public boolean isOpen() {
        return !includesLowerLimit() && !includesUpperLimit();
    }

    public boolean isClosed() {
        return includesLowerLimit() && includesUpperLimit();
    }

    public boolean isEmpty() {
        //TODO: Consider explicit empty interval
        //A 'degenerate' interval is an empty set, {}.
        return isOpen() && upperLimit().equals(lowerLimit());
    }

    public boolean isSingleElement() {
        //An interval containing a single element, {a}.
        return upperLimit().equals(lowerLimit()) && !isEmpty();
    }

    public boolean isBelow(Comparable value) {
        int comparison = upperLimit().compareTo(value);
        return comparison < 0 || (comparison == 0 && !includesUpperLimit());
    }

    public boolean isAbove(Comparable value) {
        int comparison = lowerLimit().compareTo(value);
        return comparison > 0 || (comparison == 0 && !includesLowerLimit());
    }

    public int compareTo(Object arg) {
        Interval other = (Interval) arg;
        if (!upperLimit().equals(other.upperLimit()))
            return upperLimit().compareTo(other.upperLimit());
        if (includesLowerLimit() && !other.includesLowerLimit())
            return -1;
        if (!includesLowerLimit() && other.includesLowerLimit())
            return 1;
        return lowerLimit().compareTo(other.lowerLimit());
    }

    public String toString() {
        if (isEmpty())
            return "{}";
        if (isSingleElement())
            return "{" + lowerLimit().toString() + "}";
        StringBuffer buffer = new StringBuffer();
        buffer.append(includesLowerLimit() ? "[" : "(");
        buffer.append(lowerLimit().toString());
        buffer.append(", ");
        buffer.append(upperLimit().toString());
        buffer.append(includesUpperLimit() ? "]" : ")");
        return buffer.toString();
    }

    private Comparable lesserOfLowerLimits(Interval other) {
        int lowerComparison = lowerLimit().compareTo(other.lowerLimit());
        if (lowerComparison <= 0)
            return this.lowerLimit();
        return other.lowerLimit();
    }

    Comparable greaterOfLowerLimits(Interval other) {
        int lowerComparison = lowerLimit().compareTo(other.lowerLimit());
        if (lowerComparison >= 0)
            return this.lowerLimit();
        return other.lowerLimit();
    }

    Comparable lesserOfUpperLimits(Interval other) {
        int upperComparison = upperLimit().compareTo(other.upperLimit());
        if (upperComparison <= 0)
            return this.upperLimit();
        return other.upperLimit();
    }

    private Comparable greaterOfUpperLimits(Interval other) {
        int upperComparison = upperLimit().compareTo(other.upperLimit());
        if (upperComparison >= 0)
            return this.upperLimit();
        return other.upperLimit();
    }

    private boolean greaterOfLowerIncludedInIntersection(Interval other) {
        Comparable limit = greaterOfLowerLimits(other);
        return this.includes(limit) && other.includes(limit);
    }

    private boolean lesserOfUpperIncludedInIntersection(Interval other) {
        Comparable limit = lesserOfUpperLimits(other);
        return this.includes(limit) && other.includes(limit);
    }

    private boolean greaterOfLowerIncludedInUnion(Interval other) {
        Comparable limit = greaterOfLowerLimits(other);
        return this.includes(limit) || other.includes(limit);
    }

    private boolean lesserOfUpperIncludedInUnion(Interval other) {
        Comparable limit = lesserOfUpperLimits(other);
        return this.includes(limit) || other.includes(limit);
    }

    public boolean equals(Object object) {
        //revisit: maybe use: Reflection.equalsOverClassAndNull(this, other)
        if (!(object instanceof Interval))
            return false;

        Interval other = ((Interval) object);
        boolean thisEmpty = this.isEmpty();
        boolean otherEmpty = other.isEmpty();
        if (thisEmpty & otherEmpty)
            return true;
        if (thisEmpty ^ otherEmpty)
            return false;

        boolean thisSingle = this.isSingleElement();
        boolean otherSingle = other.isSingleElement();
        if (thisSingle & otherSingle)
            return this.lowerLimit().equals(other.lowerLimit());
        if (thisSingle ^ otherSingle)
            return false;

        return compareTo(object) == 0;
    }

    public int hashCode() {
        return lowerLimit().hashCode() ^ upperLimit().hashCode();
    }

    public boolean intersects(Interval other) {
        int comparison = greaterOfLowerLimits(other).compareTo(lesserOfUpperLimits(other));
        if (comparison < 0)
            return true;
        if (comparison > 0)
            return false;
        return greaterOfLowerIncludedInIntersection(other) && lesserOfUpperIncludedInIntersection(other);
    }

    public Interval intersect(Interval other) {
        Comparable intersectLowerBound = greaterOfLowerLimits(other);
        Comparable intersectUpperBound = lesserOfUpperLimits(other);
        if (intersectLowerBound.compareTo(intersectUpperBound) > 0)
            return emptyOfSameType();
        return newOfSameType(intersectLowerBound, greaterOfLowerIncludedInIntersection(other), intersectUpperBound, lesserOfUpperIncludedInIntersection(other));
    }

    public Interval gap(Interval other) {
        if (this.intersects(other))
            return this.emptyOfSameType();

        return newOfSameType(lesserOfUpperLimits(other), !lesserOfUpperIncludedInUnion(other), greaterOfLowerLimits(other), !greaterOfLowerIncludedInUnion(other));
    }

    /** see: http://en.wikipedia.org/wiki/Set_theoretic_complement */
    public List complementRelativeTo(Interval other) {
        List intervalSequence = new ArrayList();
        if (!this.intersects(other)) {
            intervalSequence.add(other);
            return intervalSequence;
        }
        Interval left = leftComplementRelativeTo(other);
        if (left != null)
            intervalSequence.add(left);
        Interval right = rightComplementRelativeTo(other);
        if (right != null)
            intervalSequence.add(right);
        return intervalSequence;
    }

    private Interval leftComplementRelativeTo(Interval other) {
        if (this.includes(lesserOfLowerLimits(other)))
            return null;
        if (lowerLimit().equals(other.lowerLimit()) && !other.includesLowerLimit())
            return null;
        return newOfSameType(other.lowerLimit(), other.includesLowerLimit(), this.lowerLimit(), !this.includesLowerLimit());
    }

    private Interval rightComplementRelativeTo(Interval other) {
        if (this.includes(greaterOfUpperLimits(other)))
            return null;
        if (upperLimit().equals(other.upperLimit()) && !other.includesUpperLimit())
            return null;
        return newOfSameType(this.upperLimit(), !this.includesUpperLimit(), other.upperLimit(), other.includesUpperLimit());
    }

}