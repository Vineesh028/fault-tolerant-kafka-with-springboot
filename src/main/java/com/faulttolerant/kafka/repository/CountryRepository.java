package com.faulttolerant.kafka.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.faulttolerant.kafka.entity.CountryEntity;

public interface CountryRepository extends JpaRepository<CountryEntity, String>{

}