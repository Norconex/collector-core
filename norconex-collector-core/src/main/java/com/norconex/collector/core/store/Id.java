/* Copyright 2019-2020 Norconex Inc.
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
package com.norconex.collector.core.store;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * <p>
 * Add this annotation to a field to mark it as an "id" (or primary key)
 * for use in a {@link IDataStore}. This annotation do not expect
 * any arguments when used this way.
 * </p>
 * <p>
 * You an also use it on a type declaration with a value. The value
 * is then mandatory and represents the name of the field to use as the "id".
 * This is particularly useful when subclassing a parent class that does not
 * use annotations.
 * </p>
 * <p>
 * When declared on a type and a field, the type takes precedence.
 * </p>
 * @author Pascal Essiembre
 */
@Target({FIELD, TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Id {
    /**
     * Name of field representing the object "id". Only valid
     * when declared on a type.
     * @return field name
     */
    public String value() default "";
}
