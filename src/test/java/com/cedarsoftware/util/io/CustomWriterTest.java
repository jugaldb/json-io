package com.cedarsoftware.util.io;

import com.cedarsoftware.util.DeepEquals;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author John DeRegnaucourt (jdereg@gmail.com)
 * <br>
 * Copyright (c) Cedar Software LLC
 * <br><br>
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <br><br>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <br><br>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class CustomWriterTest
{
    public static Person createTestPerson()
    {
        Person p = new Person();
        p.setFirstName("Michael");
        p.setLastName("Bolton");
        p.getPets().add(createPet("Eddie", "Terrier", 6));
        p.getPets().add(createPet("Bella", "Chi hua hua", 3));
        return p;
    }

    public static Pet createPet(String name, String type, int age)
    {
        Pet pet = new Pet();
        pet.setName(name);
        pet.setType(type);
        pet.setAge(age);
        return pet;
    }

    @Test
    public void testCustomWriter()
    {
        Person p = createTestPerson();
        
        Map<Class<Person>, CustomPersonWriter> customWriters = new HashMap<>();
        customWriters.put(Person.class, new CustomPersonWriter());

        Map<Class<Person>, CustomPersonReader> customReaders = new HashMap<>();
        customReaders.put(Person.class, new CustomPersonReader());

        Map<String, Object> writeOptions0 = new WriteOptionsBuilder().withCustomWriterMap(customWriters).build();
        Map<String, Object> readOptions0 = new ReadOptionsBuilder().returnAsMaps().withCustomReaders(customReaders).withNonCustomizableClasses(new ArrayList<>()).build();
        String jsonCustom = TestUtil.toJson(p, writeOptions0);
        Map obj = TestUtil.toJava(jsonCustom, readOptions0);
        assert "Michael".equals(obj.get("f"));
        assert "Bolton".equals(obj.get("l"));
        Object[] pets = (Object[]) obj.get("p");
        assert 2 == pets.length;
        Map ed = (Map) pets[0];
        assert "Eddie".equals(ed.get("n"));
        assert "Terrier".equals(ed.get("t"));
        assert 6L == (long)ed.get("a");

        Map bella = (Map) pets[1];
        assert "Bella".equals(bella.get("n"));
        assert "Chi hua hua".equals(bella.get("t"));
        assert 3L == (long) bella.get("a");

        Map<String, Object> readOptions = new ReadOptionsBuilder().withCustomReader(Person.class, new CustomPersonReader()).build();
        Person personCustom = TestUtil.toJava(jsonCustom, readOptions);

        assert personCustom.getFirstName().equals("Michael");
        assert personCustom.getLastName().equals("Bolton");
        List<Pet> petz = personCustom.getPets();
        assert "Eddie".equals(petz.get(0).getName());
        assert "Terrier".equals(petz.get(0).getType());
        assert 6 == petz.get(0).getAge();

        assert "Bella".equals(petz.get(1).getName());
        assert "Chi hua hua".equals(petz.get(1).getType());
        assert 3 == petz.get(1).getAge();

        Map writeOptions = new WriteOptionsBuilder().withCustomWriter(Person.class, new CustomPersonWriter()).withNoCustomizationFor(Person.class).build();
        String jsonOrig = TestUtil.toJson(p, writeOptions);
        assert !jsonCustom.equals(jsonOrig);
        assert jsonCustom.length() < jsonOrig.length();

        writeOptions = new WriteOptionsBuilder().withCustomWriter(Person.class, new CustomPersonWriter()).build();
        String jsonCustom2 = TestUtil.toJson(p, writeOptions);
        String jsonOrig2 = TestUtil.toJson(p);
        assert jsonCustom.equals(jsonCustom2);
        assert jsonOrig.equals(jsonOrig2);

        Map<Class<Person>, CustomPersonReader> customPersonReaderMap = new HashMap<>();
        customPersonReaderMap.put(Person.class, new CustomPersonReader());
        Person personOrig = TestUtil.toJava(jsonOrig, new ReadOptionsBuilder().withCustomReaders(customPersonReaderMap).withNonCustomizableClasses(MetaUtils.listOf(Person.class)).build());
        assert personOrig.equals(personCustom);

        p = TestUtil.toJava(jsonCustom);
        assert null == p.getFirstName();
    }

    @Test
    public void testCustomWriterException()
    {
        Person p = createTestPerson();
        try
        {
            Map<Class<Person>, BadCustomPWriter> badCustomPWriterMap = new HashMap<>();
            badCustomPWriterMap.put(Person.class, new BadCustomPWriter());
            TestUtil.toJson(p, new WriteOptionsBuilder().withCustomWriterMap(badCustomPWriterMap).build());
            fail();
        }
        catch (JsonIoException e)
        {
            assert e.getMessage().toLowerCase().contains("unable to convert");
        }
    }

    @Test
    public void testCustomWriterAddField()
    {
        Person p = createTestPerson();
        Map<Class<Person>, CustomPersonWriterAddField> customPersonWriterAddFieldMap = new HashMap<>();
        customPersonWriterAddFieldMap.put(Person.class, new CustomPersonWriterAddField());
        String jsonCustom = TestUtil.toJson(p, new WriteOptionsBuilder().withCustomWriterMap(customPersonWriterAddFieldMap).build());
        assert jsonCustom.contains("_version\":12");
        assert jsonCustom.contains("Michael");
    }

    @Test
    public void testCustomPersonWriterReaderinCollectionTypes()
    {
        Person p = createTestPerson();

        Map<Class<Person>, CustomPersonWriter> customWriters = new HashMap<>();
        customWriters.put(Person.class, new CustomPersonWriter());

        Map<Class<Person>, CustomPersonReader> customReaders = new HashMap<>();
        customReaders.put(Person.class, new CustomPersonReader());

        Map<String, Object> writeOptions = new HashMap<>();
        Map<String, Object> readOptions = new HashMap<>();

        // Object[] { person, person }  (same instance twice - 2nd instance if simply @ref to 1st)
        // Works - not using custom writer/reader
        Object people = new Object[]{p, p};
        String json = TestUtil.toJson(people, writeOptions);
        Object obj = TestUtil.toJava(json, readOptions);
        assert DeepEquals.deepEquals(people, obj);
        assert ((Object[])people)[0] == ((Object[])people)[1];

        // Failed - (until fixed in ObjectResolver.readWithCustomReaderIfOneExists() near the bottom
        // JsonObject needs to be updated to point to the newly created actual instance
        writeOptions = new WriteOptionsBuilder().withCustomWriterMap(customWriters).build();
        readOptions = new ReadOptionsBuilder().withCustomReaders(customReaders).withNonCustomizableClasses(new ArrayList<>()).build();
        json = TestUtil.toJson(people, writeOptions);
        obj = TestUtil.toJava(json, readOptions);
        assert DeepEquals.deepEquals(people, obj);
        assert ((Object[])people)[0] == ((Object[])people)[1];

        writeOptions = new HashMap<>();
        readOptions = new HashMap<>();

        // List of { person, person }  (same instance twice - 2nd instance if simply @ref to 1st)
        // Works - not using custom writer/reader
        people = new ArrayList<>();
        ((List<Person>)people).add(p);
        ((List<Person>)people).add(p);
        json = TestUtil.toJson(people, writeOptions);
        obj = TestUtil.toJava(json, readOptions);
        assert DeepEquals.deepEquals(people, obj);
        assert ((List)people).get(0) == ((List) people).get(1);

        // Failed - (until fixed in ObjectResolver.readWithCustomReaderIfOneExists() near the bottom
        // JsonObject needs to be updated to point to the newly created actual instance
        writeOptions = new WriteOptionsBuilder().withCustomWriterMap(customWriters).build();
        readOptions = new ReadOptionsBuilder().withCustomReaders(customReaders).withNonCustomizableClasses(new ArrayList<>()).build();
        json = TestUtil.toJson(people, writeOptions);
        obj = TestUtil.toJava(json, readOptions);
        assert DeepEquals.deepEquals(people, obj);
        assert ((List)people).get(0) == ((List) people).get(1);
    }

    @Test
    public void testCustomPersonWriterReaderForCollectionFields()
    {
        Person p = createTestPerson();

        Map<Class<Person>, CustomPersonWriter> customWriters = new HashMap<>();
        customWriters.put(Person.class, new CustomPersonWriter());

        Map<Class<Person>, CustomPersonReader> customReaders = new HashMap<>();
        customReaders.put(Person.class, new CustomPersonReader());

        Map<String, Object> writeOptions = new WriteOptionsBuilder().withCustomWriterMap(customWriters).build();
        Map<String, Object> readOptions = new ReadOptionsBuilder().withCustomReaders(customReaders).withNonCustomizableClasses(new ArrayList<>()).build();
        
        People people = new People(new Object[]{p, p});
        String json = TestUtil.toJson(people, writeOptions);    // Massive @ref JSON
        people = TestUtil.toJava(json, readOptions);
        p = people.listPeeps.get(0);
        assert people.listPeeps.get(1) == p;
        assert people.arrayPeeps[0] == p;
        assert people.arrayPeeps[1] == p;
        assert people.typeArrayPeeps[0] == p;
        assert people.typeArrayPeeps[1] == p;
     }

    public static class Pet
    {
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }

            if (!(o instanceof Pet))
            {
                return false;
            }

            Pet pet = (Pet) o;

            if (age != pet.getAge())
            {
                return false;
            }

            if (!name.equals(pet.getName()))
            {
                return false;
            }

            if (!type.equals(pet.getType()))
            {
                return false;
            }

            return true;
        }

        public int hashCode()
        {
            int result;
            result = age;
            result = 31 * result + type.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }

        public int getAge()
        {
            return age;
        }

        public void setAge(int age)
        {
            this.age = age;
        }

        public String getType()
        {
            return type;
        }

        public void setType(String type)
        {
            this.type = type;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        private int age;
        private String type;
        private String name;
    }

    public static class Person
    {
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }

            if (!(o instanceof Person))
            {
                return false;
            }
            
            Person person = (Person) o;

            if (!firstName.equals(person.getFirstName()))
            {
                return false;
            }

            if (!lastName.equals(person.getLastName()))
            {
                return false;
            }
            
            if (pets.size() != person.getPets().size())
            {
                return false;
            }

            int len = pets.size();
            for (int i = 0; i < len ; i++)
            {
                if (!pets.get(i).equals(person.getPets().get(i)))
                {
                    return false;
                }
            }

            return true;
        }

        public int hashCode()
        {
            int result;
            result = firstName.hashCode();
            result = 31 * result + lastName.hashCode();
            result = 31 * result + pets.hashCode();
            return result;
        }

        public String getFirstName()
        {
            return firstName;
        }

        public void setFirstName(String firstName)
        {
            this.firstName = firstName;
        }

        public String getLastName()
        {
            return lastName;
        }

        public void setLastName(String lastName)
        {
            this.lastName = lastName;
        }

        public List<Pet> getPets()
        {
            return pets;
        }

        public void setPets(List<Pet> pets)
        {
            this.pets = pets;
        }

        private String firstName;
        private String lastName;
        private List<Pet> pets = new ArrayList<>();
    }

    static class People
    {
        List<Person> listPeeps;
        Object[] arrayPeeps;
        Person[] typeArrayPeeps;

        People(Object[] peeps)
        {
            listPeeps = new ArrayList<>(peeps.length);
            arrayPeeps = new Object[peeps.length];
            typeArrayPeeps = new Person[peeps.length];

            for (int i=0; i < peeps.length; i++)
            {
                listPeeps.add((Person)peeps[i]);
                arrayPeeps[i] = peeps[i];
                typeArrayPeeps[i] = (Person) peeps[i];
            }
        }
    }

    public static class CustomPersonWriter implements JsonWriter.JsonClassWriter
    {
        public void write(Object o, boolean showType, Writer output, Map<String, Object> args) throws IOException
        {
            Person p = (Person) o;
            output.write("\"f\":\"");
            output.write(p.getFirstName());
            output.write("\",\"l\":\"");
            output.write(p.getLastName());
            output.write("\",\"p\":[");

            Iterator<Pet> i = p.getPets().iterator();
            while (i.hasNext())
            {
                Pet pet = i.next();
                output.write("{\"n\":\"");
                output.write(pet.getName());
                output.write("\",\"t\":\"");
                output.write(pet.getType());
                output.write("\",\"a\":");
                output.write("" + pet.getAge());
                output.write("}");
                if (i.hasNext())
                {
                    output.write(",");
                }
            }

            output.write("]");

            assert getWriter(args) instanceof JsonWriter;
        }
    }

    public static class CustomPersonWriterAddField implements JsonWriter.JsonClassWriter
    {
        public void write(Object o, boolean showType, Writer output, Map<String, Object> args) throws IOException
        {
            JsonWriter writer = getWriter(args);
            output.write("\"_version\":12,");
            writer.writeObject(o, false, true);
        }
    }

    public static class CustomPersonReader implements JsonReader.JsonClassReader
    {
        public Object read(Object jOb, Deque<JsonObject> stack, Map<String, Object> args)
        {
            JsonReader reader = JsonReader.JsonClassReaderEx.Support.getReader(args);
            assert reader != null;
            JsonObject map = (JsonObject) jOb;
            Person p = new Person();
            p.setFirstName((String)map.get("f"));
            p.setLastName((String)map.get("l"));
            p.setPets(new ArrayList<>());
            Object[] petz = (Object[]) map.get("p");
            for (Object pt : petz)
            {
                Map pet = (Map) pt;
                Pet petObj = new Pet();
                Long age = (Long)pet.get("a");
                petObj.setAge(age.intValue());
                petObj.setName((String)pet.get("n"));
                petObj.setType((String)pet.get("t"));
                p.getPets().add(petObj);
            }

            return p;
        }

    }

    public static class BadCustomPWriter implements JsonWriter.JsonClassWriter
    {
        public void write(Object o, boolean showType, Writer output, Map<String, Object> args) throws IOException
        {
            throw new RuntimeException("Bad custom writer");
        }

    }
}