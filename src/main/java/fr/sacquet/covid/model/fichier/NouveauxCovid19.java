package fr.sacquet.covid.model.fichier;

import lombok.*;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class NouveauxCovid19 {
    String dep;
    int sexe;
    String jour;
    int incid_hosp;
    int incid_rea;
    int incid_rad;
    int incid_dc;
}
