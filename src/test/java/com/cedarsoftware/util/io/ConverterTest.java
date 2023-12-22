package com.cedarsoftware.util.io;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import static com.cedarsoftware.util.io.Converter.BIG_DECIMAL_ZERO;
import static com.cedarsoftware.util.io.Converter.BIG_INTEGER_ZERO;
import static com.cedarsoftware.util.io.Converter.convert;
import static com.cedarsoftware.util.io.Converter.convert2AtomicBoolean;
import static com.cedarsoftware.util.io.Converter.convert2AtomicInteger;
import static com.cedarsoftware.util.io.Converter.convert2AtomicLong;
import static com.cedarsoftware.util.io.Converter.convert2BigDecimal;
import static com.cedarsoftware.util.io.Converter.convertToAtomicBoolean;
import static com.cedarsoftware.util.io.Converter.convertToAtomicInteger;
import static com.cedarsoftware.util.io.Converter.convertToAtomicLong;
import static com.cedarsoftware.util.io.Converter.convertToBigDecimal;
import static com.cedarsoftware.util.io.Converter.convertToClass;
import static com.cedarsoftware.util.io.Converter.convertToDate;
import static com.cedarsoftware.util.io.Converter.convertToLocalDate;
import static com.cedarsoftware.util.io.Converter.convertToLocalDateTime;
import static com.cedarsoftware.util.io.Converter.convertToSqlDate;
import static com.cedarsoftware.util.io.Converter.convertToTimestamp;
import static com.cedarsoftware.util.io.Converter.convertToUUID;
import static com.cedarsoftware.util.io.Converter.convertToZonedDateTime;
import static com.cedarsoftware.util.io.Converter.localDateTimeToMillis;
import static com.cedarsoftware.util.io.Converter.localDateToMillis;
import static com.cedarsoftware.util.io.Converter.zonedDateTimeToMillis;
import static com.cedarsoftware.util.io.ConverterTest.fubar.bar;
import static com.cedarsoftware.util.io.ConverterTest.fubar.foo;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author John DeRegnaucourt (jdereg@gmail.com) & Ken Partlow
 *         <br>
 *         Copyright (c) Cedar Software LLC
 *         <br><br>
 *         Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *         <br><br>
 *         http://www.apache.org/licenses/LICENSE-2.0
 *         <br><br>
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 */
public class ConverterTest
{
    enum fubar
    {
        foo, bar, baz, quz
    }

    @Test
    public void testConstructorIsPrivateAndClassIsFinal() throws Exception
    {
        Class<?> c = Converter.class;
        assertEquals(Modifier.FINAL, c.getModifiers() & Modifier.FINAL);

        Constructor con = c.getDeclaredConstructor();
        assertEquals(Modifier.PRIVATE, con.getModifiers() & Modifier.PRIVATE);
        con.setAccessible(true);

        assertNotNull(con.newInstance());
    }

    @Test
    public void testByte()
    {
        Byte x = convert("-25", byte.class);
        assert -25 == x;
        x = convert("24", Byte.class);
        assert 24 == x;

        x = convert((byte) 100, byte.class);
        assert 100 == x;
        x = convert((byte) 120, Byte.class);
        assert 120 == x;

        x = convert(new BigDecimal("100"), byte.class);
        assert 100 == x;
        x = convert(new BigInteger("120"), Byte.class);
        assert 120 == x;

        Byte value = convert(true, Byte.class);
        assert value == 1;
        assert (byte)1 == convert(true, Byte.class);
        assert (byte)0 == convert(false, byte.class);

        assert (byte)25 == convert(new AtomicInteger(25), byte.class);
        assert (byte)100 == convert(new AtomicLong(100L), byte.class);
        assert (byte)1 == convert(new AtomicBoolean(true), byte.class);
        assert (byte)0 == convert(new AtomicBoolean(false), byte.class);

        byte z = convert("11.5", byte.class);
        assert z == 11;

        try
        {
            convert(TimeZone.getDefault(), byte.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("unsupported value"));
        }

        try
        {
            convert("45badNumber", byte.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("could not be converted"));
        }

        try
        {
            convert("257", byte.class);
            fail();
        }
        catch (IllegalArgumentException e) { }
    }

    @Test
    public void testShort()
    {
        Short x = convert("-25000", short.class);
        assert -25000 == x;
        x = convert("24000", Short.class);
        assert 24000 == x;

        x = convert((short) 10000, short.class);
        assert 10000 == x;
        x = convert((short) 20000, Short.class);
        assert 20000 == x;

        x = convert(new BigDecimal("10000"), short.class);
        assert 10000 == x;
        x = convert(new BigInteger("20000"), Short.class);
        assert 20000 == x;

        assert (short)1 == convert(true, short.class);
        assert (short)0 == convert(false, Short.class);

        assert (short)25 == convert(new AtomicInteger(25), short.class);
        assert (short)100 == convert(new AtomicLong(100L), Short.class);
        assert (short)1 == convert(new AtomicBoolean(true), Short.class);
        assert (short)0 == convert(new AtomicBoolean(false), Short.class);

        int z = convert("11.5", short.class);
        assert z == 11;

        try
        {
            convert(TimeZone.getDefault(), short.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("unsupported value"));
        }

        try
        {
            convert("45badNumber", short.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("could not be converted"));
        }

        try
        {
            convert("33000", short.class);
            fail();
        }
        catch (IllegalArgumentException e) { }

    }

    @Test
    public void testInt()
    {
        Integer x = convert("-450000", int.class);
        assertEquals((Object) (-450000), x);
        x = convert("550000", Integer.class);
        assertEquals((Object) 550000, x);

        x = convert(100000, int.class);
        assertEquals((Object) 100000, x);
        x = convert(200000, Integer.class);
        assertEquals((Object) 200000, x);

        x = convert(new BigDecimal("100000"), int.class);
        assertEquals((Object) 100000, x);
        x = convert(new BigInteger("200000"), Integer.class);
        assertEquals((Object) 200000, x);

        assert 1 == convert(true, Integer.class);
        assert 0 == convert(false, int.class);

        assert 25 == convert(new AtomicInteger(25), int.class);
        assert 100 == convert(new AtomicLong(100L), Integer.class);
        assert 1 == convert(new AtomicBoolean(true), Integer.class);
        assert 0 == convert(new AtomicBoolean(false), Integer.class);

        int z = convert("11.5", int.class);
        assert z == 11;

        try
        {
            convert(TimeZone.getDefault(), int.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("unsupported value"));
        }

        try
        {
            convert("45badNumber", int.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("could not be converted"));
        }

        try
        {
            convert("2147483649", int.class);
            fail();
        }
        catch (IllegalArgumentException e) { }
    }

