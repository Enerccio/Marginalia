# Fonts of the PDF export

`NotoSansJP-Regular-subset.ttf` is the font the PDF export (`export/PdfExporter`) draws the characters in that the Liberation fonts do not have
(Japanese kana and kanji, CJK punctuation, full-width forms). It is *Noto Sans JP*, Copyright 2014-2021 Adobe (http://www.adobe.com/), with
Reserved Font Name 'Source', under the SIL Open Font License 1.1 (`NotoSansJP-OFL.txt`), which allows it to be bundled and redistributed with
the licence text; it is not sold on its own.

It is a subset of `NotoSansJP-VF.ttf` of https://github.com/notofonts/noto-cjk (`Sans/Variable/TTF/Subset`) at the weight 400, reduced to the
characters of JIS X 0208 (kana, symbols, the 6,355 kanji of level 1 and 2), ASCII, CJK symbols and punctuation, and the full-width forms, without
layout features and hinting (7,238 glyphs, 2.2 MB). `subset.py.txt` is the script (needs `fonttools`). A character that is in no font is not drawn;
Chinese-only and Korean text needs another font.

The folder is copied into the WAR without filtering (the `pom.xml` excludes it from the filtered resources): filtering would damage the binary.
