package com.textkernel.fileupload.service;

import com.textkernel.fileupload.FileStorageProperties;
import com.textkernel.fileupload.exception.FileStorageException;
import com.textkernel.fileupload.exception.MyFileNotFoundException;
import com.textkernel.fileupload.model.FileInfo;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class FileStorageService {

    private final Path fileStorageLocationPath;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
          Path currentPath  = Paths.get(".").toAbsolutePath().normalize();
          this.fileStorageLocationPath = Paths.get(currentPath.toString(), (Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().getFileName().normalize()).toString()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocationPath);

        }catch(Exception ex) {
            throw new FileStorageException("Could not create the directory to upload");
        }
    }


    //	function to store the file
    public List<FileInfo> storeFile(MultipartFile file) {

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {

            // The idea here was to get plain text from OCR API for the uploaded file and then to save the file in directory,
            // get all uploaded files and return list of uploaded files and plain text for the last uploaded file
            // But unfortunately i get error from api : Please check if the file has sufficient permissions and allows access and is not corrupt.
            String plainText = getPlainText(fileName);

            Path targetLocation = this.fileStorageLocationPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation,StandardCopyOption.REPLACE_EXISTING);

            // get all uploaded files
            return getUploadedFiles();

        } catch(Exception ex) {
            throw new FileStorageException("Could not store file "+fileName + ". Please try again!",ex);
        }
    }

    // function go get plain text from OCR API
    private String getPlainText(String fileName) throws Exception {

        URL obj = new URL("https://api.ocr.space/parse/image"); // OCR API Endpoints
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        JSONObject postDataParams = new JSONObject();

        postDataParams.put("apikey", "5b61727e1588957"); //Add your Registered API key
        postDataParams.put("isOverlayRequired", false);
        postDataParams.put("url", fileName);
        postDataParams.put("language", "eng");
        postDataParams.put("filetype", "PDF");

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(getPostDataString(postDataParams));
            wr.flush();
            wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return String.valueOf(response);
    }

    private String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while (itr.hasNext()) {

            String key = itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), StandardCharsets.UTF_8));

        }
        return result.toString();
    }

    //method for get uploaded files
    public List<FileInfo> getUploadedFiles() {

        List<FileInfo> uploadedFiles = new ArrayList<>();

        File directoryPath = new File(this.fileStorageLocationPath.toString());

        File[] filesList = directoryPath.listFiles();

        for(File files : filesList) {
            FileInfo currentFile = new FileInfo();
            currentFile.setFilename(files.getName());
            currentFile.setFileDownloadUri(files.getAbsolutePath());
            long fileSize = files.length()/1024;
            currentFile.setSize(fileSize);
            uploadedFiles.add(currentFile);
        }
        return uploadedFiles;
    }



    //	function to load the file
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocationPath.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            }else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        }catch(MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName);
        }
    }


}
