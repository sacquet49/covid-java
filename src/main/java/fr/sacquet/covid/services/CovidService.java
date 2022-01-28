package fr.sacquet.covid.services;

import fr.sacquet.covid.model.fichier.ClasseAgeCovid19;
import fr.sacquet.covid.model.fichier.Covid19;
import fr.sacquet.covid.model.fichier.NouveauxCovid19;
import fr.sacquet.covid.model.form.FiltreCovid;
import fr.sacquet.covid.model.rest.RootFichierCovid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static fr.sacquet.covid.model.FileName.*;

@Service
@AllArgsConstructor
@Log4j2
public class CovidService {

    private RestTemplate restTemplate;

    private FileService fileService;

    public RootFichierCovid getAllCsv() {
        String url = "https://www.data.gouv.fr/api/2/datasets/5e7e104ace2080d9162b61d8/resources/";
        ResponseEntity<RootFichierCovid> response = restTemplate.getForEntity(url, RootFichierCovid.class);
        RootFichierCovid rootFichierCovid = response.getBody();
        rootFichierCovid.getData()
                .stream().filter(file -> !file.getTitle().contains("metadonnees"))
                .forEach(file -> fileService.saveFile(file));
        return response.getBody();
    }

    public Map<String, Integer> getDecesByDay() {
        NouveauxCovid19[] newCovidList = fileService.readJsonFile(NEW_HOSP, NouveauxCovid19[].class);
        List<NouveauxCovid19> nouveauxCovid19List = Arrays.asList(newCovidList);
        return nouveauxCovid19List.stream()
                .collect(Collectors.groupingBy(NouveauxCovid19::getJour, Collectors.summingInt(NouveauxCovid19::getIncid_dc)))
                .entrySet().stream().sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    public Map<String, Integer> getDataByTypeAndSexAndDepartement(FiltreCovid filtreCovid) {
        Covid19[] newCovidList = fileService.readJsonFile(HOSP, Covid19[].class);
        List<Covid19> nouveauxCovid19List = Arrays.stream(newCovidList)
                .filter(covid19 -> filtreCovid.getDepartement() != null
                        && filtreCovid.getDepartement().equals(covid19.getDep())).toList();
        Map<String, Integer> counting = null;
        if ("rea".equals(filtreCovid.getFiltre())) {
            counting = nouveauxCovid19List.stream()
                    .collect(Collectors.groupingBy(Covid19::getJour, Collectors.summingInt(Covid19::getRea)));
        } else if ("hosp".equals(filtreCovid.getFiltre())) {
            counting = nouveauxCovid19List.stream()
                    .collect(Collectors.groupingBy(Covid19::getJour, Collectors.summingInt(Covid19::getHosp)));
        }
        return counting.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    public List<ClasseAgeCovid19> getLabelsDayByDate() {
        ClasseAgeCovid19[] classAge = fileService.readJsonFile(CLASS_AGE, ClasseAgeCovid19[].class);
        return Arrays.asList(classAge);
    }
}
