package com.honeywell.coreptdu.datatypes.copytailoreddata.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.honeywell.coreptdu.datatypes.copytailoreddata.dto.response.AllInHouseDataCopyReportDTO;
import com.honeywell.coreptdu.utils.dbutils.IApplication;
import com.honeywell.coreptdu.utils.oracleutils.GenericTemplateForm;
import com.honeywell.coreptdu.utils.oracleutils.Record;
import com.honeywell.coreptdu.utils.reportutils.IPdfReport;

@Service
public class AllInHouseDataCopyReport extends GenericTemplateForm<AllInHouseDataCopyReport> implements IPdfReport {
    @Autowired
    private IApplication app;

    private static final float MARGIN_BOTTOM = 70;
    public PDFont FONT = new PDType1Font(FontName.HELVETICA);
    public PDFont HEADER_FONT = new PDType1Font(FontName.HELVETICA_BOLD);
    public PDFont HEADER_OBLIQUE = new PDType1Font(FontName.HELVETICA_BOLD_OBLIQUE);
    // double widthInInches = 11; // A3 width
    // double heightInInches = 8.5; // A3 height
    int pageNumber = 1;

    private String title = "All In House Tailored data is copied from ";
    private Integer frCycle = 0;
    private Integer toCycle = 0;
    private String supplier = "";

    PDPageContentStream contentStream = null;
    PDDocument document = null;
    PDPage page = null;
    float currentHeight = 0;

    @Override
    public PDPageContentStream newPage(float height) throws IOException {
        if (currentHeight - height <= MARGIN_BOTTOM + 10) {
            onFooter();
            contentStream.close();
            // Create a new page
            PDPage newPage = new PDPage(new PDRectangle(72 * 8.5f, 72 * 11f));
            // PDPage newPage = new PDPage(new PDRectangle(PDRectangle.LETTER.getHeight(),
            // PDRectangle.LETTER.getWidth()));
            document.addPage(newPage);
            contentStream = new PDPageContentStream(document, newPage);
            pageNumber++;
            // Reset yPosition for the new page
            currentHeight = newPage.getMediaBox().getHeight();
            onHeader();
        }
        return contentStream;
    }

    @Override
    public void initializeReport() throws IOException {

        document = new PDDocument();
        // Create a page
        // float customwidth = 800;
        // float customHeight = PDRectangle.A4.getHeight();
        // Create a page
        page = new PDPage(new PDRectangle(72 * 8.5f, 72 * 11f));
        // page = new PDPage(new PDRectangle(PDRectangle.LETTER.getHeight(),
        // PDRectangle.LETTER.getWidth()));
        document.addPage(page);
        // create Content
        contentStream = new PDPageContentStream(document, page);
        float yStart = page.getMediaBox().getHeight();
        currentHeight = yStart;
    }

    @Override
    public void onHeader() throws IOException {

        resetHeight(10.5f);
        print(contentStream, page, "Data Supplier: " + supplier, 36f, 14, false);

        resetHeight(-7.5f);
        printCourier(contentStream, page, curentDate(), 418f, 10, true);
        resetHeight(7.5f);
        resetHeight(12f);
        print(contentStream, page, title + frCycle + " to " + toCycle, 68f, 16, true);

        // top Border
        resetHeight(18f);
        drawRowWithoutWrap(contentStream, 462.5f, 58.5f,
                new String[] { "Data Type", "From Cycle", "To Cycle" },
                new float[] { 68f, 329f, 450f }, 10);
        // resetHeight(3);
    }

    @Override
    public void onFooter() throws IOException {

        // Center the page number in the footer
        // String pageNumberText = "Page " + pageNumber;
        // float textWidth = FONT.getStringWidth(pageNumberText) / 1000 * 10;
        // float pageNumberX = (page.getMediaBox().getWidth() - textWidth) / 2;
        //
        // contentStream.beginText();
        // contentStream.newLineAtOffset(pageNumberX, MARGIN_BOTTOM);
        // contentStream.showText(pageNumberText);
        // contentStream.endText();
    }

