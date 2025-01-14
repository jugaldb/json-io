package com.cedarsoftware.util.io.factory;

import java.util.Locale;

import com.cedarsoftware.util.io.JsonIoException;
import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.ReaderContext;

/**
 * @author John DeRegnaucourt (jdereg@gmail.com)
 *         <br>
 *         Copyright (c) Cedar Software LLC
 *         <br><br>
 *         Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *         <br><br>
 *         <a href="http://www.apache.org/licenses/LICENSE-2.0">License</a>
 *         <br><br>
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.*
 */
public class LocaleFactory implements JsonReader.ClassFactory {
    public Object newInstance(Class<?> c, JsonObject jObj, ReaderContext context) {
        Object language = jObj.get("language");
        if (language == null) {
            throw new JsonIoException("java.util.Locale must specify 'language' field");
        }
        Object country = jObj.get("country");
        Object variant = jObj.get("variant");
        if (country == null) {
            return new Locale((String) language);
        }
        if (variant == null) {
            return new Locale((String) language, (String) country);
        }

        return new Locale((String) language, (String) country, (String) variant);
    }

    /**
     * @return true.  Strings are always immutable, final.
     */
    public boolean isObjectFinal() {
        return true;
    }
}
