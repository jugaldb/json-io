package com.cedarsoftware.util.io;

import com.cedarsoftware.util.Convention;
import com.cedarsoftware.util.reflect.Accessor;
import com.cedarsoftware.util.reflect.AccessorFactory;
import com.cedarsoftware.util.reflect.ReflectionUtils;
import com.cedarsoftware.util.reflect.factories.GetMethodAccessorFactory;
import com.cedarsoftware.util.reflect.factories.IsMethodAccessorFactory;
import com.cedarsoftware.util.reflect.filters.FieldFilter;
import com.cedarsoftware.util.reflect.filters.MethodFilter;
import com.cedarsoftware.util.reflect.filters.field.EnumFieldFilter;
import com.cedarsoftware.util.reflect.filters.field.StaticFieldFilter;
import com.cedarsoftware.util.reflect.filters.method.AccessorMethodFilter;
import com.cedarsoftware.util.reflect.filters.method.ModifierMethodFilter;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builder class for building the writeOptions.
 *
 * @author John DeRegnaucourt (jdereg@gmail.com)
 * @author Kenny Partlow (kpartlow@gmail.com)
 * <br>
 * Copyright (c) Cedar Software LLC
 * <br><br>
 * Licensed under the Apache License, Version 2.0 (the "License");
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
public class WriteOptionsBuilder {
    // Constants
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";
    public static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private final DefaultWriteOptions options;
    private static final Map<String, String> BASE_ALIAS_MAPPINGS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, JsonWriter.JsonClassWriter> BASE_WRITERS = new ConcurrentHashMap<>();
    private static final Set<Class<?>> BASE_NON_REFS = new HashSet<>();
    private static final Set<String> BASE_FILTERED_METHOD_NAMES = new HashSet<>();
    static final Map<Class<?>, Set<String>> BASE_EXCLUDED_FIELD_NAMES = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, String>> BASE_NONSTANDARD_MAPPINGS = new ConcurrentHashMap<>();

    static {
        BASE_ALIAS_MAPPINGS.putAll(MetaUtils.loadMapDefinition("aliases.txt"));
        BASE_WRITERS.putAll(loadWriters());
        BASE_NON_REFS.addAll(loadNonRefs());
        BASE_FILTERED_METHOD_NAMES.addAll(MetaUtils.loadSetDefinition("excludedAccessorMethods.txt"));
        BASE_EXCLUDED_FIELD_NAMES.putAll(MetaUtils.loadClassToSetOfStrings("ignoredFields.txt"));
        BASE_NONSTANDARD_MAPPINGS.putAll(MetaUtils.loadNonStandardMethodNames("nonStandardAccessors.txt"));
    }

    /**
     * Start with default options
     */
    public WriteOptionsBuilder() {
        this.options = new DefaultWriteOptions();

        this.options.includedFieldNames = new HashMap<>();
        this.options.nonStandardMappings = new HashMap<>();
        this.options.aliasTypeNames = new HashMap<>();
        this.options.excludedFieldNames = new HashMap<>();
        this.options.customWrittenClasses = new HashMap<>();

        this.options.notCustomWrittenClasses = new HashSet<>();
        this.options.nonRefClasses = new HashSet<>();
        this.options.filteredMethodNames = new HashSet<>();

        this.options.fieldFilters = new ArrayList<>();
        this.options.fieldFilters.add(new StaticFieldFilter());
        this.options.fieldFilters.add(new EnumFieldFilter());

        this.options.methodFilters = new ArrayList<>();
        this.options.methodFilters.add(new AccessorMethodFilter());
        this.options.methodFilters.add(new ModifierMethodFilter(Modifier.PUBLIC));

        this.options.accessorFactories = new ArrayList<>();
        this.options.accessorFactories.add(new GetMethodAccessorFactory());
        this.options.accessorFactories.add(new IsMethodAccessorFactory());

        // Start with all BASE_ALIAS_MAPPINGS (more aliases can be added to this instance, and more aliases
        // can be added to the BASE_ALIAS_MAPPINGS via the static method, so that all instances get them.)
        options.nonStandardMappings.putAll(BASE_NONSTANDARD_MAPPINGS);
        options.aliasTypeNames.putAll(BASE_ALIAS_MAPPINGS);
        options.customWrittenClasses.putAll(BASE_WRITERS);
        options.nonRefClasses.addAll(BASE_NON_REFS);
        options.filteredMethodNames.addAll(BASE_FILTERED_METHOD_NAMES);
        options.excludedFieldNames.putAll(BASE_EXCLUDED_FIELD_NAMES);
    }

    /**
     * Call this method to add a permanent (JVM lifetime) alias of a class to an often shorter, name.
     *
     * @param clazz Class that will be aliased by a shorter name in the JSON.
     * @param alias Shorter alias name, for example, "ArrayList" as opposed to "java.util.ArrayList"
     */
    public static void addPermanentAlias(Class<?> clazz, String alias) {
        BASE_ALIAS_MAPPINGS.put(clazz.getName(), alias);
    }

