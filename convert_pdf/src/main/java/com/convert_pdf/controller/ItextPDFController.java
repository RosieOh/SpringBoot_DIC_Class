package com.convert_pdf.controller;

import com.convert_pdf.dto.ItextPDFDTO;
import com.convert_pdf.util.ItextPDFUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Objects;
import java.util.Random;

@Controller
@RequiredArgsConstructor
public class ItextPDFController {

    private final ItextPDFUtil util;

    @RequestMapping("/attachment/pdf")
    public void pdfDownload(HttpServletResponse httpServletResponse) {

        ItextPDFDTO domain = new ItextPDFDTO();
        domain.setPdfFilePath("/Users/otaehun/Desktop/DevOps_Rosie/Spring Boot/SpringBoot_Playground/convert_pdf/pdfHoleCount");

        // 윈도우는 나중에
        // domain.setPdfFilePath("/Users/otaehun/Desktop/DevOps_Rosie/Spring Boot/SpringBoot_Playground/convert_pdf/pdfHoleCount");

        // pdf 파일명(텍스트를 위해 랜덤으로 생성)
        domain.setPdfFileName(new Random().nextInt() + ".pdf");

        // getHtml에서 호출될 코드명
        domain.setPdfCode("tae");

        // ======================= PDF 존재 유무 체크 =======================
        // 없다면 PDF 파일 만들기
        File file = util.checkPDF(domain);
        int fileSize = (int) file.length();
        // ===============================================================

        // ===============================================================
        // 파일 다운로드를 위한 header 설정
        httpServletResponse.setContentType("application/octet-stream");
        httpServletResponse.setHeader("Content-Disposition", "attachment; filename="+domain.getPdfFileName()+";");
        httpServletResponse.setContentLengthLong(fileSize);
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        // ===============================================================

        // 파일 다운로드
        BufferedInputStream bi = null;
        BufferedOutputStream bo = null;

        // PDF 파일을 버퍼에 담은 후 다운로드
        try {
            bi = new BufferedInputStream(new FileInputStream(file));
            bo = new BufferedOutputStream(httpServletResponse.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            byte[] buffer = new byte[4096];
            int read = 0;
            while ((read = bi.read(buffer)) != -1) {
                bo.write(buffer, 0, read);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bi.close();
                Objects.requireNonNull(bo).flush();
                bo.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
