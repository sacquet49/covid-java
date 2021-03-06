package fr.sacquet.covid.model.rest;

import lombok.*;

import java.util.List;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class RootFichierCovid {
    private List<FichierCovid> data;
}
