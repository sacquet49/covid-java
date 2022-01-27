package fr.sacquet.covid.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fr.sacquet.covid.model.FichierCovid;
import fr.sacquet.covid.model.RootFichierCovid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    private void saveFile(FichierCovid file) {
        log.info(file.getLatest());
        try {
            URL url = new URL(file.getLatest());
            InputStream in = new BufferedInputStream(url.openStream());
            File targetFile = new File("F://covidFile//" + file.getTitle().replace(".csv", ".json" ));
            convertCsvToJson(in, targetFile);
            /*java.nio.file.Files.copy(
                    in,
                    targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            IOUtils.closeQuietly(in);*/
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
