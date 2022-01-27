package fr.sacquet.covid.model;

import lombok.Value;

@Value
public class FichierCovid {
    private String departement;
    private Long count;
}
