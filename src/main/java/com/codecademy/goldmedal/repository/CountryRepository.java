package com.codecademy.goldmedal.repository;

import com.codecademy.goldmedal.model.Country;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CountryRepository extends CrudRepository<Country, Long> {
    Optional<Country> findCountryByName(String name);
    List<Country> findAllByOrderByNameAsc();
    List<Country> findAllByOrderByNameDesc();
    List<Country> findAllByOrderByGdpAsc();
    List<Country> findAllByOrderByGdpDesc();
    List<Country> findAllByOrderByPopulationAsc();
    List<Country> findAllByOrderByPopulationDesc();
}
