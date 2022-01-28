package fr.sacquet.covid.model.form;

import lombok.*;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class FiltreCovid {
    private String filtre;
    private String sex;
    private String departement;
    private String dateMin;
    private String dateMax;
    private String date;
    private String region;
}
