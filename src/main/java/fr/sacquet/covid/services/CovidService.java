package fr.sacquet.covid.services;

import fr.sacquet.covid.model.fichier.ClasseAgeCovid19;
import fr.sacquet.covid.model.fichier.Covid19;
import fr.sacquet.covid.model.fichier.NouveauxCovid19;
import fr.sacquet.covid.model.form.FiltreCovid;
import fr.sacquet.covid.model.rest.RootFichierCovid;
import fr.sacquet.covid.model.rest.TrancheAge;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static fr.sacquet.covid.model.FileName.*;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;

@Service
@EnableScheduling
@Slf4j
public class CovidService {

    @Autowired
    private FileService fileService;

    @Value("${data.gouv.url}")
    private String dataGouvUrl;

    @Value("${enableInit}")
    private boolean enableInit;

    @PostConstruct
    public void init() {
        if (enableInit) {
            getAllCsv();
        }
    }

    @Scheduled(cron = "0 0 23 * * ?", zone = "Europe/Paris")
    public RootFichierCovid getAllCsv() {
        fileService.resetCache();
        String url = dataGouvUrl + "/api/2/datasets/5e7e104ace2080d9162b61d8/resources/";
        ResponseEntity<RootFichierCovid> response = restTemplate().getForEntity(url, RootFichierCovid.class);
        RootFichierCovid rootFichierCovid = response.getBody();
        if (rootFichierCovid != null) {
            rootFichierCovid.getData().stream()
                    .filter(file -> !file.getTitle().contains("metadonnees"))
                    .forEach(file -> fileService.saveFile(file));
            fileService.readJsonFile(HOSP, Covid19[].class);
            fileService.readJsonFile(CLASS_AGE, ClasseAgeCovid19[].class);
            fileService.readJsonFile(NEW_HOSP, NouveauxCovid19[].class);
        }
        return rootFichierCovid;
    }

