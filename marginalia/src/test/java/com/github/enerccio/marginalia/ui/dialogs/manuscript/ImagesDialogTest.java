package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.ImageAnchor;
import com.github.enerccio.marginalia.domain.model.ImageAttachment;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.components.PartView;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.IntegerField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Where the images of a part are placed (the dialog of the images of the part), and how the viewer and the preview of the editor show
 * the part.
 */
class ImagesDialogTest extends MarginaliaTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private Localization loc;

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
    private UI ui;

    private Manuscript book;
    private ChatMessage part;
    private Resource image;
    private AtomicReference<ChatMessage> changed;
    private boolean previewed;
    private ImagesDialog dialog;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        book = manuscriptService.save(manuscript);
        part = chatMessageService.createPart(book, null, "One.\n\nTwo.\n\nThree.", null);
        BufferedImage picture = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(picture, "png", png);
        image = resourceService.uploadImage("map.png", png.toByteArray(), ChatMessage.class, part.getId());
        part.setImages(List.of(new ImageAttachment(image.getUuid(), "The map")));
        part = chatMessageService.save(part);
        changed = new AtomicReference<>();
        dialog = new ImagesDialog(part, changed::set, () -> previewed = true);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
        ui = null;
    }

    private static Stream<Component> all(Component root) {
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(ImagesDialogTest::all));
    }

    private Stream<Component> components() {
        return Stream.concat(all(dialog), dialog.getFooter().getElement().getChildren()
                .flatMap(e -> e.getComponent().stream()).flatMap(ImagesDialogTest::all));
    }

    private Button button(String label) {
        return components().filter(Button.class::isInstance).map(Button.class::cast)
                .filter(b -> label.equals(b.getText()) || label.equals(b.getAriaLabel().orElse(null))).findFirst().orElseThrow();
    }

    private Checkbox checkbox(String label) {
        return components().filter(Checkbox.class::isInstance).map(Checkbox.class::cast)
                .filter(c -> label.equals(c.getLabel())).findFirst().orElseThrow();
    }

    private IntegerField width() {
        return components().filter(IntegerField.class::isInstance).map(IntegerField.class::cast).findFirst().orElseThrow();
    }

    private ImageAttachment saved() throws Exception {
        return chatMessageService.find(part.getId()).getImages().getFirst();
    }

    private static <T> void fromClientEvent(com.vaadin.flow.component.AbstractField<?, T> field, T old) {
        ComponentUtil.fireEvent(field, new ComponentValueChangeEvent<>(field, field, old, true));
    }

    @Test
    void anImageIsAfterTheTextUntilTheAuthorPlacesIt() throws Exception {
        assertThat(saved().anchor()).isNull();
        assertThat(button(loc.getValue(L.ENUM_IMAGE_ANCHOR_BOTTOM_CENTER)).getThemeName()).contains("primary");
        assertThat(checkbox(loc.getValue(L.LABEL_AUTOMATIC)).getValue()).isTrue();
        assertThat(width().isEnabled()).isFalse();
    }

    @Test
    void thePlaceIsChosenInThePickerAndSaved() throws Exception {
        button(loc.getValue(L.ENUM_IMAGE_ANCHOR_TOP_LEFT)).click();

        assertThat(saved().anchor()).isEqualTo(ImageAnchor.TOP_LEFT);
        assertThat(changed.get().getImages().getFirst().anchor()).isEqualTo(ImageAnchor.TOP_LEFT);
        // the picker shows the new place, the caption is kept
        assertThat(button(loc.getValue(L.ENUM_IMAGE_ANCHOR_TOP_LEFT)).getThemeName()).contains("primary");
        assertThat(saved().caption()).isEqualTo("The map");
    }

    @Test
    void theTextCanFlowAroundAnImageAtTheSideOrOnBothSidesOfOneInTheMiddle() throws Exception {
        button(loc.getValue(L.ENUM_IMAGE_ANCHOR_CENTER_RIGHT)).click();
        Checkbox wrap = checkbox(loc.getValue(L.LABEL_IMAGE_WRAP));
        assertThat(wrap.isEnabled()).isTrue();
        wrap.setValue(true);
        fromClientEvent(wrap, false);

        assertThat(saved().wrap()).isTrue();
        assertThat(saved().anchor()).isEqualTo(ImageAnchor.CENTER_RIGHT);
        // the width has less room, the text needs the rest
        assertThat(width().getMax()).isEqualTo(ImageAttachment.MAX_WRAPPED_WIDTH);

        // in the middle the text is on both sides, with even less room for the picture
        button(loc.getValue(L.ENUM_IMAGE_ANCHOR_CENTER_CENTER)).click();
        assertThat(saved().wrap()).isTrue();
        assertThat(checkbox(loc.getValue(L.LABEL_IMAGE_WRAP)).isEnabled()).isTrue();
        assertThat(width().getMax()).isEqualTo(ImageAttachment.MAX_SURROUNDED_WIDTH);
    }

    @Test
    void theWidthIsAutomaticOrAPercentOfTheText() throws Exception {
        Checkbox automatic = checkbox(loc.getValue(L.LABEL_AUTOMATIC));
        automatic.setValue(false);
        fromClientEvent(automatic, true);

        assertThat(saved().width()).isNotNull();
        IntegerField field = width();
        assertThat(field.isEnabled()).isTrue();
        Integer old = field.getValue();
        field.setValue(40);
        ComponentUtil.fireEvent(field, new ComponentValueChangeEvent<>(field, field, old, true));
        assertThat(saved().width()).isEqualTo(40);

        automatic = checkbox(loc.getValue(L.LABEL_AUTOMATIC));
        automatic.setValue(true);
        fromClientEvent(automatic, false);
        assertThat(saved().width()).isNull();
    }

    @Test
    void theCaptionIsKeptWhenThePlaceChanges() throws Exception {
        button(loc.getValue(L.ENUM_IMAGE_ANCHOR_BOTTOM_RIGHT)).click();
        button(loc.getValue(L.ENUM_IMAGE_ANCHOR_TOP_CENTER)).click();

        ImageAttachment kept = saved();
        assertThat(kept.caption()).isEqualTo("The map");
        assertThat(kept.resource()).isEqualTo(image.getUuid());
        assertThat(kept.anchor()).isEqualTo(ImageAnchor.TOP_CENTER);
    }

    @Test
    void theDialogOffersThePreviewOfThePartAsTheViewerShowsIt() {
        button(loc.getValue(L.LABEL_PREVIEW_AS_VIEWER)).click();

        assertThat(previewed).isTrue();
    }

    @Test
    void theViewerAndThePreviewPutThePicturesAmongTheText() {
        ImageAttachment top = new ImageAttachment(image.getUuid(), "Top", ImageAnchor.TOP_RIGHT, true, 30);
        ImageAttachment end = new ImageAttachment(image.getUuid(), "End", ImageAnchor.BOTTOM_LEFT, false, null);

        PartView view = new PartView("One.\n\nTwo.\n\nThree.", List.of(end, top));

        List<String> shape = view.getChildren().map(c -> c.getClassNames().contains(CssStyles.PART_PICTURES) ? "pictures" : "text").toList();
        assertThat(shape).containsExactly("pictures", "text", "pictures");
        assertThat(view.getClassNames()).contains(CssStyles.PART_VIEW);

        Div floated = (Div) all(view).filter(c -> c.getClassNames().contains(CssStyles.PART_PICTURE_FLOAT_RIGHT)).findFirst().orElseThrow();
        assertThat(floated.getStyle().get("width")).isEqualTo("30%");
        assertThat(floated.getClassNames()).contains(CssStyles.PART_PICTURE, CssStyles.PART_PICTURE_SIZED);

        Div block = (Div) all(view).filter(c -> c.getClassNames().contains(CssStyles.PART_PICTURES_LEFT)).findFirst().orElseThrow();
        assertThat(block.getChildren().findFirst().orElseThrow().getStyle().get("width")).isNull();
    }

    @Test
    void imagesInTheMiddleOfTheTextAreBetweenTwoColumnsOfIt() {
        String lorem = " lorem".repeat(40);
        ImageAttachment middle = new ImageAttachment(image.getUuid(), "Mid", ImageAnchor.TOP_CENTER, true, 25);

        PartView view = new PartView("One" + lorem + "\n\nTwo" + lorem + "\n\nThree" + lorem, List.of(middle));

        Div band = (Div) all(view).filter(c -> c.getClassNames().contains(CssStyles.PART_BAND)).findFirst().orElseThrow();
        assertThat(band.getStyle().get("grid-template-columns")).isEqualTo("1fr 25% 1fr");
        List<Component> cells = band.getChildren().toList();
        assertThat(cells).hasSize(3);
        assertThat(cells.get(0).getClassNames()).contains(CssStyles.PART_BAND_TEXT);
        assertThat(cells.get(1).getClassNames()).contains(CssStyles.PART_BAND_MIDDLE);
        assertThat(cells.get(2).getClassNames()).contains(CssStyles.PART_BAND_TEXT);
    }

    @Test
    void aPartWithoutTextAndImagesHasNothingToShow() {
        assertThat(new PartView("  ", List.of()).getComponentCount()).isZero();
        // an image that is gone is left out, the text stays
        PartView view = new PartView("Only text.", List.of(new ImageAttachment("no-such-uuid", "gone")));
        assertThat(view.getChildren().toList()).hasSize(1);
    }

    @Test
    void thePreviewDialogShowsThePartInTheViewerWidthOrAPhoneWidth() {
        PartPreviewDialog preview = new PartPreviewDialog("One.\n\nTwo.", List.of(new ImageAttachment(image.getUuid(), "Cap")), null);

        assertThat(all(preview).anyMatch(PartView.class::isInstance)).isTrue();
    }
}
