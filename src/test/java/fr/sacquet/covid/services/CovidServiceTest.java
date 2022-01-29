package fr.sacquet.covid.services;

import fr.sacquet.covid.model.fichier.ClasseAgeCovid19;
import fr.sacquet.covid.model.fichier.Covid19;
import fr.sacquet.covid.model.fichier.NouveauxCovid19;
import fr.sacquet.covid.model.form.FiltreCovid;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static fr.sacquet.covid.model.FileName.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
class CovidServiceTest {

    @Autowired
    private CovidService covidService;

    @MockBean
    private FileService fileService;

    @Test
    void getDecesByDay() {
        // Setup
        List<NouveauxCovid19> covid19List = new ArrayList<>();
        covid19List.add(NouveauxCovid19.builder().jour("2022-10-10").incid_dc(9).build());
        covid19List.add(NouveauxCovid19.builder().jour("2022-10-10").incid_dc(15).build());
        covid19List.add(NouveauxCovid19.builder().jour("2022-11-10").incid_dc(30).build());
        covid19List.add(NouveauxCovid19.builder().jour("2022-12-10").incid_dc(5).build());
        covid19List.add(NouveauxCovid19.builder().jour("2022-10-10").incid_dc(5).build());
        NouveauxCovid19[] nouveauxCovid19Array = covid19List.toArray(new NouveauxCovid19[0]);

        // Given
        when(fileService.readJsonFile(NEW_HOSP, NouveauxCovid19[].class)).thenReturn(nouveauxCovid19Array);

        // When
        Map<String, Integer> result = covidService.getDecesByDay();

        // Then
        assertEquals(29, result.get("2022-10-10"));
        assertEquals(30, result.get("2022-11-10"));
        assertEquals(5, result.get("2022-12-10"));
    }

    @Test
    void getDataByTypeAndSexAndDepartement_hosp_tous() {
        // Setup
        List<Covid19> covid19List = getCovid19s();
        Covid19[] nouveauxCovid19Array = covid19List.toArray(new Covid19[0]);
        FiltreCovid filtreCovid = FiltreCovid.builder().filtre("hosp").sex("0").build();

        // Given
        when(fileService.readJsonFile(HOSP, Covid19[].class)).thenReturn(nouveauxCovid19Array);

        // When
        Map<String, Integer> result = covidService.getDataHospByFiltreCovid(filtreCovid);

        // Then
        assertEquals(71, result.get("2022-10-10"));
        assertEquals(31, result.get("2022-11-10"));
        assertEquals(7, result.get("2022-12-10"));
    }

    @Test
    void getDataByTypeAndSexAndDepartement_rea_tous() {
        // Setup
        List<Covid19> covid19List = getCovid19s();
        Covid19[] nouveauxCovid19Array = covid19List.toArray(new Covid19[0]);
        FiltreCovid filtreCovid = FiltreCovid.builder().filtre("rea").sex("0").build();

        // Given
        when(fileService.readJsonFile(HOSP, Covid19[].class)).thenReturn(nouveauxCovid19Array);

        // When
        Map<String, Integer> result = covidService.getDataHospByFiltreCovid(filtreCovid);

        // Then
        assertEquals(37, result.get("2022-10-10"));
        assertEquals(16, result.get("2022-11-10"));
        assertEquals(31, result.get("2022-12-10"));
    }

    @Test
    void getDataByTypeAndSexAndDepartement_hosp_homme() {
        // Setup
        List<Covid19> covid19List = getCovid19s();
        Covid19[] nouveauxCovid19Array = covid19List.toArray(new Covid19[0]);
        FiltreCovid filtreCovid = FiltreCovid.builder().filtre("hosp").sex("1").build();

        // Given
        when(fileService.readJsonFile(HOSP, Covid19[].class)).thenReturn(nouveauxCovid19Array);

        // When
        Map<String, Integer> result = covidService.getDataHospByFiltreCovid(filtreCovid);

        // Then
        assertEquals(32, result.get("2022-10-10"));
        assertEquals(63, result.get("2022-11-10"));
        assertEquals(8, result.get("2022-12-10"));
    }

