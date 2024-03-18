package com.study.poetry.utils;

import com.study.poetry.exception.ResourceNotFoundException;
import com.study.poetry.utils.enums.ImageType;
import com.study.poetry.utils.enums.LogFileType;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Component
@Slf4j
public class FileUtils {
  @Value("${profile-image.path}")
  private String profileImagePath;
  @Value("${poem-background-image.path}")
  private String poemBackgroundImagePath;


  //이미지 저장
  public String uploadProfileImage(MultipartFile multipartFile, Long id, HttpSession session, ImageType imageType) throws IOException {
    String realPath = session.getServletContext().getRealPath("/"); //절대경로 가져오기 (/.../webapp/)
    log.info("realPath : " + realPath);

    String subPath = getImagePath(imageType) + id;
    File folder = new File(realPath + subPath);
    if (!folder.isDirectory()) {//webapp 하위에 images폴더가 없다면 생성
      folder.mkdirs();
    }

    String fileExtension = Objects.requireNonNull(multipartFile.getOriginalFilename())
        .substring(multipartFile.getOriginalFilename().lastIndexOf("."));

    String savedFileName = String.format(imageType.getImageNameFormat(), id, UUID.randomUUID(), fileExtension);

    File file = new File(folder.getAbsolutePath() + "/" + savedFileName);

    multipartFile.transferTo(file);//저장 경로에 파일 생성

    log.info("Image {} is saved on {}", savedFileName, file.getPath());
    return savedFileName;
  }

  //이미지 파일명 생성
  private String getImagePath(ImageType imageType) {
    String path;
    switch (imageType) {
      case PROFILE -> path = profileImagePath;
      case POEM_BACKGROUND -> path = poemBackgroundImagePath;
      default -> path = "/WEB-INF/upload/";
    }
    return path;
  }

  //이미지 삭제
  public void deleteImage(String filePath, ImageType imageType, HttpSession session) {//memberId/filename

    String realPath = session.getServletContext().getRealPath("/"); //절대경로 가져오기 (/.../webapp/)
    log.info("realPath : " + realPath);

    String subPath = getImagePath(imageType) + filePath;
    File fileToDelete = new File(realPath + subPath);

    //해당 파일이 실제로 존재한다면 삭제
    if (fileToDelete.exists()) {
      deleteRecursively(fileToDelete);
    } else {
      log.info("File to delete is nonexistent: " + fileToDelete.getPath());
    }
  }


  //파일 삭제.
  //삭제 대상에 폴더가 포함될 경우, 재귀적으로 폴더 내부 파일 모두 삭제
  private void deleteRecursively(File fileToDelete) {
    if (fileToDelete.isDirectory()) {
      File[] contents = fileToDelete.listFiles();
      if (contents != null) {
        for (File f : contents) {
          deleteRecursively(f);
        }
      }
    }

    if (fileToDelete.delete()) {
      log.info("Successfully deleted: " + fileToDelete.getPath());
    } else {
      String deleteLog = "Failed to delete: " + fileToDelete.getPath();
      log.info(deleteLog);
      LoggingUtils.logToFile(LogFileType.FAILED_TO_DELETE_FILE, deleteLog);
    }
  }


  //이미지 조회시, 해당 이미지를 담은 responseEntity 반환.
  public ResponseEntity<?> getImageResponse(Long id,
                                            String imageName,
                                            ImageType imageType,
                                            HttpSession session,
                                            HttpServletResponse response) throws IOException {
    Path imagePath = Paths.get(session.getServletContext().getRealPath("/") + getImagePath(imageType) + id + "/" + imageName);
    if (Files.exists(imagePath)) {
      // Set Last-Modified header
      Instant lastModified = Files.getLastModifiedTime(imagePath).toInstant();
      response.setDateHeader("Last-Modified", lastModified.toEpochMilli());
      response.setHeader("Cache-Control", "public, max-age=604800, must-revalidate"); //1주일간 캐시 유지

      return ResponseEntity.ok().body(Files.readAllBytes(imagePath));
    } else {
      throw new ResourceNotFoundException("Can not found " + imagePath);
    }
  }
}