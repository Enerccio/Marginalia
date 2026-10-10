#!/usr/bin/env python3
"""
Draws the class labels on the screenshots of the developer guide ("UI map" figures).

    python3 tools/annotate_ui_maps.py            # both figures
    python3 tools/annotate_ui_maps.py book       # manual/images/dev-ui-map-book.png
    python3 tools/annotate_ui_maps.py workspace  # manual/images/dev-ui-map-workspace.png

For each figure it reads manual/images/dev-ui-map-<name>-not-annotated.png (a plain screenshot, 1920x932, dark theme)
and writes manual/images/dev-ui-map-<name>.png. Needs Pillow (`pip install pillow`; into a scratch folder with
`pip install --target <dir> pillow` and PYTHONPATH=<dir> if you don't want it installed).

The coordinates below are pixels of a 1920x932 screenshot, measured on the figures drawn first. When the screenshot
changes (new tabs, new buttons) look at the plain screenshot and adjust:

  * `frames`  - the outline of a component: (left, top, right, bottom) in the color of its level (orange: the window,
                teal: the part inside it).
  * `labels`  - (left, top, class name, color, height): the label is as wide as its text. Window level labels sit in
                the bottom left corner of their frame, in a free spot.
  * `tabs`    - book figure only: for each tab of the book window the label of the class that builds it and the x of
                the middle of the tab's text. The connector goes from the label down to its own level (`levels`,
                one per tab, the first tab the highest, so the lines don't cross), left to the tab and down to a dot
                above the tab's text (`dot_y`). Keep the lines below the title of the window and above the tab text.

Style (as in the first versions of the figures): 3 px frames, labels in Menlo Bold (16 px for the window and part
labels, 15 px for the tab labels) with a dark fill and a 2 px border, connectors 2 px in a lighter purple.
"""
import argparse
import os
import sys

from PIL import Image, ImageDraw, ImageFont

ORANGE = (245, 181, 68)
TEAL = (79, 209, 197)
PURPLE = (199, 146, 234)
CONNECTOR = (183, 137, 218)
LABEL_BG = (14, 21, 33)
FONT = '/System/Library/Fonts/Menlo.ttc'          # Menlo Bold; any bold monospace font does

SPECS = {
    'book': {
        'frames': [((8, 8, 1912, 924), ORANGE, 8), ((28, 126, 1892, 852), TEAL, 6)],
        'labels': [(36, 870, 'ManuscriptDialog', ORANGE, 31), (49, 766, 'ManuscriptStoryPart', TEAL, 31)],
        # (tab, class, x of the tab's text, left edge of the label)
        'tabs': [('About', 'ManuscriptInfoPart', 62, 700),
                 ('Prompts', 'ManuscriptPromptPart', 140, 900),
                 ('Lorebook', 'ManuscriptLorebookPart', 230, 1118),
                 ('Branch view', 'ManuscriptTreePart', 398, 1354),
                 ('Backups', 'ManuscriptBackupPart', 500, 1554)],
        'levels': [65, 69, 73, 77, 81],
        'dot_y': 87,
        'tab_label_top': 22,
    },
    'workspace': {
        'frames': [((6, 12, 288, 922), ORANGE, 8), ((298, 24, 1900, 910), TEAL, 8)],
        'labels': [(22, 704, 'Workspace', ORANGE, 31), (318, 858, 'ManuscriptPart', TEAL, 31)],
    },
}


def label(draw, font, x, y, text, color, padx, height):
    width = int(round(draw.textlength(text, font=font))) + 2 * padx
    draw.rounded_rectangle((x, y, x + width, y + height), radius=4, fill=LABEL_BG, outline=color, width=2)
    box = draw.textbbox((0, 0), text, font=font)
    draw.text((x + padx, y + height / 2 - (box[1] + box[3]) / 2), text, font=font, fill=color)
    return x, y, x + width, y + height


def annotate(name, images):
    spec = SPECS[name]
    source = os.path.join(images, 'dev-ui-map-%s-not-annotated.png' % name)
    target = os.path.join(images, 'dev-ui-map-%s.png' % name)
    image = Image.open(source).convert('RGB')
    draw = ImageDraw.Draw(image)
    font = ImageFont.truetype(FONT, 16, index=1)
    tab_font = ImageFont.truetype(FONT, 15, index=1)

    for box, color, radius in spec['frames']:
        draw.rounded_rectangle(box, radius=radius, outline=color, width=3)
    for x, y, text, color, height in spec['labels']:
        label(draw, font, x, y, text, color, 10, height)

    for (tab, cls, tab_x, label_x), level in zip(spec.get('tabs', []), spec.get('levels', [])):
        x0, y0, x1, y1 = label(draw, tab_font, label_x, spec['tab_label_top'], cls, PURPLE, 10, 29)
        middle = (x0 + x1) // 2
        dot_y = spec['dot_y']
        draw.line([(middle, y1), (middle, level), (tab_x, level), (tab_x, dot_y)], fill=CONNECTOR, width=2, joint='curve')
        draw.ellipse((tab_x - 3, dot_y - 3, tab_x + 3, dot_y + 3), fill=CONNECTOR)

    image.save(target, optimize=True)
    print('written', target)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__.split('\n')[1])
    parser.add_argument('figure', nargs='?', default='all', choices=['all'] + sorted(SPECS))
    parser.add_argument('--images', default=os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'manual', 'images'),
                        help='folder of the screenshots (default manual/images)')
    args = parser.parse_args()
    for figure in (sorted(SPECS) if args.figure == 'all' else [args.figure]):
        if not os.path.exists(os.path.join(args.images, 'dev-ui-map-%s-not-annotated.png' % figure)):
            print('skipped %s: no dev-ui-map-%s-not-annotated.png in %s' % (figure, figure, args.images), file=sys.stderr)
            continue
        annotate(figure, args.images)
