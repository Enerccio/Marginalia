package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.traits.LLMFunction;
import com.github.enerccio.marginalia.domain.traits.LLMParam;

/**
 * What an agent whose model can look at images does with the image files of its workspace. Only a profile that is
 * marked as seeing images gets it ({@link com.github.enerccio.marginalia.domain.model.AgentModelProfile#isImages()}),
 * the runner binds it next to the file tools (see {@link ImageToolsImpl}).
 */
public interface ImageTools {

    @LLMFunction(name = "view_image", description = "Looks at an image file of your workspace (PNG, JPEG, GIF or WebP): "
            + "the picture is shown to you in the message that follows this result. A large image is scaled down, a GIF "
            + "shows its first frame. Only the last few images you looked at stay in front of you, look again at one you "
            + "need later. read_file does not open images; the user puts images in the workspace with the Upload button.")
    void viewImage(FunctionContinuation<String> done,
                   @LLMParam(description = "Path of the image, for example /refs/map.png") String path);

}
