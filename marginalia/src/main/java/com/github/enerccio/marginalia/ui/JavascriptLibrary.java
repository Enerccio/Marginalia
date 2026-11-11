package com.github.enerccio.marginalia.ui;

import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * All the JavaScript the application runs in the browser from the server side. Every method wraps one script, the
 * element it works on is always passed as {@code $0}.
 */
public final class JavascriptLibrary {

    private JavascriptLibrary() {
    }

    // --- leaving the page ---

    /**
     * Has the browser ask its own <i>Leave site?</i> before the page is closed, reloaded or left by a typed address, while {@code warn}
     * is true (a generation runs: leaving stops it); false takes the question away again. The page cannot ask a question of its own
     * then, browsers allow only theirs.
     *
     * @param element any element of the page, the question is kept on the window
     */
    public static void warnOnLeave(Element element, boolean warn) {
        element.executeJs(
                "if ($1) {"
                        + " if (!window.__marginaliaLeave) { window.__marginaliaLeave = e => { e.preventDefault(); e.returnValue = ''; };"
                        + " window.addEventListener('beforeunload', window.__marginaliaLeave); }"
                        + "} else if (window.__marginaliaLeave) {"
                        + " window.removeEventListener('beforeunload', window.__marginaliaLeave); window.__marginaliaLeave = null; }",
                element, warn);
    }

    // --- a panel that is moved ---

    /**
     * Lets the user move a fixed panel by its handle (the panel of the runs) and keeps where it was left in the browser
     * ({@code localStorage}, under the key); it stays inside the window, also when the window is made smaller. A drag is not a click
     * on the handle's parent.
     *
     * @param panel  the element with {@code position: fixed}
     * @param handle the element it is dragged by
     */
    public static void makeDraggable(Element panel, Element handle, String storageKey) {
        panel.executeJs("""
                const panel = $0, handle = $1, key = $2;
                if (panel.__draggable) return;
                panel.__draggable = true;
                let saved = null;
                const place = (left, top) => {
                  const w = panel.offsetWidth, h = panel.offsetHeight;
                  left = Math.max(0, Math.min(left, window.innerWidth - w));
                  top = Math.max(0, Math.min(top, window.innerHeight - h));
                  panel.style.left = left + 'px';
                  panel.style.top = top + 'px';
                  panel.style.bottom = 'auto';
                };
                try { saved = JSON.parse(localStorage.getItem(key) || 'null'); } catch (e) { saved = null; }
                if (saved) requestAnimationFrame(() => place(saved.left, saved.top));
                window.addEventListener('resize', () => { if (panel.style.top) place(parseFloat(panel.style.left), parseFloat(panel.style.top)); });
                let start = null, moved = false;
                handle.addEventListener('pointerdown', e => {
                  const box = panel.getBoundingClientRect();
                  start = { x: e.clientX, y: e.clientY, left: box.left, top: box.top };
                  moved = false;
                  try { handle.setPointerCapture(e.pointerId); } catch (err) {}
                  e.preventDefault();
                });
                handle.addEventListener('pointermove', e => {
                  if (!start) return;
                  const dx = e.clientX - start.x, dy = e.clientY - start.y;
                  if (!moved && Math.abs(dx) + Math.abs(dy) < 4) return;
                  moved = true;
                  place(start.left + dx, start.top + dy);
                });
                const end = () => {
                  if (!start) return;
                  start = null;
                  if (moved) {
                    try { localStorage.setItem(key, JSON.stringify({ left: parseFloat(panel.style.left), top: parseFloat(panel.style.top) })); } catch (e) {}
                  }
                };
                handle.addEventListener('pointerup', end);
                handle.addEventListener('pointercancel', end);
                handle.addEventListener('click', e => { if (moved) { e.stopPropagation(); moved = false; } }, true);
                """, handle, storageKey);
    }

    // --- clipboard ---

    /**
     * Copies the text to the clipboard of the browser.
     */
    public static void copyToClipboard(Page page, String text) {
        page.executeJs("navigator.clipboard.writeText($0);", text);
    }

    /**
     * Sends the images pasted into the element (Ctrl+V of a picture from the clipboard) to the upload, as if they were
     * picked in it. A paste of text is left alone.
     *
     * @param element the box the user pastes into
     * @param upload  the {@code vaadin-upload} the images go to
     */
    public static void forwardPastedImages(Element element, Element upload) {
        element.executeJs(
                "$0.addEventListener('paste', e => {"
                        + " const files = Array.from(e.clipboardData ? e.clipboardData.files : []).filter(f => f.type.startsWith('image/'));"
                        + " if (files.length) { e.preventDefault(); $1.uploadFiles(files); } });",
                element,
                upload
        );
    }

    // --- tree ---

    /**
     * Renders the Treant tree described by the JSON configuration into the element.
     */
    public static void renderTreant(Element element, String jsonConfig) {
        element.executeJs(
                "if (window.renderTreant) { window.renderTreant($0, JSON.parse($1)); }",
                element,
                jsonConfig
        );
    }

    // --- graph ---

    /**
     * Draws the graph described by the JSON configuration into the element.
     */
    public static void renderGraph(Element element, String jsonConfig) {
        element.executeJs(
                "if (window.renderGraph) { window.renderGraph($0, JSON.parse($1)); }",
                element,
                jsonConfig
        );
    }

    // --- chart ---

    /**
     * Draws the Chart.js chart described by the JSON configuration into the element.
     */
    public static void renderChart(Element element, String jsonConfig) {
        element.executeJs(
                "if (window.renderChart) { window.renderChart($0, JSON.parse($1)); }",
                element,
                jsonConfig
        );
    }

    // --- page ---

    /**
     * Creates the {@code <style>} element with the given id in the page head, or replaces the content of the existing one.
     */
    public static void setPageStyle(Page page, String id, String css) {
        page.executeJs("""
                let style = document.getElementById($0);
                if (!style) {
                  style = document.createElement('style');
                  style.id = $0;
                  document.head.appendChild(style);
                }
                style.textContent = $1;
                """, id, css);
    }

    public static void removePageStyle(Page page, String id) {
        page.executeJs("document.getElementById($0)?.remove();", id);
    }

    /**
     * Removes the {@code <style>} element with the given id unless an element of the page still carries the class it is for. It
     * waits for the changes of the page that come with the same response (the host of the style being removed from it).
     */
    public static void removePageStyleUnlessUsed(Page page, String id, String cssClass) {
        page.executeJs("""
                setTimeout(() => {
                  if (!document.getElementsByClassName($1).length) {
                    document.getElementById($0)?.remove();
                  }
                }, 0);
                """, id, cssClass);
    }

    /**
     * Makes the login overlay accept an empty password.
     */
    public static void disableLoginPasswordValidation(Element element) {
        element.executeJs("document.getElementById('vaadinLoginPassword').validate = document.getElementById('vaadinLoginPassword').checkValidity = function () { return true; };");
    }

    // --- element geometry ---

    /**
     * Resolves to {@code x;y;w;h} of the element, x and y relative to the document.
     */
    public static PendingJavaScriptResult getPositionAndSize(Element element) {
        return element.executeJs("" +
                "return (function () { \n" +
                "   var w = $0.clientWidth; \n" +
                "   var h = $0.clientHeight; \n" +
                "   var bb = $0.getBoundingClientRect(); \n" +
                "   var x = bb.left + window.scrollX; \n" +
                "   var y = bb.top + window.scrollY; \n" +
                "   return '' + x + ';' + y + ';' + w + ';' + h; \n" +
                "} ())", element);
    }

    // --- scrolling ---

    public static void setScrollTop(Element element, int pixelsFromTop) {
        element.executeJs("$0.scrollTop = $1", element, pixelsFromTop);
    }