    /**
     * Call this method to add a permanent (JVM lifetime) alias of a class to an often shorter, name.
     *
     * @param clazz Class that will be aliased from fullyQualifiedName -> simpleName
     */
    public static void addPermanentAlias(Class<?> clazz) {
        BASE_ALIAS_MAPPINGS.put(clazz.getName(), clazz.getSimpleName());
    }

    /**
     * Call this method to add a permanent (JVM lifetime) class that should not be treated as referencable
     * when being written out to JSON.  This means it will never have an @id nor @ref.  This feature is
     * useful for small, immutable classes.
     *
     * @param clazz Class that will no longer be treated as referenceable when being written to JSON.
     */
    public static void addPermanentNonRef(Class<?> clazz) {
        BASE_NON_REFS.add(clazz);
    }

    /**
     * Call this method to add a custom JSON writer to json-io.  It will
     * associate the Class 'c' to the writer you pass in.  The writers are
     * found with isAssignableFrom().  If this is too broad, causing too
     * many classes to be associated to the custom writer, you can indicate
     * that json-io should not use a custom write for a particular class,
     * by calling the addNotCustomWrittenClass() method.  This method will add
     * the custom writer such that it will be there permanently, for the
     * life of the JVM (static).
     *
     * @param clazz  Class to assign a custom JSON writer to
     * @param writer The JsonClassWriter which will write the custom JSON format of class.
     */
    public static void addPermanentWriter(Class<?> clazz, JsonWriter.JsonClassWriter writer) {
        BASE_WRITERS.put(clazz, writer);
    }

    /**
     * @param loader ClassLoader to use when writing JSON to resolve String named classes.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder classLoader(ClassLoader loader) {
        this.options.classLoader = loader;
        return this;
    }

    /**
     * @param shortMetaKeys boolean true to turn on short meta-keys, false for long.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder shortMetaKeys(boolean shortMetaKeys) {
        this.options.shortMetaKeys = shortMetaKeys;
        return this;
    }

    /**
     * @param aliases Map containing String class names to alias names.  The passed in Map will
     *                       be copied, and be the new baseline settings.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder aliasTypeNames(Map<String, String> aliases) {
        aliases.forEach(this::addUniqueAlias);
        return this;
    }

    /**
     * Aliases the fully qualified class name to its short name
     *
     * @param type Class to alias
     * @return ReadOptions for chained access.
     */
    public WriteOptionsBuilder aliasTypeName(Class<?> type) {
        return aliasTypeName(type.getName(), type.getSimpleName());
    }


    /**
     * @param type  Class to alias
     * @param alias String shorter name to use, typically.
     * @return ReadOptions for chained access.
     */
    public WriteOptionsBuilder aliasTypeName(Class<?> type, String alias) {
        this.options.aliasTypeNames.put(type.getName(), alias);
        return this;
    }

    /**
     * @param typeName String class name
     * @param alias    String shorter name to use, typically.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder aliasTypeName(String typeName, String alias) {
        addUniqueAlias(typeName, alias);
        return this;
    }

    /**
     * Since we are swapping keys/values, we must check for duplicate values (which are now keys).
     * @param typeName String fully qualified class name.
     * @param alias String shorter alias name.
     */
    private void addUniqueAlias(String typeName, String alias) {
        Convention.throwIfClassNotFound(typeName, this.options.classLoader);
        Convention.throwIfKeyExists(this.options.aliasTypeNames, typeName, "Tried to create @type alias" + typeName + " -> " + alias + ", but it is already aliased to: " + this.options.aliasTypeNames.get(typeName));

        this.options.aliasTypeNames.put(typeName, alias);
    }

    /**
     * @return boolean true if set to always show type (@type)
     */
    public WriteOptionsBuilder withExtendedAliases() {
        Map<String, String> extendedAliases = MetaUtils.loadMapDefinition("extendedAliases.txt");
        extendedAliases.forEach((key, value) -> this.options.aliasTypeNames.putIfAbsent(key, value));
        return this;
    }

