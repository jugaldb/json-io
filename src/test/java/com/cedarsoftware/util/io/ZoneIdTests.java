package com.cedarsoftware.util.io;

import static org.assertj.core.api.Assertions.assertThat;

import static com.cedarsoftware.util.io.TestUtil.toObjects;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ZoneIdTests extends SerializationDeserializationMinimumTests<ZoneId> {

    @Override
    protected ZoneId provideT1() {
        return ZoneId.of("Asia/Aden");
    }

    @Override
    protected ZoneId provideT2() {
        return ZoneId.of("Z");
    }

    @Override
    protected ZoneId provideT3() {
        return ZoneId.of("GMT");
    }

    @Override
    protected ZoneId provideT4() {
        return ZoneId.of("Etc/GMT-5");
    }

    @Override
    protected Class<ZoneId> getTestClass() {
        return ZoneId.class;
    }


    @Override
    protected boolean isReferenceable() {
        return false;
    }

    @Override
    protected Object provideNestedInObject_withNoDuplicates_andFieldTypeMatchesObjectType() {
        return new NestedZoneId(
                provideT1(),
                provideT2());
    }


    @Override
    protected ZoneId[] extractNestedInObject_withMatchingFieldTypes(Object o) {
        NestedZoneId nested = (NestedZoneId) o;

        return new ZoneId[]{
                nested.one,
                nested.two
        };
    }


    // currently, this test isn't testing that ZoneId type is missing because ZoneId is an abstract represented by either
    // ZoneRegion or ZoneOffset so until we provide aliases to provent the type from being written out we need to leave
    // thise as a check against empty strings because the tyep is always written out since ZoneId type won't match
    // either ZoneRegino or ZoneOffset (basically requires another alias type to prevent it from being written out).
    @Override
    protected void assertNoTypeInString(String json) {
    }

    @Override
    protected Object provideNestedInObject_withDuplicates_andFieldTypeMatchesObjectType() {
        return new NestedZoneId(provideT1());
    }

    @Override
    protected void assertT1_serializedWithoutType_parsedAsJsonTypes(ZoneId expected, Object actual) {
        assertThat(actual).isEqualTo(expected.toString());
    }

    private static Stream<Arguments> argumentsForOldFormat() {
        return Stream.of(
                Arguments.of("{\"@type\":\"java.time.ZoneId\",\"value\":\"+9\"}"),
                Arguments.of("{\"@type\":\"java.time.ZoneId\",\"value\":\"+09\"}")
        );
    }

    protected List<String> getPossibleClassNamesForType() {
        return MetaUtils.listOf(getTestClass().getName(), "java.time.ZoneRegion", "java.time.ZoneOffset");
    }

    @ParameterizedTest
    @MethodSource("argumentsForOldFormat")
    void testOldFormat_objectType(String json) {
        ZoneId zone = toObjects(json, null);
        assertThat(zone).isEqualTo(ZoneId.of("+9"));
    }

    @Test
    void testOldFormat_nestedObject() {
        String json = "{\"@type\":\"com.cedarsoftware.util.io.ZoneIdTests$NestedZoneId\",\"one\":{\"@id\":1,\"value\":\"+05:30\"},\"two\":{\"@ref\":1}}";
        NestedZoneId date = toObjects(json, null);
        assertThat(date.one)
                .isEqualTo(ZoneId.of("+05:30"))
                .isSameAs(date.two);
    }

    @Test
    void testTopLevel_serializesAsISODate() {
        ZoneId initial = ZoneId.of("-0908");
        ZoneId result = TestUtil.serializeDeserialize(initial);
        assertThat(result).isEqualTo(initial);
    }

    @Test
    void testOldFormat_simpleFile() {
        String json = loadJsonForTest("old-format-simple.json");
        ZoneId zone = toObjects(json, ZoneId.class);
        assertThat(zone.getId()).isEqualTo("Asia/Aden");
    }

    @Test
    void testOldFormat_nestedJson() {
        String json = loadJsonForTest("old-format-nested.json");
        NestedZoneId zone = toObjects(json, NestedZoneId.class);
        assertThat(zone.one.getId()).isEqualTo("Asia/Aden");
        assertThat(zone.two.getId()).isEqualTo("Z");
    }

    @Test
    void testOldForm_list() {
        String json = loadJsonForTest("old-format-list.json");
        List list = toObjects(json, null);
        assertThat(((ZoneId) list.get(0)).getId()).isEqualTo("Asia/Aden");
        assertThat(((ZoneId) list.get(3)).getId()).isEqualTo("Z");
    }


    @Test
    void testOldForm_array() {
        String json = loadJsonForTest("old-format-array.json");
        Object[] zone = toObjects(json, Object[].class);
        assertThat(((ZoneId) zone[0]).getId()).isEqualTo("Asia/Aden");
        assertThat(((ZoneId) zone[3]).getId()).isEqualTo("Z");
    }

    @Test
    void testZoneId_inArray() {
        ZoneId[] initial = new ZoneId[]{
                ZoneId.of("+06"),
                ZoneId.of("+06:02")
        };

        ZoneId[] actual = TestUtil.serializeDeserialize(initial);

        assertThat(actual).isEqualTo(initial);
    }

    private static class NestedZoneId {
        public ZoneId one;
        public ZoneId two;

        public NestedZoneId(ZoneId one, ZoneId two) {
            this.one = one;
            this.two = two;
        }

        public NestedZoneId(ZoneId date) {
            this(date, date);
        }
    }

    private String loadJsonForTest(String fileName) {
        return MetaUtils.loadResourceAsString("zoneId/" + fileName);
    }

}
