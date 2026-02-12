package com.omegafrog.My.piano.app.web.domain;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.web.multipart.MultipartFile;

import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;

import io.awspring.cloud.s3.ObjectMetadata;

public interface UploadFileExecutor {

  void uploadSheet(File file, String filename, ObjectMetadata metadata) throws IOException;

  void uploadThumbnail(PDDocument document, String filename, ObjectMetadata metadata) throws FileNotFoundException;

  void uploadProfileImg(MultipartFile profileImg, String filename, ObjectMetadata metadata) throws IOException;

  void removeProfileImg(String url);

  void removeSheetPost(SheetPost sheetPost);

  URL createFileUrl(String sheetUrl);

  String buildSheetUrl(String filename);

  String buildThumbnailUrls(String filename, int pageNum);

  void uploadSheetAsync(File file, String filename, ObjectMetadata metadata, String uploadId);

  void uploadThumbnailAsync(PDDocument document, String filename, ObjectMetadata metadata, String uploadId);

}
