package com.cedarsoftware.util.io;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import com.cedarsoftware.util.ReturnType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author John DeRegnaucourt (jdereg@gmail.com)
 * <br>
 * Copyright (c) Cedar Software LLC
 * <br><br>
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <br><br>
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">License</a>
 * <br><br>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class UnknownObjectTypeTest
{
    @Test
    public void testUnknownObjects()
    {
        String json = "{\"@type\":\"foo.bar.baz.Qux\", \"name\":\"Joe\"}";
        JsonObject myParams = TestUtil.toObjects(json, new ReadOptions().returnType(ReturnType.JSON_VALUES), null);
        Object inputParams = JsonReader.jsonObjectsToJava(myParams, new ReadOptions());
        assert inputParams instanceof Map;
        String json2 = TestUtil.toJson(inputParams);
    }

    @Test
    public void testUnknownClassType()
    {
        String json = "{\"@type\":\"foo.bar.baz.Qux\", \"name\":\"Joe\"}";
        Map java = TestUtil.toObjects(json, null);
        assert java.get("name").equals("Joe");
    }

    @Test
    public void testUnknownClassTypePassesWhenFailOptionFalse()
    {
        String json = "{\"@type\":\"foo.bar.baz.Qux\", \"name\":\"Joe\"}";
        Map java = TestUtil.toObjects(json, null);// failOnUnknownType = false (default no need to set option)
        assert java.get("name").equals("Joe");
    }

    @Test
    public void testUnknownClassTypeFailsWhenFailOptionTrue()
    {
        String json = "{\"@type\":\"foo.bar.baz.Qux\", \"name\":\"Joe\"}";
        assertThrows(JsonIoException.class, () -> { TestUtil.toObjects(json, new ReadOptions().failOnUnknownType(true), null); });
    }

    @Test
    public void testUnknownClassSwappedWithConcurrentSkipListMap()
    {
        String json = "{\"@type\":\"foo.bar.baz.Qux\", \"name\":\"Joe\",\"age\":50}";
        Map map = TestUtil.toObjects(json, new ReadOptions().unknownTypeClass(ConcurrentSkipListMap.class), null);
        assert map instanceof ConcurrentSkipListMap;
        assert map.get("name").equals("Joe");
        assert map.get("age").equals(50L);
        assert map.size() == 2;
    }

    @Test
    public void testUnknownClassSwappedWithConcurrentSkipListMapButSetToThrowIfMissing()
    {
        String json = "{\"@type\":\"foo.bar.baz.Qux\", \"name\":\"Joe\",\"age\":50}";
        Throwable t = assertThrows(JsonIoException.class, () -> {
            TestUtil.toObjects(json, new ReadOptions().failOnUnknownType(true).unknownTypeClass(ConcurrentSkipListMap.class), null);
        });
        String loMsg = t.getMessage().toLowerCase();
        assert loMsg.contains("unable to create");
        assert loMsg.contains("foo.bar.baz.qux");
        assert loMsg.contains("set 'failonunknowntype' to false");
    }
}
