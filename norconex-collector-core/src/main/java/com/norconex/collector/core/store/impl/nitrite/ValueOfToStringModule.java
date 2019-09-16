/* Copyright 2019 Norconex Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.norconex.collector.core.store.impl.nitrite;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.reflect.MethodUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

/**
 * JSON Module to serialize using "toString()" and deserialize
 * using a public static method "valueOf(String)".
 */
//TODO make a utility method?
public class ValueOfToStringModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    public final Class<?>[] types;

    public ValueOfToStringModule(Class<?>... types) {
        super();
        this.types = types;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void setupModule(SetupContext context) {
        for (Class t : types) {
            addSerializer(t, new Serializer(t));
            addDeserializer(t, new Deserializer(t));
        }
        super.setupModule(context);
    }


    @SuppressWarnings("rawtypes")
    class Serializer extends StdScalarSerializer {
        private static final long serialVersionUID = 1L;
        @SuppressWarnings("unchecked")
        Serializer(Class<?> type) {
            super(type);
        }
        @Override
        public void serialize(
                Object value, JsonGenerator gen, SerializerProvider provider)
                        throws IOException {
            if (value != null) {
                gen.writeString(value.toString());
            }
        }
    }

    @SuppressWarnings("rawtypes")
    class Deserializer extends StdScalarDeserializer {
        private static final long serialVersionUID = 1L;
        private final Class<?> type;
        @SuppressWarnings("unchecked")
        Deserializer(Class<?> type) {
            super(type);
            this.type = type;
        }
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException {
            try {
                return MethodUtils.invokeStaticMethod(
                        type, "valueOf", p.getText());
            } catch (NoSuchMethodException | IllegalAccessException
                    | InvocationTargetException e) {
                throw new IOException(e);
            }
        }
    }
}
