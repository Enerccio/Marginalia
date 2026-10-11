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

    public static void setScrollLeft(Element element, int pixelsFromLeft) {
        element.executeJs("$0.scrollLeft = $1", element, pixelsFromLeft);
    }

    public static void scrollToBottom(Element element) {
        element.executeJs("$0.scrollTop = $0.scrollHeight", element);
    }

    /**
     * Scrolls to the bottom once the layout following the current changes is done.
     */
    public static void scrollToBottomAfterLayout(Element element) {
        element.executeJs("""
                requestAnimationFrame(() => {
                  requestAnimationFrame(() => {
                    $0.scrollTop = $0.scrollHeight;
                  });
                });
                """, element);
    }

    /**
     * Scrolls to the bottom when the element is already (nearly) at the bottom, so a user who scrolled up is left alone.
     */
    public static void scrollToBottomIfAtBottom(Element element) {
        element.executeJs("""
                requestAnimationFrame(() => {
                  const el = $0;
                  const isAtBottom = (el.scrollHeight - el.scrollTop - el.clientHeight) < 100;
                  if (isAtBottom) {
                    el.scrollTop = el.scrollHeight;
                  }
                });
                """, element);
    }

    /**
     * Restores the scroll position once the layout is done; to the bottom when there is nothing saved.
     */
    public static void restoreScrollPosition(Element element, Integer savedPosition) {
        element.executeJs("""
                requestAnimationFrame(() => {
                  requestAnimationFrame(() => {
                    const el = $0;
                    const target = $1;
                    if (target !== null && target !== undefined) {
                      el.scrollTop = target;
                    } else {
                      el.scrollTop = el.scrollHeight;
                    }
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

    /**
     * Fires {@code panel-resize} ({@code detail.width}) on the element, 150 ms after its width changes.
     */
    public static void fireDebouncedResizeEvent(Element element) {
        element.executeJs("""
                const el = $0;
                if (!el._hasResizeObserver) {
                  el._hasResizeObserver = true;
                  let timer;
                  const observer = new ResizeObserver(entries => {
                    for (let entry of entries) {
                      const w = Math.round(entry.contentRect.width);
                      if (w > 0) {
                        clearTimeout(timer);
                        timer = setTimeout(() => {
                          el.dispatchEvent(new CustomEvent('panel-resize', { detail: { width: w } }));
                        }, 150);
                      }
                    }
                  });
                  observer.observe(el);
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
}
