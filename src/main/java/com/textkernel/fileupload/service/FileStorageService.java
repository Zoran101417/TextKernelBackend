package com.textkernel.fileupload.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.textkernel.fileupload.FileStorageProperties;
import com.textkernel.fileupload.exception.FileStorageException;
import com.textkernel.fileupload.exception.MyFileNotFoundException;
import com.textkernel.fileupload.model.FileInfo;
import com.textkernel.fileupload.model.dto.FileInfoDTO;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

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
    public FileInfoDTO storeFile(MultipartFile file, String extension) {

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {

            Path targetLocation = this.fileStorageLocationPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation,StandardCopyOption.REPLACE_EXISTING);

            HashMap result = getPlainText(fileName, extension);


            // get all uploaded files
            List<FileInfo> allFiles = getUploadedFiles();
            FileInfoDTO fileInfoDTO = new FileInfoDTO();
            fileInfoDTO.setFileInfoList(allFiles);
            fileInfoDTO.setPlainText(result);

            return fileInfoDTO;

        } catch(Exception ex) {
            throw new FileStorageException("Could not store file "+fileName + ". Please try again!",ex);
        }
    }

    // function go get plain text from OCR API (it is not in use)
    public HashMap getPlainText(String fileName, String extension) throws Exception {


        String commandCURL  = "curl -H apikey:5b61727e1588957 --form file=@"+fileName+" --form language=eng --form filetype="+extension+" --form isOverlayRequired=true https://api.ocr.space/Parse/Image";
        ProcessBuilder processBuilderCURL = new ProcessBuilder(commandCURL.split(" "));

        processBuilderCURL.directory(new File("D:\\React\\fileupload\\upload"));
        Process process1 = processBuilderCURL.start();

        String result3 = new BufferedReader(
                new InputStreamReader(process1.getInputStream()))
                .lines()
                .collect(Collectors.joining("\n"));
        HashMap result =
                new ObjectMapper().readValue(result3, HashMap.class);

        int exitCode = process1.exitValue();

        return result;
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
