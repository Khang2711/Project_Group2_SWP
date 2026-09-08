package utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import model.MovieRevenue;

/**
 * Tạo file Excel .xlsx đơn giản mà không cần thư viện bên ngoài.
 * File gồm phần tổng hợp doanh thu tháng và bảng doanh thu vé theo phim.
 */
public final class MonthlyReportExcelExporter {

    private MonthlyReportExcelExporter() {
    }

    public static void write(OutputStream output, YearMonth month,
            int movieCount, int orderCount, int ticketCount,
            float ticketRevenue, float comboRevenue, float totalRevenue,
            List<MovieRevenue> movieRevenueList) throws IOException {

        try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            putEntry(zip, "[Content_Types].xml", contentTypes());
            putEntry(zip, "_rels/.rels", rootRelationships());
            putEntry(zip, "xl/workbook.xml", workbookXml(month));
            putEntry(zip, "xl/_rels/workbook.xml.rels", workbookRelationships());
            putEntry(zip, "xl/styles.xml", stylesXml());
            putEntry(zip, "xl/worksheets/sheet1.xml",
                    worksheetXml(month, movieCount, orderCount, ticketCount,
                            ticketRevenue, comboRevenue, totalRevenue, movieRevenueList));
        }
    }

    private static void putEntry(ZipOutputStream zip, String name, String content) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zip.putNextEntry(entry);
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String contentTypes() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                + "</Types>";
    }

    private static String rootRelationships() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" "
                + "Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" "
                + "Target=\"xl/workbook.xml\"/>"
                + "</Relationships>";
    }

    private static String workbookXml(YearMonth month) {
        String sheetName = "Tháng " + String.format("%02d-%04d", month.getMonthValue(), month.getYear());
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                + "<sheets><sheet name=\"" + escapeXml(sheetName)
                + "\" sheetId=\"1\" r:id=\"rId1\"/></sheets>"
                + "</workbook>";
    }

    private static String workbookRelationships() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" "
                + "Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" "
                + "Target=\"worksheets/sheet1.xml\"/>"
                + "<Relationship Id=\"rId2\" "
                + "Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" "
                + "Target=\"styles.xml\"/>"
                + "</Relationships>";
    }

    private static String stylesXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<numFmts count=\"1\"><numFmt numFmtId=\"164\" "
                + "formatCode=\"#,##0 &quot;VNĐ&quot;\"/></numFmts>"
                + "<fonts count=\"4\">"
                + "<font><sz val=\"11\"/><name val=\"Calibri\"/><family val=\"2\"/></font>"
                + "<font><b/><color rgb=\"FFFFFFFF\"/><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><color rgb=\"FF172033\"/><sz val=\"18\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><color rgb=\"FF172033\"/><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                + "</fonts>"
                + "<fills count=\"4\">"
                + "<fill><patternFill patternType=\"none\"/></fill>"
                + "<fill><patternFill patternType=\"gray125\"/></fill>"
                + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFF97316\"/><bgColor indexed=\"64\"/></patternFill></fill>"
                + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFFFF7ED\"/><bgColor indexed=\"64\"/></patternFill></fill>"
                + "</fills>"
                + "<borders count=\"2\">"
                + "<border><left/><right/><top/><bottom/><diagonal/></border>"
                + "<border><left style=\"thin\"><color rgb=\"FFD1D5DB\"/></left>"
                + "<right style=\"thin\"><color rgb=\"FFD1D5DB\"/></right>"
                + "<top style=\"thin\"><color rgb=\"FFD1D5DB\"/></top>"
                + "<bottom style=\"thin\"><color rgb=\"FFD1D5DB\"/></bottom><diagonal/></border>"
                + "</borders>"
                + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                + "<cellXfs count=\"8\">"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                + "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyAlignment=\"1\"><alignment horizontal=\"center\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\" wrapText=\"1\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyAlignment=\"1\"><alignment vertical=\"center\" wrapText=\"1\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyAlignment=\"1\"><alignment horizontal=\"right\" vertical=\"center\"/></xf>"
                + "<xf numFmtId=\"164\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyNumberFormat=\"1\" applyAlignment=\"1\"><alignment horizontal=\"right\" vertical=\"center\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"3\" fillId=\"3\" borderId=\"1\" xfId=\"0\" applyAlignment=\"1\"><alignment vertical=\"center\"/></xf>"
                + "<xf numFmtId=\"164\" fontId=\"3\" fillId=\"3\" borderId=\"1\" xfId=\"0\" applyNumberFormat=\"1\" applyAlignment=\"1\"><alignment horizontal=\"right\" vertical=\"center\"/></xf>"
                + "</cellXfs>"
                + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
                + "</styleSheet>";
    }

    private static String worksheetXml(YearMonth month,
            int movieCount, int orderCount, int ticketCount,
            float ticketRevenue, float comboRevenue, float totalRevenue,
            List<MovieRevenue> movieRevenueList) {

        int detailHeaderRow = 15;
        int firstDataRow = detailHeaderRow + 1;
        int dataCount = movieRevenueList == null ? 0 : movieRevenueList.size();
        int totalRow = firstDataRow + dataCount;
        int lastRow = totalRow + 2;
        String period = String.format("Tháng %02d/%04d", month.getMonthValue(), month.getYear());
        String generatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        StringBuilder xml = new StringBuilder(12000);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        xml.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        xml.append("<dimension ref=\"A1:F").append(lastRow).append("\"/>");
        xml.append("<sheetViews><sheetView workbookViewId=\"0\">")
                .append("<pane ySplit=\"").append(detailHeaderRow)
                .append("\" topLeftCell=\"A").append(firstDataRow)
                .append("\" activePane=\"bottomLeft\" state=\"frozen\"/>")
                .append("</sheetView></sheetViews>");
        xml.append("<sheetFormatPr defaultRowHeight=\"18\"/>");
        xml.append("<cols>")
                .append("<col min=\"1\" max=\"1\" width=\"8\" customWidth=\"1\"/>")
                .append("<col min=\"2\" max=\"2\" width=\"16\" customWidth=\"1\"/>")
                .append("<col min=\"3\" max=\"3\" width=\"36\" customWidth=\"1\"/>")
                .append("<col min=\"4\" max=\"5\" width=\"18\" customWidth=\"1\"/>")
                .append("<col min=\"6\" max=\"6\" width=\"22\" customWidth=\"1\"/>")
                .append("</cols>");
        xml.append("<sheetData>");

        openRow(xml, 1, 28);
        inlineCell(xml, "A1", "BÁO CÁO DOANH THU F-CINEMA", 1);
        closeRow(xml);
        openRow(xml, 2, 21);
        inlineCell(xml, "A2", period, 1);
        closeRow(xml);
        openRow(xml, 3, 18);
        inlineCell(xml, "A3", "Thời gian xuất: " + generatedAt, 0);
        closeRow(xml);

        openRow(xml, 5, 22);
        inlineCell(xml, "A5", "TỔNG HỢP TRONG THÁNG", 2);
        closeRow(xml);
        summaryRow(xml, 6, "Phim phát sinh doanh thu", movieCount, false);
        summaryRow(xml, 7, "Đơn hàng thành công", orderCount, false);
        summaryRow(xml, 8, "Vé đã bán", ticketCount, false);
        summaryRow(xml, 9, "Doanh thu vé", ticketRevenue, true);
        summaryRow(xml, 10, "Doanh thu combo", comboRevenue, true);
        summaryRow(xml, 11, "Tổng doanh thu", totalRevenue, true);

        openRow(xml, 13, 18);
        inlineCell(xml, "A13", "Doanh thu theo phim chỉ tính tiền vé; doanh thu combo được tính trong phần tổng hợp.", 0);
        closeRow(xml);

        openRow(xml, detailHeaderRow, 30);
        inlineCell(xml, "A" + detailHeaderRow, "STT", 2);
        inlineCell(xml, "B" + detailHeaderRow, "Mã phim", 2);
        inlineCell(xml, "C" + detailHeaderRow, "Tên phim", 2);
        inlineCell(xml, "D" + detailHeaderRow, "Số đơn có phim", 2);
        inlineCell(xml, "E" + detailHeaderRow, "Số vé đã bán", 2);
        inlineCell(xml, "F" + detailHeaderRow, "Doanh thu vé", 2);
        closeRow(xml);

        int ticketTotal = 0;
        float movieRevenueTotal = 0;
        int row = firstDataRow;
        if (movieRevenueList != null) {
            int index = 1;
            for (MovieRevenue item : movieRevenueList) {
                openRow(xml, row, 22);
                numberCell(xml, "A" + row, index++, 4);
                inlineCell(xml, "B" + row, item.getMovieID(), 3);
                inlineCell(xml, "C" + row, item.getMovieTitle(), 3);
                numberCell(xml, "D" + row, item.getOrderCount(), 4);
                numberCell(xml, "E" + row, item.getTicketCount(), 4);
                numberCell(xml, "F" + row, item.getRevenue(), 5);
                closeRow(xml);
                ticketTotal += item.getTicketCount();
                movieRevenueTotal += item.getRevenue();
                row++;
            }
        }

        if (dataCount == 0) {
            openRow(xml, row, 22);
            inlineCell(xml, "A" + row, "Không có dữ liệu doanh thu trong tháng đã chọn.", 3);
            closeRow(xml);
            row++;
            totalRow = row;
        }

        openRow(xml, totalRow, 22);
        inlineCell(xml, "A" + totalRow, "TỔNG DOANH THU VÉ", 6);
        inlineCell(xml, "B" + totalRow, "", 6);
        inlineCell(xml, "C" + totalRow, "", 6);
        inlineCell(xml, "D" + totalRow, "", 6);
        numberCell(xml, "E" + totalRow, ticketTotal, 6);
        numberCell(xml, "F" + totalRow, movieRevenueTotal, 7);
        closeRow(xml);

        xml.append("</sheetData>");
        xml.append("<mergeCells count=\"5\">")
                .append("<mergeCell ref=\"A1:F1\"/>")
                .append("<mergeCell ref=\"A2:F2\"/>")
                .append("<mergeCell ref=\"A3:F3\"/>")
                .append("<mergeCell ref=\"A5:F5\"/>")
                .append("<mergeCell ref=\"A13:F13\"/>")
                .append("</mergeCells>");
        xml.append("<autoFilter ref=\"A").append(detailHeaderRow).append(":F")
                .append(Math.max(detailHeaderRow, totalRow - 1)).append("\"/>");
        xml.append("<pageMargins left=\"0.3\" right=\"0.3\" top=\"0.5\" bottom=\"0.5\" header=\"0.2\" footer=\"0.2\"/>");
        xml.append("</worksheet>");
        return xml.toString();
    }

    private static void summaryRow(StringBuilder xml, int row, String label, double value, boolean currency) {
        openRow(xml, row, 22);
        inlineCell(xml, "A" + row, label, 6);
        inlineCell(xml, "B" + row, "", 6);
        inlineCell(xml, "C" + row, "", 6);
        inlineCell(xml, "D" + row, "", 6);
        inlineCell(xml, "E" + row, "", 6);
        numberCell(xml, "F" + row, value, currency ? 7 : 6);
        closeRow(xml);
    }

    private static void openRow(StringBuilder xml, int row, int height) {
        xml.append("<row r=\"").append(row).append("\" ht=\"").append(height)
                .append("\" customHeight=\"1\">");
    }

    private static void closeRow(StringBuilder xml) {
        xml.append("</row>");
    }

    private static void inlineCell(StringBuilder xml, String reference, String value, int style) {
        xml.append("<c r=\"").append(reference).append("\" s=\"").append(style)
                .append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                .append(escapeXml(value == null ? "" : value))
                .append("</t></is></c>");
    }

    private static void numberCell(StringBuilder xml, String reference, double value, int style) {
        xml.append("<c r=\"").append(reference).append("\" s=\"").append(style)
                .append("\"><v>").append(formatNumber(value)).append("</v></c>");
    }

    private static String formatNumber(double value) {
        if (Math.rint(value) == value) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }

    private static String escapeXml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