    @Test
    public void testLong()
    {
        Long x = convert("-450000", long.class);
        assertEquals((Object)(-450000L), x);
        x = convert("550000", Long.class);
        assertEquals((Object)550000L, x);

        x = convert(100000L, long.class);
        assertEquals((Object)100000L, x);
        x = convert(200000L, Long.class);
        assertEquals((Object)200000L, x);

        x = convert(new BigDecimal("100000"), long.class);
        assertEquals((Object)100000L, x);
        x = convert(new BigInteger("200000"), Long.class);
        assertEquals((Object)200000L, x);

        assert (long)1 == convert(true, long.class);
        assert (long)0 == convert(false, Long.class);

        Date now = new Date();
        long now70 = now.getTime();
        assert now70 == convert(now, long.class);

        Calendar today = Calendar.getInstance();
        now70 = today.getTime().getTime();
        assert now70 == convert(today, Long.class);

        LocalDate localDate = LocalDate.now();
        now70 = Converter.localDateToMillis(localDate);
        assert now70 == convert(localDate, long.class);

        assert 25L == convert(new AtomicInteger(25), long.class);
        assert 100L == convert(new AtomicLong(100L), Long.class);
        assert 1L == convert(new AtomicBoolean(true), Long.class);
        assert 0L == convert(new AtomicBoolean(false), Long.class);

        long z = convert("11.5", int.class);
        assert z == 11;

        try
        {
            convert(TimeZone.getDefault(), long.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("unsupported value"));
        }

        try
        {
            convert("45badNumber", long.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("could not be converted"));
        }
    }

    @Test
    public void testAtomicLong()
    {
        AtomicLong x = convert("-450000", AtomicLong.class);
        assertEquals(-450000L, x.get());
        x = convert("550000", AtomicLong.class);
        assertEquals(550000L, x.get());

        x = convert(100000L, AtomicLong.class);
        assertEquals(100000L, x.get());
        x = convert(200000L, AtomicLong.class);
        assertEquals(200000L, x.get());

        x = convert(new BigDecimal("100000"), AtomicLong.class);
        assertEquals(100000L, x.get());
        x = convert(new BigInteger("200000"), AtomicLong.class);
        assertEquals(200000L, x.get());

        x = convert(true, AtomicLong.class);
        assertEquals((long)1, x.get());
        x = convert(false, AtomicLong.class);
        assertEquals((long)0, x.get());

        Date now = new Date();
        long now70 = now.getTime();
        x =  convert(now, AtomicLong.class);
        assertEquals(now70, x.get());

        Calendar today = Calendar.getInstance();
        now70 = today.getTime().getTime();
        x =  convert(today, AtomicLong.class);
        assertEquals(now70, x.get());

        x = convert(new AtomicInteger(25), AtomicLong.class);
        assertEquals(25L, x.get());
        x = convert(new AtomicLong(100L), AtomicLong.class);
        assertEquals(100L, x.get());
        x = convert(new AtomicBoolean(true), AtomicLong.class);
        assertEquals(1L, x.get());
        x = convert(new AtomicBoolean(false), AtomicLong.class);
        assertEquals(0L, x.get());

        try
        {
            convert(TimeZone.getDefault(), AtomicLong.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("unsupported value"));
        }

        try
        {
            convert("45badNumber", AtomicLong.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("input string: \"45bad"));
        }
    }

    @Test
    public void testString()
    {
        assertEquals("Hello", convert("Hello", String.class));
        assertEquals("25.0", convert(25.0, String.class));
        assertEquals("true", convert(true, String.class));
        assertEquals("J", convert('J', String.class));
        assertEquals("3.1415926535897932384626433", convert(new BigDecimal("3.1415926535897932384626433"), String.class));
        assertEquals("123456789012345678901234567890", convert(new BigInteger("123456789012345678901234567890"), String.class));
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2015, 0, 17, 8, 34, 49);
        assertEquals("2015-01-17T08:34:49", convert(cal.getTime(), String.class));
        assertEquals("2015-01-17T08:34:49", convert(cal, String.class));

        assertEquals("25", convert(new AtomicInteger(25), String.class));
        assertEquals("100", convert(new AtomicLong(100L), String.class));
        assertEquals("true", convert(new AtomicBoolean(true), String.class));

        assertEquals("1.23456789", convert(1.23456789d, String.class));

        int x = 8;
        String s = convert(x, String.class);
        assert s.equals("8");
        // TODO: Add following test once we have preferred method of removing exponential notation, yet retain decimal separator
//        assertEquals("123456789.12345", convert(123456789.12345, String.class));

        try
        {
            convert(TimeZone.getDefault(), String.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("unsupported value"));
        }

        try
        {
            convert(new HashMap<>(), HashMap.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("unsupported type"));
        }

