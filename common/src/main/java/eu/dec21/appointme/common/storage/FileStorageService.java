package eu.dec21.appointme.common.storage;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageService {

    private final Pattern INVALID_FILE_NAME =
            Pattern.compile("[\\\\/:?*\"<>|\\s\\p{Cntrl}]");

    private boolean isFileSubPathValid(@Nonnull String subPath) {
        return !subPath.isEmpty()
                && !subPath.contains("..")
                && !subPath.startsWith("/")
                && !subPath.startsWith("\\")
                && !INVALID_FILE_NAME.matcher(subPath).find();
    }

    private boolean isFilePathValid(String fileName, String folderName, String subFolderName) {
        return isFileSubPathValid(fileName) && isFileSubPathValid(folderName) && isFileSubPathValid(subFolderName);
    }

    public String saveFile(
            @Nonnull MultipartFile file,
            @Nonnull String fileName,
            @Nonnull String folderName,
            @Nonnull String subFolderName,
            boolean overwrite
    ) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Failed to store empty file " + fileName);
        }

        final String filePath =
                folderName.replaceAll(Pattern.quote(File.separator) + "+$", "")
                        + File.separator
                        + subFolderName.replaceAll("^" + Pattern.quote(File.separator) + "+", "")
                        + File.separator;

        if (!isFilePathValid(fileName, folderName, subFolderName)) {
            throw new IllegalArgumentException("Cannot store file with relative path outside current directory " + filePath + fileName);
        }


        return uploadFile(file, filePath, fileName, overwrite);
    }

    private String uploadFile(
            @Nonnull MultipartFile file,
            @Nonnull String filePath,
            @Nonnull String fileName,
            boolean overwrite
    ) {
        File targetFolder = new File(filePath);
        if (!targetFolder.exists() && !targetFolder.mkdirs()) {
            log.warn("Could not create directory {}", filePath);
            return null;
        }

        final String fileExtension = getFileExtension(fileName);
        final String fileNameWithoutExtension = fileName.substring(0, fileName.length() - fileExtension.length());

        final String fileFullName = overwrite
                ? (filePath + fileName)
                : (filePath + fileNameWithoutExtension + "." + System.currentTimeMillis() + fileExtension);

        try {
            file.transferTo(new File(fileFullName));
            log.info("Stored {}-file {}", fileExtension, fileFullName);
        } catch (Exception e) {
            log.error("Could not store file {}. {}", fileFullName, e.getMessage());
            throw new RuntimeException("Could not store file " + fileFullName + ". Please try again!", e);
        }
        return fileFullName;
    }

    public byte[] getFileContent(@Nonnull String filePath) {
        if (filePath.isBlank() || !new File(filePath).exists()) {
            log.warn("File {} does not exist", filePath);
            return new byte[0];
        }
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            log.error("Could not read file content of {}. {}", filePath, e.getMessage());
            return new byte[0];
        }
    }

    private String getFileExtension(String fileName) {
        final int dot = fileName.lastIndexOf('.');
        return (dot >= 0 && dot < fileName.length() - 1) ? fileName.substring(dot) : "";
    }
}
