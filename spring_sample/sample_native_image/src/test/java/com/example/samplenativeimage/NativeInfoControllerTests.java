package com.example.samplenativeimage;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.samplenativeimage.nativeapp.NativeInfoController;
import org.junit.jupiter.api.Test;

class NativeInfoControllerTests {

    @Test
    void loadsNativeInfo() throws Exception {
        var response = new NativeInfoController().info();

        assertThat(response.application()).isEqualTo("sample-native-image");
        assertThat(response.notes()).contains("Native Image");
    }
}