    /**
     * Set to always show type
     *
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder showTypeInfoAlways() {
        this.options.showTypeInfo = WriteOptions.ShowType.ALWAYS;
        return this;
    }

    /**
     * Set to never show type
     *
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder showTypeInfoNever() {
        this.options.showTypeInfo = WriteOptions.ShowType.NEVER;
        return this;
    }

    /**
     * Set to show minimal type.  This means that when the type of an object can be inferred, a type field will not
     * be output.  A Field that points to an instance of the same time, or a typed [] of objects don't need the type
     * info.  However, an Object[], a Collection with no generics, the reader will need to know what type the JSON
     * object is, in order to instantiate the write Java class to which the information will be copied.
     *
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder showTypeInfoMinimal() {
        this.options.showTypeInfo = WriteOptions.ShowType.MINIMAL;
        return this;
    }

    /**
     * @param prettyPrint boolean 'prettyPrint' setting, true to turn on, false will turn off.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder prettyPrint(boolean prettyPrint) {
        this.options.prettyPrint = prettyPrint;
        return this;
    }

    /**
     * @param writeLongsAsStrings boolean true to turn on writing longs as Strings, false to write them as
     *                            native JSON longs.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder writeLongsAsStrings(boolean writeLongsAsStrings) {
        this.options.writeLongsAsStrings = writeLongsAsStrings;
        return this;
    }

    /**
     * @param skipNullFields boolean setting, where true indicates fields with null values will not be written
     *                       to the JSON, false will allow the field to still be written.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder skipNullFields(boolean skipNullFields) {
        this.options.skipNullFields = skipNullFields;
        return this;
    }

    /**
     * @param forceMapOutputAsTwoArrays boolean 'forceMapOutputAsTwoArrays' setting.  true will force Java Maps to be
     *                                  written out as two parallel arrays, once for keys, one array for values.
     *                                  false will write out one JSON { } object, if all keys are Strings.  If not,
     *                                  then the Map will be output as two parallel arrays (@keys:[...], @values:[...]).
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder forceMapOutputAsTwoArrays(boolean forceMapOutputAsTwoArrays) {
        this.options.forceMapOutputAsTwoArrays = forceMapOutputAsTwoArrays;
        return this;
    }

    /**
     * @param allowNanAndInfinity boolean 'allowNanAndInfinity' setting.  true will allow
     *                            Double and Floats to be output as NAN and INFINITY, false
     *                            and these values will come across as null.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder allowNanAndInfinity(boolean allowNanAndInfinity) {
        this.options.allowNanAndInfinity = allowNanAndInfinity;
        return this;
    }

    /**
     * Option to write out enums as a String, it will write out the enum.name() field.
     * This is the default way enums will be written out.
     *
     * @return WriteOptionsBuilder for chained access.per
     */
    public WriteOptionsBuilder writeEnumsAsString() {
        this.options.enumWriter = new Writers.EnumsAsStringWriter();
        return this;
    }

