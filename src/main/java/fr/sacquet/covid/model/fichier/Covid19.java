package fr.sacquet.covid.model.fichier;

import lombok.*;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class Covid19 {
    String dep;
    String sexe;
    String jour;
    int hosp;
    int rea;
    String autres;
    int rad;
    int dc;
}
