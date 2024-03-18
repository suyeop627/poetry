package com.study.poetry.utils;

import com.study.poetry.dto.error.ErrorDto;
import com.study.poetry.utils.enums.LogFileType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class LoggingUtils {

  public static String LOG_DIRECTORY = "log/";
  private final static long MAX_LOG_FILE_SIZE = 10 * 1024 * 1024; //10mb

  //authenticationEntryPoint 및 ControllerException에서 ErrorDto를 생성한 뒤 로그 출력함.
  public static void loggingErrorDto(String className, ErrorDto errorDto) {
    log.error("ErrorDto created by {}. ErrorDto(path: {}, statusCode: {}, message: {}, localDateTime: {})",
        className, errorDto.getPath(), errorDto.getStatusCode(), errorDto.getMessage(), errorDto.getLocalDateTime());
  }

  //파일 삭제가 실패 또는 ControllerExceptioniHandler에서 분류하지 않은 예외가 발생한경우 파일에 로그 기록
  public static void logToFile(LogFileType logFileType, String content) {
    content += "\n";

    String formattedToday = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));

    //형식 : yyyy_mm
    String directoryPath = LOG_DIRECTORY + formattedToday.substring(0, formattedToday.lastIndexOf("_"));

    createLogDirectory(directoryPath);

    String filePath = directoryPath + "/" + formattedToday + "_" + logFileType.getFileName();
    String filePathToWrite = checkLogFileSize(filePath);
    log.info("Log Written on {}", filePathToWrite);

    try (PrintWriter writer = new PrintWriter(new FileWriter(filePathToWrite, true))) {
      String logTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
      writer.println(logTime + ": " + content);
    } catch (IOException e) {
      String errorMessage =
          String.format("Error occurred when writing on log file: %s\nPrevious content: %s", e.getMessage(), content);
      log.error(errorMessage, e);
    }
  }

  //로그를 작성할 파일의 크기가 10mb를 초과할 경우, 새 파일 경로 반환
  private static String checkLogFileSize(String filePath) {
    File logFile = new File(filePath);
    if (logFile.exists() && logFile.length() > MAX_LOG_FILE_SIZE) {
      log.info("The current log file size is reached Max log file size.");

      String newFilePath = filePath.replace(".log", "(2).log");
      int count = 2;
      while (new File(newFilePath).exists()) {
        newFilePath = filePath.replace(".log", "(" + count + ").log");
        count++;
      }
      return newFilePath;
    }
    return filePath;
  }

  //로그를 기록할 디렉토리 생성 (log/yyyy_mm)
  private static void createLogDirectory(String directoryPath) {
    File logDirectory = new File(directoryPath);
    if (!logDirectory.exists()) {
      logDirectory.mkdirs();
    }
  }
}
