package com.github.enerccio.marginalia.extensions.reviewer.model;

/**
 * A reviewer profile as the comboboxes list it.
 *
 * @param uuid {@link ReviewerSetting#getUuid()}
 * @param name name of the profile in {@link ReviewerSettings#getSettings()}
 */
public record ReviewerProfile(String uuid, String name) {
}