    @Test
    void getDataByTypeAndSexAndDepartement_hosp_femme() {
        // Setup
        List<Covid19> covid19List = getCovid19s();
        Covid19[] nouveauxCovid19Array = covid19List.toArray(new Covid19[0]);
        FiltreCovid filtreCovid = FiltreCovid.builder().filtre("hosp").sex("2").build();

        // Given
        when(fileService.readJsonFile(HOSP, Covid19[].class)).thenReturn(nouveauxCovid19Array);

        // When
        Map<String, Integer> result = covidService.getDataHospByFiltreCovid(filtreCovid);

        // Then
        assertEquals(4, result.get("2022-10-10"));
        assertEquals(28, result.get("2022-11-10"));
        assertNull(result.get("2022-12-10"));
    }

    @Test
    void getDataByTypeAndSexAndDepartement_hosp_tous_departement() {
        // Setup
        List<Covid19> covid19List = getCovid19s();
        Covid19[] nouveauxCovid19Array = covid19List.toArray(new Covid19[0]);
        FiltreCovid filtreCovid = FiltreCovid.builder().filtre("hosp").sex("0").departement("49").build();

        // Given
        when(fileService.readJsonFile(HOSP, Covid19[].class)).thenReturn(nouveauxCovid19Array);

        // When
        Map<String, Integer> result = covidService.getDataHospByFiltreCovid(filtreCovid);

        // Then
        assertEquals(6, result.get("2022-10-10"));
        assertEquals(31, result.get("2022-11-10"));
        assertEquals(7, result.get("2022-12-10"));
    }

    @Test
    void getDataByTypeAndSexAndDepartement_hosp_tous_departement_date() {
        // Setup
        List<Covid19> covid19List = getCovid19s();
        Covid19[] nouveauxCovid19Array = covid19List.toArray(new Covid19[0]);
        FiltreCovid filtreCovid = FiltreCovid.builder()
                .filtre("hosp").sex("0").departement("49")
                .dateMin("2022-10-10").dateMax("2022-11-10")
                .build();

        // Given
        when(fileService.readJsonFile(HOSP, Covid19[].class)).thenReturn(nouveauxCovid19Array);

        // When
        Map<String, Integer> result = covidService.getDataHospByFiltreCovid(filtreCovid);

        // Then
        assertEquals(6, result.get("2022-10-10"));
        assertEquals(31, result.get("2022-11-10"));
        assertNull(result.get("2022-12-10"));
    }

    @Test
    void getDataClassAgeByFiltreCovid_hosp_date() {
        // Setup
        List<ClasseAgeCovid19> covid19List = getCovid19Class();
        ClasseAgeCovid19[] nouveauxCovid19Array = covid19List.toArray(new ClasseAgeCovid19[0]);
        FiltreCovid filtreCovid = FiltreCovid.builder()
                .filtre("hosp")
                .dateMin("2022-10-10").dateMax("2022-11-10")
                .build();

        // Given
        when(fileService.readJsonFile(CLASS_AGE, ClasseAgeCovid19[].class)).thenReturn(nouveauxCovid19Array);

        // When
        Map<Pair<String, String>, Integer> result = covidService.getDataClassAgeByFiltreCovid(filtreCovid);

        // Then
        result.forEach((key, value) -> System.out.println(key + " : " + value));
    }