    public byte[] genReport(String pSupplier, Integer fromCycle, Integer to_Cycle) throws SQLException, IOException {
        try {
            frCycle = fromCycle;
            toCycle = to_Cycle;
            supplier = pSupplier;
            String query = """
                                        select table_name,
                              decode(substr(table_name,5),'OVED_SAAAR_APPROACH',0,'OVED_SAAAR_SID',0,'OVED_SAAAR_STAR',0,'AIRPORT',1,'HELIPORT',2,'VHF',3,'RUNWAY',4,
                             'AIRPORT_WAYPOINT',5,'HELI_WAYPOINT',6,'ENROUTE_WAYPOINT',7,
                             'AIRPORT_NDB',8,'HELI_NDB',9,'ENROUTE_NDB',10,
                             'LOCALIZER',11,'HELI_LOCALIZER',12,'MLS',13,'HELI_MLS',14,'GLS',15,'MSA',16,'HELI_MSA',17,
                             'SID',18,'STAR',19,'APPROACH',20,'HELI_SID',21,'HELI_STAR',22,'HELI_APPROACH',23,
                             'AIRPORT_PATH_POINT',24,'HELI_PATH_POINT',25,
                             'ENROUTE_AIRWAY',26,'COMPANY_ROUTE',27,'PREFERRED_ROUTE',28,
                             'AIRPORT_COMM',29,'HELI_COMM',30,'FIR_UIR',31,'ENROUTE_COMM',32,
                             'RESTR_AIRSPACE',33,'CONTRL_AIRSPACE',34,'GRID_MORA',35,100) VREL_ORDER,
                              forms_utilities.count_tailored_records_report(table_name,?, substr(?,1,1))  nr_fr_records,
                     forms_utilities.count_tailored_records_report(table_name,?, substr(?,1,1))  nr_to_records
                    from   all_tab_columns
                    where  owner = 'CPT'
                    and    (table_name like '%SAAAR%' or
                           table_name like 'TLD%')
                    and    column_name = 'VALIDATE_IND'
                    and    (table_name not like '%LEG' and
                            table_name not like '%SEGMENT' and
                            table_name not like '%REPL_TIME' and
                            table_name not like '%VIA' and
                            table_name not like '%SEQ' and
                            table_name not like '%ADDL_ALT_DEST' and
                            table_name not like '%ALT_EXCL%' and
                            table_name not like '%REF_PREF_RTE' and
                            table_name not like '%NOTE_RESTR_NOTE')
                    order by 2
                                                    """;

            // Data for the report
            List<Record> recs = app.executeQuery(query, fromCycle, pSupplier, toCycle, pSupplier);
            List<AllInHouseDataCopyReportDTO> inr = new ArrayList<>();
            for (Record rec : recs) {
                AllInHouseDataCopyReportDTO cr = app.mapResultSetToClass(rec, AllInHouseDataCopyReportDTO.class);
                if (cr.getTableName() != null && cr.getNrToRecords() != 0) {
                    cr.setTableName(decodeTableName(cr.getTableName()));
                    inr.add(cr);
                }
            }

            initializeReport();
            onHeader();
            if (inr.size() > 0) {
                System.out.println("Loop start completion for row " + LocalDate.now());
                for (AllInHouseDataCopyReportDTO innerAllInhouseDcr : inr) {
                    drawRowWithWrap(contentStream, 462.5f, 58.5f,
                            new String[] { innerAllInhouseDcr.getTableName(),
                                    String.valueOf(innerAllInhouseDcr.getNrFrRecords()),
                                    String.valueOf(innerAllInhouseDcr.getNrToRecords()) },
                            new float[] { 68f, 347f, 481f, 481f },
                            new String[] { "start", "start", "end" }, 10);
                }
                System.out.println("Loop end completion for row " + LocalDate.now());
            }
            onFooter();
            System.out.println("before try block " + LocalDate.now());
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

                // Close content stream and save document
                contentStream.close();
                document.save(byteArrayOutputStream);
                // ByteArrayInputStream inputStream = new
                // ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                // InputStreamResource resource = new InputStreamResource(inputStream);

                // HttpHeaders headers = new HttpHeaders();
                // headers.add(HttpHeaders.CONTENT_DISPOSITION,
                // "attachment;filename=generated.pdf");

                // Convert the output stream to a byte array
                byte[] pdfBytes = byteArrayOutputStream.toByteArray();

                return pdfBytes; // Return as byte array
                // return
                // ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).contentLength(byteArrayOutputStream.size()).body(resource);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error generating PDF", e); // Handle the exception properly
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating PDF", e); // Handle the exception properly
        }

    }