    /**
     * Scrolls the text area of a Vaadin {@code TextArea} (the element of the field) to its last line, after the value
     * that was just set is rendered.
     */
    public static void scrollTextAreaToBottom(Element textArea) {
        textArea.executeJs("""
                requestAnimationFrame(() => {
                  const input = $0.inputElement;
                  if (input) input.scrollTop = input.scrollHeight;
                });
                """, textArea);
    }

    /**
     * Keeps the inner textarea of a Vaadin {@code TextArea} as tall as its text. The field measures the text only when
     * the value or its own size changes, so when the scrollbar of the field appears (the text gets narrower and wraps
     * into more lines) or the font loads the textarea stays too short and the end of the text cannot be scrolled to
     * until the field is resized. Re-measures when the width of the scrolling container changes.
     */
    public static void keepTextAreaFitted(Element textArea) {
        textArea.executeJs("""
                const field = $0;
                if (field._fitObserver) return;
                const fit = () => {
                  field._fitFrame = null;
                  if (typeof field._updateHeight === 'function') field._updateHeight();
                };
                const schedule = () => {
                  if (!field._fitFrame) field._fitFrame = requestAnimationFrame(fit);
                };
                requestAnimationFrame(() => {
                  const inputField = field._inputField;
                  if (!inputField) return;
                  field._fitObserver = new ResizeObserver(schedule);
                  field._fitObserver.observe(inputField);
                  if (field.inputElement) field._fitObserver.observe(field.inputElement);
                  schedule();
                });
                """, textArea);
    }

    public static void setScrollLeft(Element element, int pixelsFromLeft) {
        element.executeJs("$0.scrollLeft = $1", element, pixelsFromLeft);
    }

    /**
     * Installs (once) the listener behind the sticky bottom scroll: {@code el._stick} is cleared as soon as the user
     * scrolls up and set again when the bottom is reached. Content growth never changes {@code scrollTop}, so it cannot
     * unstick or restick by itself, unlike measuring the distance to the bottom after the content already grew.
     */
    private static final String INSTALL_STICKY_SCROLL = """
            const installSticky = (el) => {
              if (el._stickInstalled) return;
              el._stickInstalled = true;
              el._lastTop = el.scrollTop;
              el._stick = (el.scrollHeight - el.scrollTop - el.clientHeight) < 8;
              el.addEventListener('scroll', () => {
                const top = el.scrollTop;
                if (top < el._lastTop) {
                  el._stick = false;
                }
                if ((el.scrollHeight - top - el.clientHeight) < 8) {
                  el._stick = true;
                }
                el._lastTop = top;
              }, {passive: true});
            };
            """;

    public static void scrollToBottom(Element element) {
        element.executeJs(INSTALL_STICKY_SCROLL + """
                installSticky($0);
                $0._stick = true;
                $0.scrollTop = $0.scrollHeight;
                $0._lastTop = $0.scrollTop;
                """, element);
    }

    /**
     * Scrolls to the bottom once the layout following the current changes is done, and sticks to the bottom again.
     */
    public static void scrollToBottomAfterLayout(Element element) {
        element.executeJs(INSTALL_STICKY_SCROLL + """
                installSticky($0);
                $0._stick = true;
                requestAnimationFrame(() => {
                  requestAnimationFrame(() => {
                    $0.scrollTop = $0.scrollHeight;
                    $0._lastTop = $0.scrollTop;
                  });
                });
                """, element);
    }

    /**
     * Puts the element back where the user had it after its content was replaced: at the bottom when it was stuck
     * there, otherwise at the same distance from the top, which is set again while the new content settles (its
     * Markdown is rendered after it is in the page) until the user scrolls on their own.
     *
     * @param element the scroll element
     * @param content the element whose size changes while the content settles
     */
    public static void restoreScroll(Element element, Element content) {
        element.executeJs(INSTALL_STICKY_SCROLL + """
                const el = $0;
                const content = $1;
                installSticky(el);
                if (el._stick) {
                  requestAnimationFrame(() => requestAnimationFrame(() => {
                    el.scrollTop = el.scrollHeight;
                    el._lastTop = el.scrollTop;
                  }));
                  return;
                }
                const top = el._lastTop;
                if (el._restoreStop) el._restoreStop();
                const apply = () => {
                  if (Math.abs(el.scrollTop - top) > 1) el.scrollTop = top;
                };
                const observer = new ResizeObserver(apply);
                const events = ['wheel', 'touchstart', 'pointerdown', 'keydown'];
                const stop = () => {
                  observer.disconnect();
                  clearTimeout(el._restoreTimer);
                  events.forEach(name => el.removeEventListener(name, stop));
                  el._restoreStop = null;
                };
                el._restoreStop = stop;
                events.forEach(name => el.addEventListener(name, stop, {passive: true}));
                observer.observe(content);
                el._restoreTimer = setTimeout(stop, 1500);
                apply();
                """, element, content);
    }

    /**
     * Opens a list at its end: scrolls to the bottom and keeps it there while the content settles (its Markdown is rendered after it
     * is in the page, so its height is not final when the first scroll runs), until the user scrolls on their own or a
     * moment has passed. Sticks to the bottom afterwards like {@link #scrollToBottom}.
     *
     * @param element the scroll element
     * @param content the element whose size changes while the content settles
     */
    public static void scrollToBottomWhileSettling(Element element, Element content) {
        element.executeJs(INSTALL_STICKY_SCROLL + """
                const el = $0;
                const content = $1;
                installSticky(el);
                el._stick = true;
                if (el._settleStop) el._settleStop();
                const apply = () => {
                  if (!el._stick) return;
                  el.scrollTop = el.scrollHeight;
                  el._lastTop = el.scrollTop;
                };
                const observer = new ResizeObserver(apply);
                const events = ['wheel', 'touchstart', 'pointerdown', 'keydown'];
                const stop = () => {
                  observer.disconnect();
                  clearTimeout(el._settleTimer);
                  events.forEach(name => el.removeEventListener(name, stop));
                  el._settleStop = null;
                };
                el._settleStop = stop;
                events.forEach(name => el.addEventListener(name, stop, {passive: true}));
                observer.observe(content);
                el._settleTimer = setTimeout(stop, 1500);
                requestAnimationFrame(() => requestAnimationFrame(apply));
                """, element, content);
    }

    /**
     * Scrolls to the bottom while the element is stuck to it, i.e. until the user scrolls up; reaching the bottom again
     * sticks it again.
     */
    public static void scrollToBottomIfAtBottom(Element element) {
        element.executeJs(INSTALL_STICKY_SCROLL + """
                const el = $0;
                installSticky(el);
                requestAnimationFrame(() => {
                  if (el._stick) {
                    el.scrollTop = el.scrollHeight;
                    el._lastTop = el.scrollTop;
                  }
                });
                """, element);
    }

    /**
     * Where a part is in the scrolled content (not in the window), to find it again after parts were added or dropped
     * around it: {@code anchorOf} picks the first part the user sees and {@code anchorPos} is its position in the
     * content, which the user's own scrolling does not change.
     */
    private static final String ANCHOR_HELPERS = """
            const anchorPos = (el, node) =>
              node.getBoundingClientRect().top - el.getBoundingClientRect().top + el.scrollTop;
            const anchorOf = (el, content) => {
              const top = el.getBoundingClientRect().top;
              for (const node of content.children) {
                const rect = node.getBoundingClientRect();
                if (rect.height > 0 && rect.bottom > top + 1) {
                  return {node: node, pos: anchorPos(el, node)};
                }
              }
              return null;
            };
            """;

