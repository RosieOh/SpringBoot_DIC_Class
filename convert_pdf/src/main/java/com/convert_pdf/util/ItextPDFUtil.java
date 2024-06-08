package com.convert_pdf.util;

import com.convert_pdf.dto.ItextPDFDTO;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.css.CssFile;
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver;
import com.itextpdf.tool.xml.html.CssAppliers;
import com.itextpdf.tool.xml.html.CssAppliersImpl;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Component
public class ItextPDFUtil {

    public File checkPDF(ItextPDFDTO domain) {
        File file = new File(domain.getPdfFilePath(), domain.getPdfFileName());
        int fileSize = (int) file.length();
        if (fileSize == 0) {
            createPDF(domain);
            file = new File(domain.getPdfFilePath(), domain.getPdfFileName());
        }
        return file;
    }


    /*
     * iText 라이브러리를 사용한 PDF 파일 생성
     * CSS , Font 설정 기능 포함
     * */
    private void createPDF(ItextPDFDTO domain) {

        // 최초 문서 사이즈 설정
        Document document = new Document(PageSize.B4, 30, 30, 30, 30);

        try {
            // PDF 파일 생성
            PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(domain.getPdfFilePath() + domain.getPdfFileName()));

            // PDF 파일에 사용할 폰트 크기 설정
            pdfWriter.setInitialLeading(12.5f);

            // PDF 파일 열기
            document.open();

            // CSS 설정 변수
            CSSResolver cssResolver = new StyleAttrCSSResolver();
            CssFile cssFile = null;

            try {
                /*
                 * CSS 파일 설정
                 * 기존 방식은 FileInputStream을 사용했으나, jar 파일로 빌드 시 파일을 찾을 수 없는 문제가 발생
                 * 따라서, ClassLoader를 사용하여 파일을 읽어오는 방식으로 변경
                 */

                InputStream cssStream = getClass().getClassLoader().getResourceAsStream("static/css/ItextPDF.css");

                // CSS 파일 닫기
                cssFile = XMLWorkerHelper.getCSS(cssStream);
            } catch (Exception e) {
                throw new IllegalArgumentException("PDF CSS 파일을 찾을 수 없습니다.");
            }
            if (cssFile == null) {
                throw new IllegalArgumentException("PDF CSS 파일을 찾을 수 없습니다.");
            }

            // CSS 파일 적용
            cssResolver.addCss(cssFile);

            // PDF 파잉에 HTML 내용 삽입
            XMLWorkerFontProvider xmlWorkerFontProvider = new XMLWorkerFontProvider();

            /*
             * 폰트 설정
             * CSS 와 다르게, fontProvider.register() 메소드를 사용하여 폰트를 등록해야 함
             * 해당 메소드 내부에서 경로처리를 하여 개발, 배포 시 폰트 파일을 찾을 수 있도록 함
             * */
            try {
                xmlWorkerFontProvider.register("static/font/AppleSDGothicNeoR.ttf\", \"AppleSDGothicNeo\");");
            } catch (Exception e) {
                throw new IllegalArgumentException("PDF 폰트 파일을 찾을 수 업습니다.");
            }
            if (xmlWorkerFontProvider.getRegisteredFonts() == null) {
                throw new IllegalArgumentException("PDF 폰트 파일을 찾을 수 없습니다.");
            }

            // 사용할 폰트를 담아두었던 내용을 CSSAppliersImpl에 담아 적용
            CssAppliers cssAppliers = new CssAppliersImpl(xmlWorkerFontProvider);

            // HTML Pipeline 생성
            HtmlPipelineContext htmlPipelineContext = new HtmlPipelineContext(cssAppliers);
            htmlPipelineContext.setTagFactory(Tags.getHtmlTagProcessorFactory());

            //================================================================================
            // Pipelines
            PdfWriterPipeline pdfWriterPipeline = new PdfWriterPipeline();
            HtmlPipeline pipeline = new HtmlPipeline(htmlPipelineContext, pdfWriterPipeline);
            CssResolverPipeline resolverPipeline = new CssResolverPipeline(cssResolver, pipeline);

            //================================================================================
            // XMLWorker
            XMLWorker xmlWorker = new XMLWorker(resolverPipeline, true);
            XMLParser xmlParser = new XMLParser(true, xmlWorker, StandardCharsets.UTF_8);

            /* HTML 내용을 담은 String 변수

            주의점
            1. HTML 태그는 반드시 닫아야 함
            2. xml 기준 html 태그 확인( ex : <p> </p> , <img/> , <col/> )
            위 조건을 지키지 않을 경우 DocumentException 발생
            */
            String htmlString = getHtml(domain.getPdfCode());

            // HTML 내용을 PDF 파일에 삽입
            StringReader stringReader = new StringReader(htmlString);

            // XML 파싱
            xmlParser.parse(stringReader);
            // PDF 문서 닫기
            document.close();
            // PDF Writer 닫기
            pdfWriter.close();

        } catch (DocumentException e1) {
            throw new IllegalArgumentException("PDF 라이브러리 설정 에러");
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            throw new IllegalArgumentException("PDF 파일 생성중 에러");
        } catch (IOException e3) {
            e3.printStackTrace();
            throw new IllegalArgumentException("PDF 파일 생성중 에러2");
        } catch (Exception e4) {
            e4.printStackTrace();
            throw new IllegalArgumentException("PDF 파일 생성중 에러3");
        } finally {
            try {
                document.close();
            } catch (Exception e) {
                System.out.println("PDF 파일 닫기 에러");
                e.printStackTrace();
            }
        }

    }

    // 사용할 HTML 코드를 가져오는 메소드
    private String getHtml(String code) {
        String return_html = "";

        switch (code) {
            case "oh" :
                return_html = "<html>" +
                        "<body>" +
                        "<h1>oh</h1>" +
                        "</body>" +
                        "</html>";
                break;
            case "tae" :
                return_html = "<html>" +
                        "<body>" +
                        "<h1>tae</h1>" +
                        "<p>CSS 테스트 입니다.</p>" +
                        "</body>" +
                        "</html>";
                break;
            case "hoon" :
                return_html = "<html>" +
                        "<body>" +
                        "<h1>hoon</h1>" +
                        "<p>이미지 테스트 합니다.</p>" +
                        "<img src='http://localhost:8080/images/test.png' />" +
                        "</body>" +
                        "</html>";
                break;
        }

        return return_html;
    }
}