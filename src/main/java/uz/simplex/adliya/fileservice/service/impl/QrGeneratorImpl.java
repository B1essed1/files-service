package uz.simplex.adliya.fileservice.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.simplex.adliya.fileservice.dto.CustomPngMultipartFile;
import uz.simplex.adliya.fileservice.service.QrGenerator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.EnumMap;
import java.util.Map;

@Service
public class QrGeneratorImpl implements QrGenerator {

    @Override
    public MultipartFile generate(String url, String name) {
        try {
            BufferedImage image = generateQRCodeImage(url);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);

            return new CustomPngMultipartFile(baos.toByteArray(), name + ".png");
        } catch (Exception e) {
            return null;
        }
    }


    private BufferedImage generateQRCodeImage(String text) throws WriterException {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.MARGIN, 0);

        /*
         * L - Low
         *
         * Represents the lowest error correction level.
         * Suitable for environments where the QR code is not likely to get damaged, such as when displayed on a screen.
         * M - Medium
         *
         * Represents a medium level of error correction.
         * This is a balanced level and is often a good choice for general-purpose use, providing a reasonable amount of error correction capability.
         * Q - Quality
         *
         * Represents a high level of error correction.
         * This level is useful when the QR code is subject to possible damage or distortion, such as when printed on a surface that might be bent or folded.
         * H - High
         *
         * Represents the highest level of error correction.
         * This level is recommended for situations where the QR code may be exposed to significant damage or distortion, providing the maximum error correction capability.
         */
        hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 300, 300, hints);

        BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 300; x++) {
            for (int y = 0; y < 300; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        return image;
    }
}
