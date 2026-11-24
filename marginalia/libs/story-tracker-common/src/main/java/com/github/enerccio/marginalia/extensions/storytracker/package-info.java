/**
 * <b>story-tracker-common</b>: what the plugins that follow the story share (the Knowledge Tracker and the State Tracker). A plain jar that each plugin embeds in its bundle ({@code Embed-Dependency story-tracker-common;inline=true}); it is never
 * loaded as an extension. See the developer manual, <i>Plugin development / Libraries / story-tracker-common</i>.
 * <p>
 * <b>The idea.</b> A tracker keeps a state per book, saved with the book, and reads the parts of the active branch in order from a start
 * point; a cursor says how far it got. Its state implements {@code process.TrackerProgress} and {@code process.BranchProcessor} keeps it
 * in line with the branch (a swipe, another branch, a reprocess take back what came from the parts that are no longer read), reads the
 * settled parts and lets the tracker note what it notes without a model. {@code scene.PresentCharacters} reads the present characters
 * field leniently, {@code block.PayloadInsertion} puts the tracker's block into the prompt like the Author's Note,
 * {@code process.PartReader} lets the model read earlier parts.
 * <p>
 * <b>Rules of a library</b> (each plugin has its own copy of every class): no state two plugins would have to share, what differs is a
 * parameter, and objects are never handed from one plugin to the other.
 */
package com.github.enerccio.marginalia.extensions.storytracker;
