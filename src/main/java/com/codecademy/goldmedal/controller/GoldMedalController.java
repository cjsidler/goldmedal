package com.codecademy.goldmedal.controller;

import com.codecademy.goldmedal.model.*;
import com.codecademy.goldmedal.repository.CountryRepository;
import com.codecademy.goldmedal.repository.GoldMedalRepository;
import org.apache.commons.text.WordUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/countries")
public class GoldMedalController {
    private final CountryRepository countryRepository;
    private final GoldMedalRepository goldMedalRepository;

    public GoldMedalController(CountryRepository countryRepository,
                               GoldMedalRepository goldMedalRepository) {
        this.countryRepository = countryRepository;
        this.goldMedalRepository = goldMedalRepository;
    }

    @GetMapping
    public CountriesResponse getCountries(@RequestParam String sort_by, @RequestParam String ascending) {
        var ascendingOrder = ascending.equalsIgnoreCase("y");
        return new CountriesResponse(getCountrySummaries(sort_by.toLowerCase(), ascendingOrder));
    }

    @GetMapping("/{country}")
    public CountryDetailsResponse getCountryDetails(@PathVariable String country) {
        String countryName = WordUtils.capitalizeFully(country);
        return getCountryDetailsResponse(countryName);
    }

    @GetMapping("/{country}/medals")
    public CountryMedalsListResponse getCountryMedalsList(@PathVariable String country, @RequestParam String sort_by, @RequestParam String ascending) {
        String countryName = WordUtils.capitalizeFully(country);
        var ascendingOrder = ascending.equalsIgnoreCase("y");
        return getCountryMedalsListResponse(countryName, sort_by.toLowerCase(), ascendingOrder);
    }

    private CountryMedalsListResponse getCountryMedalsListResponse(String countryName, String sortBy, boolean ascendingOrder) {
        List<GoldMedal> medalsList;
        switch (sortBy) {
            case "year":
                // List of medals sorted by year in the given order
                medalsList = ascendingOrder ? this.goldMedalRepository.findByCountryOrderByYearAsc(countryName) : this.goldMedalRepository.findByCountryOrderByYearDesc(countryName);
                break;
            case "season":
                // List of medals sorted by season in the given order
                medalsList = ascendingOrder ? this.goldMedalRepository.findByCountryOrderBySeasonAsc(countryName) : this.goldMedalRepository.findByCountryOrderBySeasonDesc(countryName);
                break;
            case "city":
                // List of medals sorted by city in the given order
                medalsList = ascendingOrder ? this.goldMedalRepository.findByCountryOrderByCityAsc(countryName) : this.goldMedalRepository.findByCountryOrderByCityDesc(countryName);
                break;
            case "name":
                // List of medals sorted by athlete's name in the given order
                medalsList = ascendingOrder ? this.goldMedalRepository.findByCountryOrderByNameAsc(countryName) : this.goldMedalRepository.findByCountryOrderByNameDesc(countryName);
                break;
            case "event":
                // List of medals sorted by event in the given order
                medalsList = ascendingOrder ? this.goldMedalRepository.findByCountryOrderByEventAsc(countryName) : this.goldMedalRepository.findByCountryOrderByEventDesc(countryName);
                break;
            default:
                medalsList = new ArrayList<>();
                break;
        }

        return new CountryMedalsListResponse(medalsList);
    }

