/**
 * Copyright (c) 2004 Domain Language, Inc. (http://domainlanguage.com) This
 * free software is distributed under the "MIT" licence. See file licence.txt.
 * For more information, see http://timeandmoney.sourceforge.net.
 */

package com.domainlanguage.time;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.domainlanguage.tests.SerializationTester;

public class TimePointTest extends TestCase {

  private static final String AM = "AM";
  private static final String PM = "PM";
  private static final TimeZone gmt = TimeZone.getTimeZone("Universal");
  private static final TimeZone pt = TimeZone.getTimeZone("America/Los_Angeles");
  private static final TimeZone ct = TimeZone.getTimeZone("America/Chicago");
  private static final TimePoint dec19_2003 = TimePoint.atMidnightGMT(2003, 12, 19);
  private static final TimePoint dec20_2003 = TimePoint.atMidnightGMT(2003, 12, 20);
  private static final TimePoint dec21_2003 = TimePoint.atMidnightGMT(2003, 12, 21);
  private static final TimePoint dec22_2003 = TimePoint.atMidnightGMT(2003, 12, 22);

  public void testSerialization() {
    SerializationTester.assertCanBeSerialized(dec19_2003);
  }

  public void testCreationWithDefaultTimeZone() {
    TimePoint expected = TimePoint.atGMT(2004, 1, 1, 0, 0, 0, 0);
    Assert.assertEquals("at midnight", expected, TimePoint.atMidnightGMT(2004, 1, 1));
    Assert.assertEquals("hours in 24hr clock", expected, TimePoint.atGMT(2004, 1, 1, 0, 0));
    Assert.assertEquals("hours in 12hr clock", expected, TimePoint.at12hr(2004, 1, 1, 12, TimePointTest.AM, 0, 0, 0, gmt));
    Assert.assertEquals("date from formatted String", expected, TimePoint.parseGMTFrom("2004/1/1", "yyyy/MM/dd"));
    Assert.assertEquals(
      "pm hours in 12hr clock",
      TimePoint.atGMT(2004, 1, 1, 12, 0),
      TimePoint.at12hr(2004, 1, 1, 12, TimePointTest.PM, 0, 0, 0, gmt));
  }

  public void testCreationWithTimeZone() {
    /*
     * TimePoints are based on miliseconds from the Epoc. They do not have a
     * "timezone". When that basic value needs to be converted to or from a
     * date or hours and minutes, then a Timezone must be specified or
     * assumed. The default is always GMT. So creation operations which
     * don't pass any Timezone assume the date, hours and minutes are GMT.
     * The TimeLibrary does not use the default TimeZone operation in Java,
     * the selection of the appropriate Timezone is left to the application.
     */
    TimePoint gmt10Hour = TimePoint.at(2004, 3, 5, 10, 10, 0, 0, gmt);
    TimePoint default10Hour = TimePoint.atGMT(2004, 3, 5, 10, 10, 0, 0);
    TimePoint pt2Hour = TimePoint.at(2004, 3, 5, 2, 10, 0, 0, pt);
    Assert.assertEquals(gmt10Hour, default10Hour);
    Assert.assertEquals(gmt10Hour, pt2Hour);

    TimePoint gmt6Hour = TimePoint.at(2004, 3, 5, 6, 0, 0, 0, gmt);
    TimePoint ct0Hour = TimePoint.at(2004, 3, 5, 0, 0, 0, 0, ct);
    TimePoint ctMidnight = TimePoint.atMidnight(2004, 3, 5, ct);
    Assert.assertEquals(gmt6Hour, ct0Hour);
    Assert.assertEquals(gmt6Hour, ctMidnight);
  }

  public void testStringFormat() {
    TimePoint point = TimePoint.at(2004, 3, 12, 5, 3, 14, 0, pt);
    // Try stupid date/time format, so that it couldn't work by accident.
    Assert.assertEquals("3-04-12 3:5:14", point.toString("M-yy-d m:h:s", pt));
    Assert.assertEquals("3-04-12", point.toString("M-yy-d", pt));
  }

  private Date javaUtilDateDec20_2003() {
    Calendar calendar = Calendar.getInstance(gmt);
    calendar.clear(); // non-deterministic without this!!!
    calendar.set(Calendar.YEAR, 2003);
    calendar.set(Calendar.MONTH, Calendar.DECEMBER);
    calendar.set(Calendar.DATE, 20);
    return calendar.getTime();
  }

