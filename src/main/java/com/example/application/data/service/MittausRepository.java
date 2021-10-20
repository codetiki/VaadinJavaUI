package com.example.application.data.service;

import com.example.application.data.entity.Henkilo;
import com.example.application.data.entity.Mittaus;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MittausRepository extends CrudRepository<Mittaus, Integer> {
	
	List<Mittaus> findAll();
	
	List<Mittaus> findByHenkilo(Henkilo henkilo);
	
	List<Mittaus> findByHenkiloId(Integer henkilo_id);
	
	List<Mittaus> findByToimenpideContaining(String toimenpide);
	
	List<Mittaus> findByPvmBetween(LocalDateTime alku, LocalDateTime loppu);
	
	// List<Mittaus> findByTulosContaining(String tulos);
	
	List<Mittaus> findByHenkiloAndToimenpideContaining(Henkilo henkilo, String toimenpide);
	
	List<Mittaus> findByPvmBetweenAndHenkilo(LocalDateTime alku, LocalDateTime loppu, Henkilo henkilo);
	
	List<Mittaus> findByPvmBetweenAndToimenpideContaining(LocalDateTime alku, LocalDateTime loppu, String toimenpide);
	

	// List<Mittaus> findByToimenpideContainingAndTulosContaining(String toimenpide, String tulos);
	
	// List<Mittaus> findByHenkiloAndTulosContaining(Henkilo henkilo, String tulos);
	
	// List<Mittaus> findByHenkiloAndToimenpideContainingAndTulosContaining(Henkilo henkilo, String toimenpide, String tulos);
	
	
	
	List<Mittaus> findByPvmBetweenAndHenkiloAndToimenpideContaining(LocalDateTime alku, LocalDateTime loppu, Henkilo henkilo, String toimenpide);
	
	<S extends Mittaus> S save(S entity);

}