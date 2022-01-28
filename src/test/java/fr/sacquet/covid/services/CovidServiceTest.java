package fr.sacquet.covid.services;

import fr.sacquet.covid.model.fichier.NouveauxCovid19;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static fr.sacquet.covid.model.FileName.NEW_HOSP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
}