  public void testAsJavaUtilDate() {
    TimePoint dec20_2003 = TimePoint.atMidnightGMT(2003, 12, 20);
    Assert.assertEquals(javaUtilDateDec20_2003(), dec20_2003.asJavaUtilDate());
  }

  public void testGetTime() {
    TimePoint dec20_2003 = TimePoint.atMidnightGMT(2003, 12, 20);
    Assert.assertEquals(javaUtilDateDec20_2003().getTime(), dec20_2003.getTime());
  }

  public void testBackToMidnight() {
    TimePoint threeOClock = TimePoint.atGMT(2004, 11, 22, 3, 0);
    Assert.assertEquals(TimePoint.atMidnightGMT(2004, 11, 22), threeOClock.backToMidnight(gmt));
    TimePoint thirteenOClock = TimePoint.atGMT(2004, 11, 22, 13, 0);
    Assert.assertEquals(TimePoint.atMidnightGMT(2004, 11, 22), thirteenOClock.backToMidnight(gmt));
  }

  public void testFromString() {
    TimePoint expected = TimePoint.atGMT(2004, 3, 29, 22, 44, 58, 0);
    String source = "2004-Mar-29 10:44:58 PM";
    String pattern = "yyyy-MMM-dd hh:mm:ss a";
    Assert.assertEquals(expected, TimePoint.parseGMTFrom(source, pattern));
  }

  public void testEquals() {
    TimePoint createdFromJavaDate = TimePoint.from(javaUtilDateDec20_2003());
    TimePoint dec5_2003 = TimePoint.atMidnightGMT(2003, 12, 5);
    TimePoint dec20_2003 = TimePoint.atMidnightGMT(2003, 12, 20);
    Assert.assertEquals(createdFromJavaDate, dec20_2003);
    Assert.assertTrue(createdFromJavaDate.equals(dec20_2003));
    Assert.assertFalse(createdFromJavaDate.equals(dec5_2003));
  }

  public void testEqualsOverYearMonthDay() {
    TimePoint thePoint = TimePoint.atGMT(2000, 1, 1, 8, 0);
    TimeZone gmt = TimeZone.getTimeZone("Universal");

    Assert.assertTrue("exactly the same", TimePoint.atGMT(2000, 1, 1, 8, 0).isSameDayAs(thePoint, gmt));
    Assert.assertTrue("same second", TimePoint.atGMT(2000, 1, 1, 8, 0, 0, 500).isSameDayAs(thePoint, gmt));
    Assert.assertTrue("same minute", TimePoint.atGMT(2000, 1, 1, 8, 0, 30, 0).isSameDayAs(thePoint, gmt));
    Assert.assertTrue("same hour", TimePoint.atGMT(2000, 1, 1, 8, 30, 0, 0).isSameDayAs(thePoint, gmt));
    Assert.assertTrue("same day", TimePoint.atGMT(2000, 1, 1, 20, 0).isSameDayAs(thePoint, gmt));
    Assert.assertTrue("midnight (in the moring), start of same day", TimePoint.atMidnightGMT(2000, 1, 1).isSameDayAs(thePoint, gmt));

    Assert.assertFalse("midnight (night), start of next day", TimePoint.atMidnightGMT(2000, 1, 2).isSameDayAs(thePoint, gmt));
    Assert.assertFalse("next day", TimePoint.atGMT(2000, 1, 2, 8, 0).isSameDayAs(thePoint, gmt));
    Assert.assertFalse("next month", TimePoint.atGMT(2000, 2, 1, 8, 0).isSameDayAs(thePoint, gmt));
    Assert.assertFalse("next year", TimePoint.atGMT(2001, 1, 1, 8, 0).isSameDayAs(thePoint, gmt));
  }

  public void testBeforeAfter() {
    TimePoint dec5_2003 = TimePoint.atMidnightGMT(2003, 12, 5);
    TimePoint dec20_2003 = TimePoint.atMidnightGMT(2003, 12, 20);
    Assert.assertTrue(dec5_2003.isBefore(dec20_2003));
    Assert.assertFalse(dec20_2003.isBefore(dec20_2003));
    Assert.assertFalse(dec20_2003.isBefore(dec5_2003));
    Assert.assertFalse(dec5_2003.isAfter(dec20_2003));
    Assert.assertFalse(dec20_2003.isAfter(dec20_2003));
    Assert.assertTrue(dec20_2003.isAfter(dec5_2003));

    TimePoint oneSecondLater = TimePoint.atGMT(2003, 12, 20, 0, 0, 1, 0);
    Assert.assertTrue(dec20_2003.isBefore(oneSecondLater));
    Assert.assertFalse(dec20_2003.isAfter(oneSecondLater));
  }

