package com.textkernel.fileupload.web;

import com.textkernel.fileupload.model.FileInfo;
import com.textkernel.fileupload.model.dto.FileInfoDTO;
import com.textkernel.fileupload.service.ApiResponse;
import com.textkernel.fileupload.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("api/files")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    @PutMapping(path ="/upload")
    public ApiResponse<?> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("extension") String extension){
        FileInfoDTO fileName = null;
        try {
             fileName = fileStorageService.storeFile(file, extension);

        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.NOT_ACCEPTABLE.value(), "Information updated unsuccessfully", null);
        }

        return new ApiResponse<>(HttpStatus.OK.value(), "Information updated successfully", fileName);
    }

    @GetMapping(path = "getUploadedFiles")
    public ApiResponse<?> getUploadedFiles() {
        List<FileInfo> uploadedFiles = null;
        try {
            uploadedFiles = fileStorageService.getUploadedFiles();
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.NOT_ACCEPTABLE.value(), "Information fetch unsuccessfully", null);
        }

        return new ApiResponse<>(HttpStatus.OK.value(), "Information fetch successfully", uploadedFiles);
    }

    @GetMapping(path = "/getPlainTextForFile")
    public ApiResponse<?> getPlainTextForFile(@RequestParam("fileName") String fileName, @RequestParam("extension") String extension) throws Exception {
        HashMap plainText = null;
        try {
            plainText = fileStorageService.getPlainText(fileName, extension);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.NOT_ACCEPTABLE.value(), "Information fetch unsuccessfully", null);
        }

        return new ApiResponse<>(HttpStatus.OK.value(), "Information fetch successfully", plainText);
    }

    @GetMapping(path = "/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request){

        Resource resource = fileStorageService.loadFileAsResource(fileName);

        String contentType = null;

        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        }catch(IOException ex) {
            System.out.println("Could not determine fileType");
        }

        if(contentType==null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

}
