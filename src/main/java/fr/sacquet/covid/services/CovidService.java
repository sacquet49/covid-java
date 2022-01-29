package fr.sacquet.covid.services;

import fr.sacquet.covid.model.fichier.ClasseAgeCovid19;
import fr.sacquet.covid.model.fichier.Covid19;
import fr.sacquet.covid.model.fichier.NouveauxCovid19;
import fr.sacquet.covid.model.form.FiltreCovid;
import fr.sacquet.covid.model.rest.RootFichierCovid;
import fr.sacquet.covid.model.rest.TrancheAge;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static fr.sacquet.covid.model.FileName.*;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;

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
                .collect(groupingBy(NouveauxCovid19::getJour, summingInt(NouveauxCovid19::getIncid_dc)))
                .entrySet().stream().sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    public Map<String, Integer> getDataHospByFiltreCovid(FiltreCovid filtreCovid) {
        Covid19[] newCovidList = fileService.readJsonFile(HOSP, Covid19[].class);
        List<Covid19> nouveauxCovid19List = Arrays.stream(newCovidList)
                .filter(covid19 -> filterSexe(filtreCovid.getSex(), covid19) &&
                        filterDepartement(filtreCovid.getDepartement(), covid19) &&
                        filterDate(filtreCovid, covid19.getJour())
                ).toList();
        Map<String, Integer> counting = null;
        if ("rea".equals(filtreCovid.getFiltre())) {
            counting = nouveauxCovid19List.stream()
                    .collect(groupingBy(Covid19::getJour, summingInt(Covid19::getRea)));
        } else if ("hosp".equals(filtreCovid.getFiltre())) {
            counting = nouveauxCovid19List.stream()
                    .collect(groupingBy(Covid19::getJour, summingInt(Covid19::getHosp)));
        }
        return counting.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    public Map<Pair<String, String>, Integer> getDataClassAgeByFiltreCovid(FiltreCovid filtreCovid) {
        ClasseAgeCovid19[] newCovidList = fileService.readJsonFile(CLASS_AGE, ClasseAgeCovid19[].class);
        List<ClasseAgeCovid19> nouveauxCovid19List = Arrays.stream(newCovidList)
                .filter(covid19 -> filterRegion(filtreCovid.getDepartement(), covid19) &&
                        filterDate(filtreCovid, covid19.getJour())
                ).toList();
        Map<Pair<String, String>, Integer> counting = nouveauxCovid19List.stream()
                .collect(groupingBy(covid ->
                        Pair.of(covid.getJour(), covid.getCl_age90()), summingInt(ClasseAgeCovid19::getHosp)));
        return counting.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    public List<ClasseAgeCovid19> getLabelsDayByDate() {
        ClasseAgeCovid19[] classAge = fileService.readJsonFile(CLASS_AGE, ClasseAgeCovid19[].class);
        return Arrays.asList(classAge);
    }

    private boolean filterDate(FiltreCovid filtreCovid, String jour) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return (filtreCovid.getDateMin() == null || filtreCovid.getDateMax() == null)
                || (LocalDate.parse(jour, formatter)
                .isAfter(LocalDate.parse(filtreCovid.getDateMin(), formatter).plusDays(-1)) &&
                LocalDate.parse(jour, formatter)
                        .isBefore(LocalDate.parse(filtreCovid.getDateMax(), formatter).plusDays(1)));
    }

    private boolean filterSexe(String sexe, Covid19 covid19) {
        return sexe == null || sexe.equals(covid19.getSexe());
    }

    private boolean filterDepartement(String departement, Covid19 covid19) {
        return departement == null || departement.equals(covid19.getDep());
    }

    private boolean filterRegion(String region, ClasseAgeCovid19 covid19) {
        return region == null || region.equals(covid19.getReg());
    }

    private List<TrancheAge> trancheAges() {
        List<TrancheAge> trancheAges = new ArrayList<>();
        trancheAges.add(TrancheAge.builder().indice("9").label("0 - 9").color("#0050ff").build());
        trancheAges.add(TrancheAge.builder().indice("19").label("10 - 19").color("#ff00e5").build());
        trancheAges.add(TrancheAge.builder().indice("29").label("20 - 29").color("#00f7ff").build());
        trancheAges.add(TrancheAge.builder().indice("39").label("30 - 39").color("#6aff00").build());
        trancheAges.add(TrancheAge.builder().indice("49").label("40 - 49").color("#ff0000").build());
        trancheAges.add(TrancheAge.builder().indice("59").label("50 - 59").color("#ff7700").build());
        trancheAges.add(TrancheAge.builder().indice("69").label("60 - 69").color("#9500ff").build());
        trancheAges.add(TrancheAge.builder().indice("79").label("70 - 79").color("#d0ff00").build());
        trancheAges.add(TrancheAge.builder().indice("89").label("80 - 89").color("#0b0b18").build());
        trancheAges.add(TrancheAge.builder().indice("90").label(">90").color("#02a705").build());
        return trancheAges;
    }
}
