package com.textkernel.fileupload.model.dto;

import com.textkernel.fileupload.model.FileInfo;

import java.util.HashMap;
import java.util.List;

public class FileInfoDTO {

    private List<FileInfo> fileInfoList;
    private HashMap plainText;

    public List<FileInfo> getFileInfoList() {
        return fileInfoList;
    }

    public void setFileInfoList(List<FileInfo> fileInfoList) {
        this.fileInfoList = fileInfoList;
    }

    public HashMap getPlainText() {
        return plainText;
    }

    public void setPlainText(HashMap plainText) {
        this.plainText = plainText;
    }
}
