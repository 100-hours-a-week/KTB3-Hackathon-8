package com.ktb.image.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageForwardingService 테스트")
class ImageForwardingServiceTest {

    private ImageForwardingService imageForwardingService;
    private RestTemplate mockRestTemplate;

    @Captor
    private ArgumentCaptor<HttpEntity<MultiValueMap<String, Object>>> requestCaptor;

    @BeforeEach
    void setUp() {
        mockRestTemplate = mock(RestTemplate.class);
        imageForwardingService = new ImageForwardingService(new RestTemplateBuilder());

        // RestTemplate을 mock으로 교체
        ReflectionTestUtils.setField(imageForwardingService, "restTemplate", mockRestTemplate);
        ReflectionTestUtils.setField(imageForwardingService, "targetServerUrl", "http://localhost:8000/receipt");
    }

    @Test
    @DisplayName("receipt.png 파일을 성공적으로 전송")
    void forwardImage_Success() throws IOException {
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

        String expectedResponse = "{\"status\":\"success\",\"message\":\"Image processed successfully\"}";
        ResponseEntity<String> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        given(mockRestTemplate.postForEntity(
            anyString(),
            any(HttpEntity.class),
            eq(String.class)
        )).willReturn(mockResponse);

        // when
        imageForwardingService.forwardImage(receiptFile);

        // then
        verify(mockRestTemplate, times(1)).postForEntity(
            eq("http://localhost:8000/receipt"),
            requestCaptor.capture(),
            eq(String.class)
        );

        HttpEntity<MultiValueMap<String, Object>> capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest).isNotNull();
        assertThat(capturedRequest.getHeaders().getContentType().toString())
            .contains("multipart/form-data");
        assertThat(capturedRequest.getBody()).isNotNull();
        assertThat(capturedRequest.getBody().get("file")).isNotNull();
    }

    @Test
    @DisplayName("빈 파일 전송 시 예외 발생하지 않음")
    void forwardImage_EmptyFile() throws IOException {
        // given
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file",
            "empty.png",
            "image/png",
            new byte[0]
        );

        ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
        given(mockRestTemplate.postForEntity(
            anyString(),
            any(HttpEntity.class),
            eq(String.class)
        )).willReturn(mockResponse);

        // when
        imageForwardingService.forwardImage(emptyFile);

        // then
        verify(mockRestTemplate, times(1)).postForEntity(
            anyString(),
            any(HttpEntity.class),
            eq(String.class)
        );
    }

    @Test
    @DisplayName("파일 이름이 null인 경우 처리")
    void forwardImage_NullFilename() throws IOException {
        // given
        MockMultipartFile fileWithNullName = new MockMultipartFile(
            "file",
            null,
            "image/png",
            "test content".getBytes()
        );

        ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
        given(mockRestTemplate.postForEntity(
            anyString(),
            any(HttpEntity.class),
            eq(String.class)
        )).willReturn(mockResponse);

        // when
        imageForwardingService.forwardImage(fileWithNullName);

        // then
        verify(mockRestTemplate, times(1)).postForEntity(
            anyString(),
            requestCaptor.capture(),
            eq(String.class)
        );
    }

    @Test
    @DisplayName("다양한 이미지 포맷 지원 - JPEG")
    void forwardImage_JpegFormat() throws IOException {
        // given
        MockMultipartFile jpegFile = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "fake jpeg content".getBytes()
        );

        ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
        given(mockRestTemplate.postForEntity(
            anyString(),
            any(HttpEntity.class),
            eq(String.class)
        )).willReturn(mockResponse);

        // when
        imageForwardingService.forwardImage(jpegFile);

        // then
        verify(mockRestTemplate, times(1)).postForEntity(
            eq("http://localhost:8000/receipt"),
            any(HttpEntity.class),
            eq(String.class)
        );
    }

    @Test
    @DisplayName("실제 receipt.png 파일 크기 검증")
    void verifyReceiptFileSize() throws IOException {
        // given
        ClassPathResource resource = new ClassPathResource("receipt.png");
        InputStream inputStream = resource.getInputStream();
        byte[] fileBytes = inputStream.readAllBytes();
        inputStream.close();

        // then
        assertThat(fileBytes).isNotEmpty();
        assertThat(fileBytes.length).isGreaterThan(0);
        System.out.println("Receipt.png file size: " + fileBytes.length + " bytes");
    }

    @Test
    @DisplayName("서버 URL 설정 검증")
    void verifyServerUrlConfiguration() {
        // given & when
        String serverUrl = (String) ReflectionTestUtils.getField(imageForwardingService, "targetServerUrl");

        // then
        assertThat(serverUrl).isNotNull();
        assertThat(serverUrl).isEqualTo("http://localhost:8000/receipt");
    }
}
