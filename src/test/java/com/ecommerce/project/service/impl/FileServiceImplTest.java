package com.ecommerce.project.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class FileServiceImplTest {
    @InjectMocks
    private FileServiceImpl fileService;

    @Test
    void uploadImage_shouldReturnFileNameAndSaveFile() throws Exception {
        String path = "test-folder";

        MultipartFile image = mock(MultipartFile.class);

        when(image.getOriginalFilename()).thenReturn("test.jpg");
        when(image.getInputStream()).thenReturn(
                new ByteArrayInputStream("dummy data".getBytes())
        );

        String result = fileService.uploadImage(path, image);

        assertNotNull(result);
        assertTrue(result.endsWith(".jpg"));

        // Verify file is created
        File file = new File(path + File.separator + result);
        assertTrue(file.exists());

//        // Cleanup
        file.delete();
        new File(path).delete();
    }

}