    private String curentDate() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yy hh:mm a");
        String formattedDateTime = now.format(formatter).toUpperCase();
        return formattedDateTime;
    }

    // coverity-fix
    // private String getDateString(Date dt) {
    // // Create SimpleDateFormat for the desired format
    // SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yy HH:mm");
    // String formattedDateTime = formatter.format(dt).toUpperCase();
    // return formattedDateTime;
    // }

    private void resetHeight(float height) throws IOException {

        currentHeight -= height;
    }

    public void print(PDPageContentStream contentStream, PDPage page, String text, float x, float fontSize,
            boolean isbold) throws IOException {

    	PDPageContentStream      localContentStream = newPage(1 * (fontSize + 2));
        if (!isbold)
        	localContentStream.setFont(FONT, fontSize);
        else
        	localContentStream.setFont(HEADER_FONT, fontSize);

        localContentStream.beginText();
        localContentStream.newLineAtOffset(x, currentHeight - fontSize + 2);
        localContentStream.showText(text);
        localContentStream.endText();

        currentHeight -= fontSize + 2;
    }

    public void outputTo(PDPageContentStream contentStream, PDDocument document, String fileName) throws IOException {
        // Save the document
        contentStream.close();
        document.save(fileName);
    }

    public void drawRowWithWrap(PDPageContentStream contentStream, float x, float offset, String[] columns,
            float[] columnSizes, String[] alignment, int fontSize)
            throws IOException {
        int i = 0;
        float vheight = 0f;
        contentStream.setFont(FONT, fontSize);

        List<List<String>> texts = new ArrayList<>();
        for (String str : columns) {

            List<String> lines = wrapText(str, FONT, fontSize, columnSizes[i + 1] - columnSizes[i]);
            if (vheight < (lines.size() * (fontSize + 2))) {
                vheight = lines.size() * (fontSize + 2);
            }
            texts.add(lines);
        }
        // buffer space
        vheight += 2f;
        contentStream = newPage(vheight);
        contentStream.setFont(FONT, fontSize);
        // Draw the top and bottom horizontal borders
        contentStream.moveTo(offset, currentHeight); // Top border
        contentStream.lineTo(offset + x, currentHeight);
        contentStream.stroke();
        int col = 0;
        for (List<String> lines : texts) {
            float columnWidth = columnSizes[col + 1] - columnSizes[col];
            int lineno = 1;
            for (String line : lines) {
                float lineWidth = FONT.getStringWidth(line) / 1000 * fontSize;

                // Calculate alignment offset
                float textOffsetX = 0;
                switch (alignment[col]) {
                    case "center":
                        textOffsetX = (columnWidth - lineWidth) / 2;
                        break;
                    case "end":
                        textOffsetX = columnWidth - lineWidth;
                        break;
                    case "start":
                    default:
                        textOffsetX = 0;
                        break;
                }
                contentStream.beginText();
                contentStream.newLineAtOffset(columnSizes[col] + textOffsetX, currentHeight - (lineno * (fontSize)));
                contentStream.showText(line);
                contentStream.endText();
                lineno++;
            }
            col++;
        }

        // Draw vertical borders at the start and end of the row
        contentStream.moveTo(offset, currentHeight);
        contentStream.lineTo(offset, currentHeight - vheight); // Left vertical border
        contentStream.stroke();

        // Draw vertical borders at the start and end of the row
        contentStream.moveTo(offset + x, currentHeight);
        contentStream.lineTo(offset + x, currentHeight - vheight); // Right vertical border
        contentStream.stroke();

        // Draw the top and bottom horizontal borders
        contentStream.moveTo(offset, currentHeight - vheight); // Top border
        contentStream.lineTo(offset + x, currentHeight - vheight);
        contentStream.stroke();

        currentHeight -= vheight;
    }

    public void drawRowWithoutWrap(PDPageContentStream contentStream, float x, float offset, String[] columns,
            float[] columnSizes, int fontSize)
            throws IOException {
        int i = 0;
        float vheight = fontSize + 2;

        PDPageContentStream   localcontentStream = newPage(vheight);
        localcontentStream.setFont(HEADER_OBLIQUE, fontSize);
        // Draw the top and bottom horizontal borders
        localcontentStream.moveTo(offset, currentHeight + 5.5f); // Top border
        localcontentStream.lineTo(offset + x, currentHeight + 5.5f);// added extra 10f for increasing extra row height
        localcontentStream.stroke();
        for (String str : columns) {
        	localcontentStream.setFont(HEADER_OBLIQUE, fontSize);
        	localcontentStream.beginText();
        	localcontentStream.newLineAtOffset(columnSizes[i], currentHeight - (vheight * 3 / 4) + 1.5f);
        	localcontentStream.showText(str);
        	localcontentStream.endText();
            i++;
        }
        resetHeight(5f);

        // Draw vertical borders at the start and end of the row
        localcontentStream.moveTo(offset, currentHeight - 12.5f);
        localcontentStream.lineTo(offset, currentHeight + 10.5f); // Left vertical border
        localcontentStream.stroke();

        // Draw vertical borders at the start and end of the row
        localcontentStream.moveTo(offset + x, currentHeight - 12.5f);
        localcontentStream.lineTo(offset + x, currentHeight + 10.5f); // Right vertical border
        localcontentStream.stroke();

        // Draw the top and bottom horizontal borders
        localcontentStream.moveTo(offset, currentHeight - vheight); // Top border
        localcontentStream.lineTo(offset + x, currentHeight - vheight);
        localcontentStream.stroke();

        currentHeight -= vheight;
    }

    private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        float spaceWidth = font.getStringWidth(" ") / 1000 * fontSize;

        for (String word : text.split(" ")) {
            float currentLineWidth = font.getStringWidth(currentLine + word) / 1000 * fontSize;
            if (currentLineWidth + spaceWidth > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word); // Start a new line
            } else {
                if (currentLine.length() > 0)
                    currentLine.append(" ");
                currentLine.append(word);
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    // public ResponseEntity<InputStreamResource> generateReort(){
    // try {
    //// custDcr = dcrno;
    // // Group Generation
    // String query = """
    // select table_name,
    // decode(substr(table_name,5),'OVED_SAAAR_APPROACH',0,'OVED_SAAAR_SID',0,'OVED_SAAAR_STAR',0,'AIRPORT',1,'HELIPORT',2,'VHF',3,'RUNWAY',4,
    // 'AIRPORT_WAYPOINT',5,'HELI_WAYPOINT',6,'ENROUTE_WAYPOINT',7,
    // 'AIRPORT_NDB',8,'HELI_NDB',9,'ENROUTE_NDB',10,
    // 'LOCALIZER',11,'HELI_LOCALIZER',12,'MLS',13,'HELI_MLS',14,'GLS',15,'MSA',16,'HELI_MSA',17,
    // 'SID',18,'STAR',19,'APPROACH',20,'HELI_SID',21,'HELI_STAR',22,'HELI_APPROACH',23,
    // 'AIRPORT_PATH_POINT',24,'HELI_PATH_POINT',25,
    // 'ENROUTE_AIRWAY',26,'COMPANY_ROUTE',27,'PREFERRED_ROUTE',28,
    // 'AIRPORT_COMM',29,'HELI_COMM',30,'FIR_UIR',31,'ENROUTE_COMM',32,
    // 'RESTR_AIRSPACE',33,'CONTRL_AIRSPACE',34,'GRID_MORA',35,100) VREL_ORDER,
    // forms_utilities.count_tailored_records_report(table_name,?, substr(?,1,1))
    // nr_fr_records,
    // forms_utilities.count_tailored_records_report(table_name,?, substr(?,1,1))
    // nr_to_records
    // from all_tab_columns
    // where owner = 'CPT'
    // and (table_name like '%SAAAR%' or
    // table_name like 'TLD%')
    // and column_name = 'VALIDATE_IND'
    // and (table_name not like '%LEG' and
    // table_name not like '%SEGMENT' and
    // table_name not like '%REPL_TIME' and
    // table_name not like '%VIA' and
    // table_name not like '%SEQ' and
    // table_name not like '%ADDL_ALT_DEST' and
    // table_name not like '%ALT_EXCL%' and
    // table_name not like '%REF_PREF_RTE' and
    // table_name not like '%NOTE_RESTR_NOTE')
    // order by 2
    // """;
    //
    // // Data for the report
    // List<Record> recs = app.executeQuery(query, 202411, "J", 202412,"J");
    // List<AllInHouseDataCopyReportDTO> inr = new ArrayList<>();
    // for (Record rec : recs) {
    // AllInHouseDataCopyReportDTO cr = app.mapResultSetToClass(rec,
    // AllInHouseDataCopyReportDTO.class);
    // if(cr.getTableName()!= null && cr.getNrToRecords()!=0) {
    // inr.add(cr);
    // }
    // }
    //
    //// Map<String, Map<String, List<CustDcrReportDto>>> grouped = inr.stream()
    //// .collect(
    //// Collectors.groupingBy(
    //// CustDcrReportDto::getType,
    //// () -> new TreeMap<>(Comparator.naturalOrder()),
    //// Collectors.groupingBy(
    //// CustDcrReportDto::getTableName,
    //// () -> new TreeMap<>(Comparator.naturalOrder()),
    //// Collectors.mapping(
    //// r -> r,
    //// Collectors.toList()))));
    // initializeReport();
    // onHeader();
    // if(true){
    //// for (String type : grouped.keySet()) {
    //// resetHeight(4f);
    //// print(contentStream, page, type, 15.03792f, 10, true);
    ////
    //// for (String tableName : grouped.get(type).keySet()) {
    //// resetHeight(4f);
    //// print(contentStream, page, tableName, 32.756f, 10, false);
    // for (AllInHouseDataCopyReportDTO innerAllInhouseDcr : inr) {
    // drawRowWithWrap(contentStream, 704.25f, 42.75f,
    // new String[] { innerAllInhouseDcr.getTableName(),
    // String.valueOf(innerAllInhouseDcr.getNrFrRecords()),
    // String.valueOf(innerAllInhouseDcr.getNrToRecords()) },
    // new float[] { 63.01728f, 556.49736f, 649.50264f, 739.43208f },
    // new String[] { "start", "center", "center" }, 10);
    // }
    //// }
    //// }
    // }else{
    // resetHeight(15f);
    // print(contentStream, page, "There is no modification under this DCR.",
    // 312.00264f, 14, false);
    // }
    // onFooter();
    // try (ByteArrayOutputStream byteArrayOutputStream = new
    // ByteArrayOutputStream()) {
    //
    // // Close content stream and save document
    // contentStream.close();
    // document.save(byteArrayOutputStream);
    // ByteArrayInputStream inputStream = new
    // ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    // InputStreamResource resource = new InputStreamResource(inputStream);
    //
    // HttpHeaders headers = new HttpHeaders();
    // headers.add(HttpHeaders.CONTENT_DISPOSITION,
    // "attachment;filename=generated.pdf");
    //
    // // Convert the output stream to a byte array
    // // byte[] pdfBytes = byteArrayOutputStream.toByteArray();
    //
    // // return pdfBytes; // Return as byte array
    // return
    // ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).contentLength(byteArrayOutputStream.size()).body(resource);
    // } catch (Exception e) {
    // e.printStackTrace();
    // throw new RuntimeException("Error generating PDF", e); // Handle the
    // exception properly
    // }
    //
    // } catch (Exception e) {
    // e.getStackTrace();
    // return null;
    // }
    // }
    //
    public String decodeTableName(String pTable) throws Exception {
        // log.info("decodeTableName Executing");
        try {
            String vReturn = null;

            if (Arrays.asList("STD_SID", "TLD_SID").contains(pTable)) {
                vReturn = "TLD AIRPORT_SID";

            }

            else if (Arrays.asList("STD_STAR", "TLD_STAR").contains(pTable)) {
                vReturn = "TLD AIRPORT_STAR";

            }

            else if (Arrays.asList("STD_APPROACH", "TLD_APPROACH").contains(pTable)) {
                vReturn = "TLD AIRPORT_APPROACH";

            } else if (like("%SAAAR_APPROACH", pTable)) {
                vReturn = "APPROVED_SAAAR_APPROACH";
            }

            else if (like("%SAAAR_SID", pTable)) {
                vReturn = "APPROVED_SAAAR_SID";
            } else if (like("%SAAAR_STAR", pTable)) {
                vReturn = "APPROVED_SAAAR_STAR";
            }

            else if (Arrays.asList("STD_LOCALIZER", "TLD_LOCALIZER").contains(pTable)) {
                vReturn = "TLD AIRPORT_LOCALIZER";

            }

            else if (Arrays.asList("STD_MLS", "TLD_MLS").contains(pTable)) {
                vReturn = "TLD AIRPORT_MLS";

            }

            else if (Arrays.asList("STD_MSA", "TLD_MSA").contains(pTable)) {
                vReturn = "TLD AIRPORT_MSA";

            }

            else if (Arrays.asList("STD_LOCALIZER_MARKER", "TLD_LOCALIZER_MARKER").contains(pTable)) {
                vReturn = "";

            }

            else if (Arrays.asList("STD_SUPPL_ARPT_NDB", "TLD_SUPPL_ARPT_NDB").contains(pTable)) {
                vReturn = "TLD_SUPPL_AIRPORT_NDB";

            }

            else {
                vReturn = "TLD " + substr(toString(pTable), 5);

            }

            return vReturn;

        } catch (Exception e) {
            throw e;

        }
    }

    public void printCourier(PDPageContentStream contentStream, PDPage page, String text, float x, float fontSize,
            boolean isbold) throws IOException {

    	PDPageContentStream   localContentStream = newPage(1 * (fontSize + 2));
        if (!isbold)
        	localContentStream.setFont(new PDType1Font(FontName.COURIER), fontSize);
        else
        	localContentStream.setFont(new PDType1Font(FontName.COURIER_BOLD), fontSize);

        localContentStream.beginText();
        localContentStream.newLineAtOffset(x, currentHeight - fontSize + 2);
        localContentStream.showText(text);
        localContentStream.endText();

        currentHeight -= fontSize + 2;
    }

}