    /**
     * Fires {@code edge-top} (or {@code edge-bottom}) on the scroll element when it is scrolled close to that edge, to
     * load more content there. The event has {@code detail.far}, the distance in pixels from the opposite edge, so
     * the server drops parts there only when that is far enough not to be needed again at once. After every load the
     * server calls this again: {@code armed} is whether there is more to load, if there is not, nothing fires until it
     * is called with {@code true}. The check also runs once the layout following the current changes is done, so
     * content that does not fill the element asks for more at once. The part the user sees first is remembered when
     * the event fires, {@link #restoreAnchor} puts it back in place.
     *
     * @param element the scroll element
     * @param content the element whose children are the parts
     */
    public static void watchEdge(Element element, Element content, boolean top, boolean armed) {
        element.executeJs(ANCHOR_HELPERS + """
                const el = $0;
                const content = $1;
                const top = $2;
                const name = top ? 'edge-top' : 'edge-bottom';
                const busy = '_busy-' + name;
                const check = '_check-' + name;
                if (!el[check]) {
                  el[check] = () => {
                    if (el[busy]) return;
                    const bottomGap = el.scrollHeight - el.scrollTop - el.clientHeight;
                    const distance = top ? el.scrollTop : bottomGap;
                    if (distance < 400) {
                      el[busy] = true;
                      el._anchor = anchorOf(el, content);
                      el.dispatchEvent(new CustomEvent(name, {detail: {far: top ? bottomGap : el.scrollTop}}));
                    }
                  };
                  el.addEventListener('scroll', el[check], {passive: true});
                }
                el[busy] = !$3;
                requestAnimationFrame(() => requestAnimationFrame(el[check]));
                """, element, content, top, armed);
    }

    /**
     * Keeps the part the user sees in place after parts were added above it or dropped from there (the part that
     * {@link #watchEdge} remembered when it fired). The position is corrected again while the new parts settle, their
     * Markdown is rendered after they are in the page.
     *
     * @param element the scroll element
     * @param content the element whose children are the parts
     */
    public static void restoreAnchor(Element element, Element content) {
        element.executeJs(ANCHOR_HELPERS + """
                const el = $0;
                const content = $1;
                const anchor = el._anchor;
                if (!anchor || !anchor.node.isConnected) return;
                const apply = () => {
                  if (!anchor.node.isConnected) return;
                  const pos = anchorPos(el, anchor.node);
                  if (pos !== anchor.pos) {
                    el.scrollTop += pos - anchor.pos;
                    el._lastTop = el.scrollTop;
                    anchor.pos = pos;
                  }
                };
                apply();
                if (el._anchorObserver) el._anchorObserver.disconnect();
                el._anchorObserver = new ResizeObserver(apply);
                el._anchorObserver.observe(content);
                clearTimeout(el._anchorTimer);
                el._anchorTimer = setTimeout(() => el._anchorObserver.disconnect(), 1500);
                """, element, content);
    }

    /**
     * Scrolls the element to the top of its scroll parent and flashes an outline around it.
     */
    public static void scrollToAndHighlight(Element element) {
        element.executeJs("""
                requestAnimationFrame(() => {
                  requestAnimationFrame(() => {
                    $0.scrollIntoView({block: 'start'});
                    $0.animate([{outline: '3px solid var(--lumo-warning-color)'}, {outline: '3px solid transparent'}],
                               {duration: 2000});
                  });
                });
                """, element);
    }

    /**
     * Scrolls the middle of the screen to the match of a search inside the element (the {@code mark} with the CSS class),
     * to the element itself when it has none.
     */
    public static void scrollToSearchMatch(Element element, String currentCssClass) {
        element.executeJs("""
                requestAnimationFrame(() => {
                  requestAnimationFrame(() => {
                    const match = $0.querySelector('mark.' + $1);
                    (match || $0).scrollIntoView({block: 'center'});
                  });
                });
                """, element, currentCssClass);
    }

    /**
     * Scrolls the element to the top of its scroll parent once the layout following the current changes is done.
     */
    public static void scrollIntoViewAfterLayout(Element element) {
        element.executeJs("""
                requestAnimationFrame(() => {
                  requestAnimationFrame(() => $0.scrollIntoView({block: 'start'}));
                });
                """, element);
    }

    // --- reader position ---

    private static final String READER_POSITION_KEY = "marginalia.reader.position.";

    /**
     * Keeps the id of the part the reader sees first in the {@code localStorage} of the browser, under the uuid of
     * the book, while the user scrolls (the server never learns it, it is read back by {@link #readReaderPosition}).
     * The parts are the children of {@code content} that have the attribute {@code data-message-id}.
     *
     * @param element the scroll element
     * @param content the element whose children are the parts
     */
    public static void rememberReaderPosition(Element element, Element content, String manuscriptUuid) {
        element.executeJs("""
                const el = $0;
                const content = $1;
                const key = $2 + $3;
                if (el._positionKey === key) return;
                el._positionKey = key;
                if (!el._positionInstalled) {
                  el._positionInstalled = true;
                  const save = () => {
                    const top = el.getBoundingClientRect().top;
                    for (const node of el._positionContent.children) {
                      const id = node.getAttribute('data-message-id');
                      if (id !== null && node.getBoundingClientRect().bottom > top + 1) {
                        try { localStorage.setItem(el._positionKey, id); } catch (e) {}
                        return;
                      }
                    }
                  };
                  el.addEventListener('scroll', () => {
                    clearTimeout(el._positionTimer);
                    el._positionTimer = setTimeout(save, 250);
                  }, {passive: true});
                  window.addEventListener('pagehide', save);
                }
                el._positionContent = content;
                """, element, content, READER_POSITION_KEY, manuscriptUuid);
    }

    /**
     * The id of the part the reader saw first when the user left the book last time, as text; {@code null} when there
     * is none or the storage is blocked.
     */
    public static PendingJavaScriptResult readReaderPosition(Page page, String manuscriptUuid) {
        return page.executeJs("""
                try { return localStorage.getItem($0 + $1); } catch (e) { return null; }
                """, READER_POSITION_KEY, manuscriptUuid);
    }

    private static final String VIEWER_FOLDER_COOKIE = "marginalia_viewer_folder";

    /**
     * Keeps where the list of the viewer was left (the uuid of the folder, or a marker for the favorites; empty is the top
     * level) in a cookie of the browser, for a year. The list reads it back by {@link #readViewerFolder}.
     */
    public static void rememberViewerFolder(Page page, String value) {
        page.executeJs("""
                document.cookie = $0 + '=' + encodeURIComponent($1) + '; path=/; max-age=31536000; SameSite=Lax'
                        + (location.protocol === 'https:' ? '; Secure' : '');
                """, VIEWER_FOLDER_COOKIE, value);
    }

    /**
     * What {@link #rememberViewerFolder} stored in this browser; empty text when nothing was or cookies are blocked.
     */
    public static PendingJavaScriptResult readViewerFolder(Page page) {
        return page.executeJs("""
                const prefix = $0 + '=';
                const found = document.cookie.split('; ').find(c => c.startsWith(prefix));
                if (!found) return '';
                try { return decodeURIComponent(found.substring(prefix.length)); } catch (e) { return ''; }
                """, VIEWER_FOLDER_COOKIE);
    }

    // --- element events ---

    /**
     * Fires {@code chip-click} ({@code detail.label}) on the multi select combo box when the user clicks the label of
     * one of its chips (not its remove button), without opening the dropdown.
     */
    public static void fireChipClickEvent(Element comboBox) {
        comboBox.executeJs("""
                const el = $0;
                if (!el._hasChipClickListener) {
                  el._hasChipClickListener = true;
                  el.addEventListener('click', e => {
                    const path = e.composedPath();
                    const chip = path.find(n => n.localName === 'vaadin-multi-select-combo-box-chip');
                    if (!chip || path.some(n => n.getAttribute && n.getAttribute('part') === 'remove-button')) {
                      return;
                    }
                    e.stopPropagation();
                    el.dispatchEvent(new CustomEvent('chip-click', { detail: { label: chip.label || chip.textContent.trim() } }));
                  }, true);
                }
                """, comboBox);
    }

