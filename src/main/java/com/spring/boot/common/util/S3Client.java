package com.spring.boot.common.util;

import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.InputStream;
import java.util.Map;

public interface S3Client {
  String upload(UploadFile uploadFile);
  String upload(InputStream inputStream, long bytesLength, String fileName, String contentType, Map<String, String> metaData);
  String putS3(PutObjectRequest putObjectRequest);
}
