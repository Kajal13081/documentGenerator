package org.example.backend;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public abstract class ExportOptions implements Exporter{

    public static class MdExporter implements Exporter {
        @Override
        public void export(String path, String content) throws IOException {
            if (!path.endsWith(".md")) {
                path += ".md";
            }
            Files.writeString(Paths.get(path), content);
        }
    }

    public static class TxtExporter implements Exporter {
        @Override
        public void export(String path, String content) throws IOException {
            if (!path.endsWith(".txt")) {
                path += ".txt";
            }
            Files.writeString(Paths.get(path), content);
        }
    }

    public static class PdfExporter implements Exporter {
        @Override
        public void export(String path, String content) throws IOException {
            if (!path.endsWith(".pdf")) {
                path += ".pdf";
            }
            String asciiOnly = content.replaceAll("[^\\x20-\\x7E\\n]", "").replace("\t", "   ");

            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.LETTER);
                document.addPage(page);
                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(25, 700);

                String[] lines = asciiOnly.split("\n");
                float yPosition = 700;
                for (String line : lines) {
                    if (yPosition <= 50) {  // Check if the current position is near the bottom of the page
                        contentStream.endText();
                        contentStream.close();
                        page = new PDPage(PDRectangle.LETTER);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        contentStream.beginText();
                        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                        contentStream.setLeading(14.5f);
                        contentStream.newLineAtOffset(25, 700);
                        yPosition = 700;
                    }
                    contentStream.showText(line);
                    contentStream.newLine();
                    yPosition -= 14.5f;  // Adjust yPosition based on leading
                }

                contentStream.endText();
                contentStream.close();

                document.save(path);
            }
        }
    }

    public static class DocExporter implements Exporter {
        @Override
        public void export(String path, String content) throws IOException {
            if (!path.endsWith(".docx")) {
                path += ".docx";
            }

            try (XWPFDocument doc = new XWPFDocument()) {
                String[] lines = content.split("\n");
                for (String line : lines) {
                    XWPFParagraph p = doc.createParagraph();
                    p.createRun().setText(line);
                }

                try (FileOutputStream out = new FileOutputStream(path)) {
                    doc.write(out);
                }
            }
        }
    }

    public static class HtmlExporter implements Exporter {
        @Override
        public void export(String path, String content) throws IOException {
            if (!path.endsWith(".html")) {
                path += ".html";
            }

            // Wrap content in basic HTML tags and escape special characters for safety
            String htmlContent = "<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n"
                    + "<title>Documentation Export</title>\n"
                    + "<style> pre { font-family: Consolas, monospace; white-space: pre-wrap; } </style>\n"
                    + "</head>\n<body>\n<pre>"
                    + escapeHtml(content)
                    + "</pre>\n</body>\n</html>";

            Files.writeString(Paths.get(path), htmlContent);
        }

        private static String escapeHtml(String s) {
            return s.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
        }
    }

    public static class JsonExporter implements Exporter {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public void export(String path, String content) throws IOException {
            if (!path.endsWith(".json")) {
                path += ".json";
            }

            Map<String, String> jsonMap = Map.of("documentation", content);

            // Write JSON to file
            try (FileWriter writer = new FileWriter(path)) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, jsonMap);
            }
        }
    }
}
