package com.omegafrog.My.piano.app.web.enums;

public enum FileUploadStatus {
    PENDING("업로드 대기"),
    UPLOADING("업로드 중"),
    COMPLETED("업로드 완료"),
    LINKED("파일이 post와 링크됨"),
    FAILED("업로드 실패");

    public final String description;

    FileUploadStatus(String description) {
        this.description = description;
    }
}