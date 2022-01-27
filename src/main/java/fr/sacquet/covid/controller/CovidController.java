package fr.sacquet.covid.controller;

import fr.sacquet.covid.services.CovidService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static fr.sacquet.covid.conf.Constante.PUBLIC_API;

@RestController
@RequestMapping("")
@AllArgsConstructor
public class CovidController {

    private CovidService service;

   /* @GetMapping(value = PUBLIC_API + "/association/{id}")
    public Association getAssociation(@PathVariable String id) {
        return service.getAssociation(id);
    }*/
}
