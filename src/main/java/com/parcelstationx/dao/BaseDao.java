package com.parcelstationx.dao;
import java.util.List;
import java.util.Optional;
public interface BaseDao<T, ID> { T save(T entity); Optional<T> findById(ID id); List<T> findAll(); boolean deleteById(ID id); }
