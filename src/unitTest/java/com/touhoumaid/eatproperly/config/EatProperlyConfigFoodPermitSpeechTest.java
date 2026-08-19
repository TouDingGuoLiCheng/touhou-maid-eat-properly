package com.touhoumaid.eatproperly.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EatProperlyConfigFoodPermitSpeechTest {

    @Test
    void defaultFoodPermitSpeechListsMatchN36SpeechPlus() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/touhoumaid/eatproperly/config/EatProperlyConfig.java"));

        assertTrue(source.contains("\"foodPermitApproachSpeech\""));
        assertTrue(source.contains("\"{maid}去拿点吃的，马上回来～\""));
        assertTrue(source.contains("\"补给一下~补给一下~\""));
        assertTrue(source.contains("\"今天吃什么好呢？让我看看\""));

        assertTrue(source.contains("\"foodPermitSpeech\""));
        assertTrue(source.contains("\"补给大成功！\""));
        assertTrue(source.contains("\"好～拿到啦！\""));
        assertTrue(source.contains("\"哇啊，这么多好吃的，向伟大的主人敬礼~\""));
    }
}
