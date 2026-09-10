package com.parcelstationx.dao;
import com.parcelstationx.model.Shelf; import java.util.Optional;
public interface ShelfDao extends BaseDao<Shelf, Long> { Optional<Shelf> findAvailable(); }
