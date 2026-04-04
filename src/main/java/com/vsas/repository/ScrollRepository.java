package com.vsas.repository;

import com.vsas.entity.Scroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ScrollRepository extends JpaRepository<Scroll, Long>, JpaSpecificationExecutor<Scroll> {

    boolean existsByScrollId(String scrollId);

    boolean existsByName(String name);

    boolean existsByScrollIdAndIdNot(String scrollId, Long id);

    boolean existsByNameAndIdNot(String name, Long id);

    long countByUploader_Username(String username);
}
