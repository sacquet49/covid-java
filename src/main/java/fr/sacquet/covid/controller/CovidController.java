package fr.sacquet.covid.controller;

import fr.sacquet.covid.model.form.FiltreCovid;
import fr.sacquet.covid.model.rest.RootFichierCovid;
import fr.sacquet.covid.model.rest.TrancheAge;
import fr.sacquet.covid.services.CovidService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static fr.sacquet.covid.conf.Constante.PUBLIC_API;

@RestController
@RequestMapping("")
@AllArgsConstructor
public class CovidController {

    private CovidService service;

    @GetMapping(value = PUBLIC_API + "/update")
    public RootFichierCovid getAssociation() {
        return service.getAllCsv();
    }

    @GetMapping(value = PUBLIC_API + "/{filtre}/{sex}/{departement}")
    public Map<String, Integer> getDataBySexAndDepartement(FiltreCovid filtreCovid) {
        return service.getDataHospByFiltreCovid(filtreCovid);
    }

    @GetMapping(value = PUBLIC_API + "/hospCourant/byDate/{filtre}/{sex}/{departement}/{dateMin}/{dateMax}")
    public Map<String, Integer> getDataByTypeAndSexAndDepartementAndDate(FiltreCovid filtreCovid) {
        return service.getDataHospByFiltreCovid(filtreCovid);
    }

    @GetMapping(value = PUBLIC_API + "/decesByDay")
    public Map<String, Integer> getDecesByDay() {
        return service.getDecesByDay();
    }


    @GetMapping(value = PUBLIC_API + "/trancheAge/{filtre}/{dateMin}/{dateMax}/{region}")
    public List<TrancheAge> getHospitaliseByTrancheAge(FiltreCovid filtreCovid) {
        return service.getDataClassAgeByFiltreCovid(filtreCovid);
    }

    @GetMapping(value = PUBLIC_API + "/hospitalise/{filtre}/trancheAge/byDate/{date}")
    public Map<Integer, Integer> getHospitaliseTrancheAgeByDate(FiltreCovid filtreCovid) {
        return service.getHospitaliseTrancheAgeByDate(filtreCovid);
    }

    @GetMapping(value = PUBLIC_API + "/hospitalise/variation/{filtre}/trancheAge/byDate/{dateMin}/{dateMax}")
    public Map<Integer, Integer> getHospitaliseVariationTrancheAgeByDate(FiltreCovid filtreCovid) {
        return service.getHospitaliseVariationTrancheAgeByDate(filtreCovid);
    }
}
