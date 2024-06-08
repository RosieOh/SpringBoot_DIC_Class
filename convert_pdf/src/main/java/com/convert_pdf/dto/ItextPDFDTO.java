package com.convert_pdf.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItextPDFDTO {
    private String pdfCode;
    private String pdfFilePath;
    private String pdfFileName;
}
