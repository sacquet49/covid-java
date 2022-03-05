package fr.sacquet.covid.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sacquet.covid.model.rest.FichierCovid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

import static fr.sacquet.covid.model.FileName.HOSP;

@Service
@Log4j2
public class FileService {

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${path.file}")
    private String pathFile;

    private Map<String, Object> cacheMap = new HashMap<>();

    private static final String JSON = ".json";

    public void saveFile(FichierCovid file) {
        log.info(file.getLatest());
        try {
            URL url = getUrlWithoutCert(file.getLatest());
            InputStream in = new BufferedInputStream(url.openStream());
            String nomFichier = file.getTitle().substring(0, file.getTitle().length() - 21);
            log.info(nomFichier);
            File targetFile = new File(pathFile + nomFichier + JSON);
            convertCsvToJson(in, targetFile);
        } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
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

    public void resetCache() {
        cacheMap = new HashMap<>();
    }

    public <T> T readJsonFile(String fileName, Class<T> className) {
        try {
            if(cacheMap.get(fileName) == null) {
                File targetFile = new File(pathFile + fileName + JSON);
                cacheMap.put(fileName, objectMapper.readValue(targetFile, className));
                return objectMapper.readValue(targetFile, className);
            } else {
               return (T) cacheMap.get(fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Erreur lecture du fichier " + HOSP);
        }
        return null;
    }

    private URL getUrlWithoutCert(String url) throws NoSuchAlgorithmException, KeyManagementException, MalformedURLException {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509ExtendedTrustManager() {
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                    @Override
                    public void checkClientTrusted(X509Certificate[] xcs, String string, Socket socket) throws CertificateException {
                    }
                    @Override
                    public void checkServerTrusted(X509Certificate[] xcs, String string, Socket socket) throws CertificateException {
                    }
                    @Override
                    public void checkClientTrusted(X509Certificate[] xcs, String string, SSLEngine ssle) throws CertificateException {
                    }
                    @Override
                    public void checkServerTrusted(X509Certificate[] xcs, String string, SSLEngine ssle) throws CertificateException {
                    }
                }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        return new URL(url);
    }
}
