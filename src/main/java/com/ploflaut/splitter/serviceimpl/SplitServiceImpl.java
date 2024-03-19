package com.ploflaut.splitter.serviceimpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.ploflaut.splitter.entity.ProfPdfSplitterParentEntity;
import com.ploflaut.splitter.helper.SplitHelper;
import com.ploflaut.splitter.model.ProfSplitPdfRequest;
import com.ploflaut.splitter.model.ProfSplitPdfResponse;
import com.ploflaut.splitter.repository.ProfSplitterParentRepository;
import com.proflaut.dms.constant.DMSConstant;
import org.apache.pdfbox.text.PDFTextStripper;

@Service
public class SplitServiceImpl {

	private static final Logger logger = LogManager.getLogger(SplitServiceImpl.class);

	SplitHelper splitHelper;

	ProfSplitterParentRepository parentRepository;

	@Autowired
	public SplitServiceImpl(SplitHelper splitHelper, ProfSplitterParentRepository parentRepository) {
		this.splitHelper = splitHelper;
		this.parentRepository = parentRepository;
	}

	Set<String> uniqueBarcodeNames = null;

	public List<ProfSplitPdfResponse> splitPdfByBarcode(ProfSplitPdfRequest profSplitPdfRequest)
			throws ChecksumException, FormatException {
		List<ProfSplitPdfResponse> pdfResponses = new ArrayList<>();
		byte[] decodedPdf = Base64.getDecoder().decode((profSplitPdfRequest.getPdf()));

		try (PDDocument document = PDDocument.load(new ByteArrayInputStream(decodedPdf))) {
			List<Integer> barcodePages = splitHelper.detectBarcodePages(document);
			uniqueBarcodeNames = new HashSet<>();

			if (!barcodePages.isEmpty()) {
				int startPage = 0;
				for (Integer barcodePage : barcodePages) {
					splitHelper.addWatermark(document, "PROFLAUT");
					if (startPage != barcodePage) {
						List<ProfSplitPdfResponse> splitResponses = splitHelper.splitPdf(document, startPage,
								barcodePage);
						addUniqueResponses(splitResponses, uniqueBarcodeNames, pdfResponses);
					}
					startPage = barcodePage + 1;
				}
				if (startPage < document.getNumberOfPages()) {
					List<ProfSplitPdfResponse> splitResponses = splitHelper.splitPdf(document, startPage,
							document.getNumberOfPages());
					addUniqueResponses(splitResponses, uniqueBarcodeNames, pdfResponses);
				}
			} else {
				ProfSplitPdfResponse response = new ProfSplitPdfResponse();
				response.setPdf(profSplitPdfRequest.getPdf());
				pdfResponses.add(response);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		ProfPdfSplitterParentEntity parentEntity = splitHelper.convertResponseToEntity(uniqueBarcodeNames,
				profSplitPdfRequest);
		parentRepository.save(parentEntity);
		return pdfResponses;
	}

	private void addUniqueResponses(List<ProfSplitPdfResponse> splitResponses, Set<String> uniqueBarcodeNames,
			List<ProfSplitPdfResponse> pdfResponses) {
		for (ProfSplitPdfResponse response : splitResponses) {
			if (uniqueBarcodeNames.add(response.getBarcodeName())) {
				pdfResponses.add(response);
			}
		}
	}

	public static String removeBlankPagesAndGetBase64(String base64Pdf) {
		try (PDDocument document = PDDocument.load(Base64.getDecoder().decode(base64Pdf))) {
			List<PDPage> blankPages = new ArrayList<>();
			for (PDPage page : document.getPages()) {
				if (isPageBlank(page)) {
					blankPages.add(page);
				}
			}
			for (PDPage page : blankPages) {
				document.removePage(page);
			}

			// Convert modified document to byte array
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			document.save(byteArrayOutputStream);
			byte[] modifiedPdfBytes = byteArrayOutputStream.toByteArray();

			// Encode the byte array as Base64 string
			return Base64.getEncoder().encodeToString(modifiedPdfBytes);
		} catch (IOException e) {
			logger.error(DMSConstant.PRINTSTACKTRACE, e.getMessage(), e);
			return null;
		}
	}

	private static boolean isPageBlank(PDPage page) throws IOException {
		try (PDDocument singlePageDocument = new PDDocument()) {
			singlePageDocument.addPage(page);
			PDFTextStripper stripper = new PDFTextStripper();
			String text = stripper.getText(singlePageDocument);
			return text.trim().isEmpty();
		}
	}

}
