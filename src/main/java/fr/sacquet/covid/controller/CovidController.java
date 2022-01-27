package fr.sacquet.covid.controller;

import fr.sacquet.covid.model.RootFichierCovid;
import fr.sacquet.covid.services.CovidService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static fr.sacquet.covid.conf.Constante.PUBLIC_API;

@RestController
@RequestMapping("")
@AllArgsConstructor
public class CovidController {

    private CovidService service;

    @GetMapping(value = PUBLIC_API + "/fichierCovid")
    public RootFichierCovid getAssociation() {
        return service.getAllCsv();
    }
}
