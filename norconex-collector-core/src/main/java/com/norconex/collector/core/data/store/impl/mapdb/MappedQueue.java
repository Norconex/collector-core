/* Copyright 2014-2016 Norconex Inc.
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
package com.norconex.collector.core.data.store.impl.mapdb;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

import org.mapdb.DB;

import com.norconex.collector.core.data.ICrawlData;

/**
 * A MapDB {@link Queue} implementation backed by a 
 * MapDB {@link Map}.
 * @author Pascal Essiembre
 *
 * @param <T> the type of {@link ICrawlData}
 * @deprecated Since 1.6.0.  Will be removed in future release.
 */
@Deprecated
public class MappedQueue<T extends ICrawlData> implements Queue<T> {

    private final Queue<String> queue;
    private final Map<String, T> map;

    public MappedQueue(DB db, String name, boolean create) {
        super();
        if (create) {
            queue = db.createQueue(name + "-q", null, true);
            map = db.createHashMap(name + "-m").counterEnable().make();
        } else {
            queue = db.getQueue(name + "-q");
            map = db.getHashMap(name + "-m");
        }
    }
    @Override
    public int size() {
        return map.size();
    }
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }
    @Override
    public void clear() {
        queue.clear();
        map.clear();
    }
    @Override
    public boolean offer(T reference) {
        if (queue.offer(reference.getReference())) {
            map.put(reference.getReference(), reference);
            return true;
        }
        return false;
    }
    @Override
    public T remove() {
        String reference = queue.remove();
        return map.remove(reference);
    }
    @Override
    public T poll() {
        String reference = queue.poll();
        if (reference != null) {
            return map.remove(reference);
        }
        return null;
    }
    @Override
    public T element() {
        String reference = queue.element();
        return map.get(reference);
    }
    @Override
    public T peek() {
        String reference = queue.peek();
        if (reference != null) {
            return map.get(reference);
        }
        return null;
    }
    @Override
    public boolean contains(Object o) {
        if (o instanceof String) {
            return map.containsKey((String) o);
        }
        if (o instanceof ICrawlData) {
            return map.containsKey(((ICrawlData) o).getReference());
        }
        return false;
    }
    @Override
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException("iterator() not supported.");
    }
    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("toArray() not supported.");
    }
    @Override
    public <A> A[] toArray(A[] a) {
        throw new UnsupportedOperationException(
                "toArray(A[] a) not supported.");
    }
    @Override
    public boolean remove(Object o) {
        if (o instanceof String) {
            boolean present = queue.remove(o);
            if (present) {
                map.remove(o);
            }
            return present;
        }
        if (o instanceof ICrawlData) {
            String reference = ((ICrawlData) o).getReference();
            boolean present = queue.remove(reference);
            if (present) {
                map.remove(reference);
            }
            return present;
        }
        return false;
    }
    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }
    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException("addAll(...) not supported.");
    }
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException(
                "removeAll(...) not supported.");
    }
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException(
                "retainAll(...) not supported.");
    }
    @Override
    public boolean add(T e) {
        if (e == null) {
            return false;
        }
        boolean changed = queue.add(e.getReference());
        map.put(e.getReference(), e);
        return changed;
    }
}
