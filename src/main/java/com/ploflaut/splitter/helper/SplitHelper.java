package com.ploflaut.splitter.helper;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.springframework.stereotype.Component;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.oned.Code128Reader;
import com.ploflaut.splitter.entity.ProfPdfSplitterChildEntity;
import com.ploflaut.splitter.entity.ProfPdfSplitterParentEntity;
import com.ploflaut.splitter.model.ProfSplitPdfRequest;
import com.ploflaut.splitter.model.ProfSplitPdfResponse;

@Component
public class SplitHelper {

	public static String formatCurrentDateTime() {
		LocalDateTime currentDateTime = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(" dd-MM-yyyy HH:mm ");
		return currentDateTime.format(formatter);
	}

	public String generateOTP() {
		SecureRandom secureRandom = new SecureRandom();
		int otp = 1000 + secureRandom.nextInt(9000);
		return String.valueOf(otp);
	}

	static List<String> barcodeNames = new ArrayList<>();

	public List<Integer> detectBarcodePages(PDDocument document)
			throws IOException, ChecksumException, FormatException {
		List<Integer> barcodePages = new ArrayList<>();
		PDFRenderer pdfRenderer = new PDFRenderer(document);

		for (int i = 0; i < document.getNumberOfPages(); i++) {
			BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(i, 300);
			Result result = decodeBarcode(bufferedImage);
			if (result != null) {
				barcodePages.add(i);
			}
		}

		System.out.println("BARCODE PAGES -->" + barcodePages.toString());
		return barcodePages;
	}

	public static Result decodeBarcode(BufferedImage bufferedImage) throws ChecksumException, FormatException {
		LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		Reader reader = new Code128Reader();
		try {
			Result result = reader.decode(bitmap);
			String barcodeValue = result.getText();
			System.out.println("BARCODE VALUE: " + barcodeValue);
			barcodeNames.add(barcodeValue); // Add the barcode value to the list
			return result;
		} catch (NotFoundException e) {
			return null;
		}
	}

	public List<ProfSplitPdfResponse> splitPdf(PDDocument document, int startPage, int endPage) throws IOException {
		List<ProfSplitPdfResponse> response = new ArrayList<>();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try (PDDocument newDocument = new PDDocument()) {
			for (int i = startPage; i < endPage; i++) {
				newDocument.addPage(document.getPage(i));
			}
			newDocument.save(outputStream);
		}
		for (String string : barcodeNames) {
			ProfSplitPdfResponse responses = new ProfSplitPdfResponse();
			responses.setBarcodeName(string);
			responses.setPdf(Base64.getEncoder().encodeToString(outputStream.toByteArray()));
			response.add(responses);
		}
		return response;
	}

	public void addWatermark(PDDocument document, String watermarkText) throws IOException {
		for (int i = 0; i < document.getNumberOfPages(); i++) {
			PDPage page = document.getPage(i);
			try (PDPageContentStream contentStream = new PDPageContentStream(document, page,
					PDPageContentStream.AppendMode.APPEND, true, true)) {
				contentStream.setFont(PDType1Font.TIMES_ITALIC, 50);
				// Set transparency for watermark
				PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
				gs.setNonStrokingAlphaConstant(0.3f); // Adjust as needed, lower value means more transparent
				contentStream.setGraphicsStateParameters(gs);
				// Set the color for watermark
				contentStream.setNonStrokingColor(200, 200, 200); // Adjust as needed
				// Set the rotation angle (45 degrees)
				double angle = Math.PI / 4;
				double x = 100;
				double y = 100;
				// Begin text and set rotation matrix
				contentStream.beginText();
				contentStream.setTextRotation(angle, x, y);
				// Draw the text
				contentStream.newLineAtOffset((float) x, (float) y); // Starting position
				contentStream.showText(watermarkText);
				// End text
				contentStream.endText();
			}
		}
	}

	public ProfPdfSplitterParentEntity convertResponseToEntity(Set<String> uniqueBarcodeNames,
			ProfSplitPdfRequest profSplitPdfRequest) {
		String docId = generateOTP();
		ProfPdfSplitterParentEntity parentEntity = new ProfPdfSplitterParentEntity();
		parentEntity.setParentDocName(profSplitPdfRequest.getParentDocName());
		parentEntity.setDocId(docId);
		parentEntity.setCreatedAt(formatCurrentDateTime());

		List<ProfPdfSplitterChildEntity> childEntities = new ArrayList<>();
		for (String barcodeName : uniqueBarcodeNames) {
			ProfPdfSplitterChildEntity childEntity = new ProfPdfSplitterChildEntity();
			childEntity.setDocname(barcodeName);
			childEntity.setChildDocId(docId + "_" + generateRandom());
			childEntity.setParentEntity(parentEntity);
			childEntities.add(childEntity);
		}

		parentEntity.setChildEntities(childEntities);
		return parentEntity;
	}

	private int generateRandom() {
		return ThreadLocalRandom.current().nextInt(100, 1000);
	}

}