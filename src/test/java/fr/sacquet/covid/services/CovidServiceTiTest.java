package fr.sacquet.covid.services;

import fr.sacquet.covid.model.fichier.ClasseAgeCovid19;
import fr.sacquet.covid.model.fichier.Covid19;
import fr.sacquet.covid.model.fichier.NouveauxCovid19;
import fr.sacquet.covid.model.rest.RootFichierCovid;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import wiremock.org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static fr.sacquet.covid.model.FileName.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.cloud.contract.spec.internal.MediaTypes.APPLICATION_JSON;
import static org.springframework.cloud.contract.spec.internal.MediaTypes.APPLICATION_OCTET_STREAM;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 9550)
@ActiveProfiles("test")
class CovidServiceTiTest {

    @Autowired
    private CovidService covidService;

    @Autowired
    private FileService fileService;

    private static final String BASE_PATH = "src/test/resources/services/covid/";

    @Test
    void getAllCsv() throws IOException {
        // Setup
        FileInputStream fis = new FileInputStream(BASE_PATH + "data_gouv_covidFile.json");
        String data = IOUtils.toString(fis, "UTF-8");
        stubFor(get(urlEqualTo("/api/2/datasets/5e7e104ace2080d9162b61d8/resources/"))
                .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(data)));

        // donnees-hospitalieres-covid19
        FileInputStream dhc = new FileInputStream(BASE_PATH + "donnees-hospitalieres-covid19.csv");
        String dhcData = IOUtils.toString(dhc, "UTF-8");
        stubFor(get(urlEqualTo("/fr/datasets/r/63352e38-d353-4b54-bfd1-f1b3ee1cabd7"))
                .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_OCTET_STREAM)
                        .withBody(dhcData.getBytes(StandardCharsets.UTF_8))));

        // donnees-hospitalieres-nouveaux-covid19
        FileInputStream dhnc = new FileInputStream(BASE_PATH + "donnees-hospitalieres-nouveaux-covid19.csv");
        String dhncData = IOUtils.toString(dhnc, "UTF-8");
        stubFor(get(urlEqualTo("/fr/datasets/r/6fadff46-9efd-4c53-942a-54aca783c30c"))
                .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_OCTET_STREAM)
                        .withBody(dhncData.getBytes(StandardCharsets.UTF_8))));

        // donnees-hospitalieres-classe-age-covid19
        FileInputStream dhcac = new FileInputStream(BASE_PATH + "donnees-hospitalieres-classe-age-covid19.csv");
        String dhcacData = IOUtils.toString(dhcac, "UTF-8");
        stubFor(get(urlEqualTo("/fr/datasets/r/08c18e08-6780-452d-9b8c-ae244ad529b3"))
                .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_OCTET_STREAM)
                        .withBody(dhcacData.getBytes(StandardCharsets.UTF_8))));

        // When
        RootFichierCovid rootFichierCovid = covidService.getAllCsv();

        // Then
        assertNotNull(rootFichierCovid);
        verify(1, getRequestedFor(urlEqualTo("/api/2/datasets/5e7e104ace2080d9162b61d8/resources/")));
        verify(1, getRequestedFor(urlEqualTo("/fr/datasets/r/63352e38-d353-4b54-bfd1-f1b3ee1cabd7")));
        verify(1, getRequestedFor(urlEqualTo("/fr/datasets/r/6fadff46-9efd-4c53-942a-54aca783c30c")));
        verify(1, getRequestedFor(urlEqualTo("/fr/datasets/r/08c18e08-6780-452d-9b8c-ae244ad529b3")));
        verify(0, getRequestedFor(urlEqualTo("/fr/datasets/r/9f94a259-2a8a-441d-bd0b-d6b45697d477")));

        assertEquals(174, fileService.readJsonFile(HOSP, Covid19[].class).length);
        assertEquals(352,fileService.readJsonFile(CLASS_AGE, ClasseAgeCovid19[].class).length);
        assertEquals(298, fileService.readJsonFile(NEW_HOSP, NouveauxCovid19[].class).length);
    }
}
