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

    /**
     * Resolves to the rounded {@code scrollTop} of the element.
     */
    public static PendingJavaScriptResult getScrollTop(Element element) {
        return element.executeJs("return Math.round($0.scrollTop);", element);
    }

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
     * Restores the scroll position once the layout is done; to the bottom when there is nothing saved.
     */
    public static void restoreScrollPosition(Element element, Integer savedPosition) {
        element.executeJs(INSTALL_STICKY_SCROLL + """
                installSticky($0);
                requestAnimationFrame(() => {
                  requestAnimationFrame(() => {
                    const el = $0;
                    const target = $1;
                    if (target !== null && target !== undefined) {
                      el.scrollTop = target;
                    } else {
                      el.scrollTop = el.scrollHeight;
                    }
                    el._lastTop = el.scrollTop;
                    el._stick = (el.scrollHeight - el.scrollTop - el.clientHeight) < 8;
                  });
                });
                """, element, savedPosition);
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

    // --- element events ---

    /**
     * Fires {@code panel-scroll} ({@code detail.scrollTop}) on the element, 300 ms after the user stops scrolling.
     */
    public static void fireDebouncedScrollEvent(Element element) {
        element.executeJs("""
                const el = $0;
                if (!el._hasScrollListener) {
                  el._hasScrollListener = true;
                  let timer;
                  el.addEventListener('scroll', () => {
                    clearTimeout(timer);
                    timer = setTimeout(() => {
                      el.dispatchEvent(new CustomEvent('panel-scroll', { detail: { scrollTop: Math.round(el.scrollTop) } }));
                    }, 300);
                  });
                }
                """, element);
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