    private CountryDetailsResponse getCountryDetailsResponse(String countryName) {
        // get the country; this repository method should return a java.util.Optional
        Optional<Country> countryOptional = this.countryRepository.findCountryByName(countryName);
        if (countryOptional.isEmpty()) {
            return new CountryDetailsResponse(countryName);
        }

        var country = countryOptional.get();
        // Get the medal count
        var goldMedalCount = this.goldMedalRepository.countByCountry(countryName);

        // Get the collection of wins at the Summer Olympics, sorted by year in ascending order
        var summerWins = this.goldMedalRepository.findByCountryAndSeasonOrderByYearAsc(countryName, "Summer");
        var numberSummerWins = summerWins.size() > 0 ? summerWins.size() : null;

        // Get the total number of events at the Summer Olympics
        var totalSummerEvents = this.goldMedalRepository.countBySeason("Summer");
        var percentageTotalSummerWins = totalSummerEvents != 0 && numberSummerWins != null ? (float) summerWins.size() / totalSummerEvents : null;
        var yearFirstSummerWin = summerWins.size() > 0 ? summerWins.get(0).getYear() : null;

        // Get the collection of wins at the Winter Olympics
        var winterWins = this.goldMedalRepository.findByCountryAndSeasonOrderByYearAsc(countryName, "Winter");
        var numberWinterWins = winterWins.size() > 0 ? winterWins.size() : null;

        // Get the total number of events at the Winter Olympics, sorted by year in ascending order
        var totalWinterEvents = this.goldMedalRepository.countBySeason("Winter");
        var percentageTotalWinterWins = totalWinterEvents != 0 && numberWinterWins != null ? (float) winterWins.size() / totalWinterEvents : null;
        var yearFirstWinterWin = winterWins.size() > 0 ? winterWins.get(0).getYear() : null;

        // Get the number of wins by female athletes
        var numberEventsWonByFemaleAthletes = this.goldMedalRepository.countByCountryAndGender(countryName, "Women");

        // Get the number of wins by male athletes
        var numberEventsWonByMaleAthletes = this.goldMedalRepository.countByCountryAndGender(countryName, "Men");

        return new CountryDetailsResponse(
                countryName,
                country.getGdp(),
                country.getPopulation(),
                goldMedalCount,
                numberSummerWins,
                percentageTotalSummerWins,
                yearFirstSummerWin,
                numberWinterWins,
                percentageTotalWinterWins,
                yearFirstWinterWin,
                numberEventsWonByFemaleAthletes,
                numberEventsWonByMaleAthletes);
    }

    private List<CountrySummary> getCountrySummaries(String sortBy, boolean ascendingOrder) {
        List<Country> countries;
        switch (sortBy) {
            case "name":
                // List of countries sorted by name in the given order
                countries = ascendingOrder ? this.countryRepository.findAllByOrderByNameAsc() : this.countryRepository.findAllByOrderByNameDesc();
                break;
            case "gdp":
                // List of countries sorted by gdp in the given order
                countries = ascendingOrder ? this.countryRepository.findAllByOrderByGdpAsc() : this.countryRepository.findAllByOrderByGdpDesc();
                break;
            case "population":
                // List of countries sorted by population in the given order
                countries = ascendingOrder ? this.countryRepository.findAllByOrderByPopulationAsc() : this.countryRepository.findAllByOrderByPopulationDesc();
                break;
            case "medals":
            default:
                // List of countries in any order you choose; for sorting by medal count, additional logic below will handle that
                countries = this.countryRepository.findAllByOrderByNameAsc();
                break;
        }

        var countrySummaries = getCountrySummariesWithMedalCount(countries);

        if (sortBy.equalsIgnoreCase("medals")) {
            countrySummaries = sortByMedalCount(countrySummaries, ascendingOrder);
        }

        return countrySummaries;
    }

    private List<CountrySummary> sortByMedalCount(List<CountrySummary> countrySummaries, boolean ascendingOrder) {
        return countrySummaries.stream()
                .sorted((t1, t2) -> ascendingOrder ?
                        t1.getMedals() - t2.getMedals() :
                        t2.getMedals() - t1.getMedals())
                .collect(Collectors.toList());
    }

    private List<CountrySummary> getCountrySummariesWithMedalCount(List<Country> countries) {
        List<CountrySummary> countrySummaries = new ArrayList<>();
        for (var country : countries) {
            // Get count of medals for the given country
            var goldMedalCount = this.goldMedalRepository.countByCountry(country.getName());
            countrySummaries.add(new CountrySummary(country, goldMedalCount));
        }
        return countrySummaries;
    }
}
