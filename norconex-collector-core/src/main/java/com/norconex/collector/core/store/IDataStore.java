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

import java.io.Closeable;
import java.util.Optional;
import java.util.function.BiPredicate;

// Stores anything for fast retreival.
public interface IDataStore<T> extends Closeable {

    String getName();

    void save(String id, T object);
    Optional<T> find(String id);
    Optional<T> findFirst();
    boolean exists(String id);
    long count();
    boolean delete(String id);
    // returns deleted item
    Optional<T> deleteFirst();
    void clear();
    @Override
    void close();
    boolean forEach(BiPredicate<String, T> predicate);
    boolean isEmpty();
}
