package com.github.enerccio.marginalia.extensions.reviewer.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.InferenceErrors;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceServices;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.extensions.reviewer.model.AdvancedOptions;
import com.github.enerccio.marginalia.extensions.reviewer.model.ReviewData;
import com.github.enerccio.marginalia.extensions.reviewer.model.ReviewItem;
import com.github.enerccio.marginalia.extensions.reviewer.model.ReviewerSettings;
import com.github.enerccio.marginalia.extensions.reviewer.service.ReviewerService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

@Configurable(preConstruction = true)
public class ReviewDialog extends Dialog {

    @Autowired
    private InferenceServices inferenceServices;

    @Autowired
    private Localization loc;

    private ChatMessage message;
    private Manuscript manuscript;
    private final ReviewerService reviewerService;

    private ReviewData reviewData;
    private AdvancedOptions pendingAdvancedOptions;

    private Button leftSwipeBtn;
    private Button rightSwipeBtn;
    private Span counterSpan;
    private Button stopContinueBtn;

    private Details reasoningDetails;
    private Markdown reasoningMarkdown;
    private Markdown reviewMarkdown;
    private TextArea reviewEditArea;

    private boolean editing = false;
    private CancellationToken isGenerating;

    public ReviewDialog(ReviewerService reviewerService,
            ChatMessage message,
            Manuscript manuscript,
            AdvancedOptions advancedOptions) {

        this.reviewerService = reviewerService;
        this.message = message;
        this.manuscript = manuscript;
        this.pendingAdvancedOptions = advancedOptions;

        setHeaderTitle("Message Review");
        setWidth("80vw");
        setHeight("80vh");

        loadData();
        buildUI();

        if (reviewData.getReviews().get(reviewData.getCurrent()).getText().isEmpty()) {
            startGeneration();
        } else {
            displayReview();
        }
    }

    private void loadData() {
        reviewData = reviewerService.getReviewData(message);
        if (reviewData == null) {
            reviewData = new ReviewData();
            persistData();
        }
    }

    private void persistData() {
        try {
            message = reviewerService.saveReviewData(message, reviewData);
        } catch (Exception ignored) {}
    }

