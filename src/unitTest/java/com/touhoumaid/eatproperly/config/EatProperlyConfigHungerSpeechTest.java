package com.touhoumaid.eatproperly.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EatProperlyConfigHungerSpeechTest {

    @Test
    void defaultHungerSpeechIntervalIsThirtySeconds() throws IOException {
        String source = Files.readString(Path.of("src/main/java/com/touhoumaid/eatproperly/config/EatProperlyConfig.java"));

        assertTrue(source.contains(".defineInRange(\"hungerSpeechIntervalTicks\", 600, 200, 720000)"));
    }

    @Test
    void defaultHungerSpeechSourceIncludesMilestoneAcceptanceLines() throws IOException {
        String source = Files.readString(Path.of("src/main/java/com/touhoumaid/eatproperly/config/EatProperlyConfig.java"));

        assertTrue(source.contains("\"真是的，明明说好有好吃的才跟来的啊，肚子饿了啊——太过分啦！\""));
        assertTrue(source.contains("\"那个，我肚子饿了呀\""));
        assertTrue(source.contains("\"肚子好饿，肚子好饿啊~\""));
    }
}
