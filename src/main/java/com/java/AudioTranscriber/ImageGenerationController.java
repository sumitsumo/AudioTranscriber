package com.java.AudioTranscriber;

import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.stabilityai.StabilityAiImageModel;
import org.springframework.ai.stabilityai.api.StabilityAiApi;
import org.springframework.ai.stabilityai.api.StabilityAiImageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

@RestController
@RequestMapping("/api/generate")
public class ImageGenerationController {

    private final StabilityAiImageModel stabilityAiImageModel;

    public ImageGenerationController(@Value("${spring.ai.stabilityai.api-key}") String apiKey) {
        StabilityAiApi key = new StabilityAiApi(apiKey);
        this.stabilityAiImageModel = new StabilityAiImageModel(key);
    }

    @PostMapping
    public ResponseEntity<byte[]> generateImage(@RequestParam("Text") String prompt) {
        ImageResponse response = stabilityAiImageModel.call(
                new ImagePrompt(prompt,
                        StabilityAiImageOptions.builder()
                                .withStylePreset("cinematic")
                                .withN(1) // Get a single image
                                .withHeight(1024)
                                .withWidth(1024).build())
        );

        try {
            // Retrieve the base64-encoded image string
            String base64Image = response.getResult().getOutput().getB64Json();

            // Decode the base64 string into a byte array
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            // Set response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG); // Adjust if the image format is different
            headers.setContentLength(imageBytes.length);

            // Return the image as a byte array in the response
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