  public void testIncrementDuration() {
    Duration twoDays = Duration.days(2);
    Assert.assertEquals(dec22_2003, dec20_2003.plus(twoDays));
  }

  public void testDecrementDuration() {
    Duration twoDays = Duration.days(2);
    Assert.assertEquals(dec19_2003, dec21_2003.minus(twoDays));
  }

  // This is only an integration test. The primary responsibility is in
  // TimePeriod
  public void testBeforeAfterPeriod() {
    TimeInterval period = TimeInterval.closed(dec20_2003, dec22_2003);
    Assert.assertTrue(dec19_2003.isBefore(period));
    Assert.assertFalse(dec19_2003.isAfter(period));
    Assert.assertFalse(dec20_2003.isBefore(period));
    Assert.assertFalse(dec20_2003.isAfter(period));
    Assert.assertFalse(dec21_2003.isBefore(period));
    Assert.assertFalse(dec21_2003.isAfter(period));
  }

  public void testNextDay() {
    Assert.assertEquals(dec20_2003, dec19_2003.nextDay());
  }

  public void testCompare() {
    Assert.assertTrue(dec19_2003.compareTo(dec20_2003) < 0);
    Assert.assertTrue(dec20_2003.compareTo(dec19_2003) > 0);
    Assert.assertTrue(dec20_2003.compareTo(dec20_2003) == 0);
  }

  // This test verifies bug #1336072 fix
  // The problem is Duration.days(25) overflowed and became negative
  // on a conversion from a long to int in the bowels of the model.
  // We made the conversion unnecessary
  public void testPotentialProblemDueToOldUsageOf_Duration_toBaseUnitsUsage() {
    TimePoint start = TimePoint.atGMT(2005, 10, 1, 0, 0);
    TimePoint end1 = start.plus(Duration.days(24));
    TimePoint end2 = start.plus(Duration.days(25));
    Assert.assertTrue("Start timepoint is before end1", start.isBefore(end1));
    Assert.assertTrue("and should of course be before end2", start.isBefore(end2));
  }

  // TimePoint.at() ignores the minute parameter.
  public void testNotIgnoringMinuteParameter() {
    TimePoint point = TimePoint.at(2006, 03, 22, 13, 45, 59, 499, gmt);
    Assert.assertEquals("2006-03-22 13:45:59:499", point.toString("yyyy-MM-dd HH:mm:ss:SSS", gmt));
    TimePoint pointNoMilli = TimePoint.at(2006, 03, 22, 13, 45, 59, gmt);
    Assert.assertEquals("2006-03-22 13:45:59:000", pointNoMilli.toString("yyyy-MM-dd HH:mm:ss:SSS", gmt));
  }

  public void testAtWithTimeZone() {
    TimePoint someTime = TimePoint.at(2006, 6, 8, 16, 45, 33, TimeZone.getDefault());
    Calendar someTimeAsJavaCalendar = someTime.asJavaCalendar(TimeZone.getDefault());

    Assert.assertEquals(2006, someTimeAsJavaCalendar.get(Calendar.YEAR));
    Assert.assertEquals(Calendar.JUNE, someTimeAsJavaCalendar.get(Calendar.MONTH));
    Assert.assertEquals(8, someTimeAsJavaCalendar.get(Calendar.DAY_OF_MONTH));
    Assert.assertEquals(16, someTimeAsJavaCalendar.get(Calendar.HOUR_OF_DAY));
    Assert.assertEquals(45, someTimeAsJavaCalendar.get(Calendar.MINUTE));
    Assert.assertEquals(33, someTimeAsJavaCalendar.get(Calendar.SECOND));
  }

  public void testParseIsNotLenient() {
    try {
      TimePoint.parseGMTFrom("Jan 1 2004", "yyyy/MM/dd");
      Assert.fail();
    } catch (RuntimeException re) {
      Assert.assertEquals("Unparseable date: \"Jan 1 2004\"", re.getMessage());
    }
  }
}
