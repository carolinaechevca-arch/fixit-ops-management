package com.fixit_ops_management.application.port.out;

import java.util.List;
import java.util.Optional;

public interface ICrudPersistencePort<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    void deleteById(ID id);
}