package com.textkernel.fileupload.web;

import com.textkernel.fileupload.model.FileInfo;
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
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("api/files")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    @PutMapping(path ="/upload")
    public ApiResponse<?> uploadFile(@RequestParam("file") MultipartFile file){
        List<FileInfo> fileName = fileStorageService.storeFile(file);

        return new ApiResponse<>(HttpStatus.OK.value(), "Information updated successfully", fileName);
    }

    @GetMapping(path = "getUploadedFiles")
    public ApiResponse<?> getUploadedFiles() {
        List<FileInfo> uploadedFiles = fileStorageService.getUploadedFiles();

        return new ApiResponse<>(HttpStatus.OK.value(), "Information fetch successfully", uploadedFiles);
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
