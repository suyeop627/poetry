package com.study.poetry.utils.enums;

public enum LogFileType {
  FAILED_TO_DELETE_FILE("failed_to_delete_file.log"),
  INTERNAL_SERVER_ERROR("internal_server_error.log"),
  WRITING_LOG_ERROR("logging_error.log");

  final String fileName;
  LogFileType(String fileName) {
    this.fileName = fileName;
  }

  public String getFileName() {
    return fileName;
  }
}
