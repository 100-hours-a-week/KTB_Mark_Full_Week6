package com.mark.community.service;

import com.mark.community.dto.FileResponse;
import com.mark.community.entity.UploadFile;
import com.mark.community.enums.FileType;
import com.mark.community.exception.CustomException;
import com.mark.community.messages.ApiResponseErrorMessage;
import com.mark.community.messages.ErrorMessage;
import com.mark.community.repository.UploadFileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileService {
    @Value("${file.upload-dir:uploads/}")
    private String UPLOAD_DIR;

    private final UploadFileRepository uploadFileRepository;

    public FileService(UploadFileRepository uploadFileRepository){
        this.uploadFileRepository = uploadFileRepository;
    }

    public UploadFile upload(MultipartFile multipartFile) {
        String originalName = multipartFile.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf('.'));
        FileType fileType = FileType.checkFileType(multipartFile.getContentType());
        Long fileSize = multipartFile.getSize();
        String filePath = UPLOAD_DIR + UUID.randomUUID() + extension;

        if(!saveFile(multipartFile, filePath)) return null;

        UploadFile uploadFile = new UploadFile(originalName, filePath, fileType, fileSize);

        return uploadFileRepository.save(uploadFile);

    }

    private boolean saveFile(MultipartFile file, String filePath){
        try{
            file.transferTo(new File(filePath).getAbsoluteFile());
        } catch (IOException e){
            log.error(ErrorMessage.FAIL_UPLOAD.getMessage());
            return false;
        }
        return true;
    }

    public FileResponse getFile(Long fileId) {
        UploadFile file = uploadFileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ApiResponseErrorMessage.FILE_NOT_FOUND));
        return new FileResponse(file.getFileName(), file.getFilePath());
    }

    public List<FileResponse> getFiles(List<Long> fileIds) {
        return uploadFileRepository.findAllById(fileIds).stream()
                .map(file -> new FileResponse(file.getFileName(), file.getFilePath()))
                .toList();
    }
}