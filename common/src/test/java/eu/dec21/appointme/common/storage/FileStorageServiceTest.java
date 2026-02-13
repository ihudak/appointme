package eu.dec21.appointme.common.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FileStorageService Tests")
class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    private String testFolder;
    private String testSubFolder;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService();
        // Use simple folder names without separators - these will be concatenated by the service
        testFolder = "testfiles";
        testSubFolder = "uploads";
    }

    // === saveFile - Happy Path ===

    @Test
    @DisplayName("saveFile should store file successfully with valid inputs")
    void saveFile_shouldStoreFileSuccessfully() {
        MultipartFile file = createMockFile("test.txt", "Hello World");

        String result = fileStorageService.saveFile(file, "test.txt", testFolder, testSubFolder, true);

        assertThat(result).isNotNull();
        assertThat(new File(result)).exists();
        assertThat(result).contains("test.txt");
        assertThat(result).contains(testSubFolder);
    }

    @Test
    @DisplayName("saveFile should create directories if they don't exist")
    void saveFile_shouldCreateDirectoriesIfNotExist() {
        String newSubFolder = "new-folder";
        MultipartFile file = createMockFile("test.txt", "Content");

        String result = fileStorageService.saveFile(file, "test.txt", testFolder, newSubFolder, true);

        assertThat(result).isNotNull();
        assertThat(new File(result)).exists();
        Path expectedDir = Paths.get(testFolder, newSubFolder);
        assertThat(expectedDir).exists();
    }

    @Test
    @DisplayName("saveFile with overwrite=true should replace existing file")
    void saveFile_withOverwriteTrue_shouldReplaceExistingFile() throws IOException {
        MultipartFile file1 = createMockFile("test.txt", "Original Content");
        MultipartFile file2 = createMockFile("test.txt", "New Content");

        String path1 = fileStorageService.saveFile(file1, "test.txt", testFolder, testSubFolder, true);
        String path2 = fileStorageService.saveFile(file2, "test.txt", testFolder, testSubFolder, true);

        assertThat(path1).isEqualTo(path2);
        byte[] content = Files.readAllBytes(Paths.get(path2));
        assertThat(new String(content)).isEqualTo("New Content");
    }

    @Test
    @DisplayName("saveFile with overwrite=false should create timestamped filename")
    void saveFile_withOverwriteFalse_shouldCreateTimestampedFilename() throws InterruptedException {
        MultipartFile file1 = createMockFile("test.txt", "First");
        MultipartFile file2 = createMockFile("test.txt", "Second");

        String path1 = fileStorageService.saveFile(file1, "test.txt", testFolder, testSubFolder, false);
        Thread.sleep(10); // Ensure different timestamp
        String path2 = fileStorageService.saveFile(file2, "test.txt", testFolder, testSubFolder, false);

        assertThat(path1).isNotEqualTo(path2);
        assertThat(new File(path1)).exists();
        assertThat(new File(path2)).exists();
        assertThat(path1).contains("test.");
        assertThat(path2).contains("test.");
    }

    @Test
    @DisplayName("saveFile should handle file with extension correctly")
    void saveFile_shouldHandleFileWithExtension() {
        MultipartFile file = createMockFile("document.pdf", "PDF Content");

        String result = fileStorageService.saveFile(file, "document.pdf", testFolder, testSubFolder, true);

        assertThat(result).isNotNull();
        assertThat(result).endsWith(".pdf");
        assertThat(new File(result)).exists();
    }

    @Test
    @DisplayName("saveFile should handle file without extension")
    void saveFile_shouldHandleFileWithoutExtension() {
        MultipartFile file = createMockFile("README", "ReadMe Content");

        String result = fileStorageService.saveFile(file, "README", testFolder, testSubFolder, true);

        assertThat(result).isNotNull();
        assertThat(new File(result)).exists();
    }

    @Test
    @DisplayName("saveFile should reject folder with trailing separator")
    void saveFile_shouldRejectFolderWithTrailingSeparator() {
        String folderWithSeparator = testFolder + File.separator;
        MultipartFile file = createMockFile("test.txt", "Content");

        assertThatThrownBy(() ->
                fileStorageService.saveFile(file, "test.txt", folderWithSeparator, testSubFolder, true)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot store file with relative path outside current directory");
    }

    @Test
    @DisplayName("saveFile should reject subfolder with leading separator")
    void saveFile_shouldRejectSubfolderWithLeadingSeparator() {
        String subFolderWithSeparator = File.separator + testSubFolder;
        MultipartFile file = createMockFile("test.txt", "Content");

        assertThatThrownBy(() ->
                fileStorageService.saveFile(file, "test.txt", testFolder, subFolderWithSeparator, true)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot store file with relative path outside current directory");
    }

    // === saveFile - Validation Edge Cases ===

    @Test
    @DisplayName("saveFile should throw exception for empty file")
    void saveFile_shouldThrowExceptionForEmptyFile() {
        MultipartFile emptyFile = createMockFile("empty.txt", "");

        assertThatThrownBy(() ->
                fileStorageService.saveFile(emptyFile, "empty.txt", testFolder, testSubFolder, true)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to store empty file");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "../../../etc/passwd",
            "..\\..\\windows\\system32",
            "./../test.txt",
            "test/../../../file.txt"
    })
    @DisplayName("saveFile should reject path traversal attempts in filename")
    void saveFile_shouldRejectPathTraversalInFilename(String maliciousFilename) {
        MultipartFile file = createMockFile(maliciousFilename, "Malicious");

        assertThatThrownBy(() ->
                fileStorageService.saveFile(file, maliciousFilename, testFolder, testSubFolder, true)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot store file with relative path outside current directory");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "../etc",
            "..\\windows",
            ".."
    })
    @DisplayName("saveFile should reject path traversal attempts in folderName")
    void saveFile_shouldRejectPathTraversalInFolderName(String maliciousFolder) {
        MultipartFile file = createMockFile("test.txt", "Content");

        assertThatThrownBy(() ->
                fileStorageService.saveFile(file, "test.txt", maliciousFolder, testSubFolder, true)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot store file with relative path outside current directory");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "../uploads",
            "..\\uploads",
            "uploads/../.."
    })
    @DisplayName("saveFile should reject path traversal attempts in subFolderName")
    void saveFile_shouldRejectPathTraversalInSubFolderName(String maliciousSubFolder) {
        MultipartFile file = createMockFile("test.txt", "Content");

        assertThatThrownBy(() ->
                fileStorageService.saveFile(file, "test.txt", testFolder, maliciousSubFolder, true)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot store file with relative path outside current directory");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "test\\file.txt",
            "test/file.txt",
            "test:file.txt",
            "test*file.txt",
            "test?file.txt",
            "test\"file.txt",
            "test<file.txt",
            "test>file.txt",
            "test|file.txt",
            "test file.txt", // space
            "test\tfile.txt", // tab
            "test\nfile.txt"  // newline
    })
    @DisplayName("saveFile should reject invalid characters in filename")
    void saveFile_shouldRejectInvalidCharactersInFilename(String invalidFilename) {
        MultipartFile file = createMockFile(invalidFilename, "Content");

        assertThatThrownBy(() ->
                fileStorageService.saveFile(file, invalidFilename, testFolder, testSubFolder, true)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot store file with relative path outside current directory");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/absolute/path",
            "\\absolute\\path",
            "/tmp"
    })
    @DisplayName("saveFile should reject absolute path in filename")
    void saveFile_shouldRejectAbsolutePathInFilename(String absolutePath) {
        MultipartFile file = createMockFile(absolutePath, "Content");

        assertThatThrownBy(() ->
                fileStorageService.saveFile(file, absolutePath, testFolder, testSubFolder, true)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot store file with relative path outside current directory");
    }

    @Test
    @DisplayName("saveFile should throw RuntimeException when file transfer fails")
    void saveFile_shouldThrowRuntimeExceptionWhenTransferFails() throws IOException {
        MultipartFile mockFile = new MockMultipartFile("test.txt", "test.txt", "text/plain", "Content".getBytes()) {
            @Override
            public void transferTo(File dest) throws IOException {
                throw new IOException("Simulated transfer failure");
            }
        };

        assertThatThrownBy(() ->
                fileStorageService.saveFile(mockFile, "test.txt", testFolder, testSubFolder, true)
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Could not store file")
                .hasMessageContaining("Please try again!");
    }

    // === getFileContent - Happy Path ===

    @Test
    @DisplayName("getFileContent should return file bytes for existing file")
    void getFileContent_shouldReturnFileBytesForExistingFile() throws IOException {
        String content = "Test File Content";
        MultipartFile file = createMockFile("test.txt", content);
        String filePath = fileStorageService.saveFile(file, "test.txt", testFolder, testSubFolder, true);

        byte[] result = fileStorageService.getFileContent(filePath);

        assertThat(result).isNotEmpty();
        assertThat(new String(result)).isEqualTo(content);
    }

    @Test
    @DisplayName("getFileContent should handle binary content correctly")
    void getFileContent_shouldHandleBinaryContentCorrectly() throws IOException {
        byte[] binaryData = {0x00, 0x01, 0x02, (byte) 0xFF, (byte) 0xFE};
        MultipartFile file = new MockMultipartFile("binary.dat", "binary.dat", "application/octet-stream", binaryData);
        String filePath = fileStorageService.saveFile(file, "binary.dat", testFolder, testSubFolder, true);

        byte[] result = fileStorageService.getFileContent(filePath);

        assertThat(result).isEqualTo(binaryData);
    }

    @Test
    @DisplayName("getFileContent should handle large files")
    void getFileContent_shouldHandleLargeFiles() throws IOException {
        // Create 1MB file
        byte[] largeContent = new byte[1024 * 1024];
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }
        MultipartFile file = new MockMultipartFile("large.bin", "large.bin", "application/octet-stream", largeContent);
        String filePath = fileStorageService.saveFile(file, "large.bin", testFolder, testSubFolder, true);

        byte[] result = fileStorageService.getFileContent(filePath);

        assertThat(result).hasSize(1024 * 1024);
        assertThat(result).isEqualTo(largeContent);
    }

    // === getFileContent - Edge Cases ===

    @Test
    @DisplayName("getFileContent should return empty array for non-existent file")
    void getFileContent_shouldReturnEmptyArrayForNonExistentFile() {
        String nonExistentPath = tempDir.resolve("non-existent.txt").toString();

        byte[] result = fileStorageService.getFileContent(nonExistentPath);

        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "\t", "\n"})
    @DisplayName("getFileContent should return empty array for blank path")
    void getFileContent_shouldReturnEmptyArrayForBlankPath(String blankPath) {
        byte[] result = fileStorageService.getFileContent(blankPath);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getFileContent should return empty array when IOException occurs")
    void getFileContent_shouldReturnEmptyArrayWhenIOExceptionOccurs() {
        // Use a directory path instead of file path to trigger IOException
        String directoryPath = tempDir.toString();

        byte[] result = fileStorageService.getFileContent(directoryPath);

        assertThat(result).isEmpty();
    }

    // === Integration Scenarios ===

    @Test
    @DisplayName("Should handle complete save and retrieve workflow")
    void shouldHandleCompleteSaveAndRetrieveWorkflow() {
        String originalContent = "Original File Content";
        MultipartFile file = createMockFile("workflow.txt", originalContent);

        // Save file
        String savedPath = fileStorageService.saveFile(file, "workflow.txt", testFolder, testSubFolder, true);
        assertThat(savedPath).isNotNull();
        assertThat(new File(savedPath)).exists();

        // Retrieve file content
        byte[] retrievedContent = fileStorageService.getFileContent(savedPath);
        assertThat(retrievedContent).isNotEmpty();
        assertThat(new String(retrievedContent)).isEqualTo(originalContent);
    }

    @Test
    @DisplayName("Should handle multiple files in same directory")
    void shouldHandleMultipleFilesInSameDirectory() {
        MultipartFile file1 = createMockFile("file1.txt", "Content 1");
        MultipartFile file2 = createMockFile("file2.txt", "Content 2");
        MultipartFile file3 = createMockFile("file3.txt", "Content 3");

        String path1 = fileStorageService.saveFile(file1, "file1.txt", testFolder, testSubFolder, true);
        String path2 = fileStorageService.saveFile(file2, "file2.txt", testFolder, testSubFolder, true);
        String path3 = fileStorageService.saveFile(file3, "file3.txt", testFolder, testSubFolder, true);

        assertThat(path1).isNotEqualTo(path2);
        assertThat(path2).isNotEqualTo(path3);
        assertThat(new File(path1)).exists();
        assertThat(new File(path2)).exists();
        assertThat(new File(path3)).exists();

        assertThat(new String(fileStorageService.getFileContent(path1))).isEqualTo("Content 1");
        assertThat(new String(fileStorageService.getFileContent(path2))).isEqualTo("Content 2");
        assertThat(new String(fileStorageService.getFileContent(path3))).isEqualTo("Content 3");
    }

    @Test
    @DisplayName("Should handle various file extensions correctly")
    void shouldHandleVariousFileExtensionsCorrectly() {
        String[] extensions = {".txt", ".pdf", ".jpg", ".png", ".doc", ".xlsx", ".zip"};

        for (String ext : extensions) {
            String filename = "test" + ext;
            MultipartFile file = createMockFile(filename, "Content for " + ext);

            String path = fileStorageService.saveFile(file, filename, testFolder, testSubFolder, true);

            assertThat(path).endsWith(ext);
            assertThat(new File(path)).exists();
        }
    }

    @Test
    @DisplayName("Should handle file with multiple dots in name")
    void shouldHandleFileWithMultipleDotsInName() {
        MultipartFile file = createMockFile("my.file.name.txt", "Content");

        String path = fileStorageService.saveFile(file, "my.file.name.txt", testFolder, testSubFolder, true);

        assertThat(path).isNotNull();
        assertThat(path).endsWith(".txt");
        assertThat(new File(path)).exists();
    }

    @Test
    @DisplayName("Should preserve file content integrity across save and retrieve")
    void shouldPreserveFileContentIntegrityAcrossSaveAndRetrieve() throws IOException {
        // Unicode content
        String unicodeContent = "Hello 世界 🌍 Привет مرحبا";
        MultipartFile file = createMockFile("unicode.txt", unicodeContent);

        String path = fileStorageService.saveFile(file, "unicode.txt", testFolder, testSubFolder, true);
        byte[] retrieved = fileStorageService.getFileContent(path);

        assertThat(new String(retrieved)).isEqualTo(unicodeContent);
    }

    // === Helper Methods ===

    private MockMultipartFile createMockFile(String filename, String content) {
        return new MockMultipartFile(
                "file",
                filename,
                "text/plain",
                content.getBytes()
        );
    }
}
