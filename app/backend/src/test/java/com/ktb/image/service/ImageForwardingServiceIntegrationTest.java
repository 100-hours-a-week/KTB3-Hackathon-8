package com.ktb.image.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * ImageForwardingService 통합 테스트
 *
 * 실제 OCR 서버와 연결하여 테스트합니다.
 * IMAGE_OCR_SERVER_URL 환경변수가 설정되어 있을 때만 실행됩니다.
 *
 * 사용법:
 * export IMAGE_OCR_SERVER_URL=http://your-ocr-server:8000/receipt
 * ./gradlew test --tests ImageForwardingServiceIntegrationTest
 */
@DisplayName("ImageForwardingService 통합 테스트 (실제 OCR 서버 연결)")
class ImageForwardingServiceIntegrationTest {

    private ImageForwardingService imageForwardingService;

    @BeforeEach
    void setUp() {
        imageForwardingService = new ImageForwardingService(new RestTemplateBuilder());

        // 환경변수에서 OCR 서버 URL 가져오기
        String ocrServerUrl = System.getenv("IMAGE_OCR_SERVER_URL");
        if (ocrServerUrl != null && !ocrServerUrl.isEmpty()) {
            ReflectionTestUtils.setField(imageForwardingService, "targetServerUrl", ocrServerUrl);
        } else {
            ReflectionTestUtils.setField(imageForwardingService, "targetServerUrl", "http://localhost:8000/receipt");
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "IMAGE_OCR_SERVER_URL", matches = ".*")
    @DisplayName("실제 OCR 서버로 receipt.png 전송")
    void forwardReceiptToRealOcrServer() throws IOException {
        // given
        ClassPathResource resource = new ClassPathResource("receipt.png");
        InputStream inputStream = resource.getInputStream();
        byte[] fileBytes = inputStream.readAllBytes();
        inputStream.close();

        MockMultipartFile receiptFile = new MockMultipartFile(
            "file",
            "receipt.png",
            "image/png",
            fileBytes
        );

        System.out.println("=== 실제 OCR 서버 테스트 시작 ===");
        System.out.println("파일명: " + receiptFile.getOriginalFilename());
        System.out.println("파일 크기: " + fileBytes.length + " bytes");
        System.out.println("서버 URL: " + ReflectionTestUtils.getField(imageForwardingService, "targetServerUrl"));

        // when & then
        assertDoesNotThrow(() -> {
            imageForwardingService.forwardImage(receiptFile);
            System.out.println("✅ 이미지 전송 성공!");
        });
    }

    @Test
    @DisplayName("receipt.png 파일 존재 및 읽기 검증")
    void verifyReceiptFileExists() throws IOException {
        // given
        ClassPathResource resource = new ClassPathResource("receipt.png");

        // when & then
        assertThat(resource.exists()).isTrue();
        assertThat(resource.isReadable()).isTrue();

        try (InputStream inputStream = resource.getInputStream()) {
            byte[] fileBytes = inputStream.readAllBytes();

            assertThat(fileBytes).isNotEmpty();
            assertThat(fileBytes.length).isGreaterThan(1000); // PNG 파일은 최소 1KB 이상

            // PNG 파일 시그니처 검증 (89 50 4E 47)
            assertThat(fileBytes[0]).isEqualTo((byte) 0x89);
            assertThat(fileBytes[1]).isEqualTo((byte) 0x50);
            assertThat(fileBytes[2]).isEqualTo((byte) 0x4E);
            assertThat(fileBytes[3]).isEqualTo((byte) 0x47);

            System.out.println("✅ receipt.png 파일 검증 성공!");
            System.out.println("   - 파일 크기: " + fileBytes.length + " bytes");
            System.out.println("   - PNG 시그니처: 확인됨");
        }
    }

    @Test
    @DisplayName("MockMultipartFile 생성 검증")
    void verifyMockMultipartFileCreation() throws IOException {
        // given
        ClassPathResource resource = new ClassPathResource("receipt.png");
        byte[] fileBytes = resource.getInputStream().readAllBytes();

        // when
        MockMultipartFile receiptFile = new MockMultipartFile(
            "file",
            "receipt.png",
            "image/png",
            fileBytes
        );

        // then
        assertThat(receiptFile.getName()).isEqualTo("file");
        assertThat(receiptFile.getOriginalFilename()).isEqualTo("receipt.png");
        assertThat(receiptFile.getContentType()).isEqualTo("image/png");
        assertThat(receiptFile.getSize()).isEqualTo(fileBytes.length);
        assertThat(receiptFile.isEmpty()).isFalse();

        System.out.println("✅ MockMultipartFile 생성 검증 성공!");
    }

    @Test
    @DisplayName("환경변수 설정 가이드 출력")
    void printEnvironmentSetupGuide() {
        String currentUrl = (String) ReflectionTestUtils.getField(imageForwardingService, "targetServerUrl");
        String envUrl = System.getenv("IMAGE_OCR_SERVER_URL");

        System.out.println("\n==========================================");
        System.out.println("📋 OCR 서버 연결 설정 가이드");
        System.out.println("==========================================");
        System.out.println("현재 설정된 URL: " + currentUrl);
        System.out.println("환경변수 IMAGE_OCR_SERVER_URL: " + (envUrl != null ? envUrl : "설정되지 않음"));
        System.out.println("\n실제 OCR 서버와 연결하려면:");
        System.out.println("1. 터미널에서 환경변수 설정:");
        System.out.println("   export IMAGE_OCR_SERVER_URL=http://your-server:8000/receipt");
        System.out.println("\n2. 테스트 실행:");
        System.out.println("   ./gradlew test --tests ImageForwardingServiceIntegrationTest");
        System.out.println("==========================================\n");

        assertThat(currentUrl).isNotNull();
    }
}