    // --- annotations ---

    /**
     * Fires {@code annotation-click} ({@code detail.ids}, the uuids of the annotations separated by commas) on the markdown element when
     * the user clicks a mark of an annotation: every annotation that covers the point, the innermost first. The anchor (a small
     * absolutely placed element in the same container) is moved onto the mark first, so a popover that has it as its target opens at
     * the mark. A click that ends a selection is not a click on a mark.
     */
    public static void fireAnnotationClickEvent(Element markdown, Element anchor) {
        markdown.executeJs("""
                const el = $0;
                const anchor = $1;
                if (!el._hasAnnotationClickListener) {
                  el._hasAnnotationClickListener = true;
                  el.addEventListener('click', e => {
                    const selection = window.getSelection();
                    if (selection && !selection.isCollapsed && String(selection).length > 0) {
                      return;
                    }
                    const marks = [];
                    for (let n = e.target; n && n !== el; n = n.parentElement) {
                      if (n.dataset && n.dataset.annotation) {
                        marks.push(n);
                      }
                    }
                    if (!marks.length) {
                      return;
                    }
                    const ids = [...new Set(marks.map(m => m.dataset.annotation))];
                    const host = anchor.offsetParent || anchor.parentElement;
                    const hostRect = host.getBoundingClientRect();
                    const rect = marks[0].getBoundingClientRect();
                    anchor.style.left = (rect.left - hostRect.left + host.scrollLeft) + 'px';
                    anchor.style.top = (rect.top - hostRect.top + host.scrollTop) + 'px';
                    anchor.style.width = rect.width + 'px';
                    anchor.style.height = rect.height + 'px';
                    el.dispatchEvent(new CustomEvent('annotation-click', { detail: { ids: ids.join(',') } }));
                  });
                }
                """, markdown, anchor);
    }

    /**
     * The script that works out the selection inside an element, as JSON ({@code {"text", "before", "after"}}) or an empty text. It is
     * the body of the scripts below, which have the element as {@code $0} and the number of characters around the selection as {@code $1}.
     */
    private static final String SELECTION_SNAPSHOT = """
            const snapshot = (el, around) => {
              const selection = window.getSelection();
              if (!selection || selection.rangeCount === 0 || selection.isCollapsed) {
                return '';
              }
              const range = selection.getRangeAt(0);
              if (!el.contains(range.startContainer) || !el.contains(range.endContainer)) {
                return '';
              }
              const text = String(selection);
              if (!text.trim()) {
                return '';
              }
              const before = document.createRange();
              before.selectNodeContents(el);
              before.setEnd(range.startContainer, range.startOffset);
              const start = before.toString().length;
              const all = el.textContent;
              return JSON.stringify({
                text: text,
                before: all.substring(Math.max(0, start - around), start),
                after: all.substring(start + text.length, start + text.length + around)
              });
            };
            """;

    /**
     * Part of the scripts below: paints the remembered selection with the CSS Custom Highlight API ({@code ::highlight(...)} in
     * {@code shared-styles.css}), because the context menu takes the real selection away visually (the text is still remembered, but it
     * would look as if it were not). {@code paintRemembered(range)} shows it, {@code clearRemembered()} removes it again; where the API
     * does not exist they do nothing.
     */
    private static final String REMEMBERED_HIGHLIGHT = """
            const HIGHLIGHT_NAME = 'marginalia-remembered-selection';
            const clearRemembered = () => {
              if (window.CSS && CSS.highlights) {
                CSS.highlights.delete(HIGHLIGHT_NAME);
              }
            };
            const paintRemembered = range => {
              if (window.CSS && CSS.highlights && typeof Highlight !== 'undefined') {
                CSS.highlights.set(HIGHLIGHT_NAME, new Highlight(range));
              }
            };
            """;

    /**
     * Remembers the selection of the markdown element at the moment the user opens its context menu (right click, long press), before
     * the menu can take it away, and keeps it painted while the menu is open; {@link #getSelectionContext} and {@link #copySelection} use
     * the memory when nothing is selected any more. A click on the text, a key press or the use of the memory forgets it.
     */
    public static void rememberSelectionOnContextMenu(Element markdown, int around) {
        markdown.executeJs(SELECTION_SNAPSHOT + REMEMBERED_HIGHLIGHT + """
                const el = $0;
                if (!el._hasSelectionMemory) {
                  el._hasSelectionMemory = true;
                  el.addEventListener('contextmenu', () => {
                    const found = snapshot(el, $1);
                    if (found) {
                      el._rememberedSelection = found;
                      paintRemembered(window.getSelection().getRangeAt(0).cloneRange());
                    }
                  }, true);
                  el.addEventListener('mousedown', e => {
                    if (e.button === 0) {
                      el._rememberedSelection = null;
                    }
                  }, true);
                  if (!window.__rememberedSelectionReset) {
                    window.__rememberedSelectionReset = true;
                    document.addEventListener('mousedown', clearRemembered, true);
                    document.addEventListener('keydown', clearRemembered, true);
                  }
                }
                """, markdown, around);
    }

    /**
     * Resolves to the text the user selected inside the markdown element and some text around it, as JSON
     * ({@code {"text": ..., "before": ..., "after": ...}}), or to an empty text when nothing is selected in it. The text is the one
     * the browser shows, which is not the markdown source: the server finds it there. When the selection is gone but was
     * remembered by {@link #rememberSelectionOnContextMenu}, the remembered one is given (once).
     */
    public static PendingJavaScriptResult getSelectionContext(Element markdown, int around) {
        return markdown.executeJs(SELECTION_SNAPSHOT + REMEMBERED_HIGHLIGHT + """
                const el = $0;
                let found = snapshot(el, $1);
                if (!found && el._rememberedSelection) {
                  found = el._rememberedSelection;
                }
                el._rememberedSelection = null;
                clearRemembered();
                return found;
                """, markdown, around);
    }

    /**
     * Copies the text selected inside the markdown element (the remembered one when the selection is gone, see
     * {@link #rememberSelectionOnContextMenu}) to the clipboard of the browser.
     */
    public static void copySelection(Element markdown) {
        markdown.executeJs(SELECTION_SNAPSHOT + REMEMBERED_HIGHLIGHT + """
                const el = $0;
                let found = snapshot(el, 0) || el._rememberedSelection;
                if (found) {
                  navigator.clipboard.writeText(JSON.parse(found).text);
                }
                clearRemembered();
                """, markdown);
    }

    /**
     * Resolves to {@code start;end}, the selection in the text area.
     */
    public static PendingJavaScriptResult getTextAreaSelection(Element textArea) {
        return textArea.executeJs("""
                const input = $0.inputElement || $0.querySelector('textarea');
                return input ? input.selectionStart + ';' + input.selectionEnd : '';
                """, textArea);
    }

    /**
     * Flashes the marks of the annotation inside the element.
     */
    public static void flashAnnotation(Element markdown, String uuid) {
        markdown.executeJs("""
                requestAnimationFrame(() => {
                  const marks = $0.querySelectorAll('[data-annotation="' + $1 + '"]');
                  if (marks.length) {
                    marks[0].scrollIntoView({block: 'center'});
                  }
                  marks.forEach(m => m.animate([{outline: '3px solid var(--lumo-warning-color)'}, {outline: '3px solid transparent'}],
                                               {duration: 1500}));
                });
                """, markdown, uuid);
    }

