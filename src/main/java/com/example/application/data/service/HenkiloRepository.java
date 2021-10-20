package com.example.application.data.service;

import com.example.application.data.entity.Henkilo;
import com.example.application.data.entity.Mittaus;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface HenkiloRepository extends CrudRepository<Henkilo, Integer> {
	
	List<Henkilo> findAll();
	
	
	List<Henkilo> findByNimiContaining(String nimi);
	
	// List<Henkilo> findByMittaus(Mittaus henkilo);
	
	<S extends Henkilo> S save(S entity);

}