package com.ploflaut.splitter.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProfSplitPdfRequest {
	
	@NotBlank(message = "PDF cannot be blank")
	private String pdf;
	@NotBlank(message = "parentDocName cannot be blank")
	private String parentDocName;
	
}