    /**
     * Lights up together the note in the margin and the marks of its passage: the pointer over a mark lights the notes of the
     * annotations it is part of, the pointer over a note lights its marks. The listeners are added once to the elements.
     *
     * @param markdown the text of the part, whose marks have the uuid of their annotation in {@code data-annotation}
     * @param margin   the column of the notes, whose notes have it in {@code data-annotation-id}
     */
    public static void linkMarginNotes(Element markdown, Element margin) {
        markdown.executeJs("""
                const text = $0;
                const margin = $1;
                if (!margin || text._marginLinked) {
                  // the phone layout has no margin: its column is never added to the card
                  return;
                }
                text._marginLinked = true;
                const lit = 'annotation-lit';
                const clear = () => {
                  text.querySelectorAll('.' + lit).forEach(n => n.classList.remove(lit));
                  margin.querySelectorAll('.' + lit).forEach(n => n.classList.remove(lit));
                };
                text.addEventListener('mouseover', e => {
                  clear();
                  for (let n = e.target; n && n !== text; n = n.parentElement) {
                    if (n.dataset && n.dataset.annotation) {
                      const note = margin.querySelector('[data-annotation-id="' + n.dataset.annotation + '"]');
                      if (note) {
                        note.classList.add(lit);
                      }
                    }
                  }
                });
                text.addEventListener('mouseleave', clear);
                margin.addEventListener('mouseover', e => {
                  clear();
                  const note = e.target.closest ? e.target.closest('[data-annotation-id]') : null;
                  if (note) {
                    note.classList.add(lit);
                    text.querySelectorAll('[data-annotation="' + note.dataset.annotationId + '"]').forEach(m => m.classList.add(lit));
                  }
                });
                margin.addEventListener('mouseleave', clear);
                """, markdown, margin);
    }

    /**
     * Brings the note of the annotation in the margin into view and flashes it.
     */
    public static void flashMarginNote(Element margin, String uuid) {
        margin.executeJs("""
                requestAnimationFrame(() => {
                  const note = $0.querySelector('[data-annotation-id="' + $1 + '"]');
                  if (note) {
                    note.scrollIntoView({block: 'nearest'});
                    note.animate([{outline: '3px solid var(--lumo-warning-color)'}, {outline: '3px solid transparent'}], {duration: 1500});
                  }
                });
                """, margin, uuid);
    }

    // --- tab sheet ---

    /**
     * Calls {@code onScrollToEnd()} on the server when the tab sheet content is scrolled to the end.
     */
    public static void notifyTabSheetScrolledToEnd(Element tabSheet) {
        tabSheet.executeJs("""
                var self = $0;
                var el = self.querySelector("vaadin-horizontal-layout");
                if (el) {
                    el.addEventListener("scroll", function(e) {
                        if(el.scrollTop + el.clientHeight === el.scrollHeight) {
                            self.$server.onScrollToEnd();
                        }
                    });
                }""", tabSheet);
    }

    /**
     * Fires {@code myscroll} ({@code detail}: {@code scrollTop,scrollLeft}) on the tab sheet when its content scrolls.
     */
    public static void fireTabSheetScrollEvent(Element tabSheet) {
        tabSheet.executeJs("""
                var el = $0;
                el.querySelector("vaadin-horizontal-layout").addEventListener('scroll', e => {
                    const event = new CustomEvent('myscroll', { detail: e.target.scrollTop + ',' + e.target.scrollLeft });
                    el.dispatchEvent(event);
                })""", tabSheet);
    }

    // --- swipe ---

    /**
     * Fires {@code swipe} ({@code detail}: {@code left} or {@code right}) on the element when a finger is drawn across it sideways:
     * far enough, quickly enough, and mostly horizontally. A touch that starts in a box that takes the finger itself (a field, a
     * table, anything that scrolls sideways) is left alone. Done once for an element.
     */
    public static void fireSwipeEvents(Element element) {
        element.executeJs("""
                var el = $0;
                if (el.__swipeEvents) { return; }
                el.__swipeEvents = true;
                var start = null;
                var own = ['INPUT', 'TEXTAREA', 'SELECT', 'VAADIN-GRID', 'VAADIN-TEXT-FIELD', 'VAADIN-TEXT-AREA', 'VAADIN-COMBO-BOX',
                    'VAADIN-SELECT', 'VAADIN-SLIDER', 'VAADIN-RICH-TEXT-EDITOR', 'VAADIN-TABS', 'VAADIN-TAB-SHEET'];
                function takesFinger(event) {
                    var path = event.composedPath();
                    for (var i = 0; i < path.length && path[i] !== el; i++) {
                        var node = path[i];
                        if (!(node instanceof HTMLElement)) { continue; }
                        if (own.indexOf(node.tagName) >= 0 || node.isContentEditable) { return true; }
                        var overflow = getComputedStyle(node).overflowX;
                        if ((overflow === 'auto' || overflow === 'scroll') && node.scrollWidth > node.clientWidth + 1) { return true; }
                    }
                    return false;
                }
                el.addEventListener('touchstart', function (e) {
                    start = e.touches.length === 1 && !takesFinger(e)
                        ? { x: e.touches[0].clientX, y: e.touches[0].clientY, time: Date.now() } : null;
                }, { passive: true });
                el.addEventListener('touchcancel', function () { start = null; }, { passive: true });
                el.addEventListener('touchend', function (e) {
                    if (!start) { return; }
                    var from = start;
                    start = null;
                    var dx = e.changedTouches[0].clientX - from.x;
                    var dy = e.changedTouches[0].clientY - from.y;
                    if (Math.abs(dx) < 80 || Math.abs(dx) < 2 * Math.abs(dy) || Date.now() - from.time > 700) { return; }
                    el.dispatchEvent(new CustomEvent('swipe', { detail: dx < 0 ? 'left' : 'right' }));
                }, { passive: true });
                """, element);
    }

    // --- two-pane dialog ---

    /**
     * Fires {@code organise-key} ({@code detail}: {@code ArrowLeft}, {@code ArrowRight}, {@code Enter}, {@code Backspace},
     * {@code Insert}, {@code Space}, {@code Shift+ArrowUp}, {@code Shift+ArrowDown}) on the grid when the user presses one of the
     * keys in it; the key combinations of {@code actions} fire it with the id of the action as the {@code detail}. The keys are caught before the grid sees them, so Left / Right do not move the focus between the cells and Enter
     * does not toggle the checkbox of the row; Up / Down (with or without Shift) are left to the grid, which moves between the
     * rows. Any other key with Ctrl, Alt or Meta, and a key typed in a field, are left alone. The listener is added once for a
     * grid, a later call only replaces the actions.
     *
     * @param actions JSON object, canonical key combination ({@code KeyCombo}) to the id of the action
     */
    public static void fireOrganiseKeys(Element grid, String actions) {
        grid.executeJs(KEY_COMBO + """
                var el = $0;
                el.__organiseActions = JSON.parse($1);
                if (el.__organiseKeys) { return; }
                el.__organiseKeys = true;
                var caught = ['ArrowLeft', 'ArrowRight', 'Enter', 'Backspace', 'Insert', ' '];
                el.addEventListener('keydown', function (e) {
                    if (e.isComposing) { return; }
                    var path = e.composedPath();
                    for (var i = 0; i < path.length && path[i] !== el; i++) {
                        var tag = path[i].tagName;
                        if (tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT' || path[i].isContentEditable) { return; }
                    }
                    var action = e.repeat ? null : el.__organiseActions[window.marginaliaKeyCombo(e)];
                    if (action) {
                        e.preventDefault();
                        e.stopPropagation();
                        el.dispatchEvent(new CustomEvent('organise-key', { detail: action }));
                        return;
                    }
                    if (e.ctrlKey || e.altKey || e.metaKey) { return; }
                    if (e.shiftKey && (e.key === 'ArrowUp' || e.key === 'ArrowDown')) {
                        el.dispatchEvent(new CustomEvent('organise-key', { detail: 'Shift+' + e.key }));
                        return;
                    }
                    if (e.shiftKey || caught.indexOf(e.key) < 0) { return; }
                    e.preventDefault();
                    e.stopPropagation();
                    el.dispatchEvent(new CustomEvent('organise-key', { detail: e.key === ' ' ? 'Space' : e.key }));
                }, true);
                """, grid, actions);
    }

