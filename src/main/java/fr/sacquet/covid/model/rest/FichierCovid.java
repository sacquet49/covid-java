package fr.sacquet.covid.model.rest;

import lombok.*;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class FichierCovid {
    private String title;
    private String latest;
}
