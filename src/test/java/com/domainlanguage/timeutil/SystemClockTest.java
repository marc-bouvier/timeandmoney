/**
 * Copyright (c) 2004 Domain Language, Inc. (http://domainlanguage.com) This
 * free software is distributed under the "MIT" licence. See file licence.txt.
 * For more information, see http://timeandmoney.sourceforge.net.
 */

package com.domainlanguage.timeutil;

import java.util.Date;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.domainlanguage.time.Duration;
import com.domainlanguage.time.TimePoint;
import com.domainlanguage.time.TimeSource;

public class SystemClockTest extends TestCase {

  public void testSystemClockTimeSource() throws Exception {
    // The following calls allow polymorphic substitution of TimeSources
    // either in applications or, more often, in testing.
    TimeSource source = SystemClock.timeSource();
    TimePoint expectedNow = TimePoint.from(new Date());
    TimePoint now = source.now();
    Assert.assertTrue(expectedNow.until(now).length().compareTo(Duration.milliseconds(50)) < 0);
  }

}
