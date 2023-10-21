package com.cedarsoftware.util.io.factory;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonReader.ClassFactory;
import com.cedarsoftware.util.io.TestCustomWriter;

public class PersonFactory implements ClassFactory {
    @Override
    public Object newInstance(Class c, Object o, JsonReader reader) {
        return new TestCustomWriter.Person();
    }
}
