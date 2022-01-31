package fr.sacquet.covid.model.fichier;

import lombok.*;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class ClasseAgeCovid19 {
    String reg;
    int cl_age90;
    String jour;
    int hosp;
    int rea;
    String autres;
    int rad;
    int dc;
}
