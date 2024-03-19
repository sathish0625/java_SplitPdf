package com.ploflaut.splitter.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ploflaut.splitter.model.ProfSplitPdfRequest;
import com.ploflaut.splitter.model.ProfSplitPdfResponse;
import com.ploflaut.splitter.serviceimpl.SplitServiceImpl;
import com.proflaut.dms.constant.DMSConstant;
import com.proflaut.dms.repository.ProfUserPropertiesRepository;

@RestController
@RequestMapping("/split")
@CrossOrigin
public class SplitController {

	private static final Logger logger = LogManager.getLogger(SplitController.class);

	SplitServiceImpl splitServiceImpl;

	@Autowired
	public SplitController(SplitServiceImpl splitServiceImpl) {
		this.splitServiceImpl = splitServiceImpl;
	}

	@PostMapping("/splitPdf")
	public ResponseEntity<List<ProfSplitPdfResponse>> splitPdf(@RequestBody ProfSplitPdfRequest profSplitPdfRequest) {
		
		if (profSplitPdfRequest.getParentDocName().isEmpty() || profSplitPdfRequest.getPdf().isEmpty()) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		List<ProfSplitPdfResponse> pdfResponses = null;

		try {
			pdfResponses = splitServiceImpl.splitPdfByBarcode(profSplitPdfRequest);
			if (!pdfResponses.isEmpty()) {
				return new ResponseEntity<>(pdfResponses, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			logger.error(DMSConstant.PRINTSTACKTRACE, e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
