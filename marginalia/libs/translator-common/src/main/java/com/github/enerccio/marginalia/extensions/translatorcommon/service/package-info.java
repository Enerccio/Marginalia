/**
 * Services and run loops that the translator plugins share. Everything here is parameterized by the plugin (the passage kind, the name of the worker
 * threads, the work to do), and none of it keeps state that two plugins would have to share.
 * <ul>
 *   <li>{@link com.github.enerccio.marginalia.extensions.translatorcommon.service.PassageAccess}: reads the translated passages of the parts for one
 *   passage kind.</li>
 *   <li>{@link com.github.enerccio.marginalia.extensions.translatorcommon.service.ModelClient}: asks a model one question and waits for the answer.</li>
 *   <li>{@link com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop} and
 *   {@link com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeRuns}: an action over a range of parts, one part after the other, on a
 *   worker thread of the user's session, with a stop.</li>
 *   <li>{@link com.github.enerccio.marginalia.extensions.translatorcommon.service.PartRuns} and
 *   {@link com.github.enerccio.marginalia.extensions.translatorcommon.service.AsyncRuns}: one job on a worker thread, the result handed to the page.</li>
 *   <li>{@link com.github.enerccio.marginalia.extensions.translatorcommon.service.PluginLog}: the logger of the extension, for the code that has none.</li>
 * </ul>
 */
package com.github.enerccio.marginalia.extensions.translatorcommon.service;
