 package com.fixit_ops_management.application.port.in;

import java.util.List;

public interface ICrudServicePort<T, ID> {
    T create(T entity);
    List<T> getAll();
    T getById(ID id);
    void delete(ID id);
}