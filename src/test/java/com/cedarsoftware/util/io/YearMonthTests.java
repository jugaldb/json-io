package com.cedarsoftware.util.io;

import java.time.YearMonth;

import org.junit.jupiter.api.Test;

import static com.cedarsoftware.util.io.TestUtil.toObjects;
import static org.assertj.core.api.Assertions.assertThat;

class YearMonthTests extends SerializationDeserializationMinimumTests<YearMonth> {

    @Override
    protected YearMonth provideT1() {
        return YearMonth.of(1970, 6);
    }

    @Override
    protected YearMonth provideT2() {
        return YearMonth.of(1971, 7);
    }

    @Override
    protected YearMonth provideT3() {
        return YearMonth.of(1973, 12);
    }

    @Override
    protected YearMonth provideT4() {
        return YearMonth.of(1950, 1);
    }

    @Override
    protected Class<YearMonth> getTestClass() {
        return YearMonth.class;
    }


    @Override
    protected boolean isReferenceable() {
        return false;
    }

    @Override
    protected Object provideNestedInObject_withNoDuplicates_andFieldTypeMatchesObjectType() {
        return new NestedYearMonth(
                provideT1(),
                provideT2());
    }

    @Override
    protected YearMonth[] extractNestedInObject_withMatchingFieldTypes(Object o) {
        NestedYearMonth nested = (NestedYearMonth) o;

        return new YearMonth[]{
                nested.one,
                nested.two
        };
    }

    @Override
    protected Object provideNestedInObject_withDuplicates_andFieldTypeMatchesObjectType() {
        return new NestedYearMonth(provideT1());
    }

    @Override
    protected void assertT1_serializedWithoutType_parsedAsJsonTypes(YearMonth expected, Object actual) {
        assertThat(actual).isEqualTo("1950-01");
    }

    @Test
    void testOldFormat_objectType() {
        String json = "{\"@type\":\"java.time.YearMonth\",\"year\":1970,\"month\":6}";
        YearMonth date = toObjects(json, null);
        assertThat(date.getYear()).isEqualTo(1970);
        assertThat(date.getMonthValue()).isEqualTo(6);
    }

    @Test
    void testOldFormat_nestedObject() {
        String json = "{\"@type\":\"com.cedarsoftware.util.io.YearMonthTests$NestedYearMonth\",\"one\":{\"@id\":1,\"year\":1970,\"month\":6},\"two\":{\"@ref\":1}}";
        NestedYearMonth date = toObjects(json, null);
        assertThat(date.one.getYear()).isEqualTo(1970);
        assertThat(date.one.getMonthValue()).isEqualTo(6);
        assertThat(date.one).isSameAs(date.two);
    }

    @Test
    void testTopLevel_serializesAsISODate() {
        YearMonth date = YearMonth.of(2014, 10);
        String json = TestUtil.toJson(date);
        YearMonth result = toObjects(json, null);
        assertThat(result).isEqualTo(date);
    }

    @Test
    void testYearMonth_inArray() {
        YearMonth[] initial = new YearMonth[]{
                YearMonth.of(2014, 10),
                YearMonth.of(2023, 6)
        };

        YearMonth[] actual = TestUtil.serializeDeserialize(initial);

        assertThat(actual).isEqualTo(initial);
    }

    private static class NestedYearMonth {
        public YearMonth one;
        public YearMonth two;

        public NestedYearMonth(YearMonth one, YearMonth two) {
            this.one = one;
            this.two = two;
        }

        public NestedYearMonth(YearMonth date) {
            this(date, date);
        }
    }
}
