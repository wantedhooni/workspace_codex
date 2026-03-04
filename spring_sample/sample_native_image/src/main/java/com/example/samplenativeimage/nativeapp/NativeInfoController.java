package com.example.samplenativeimage.nativeapp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/native")
public class NativeInfoController {

    @GetMapping("/info")
    public NativeInfoResponse info() throws IOException {
        String notes = new ClassPathResource("messages/release-notes.txt")
                .getContentAsString(StandardCharsets.UTF_8);

        return new NativeInfoResponse("sample-native-image", "JVM_OR_NATIVE", notes);
    }
}
