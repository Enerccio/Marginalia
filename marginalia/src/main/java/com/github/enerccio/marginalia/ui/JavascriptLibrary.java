package com.github.enerccio.marginalia.ui;

import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.dom.Element;

/**
 * All the JavaScript the application runs in the browser from the server side. Every method wraps one script, the
 * element it works on is always passed as {@code $0}.
 */
public final class JavascriptLibrary {

    private JavascriptLibrary() {
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
                new MutationObserver(later).observe(document.body,
                    { subtree: true, attributes: true, attributeFilter: ['opened'] });
                new MutationObserver(records => { if (records.some(r => r.target.parentElement === root)) later(); })
                    .observe(root, { subtree: true, attributes: true, attributeFilter: ['hidden'] });
                later();
                """, root);
    }
}