    /**
     * Puts the keyboard focus into a cell of the grid (the host element of a {@code vaadin-grid} can't take the focus itself, so
     * {@code focus()} on it does nothing and the focus would stay in the other pane): the row with the {@code index}. Scrolls to
     * the row and tries again until the rows are rendered and the cell has the focus.
     */
    public static void focusOrganiseGrid(Element grid, int index) {
        grid.executeJs("""
                var el = $0;
                var index = $1;
                var tries = 0;
                if (el.scrollToIndex) { el.scrollToIndex(index); }
                var timer = setInterval(function () {
                    var rows = (el._getRenderedRows ? el._getRenderedRows() : []).filter(function (r) { return !r.hidden; });
                    var row = rows.filter(function (r) { return r.index === index; })[0]
                        || rows.sort(function (a, b) { return a.index - b.index; })[0];
                    var cell = row ? row.children[0] : null;
                    if (cell) { cell.focus(); }
                    if ((cell && el.contains(document.activeElement)) || ++tries > 20) { clearInterval(timer); }
                }, 30);
                """, grid, index);
    }

    // --- keyboard shortcuts ---

    /**
     * Turns a keydown event into the canonical key combination ({@code KeyCombo}): the modifiers {@code Ctrl},
     * {@code Alt}, {@code Shift}, {@code Meta} in that order, then the key (a character in lower case, {@code Space} or a
     * name such as {@code Enter}); {@code Shift} is left out for a character that is no letter or digit. Returns null for a
     * modifier alone and for a character typed with AltGr.
     */
    private static final String KEY_COMBO = """
            window.marginaliaKeyCombo = window.marginaliaKeyCombo || function (e) {
                var key = e.key;
                if (!key || ['Control', 'Shift', 'Alt', 'Meta', 'AltGraph', 'Dead', 'Unidentified', 'CapsLock', 'OS'].indexOf(key) >= 0) { return null; }
                if (e.getModifierState && e.getModifierState('AltGraph')) { return null; }
                if (e.altKey && /^(Key[A-Z]|Digit[0-9])$/.test(e.code || '')) {
                    // Option/Alt composes another character on some keyboards, the physical key is what the user means
                    key = e.code.charAt(e.code.length - 1);
                } else if ((e.ctrlKey || e.metaKey) && /^Key[A-Z]$/.test(e.code || '') && !/^[\\x00-\\x7f]$/.test(key)) {
                    // a layout that types no Latin letter (Cyrillic, Greek...): Ctrl+S is still the key where the S is
                    key = e.code.charAt(3);
                }
                if (key === ' ') { key = 'Space'; }
                else if (Array.from(key).length === 1) { key = key.toLowerCase(); }
                var shift = e.shiftKey && !(Array.from(key).length === 1 && !/[\\p{L}\\p{N}]/u.test(key));
                return (e.ctrlKey ? 'Ctrl+' : '') + (e.altKey ? 'Alt+' : '') + (shift ? 'Shift+' : '') + (e.metaKey ? 'Meta+' : '') + key;
            };
            """;

    /**
     * Keeps the browser from selecting the text between two clicks when Shift is held on a check box with the class
     * {@code boxClass} inside the element (the click still ticks the box). Done once for an element.
     */
    public static void noTextSelectionOnShiftClick(Element element, String boxClass) {
        element.executeJs("""
                var el = $0;
                if (el.__noShiftSelection) { return; }
                el.__noShiftSelection = true;
                el.addEventListener('mousedown', function (e) {
                    if (e.shiftKey && e.target.classList && e.target.classList.contains($1)) { e.preventDefault(); }
                }, true);
                """, element, boxClass);
    }

    /**
     * Makes the element fire {@code marginalia-shortcut} ({@code detail}: the id of the action) when the user presses the
     * key of an action. The keys are the page wide map in {@code window.marginaliaKeys} ({@link #setShortcutKeys}), so
     * a change of the settings reaches the open story tabs. The shortcut fires only while the element is visible (the tab is
     * the open one) and nothing is open above it (a dialog over the book); a key without Ctrl, Alt or Meta fires only
     * when no input has the focus. On a Mac a combination with Cmd that no action has fires the action of the same combination with
     * Ctrl. Done once for an element.
     *
     * @param keys JSON object, action id to canonical key combination
     */
    public static void fireShortcutEvents(Element element, String keys) {
        element.executeJs(KEY_COMBO + """
                var el = $0;
                window.marginaliaKeys = JSON.parse($1);
                if (el.__shortcutHandler) { return; }
                var handler = function (e) {
                    if (!el.isConnected) {
                        document.removeEventListener('keydown', handler, true);
                        el.__shortcutHandler = null;
                        return;
                    }
                    if (e.defaultPrevented || e.isComposing || e.repeat || el.getClientRects().length === 0) { return; }
                    var combo = window.marginaliaKeyCombo(e);
                    if (!combo) { return; }
                    var keys = window.marginaliaKeys || {};
                    var find = function (c) { return Object.keys(keys).find(function (k) { return keys[k] === c; }); };
                    var id = find(combo);
                    if (!id && e.metaKey && !e.ctrlKey && /Mac/.test(navigator.platform || '')) {
                        // on a Mac Cmd is what Ctrl is elsewhere (Cmd+S is the browser's save), and a keyboard may swap the two
                        id = find('Ctrl+' + combo.replace('Meta+', ''));
                    }
                    if (!id) { return; }
                    var overlays = document.querySelectorAll('vaadin-dialog-overlay[opened], vaadin-confirm-dialog-overlay[opened]');
                    if (overlays.length > 0 && !overlays[overlays.length - 1].contains(el)) { return; }
                    var active = document.activeElement;
                    while (active && active.shadowRoot && active.shadowRoot.activeElement) { active = active.shadowRoot.activeElement; }
                    if (!/(Ctrl|Alt|Meta)\\+./.test(combo)) {
                        if (active && (['INPUT', 'TEXTAREA', 'SELECT'].indexOf(active.tagName) >= 0 || active.isContentEditable)) { return; }
                    }
                    // a field sends its value to the server on change (when it loses the focus): what the user typed must be there first
                    var field = active && active.closest ? active.closest('vaadin-text-area, vaadin-text-field') : null;
                    if (field) { field.dispatchEvent(new Event('change', { bubbles: true })); }
                    e.preventDefault();
                    e.stopPropagation();
                    el.dispatchEvent(new CustomEvent('marginalia-shortcut', { detail: id }));
                };
                el.__shortcutHandler = handler;
                document.addEventListener('keydown', handler, true);
                """, element, keys);
    }

    /**
     * The script that picks the most visible of {@code elements}: the one with the largest height inside the viewport and the scrolling
     * areas around it. Leaves the index in {@code best}, -1 when none is visible.
     */
    private static final String MOST_VISIBLE = """
            let best = -1;
            let bestHeight = 0;
            elements.forEach((el, index) => {
              if (!el || !el.isConnected) {
                return;
              }
              const rect = el.getBoundingClientRect();
              let top = Math.max(rect.top, 0);
              let bottom = Math.min(rect.bottom, window.innerHeight);
              for (let parent = el.parentElement; parent; parent = parent.parentElement) {
                if (parent.scrollHeight > parent.clientHeight && getComputedStyle(parent).overflowY !== 'visible') {
                  const box = parent.getBoundingClientRect();
                  top = Math.max(top, box.top);
                  bottom = Math.min(bottom, box.bottom);
                }
              }
              if (bottom - top > bestHeight) {
                bestHeight = bottom - top;
                best = index;
              }
            });
            """;

