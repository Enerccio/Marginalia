package com.github.enerccio.marginalia.utils.docimport;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * EPUB books and the zips of an Obsidian vault and of a Scrivener project.
 */
class ProjectFormatsTest {

    private static byte[] zip(String... namesAndTexts) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (int i = 0; i < namesAndTexts.length; i += 2) {
                zip.putNextEntry(new ZipEntry(namesAndTexts[i]));
                zip.write(namesAndTexts[i + 1].getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return out.toByteArray();
    }

    private static List<String> lines(TextDocument document) {
        return document.blocks().stream().map(block -> (block.isHeading() ? "#".repeat(block.level()) + " " : "") + block.text()).toList();
    }

    private static final String CONTAINER = """
            <?xml version="1.0"?>
            <container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
              <rootfiles><rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/></rootfiles>
            </container>""";

    private static final String OPF = """
            <?xml version="1.0"?>
            <package xmlns="http://www.idpf.org/2007/opf" version="2.0">
              <metadata xmlns:dc="http://purl.org/dc/elements/1.1/"><dc:title>My Book</dc:title></metadata>
              <manifest>
                <item id="ncx" href="toc.ncx" media-type="application/x-dtbncx+xml"/>
                <item id="cover" href="cover.xhtml" media-type="application/xhtml+xml"/>
                <item id="c1" href="text/ch%201.xhtml" media-type="application/xhtml+xml"/>
                <item id="c2" href="text/ch2.xhtml" media-type="application/xhtml+xml"/>
              </manifest>
              <spine toc="ncx"><itemref idref="cover"/><itemref idref="c1"/><itemref idref="c2"/></spine>
              <guide><reference type="cover" href="cover.xhtml"/></guide>
            </package>""";

    private static final String NCX = """
            <?xml version="1.0"?>
            <ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1">
              <navMap>
                <navPoint id="a"><navLabel><text>Chapter One</text></navLabel><content src="text/ch%201.xhtml"/></navPoint>
                <navPoint id="b"><navLabel><text>Chapter Two</text></navLabel><content src="text/ch2.xhtml#start"/></navPoint>
              </navMap>
            </ncx>""";

    private static byte[] epub() throws Exception {
        return zip("mimetype", "application/epub+zip",
                "META-INF/container.xml", CONTAINER,
                "OEBPS/content.opf", OPF,
                "OEBPS/toc.ncx", NCX,
                "OEBPS/cover.xhtml", "<html><body><p>Cover text that is not read</p></body></html>",
                "OEBPS/text/ch 1.xhtml", """
                        <?xml version="1.0"?>
                        <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
                        <html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops"><head><title>x</title><style>p {}</style></head>
                        <body><h2>Chapter One</h2>
                        <p>Hello <em>big</em> world &amp; more&nbsp;text<a epub:type="noteref" href="#n1">1</a>.</p>
                        <p>Second <span style="font-weight: bold">bold</span> line<br/>after break.</p>
                        <hr/><p>After the scene break.</p>
                        <aside epub:type="footnote" id="n1"><p>A note that is left out</p></aside></body></html>""",
                "OEBPS/text/ch2.xhtml", "<html><body><p>Chapter Two</p><p>No heading in this page.</p><p>Last words.</p></body></html>");
    }

    @Test
    void epubGivesTheSpineWithHeadingsEmphasisAndTheTitle() throws Exception {
        TextDocument document = DocumentFormat.of("Book.EPUB").read(epub());

        assertThat(document.title()).isEqualTo("My Book");
        assertThat(lines(document)).containsExactly(
                "## Chapter One",
                "Hello *big* world & more text.",
                "Second **bold** line  \nafter break.",
                "\\* \\* \\*",
                "After the scene break.",
                // the page has no heading: the table of contents names it, and its first paragraph was that name
                "# Chapter Two",
                "No heading in this page.",
                "Last words.");
        assertThat(DocumentFormat.of("a.epub").baseName("a.epub")).isEqualTo("a");
    }

    @Test
    void anEpubThatIsNotOneIsRefused() throws Exception {
        assertThatThrownBy(() -> DocumentFormat.EPUB.read("not a zip".getBytes(StandardCharsets.UTF_8))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DocumentFormat.EPUB.read(zip("a.txt", "x"))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anObsidianVaultIsChaptersAndPartsInNaturalOrder() throws Exception {
        TextDocument document = DocumentFormat.vault(zip(
                "Vault/02 Next.md", "## Scene\n\nSee [[Intro|the start]] and [[Place#Heading]] ==marked== ![[map.png]] ^abc\n\n%%hidden\ncomment%%Visible.",
                "Vault/01 Intro.md", "---\ntitle: The Beginning\n---\nFirst words.",
                "Vault/Part 1/Chapter 10.md", "Ten.",
                "Vault/Part 1/Chapter 2.md", "Two.",
                "Vault/Part 1/Empty.md", "   ",
                "Vault/.obsidian/hidden.md", "Not read.",
                "Vault/map.png", "binary"));

        assertThat(document.title()).isNull();
        assertThat(lines(document)).containsExactly(
                "# The Beginning",
                "First words.",
                "# 02 Next",
                "## Scene",
                "See the start and Place Heading marked",
                "Visible.",
                "# Part 1",
                "## Chapter 2",
                "Two.",
                "## Chapter 10",
                "Ten.");
        assertThatThrownBy(() -> DocumentFormat.vault(zip("Vault/.obsidian/x.md", "x"))).isInstanceOf(IllegalArgumentException.class);
    }

    private static String scrivx() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <ScrivenerProject Version="2.0">
                  <Binder>
                    <BinderItem UUID="DRAFT" Type="DraftFolder"><Title>Draft</Title>
                      <Children>
                        <BinderItem UUID="CH1" Type="Folder"><Title>The Storm</Title>
                          <Children>
                            <BinderItem UUID="S1" Type="Text"><Title>Scene one</Title></BinderItem>
                            <BinderItem UUID="S2" Type="Text"><Title>Scene two</Title></BinderItem>
                            <BinderItem UUID="S3" Type="Text"><Title>Left out</Title><MetaData><IncludeInCompile>No</IncludeInCompile></MetaData></BinderItem>
                          </Children>
                        </BinderItem>
                        <BinderItem UUID="EP" Type="Text"><Title>Epilogue</Title></BinderItem>
                      </Children>
                    </BinderItem>
                    <BinderItem UUID="RES" Type="ResearchFolder"><Title>Research</Title>
                      <Children><BinderItem UUID="R1" Type="Text"><Title>Notes</Title></BinderItem></Children></BinderItem>
                  </Binder>
                </ScrivenerProject>""";
    }

    private static String rtf(String text) {
        return "{\\rtf1\\ansi\\ansicpg1252 " + text + "\\par}";
    }

    @Test
    void aScrivenerProjectIsItsManuscript() throws Exception {
        TextDocument document = DocumentFormat.scrivener(zip(
                "My Novel.scriv/My Novel.scrivx", scrivx(),
                "My Novel.scriv/Files/Data/S1/content.rtf", rtf("First scene."),
                "My Novel.scriv/Files/Data/S2/content.rtf", rtf("Second scene."),
                "My Novel.scriv/Files/Data/S3/content.rtf", rtf("Excluded."),
                "My Novel.scriv/Files/Data/EP/content.rtf", rtf("The end."),
                "My Novel.scriv/Files/Data/R1/content.rtf", rtf("Research."),
                "__MACOSX/My Novel.scriv/._My Novel.scrivx", "junk"));

        assertThat(document.title()).isEqualTo("My Novel");
        assertThat(lines(document)).containsExactly(
                "# The Storm",
                "First scene.",
                "\\* \\* \\*",
                "Second scene.",
                "# Epilogue",
                "The end.");
    }

    @Test
    void aScrivener2ProjectNamesItsFilesByNumber() throws Exception {
        String scrivx = """
                <ScrivenerProject Version="1.0"><Binder>
                  <BinderItem ID="0" Type="DraftFolder"><Title>Draft</Title><Children>
                    <BinderItem ID="5" Type="Text"><Title>One</Title></BinderItem>
                  </Children></BinderItem></Binder></ScrivenerProject>""";
        TextDocument document = DocumentFormat.scrivener(zip("N.scrivx", scrivx, "Files/Docs/5.rtf", rtf("Words here.")));
        assertThat(lines(document)).containsExactly("# One", "Words here.");
        assertThatThrownBy(() -> DocumentFormat.scrivener(zip("a.txt", "x"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DocumentFormat.scrivener(zip("N.scrivx", scrivx))).isInstanceOf(IllegalArgumentException.class);
    }
}
