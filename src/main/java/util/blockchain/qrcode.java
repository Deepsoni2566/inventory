package util.blockchain;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * Utility class for generating QR code images.
 */
public class qrcode {

    /**
     * Generates a QR code image from the given text and writes it to the specified file path.
     *
     * @param text the content to encode in the QR code
     * @param width the width of the QR code image
     * @param height the height of the QR code image
     * @param filePath the file path (including filename.png) to write the QR image
     * @throws WriterException if an error occurs during QR code generation
     * @throws IOException if an error occurs writing the image file
     */
    public static void generateQRCodeImage(String text, int width, int height, String filePath)
            throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        Path path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
    }
}
