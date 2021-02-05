package com.textkernel.fileupload.model.dto;

import com.textkernel.fileupload.model.FileInfo;

import java.util.List;

public class FileInfoDTO {

    private List<FileInfo> fileInfoList;
    private String plainText;

    public List<FileInfo> getFileInfoList() {
        return fileInfoList;
    }

    public void setFileInfoList(List<FileInfo> fileInfoList) {
        this.fileInfoList = fileInfoList;
    }

    public String getPlainText() {
        return plainText;
    }

    public void setPlainText(String plainText) {
        this.plainText = plainText;
    }
}
