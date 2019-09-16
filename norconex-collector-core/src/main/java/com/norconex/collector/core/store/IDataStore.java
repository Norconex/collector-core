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
package com.norconex.collector.core.store;

import java.io.Closeable;
import java.util.Optional;

// Stores anything for fast retreival.
// When created we can specify a few things, like if it acts as a queue,
// what property should be serialized, etc.
public interface IDataStore<T> extends Closeable {

    String getName();

    void save(T object);
    Optional<T> findById(String id);
    Iterable<T> findBy(String property, Object value);
    Optional<T> findFirstBy(String property, Object value);
    Iterable<T> findAll();
//    <V> V findPropertyValue(String id, String property);
    boolean existsById(String id);
    boolean existsBy(String property, Object value);
    long count();
    long countBy(String property, Object value);
    boolean deleteById(String id);
    long deleteBy(String property, Object value);

    boolean modifyById(String id, String property, Object value);
    // filterProperty is also targetProperty:
    long modifyBy(
            String filterProperty, Object filterValue, Object newValue);
    long modifyBy(
            String filterProperty, Object filterValue,
            String targetProperty, Object newValue);
    long clear();
    @Override
    void close();
}