    public Map<String, Integer> getDecesByDay() {
        NouveauxCovid19[] newCovidList = fileService.readJsonFile(NEW_HOSP, NouveauxCovid19[].class);
        List<NouveauxCovid19> nouveauxCovid19List = Arrays.asList(newCovidList);
        return nouveauxCovid19List.parallelStream()
                .collect(groupingBy(NouveauxCovid19::getJour, summingInt(NouveauxCovid19::getIncid_dc)))
                .entrySet().parallelStream().sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    public Map<String, Integer> getDataHospByFiltreCovid(FiltreCovid filtreCovid) {
        Covid19[] newCovidList = fileService.readJsonFile(HOSP, Covid19[].class);
        List<Covid19> List = Arrays.asList(newCovidList);
        List<Covid19> nouveauxCovid19List = List.parallelStream()
                .filter(covid19 -> filterSexe(filtreCovid.getSex(), covid19))
                .filter(covid19 -> filterDateMinMax(filtreCovid, covid19.getJour()))
                .filter(covid19 -> filterDepartement(filtreCovid.getDepartement(), covid19))
                .toList();
        Map<String, Integer> counting = null;
        if ("rea".equals(filtreCovid.getFiltre())) {
            counting = nouveauxCovid19List.parallelStream()
                    .collect(groupingBy(Covid19::getJour, summingInt(Covid19::getRea)));
        } else if ("hosp".equals(filtreCovid.getFiltre())) {
            counting = nouveauxCovid19List.parallelStream()
                    .collect(groupingBy(Covid19::getJour, summingInt(Covid19::getHosp)));
        } else if ("dc".equals(filtreCovid.getFiltre())) {
            counting = nouveauxCovid19List.parallelStream()
                    .collect(groupingBy(Covid19::getJour, summingInt(Covid19::getDc)));
        }
        return Objects.requireNonNull(counting)
                .entrySet().parallelStream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    public List<TrancheAge> getDataClassAgeByFiltreCovid(FiltreCovid filtreCovid) {
        ClasseAgeCovid19[] newCovidList = fileService.readJsonFile(CLASS_AGE, ClasseAgeCovid19[].class);
        long startTime = System.currentTimeMillis();
        List<ClasseAgeCovid19> nouveauxCovid19List = filterTrancheAge(filtreCovid, newCovidList);
        log.info(" Filtrage : " + (System.currentTimeMillis() - startTime) + " ms");
        Map<String, Integer> totalAgeByDate = getTotalAgeByDate(filtreCovid, nouveauxCovid19List);
        Map<Integer, TrancheAge> trancheAgesMap = trancheAges();
        Map<Pair<String, Integer>, Integer> counting = null;

        if ("rea".equals(filtreCovid.getFiltre())) {
            counting = nouveauxCovid19List.parallelStream()
                    .collect(groupingBy(covid ->
                            Pair.of(covid.getJour(), covid.getCl_age90()), summingInt(ClasseAgeCovid19::getRea)));
        } else if ("hosp".equals(filtreCovid.getFiltre())) {
            counting = nouveauxCovid19List.parallelStream()
                    .collect(groupingBy(covid ->
                            Pair.of(covid.getJour(), covid.getCl_age90()), summingInt(ClasseAgeCovid19::getHosp)));
        } else if ("dc".equals(filtreCovid.getFiltre())) {
            counting = nouveauxCovid19List.parallelStream()
                    .collect(groupingBy(covid ->
                            Pair.of(covid.getJour(), covid.getCl_age90()), summingInt(ClasseAgeCovid19::getDc)));
        }

        for (var entry : Objects.requireNonNull(counting).entrySet()) {
            if (entry.getKey().getValue() != 0) {
                trancheAgesMap.get(entry.getKey().getValue()).getData()
                        .put(entry.getKey().getKey(), entry.getValue());

                int pourcentage = (entry.getValue() * 100) / totalAgeByDate.get(entry.getKey().getKey());
                trancheAgesMap.get(entry.getKey().getValue()).getDataP()
                        .put(entry.getKey().getKey(), pourcentage);
            }
        }

        return new ArrayList(trancheAgesMap.values());
    }

    public Map<Integer, Integer> getHospitaliseTrancheAgeByDate(FiltreCovid filtreCovid) {
        return getHospByFilterAndDate(filtreCovid, filtreCovid.getDate());
    }

    public Map<Integer, Integer> getHospitaliseVariationTrancheAgeByDate(FiltreCovid filtreCovid) {
        Map<Integer, Integer> returnData = new HashMap<>();
        Map<Integer, Integer> dataRea = getHospByFilterAndDate(filtreCovid, filtreCovid.getDateMin());
        Map<Integer, Integer> dataRea2 = getHospByFilterAndDate(filtreCovid, filtreCovid.getDateMax());
        for (Integer key : dataRea.keySet()) {
            int variation = 100 * (dataRea2.get(key) - dataRea.get(key)) / dataRea2.get(key);
            returnData.put(key, variation);
        }
        return returnData;
    }

    private Map<Integer, Integer> getHospByFilterAndDate(FiltreCovid filtreCovid, String date) {
        ClasseAgeCovid19[] newCovidList = fileService.readJsonFile(CLASS_AGE, ClasseAgeCovid19[].class);
        List<ClasseAgeCovid19> List = Arrays.asList(newCovidList);
        List<ClasseAgeCovid19> nouveauxCovid19List = List.parallelStream()
                .filter(covid19 -> filterDate(date, covid19.getJour())
                ).toList();
        Map<Integer, Integer> counting = null;

        if ("rea".equals(filtreCovid.getFiltre())) {
            counting = nouveauxCovid19List.parallelStream()
                    .collect(groupingBy(ClasseAgeCovid19::getCl_age90, summingInt(ClasseAgeCovid19::getRea)));
        } else if ("hosp".equals(filtreCovid.getFiltre())) {
            counting = nouveauxCovid19List.parallelStream()
                    .collect(groupingBy(ClasseAgeCovid19::getCl_age90, summingInt(ClasseAgeCovid19::getHosp)));
        } else if ("dc".equals(filtreCovid.getFiltre())) {
            counting = nouveauxCovid19List.parallelStream()
                    .collect(groupingBy(ClasseAgeCovid19::getCl_age90, summingInt(ClasseAgeCovid19::getDc)));
        }

        if (counting != null && counting.size() > 0) {
            counting.remove(0);
        }

        return Objects.requireNonNull(counting)
                .entrySet().parallelStream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    private boolean filterDate(String date, String jour) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date == null || "undefined".equals(date)
                || "null".equals(date)
                || LocalDate.parse(jour, formatter)
                .equals(LocalDate.parse(date, formatter));
    }

    private boolean filterDateMinMax(FiltreCovid filtreCovid, String jour) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return (filtreCovid.getDateMin() == null || filtreCovid.getDateMax() == null
                || "undefined".equals(filtreCovid.getDateMin())
                || "null".equals(filtreCovid.getDateMin()) || "undefined".equals(filtreCovid.getDateMax())
                || "null".equals(filtreCovid.getDateMax()))
                || (LocalDate.parse(jour, formatter)
                .isAfter(LocalDate.parse(filtreCovid.getDateMin(), formatter).plusDays(-1)) &&
                LocalDate.parse(jour, formatter)
                        .isBefore(LocalDate.parse(filtreCovid.getDateMax(), formatter).plusDays(1)));
    }

    private Map<String, Integer> getTotalAgeByDate(FiltreCovid filtreCovid, List<ClasseAgeCovid19> nouveauxCovid19List) {
        Map<String, Integer> totalAgeByDate = null;
        if ("rea".equals(filtreCovid.getFiltre())) {
            totalAgeByDate = nouveauxCovid19List.parallelStream()
                    .collect(groupingBy(ClasseAgeCovid19::getJour, summingInt(ClasseAgeCovid19::getRea)));
        } else if ("hosp".equals(filtreCovid.getFiltre())) {
            totalAgeByDate = nouveauxCovid19List.parallelStream()
                    .collect(groupingBy(ClasseAgeCovid19::getJour, summingInt(ClasseAgeCovid19::getHosp)));
        } else if ("dc".equals(filtreCovid.getFiltre())) {
            totalAgeByDate = nouveauxCovid19List.parallelStream()
                    .collect(groupingBy(ClasseAgeCovid19::getJour, summingInt(ClasseAgeCovid19::getDc)));
        }
        return Objects.requireNonNull(totalAgeByDate)
                .entrySet().parallelStream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    private List<ClasseAgeCovid19> filterTrancheAge(FiltreCovid filtreCovid, ClasseAgeCovid19[] newCovidList) {
        List<ClasseAgeCovid19> list = Arrays.asList(newCovidList);
        return list.parallelStream()
                .filter(covid19 -> filterDateMinMax(filtreCovid, covid19.getJour()))
                .filter(covid19 -> filterRegion(filtreCovid.getRegion(), covid19))
                .toList();
    }

    private boolean filterSexe(String sexe, Covid19 covid19) {
        return sexe == null || "undefined".equals(sexe)
                || "null".equals(sexe) || sexe.equals(covid19.getSexe());
    }

    private boolean filterDepartement(String departement, Covid19 covid19) {
        return departement == null || "undefined".equals(departement)
                || "null".equals(departement) || departement.equals(covid19.getDep());
    }

    private boolean filterRegion(String region, ClasseAgeCovid19 covid19) {
        return region == null || "undefined".equals(region)
                || "null".equals(region) || region.equals(covid19.getReg());
    }

    private Map<Integer, TrancheAge> trancheAges() {
        Map<Integer, TrancheAge> trancheAges = new HashMap<>();
        trancheAges.put(9, TrancheAge.builder().indice("9").label("0 - 9").color("#0050ff")
                .data(new HashMap<>()).dataP(new HashMap<>()).build());
        trancheAges.put(19, TrancheAge.builder().indice("19").label("10 - 19").color("#ff00e5")
                .data(new HashMap<>()).dataP(new HashMap<>()).build());
        trancheAges.put(29, TrancheAge.builder().indice("29").label("20 - 29").color("#00f7ff")
                .data(new HashMap<>()).dataP(new HashMap<>()).build());
        trancheAges.put(39, TrancheAge.builder().indice("39").label("30 - 39").color("#6aff00")
                .data(new HashMap<>()).dataP(new HashMap<>()).build());
        trancheAges.put(49, TrancheAge.builder().indice("49").label("40 - 49").color("#ff0000")
                .data(new HashMap<>()).dataP(new HashMap<>()).build());
        trancheAges.put(59, TrancheAge.builder().indice("59").label("50 - 59").color("#ff7700")
                .data(new HashMap<>()).dataP(new HashMap<>()).build());
        trancheAges.put(69, TrancheAge.builder().indice("69").label("60 - 69").color("#9500ff")
                .data(new HashMap<>()).dataP(new HashMap<>()).build());
        trancheAges.put(79, TrancheAge.builder().indice("79").label("70 - 79").color("#d0ff00")
                .data(new HashMap<>()).dataP(new HashMap<>()).build());
        trancheAges.put(89, TrancheAge.builder().indice("89").label("80 - 89").color("#0b0b18")
                .data(new HashMap<>()).dataP(new HashMap<>()).build());
        trancheAges.put(90, TrancheAge.builder().indice("90").label(">90").color("#02a705")
                .data(new HashMap<>()).dataP(new HashMap<>()).build());
        return trancheAges;
    }

    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
            SSLContext sslContext = null;

            sslContext = org.apache.http.ssl.SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();

            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(csf)
                    .build();
            HttpComponentsClientHttpRequestFactory requestFactory =
                    new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            restTemplate = new RestTemplate(requestFactory);
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        }
        return restTemplate;
    }
}