    /**
     * Finds out which of the elements the user means: the one that holds the text the user has selected, else the one that
     * is the most visible (the largest part of it inside the viewport and the scrolling areas around it).
     *
     * @return the index in {@code candidates} of the first element that contains the start of the selection, else of the
     * most visible element, -1 when nothing is selected and no element is visible
     */
    public static PendingJavaScriptResult selectionOwner(Element anchor, List<Element> candidates) {
        return anchor.executeJs("""
                const elements = [""" + placeholders(candidates) + """
                ];
                const selection = window.getSelection();
                if (selection && selection.rangeCount > 0 && !selection.isCollapsed) {
                  const node = selection.getRangeAt(0).startContainer;
                  const owner = elements.findIndex(el => el && el.contains(node));
                  if (owner >= 0) {
                    return owner;
                  }
                }
                """ + MOST_VISIBLE + """
                return best;
                """, concat(anchor, candidates));
    }

    /**
     * Finds out which of the elements is the most visible: the largest part of it inside the viewport and the scrolling areas
     * around it.
     *
     * @return the index in {@code candidates}, -1 when no element is visible
     */
    public static PendingJavaScriptResult mostVisible(Element anchor, List<Element> candidates) {
        return anchor.executeJs("""
                const elements = [""" + placeholders(candidates) + """
                ];
                """ + MOST_VISIBLE + """
                return best;
                """, concat(anchor, candidates));
    }