    private List<Covid19> getCovid19s() {
        List<Covid19> covid19List = new ArrayList<>();
        covid19List.add(Covid19.builder().jour("2022-10-10").hosp(9).rea(18).sexe("1").dep("49").build());
        covid19List.add(Covid19.builder().jour("2022-10-10").hosp(12).rea(14).sexe("1").dep("49").build());
        covid19List.add(Covid19.builder().jour("2022-11-10").hosp(13).rea(21).sexe("1").dep("49").build());
        covid19List.add(Covid19.builder().jour("2022-11-10").hosp(50).rea(24).sexe("1").dep("49").build());
        covid19List.add(Covid19.builder().jour("2022-11-10").hosp(10).rea(25).sexe("2").dep("49").build());
        covid19List.add(Covid19.builder().jour("2022-11-10").hosp(18).rea(20).sexe("2").dep("44").build());
        covid19List.add(Covid19.builder().jour("2022-11-10").hosp(31).rea(16).sexe("0").dep("49").build());
        covid19List.add(Covid19.builder().jour("2022-12-10").hosp(8).rea(19).sexe("1").dep("49").build());
        covid19List.add(Covid19.builder().jour("2022-12-10").hosp(7).rea(31).sexe("0").dep("49").build());
        covid19List.add(Covid19.builder().jour("2022-10-10").hosp(9).rea(35).sexe("1").dep("49").build());
        covid19List.add(Covid19.builder().jour("2022-10-10").hosp(65).rea(33).sexe("0").dep("44").build());
        covid19List.add(Covid19.builder().jour("2022-10-10").hosp(4).rea(39).sexe("2").dep("49").build());
        covid19List.add(Covid19.builder().jour("2022-10-10").hosp(2).rea(5).sexe("1").dep("49").build());
        covid19List.add(Covid19.builder().jour("2022-10-10").hosp(6).rea(4).sexe("0").dep("49").build());
        return covid19List;
    }

    private List<ClasseAgeCovid19> getCovid19Class() {
        List<ClasseAgeCovid19> covid19List = new ArrayList<>();
        covid19List.add(ClasseAgeCovid19.builder().jour("2022-10-10").hosp(9).rea(18).reg("49").cl_age90("9").build());
        covid19List.add(ClasseAgeCovid19.builder().jour("2022-10-10").hosp(12).rea(14).reg("49").cl_age90("9").build());
        covid19List.add(ClasseAgeCovid19.builder().jour("2022-11-10").hosp(13).rea(21).reg("49").cl_age90("19").build());
        covid19List.add(ClasseAgeCovid19.builder().jour("2022-11-10").hosp(50).rea(24).reg("49").cl_age90("29").build());
        covid19List.add(ClasseAgeCovid19.builder().jour("2022-11-10").hosp(10).rea(25).reg("49").cl_age90("9").build());
        covid19List.add(ClasseAgeCovid19.builder().jour("2022-11-10").hosp(18).rea(20).reg("44").cl_age90("39").build());
        covid19List.add(ClasseAgeCovid19.builder().jour("2022-11-10").hosp(31).rea(16).reg("49").cl_age90("9").build());
        covid19List.add(ClasseAgeCovid19.builder().jour("2022-12-10").hosp(8).rea(19).reg("49").cl_age90("9").build());
        covid19List.add(ClasseAgeCovid19.builder().jour("2022-12-10").hosp(7).rea(31).reg("49").cl_age90("9").build());
        covid19List.add(ClasseAgeCovid19.builder().jour("2022-10-10").hosp(9).rea(35).reg("49").cl_age90("49").build());
        covid19List.add(ClasseAgeCovid19.builder().jour("2022-10-10").hosp(65).rea(33).reg("44").cl_age90("9").build());
        covid19List.add(ClasseAgeCovid19.builder().jour("2022-10-10").hosp(4).rea(39).reg("49").cl_age90("9").build());
        covid19List.add(ClasseAgeCovid19.builder().jour("2022-10-10").hosp(2).rea(5).reg("49").cl_age90("89").build());
        covid19List.add(ClasseAgeCovid19.builder().jour("2022-10-10").hosp(6).rea(4).reg("49").cl_age90("9").build());
        return covid19List;
    }
}
