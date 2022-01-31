package fr.sacquet.covid.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sacquet.covid.model.rest.FichierCovid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static fr.sacquet.covid.model.FileName.HOSP;

@Service
@Log4j2
public class FileService {

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${path.file}")
    private String pathFile;

    private static final String JSON = ".json";

    public void saveFile(FichierCovid file) {
        log.info(file.getLatest());
        try {
            URL url = new URL(file.getLatest());
            InputStream in = new BufferedInputStream(url.openStream());
            String nomFichier = file.getTitle().substring(0, file.getTitle().length() - 21);
            log.info(nomFichier);
            File targetFile = new File(pathFile + nomFichier + JSON);
            convertCsvToJson(in, targetFile);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Erreur ecriture du fichier");
        }
    }

    public void convertCsvToJson(InputStream in, File targetFile) {
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
            objectMapper.writeValue(targetFile, list);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Erreur ecriture du fichier");
        }
    }

    public <T> T readJsonFile(String fileName, Class<T> className) {
        try {
            File targetFile = new File(pathFile + fileName + JSON);
            return objectMapper.readValue(targetFile, className);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Erreur lecture du fichier " + HOSP);
        }
        return null;
    }
}
