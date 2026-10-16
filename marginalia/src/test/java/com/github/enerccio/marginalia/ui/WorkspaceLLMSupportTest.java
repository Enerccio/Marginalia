package com.github.enerccio.marginalia.ui;

import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceLLMSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What a workspace tells the help assistant about its sections (the workspaces themselves need the OSGi service, which
 * the test context does not have).
 */
class WorkspaceLLMSupportTest extends MarginaliaTestBase {

    @Autowired
    private Localization loc;

    @Test
    void namesTheSectionsByTheirLabelsAndTheOpenOne() {
        String state = WorkspaceLLMSupport.describe(loc, "desktop", List.of("books", "lorebooks", "models", "trash"), "models", false);

        assertThat(state).contains("desktop layout").contains(loc.getValue(com.github.enerccio.marginalia.loc.L.LABEL_BOOKS))
                .contains("The open section is **" + loc.getValue(com.github.enerccio.marginalia.loc.L.LABEL_MODELS) + "**.")
                .doesNotContain("unsaved");
    }

    @Test
    void saysWhenTheNavigationIsLocked() {
        String state = WorkspaceLLMSupport.describe(loc, "phone", List.of("books"), "books", true);

        assertThat(state).contains("phone layout").contains("unsaved changes");
    }

    @Test
    void anUnknownKeyIsShownAsItIs() {
        assertThat(WorkspaceLLMSupport.describe(loc, "desktop", List.of("extra"), "extra", false)).contains("extra");
    }

    @Test
    void offersEverySectionButTheOpenOneByItsLabel() {
        List<String> opened = new ArrayList<>();
        Map<String, Runnable> sections = new LinkedHashMap<>();
        for (String key : List.of("books", "lorebooks", "admin")) {
            sections.put(key, () -> opened.add(key));
        }

        Map<String, LLMInteraction> offered = WorkspaceLLMSupport.openSections(loc, sections, "books", false);

        String lorebooks = loc.getValue(com.github.enerccio.marginalia.loc.L.LABEL_LOREBOOKS);
        String admin = loc.getValue(com.github.enerccio.marginalia.loc.L.LABEL_ADMIN);
        assertThat(offered).containsOnlyKeys("open-section:" + lorebooks, "open-section:" + admin);
        assertThat(offered.get("open-section:" + lorebooks).description()).isEqualTo("Open the section \"" + lorebooks + "\".");
        offered.get("open-section:" + lorebooks).action().run();
        assertThat(opened).containsExactly("lorebooks");
    }

    @Test
    void offersNothingWhileTheNavigationIsLocked() {
        Map<String, Runnable> sections = new LinkedHashMap<>();
        sections.put("books", () -> {});
        sections.put("lorebooks", () -> {});

        assertThat(WorkspaceLLMSupport.openSections(loc, sections, "books", true)).isEmpty();
    }
}