    /**
     * Option to write out all the member fields of an enum.  You can also filter the
     * field to write out only the public fields on the enum.
     *
     * @param writePublicFieldsOnly boolean, only write out the public fields when writing enums as objects
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder writeEnumAsJsonObject(boolean writePublicFieldsOnly) {
        this.options.enumWriter = DefaultWriteOptions.nullWriter;
        this.options.enumPublicFieldsOnly = writePublicFieldsOnly;
        return this;
    }

    /**
     * @param closeStream boolean set to 'true' to have JsonIo close the OutputStream when it is finished writinging
     *                    to it.  The default is 'true'.  If false, the OutputStream will not be closed, allowing
     *                    you to continue writing further.  Example, NDJSON that has new line eliminated JSON
     *                    objects repeated.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder closeStream(boolean closeStream) {
        this.options.closeStream = closeStream;
        return this;
    }

    /**
     * @param customWrittenClasses Map of Class to JsonWriter.JsonClassWriter.  Establish the passed in Map as the
     *                             established Map of custom writers to be used when writing JSON. Using this method
     *                             more than once, will set the custom writers to only the values from the Set in
     *                             the last call made.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder setCustomWrittenClasses(Map<Class<?>, JsonWriter.JsonClassWriter> customWrittenClasses) {
        this.options.customWrittenClasses.clear();
        addCustomWrittenClasses(customWrittenClasses);
        return this;
    }

    /**
     * @param customWrittenClasses Map of Class to JsonWriter.JsonClassWriter.  Adds all custom writers into the custom
     *                             writers map.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder addCustomWrittenClasses(Map<Class<?>, JsonWriter.JsonClassWriter> customWrittenClasses) {
        this.options.customWrittenClasses.putAll(customWrittenClasses);
        return this;
    }

    /**
     * @param clazz        Class to add a custom writer for.
     * @param customWriter JsonClassWriter to use when the passed in Class is encountered during serialization.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder addCustomWrittenClass(Class<?> clazz, JsonWriter.JsonClassWriter customWriter) {
        this.options.customWrittenClasses.put(clazz, customWriter);
        return this;
    }

    /**
     * Add a class to the not-customized list - the list of classes that you do not want to be picked up by a
     * custom writer (that could happen through inheritance).
     *
     * @param notCustomClass Class to add to the not-customized list.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder addNotCustomWrittenClass(Class<?> notCustomClass) {
        this.options.notCustomWrittenClasses.add(notCustomClass);
        return this;
    }

    /**
     * @param notCustomClasses initialize the list of classes on the non-customized list.  All prior associations
     *                         will be dropped and this Collection will establish the new list.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder setNotCustomWrittenClasses(Collection<Class<?>> notCustomClasses) {
        this.options.notCustomWrittenClasses.clear();
        this.options.notCustomWrittenClasses.addAll(notCustomClasses);
        return this;
    }

    /**
     * @param clazz         Class to add a single field to be included in the written JSON.
     * @param includedFieldName String name of field to include in written JSON.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder addIncludedField(Class<?> clazz, String includedFieldName) {
        Convention.throwIfNull(includedFieldName, "includedFieldName cannot be null");
        this.options.includedFieldNames.computeIfAbsent(clazz, k -> new LinkedHashSet<>()).add(includedFieldName);
        return this;
    }

    /**
     * @param clazz          Class to add a Collection of fields to be included in written JSON.
     * @param includedFieldNames Collection of String name of fields to include in written JSON.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder addIncludedFields(Class<?> clazz, Collection<String> includedFieldNames) {
        this.options.includedFieldNames.computeIfAbsent(clazz, k -> new LinkedHashSet<>()).addAll(includedFieldNames);
        return this;
    }

    /**
     * @param includedFieldNames Map of Class's mapped to Collection of String field names to include in the written JSON.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder addIncludedFields(Map<Class<?>, Collection<String>> includedFieldNames) {
        includedFieldNames.forEach(this::addIncludedFields);
        return this;
    }

    /**
     * @param clazz         Class to add a single field to be excluded.
     * @param excludedFieldName String name of field to exclude from written JSON.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder addExcludedField(Class<?> clazz, String excludedFieldName) {
        this.options.excludedFieldNames.computeIfAbsent(clazz, k -> new LinkedHashSet<>()).add(excludedFieldName);
        return this;
    }

    /**
     * @param clazz          Class to add a Collection of fields to be excluded in written JSON.
     * @param excludedFields Collection of String name of fields to exclude in written JSON.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder addExcludedFields(Class<?> clazz, Collection<String> excludedFields) {
        this.options.excludedFieldNames.computeIfAbsent(clazz, k -> new LinkedHashSet<>()).addAll(excludedFields);
        return this;
    }

    /**
     * @param excludedFieldNames Map of Class's mapped to Collection of String field names to exclude from written JSON.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder addExcludedFields(Map<Class<?>, Collection<String>> excludedFieldNames) {
        excludedFieldNames.forEach(this::addExcludedFields);
        return this;
    }

    /**
     * Change the date-time format to the ISO date format: "yyyy-MM-dd".  This is for java.util.Data and
     * java.sql.Date.
     *
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder isoDateFormat() {
        return dateTimeFormat(ISO_DATE_FORMAT);
    }

    /**
     * Change the date-time format to the ISO date-time format: "yyyy-MM-dd'T'HH:mm:ss" (default).  This is
     * for java.util.Date and java.sql.Date.
     *
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder isoDateTimeFormat() {
        return dateTimeFormat(ISO_DATE_TIME_FORMAT);
    }

    /**
     * Change the java.uti.Date and java.sql.Date format output to a "long," the number of seconds since Jan 1, 1970
     * at midnight. Useful if you do not need to see the JSON, and want to keep the format smaller.  This is for
     * java.util.Date and java.sql.Date.
     *
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder longDateFormat() {
        addCustomWrittenClass(Date.class, new Writers.DateAsLongWriter());
        return this;
    }

    /**
     * Change the date-time format to the passed in format.  The format pattens can be found in the Java Doc
     * for the java.time.format.DateTimeFormatter class.  There are many constants you can use, as well as
     * the definition of how to construct your own patterns.  This is for java.util.Date and java.sql.Date.
     *
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder dateTimeFormat(String format) {
        addCustomWrittenClass(Date.class, new Writers.DateWriter(format));
        return this;
    }

    /**
     * @param methodNames Replaces the collection of methodNames that are not to be considered as accessors.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder setFilteredMethodNames(Collection<String> methodNames) {
        Convention.throwIfNull(methodNames, "methodNames cannot be null");
        this.options.filteredMethodNames.clear();
        this.options.filteredMethodNames.addAll(methodNames);
        return this;
    }

    /**
     * @param methodNames Adds to the collection of methodNames that are not to be considered as accessors.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder addFilteredMethodNames(Collection<String> methodNames) {
        Convention.throwIfNull(methodNames, "methodNames cannot be null");
        this.options.filteredMethodNames.addAll(methodNames);
        return this;
    }

    /**
     * @param methodName Adds to the collection of methodNames that are not to be considered as accessors.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder addFilteredMethodName(String methodName) {
        Convention.throwIfNull(methodName, "methodName cannot be null");
        this.options.filteredMethodNames.add(methodName);
        return this;
    }

    /**
     * @param nonStandardMappings Replaces the collection of methodNames that are not to be considered as accessors.
     * @return WriteOptionsBuilder for chained access.
     */
    WriteOptionsBuilder setNonStandardMappings(Map<Class<?>, Map<String, String>> nonStandardMappings) {
        Convention.throwIfNull(nonStandardMappings, "nonStandardMappings cannot be null");

        this.options.nonStandardMappings.clear();
        this.options.nonStandardMappings.putAll(nonStandardMappings);
        return this;
    }

