package com.github.enerccio.marginalia.domain.service.manual;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.traits.LLMFunction;
import com.github.enerccio.marginalia.domain.traits.LLMParam;

/**
 * What the help assistant's model may read of the Admin page: the state of the extensions, the users, the backups, the
 * cleanup, the application and the logs of the extensions. Given to the model only for an administrator, and only when
 * an administrator allowed it ({@code helpAdminData} in the application settings). Read only.
 */
public interface AdminHelpTools {

    @LLMFunction(name = "get_admin_data", description = "Reads the state of the Marginalia installation, what the Admin "
            + "page shows: topic extensions (every installed extension with its version, whether it is running, "
            + "stopped or failed, and the result of its verification), users (how many, how many administrators or "
            + "locked; no names), backups (the database backups, the schedule, the size of the database), cleanup "
            + "(what is deleted and waiting to be purged, what blocks it), application (Java, memory, uptime). Call it "
            + "when the administrator asks why something does not work or what state something is in.")
    void getAdminData(FunctionContinuation<String> done,
                      @LLMParam(description = "One of: extensions, users, backups, cleanup, application") String topic);

    @LLMFunction(name = "read_extension_log", description = "Reads the last lines of the log of an extension (what it "
            + "wrote itself, errors and warnings among them), newest last: the same as Admin > Extensions > Log. Use it "
            + "when an extension fails or misbehaves; get_admin_data with topic extensions lists their names.")
    void readExtensionLog(FunctionContinuation<String> done,
                          @LLMParam(description = "Name of the extension as get_admin_data listed it") String extension,
                          @LLMParam(description = "How many of the last lines to read (default 40, at most 200)",
                                  required = false) int lines);
}
