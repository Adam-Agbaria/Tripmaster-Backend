package com.example.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.app.model.EntityObject;

public interface EntityObjectRepository extends JpaRepository<EntityObject, EntityObject.ObjectId> {
	
	@Query("SELECT e FROM EntityObject e WHERE e.active = true")
    Page<EntityObject> findAllActive(Pageable pageable);

    @Query("SELECT e FROM EntityObject e WHERE e.type = :type")
    Page<EntityObject> findByType(@Param("type") String type, Pageable pageable);

    @Query("SELECT e FROM EntityObject e WHERE e.type = :type AND e.active = true")
    Page<EntityObject> findActiveByType(@Param("type") String type, Pageable pageable);

    @Query("SELECT e FROM EntityObject e WHERE e.alias = :alias")
    Page<EntityObject> findByAlias(@Param("alias") String alias, Pageable pageable);

    @Query("SELECT e FROM EntityObject e WHERE e.alias = :alias AND e.active = true")
    Page<EntityObject> findActiveByAlias(@Param("alias") String alias, Pageable pageable);

    @Query("SELECT e FROM EntityObject e WHERE e.alias LIKE %:pattern%")
    Page<EntityObject> findByAliasPattern(@Param("pattern") String pattern, Pageable pageable);

    @Query("SELECT e FROM EntityObject e WHERE e.alias LIKE %:pattern% AND e.active = true")
    Page<EntityObject> findActiveByAliasPattern(@Param("pattern") String pattern, Pageable pageable);

    @Query("SELECT e FROM EntityObject e WHERE (" +
            "(CASE WHEN :units = 'MILES' THEN 3963 " +
            "      WHEN :units = 'KILOMETERS' THEN 6371 " +
            "      ELSE 1 END) * " +
            "acos(LEAST(GREATEST(cos(radians(:lat)) * cos(radians(e.lat)) * " +
            "cos(radians(e.lng) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(e.lat)), -1), 1))) < :distance " +
            "ORDER BY e.objectId.id ASC")
     List<EntityObject> findByLocation(@Param("lat") double lat,
                                       @Param("lng") double lng,
                                       @Param("distance") double distance,
                                       @Param("units") String units,
                                       Pageable pageable);

     @Query("SELECT e FROM EntityObject e WHERE (" +
            "(CASE WHEN :units = 'MILES' THEN 3963 " +
            "      WHEN :units = 'KILOMETERS' THEN 6371 " +
            "      ELSE 1 END) * " +
            "acos(LEAST(GREATEST(cos(radians(:lat)) * cos(radians(e.lat)) * " +
            "cos(radians(e.lng) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(e.lat)), -1), 1))) < :distance " +
            "AND e.active = true " +
            "ORDER BY e.objectId.id ASC")
     List<EntityObject> findActiveByLocation(@Param("lat") double lat,
                                             @Param("lng") double lng,
                                             @Param("distance") double distance,
                                             @Param("units") String units,
                                             Pageable pageable);
     
     @Query("SELECT e FROM EntityObject e WHERE e.alias = :alias AND e.type = :type")
     Page<EntityObject> findByAliasAndType(@Param("alias") String alias, @Param("type") String type, Pageable pageable);

     @Query("SELECT e FROM EntityObject e WHERE e.alias = :alias AND e.type = :type AND e.active = true")
     Page<EntityObject> findActiveByAliasAndType(@Param("alias") String alias, @Param("type") String type, Pageable pageable);
     
     @Query(value = "SELECT * FROM objects WHERE createdBy::jsonb->'userId'->>'email' = :email AND type = :type",
             countQuery = "SELECT count(*) FROM objects WHERE createdBy::jsonb->'userId'->>'email' = :email AND type = :type",
             nativeQuery = true)
      Page<EntityObject> findByCreatorEmailAndType(@Param("email") String email, @Param("type") String type, Pageable pageable);

      @Query(value = "SELECT * FROM objects WHERE createdBy::jsonb->'userId'->>'email' = :email AND type = :type AND active = true",
             countQuery = "SELECT count(*) FROM objects WHERE createdBy::jsonb->'userId'->>'email' = :email AND type = :type AND active = true",
             nativeQuery = true)
      Page<EntityObject> findActiveByCreatorEmailAndType(@Param("email") String email, @Param("type") String type, Pageable pageable);
      
      @Query(value = "SELECT * FROM objects WHERE objectDetails::jsonb->>'email' = :email AND type = :type AND active = true",
              countQuery = "SELECT count(*) FROM objects WHERE objectDetails::jsonb->>'email' = :email AND type = :type AND active = true",
              nativeQuery = true)
     Page<EntityObject> findByTypeAndObjectDetailsEmail( @Param("email") String email,@Param("type") String type, Pageable pageable);
 }