    /**
     * @param nonStandardMappings Adds to the collection of methodNames that are not to be considered as accessors.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder addNonStandardMappings(Map<Class<?>, Map<String, String>> nonStandardMappings) {
        Convention.throwIfNull(nonStandardMappings, "nonStandardMappings cannot be null");
        this.options.nonStandardMappings.putAll(nonStandardMappings);
        return this;
    }

    /**
     * @param methodName Adds to the collection of methodNames that are not to be considered as accessors.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder addNonStandardMapping(Class<?> c, String fieldName, String methodName) {
        Convention.throwIfNull(c, "class cannot be null");
        Convention.throwIfNull(fieldName, "fieldName cannot be null");
        Convention.throwIfNull(methodName, "methodName cannot be null");
        this.options.nonStandardMappings.computeIfAbsent(c, cls -> new LinkedHashMap<>()).put(fieldName, methodName);
        return this;
    }

    /**
     * @param classes Replaces the collection of classes to treat as non-referenceable with the given class.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder setNonReferenceableClasses(Collection<Class<?>> classes) {
        Convention.throwIfNull(classes, "classes cannot be null");
        this.options.nonRefClasses.clear();
        this.options.nonRefClasses.addAll(classes);
        return this;
    }

    /**
     * @param classes classes to add to be considered a non-referenceable object.  Just like an "int" for example, any
     *                class added here will never use an @id/@ref pair.  The downside, is that when read,
     *                each instance, even if the same as another, will use memory.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder addNonReferenceableClasses(Collection<Class<?>> classes) {
        Convention.throwIfNull(classes, "classes cannot be null");
        this.options.nonRefClasses.addAll(classes);
        return this;
    }

    /**
     * @param clazz class to add to be considered a non-referenceable object.  Just like an "int" for example, any
     *              class added here will never use an @id/@ref pair.  The downside, is that when read,
     *              each instance, even if the same as another, will use memory.
     * @return WriteOptionsBuilder for chained access.
     */
    public WriteOptionsBuilder addNonReferenceableClass(Class<?> clazz) {
        Convention.throwIfNull(clazz, "clazz cannot be null");
        this.options.nonRefClasses.add(clazz);
        return this;
    }

    /**
     * Seal the instance of this class so that no more changes can be made to it.
     *
     * @return WriteOptionsBuilder for chained access.
     */
    @SuppressWarnings("unchecked")
    public WriteOptions build() {
        this.options.clearCaches();
        return this.options;
    }

