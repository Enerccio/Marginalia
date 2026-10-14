#!/usr/bin/env python3
"""Makes out/broken/authorsnote-1.2.0.jar: the Author's Note plugin with the name of a decorated method misspelled in
its class file, so that Admin -> Extensions refuses it with a verification report (screenshot extension-verification)."""
import os, zipfile
root = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
src = os.path.join(root, '..', 'marginalia/plugins/authorsnote/target/authorsnote-1.2.0.jar')
dst = os.path.join(root, 'out/broken/authorsnote-1.2.0.jar')
os.makedirs(os.path.dirname(dst), exist_ok=True)
zin = zipfile.ZipFile(src)
zout = zipfile.ZipFile(dst, 'w', zipfile.ZIP_DEFLATED)
patched = 0
for item in zin.infolist():
    data = zin.read(item.filename)
    if item.filename.endswith('.class') and b'renderStoryContent' in data:
        data = data.replace(b'renderStoryContent', b'renderStoryContenX')
        patched += 1
    zout.writestr(item, data)
zout.close()
print('patched', patched, 'class file(s) ->', dst)
