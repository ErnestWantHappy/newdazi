package com.ruoyi.business.service;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class FlowchartImageRendererTest {
    @TempDir Path temp;

    @Test
    void savedElbowMustBeDrawnInsteadOfDiagonal() throws Exception {
        String document = "{\"nodes\":[{\"id\":\"a\",\"type\":\"process\",\"x\":100,\"y\":100},"
                + "{\"id\":\"b\",\"type\":\"process\",\"x\":400,\"y\":300}],\"edges\":[{\"id\":\"e\","
                + "\"sourceNodeId\":\"a\",\"targetNodeId\":\"b\",\"pointsList\":[{\"x\":174,\"y\":100},"
                + "{\"x\":250,\"y\":100},{\"x\":250,\"y\":300},{\"x\":326,\"y\":300}]}]}";
        BufferedImage image = ImageIO.read(new FlowchartImageRenderer(new ObjectMapper()).render(document, temp.resolve("elbow.jpg").toFile()));
        assertTrue(new Color(image.getRGB(290, 190)).getRed() < 170);
        assertTrue(new Color(image.getRGB(290, 290)).getRed() < 170);
        assertTrue(new Color(image.getRGB(340, 240)).getRed() > 230);
    }

    @Test
    void negativeAndLargeCoordinatesFitInsideBoundedImage() throws Exception {
        String document = "{\"nodes\":[{\"id\":\"a\",\"type\":\"decision\",\"x\":-2000,\"y\":-1000},"
                + "{\"id\":\"b\",\"type\":\"terminal\",\"x\":3000,\"y\":2000}],\"edges\":[]}";
        BufferedImage image = ImageIO.read(new FlowchartImageRenderer(new ObjectMapper()).render(document, temp.resolve("large.jpg").toFile()));
        assertTrue(image.getWidth() <= 2200); assertTrue(image.getHeight() <= 1600);
        int nonWhite = 0;
        for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
            if (new Color(image.getRGB(x, y)).getRed() < 200) nonWhite++;
        }
        assertTrue(nonWhite > 50);
    }
}