    /**
     * Load custom writer classes based on contents of resources/customWriters.txt.
     * Verify that classes listed are indeed valid classes loaded in the JVM.
     *
     * @return Map<Class < ?>, JsonWriter.JsonClassWriter> containing the resolved Class -> JsonClassWriter instance.
     */
    private static Map<Class<?>, JsonWriter.JsonClassWriter> loadWriters() {
        Map<String, String> map = MetaUtils.loadMapDefinition("customWriters.txt");
        Map<Class<?>, JsonWriter.JsonClassWriter> writers = new HashMap<>();
        ClassLoader classLoader = WriteOptions.class.getClassLoader();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            String className = entry.getKey();
            String writerClassName = entry.getValue();
            Class<?> clazz = MetaUtils.classForName(className, classLoader);
            if (clazz == null) {
                System.out.println("Class: " + className + " not defined in the JVM, so custom writer: " + writerClassName + ", will not be used.");
                continue;
            }
            Class<JsonWriter.JsonClassWriter> customWriter = (Class<JsonWriter.JsonClassWriter>) MetaUtils.classForName(writerClassName, classLoader);
            if (customWriter == null) {
                throw new JsonIoException("Note: class not found (custom JsonClassWriter class): " + writerClassName + ", listed in resources/customWriters.txt as a custom writer for: " + className);
            }
            try {
                JsonWriter.JsonClassWriter writer = customWriter.newInstance();
                writers.put(clazz, writer);
            } catch (Exception e) {
                throw new JsonIoException("Note: class failed to instantiate (a custom JsonClassWriter class): " + writerClassName + ", listed in resources/customWriters.txt as a custom writer for: " + className);
            }
        }
        return writers;
    }

    /**
     * Load custom writer classes based on contents of resources/customWriters.txt.
     * Verify that classes listed are indeed valid classes loaded in the JVM.
     *
     * @return Map<Class<?>, JsonWriter.JsonClassWriter> containing the resolved Class -> JsonClassWriter instance.
     */
    private static Map<Class<?>, Map<String, String>> loadNonStandardMethodNames() {
        Map<String, String> map = MetaUtils.loadMapDefinition("nonStandardAccessors.txt");
        Map<Class<?>, Map<String, String>> nonStandardMapping = new ConcurrentHashMap<>();
        ClassLoader classLoader = WriteOptions.class.getClassLoader();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            String className = entry.getKey();
            String mappings = entry.getValue();
            Class<?> clazz = MetaUtils.classForName(className, classLoader);
            if (clazz == null) {
                System.out.println("Class: " + className + " not defined in the JVM");
                continue;
            }

            Map<String, String> mapping = nonStandardMapping.computeIfAbsent(clazz, c -> new ConcurrentHashMap<>());
            for (String split : mappings.split(",")) {
                String[] parts = split.split(":");
                mapping.put(parts[0].trim(), parts[1].trim());
            }
        }
        return nonStandardMapping;
    }


    /**
     * Load the list of classes that are intended to be treated as non-referenceable, immutable classes.
     *
     * @return Set<Class < ?>> which is the loaded from resource/nonRefs.txt and verified to exist in JVM.
     */
    static Set<Class<?>> loadNonRefs() {
        Set<Class<?>> nonRefs = new LinkedHashSet<>();
        Set<String> set = MetaUtils.loadSetDefinition("nonRefs.txt");
        set.forEach((className) -> {
            Class<?> clazz = MetaUtils.classForName(className, WriteOptions.class.getClassLoader());
            if (clazz == null) {
                throw new JsonIoException("Class: " + className + " undefined.  Cannot be used as non-referenceable class, listed in resources/nonRefs.txt");
            }
            nonRefs.add(clazz);
        });
        return nonRefs;
    }


    public boolean addFilter(FieldFilter filter) {
        return this.options.fieldFilters.add(filter);
    }

    public boolean removeFilter(FieldFilter filter) {
        return this.options.fieldFilters.remove(filter);
    }


    public static class DefaultWriteOptions implements WriteOptions {
        // Properties

        /**
         * @return boolean true if showing short meta-keys (@i instead of @id, @ instead of @ref, @t
         * instead of @type, @k instead of @keys, @v instead of @values), false for full size. 'false' is the default.
         */
        @Getter
        private boolean shortMetaKeys = false;

        private ShowType showTypeInfo = WriteOptions.ShowType.MINIMAL;

        /**
         * @return boolean 'prettyPrint' setting, true being yes, pretty-print mode using lots of vertical
         * white-space and indentations, 'false' will output JSON in one line.  The default is false.
         */
        @Getter
        private boolean prettyPrint = false;

        /**
         * @return boolean 'writeLongsAsStrings' setting, true indicating longs will be written as Strings,
         * false to write them out as native JSON longs.  Writing Strings as Longs to the JSON, will fix errors
         * in Javascript when an 18-19 digit long value is sent to Javascript.  This is because Javascript stores
         * them in Doubles, which cannot handle the precision of an 18-19 digit long, but a String will retain
         * the full value into Javascript.  The default is false.
         */
        @Getter
        private boolean writeLongsAsStrings = false;

        /**
         * @return boolean skipNullFields setting, true indicates fields with null values will not be written,
         * false will still output the field with an associated null value.  false is the default.
         */
        @Getter
        private boolean skipNullFields = false;
        /**
         * @return boolean 'forceMapOutputAsTwoArrays' setting.  true indicates that two arrays will be written to
         * represent a Java Map, one for keys, one for values.  false indicates one Java object will be used, if
         * all the keys of the Map are Strings.  If not, then the Map will be written out with a key array, and a
         * parallel value array. (@keys:[...], @values:[...]).  false is the default.
         */
        @Getter
        private boolean forceMapOutputAsTwoArrays = false;
        /**
         * @return boolean will return true if NAN and Infinity are allowed to be written out for
         * Doubles and Floats, else null will be written out..
         */
        @Getter
        private boolean allowNanAndInfinity = false;

        /**
         * true indicates that only public fields will be output on an Enum.  Enums don't often have fields added to them
         * but if so, then only the public fields will be written.  The Enum will be written out in JSON object { } format.
         * If there are not added fields to an Enum, it will be written out as a single line value.  The default value
         * is true.  If you set this to false, it will change the 'enumFieldsAsObject' to true - because you will be
         * showing potentially more than one value, it will require the enum to be written as an object.
         */
        @Getter
        private boolean enumPublicFieldsOnly = false;

        /**
         * @return boolean 'true' if the OutputStream should be closed when the reading is finished.  The default is 'true.'
         */
        @Getter
        private boolean closeStream = true;
        private JsonWriter.JsonClassWriter enumWriter = new Writers.EnumsAsStringWriter();

        /**
         * @return ClassLoader to be used when writing JSON to resolve String named classes.
         */
        @Getter
        private ClassLoader classLoader = WriteOptions.class.getClassLoader();
        private Map<Class<?>, Set<String>> includedFieldNames;
        private Map<Class<?>, Map<String, String>> nonStandardMappings;
        private Map<String, String> aliasTypeNames;
        private Set<Class<?>> notCustomWrittenClasses;
        private Set<Class<?>> nonRefClasses;
        private Map<Class<?>, Set<String>> excludedFieldNames;
        private List<FieldFilter> fieldFilters;
        private List<MethodFilter> methodFilters;
        private List<AccessorFactory> accessorFactories;
        private Set<String> filteredMethodNames;
        private Map<Class<?>, JsonWriter.JsonClassWriter> customWrittenClasses;

        // Runtime caches (not feature options), since looking up writers can be expensive
        // when one does not exist, we cache the write or a nullWriter if one does not exist.
        private Map<Class<?>, JsonWriter.JsonClassWriter> writerCache = new ConcurrentHashMap<>(200, 0.8f, Runtime.getRuntime().availableProcessors());

        // Creating the Accessors (methodHandles) is expensive so cache the list of Accessors per Class
        private final Map<Class<?>, List<Accessor>> accessorsCache = new ConcurrentHashMap<>(200, 0.8f, Runtime.getRuntime().availableProcessors());

        private final Map<Class<?>, Map<String, Field>> classMetaCache = new ConcurrentHashMap(200, 0.8f, Runtime.getRuntime().availableProcessors());

        /**
         * Default Constructor.  Prevent instantiation outside of package.
         */
        private DefaultWriteOptions() {
        }

        /**
         * Alias Type Names, e.g. "ArrayList" instead of "java.util.ArrayList".
         *
         * @param typeName String name of type to fetch alias for.  There are no default aliases.
         * @return String alias name or null if type name is not aliased.
         */
        public String getTypeNameAlias(String typeName) {
            String alias = aliasTypeNames.get(typeName);
            return alias == null ? typeName : alias;
        }

        /**
         * @return boolean true if set to always show type (@type)
         */
        public boolean isAlwaysShowingType() {
            return showTypeInfo == ShowType.ALWAYS;
        }

        /**
         * @return boolean true if set to never show type (no @type)
         */
        public boolean isNeverShowingType() {
            return showTypeInfo == ShowType.NEVER;
        }

        /**
         * @return boolean true if set to show minimal type (@type)
         */
        public boolean isMinimalShowingType() {
            return showTypeInfo == ShowType.MINIMAL;
        }

        /**
         * @param clazz Class to check to see if there is a custom writer associated to it.
         * @return boolean true if there is an associated custom writer class associated to the passed in class,
         * false otherwise.
         */
        public boolean isCustomWrittenClass(Class<?> clazz) {
            return customWrittenClasses.containsKey(clazz);
        }

        /**
         * @param clazz Class to see if it is on the not-customized list.  Classes are added to this list when
         *              a class is being picked up through inheritance, and you don't want it to have a custom
         *              writer associated to it.
         * @return boolean true if the passed in class is on the not-customized list, false otherwise.
         */
        public boolean isNotCustomWrittenClass(Class<?> clazz) {
            return notCustomWrittenClasses.contains(clazz);
        }

        public List<Accessor> getAccessorsForClass(final Class<?> c) {
            return accessorsCache.computeIfAbsent(c, this::buildDeepAccessors);
        }

        /**
         * @return boolean true if java.util.Date and java.sql.Date's are being written in long (numeric) format.
         */
        public boolean isLongDateFormat() {
            Object a = customWrittenClasses.get(Date.class);
            return a instanceof Writers.DateAsLongWriter;
        }

        /**
         * @param clazz Class to check to see if it is non-referenceable.  Non-referenceable classes will always create
         *              a new instance when read in and never use @id/@ref. This uses more memory when the JSON is read in,
         *              as there will be a separate instance in memory for each occurrence. There are certain classes that
         *              json-io automatically treats as non-referenceable, like Strings, Enums, Class, and any Number
         *              instance (BigDecimal, AtomicLong, etc.)  You can add to this list. Often, non-referenceable classes
         *              are useful for classes that can be defined in one line as a JSON, like a LocalDateTime, for example.
         * @return boolean true if the passed in class is considered a non-referenceable class.
         */
        public boolean isNonReferenceableClass(Class<?> clazz) {
            return nonRefClasses.contains(clazz) ||     // Covers primitives, primitive wrappers, Atomic*, Big*, String
                    Number.class.isAssignableFrom(clazz) ||
                    Date.class.isAssignableFrom(clazz) ||
                    clazz.isEnum();
        }

        /**
         * Dummy place-holder class exists only because ConcurrentHashMap cannot contain a
         * null value.  Instead, singleton instance of this class is placed where null values
         * are needed.
         */
        private static final class NullClass implements JsonWriter.JsonClassWriter {
        }

        static final NullClass nullWriter = new NullClass();

        /**
         * Fetch the custom writer for the passed in Class.  If it is cached (already associated to the
         * passed in Class), return the same instance, otherwise, make a call to get the custom writer
         * and store that result.
         *
         * @param c Class of object for which fetch a custom writer
         * @return JsonClassWriter for the custom class (if one exists), null otherwise.
         */
        public JsonWriter.JsonClassWriter getCustomWriter(Class<?> c) {
            JsonWriter.JsonClassWriter writer = writerCache.computeIfAbsent(c, this::findCustomWriter);
            return writer == nullWriter ? null : writer;
        }

        public JsonWriter.JsonClassWriter findCustomWriter(Class<?> c) {
            JsonWriter.JsonClassWriter writer = MetaUtils.findClosest(c, customWrittenClasses, nullWriter);
            return writer != nullWriter ? writer : MetaUtils.getClassIfEnum(c).isPresent() ? enumWriter : nullWriter;
        }

        ///// ACCESSOR PULL IN ???????

        public void clearCaches() {
            accessorsCache.clear();
        }

        private List<Accessor> buildDeepAccessors(final Class<?> c) {

            final Map<String, Field> fields = getDeepDeclaredFields(c);

            final List<Accessor> accessors = new ArrayList<>(fields.size());
            for (final Map.Entry<String, Field> entry : fields.entrySet()) {

                final Field field = entry.getValue();
                final String key = entry.getKey();

                Accessor accessor = this.findAccessor(field, key);

                if (accessor == null) {
                    accessor = Accessor.create(field, key);
                }

                if (accessor != null) {
                    accessors.add(accessor);
                }
            }

            return Collections.unmodifiableList(accessors);
        }

        /**
         * Gets the declared fields for the full class hierarchy of a given class
         *
         * @param c - given class.
         * @return Map - map of string fieldName to Field Object.  This will have the
         * deep list of fields for a given class.
         */
        public Map<String, Field> getDeepDeclaredFields(final Class<?> c) {
            final Set<String> inclusions = includedFieldNames.get(c);

            return (inclusions == null) ?
                    classMetaCache.computeIfAbsent(c, this::buildExclusiveFields) :
                    classMetaCache.computeIfAbsent(c, cls -> buildInclusiveFields(cls, inclusions));
        }

        private Accessor findAccessor(Field field, String key) {
            for (final AccessorFactory factory : this.accessorFactories) {
                try {
                    final Accessor accessor = factory.createAccessor(field, this.nonStandardMappings, key);

                    if (accessor != null) {
                        return accessor;
                    }
                } catch (Exception ignore) {
                    // Handle the exception if needed
                }
            }
            return null;
        }

        private Map<String, Field> buildInclusiveFields(Class<?> c, final Set<String> inclusions) {
            Convention.throwIfNull(c, "class cannot be null");

            final Map<String, Field> map = new LinkedHashMap<>();

            Class<?> curr = c;
            while (curr != null) {
                List<Field> fields = ReflectionUtils.getDeclaredFields(curr);

                for (Field field : fields) {

                    String name = field.getName();

                    if (map.containsKey(name)) {
                        name = field.getDeclaringClass().getSimpleName() + '.' + name;
                    }


                    if (inclusions.contains(name) && !fieldIsFiltered(field)) {
                        map.put(name, field);
                    }

                }

                curr = curr.getSuperclass();
            }

            return Collections.unmodifiableMap(map);
        }

        private Map<String, Field> buildExclusiveFields(final Class<?> c) {

            Convention.throwIfNull(c, "class cannot be null");

            final Map<String, Field> map = new LinkedHashMap<>();
            final Set<String> exclusions = new HashSet<>();

            Class<?> curr = c;
            while (curr != null) {

                final List<Field> fields = ReflectionUtils.getDeclaredFields(curr);

                final Set<String> excludedForClass = this.excludedFieldNames.get(curr);

                if (excludedForClass != null) {
                    exclusions.addAll(excludedForClass);
                }

                for (Field field : fields) {

                    if (Modifier.isTransient(field.getModifiers()) ||
                            exclusions.contains(field.getName()) ||
                            fieldIsFiltered(field)) {
                        continue;
                    }

                    String name = field.getName();

                    if (map.putIfAbsent(name, field) != null) {
                        map.put(field.getDeclaringClass().getSimpleName() + '.' + name, field);
                    }
                }

                curr = curr.getSuperclass();
            }

            return Collections.unmodifiableMap(map);
        }

        private boolean fieldIsFiltered(Field field) {
            for (FieldFilter filter : this.fieldFilters) {
                if (filter.filter(field)) {
                    return true;
                }
            }

            return false;
        }
    }
}




