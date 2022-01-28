package fr.sacquet.covid.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fr.sacquet.covid.model.*;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static fr.sacquet.covid.model.FileName.*;

@Service
@AllArgsConstructor
@Log4j2
public class CovidService {

    private RestTemplate restTemplate;

    public RootFichierCovid getAllCsv() {
        String url = "https://www.data.gouv.fr/api/2/datasets/5e7e104ace2080d9162b61d8/resources/";
        ResponseEntity<RootFichierCovid> response = restTemplate.getForEntity(url, RootFichierCovid.class);
        RootFichierCovid rootFichierCovid = response.getBody();
        rootFichierCovid.getData()
                .stream().filter(file -> !file.getTitle().contains("metadonnees"))
                .forEach(file -> saveFile(file));
        return response.getBody();
    }

    public List<NouveauxCovid19> getDecesByDay() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File targetFile = new File("F://covidFile//" + NEW_HOSP + ".json");
            NouveauxCovid19[] newCovidList = objectMapper.readValue(targetFile, NouveauxCovid19[].class);
            return Arrays.asList(newCovidList);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Erreur lecture du fichier " + NEW_HOSP);
        }
        return Collections.emptyList();
    }

    public List<ClasseAgeCovid19> getLabelsDayByDate() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            File targetFile = new File("F://covidFile//" + CLASS_AGE + ".json");
            ClasseAgeCovid19[] classAge = objectMapper.readValue(targetFile, ClasseAgeCovid19[].class);
            return Arrays.asList(classAge);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Erreur lecture du fichier " + CLASS_AGE);
        }
        return Collections.emptyList();
    }

    public List<Covid19> getDataByTypeAndSexAndDepartement() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            File targetFile = new File("F://covidFile//" + HOSP + ".json");
            Covid19[] covid = objectMapper.readValue(targetFile, Covid19[].class);
            return Arrays.asList(covid);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Erreur lecture du fichier " + HOSP);
        }
        return Collections.emptyList();
    }

    private void saveFile(FichierCovid file) {
        log.info(file.getLatest());
        try {
            URL url = new URL(file.getLatest());
            InputStream in = new BufferedInputStream(url.openStream());
            String nomFichier = file.getTitle().substring(0, file.getTitle().length() - 21);
            log.info(nomFichier);
            File targetFile = new File("F://covidFile//" + nomFichier + ".json");
            convertCsvToJson(in, targetFile);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            log.error("Erreur ecriture du fichier");
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Erreur ecriture du fichier");
        }
    }

    private void convertCsvToJson(InputStream in, File targetFile) {
        try {
            CSV csv = new CSV(true, ';', in);
            List<String> fieldNames = null;
            if (csv.hasNext()) fieldNames = new ArrayList<>(csv.next());
            List<Map<String, String>> list = new ArrayList<>();
            while (csv.hasNext()) {
                List<String> x = csv.next();
                Map<String, String> obj = new LinkedHashMap<>();
                for (int i = 0; i < fieldNames.size(); i++) {
                    obj.put(fieldNames.get(i), x.get(i));
                }
                list.add(obj);
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(targetFile, list);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Erreur ecriture du fichier");
        }
    }
}
