package fr.sacquet.covid.model;

import lombok.*;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class FichierCovid {
    private String title;
    private String latest;
}
