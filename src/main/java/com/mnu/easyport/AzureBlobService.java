package com.mnu.easyport;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.ListBlobContainersOptions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@Service
public class AzureBlobService {

    @Autowired
    private BlobServiceClient blobServiceClient;

    // storage내에 새로운 blob 컨테이너를 생성하는 메서드
    // private 스토리지에는 업로드는 되지만 받아올수는 없음
    public void createBlob(String containerName) {
        blobServiceClient.createBlobContainer(containerName);
    }

    // storage내에 있는 blob 컨테이너를 삭제하는 메서드 [계정 삭제시]
    public void deleteBlob(String containerName) {
        // 컨테이너 삭제 후 최소 30초 동안 같은 이름의 컨테이너 생성 불가
        blobServiceClient.deleteBlobContainer(containerName);
    }

    // storage내에서 삭제한 blob 컨테이너 복구하는 메서드 - TODO : 수정 필요
    public void restoreContainer(BlobServiceClient blobServiceClient) {
        ListBlobContainersOptions options = new ListBlobContainersOptions();
        options.getDetails().setRetrieveDeleted(true);

        // Delete the container with the specified prefix using the service client
        for (BlobContainerItem deletedContainerItem : blobServiceClient.listBlobContainers(options, null)) {
            BlobContainerClient containerClient = blobServiceClient
                    .undeleteBlobContainer(deletedContainerItem.getName(), deletedContainerItem.getVersion());
        }
    }

    //containerName의 blob스토리지에 blobName을 가진 파일을 업로드 파일은 filestream으로 받음
    public void uploadBlobFromFile(String containerName, String blobName, InputStream filestream) throws IOException{
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(blobName);

        try {
            blobClient.upload(filestream, filestream.available(), true);
        } catch (UncheckedIOException ex) {
            System.err.printf("Failed to upload from file: %s%n", ex.getMessage());
        }
    }

    
}