    private void buildUI() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);

        HorizontalLayout controlsHeader = new HorizontalLayout();
        controlsHeader.setWidthFull();
        controlsHeader.setAlignItems(FlexComponent.Alignment.CENTER);

        stopContinueBtn = new Button("Stop generating", e -> toggleGeneration());
        stopContinueBtn.setThemeName("error primary");

        leftSwipeBtn = new Button(Solid.CHEVRON_LEFT.create(), e -> shiftLeft());
        rightSwipeBtn = new Button(Solid.CHEVRON_RIGHT.create(), e -> shiftRight());
        counterSpan = new Span("0 / 0");

        controlsHeader.add(stopContinueBtn, UIUtils.voidComponent(), leftSwipeBtn, counterSpan, rightSwipeBtn);

        reasoningMarkdown = new Markdown();
        reasoningDetails = new Details("Thinking Process", reasoningMarkdown);
        reasoningDetails.setWidthFull();
        reasoningDetails.setVisible(false);

        reviewMarkdown = new Markdown();
        reviewMarkdown.setWidthFull();
        reviewMarkdown.getElement().addEventListener("click", e -> toggleEdit());

        reviewEditArea = new TextArea();
        reviewEditArea.setSizeFull();
        reviewEditArea.setVisible(false);
        reviewEditArea.addBlurListener(e -> confirmEdit());

        ScrollPanel scrollPanel = new ScrollPanel();
        scrollPanel.setSizeFull();
        scrollPanel.add(new VerticalLayout(reasoningDetails, reviewMarkdown, reviewEditArea));

        mainLayout.add(controlsHeader, scrollPanel);
        mainLayout.setFlexGrow(1, scrollPanel);
        add(mainLayout);

        Button closeBtn = new Button("Close", e -> close());
        getFooter().add(closeBtn);
    }

    private void displayReview() {
        ReviewItem item = reviewData.getReviews().get(reviewData.getCurrent());

        if (item.getMetadata() != null && !item.getMetadata().getReasoning().isEmpty()) {
            reasoningMarkdown.setContent(item.getMetadata().getReasoning());
            reasoningDetails.setVisible(true);
        } else {
            reasoningDetails.setVisible(false);
        }

        reviewMarkdown.setContent(item.getText().isEmpty() ? "Generating review, please wait..." : item.getText());
        counterSpan.setText((reviewData.getCurrent() + 1) + " / " + reviewData.getReviews().size());

        stopContinueBtn.setEnabled(isGenerating != null && !isGenerating.isCancelled());
    }

    private void shiftLeft() {
        if (reviewData.getCurrent() > 0) {
            reviewData.setCurrent(reviewData.getCurrent() - 1);
            persistData();
            displayReview();
        }
    }

    private void shiftRight() {
        if (reviewData.getCurrent() < reviewData.getReviews().size() - 1) {
            reviewData.setCurrent(reviewData.getCurrent() + 1);
            persistData();
            displayReview();
        } else {
            ReviewItem newItem = new ReviewItem();
            reviewData.getReviews().add(newItem);
            reviewData.setCurrent(reviewData.getReviews().size() - 1);
            persistData();
            startGeneration();
        }
    }

    private void toggleEdit() {
        if (isGenerating != null && !isGenerating.isCancelled()) return;
        editing = true;
        ReviewItem item = reviewData.getReviews().get(reviewData.getCurrent());
        reviewEditArea.setValue(item.getText());
        reviewMarkdown.setVisible(false);
        reviewEditArea.setVisible(true);
        reviewEditArea.focus();
    }

    private void confirmEdit() {
        if (!editing) return;
        ReviewItem item = reviewData.getReviews().get(reviewData.getCurrent());
        item.setText(reviewEditArea.getValue());
        persistData();
        reviewEditArea.setVisible(false);
        reviewMarkdown.setVisible(true);
        editing = false;
        displayReview();
    }

    private void toggleGeneration() {
        if (isGenerating != null && !isGenerating.isCancelled()) {
            isGenerating.cancel();
            displayReview();
        }
    }

    private void startGeneration() {
        if (isGenerating != null) {
            isGenerating.cancel();
        }
        displayReview();
        UI ui = UI.getCurrent();

        try {
            ReviewerSettings settings = reviewerService.getSettings();
            AI targetAi = reviewerService.resolveAI(settings, manuscript);
            Protocol targetProtocol = reviewerService.resolveProtocol(settings, manuscript);
            InferenceService service = inferenceServices.forAI(targetAi);

            AdvancedOptions opts = pendingAdvancedOptions != null ? pendingAdvancedOptions : new AdvancedOptions();

            List<LLMChatMessage> payload = reviewerService.buildChatCompletePrompts(service,
                    message, settings, opts);

            ReviewItem currentItem = reviewData.getReviews().get(reviewData.getCurrent());
            currentItem.getMetadata().setAdvancedInfo(opts);

            isGenerating = service.stream(payload, targetProtocol, new InferenceService.InferenceAsyncCallback() {
                private final StringBuilder responseBuf = new StringBuilder();
                private final StringBuilder reasoningBuf = new StringBuilder();

                @Override
                public void onChunk(InferenceService.InferenceAsyncController controller, InferenceService.ChunkType chunkType, String text) {
                    ui.access(() -> {
                        if (chunkType == InferenceService.ChunkType.REASONING) {
                            reasoningBuf.append(text);
                            currentItem.getMetadata().setReasoning(reasoningBuf.toString());
                        } else {
                            responseBuf.append(text);
                            currentItem.setText(responseBuf.toString());
                        }
                        displayReview();
                        UIPushGuard.push(ui);
                        controller.continueInference();
                    });
                }

                @Override
                public void onCompletion() {
                    ui.access(() -> {
                        isGenerating = null;
                        persistData();
                        displayReview();
                        UIPushGuard.push(ui);
                    });
                }

                @Override
                public void onCancel() {
                    ui.access(() -> {
                        isGenerating = null;
                        displayReview();
                        UIPushGuard.push(ui);
                    });
                }

                @Override
                public void onError(Throwable exception) {
                    ui.access(() -> {
                        isGenerating = null;
                        currentItem.setText("Error generating review: " + InferenceErrors.messageOf(loc, exception));
                        displayReview();
                        UIPushGuard.push(ui);
                    });
                }

                @Override
                public boolean isDead() {
                    return isGenerating != null && isGenerating.isCancelled();
                }
            });

        } catch (Exception e) {
            if (isGenerating != null) {
                isGenerating.cancel();
                isGenerating = null;
            }
            ReviewItem currentItem = reviewData.getReviews().get(reviewData.getCurrent());
            currentItem.setText("Failed to start inference: " + e.getMessage());
            UIUtils.internalServerError(loc, e);
            displayReview();
        }
    }
}