        try
        {
            convert(ZoneId.systemDefault(), String.class);
            fail();
        }
        catch (Exception e)
        {
            TestUtil.assertContainsIgnoreCase(e.getMessage(), "unsupported", "type", "zone");
        }
    }

    @Test
    public void testBigDecimal()
    {
        BigDecimal x = convert("-450000", BigDecimal.class);
        assertEquals(new BigDecimal("-450000"), x);

        assertEquals(new BigDecimal("3.14"), convert(new BigDecimal("3.14"), BigDecimal.class));
        assertEquals(new BigDecimal("8675309"), convert(new BigInteger("8675309"), BigDecimal.class));
        assertEquals(new BigDecimal("75"), convert((short) 75, BigDecimal.class));
        assertEquals(BigDecimal.ONE, convert(true, BigDecimal.class));
        assertSame(BigDecimal.ONE, convert(true, BigDecimal.class));
        assertEquals(BigDecimal.ZERO, convert(false, BigDecimal.class));
        assertSame(BigDecimal.ZERO, convert(false, BigDecimal.class));

        Date now = new Date();
        BigDecimal now70 = new BigDecimal(now.getTime());
        assertEquals(now70, convert(now, BigDecimal.class));

        Calendar today = Calendar.getInstance();
        now70 = new BigDecimal(today.getTime().getTime());
        assertEquals(now70, convert(today, BigDecimal.class));

        assertEquals(new BigDecimal(25), convert(new AtomicInteger(25), BigDecimal.class));
        assertEquals(new BigDecimal(100), convert(new AtomicLong(100L), BigDecimal.class));
        assertEquals(BigDecimal.ONE, convert(new AtomicBoolean(true), BigDecimal.class));
        assertEquals(BigDecimal.ZERO, convert(new AtomicBoolean(false), BigDecimal.class));

        try
        {
            convert(TimeZone.getDefault(), BigDecimal.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("unsupported value"));
        }

        try
        {
            convert("45badNumber", BigDecimal.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("could not be converted"));
        }
    }

    @Test
    public void testBigInteger()
    {
        BigInteger x = convert("-450000", BigInteger.class);
        assertEquals(new BigInteger("-450000"), x);

        assertEquals(new BigInteger("3"), convert(new BigDecimal("3.14"), BigInteger.class));
        assertEquals(new BigInteger("8675309"), convert(new BigInteger("8675309"), BigInteger.class));
        assertEquals(new BigInteger("75"), convert((short) 75, BigInteger.class));
        assertEquals(BigInteger.ONE, convert(true, BigInteger.class));
        assertSame(BigInteger.ONE, convert(true, BigInteger.class));
        assertEquals(BigInteger.ZERO, convert(false, BigInteger.class));
        assertSame(BigInteger.ZERO, convert(false, BigInteger.class));

        Date now = new Date();
        BigInteger now70 = new BigInteger(Long.toString(now.getTime()));
        assertEquals(now70, convert(now, BigInteger.class));

        Calendar today = Calendar.getInstance();
        now70 = new BigInteger(Long.toString(today.getTime().getTime()));
        assertEquals(now70, convert(today, BigInteger.class));

        assertEquals(new BigInteger("25"), convert(new AtomicInteger(25), BigInteger.class));
        assertEquals(new BigInteger("100"), convert(new AtomicLong(100L), BigInteger.class));
        assertEquals(BigInteger.ONE, convert(new AtomicBoolean(true), BigInteger.class));
        assertEquals(BigInteger.ZERO, convert(new AtomicBoolean(false), BigInteger.class));

        try
        {
            convert(TimeZone.getDefault(), BigInteger.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("unsupported value"));
        }

        try
        {
            convert("45badNumber", BigInteger.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("could not be converted"));
        }
    }

    @Test
    public void testAtomicInteger()
    {
        AtomicInteger x =  convert("-450000", AtomicInteger.class);
        assertEquals(-450000, x.get());

        assertEquals(3, ( convert(new BigDecimal("3.14"), AtomicInteger.class)).get());
        assertEquals(8675309, (convert(new BigInteger("8675309"), AtomicInteger.class)).get());
        assertEquals(75, (convert((short) 75, AtomicInteger.class)).get());
        assertEquals(1, (convert(true, AtomicInteger.class)).get());
        assertEquals(0, (convert(false, AtomicInteger.class)).get());

        assertEquals(25, (convert(new AtomicInteger(25), AtomicInteger.class)).get());
        assertEquals(100, (convert(new AtomicLong(100L), AtomicInteger.class)).get());
        assertEquals(1, (convert(new AtomicBoolean(true), AtomicInteger.class)).get());
        assertEquals(0, (convert(new AtomicBoolean(false), AtomicInteger.class)).get());

        try
        {
            convert(TimeZone.getDefault(), AtomicInteger.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("unsupported value"));
        }

        try
        {
            convert("45badNumber", AtomicInteger.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("input string: \"45bad"));
        }
    }

    @Test
    public void testDate()
    {
        // Date to Date
        Date utilNow = new Date();
        Date coerced = convert(utilNow, Date.class);
        assertEquals(utilNow, coerced);
        assertFalse(coerced instanceof java.sql.Date);
        assert coerced != utilNow;

        // Date to java.sql.Date
        java.sql.Date sqlCoerced = convert(utilNow, java.sql.Date.class);
        assertEquals(utilNow, sqlCoerced);

        // java.sql.Date to java.sql.Date
        java.sql.Date sqlNow = new java.sql.Date(utilNow.getTime());
        sqlCoerced = convert(sqlNow, java.sql.Date.class);
        assertEquals(sqlNow, sqlCoerced);

        // java.sql.Date to Date
        coerced = convert(sqlNow, Date.class);
        assertEquals(sqlNow, coerced);
        assertFalse(coerced instanceof java.sql.Date);

        // Date to Timestamp
        Timestamp tstamp = convert(utilNow, Timestamp.class);
        assertEquals(utilNow, tstamp);

        // Timestamp to Date
        Date someDate = convert(tstamp, Date.class);
        assertEquals(utilNow, tstamp);
        assertFalse(someDate instanceof Timestamp);

        // java.sql.Date to Timestamp
        tstamp = convert(sqlCoerced, Timestamp.class);
        assertEquals(sqlCoerced, tstamp);

        // Timestamp to java.sql.Date
        java.sql.Date someDate1 = convert(tstamp, java.sql.Date.class);
        assertEquals(someDate1, utilNow);

        // String to Date
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2015, 0, 17, 9, 54);
        Date date = convert("2015-01-17 09:54", Date.class);
        assertEquals(cal.getTime(), date);
        assert date != null;
        assertFalse(date instanceof java.sql.Date);

        // String to java.sql.Date
        java.sql.Date sqlDate = convert("2015-01-17 09:54", java.sql.Date.class);
        assertEquals(cal.getTime(), sqlDate);
        assert sqlDate != null;

        // Calendar to Date
        date = convert(cal, Date.class);
        assertEquals(date, cal.getTime());
        assert date != null;
        assertFalse(date instanceof java.sql.Date);

        // Calendar to java.sql.Date
        sqlDate = convert(cal, java.sql.Date.class);
        assertEquals(sqlDate, cal.getTime());
        assert sqlDate != null;

        // long to Date
        long now = System.currentTimeMillis();
        Date dateNow = new Date(now);
        Date converted = convert(now, Date.class);
        assert converted != null;
        assertEquals(dateNow, converted);
        assertFalse(converted instanceof java.sql.Date);

        // long to java.sql.Date
        Date sqlConverted = convert(now, java.sql.Date.class);
        assertEquals(dateNow, sqlConverted);
        assert sqlConverted != null;

        // AtomicLong to Date
        now = System.currentTimeMillis();
        dateNow = new Date(now);
        converted = convert(new AtomicLong(now), Date.class);
        assert converted != null;
        assertEquals(dateNow, converted);
        assertFalse(converted instanceof java.sql.Date);

        // long to java.sql.Date
        dateNow = new java.sql.Date(now);
        sqlConverted = convert(new AtomicLong(now), java.sql.Date.class);
        assert sqlConverted != null;
        assertEquals(dateNow, sqlConverted);

        // BigInteger to java.sql.Date
        BigInteger bigInt = new BigInteger("" + now);
        sqlDate = convert(bigInt, java.sql.Date.class);
        assert sqlDate.getTime() == now;

        // BigDecimal to java.sql.Date
        BigDecimal bigDec = new BigDecimal(now);
        sqlDate = convert(bigDec, java.sql.Date.class);
        assert sqlDate.getTime() == now;

        // BigInteger to Timestamp
        bigInt = new BigInteger("" + now);
        tstamp = convert(bigInt, Timestamp.class);
        assert tstamp.getTime() == now;

        // BigDecimal to TimeStamp
        bigDec = new BigDecimal(now);
        tstamp = convert(bigDec, Timestamp.class);
        assert tstamp.getTime() == now;

        // Invalid source type for Date
        try
        {
            convert(TimeZone.getDefault(), Date.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("unsupported value type"));
        }

        // Invalid source type for java.sql.Date
        try
        {
            convert(TimeZone.getDefault(), java.sql.Date.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("unsupported value type"));
        }

        // Invalid source date for Date
        try
        {
            convert("2015/01/33", Date.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("day must be between 1 and 31"));
        }

        // Invalid source date for java.sql.Date
        try
        {
            convert("2015/01/33", java.sql.Date.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("day must be between 1 and 31"));
        }
    }

    @Test
    void testBogusSqlDate2()
    {
        assertThatThrownBy(() -> Converter.convertToSqlDate(true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported value type [java.lang.Boolean (true)] attempting to convert to 'java.sql.Date'");
    }

    @Test
    public void testCalendar()
    {
        // Date to Calendar
        Date now = new Date();
        Calendar calendar = convert(new Date(), Calendar.class);
        assertEquals(calendar.getTime(), now);

        // SqlDate to Calendar
        java.sql.Date sqlDate = convert(now, java.sql.Date.class);
        calendar = convert(sqlDate, Calendar.class);
        assertEquals(calendar.getTime(), sqlDate);

        // Timestamp to Calendar
        Timestamp timestamp = convert(now, Timestamp.class);
        calendar = convert(timestamp, Calendar.class);
        assertEquals(calendar.getTime(), timestamp);

        // Long to Calendar
        calendar = convert(now.getTime(), Calendar.class);
        assertEquals(calendar.getTime(), now);

        // AtomicLong to Calendar
        AtomicLong atomicLong = new AtomicLong(now.getTime());
        calendar = convert(atomicLong, Calendar.class);
        assertEquals(calendar.getTime(), now);

        // String to Calendar
        String strDate = convert(now, String.class);
        calendar = convert(strDate, Calendar.class);
        String strDate2 = convert(calendar, String.class);
        assertEquals(strDate, strDate2);

        // BigInteger to Calendar
        BigInteger bigInt = new BigInteger("" + now.getTime());
        calendar = convert(bigInt, Calendar.class);
        assertEquals(calendar.getTime(), now);

        // BigDecimal to Calendar
        BigDecimal bigDec = new BigDecimal(now.getTime());
        calendar = convert(bigDec, Calendar.class);
        assertEquals(calendar.getTime(), now);

        // Other direction --> Calendar to other date types

        // Calendar to Date
        calendar = convert(now, Calendar.class);
        Date date = convert(calendar, Date.class);
        assertEquals(calendar.getTime(), date);

        // Calendar to SqlDate
        sqlDate = convert(calendar, java.sql.Date.class);
        assertEquals(calendar.getTime().getTime(), sqlDate.getTime());

        // Calendar to Timestamp
        timestamp = convert(calendar, Timestamp.class);
        assertEquals(calendar.getTime().getTime(), timestamp.getTime());

        // Calendar to Long
        long tnow = convert(calendar, long.class);
        assertEquals(calendar.getTime().getTime(), tnow);

        // Calendar to AtomicLong
        atomicLong = convert(calendar, AtomicLong.class);
        assertEquals(calendar.getTime().getTime(), atomicLong.get());

        // Calendar to String
        strDate = convert(calendar, String.class);
        strDate2 = convert(now, String.class);
        assertEquals(strDate, strDate2);

        // Calendar to BigInteger
        bigInt = convert(calendar, BigInteger.class);
        assertEquals(now.getTime(), bigInt.longValue());

        // Calendar to BigDecimal
        bigDec = convert(calendar, BigDecimal.class);
        assertEquals(now.getTime(), bigDec.longValue());
    }

    @Test
    public void testLocalDateToOthers()
    {
        // Date to LocalDate
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2020, 8, 30, 0, 0, 0);
        Date now = calendar.getTime();
        LocalDate localDate = convert(now, LocalDate.class);
        assertEquals(localDateToMillis(localDate), now.getTime());

        // LocalDate to LocalDate - identity check
        LocalDate x = convertToLocalDate(localDate);
        assert localDate == x;

        // LocalDateTime to LocalDate
        LocalDateTime ldt = LocalDateTime.of(2020, 8, 30, 0, 0, 0);
        x = convertToLocalDate(ldt);
        assert localDateTimeToMillis(ldt) == localDateToMillis(x);

        // ZonedDateTime to LocalDate
        ZonedDateTime zdt = ZonedDateTime.of(2020, 8, 30, 0, 0, 0, 0, ZoneId.systemDefault());
        x = convertToLocalDate(zdt);
        assert zonedDateTimeToMillis(zdt) == localDateToMillis(x);

        // Calendar to LocalDate
        x = convertToLocalDate(calendar);
        assert localDateToMillis(localDate) == calendar.getTime().getTime();

        // SqlDate to LocalDate
        java.sql.Date sqlDate = convert(now, java.sql.Date.class);
        localDate = convert(sqlDate, LocalDate.class);
        assertEquals(localDateToMillis(localDate), sqlDate.getTime());

        // Timestamp to LocalDate
        Timestamp timestamp = convert(now, Timestamp.class);
        localDate = convert(timestamp, LocalDate.class);
        assertEquals(localDateToMillis(localDate), timestamp.getTime());

        // Long to LocalDate
        localDate = convert(now.getTime(), LocalDate.class);
        assertEquals(localDateToMillis(localDate), now.getTime());

        // AtomicLong to LocalDate
        AtomicLong atomicLong = new AtomicLong(now.getTime());
        localDate = convert(atomicLong, LocalDate.class);
        assertEquals(localDateToMillis(localDate), now.getTime());

        // String to LocalDate
        String strDate = convert(now, String.class);
        localDate = convert(strDate, LocalDate.class);
        String strDate2 = convert(localDate, String.class);
        assert strDate.startsWith(strDate2);

        // BigInteger to LocalDate
        BigInteger bigInt = new BigInteger("" + now.getTime());
        localDate = convert(bigInt, LocalDate.class);
        assertEquals(localDateToMillis(localDate), now.getTime());

        // BigDecimal to LocalDate
        BigDecimal bigDec = new BigDecimal(now.getTime());
        localDate = convert(bigDec, LocalDate.class);
        assertEquals(localDateToMillis(localDate), now.getTime());

        // Other direction --> LocalDate to other date types

        // LocalDate to Date
        localDate = convert(now, LocalDate.class);
        Date date = convert(localDate, Date.class);
        assertEquals(localDateToMillis(localDate), date.getTime());

        // LocalDate to SqlDate
        sqlDate = convert(localDate, java.sql.Date.class);
        assertEquals(localDateToMillis(localDate), sqlDate.getTime());

        // LocalDate to Timestamp
        timestamp = convert(localDate, Timestamp.class);
        assertEquals(localDateToMillis(localDate), timestamp.getTime());

        // LocalDate to Long
        long tnow = convert(localDate, long.class);
        assertEquals(localDateToMillis(localDate), tnow);

        // LocalDate to AtomicLong
        atomicLong = convert(localDate, AtomicLong.class);
        assertEquals(localDateToMillis(localDate), atomicLong.get());

        // LocalDate to String
        strDate = convert(localDate, String.class);
        strDate2 = convert(now, String.class);
        assert strDate2.startsWith(strDate);

        // LocalDate to BigInteger
        bigInt = convert(localDate, BigInteger.class);
        assertEquals(now.getTime(), bigInt.longValue());

        // LocalDate to BigDecimal
        bigDec = convert(localDate, BigDecimal.class);
        assertEquals(now.getTime(), bigDec.longValue());

        // Error handling
        try {
            convertToLocalDate("2020-12-40");
            fail();
        }
        catch (IllegalArgumentException e) {
            TestUtil.assertContainsIgnoreCase(e.getMessage(), "day must be between 1 and 31");
        }

        assert convertToLocalDate(null) == null;
    }

    @Test
    public void testLocalDateTimeToOthers()
    {
        // Date to LocalDateTime
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2020, 8, 30, 13, 1, 11);
        Date now = calendar.getTime();
        LocalDateTime localDateTime = convert(now, LocalDateTime.class);
        assertEquals(localDateTimeToMillis(localDateTime), now.getTime());

        // LocalDateTime to LocalDateTime - identity check
        LocalDateTime x = convertToLocalDateTime(localDateTime);
        assert localDateTime == x;

        // LocalDate to LocalDateTime
        LocalDate ld = LocalDate.of(2020, 8, 30);
        x = convertToLocalDateTime(ld);
        assert localDateToMillis(ld) == localDateTimeToMillis(x);

        // ZonedDateTime to LocalDateTime
        ZonedDateTime zdt = ZonedDateTime.of(2020, 8, 30, 13, 1, 11, 0, ZoneId.systemDefault());
        x = convertToLocalDateTime(zdt);
        assert zonedDateTimeToMillis(zdt) == localDateTimeToMillis(x);

        // Calendar to LocalDateTime
        x = convertToLocalDateTime(calendar);
        assert localDateTimeToMillis(localDateTime) == calendar.getTime().getTime();

        // SqlDate to LocalDateTime
        java.sql.Date sqlDate = convert(now, java.sql.Date.class);
        localDateTime = convert(sqlDate, LocalDateTime.class);
        assertEquals(localDateTimeToMillis(localDateTime), localDateToMillis(sqlDate.toLocalDate()));

        // Timestamp to LocalDateTime
        Timestamp timestamp = convert(now, Timestamp.class);
        localDateTime = convert(timestamp, LocalDateTime.class);
        assertEquals(localDateTimeToMillis(localDateTime), timestamp.getTime());

        // Long to LocalDateTime
        localDateTime = convert(now.getTime(), LocalDateTime.class);
        assertEquals(localDateTimeToMillis(localDateTime), now.getTime());

        // AtomicLong to LocalDateTime
        AtomicLong atomicLong = new AtomicLong(now.getTime());
        localDateTime = convert(atomicLong, LocalDateTime.class);
        assertEquals(localDateTimeToMillis(localDateTime), now.getTime());

        // String to LocalDateTime
        String strDate = convert(now, String.class);
        localDateTime = convert(strDate, LocalDateTime.class);
        String strDate2 = convert(localDateTime, String.class);
        assert strDate.startsWith(strDate2);

        // BigInteger to LocalDateTime
        BigInteger bigInt = new BigInteger("" + now.getTime());
        localDateTime = convert(bigInt, LocalDateTime.class);
        assertEquals(localDateTimeToMillis(localDateTime), now.getTime());

        // BigDecimal to LocalDateTime
        BigDecimal bigDec = new BigDecimal(now.getTime());
        localDateTime = convert(bigDec, LocalDateTime.class);
        assertEquals(localDateTimeToMillis(localDateTime), now.getTime());

        // Other direction --> LocalDateTime to other date types

        // LocalDateTime to Date
        localDateTime = convert(now, LocalDateTime.class);
        Date date = convert(localDateTime, Date.class);
        assertEquals(localDateTimeToMillis(localDateTime), date.getTime());

        // LocalDateTime to SqlDate
        sqlDate = convert(localDateTime, java.sql.Date.class);
        assertEquals(localDateTimeToMillis(localDateTime), sqlDate.getTime());

        // LocalDateTime to Timestamp
        timestamp = convert(localDateTime, Timestamp.class);
        assertEquals(localDateTimeToMillis(localDateTime), timestamp.getTime());

        // LocalDateTime to Long
        long tnow = convert(localDateTime, long.class);
        assertEquals(localDateTimeToMillis(localDateTime), tnow);

        // LocalDateTime to AtomicLong
        atomicLong = convert(localDateTime, AtomicLong.class);
        assertEquals(localDateTimeToMillis(localDateTime), atomicLong.get());

        // LocalDateTime to String
        strDate = convert(localDateTime, String.class);
        strDate2 = convert(now, String.class);
        assert strDate2.startsWith(strDate);

        // LocalDateTime to BigInteger
        bigInt = convert(localDateTime, BigInteger.class);
        assertEquals(now.getTime(), bigInt.longValue());

        // LocalDateTime to BigDecimal
        bigDec = convert(localDateTime, BigDecimal.class);
        assertEquals(now.getTime(), bigDec.longValue());

        // Error handling
        try
        {
            convertToLocalDateTime("2020-12-40");
            fail();
        }
        catch (IllegalArgumentException e)
        {
            TestUtil.assertContainsIgnoreCase(e.getMessage(), "day must be between 1 and 31");
        }

        assert convertToLocalDateTime(null) == null;
    }

    @Test
    public void testZonedDateTimeToOthers()
    {
        // Date to ZonedDateTime
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2020, 8, 30, 13, 1, 11);
        Date now = calendar.getTime();
        ZonedDateTime zonedDateTime = convert(now, ZonedDateTime.class);
        assertEquals(zonedDateTimeToMillis(zonedDateTime), now.getTime());

        // ZonedDateTime to ZonedDateTime - identity check
        ZonedDateTime x = convertToZonedDateTime(zonedDateTime);
        assert zonedDateTime == x;

        // LocalDate to ZonedDateTime
        LocalDate ld = LocalDate.of(2020, 8, 30);
        x = convertToZonedDateTime(ld);
        assert localDateToMillis(ld) == zonedDateTimeToMillis(x);

        // LocalDateTime to ZonedDateTime
        LocalDateTime ldt = LocalDateTime.of(2020, 8, 30, 13, 1, 11);
        x = convertToZonedDateTime(ldt);
        assert localDateTimeToMillis(ldt) == zonedDateTimeToMillis(x);

        // ZonedDateTime to ZonedDateTime
        ZonedDateTime zdt = ZonedDateTime.of(2020, 8, 30, 13, 1, 11, 0, ZoneId.systemDefault());
        x = convertToZonedDateTime(zdt);
        assert zonedDateTimeToMillis(zdt) == zonedDateTimeToMillis(x);

        // Calendar to ZonedDateTime
        x = convertToZonedDateTime(calendar);
        assert zonedDateTimeToMillis(zonedDateTime) == calendar.getTime().getTime();

        // SqlDate to ZonedDateTime
        java.sql.Date sqlDate = convert(now, java.sql.Date.class);
        zonedDateTime = convert(sqlDate, ZonedDateTime.class);
        assertEquals(zonedDateTimeToMillis(zonedDateTime), localDateToMillis(sqlDate.toLocalDate()));

        // Timestamp to ZonedDateTime
        Timestamp timestamp = convert(now, Timestamp.class);
        zonedDateTime = convert(timestamp, ZonedDateTime.class);
        assertEquals(zonedDateTimeToMillis(zonedDateTime), timestamp.getTime());

        // Long to ZonedDateTime
        zonedDateTime = convert(now.getTime(), ZonedDateTime.class);
        assertEquals(zonedDateTimeToMillis(zonedDateTime), now.getTime());

        // AtomicLong to ZonedDateTime
        AtomicLong atomicLong = new AtomicLong(now.getTime());
        zonedDateTime = convert(atomicLong, ZonedDateTime.class);
        assertEquals(zonedDateTimeToMillis(zonedDateTime), now.getTime());

        // String to ZonedDateTime
        String strDate = convert(now, String.class);
        zonedDateTime = convert(strDate, ZonedDateTime.class);
        String strDate2 = convert(zonedDateTime, String.class);
        assert strDate2.startsWith(strDate);

        // BigInteger to ZonedDateTime
        BigInteger bigInt = new BigInteger("" + now.getTime());
        zonedDateTime = convert(bigInt, ZonedDateTime.class);
        assertEquals(zonedDateTimeToMillis(zonedDateTime), now.getTime());

        // BigDecimal to ZonedDateTime
        BigDecimal bigDec = new BigDecimal(now.getTime());
        zonedDateTime = convert(bigDec, ZonedDateTime.class);
        assertEquals(zonedDateTimeToMillis(zonedDateTime), now.getTime());

        // Other direction --> ZonedDateTime to other date types

        // ZonedDateTime to Date
        zonedDateTime = convert(now, ZonedDateTime.class);
        Date date = convert(zonedDateTime, Date.class);
        assertEquals(zonedDateTimeToMillis(zonedDateTime), date.getTime());

        // ZonedDateTime to SqlDate
        sqlDate = convert(zonedDateTime, java.sql.Date.class);
        assertEquals(zonedDateTimeToMillis(zonedDateTime), sqlDate.getTime());

        // ZonedDateTime to Timestamp
        timestamp = convert(zonedDateTime, Timestamp.class);
        assertEquals(zonedDateTimeToMillis(zonedDateTime), timestamp.getTime());

        // ZonedDateTime to Long
        long tnow = convert(zonedDateTime, long.class);
        assertEquals(zonedDateTimeToMillis(zonedDateTime), tnow);

        // ZonedDateTime to AtomicLong
        atomicLong = convert(zonedDateTime, AtomicLong.class);
        assertEquals(zonedDateTimeToMillis(zonedDateTime), atomicLong.get());

        // ZonedDateTime to String
        strDate = convert(zonedDateTime, String.class);
        strDate2 = convert(now, String.class);
        assert strDate.startsWith(strDate2);

        // ZonedDateTime to BigInteger
        bigInt = convert(zonedDateTime, BigInteger.class);
        assertEquals(now.getTime(), bigInt.longValue());

        // ZonedDateTime to BigDecimal
        bigDec = convert(zonedDateTime, BigDecimal.class);
        assertEquals(now.getTime(), bigDec.longValue());

        // Error handling
        try {
            convertToZonedDateTime("2020-12-40");
            fail();
        }
        catch (IllegalArgumentException e) {
            TestUtil.assertContainsIgnoreCase(e.getCause().getMessage(), "day must be between 1 and 31");
        }

        assert convertToZonedDateTime(null) == null;
    }

    @Test
    public void testDateErrorHandlingBadInput()
    {
        assertNull(convert(" ", java.util.Date.class));
        assertNull(convert("", java.util.Date.class));
        assertNull(convert(null, java.util.Date.class));

        assertNull(convertToDate(" "));
        assertNull(convertToDate(""));
        assertNull(convertToDate(null));

        assertNull(convert(" ", java.sql.Date.class));
        assertNull(convert("", java.sql.Date.class));
        assertNull(convert(null, java.sql.Date.class));

        assertNull(convertToSqlDate(" "));
        assertNull(convertToSqlDate(""));
        assertNull(convertToSqlDate(null));

        assertNull(convert(" ", java.sql.Timestamp.class));
        assertNull(convert("", java.sql.Timestamp.class));
        assertNull(convert(null, java.sql.Timestamp.class));

        assertNull(convertToTimestamp(" "));
        assertNull(convertToTimestamp(""));
        assertNull(convertToTimestamp(null));
    }

    @Test
    public void testTimestamp()
    {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        assertEquals(now, convert(now, Timestamp.class));
        assert convert(now, Timestamp.class) instanceof Timestamp;

        Timestamp christmas = convert("2015/12/25", Timestamp.class);
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(2015, 11, 25);
        assert christmas.getTime() == c.getTime().getTime();

        Timestamp christmas2 = convert(c, Timestamp.class);

        assertEquals(christmas, christmas2);
        assertEquals(christmas2, convert(christmas.getTime(), Timestamp.class));

        AtomicLong al = new AtomicLong(christmas.getTime());
        assertEquals(christmas2, convert(al, Timestamp.class));

        ZonedDateTime zdt = ZonedDateTime.of(2020, 8, 30, 13, 11, 17, 0, ZoneId.systemDefault());
        Timestamp alexaBirthday = convertToTimestamp(zdt);
        assert alexaBirthday.getTime() == zonedDateTimeToMillis(zdt);
        try
        {
            convert(Boolean.TRUE, Timestamp.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assert e.getMessage().toLowerCase().contains("unsupported value type");
        }

        try
        {
            convert("123dhksdk", Timestamp.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assert e.getMessage().toLowerCase().contains("unable to parse: 123");
        }
    }

    @Test
    public void testFloat()
    {
        assert -3.14f == convert(-3.14f, float.class);
        assert -3.14f == convert(-3.14f, Float.class);
        assert -3.14f == convert("-3.14", float.class);
        assert -3.14f == convert("-3.14", Float.class);
        assert -3.14f == convert(-3.14d, float.class);
        assert -3.14f == convert(-3.14d, Float.class);
        assert 1.0f == convert(true, float.class);
        assert 1.0f == convert(true, Float.class);
        assert 0.0f == convert(false, float.class);
        assert 0.0f == convert(false, Float.class);

        assert 0.0f == convert(new AtomicInteger(0), Float.class);
        assert 0.0f == convert(new AtomicLong(0), Float.class);
        assert 0.0f == convert(new AtomicBoolean(false), Float.class);
        assert 1.0f == convert(new AtomicBoolean(true), Float.class);

        try
        {
            convert(TimeZone.getDefault(), float.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("unsupported value"));
        }

        try
        {
            convert("45.6badNumber", Float.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("could not be converted"));
        }
    }

    @Test
    public void testDouble()
    {
        assert -3.14d == convert(-3.14d, double.class);
        assert -3.14d == convert(-3.14d, Double.class);
        assert -3.14d == convert("-3.14", double.class);
        assert -3.14d == convert("-3.14", Double.class);
        assert -3.14d == convert(new BigDecimal("-3.14"), double.class);
        assert -3.14d == convert(new BigDecimal("-3.14"), Double.class);
        assert 1.0d == convert(true, double.class);
        assert 1.0d == convert(true, Double.class);
        assert 0.0d == convert(false, double.class);
        assert 0.0d == convert(false, Double.class);

        assert 0.0d == convert(new AtomicInteger(0), double.class);
        assert 0.0d == convert(new AtomicLong(0), double.class);
        assert 0.0d == convert(new AtomicBoolean(false), Double.class);
        assert 1.0d == convert(new AtomicBoolean(true), Double.class);

        try
        {
            convert(TimeZone.getDefault(), double.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("unsupported value"));
        }

        try
        {
            convert("45.6badNumber", Double.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("could not be converted"));
        }
    }

    @Test
    public void testBoolean()
    {
        assertEquals(true, convert(-3.14d, boolean.class));
        assertEquals(false, convert(0.0d, boolean.class));
        assertEquals(true, convert(-3.14f, Boolean.class));
        assertEquals(false, convert(0.0f, Boolean.class));

        assertEquals(false, convert(new AtomicInteger(0), boolean.class));
        assertEquals(false, convert(new AtomicLong(0), boolean.class));
        assertEquals(false, convert(new AtomicBoolean(false), Boolean.class));
        assertEquals(true, convert(new AtomicBoolean(true), Boolean.class));

        assertEquals(true, convert("TRue", Boolean.class));
        assertEquals(true, convert("true", Boolean.class));
        assertEquals(false, convert("fALse", Boolean.class));
        assertEquals(false, convert("false", Boolean.class));
        assertEquals(false, convert("john", Boolean.class));

        assertEquals(true, convert(true, Boolean.class));
        assertEquals(true, convert(Boolean.TRUE, Boolean.class));
        assertEquals(false, convert(false, Boolean.class));
        assertEquals(false, convert(Boolean.FALSE, Boolean.class));

        try
        {
            convert(new Date(), Boolean.class);
            fail();
        }
        catch (Exception e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("unsupported value"));
        }
    }

    @Test
    public void testAtomicBoolean()
    {
        assert (convert(-3.14d, AtomicBoolean.class)).get();
        assert !(convert(0.0d, AtomicBoolean.class)).get();
        assert (convert(-3.14f, AtomicBoolean.class)).get();
        assert !(convert(0.0f, AtomicBoolean.class)).get();

        assert !(convert(new AtomicInteger(0), AtomicBoolean.class)).get();
        assert !(convert(new AtomicLong(0), AtomicBoolean.class)).get();
        assert !(convert(new AtomicBoolean(false), AtomicBoolean.class)).get();
        assert (convert(new AtomicBoolean(true), AtomicBoolean.class)).get();

        assert (convert("TRue", AtomicBoolean.class)).get();
        assert !(convert("fALse", AtomicBoolean.class)).get();
        assert !(convert("john", AtomicBoolean.class)).get();

        assert (convert(true, AtomicBoolean.class)).get();
        assert (convert(Boolean.TRUE, AtomicBoolean.class)).get();
        assert !(convert(false, AtomicBoolean.class)).get();
        assert !(convert(Boolean.FALSE, AtomicBoolean.class)).get();

        AtomicBoolean b1 = new AtomicBoolean(true);
        AtomicBoolean b2 = convert(b1, AtomicBoolean.class);
        assert b1 != b2; // ensure that it returns a different but equivalent instance
        assert b1.get() == b2.get();

        try
        {
            convert(new Date(), AtomicBoolean.class);
            fail();
        }
        catch (Exception e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("unsupported value"));
        }
    }

    @Test
    public void testUnsupportedType()
    {
        try
        {
            convert("Lamb", TimeZone.class);
            fail();
        }
        catch (Exception e)
        {
            assertTrue(e.getMessage().toLowerCase().contains("unsupported type"));
        }
    }

    @Test
    public void testNullInstance()
    {
        assert false == convert(null, boolean.class);
        assert null == convert(null, Boolean.class);
        assert 0 == convert(null, byte.class);
        assert null == convert(null, Byte.class);
        assert 0 == convert(null, short.class);
        assert null == convert(null, Short.class);
        assert 0 == convert(null, int.class);
        assert null == convert(null, Integer.class);
        assert 0L == convert(null, long.class);
        assert null == convert(null, Long.class);
        assert 0.0f == convert(null, float.class);
        assert null == convert(null, Float.class);
        assert 0.0d == convert(null, double.class);
        assert null == convert(null, Double.class);
        assert (char)0 == convert(null, char.class);
        assert null == convert(null, Character.class);

        assert null == convert(null, Date.class);
        assert null == convert(null, java.sql.Date.class);
        assert null == convert(null, Timestamp.class);
        assert null == convert(null, Calendar.class);
        assert null == convert(null, String.class);
        assert null == convert(null, BigInteger.class);
        assert null == convert(null, BigDecimal.class);
        assert null == convert(null, AtomicBoolean.class);
        assert null == convert(null, AtomicInteger.class);
        assert null == convert(null, AtomicLong.class);

        assert null == convert(null, Byte.class);
        assert null == convert(null, Integer.class);
        assert null == convert(null, Short.class);
        assert null == convert(null, Long.class);
        assert null == convert(null, Float.class);
        assert null == convert(null, Double.class);
        assert null == convert(null, Character.class);
        assert null == convertToDate(null);
        assert null == convertToSqlDate(null);
        assert null == convertToTimestamp(null);
        assert null == convertToAtomicBoolean(null);
        assert null == convertToAtomicInteger(null);
        assert null == convertToAtomicLong(null);
        assert null == convert(null, String.class);

        assert false == convert(null, boolean.class);
        assert 0 == convert(null, byte.class);
        assert 0 == convert(null, int.class);
        assert 0 == convert(null, short.class);
        assert 0 == convert(null, long.class);
        assert 0.0f == convert(null, float.class);
        assert 0.0d == convert(null, double.class);
        assert (char)0 == convert(null, char.class);
        assert null == convert(null, BigInteger.class);
        assert BIG_DECIMAL_ZERO == convert2BigDecimal(null);
        assert false == convert2AtomicBoolean(null).get();
        assert 0 == convert2AtomicInteger(null).get();
        assert 0L == convert2AtomicLong(null).get();
        assert null == convert(null, String.class);
    }

    @Test
    public void testConvert2()
    {
        assert convert("true", boolean.class);
        assert convert("true", Boolean.class);
        assert !convert("false", boolean.class);
        assert !convert("false", Boolean.class);
        assert !convert("", boolean.class);
        assert !convert("", Boolean.class);
        assert !convert(null, boolean.class);
        assert null == convert(null, Boolean.class);
        assert -8 == convert("-8", byte.class);
        assert -8 == convert("-8", int.class);
        assert -8 == convert("-8", short.class);
        assert -8 == convert("-8", long.class);
        assert -8.0f == convert("-8", float.class);
        assert -8.0d == convert("-8", double.class);
        assert 'A' == convert(65, char.class);
        assert new BigInteger("-8").equals(convert("-8", BigInteger.class));
        assert new BigDecimal(-8.0d).equals(convert2BigDecimal("-8"));
        assert convert2AtomicBoolean("true").get();
        assert -8 == convert2AtomicInteger("-8").get();
        assert -8L == convert2AtomicLong("-8").get();
        assert "-8".equals(convert(-8, String.class));
    }

    @Test
    public void testNullType()
    {
        try
        {
            convert("123", null);
            fail();
        }
        catch (Exception e)
        {
            e.getMessage().toLowerCase().contains("type cannot be null");
        }
    }

    @Test
    public void testEmptyString()
    {
        assertEquals(false, convert("", boolean.class));
        assertEquals(false, convert("", boolean.class));
        assert (byte) 0 == convert("", byte.class);
        assert (short) 0 == convert("", short.class);
        assert 0 == convert("", int.class);
        assert (long) 0 == convert("", long.class);
        assert 0.0f == convert("", float.class);
        assert 0.0d == convert("", double.class);
        assertEquals(BigDecimal.ZERO, convert("", BigDecimal.class));
        assertEquals(BigInteger.ZERO, convert("", BigInteger.class));
        assertEquals(new AtomicBoolean(false).get(), convert("", AtomicBoolean.class).get());
        assertEquals(new AtomicInteger(0).get(), convert("", AtomicInteger.class).get());
        assertEquals(new AtomicLong(0L).get(), convert("", AtomicLong.class).get());
    }

    @Test
    public void testEnumSupport()
    {
        assertEquals("foo", convert(foo, String.class));
        assertEquals("bar", convert(bar, String.class));
    }

    @Test
    public void testCharacterSupport()
    {
        assert 65 == convert('A', Short.class);
        assert 65 == convert('A', short.class);
        assert 65 == convert('A', Integer.class);
        assert 65 == convert('A', int.class);
        assert 65 == convert('A', Long.class);
        assert 65 == convert('A', long.class);
        assert 65 == convert('A', BigInteger.class).longValue();
        assert 65 == convert('A', BigDecimal.class).longValue();

        assert '1' == convert(true, char.class);
        assert '0' == convert(false, char.class);
        assert '1' == convert(new AtomicBoolean(true), char.class);
        assert '0' == convert(new AtomicBoolean(false), char.class);
        assert 'z' == convert('z', char.class);
        assert 0 == convert("", char.class);
        assert 0 == convert("", Character.class);
        assert 'A' == convert("65", char.class);
        assert 'A' == convert("65", Character.class);
        try
        {
            convert("This is not a number", char.class);
            fail();
        }
        catch (IllegalArgumentException e) { }
        try
        {
            convert(new Date(), char.class);
            fail();
        }
        catch (IllegalArgumentException e) { }
    }

    @Test
    public void testConvertUnknown()
    {
        try
        {
            convert(TimeZone.getDefault(), String.class);
            fail();
        }
        catch (IllegalArgumentException e) { }
    }

    @Test
    public void testLongToBigDecimal()
    {
        BigDecimal big = convert2BigDecimal(7L);
        assert big instanceof BigDecimal;
        assert big.longValue() == 7L;

        big = convertToBigDecimal(null);
        assert big == null;
    }

    @Test
    public void testLocalDate()
    {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2020, 8, 4);   // 0-based for month

        BigDecimal big = convert2BigDecimal(LocalDate.of(2020, 9, 4));
        assert big.longValue() == cal.getTime().getTime();

        BigInteger bigI = convert(LocalDate.of(2020, 9, 4), BigInteger.class);
        assert bigI.longValue() == cal.getTime().getTime();

        java.sql.Date sqlDate = convertToSqlDate(LocalDate.of(2020, 9, 4));
        assert sqlDate.getTime() == cal.getTime().getTime();

        Timestamp timestamp = convertToTimestamp(LocalDate.of(2020, 9, 4));
        assert timestamp.getTime() == cal.getTime().getTime();

        Date date = convertToDate(LocalDate.of(2020, 9, 4));
        assert date.getTime() == cal.getTime().getTime();

        Long lng = convert(LocalDate.of(2020, 9, 4), Long.class);
        assert lng == cal.getTime().getTime();

        AtomicLong atomicLong = convertToAtomicLong(LocalDate.of(2020, 9, 4));
        assert atomicLong.get() == cal.getTime().getTime();
    }

    @Test
    public void testLocalDateTimeToBig()
    {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2020, 8, 8, 13, 11, 1);   // 0-based for month

        BigDecimal big = convert2BigDecimal(LocalDateTime.of(2020, 9, 8, 13, 11, 1));
        assert big.longValue() == cal.getTime().getTime();

        BigInteger bigI = convert(LocalDateTime.of(2020, 9, 8, 13, 11, 1), BigInteger.class);
        assert bigI.longValue() == cal.getTime().getTime();

        java.sql.Date sqlDate = convertToSqlDate(LocalDateTime.of(2020, 9, 8, 13, 11, 1));
        assert sqlDate.getTime() == cal.getTime().getTime();

        Timestamp timestamp = convertToTimestamp(LocalDateTime.of(2020, 9, 8, 13, 11, 1));
        assert timestamp.getTime() == cal.getTime().getTime();

        Date date = convertToDate(LocalDateTime.of(2020, 9, 8, 13, 11, 1));
        assert date.getTime() == cal.getTime().getTime();

        Long lng = convert(LocalDateTime.of(2020, 9, 8, 13, 11, 1), Long.class);
        assert lng == cal.getTime().getTime();

        AtomicLong atomicLong = convertToAtomicLong(LocalDateTime.of(2020, 9, 8, 13, 11, 1));
        assert atomicLong.get() == cal.getTime().getTime();
    }

    @Test
    public void testLocalZonedDateTimeToBig()
    {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2020, 8, 8, 13, 11, 1);   // 0-based for month

        BigDecimal big = convert2BigDecimal(ZonedDateTime.of(2020, 9, 8, 13, 11, 1, 0, ZoneId.systemDefault()));
        assert big.longValue() == cal.getTime().getTime();

        BigInteger bigI = convert(ZonedDateTime.of(2020, 9, 8, 13, 11, 1, 0, ZoneId.systemDefault()), BigInteger.class);
        assert bigI.longValue() == cal.getTime().getTime();

        java.sql.Date sqlDate = convertToSqlDate(ZonedDateTime.of(2020, 9, 8, 13, 11, 1, 0, ZoneId.systemDefault()));
        assert sqlDate.getTime() == cal.getTime().getTime();

        Date date = convertToDate(ZonedDateTime.of(2020, 9, 8, 13, 11, 1, 0, ZoneId.systemDefault()));
        assert date.getTime() == cal.getTime().getTime();

        AtomicLong atomicLong = convertToAtomicLong(ZonedDateTime.of(2020, 9, 8, 13, 11, 1, 0, ZoneId.systemDefault()));
        assert atomicLong.get() == cal.getTime().getTime();
    }

    @Test
    public void testStringToClass()
    {
        Class<?> clazz = convertToClass("java.math.BigInteger");
        assert clazz.getName().equals("java.math.BigInteger");

        assertThatThrownBy(() -> convertToClass("foo.bar.baz.Qux"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("value [java.lang.String (foo.bar.baz.Qux)] could not be converted to a 'Class'");

        assertThatThrownBy(() -> convertToClass(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("value [null] could not be converted to a 'Class'");

        assertThatThrownBy(() -> convertToClass(16.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported value type [java.lang.Double (16.0)] attempting to convert to 'Class'");
    }

    @Test
    void testClassToClass()
    {
        Class<?> clazz = convertToClass(ConverterTest.class);
        assert clazz.getName() == ConverterTest.class.getName();
    }

    @Test
    public void testStringToUUID()
    {
        UUID uuid = Converter.convertToUUID("00000000-0000-0000-0000-000000000064");
        BigInteger bigInt = Converter.convert(uuid, BigInteger.class);
        assert bigInt.intValue() == 100;

        assertThatThrownBy(() -> Converter.convertToUUID("00000000"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("value [java.lang.String (00000000)] could not be converted to a 'UUID'");
    }

    @Test
    public void testUUIDToUUID()
    {
        UUID uuid = Converter.convertToUUID("00000007-0000-0000-0000-000000000064");
        UUID uuid2 = Converter.convertToUUID(uuid);
        assert uuid.equals(uuid2);
    }

    @Test
    public void testBogusToUUID()
    {
        assertThatThrownBy(() -> Converter.convertToUUID((short)77))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported value type [java.lang.Short (77)] attempting to convert to 'UUID'");
    }

    @Test
    public void testBigIntegerToUUID()
    {
        UUID uuid = convertToUUID(new BigInteger("100"));
        BigInteger hundred = convert(uuid, BigInteger.class);
        assert hundred.intValue() == 100;
    }

    @Test
    public void testUUIDToBigInteger()
    {
        BigInteger bigInt = Converter.convert(UUID.fromString("00000000-0000-0000-0000-000000000064"), BigInteger.class);
        assert bigInt.intValue() == 100;

        bigInt = Converter.convert(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"), BigInteger.class);
        assert bigInt.toString().equals("-18446744073709551617");

        bigInt = Converter.convert(UUID.fromString("00000000-0000-0000-0000-000000000000"), BigInteger.class);
        assert bigInt.intValue() == 0;

        assertThatThrownBy(() -> convertToClass(16.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported value type [java.lang.Double (16.0)] attempting to convert to 'Class'");
    }

    @Test
    public void testMapToUUID()
    {
        UUID uuid = convertToUUID(new BigInteger("100"));
        Map<String, Object> map = new HashMap<>();
        map.put("mostSigBits", uuid.getMostSignificantBits());
        map.put("leastSigBits", uuid.getLeastSignificantBits());
        UUID hundred = convertToUUID(map);
        assertEquals("00000000-0000-0000-0000-000000000064", hundred.toString());
    }

    @Test
    public void testBadMapToUUID()
    {
        UUID uuid = convertToUUID(new BigInteger("100"));
        Map<String, Object> map = new HashMap<>();
        map.put("leastSigBits", uuid.getLeastSignificantBits());
        assertThatThrownBy(() -> convertToUUID(map))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("value [java.util.HashMap ({leastSigBits=100})] could not be converted to a 'UUID'");
    }

    @Test
    public void testClassToString()
    {
        String str = Converter.convert(BigInteger.class, String.class);
        assert str.equals("java.math.BigInteger");
        
        str = Converter.convert(null, String.class);
        assert str == null;
    }
}
