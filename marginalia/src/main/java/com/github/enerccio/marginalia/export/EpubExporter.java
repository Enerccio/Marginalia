package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * EPUB 3, which is a zip of XHTML files, so there is no library needed.
 */
public class EpubExporter extends ExporterBase {
    // readers load a part as a whole, so the text is split to parts of about this size
    private static final int PART_SIZE = 100_000;

    private static final String STYLE = """
            body { font-family: serif; line-height: 1.5; margin: 5%; }
            p { margin: 0 0 1em 0; text-align: justify; }
            h1, h2, h3, h4, h5, h6 { margin: 1.5em 0 1em 0; }
            blockquote { margin: 1em 0 1em 1.5em; padding-left: 1em; border-left: 3px solid #999; }
            pre, code { font-family: monospace; }
            pre { white-space: pre-wrap; }
            hr { border: 0; text-align: center; margin: 2em 0; }
            hr::after { content: '* * *'; }
            .title-page { margin-top: 30%; text-align: center; }
            .title-page h1 { font-size: 2.4em; }
            .title-page p { text-align: center; font-size: 1.3em; }
            """;

    @Override
    public String getName() {
        return "EPUB";
    }

    @Override
    public String getFileExtension() {
        return "epub";
    }

    @Override
    public String getMimeType() {
        return "application/epub+zip";
    }

    @Override
    protected ExportWriter open(ExportOptions options) {
        return new EpubWriter(options);
    }

    private static class EpubWriter implements ExportWriter {
        private final ExportOptions options;
        private final List<String> parts = new ArrayList<>();
        private final StringBuilder current = new StringBuilder();
        private String titlePage;

        private EpubWriter(ExportOptions options) {
            this.options = options;
        }

        @Override
        public void titlePage(String markdown) {
            titlePage = "<div class=\"title-page\">\n" + toHtml(markdown) + "</div>\n";
        }

        @Override
        public void message(String markdown) {
            current.append(toHtml(markdown));
            if (current.length() >= PART_SIZE) {
                parts.add(current.toString());
                current.setLength(0);
            }
        }

        @Override
        public byte[] finish() throws Exception {
            if (!current.isEmpty() || parts.isEmpty()) {
                parts.add(current.toString());
            }
            String language = StringUtils.defaultIfBlank(options.language(), "en");

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
                // the mimetype must be the first entry and must not be compressed
                byte[] mimetype = "application/epub+zip".getBytes(StandardCharsets.US_ASCII);
                ZipEntry mimetypeEntry = new ZipEntry("mimetype");
                mimetypeEntry.setMethod(ZipEntry.STORED);
                mimetypeEntry.setSize(mimetype.length);
                mimetypeEntry.setCompressedSize(mimetype.length);
                CRC32 crc = new CRC32();
                crc.update(mimetype);
                mimetypeEntry.setCrc(crc.getValue());
                zip.putNextEntry(mimetypeEntry);
                zip.write(mimetype);
                zip.closeEntry();

                add(zip, "META-INF/container.xml", """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
                          <rootfiles>
                            <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
                          </rootfiles>
                        </container>
                        """);
                add(zip, "OEBPS/style.css", STYLE);

                List<String> files = new ArrayList<>();
                List<String> labels = new ArrayList<>();
                if (titlePage != null) {
                    files.add("title.xhtml");
                    labels.add(options.bookName());
                    add(zip, "OEBPS/title.xhtml", page(language, options.bookName(), titlePage));
                }
                for (int i = 0; i < parts.size(); i++) {
                    String file = "part" + (i + 1) + ".xhtml";
                    files.add(file);
                    labels.add(parts.size() == 1 ? options.bookName() : options.bookName() + " (" + (i + 1) + ")");
                    add(zip, "OEBPS/" + file, page(language, options.bookName(), parts.get(i)));
                }

                add(zip, "OEBPS/nav.xhtml", navigation(language, files, labels));
                add(zip, "OEBPS/content.opf", packageDocument(language, files));
            }
            return bytes.toByteArray();
        }

        private static void add(ZipOutputStream zip, String name, String content) throws Exception {
            zip.putNextEntry(new ZipEntry(name));
            zip.write(content.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }

        private static String page(String language, String title, String body) {
            return """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <!DOCTYPE html>
                    <html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops" lang="%1$s" xml:lang="%1$s">
                    <head>
                    <meta charset="utf-8"/>
                    <title>%2$s</title>
                    <link rel="stylesheet" type="text/css" href="style.css"/>
                    </head>
                    <body>
                    %3$s</body>
                    </html>
                    """.formatted(escapeXml(language), escapeXml(title), body);
        }

        private String navigation(String language, List<String> files, List<String> labels) {
            StringBuilder items = new StringBuilder();
            for (int i = 0; i < files.size(); i++) {
                items.append("<li><a href=\"").append(files.get(i)).append("\">").append(escapeXml(labels.get(i))).append("</a></li>\n");
            }
            return """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <!DOCTYPE html>
                    <html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops" lang="%1$s" xml:lang="%1$s">
                    <head>
                    <meta charset="utf-8"/>
                    <title>%2$s</title>
                    </head>
                    <body>
                    <nav epub:type="toc" id="toc">
                    <ol>
                    %3$s</ol>
                    </nav>
                    </body>
                    </html>
                    """.formatted(escapeXml(language), escapeXml(options.bookName()), items);
        }

        private String packageDocument(String language, List<String> files) {
            StringBuilder manifest = new StringBuilder();
            StringBuilder spine = new StringBuilder();
            for (int i = 0; i < files.size(); i++) {
                manifest.append("<item id=\"c").append(i).append("\" href=\"").append(files.get(i))
                        .append("\" media-type=\"application/xhtml+xml\"/>\n");
                spine.append("<itemref idref=\"c").append(i).append("\"/>\n");
            }
            String author = StringUtils.isBlank(options.author()) ? "" :
                    "<dc:creator>" + escapeXml(options.author().trim()) + "</dc:creator>\n";
            String modified = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
            return """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <package xmlns="http://www.idpf.org/2007/opf" version="3.0" unique-identifier="book-id" xml:lang="%1$s">
                    <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
                    <dc:identifier id="book-id">urn:uuid:%2$s</dc:identifier>
                    <dc:title>%3$s</dc:title>
                    %4$s<dc:language>%1$s</dc:language>
                    <meta property="dcterms:modified">%5$s</meta>
                    </metadata>
                    <manifest>
                    <item id="nav" href="nav.xhtml" media-type="application/xhtml+xml" properties="nav"/>
                    <item id="css" href="style.css" media-type="text/css"/>
                    %6$s</manifest>
                    <spine>
                    %7$s</spine>
                    </package>
                    """.formatted(escapeXml(language), UUID.randomUUID(), escapeXml(options.bookName()), author, modified, manifest, spine);
        }
    }
}