    /** {@code $2,$3...}: the references to the candidates in the arguments of a script ({@code $0} is the anchor). */
    private static String placeholders(List<Element> candidates) {
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < candidates.size(); i++) {
            list.append(i == 0 ? "" : ",").append('$').append(i + 1);
        }
        return list.toString();
    }

    private static Object[] concat(Element first, List<Element> rest) {
        List<Object> all = new ArrayList<>();
        all.add(first);
        all.addAll(rest);
        return all.toArray();
    }

    /**
     * Replaces the keys of the shortcuts on the page (the open story tabs use them from the next key on).
     *
     * @param keys JSON object, action id to canonical key combination
     */
    public static void setShortcutKeys(Page page, String keys) {
        page.executeJs("window.marginaliaKeys = JSON.parse($0);", keys);
    }

    /**
     * Records the next key combination the user presses: fires {@code key-recorded} ({@code detail}: the canonical
     * combination) on the element, or {@code key-record-cancelled} when the user clicks anywhere first. The key press does
     * nothing else (not even in the browser). Pressing a modifier alone waits for the key.
     */
    public static void recordKeyCombo(Element element) {
        element.executeJs(KEY_COMBO + """
                var el = $0;
                if (el.__keyRecorder) { el.__keyRecorder(null); }
                var finish = function (combo) {
                    window.removeEventListener('keydown', onKey, true);
                    window.removeEventListener('mousedown', onMouse, true);
                    el.__keyRecorder = null;
                    el.dispatchEvent(new CustomEvent(combo ? 'key-recorded' : 'key-record-cancelled', { detail: combo }));
                };
                var onKey = function (e) {
                    e.preventDefault();
                    e.stopPropagation();
                    var combo = window.marginaliaKeyCombo(e);
                    if (combo) { finish(combo); }
                };
                var onMouse = function () { finish(null); };
                el.__keyRecorder = finish;
                window.addEventListener('keydown', onKey, true);
                window.addEventListener('mousedown', onMouse, true);
                """, element);
    }

    /**
     * Gives the element the focus once the browser has rendered it and a menu that was just closed has given the focus
     * back to its button (a focus asked for from the server right away is lost to one of them): tries again until it has it.
     */
    public static void focusAfterRender(Element element) {
        element.executeJs("""
                var el = $0;
                var tries = 0;
                var timer = setInterval(function () {
                    el.focus();
                    var active = document.activeElement;
                    while (active && active.shadowRoot && active.shadowRoot.activeElement) { active = active.shadowRoot.activeElement; }
                    if (el.contains(active) || ++tries > 20) { clearInterval(timer); }
                }, 50);
                """, element);
    }

    /**
     * Gives the element the focus when a closing popover left it nowhere (on the page body or still in the popover), so the
     * keyboard goes on where the user was.
     */
    public static void focusIfFocusLost(Element element) {
        element.executeJs("""
                var el = $0;
                setTimeout(function () {
                    var active = document.activeElement;
                    if (!active || active === document.body || (active.closest && active.closest('vaadin-popover-overlay'))) { el.focus(); }
                }, 50);
                """, element);
    }

    // --- activity (the time of the writing statistics) ---

    /**
     * Tells the element, with the event {@code marginalia-visibility} (the detail is true or false), whether the page of the
     * browser is visible: now, and on every change. A book left open in a hidden tab does not count as open. Calling it again
     * replaces the earlier watch.
     */
    public static void watchPageVisibility(Element element) {
        element.executeJs("""
                var el = $0;
                if (el.__visibilityWatch) { el.__visibilityWatch(); }
                var send = function () {
                    el.dispatchEvent(new CustomEvent('marginalia-visibility', { detail: !document.hidden }));
                };
                document.addEventListener('visibilitychange', send);
                el.__visibilityWatch = function () {
                    document.removeEventListener('visibilitychange', send);
                    el.__visibilityWatch = null;
                };
                send();
                """, element);
    }

    /**
     * Stops {@link #watchPageVisibility}.
     */
    public static void unwatchPageVisibility(Element element) {
        element.executeJs("if ($0.__visibilityWatch) { $0.__visibilityWatch(); }", element);
    }

    /**
     * Tells the element, with the event {@code marginalia-typing} (the detail is true or false), whether the author is typing in
     * the text area: true on a key after an idle time, again at most once a minute while the keys go on (the server treats a
     * silence longer than the cut-off as idle, so a long run of typing must be heard now and then), false when the last key was
     * {@code cutoffMillis} ago. Only the keys of the text areas that were watched count; the mouse and the scrolling are reading.
     *
     * @param element      the element the events are fired on
     * @param textArea     the editor of a part (its element)
     * @param cutoffMillis the time after the last key when the author is idle
     */
    public static void watchTyping(Element element, Element textArea, int cutoffMillis) {
        element.executeJs("""
                var el = $0, area = $1, cutoff = $2;
                var state = el.__typing;
                if (!state) {
                    state = el.__typing = { areas: [], typing: false, timer: null, sent: 0 };
                }
                var send = function (typing) {
                    state.typing = typing;
                    state.sent = Date.now();
                    el.dispatchEvent(new CustomEvent('marginalia-typing', { detail: typing }));
                };
                if (area.__typingHandler) { return; }
                area.__typingHandler = function (e) {
                    if (e.isComposing && e.key === 'Process') { return; }
                    if (!state.typing || Date.now() - state.sent > 60000) { send(true); }
                    clearTimeout(state.timer);
                    state.timer = setTimeout(function () { state.timer = null; send(false); }, cutoff);
                };
                area.addEventListener('keydown', area.__typingHandler);
                state.areas.push(area);
                """, element, textArea, cutoffMillis);
    }

    /**
     * Stops watching the keys of the editor of a part; when it was the last one the author is idle.
     */
    public static void unwatchTyping(Element element, Element textArea) {
        element.executeJs("""
                var el = $0, area = $1, state = el.__typing;
                if (!state) { return; }
                if (area.__typingHandler) {
                    area.removeEventListener('keydown', area.__typingHandler);
                    area.__typingHandler = null;
                }
                state.areas = state.areas.filter(function (a) { return a !== area; });
                if (state.areas.length === 0) {
                    clearTimeout(state.timer);
                    state.timer = null;
                    if (state.typing) {
                        state.typing = false;
                        el.dispatchEvent(new CustomEvent('marginalia-typing', { detail: false }));
                    }
                }
                """, element, textArea);
    }

    // --- desktop notifications ---

    /**
     * Tells the element, with the event {@code marginalia-notification-permission} (the detail is {@code {state, requested}}), what the
     * browser lets the page do with notifications: now, and when the element is clicked ({@code state}: {@code granted},
     * {@code denied}, {@code default} when the browser has not been asked, or {@code unsupported} when the browser has no
     * notifications or the page is not served over HTTPS or from localhost). A click on the element (a button) asks the browser for the
     * permission when it has not been asked yet, from this very click, which browsers require ({@code requested} is true for the events
     * that follow a click). The permission belongs to the browser, not to the user. Calling it again replaces the earlier watch.
     */
    public static void watchNotificationPermission(Element element) {
        element.executeJs("""
                var el = $0;
                if (el.__notificationWatch) { el.__notificationWatch(); }
                var state = function () {
                    if (!('Notification' in window) || !window.isSecureContext) { return 'unsupported'; }
                    return Notification.permission;
                };
                var send = function (requested) {
                    el.dispatchEvent(new CustomEvent('marginalia-notification-permission',
                        { detail: { state: state(), requested: requested } }));
                };
                var onClick = function () {
                    if (state() !== 'default') { send(true); return; }
                    var done = function () { send(true); };
                    var result = Notification.requestPermission(done);
                    if (result && result.then) { result.then(done); }
                };
                el.addEventListener('click', onClick);
                el.__notificationWatch = function () {
                    el.removeEventListener('click', onClick);
                    el.__notificationWatch = null;
                };
                send(false);
                """, element);
    }

    /**
     * Stops {@link #watchNotificationPermission}.
     */
    public static void unwatchNotificationPermission(Element element) {
        element.executeJs("if ($0.__notificationWatch) { $0.__notificationWatch(); }", element);
    }

    /**
     * Shows a desktop notification of the browser. Nothing is shown unless the browser allows it and the user is not looking at
     * the page (the tab is hidden or the window has no focus), a notification about what is on the screen is noise. Clicking it brings the
     * page to the front.
     *
     * @param tag   a notification with the same tag replaces the earlier one
     * @param force show it also when the page is in front (the test button)
     * @return what happened: {@code shown}, {@code focused} (the user is looking at the page), or why nothing was shown:
     * {@code denied}, {@code default} (the browser was not asked), {@code unsupported} or {@code error}
     */
    public static PendingJavaScriptResult showDesktopNotification(Page page, String title, String body, String tag, boolean force) {
        return page.executeJs("""
                var title = $0, body = $1, tag = $2, force = $3;
                if (!('Notification' in window) || !window.isSecureContext) { return 'unsupported'; }
                if (Notification.permission !== 'granted') { return Notification.permission; }
                if (!force && !document.hidden && document.hasFocus()) { return 'focused'; }
                try {
                    var n = new Notification(title, { body: body, tag: tag });
                    n.onclick = function () { window.focus(); n.close(); };
                } catch (e) {
                    // browsers on phones only show them through a service worker
                    if (!navigator.serviceWorker) { return 'error'; }
                    navigator.serviceWorker.getRegistration().then(function (registration) {
                        if (registration) { registration.showNotification(title, { body: body, tag: tag }); }
                    });
                }
                return 'shown';
                """, title, body, tag, force);
    }

    // --- help assistant ---

    /** The sessionStorage key of the problems the browser keeps for the help assistant. */
    private static final String PROBLEMS_KEY = "marginalia.problems";

    /**
     * Adds a problem (a JSON object) to the ones the browser tab keeps in its sessionStorage, the last {@code keep} of
     * them: they survive a reload of the page, which the session's own copy does not outlive a new session.
     */
    public static void rememberProblem(Page page, String json, int keep) {
        page.executeJs("""
                try {
                  const list = JSON.parse(sessionStorage.getItem($0) || '[]');
                  list.push(JSON.parse($1));
                  sessionStorage.setItem($0, JSON.stringify(list.slice(-$2)));
                } catch (e) {}
                """, PROBLEMS_KEY, json, keep);
    }

    /** The problems the browser tab kept, as the text of a JSON array (empty when there are none or storage is blocked). */
    public static PendingJavaScriptResult readRememberedProblems(Page page) {
        return page.executeJs("""
                try { return sessionStorage.getItem($0) || '[]'; } catch (e) { return '[]'; }
                """, PROBLEMS_KEY);
    }

    /**
     * Makes the links inside the element (also those added later, like the answers of the help assistant) open in a
     * new tab, so that following a link does not leave the application.
     */
    public static void openLinksInNewTab(Element element) {
        element.executeJs("""
                if ($0.__linksInNewTab) return;
                $0.__linksInNewTab = true;
                $0.addEventListener('click', e => {
                  const link = e.composedPath().find(n => n.tagName === 'A' && n.href);
                  if (link && /^https?:/.test(link.href)) {
                    e.preventDefault();
                    window.open(link.href, '_blank', 'noopener');
                  }
                });
                """, element);
    }

    /**
     * Keeps the children of the element (the button and the panel of the help assistant) usable while a dialog is
     * open: a dialog and its curtain are shown in the top layer of the browser, above everything on the page and
     * blocking it, so while one is open (and again whenever another opens) the children are moved into the top layer
     * after it, as manual popovers. When the last dialog closes they are put back.
     */
    public static void keepOnTopOfDialogs(Element root) {
        root.executeJs("""
                if ($0.__keepOnTop || !HTMLElement.prototype.showPopover) return;
                $0.__keepOnTop = true;
                const root = $0;
                const dialogOpen = () => !!document.querySelector('vaadin-dialog[opened], vaadin-confirm-dialog[opened]');
                const sync = () => {
                  const modal = dialogOpen();
                  for (const el of root.children) {
                    if (modal && !el.hasAttribute('hidden')) {
                      if (!el.hasAttribute('popover')) el.setAttribute('popover', 'manual');
                      try { if (el.matches(':popover-open')) el.hidePopover(); el.showPopover(); } catch (e) {}
                    } else if (el.hasAttribute('popover')) {
                      try { el.hidePopover(); } catch (e) {}
                      el.removeAttribute('popover');
                    }
                  }
                };
                // the overlay of a dialog is shown a moment after it is marked as opened
                const later = () => { sync(); setTimeout(sync, 100); };
                // a notification (also marked 'opened') is in the top layer too, and the container of the notifications stays there
                // from the first one, so it would be below the button put there later: raise it above the button again
                const raiseNotifications = () => {
                  for (const c of document.querySelectorAll('vaadin-notification-container')) {
                    try { if (c.matches(':popover-open')) { c.hidePopover(); c.showPopover(); } } catch (e) {}
                  }
                };
                new MutationObserver(records => {
                  if (records.some(r => r.target.localName === 'vaadin-dialog' || r.target.localName === 'vaadin-confirm-dialog')) {
                    later();
                    setTimeout(raiseNotifications, 150);
                  } else if (records.some(r => r.target.localName === 'vaadin-notification' && r.target.opened) && dialogOpen()) {
                    raiseNotifications();
                  }
                }).observe(document.body, { subtree: true, attributes: true, attributeFilter: ['opened'] });
                new MutationObserver(records => { if (records.some(r => r.target.parentElement === root)) later(); })
                    .observe(root, { subtree: true, attributes: true, attributeFilter: ['hidden'] });
                later();
                """, root);
    }
}
