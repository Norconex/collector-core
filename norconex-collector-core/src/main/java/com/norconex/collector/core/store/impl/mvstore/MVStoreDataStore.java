/* Copyright 2020 Norconex Inc.
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
package com.norconex.collector.core.store.impl.mvstore;

import static java.util.Objects.requireNonNull;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiPredicate;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import com.norconex.collector.core.store.IDataStore;
import com.norconex.commons.lang.bean.BeanUtil;

//TODO extract Ids and Indices in a generic way before creating
// a datastore (passing as argument) or in a utility class.
public class MVStoreDataStore<T> implements IDataStore<T> {

//    private final MVStore mvstore;
    private final MVMap<String, T> map;
    private String name;
//    private final Class<T> type;

    public MVStoreDataStore(MVStore mvstore, String name) {
        super();
        /*this.mvstore = */ requireNonNull(mvstore, "'mvstore' must not be null.");
        this.name = requireNonNull(name, "'name' must not be null.");
//        this.type = requireNonNull(type, "'type' must not be null.");
        map = mvstore.openMap(name);
    }

    @Override
    public String getName() {
        return name;
    }
    String rename(String newName) {
        String oldName = name;
        map.store.renameMap(map, newName);
        name = newName;
        return oldName;
    }

    @Override
    public void save(String id, T object) {
        //TODO test if cloning is necessary.  MVStore doc says values
        // cannot be changed after stored... but maybe if we commit
        // right after it is persisted, so fine?
        map.put(id, BeanUtil.clone(object));
        map.store.commit();
    }

    @Override
    public Optional<T> find(String id) {
        return Optional.ofNullable(map.get(id));
    }

    @Override
    public Optional<T> findFirst() {
        String id = map.firstKey();
        if (id != null) {
            return Optional.ofNullable(map.get(id));
        }
        return Optional.empty();
    }

    @Override
    public boolean exists(String id) {
        return map.containsKey(id);
    }

    @Override
    public long count() {
        return map.sizeAsLong();
    }

    @Override
    public boolean delete(String id) {
        boolean existed = map.remove(id) != null;
        map.store.commit();
        return existed;
    }

    @Override
    public Optional<T> deleteFirst() {
        String id = map.firstKey();
        if (id != null) {
            T removed = map.remove(id);
            map.store.commit();
            return Optional.ofNullable(removed);
        }
        return Optional.empty();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public void clear() {
        map.clear();
        map.store.commit();
    }

    @Override
    public void close() {
        //NOOP, Closed implicitely when engine is closed.
    }

    // returns true if was all read
    @Override
    public boolean forEach(BiPredicate<String, T> predicate) {
        for (Entry<String, T> en : map.entrySet()) {
            if (!predicate.test(en.getKey(), en.getValue())) {
                return false;
            }
        }
        return true;
    }

    MVMap<String, T> getMVMap() {
        return map;
    }
}
