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
package com.norconex.collector.core.store.impl.nitrite;

import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.dizitart.no2.objects.filters.ObjectFilters.eq;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.dizitart.no2.Document;
import org.dizitart.no2.IndexOptions;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;

import com.norconex.collector.core.CollectorException;
import com.norconex.collector.core.store.IDataStore;
import com.norconex.collector.core.store.Id;
import com.norconex.collector.core.store.Index;
import com.norconex.commons.lang.ClassUtil;
import com.norconex.commons.lang.file.FileUtil;

//TODO extract Ids and Indices in a generic way before creating
// a datastore (passing as argument) or in a utility class.
public class NitriteDataStore<T> implements IDataStore<T> {

    private final Nitrite db;
    private final ObjectRepository<T> repository;
    private final Field idField;

    public NitriteDataStore(Nitrite db, String name, Class<T> type) {
        super();
        Objects.requireNonNull(db, "'db' must not be null.");
        Objects.requireNonNull(name, "'name' must not be null.");
        Objects.requireNonNull(type, "'type' must not be null.");
        this.db = db;
        this.repository = db.getRepository(FileUtil.toSafeFileName(name), type);

        Field idFld = null;

        // ID field from Type
        Id id = ClassUtil.getAnnotation(type, Id.class);
        if (id != null) {
            if (StringUtils.isBlank(id.value())) {
                throw new CollectorException("@Id anotation must have "
                        + "a field name argument when declared on a type.");
            }
            idFld = FieldUtils.getField(type, id.value(), true);
        }

        List<Field> fields = null;

        // ID field from field if still undefined
        if (idFld == null) {
            fields = FieldUtils.getFieldsListWithAnnotation(type, Id.class);
            idFld = fields.isEmpty() ? null : fields.get(0);
            if (idFld != null) {
                idFld.setAccessible(true);
                if (!repository.hasIndex(idFld.getName())) {
                    repository.createIndex(idFld.getName(),
                            IndexOptions.indexOptions(IndexType.Unique));
                }
            }
        }

        if (idFld == null) {
            throw new IllegalArgumentException("Class must have an @id "
                    + "annotation on a field or type: " + type.getName());
        }

        this.idField = idFld;

        // Indexed fields
        fields = FieldUtils.getFieldsListWithAnnotation(type, Index.class);
        for (Field field : fields) {
            if (!repository.hasIndex(field.getName())) {
                field.setAccessible(true);
                repository.createIndex(field.getName(),
                        IndexOptions.indexOptions(IndexType.NonUnique));
            }
        }
    }

    @Override
    public String getName() {
        return FileUtil.fromSafeFileName(
                substringAfter(repository.getName(), "+"));
    }

    @Override
    public void save(T object) {
        Objects.requireNonNull(object, "'object' must not be null.");
        try {
            repository.update(
                    eq(idField.getName(), idField.get(object)), object, true);
            db.commit();
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new CollectorException("Could not save object.", e);
        }
    }

    @Override
    public Optional<T> findById(String id) {
        return findFirstBy(idField.getName(), id);
    }
    @Override
    public Iterable<T> findBy(String property, Object value) {
        return repository.find(eq(property, value));
    }
    @Override
    public Optional<T> findFirstBy(String property, Object value) {
        return Optional.ofNullable(
                repository.find(eq(property, value)).firstOrDefault());
    }
    @Override
    public Iterable<T> findAll() {
        return repository.find();
    }

    @Override
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }
    @Override
    public boolean existsBy(String property, Object value) {
        return findFirstBy(property, value).isPresent();
    }

    @Override
    public long count() {
        return repository.find().totalCount();
    }
    @Override
    public long countBy(String property, Object value) {
        return repository.find(eq(property, value)).totalCount();
    }

    @Override
    public boolean deleteById(String id) {
        return deleteBy(idField.getName(), id) > 0;
    }
    @Override
    public long deleteBy(String property, Object value) {
        long cnt = repository.remove(eq(property, value)).getAffectedCount();
        if (cnt > 0) {
            db.commit();
        }
        return cnt;
    }

    @Override
    public boolean modifyById(String id, String property, Object value) {
        return modifyBy(idField.getName(), id, property, value) > 0;
    }
    @Override
    public long modifyBy(
            String filterProperty, Object filterValue, Object newValue) {
        return modifyBy(filterProperty, filterValue, filterProperty, newValue);
    }
    @Override
    public long modifyBy(String filterProperty, Object filterValue,
            String property, Object value) {
        long cnt = repository.update(eq(filterProperty, filterValue),
                new Document().put(property, value)).getAffectedCount();
        if (cnt > 0) {
            db.commit();
        }
        return cnt;
    }

    @Override
    public long clear() {
        long cnt = repository.remove(ObjectFilters.ALL).getAffectedCount();
        if (cnt > 0) {
            db.commit();
        }
        return cnt;
    }

    @Override
    public void close() {
        repository.close();
    }
}
