package com.github.enerccio.marginalia.loc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class LocalizationEN extends LocalizationBase {

    @Override
    protected void loadMessages() {
        setValue(L.LABEL_YES, "Yes");
        setValue(L.LABEL_NO, "No");
        setValue(L.LABEL_OK, "OK");
        setValue(L.LABEL_CANCEL, "Cancel");
        setValue(L.LABEL_SAVE, "Save");
        setValue(L.LABEL_EXIT, "Exit");
        setValue(L.LABEL_USERNAME, "Username");
        setValue(L.LABEL_USER_FULLNAME, "Full Name");
        setValue(L.LABEL_EMAIL, "Email");
        setValue(L.LABEL_LOGIN, "Login");
        setValue(L.LABEL_LOGOUT, "Logout");
        setValue(L.LABEL_EXIT_APPLICATION, "Close application");
        setValue(L.LABEL_PASSWORD, "Password");
        setValue(L.LABEL_PASSWORD_AGAIN, "Repeat password");
        setValue(L.LABEL_FORGOT_PASSWORD, "Forgotten password?");
        setValue(L.LABEL_SAVE_LOGIN, "Save login");
        setValue(L.LABEL_AUTHENTICATION_FAILED, "Failed to authenticate");
        setValue(L.LABEL_APPLICATION_ERROR, "Application Error");
        setValue(L.LABEL_MODELS, "Inference Providers");
        setValue(L.LABEL_ADD_MODEL, "Add Inference Provider");
        setValue(L.LABEL_TYPE, "Type");
        setValue(L.LABEL_NAME, "Name");
        setValue(L.LABEL_EDIT, "Edit");
        setValue(L.LABEL_ADMIN, "Admin");
        setValue(L.LABEL_EDIT_AI, "Edit Inference Provider");
        setValue(L.LABEL_NEW_AI, "New Inference Provider");
        setValue(L.LABEL_URL, "OpenAI URL (ends with /v1)");
        setValue(L.LABEL_API_KEY, "Api Key");
        setValue(L.LABEL_RESET, "Reset Api Key");
        setValue(L.LABEL_MODEL, "Model");
        setValue(L.LABEL_REFRESH, "Refresh");
        setValue(L.LABEL_MAX_CONTEXT, "Max Context Size");
        setValue(L.LABEL_MAX_RESPONSE, "Max Response Tokens");
        setValue(L.LABEL_ADDITIONAL_PARAMETERS, "Additional Parameters");
        setValue(L.LABEL_ADMINISTRATOR, "Administrator");
        setValue(L.LABEL_GENERAL_SETTINGS, "General Settings");
        setValue(L.LABEL_TYPE_SETTINGS, "Per Type Settings");
        setValue(L.LABEL_JAILBREAK, "Jailbreak Prompt");
        setValue(L.LABEL_NEEDS_JAILBREAK, "Needs Jailbreak");
        setValue(L.LABEL_ENABLED_REASONING, "Enable Reasoning");
        setValue(L.LABEL_REASONING_EFFORT, "Reasoning Effort");
        setValue(L.LABEL_PROTOCOLS, "Protocols");
        setValue(L.LABEL_ADD_PROTOCOL, "Add Protocol");
        setValue(L.LABEL_EDIT_PROTOCOL, "Edit Protocol");
        setValue(L.LABEL_NEW_PROTOCOL, "New Protocol");
        setValue(L.LABEL_MAX_TOKENS, "Max Context Tokens");
        setValue(L.LABEL_REPLY_TOKENS, "Max Reply Tokens");
        setValue(L.LABEL_ENABLE_TEMPERATURE, "Enable Temperature");
        setValue(L.LABEL_TEMPERATURE, "Temperature");
        setValue(L.LABEL_ENABLE_TOP_P, "Enable Top P");
        setValue(L.LABEL_TOP_P, "Top P");
        setValue(L.LABEL_ENABLE_FREQUENCY_PENALTY, "Enable Frequency Penalty");
        setValue(L.LABEL_FREQUENCY_PENALTY, "Frequency Penalty");
        setValue(L.LABEL_ENABLE_PRESENCE_PENALTY, "Enable Presence Penalty");
        setValue(L.LABEL_PRESENCE_PENALTY, "Presence Penalty");
        setValue(L.LABEL_SETTINGS, "Settings");
        setValue(L.LABEL_DEFAULT_MODEL, "Default Model");
        setValue(L.LABEL_DEFAULT_PROTOCOL, "Default Protocol");
        setValue(L.LABEL_TEMPLATES, "Templates");
        setValue(L.LABEL_MASTER_TEMPLATE, "Master Template");
        setValue(L.LABEL_POV, "Point of View (POV)");
        setValue(L.LABEL_TENSE, "Tense");
        setValue(L.LABEL_STYLE, "Style");
        setValue(L.LABEL_USER_PROMPT, "Default User Prompt");
        setValue(L.LABEL_AVAILABLE_VARIABLES, "Available Template Variables");
        setValue(L.LABEL_FILL_DEFAULT, "Insert Default Template");
        setValue(L.LABEL_LOREBOOKS, "Lorebooks");
        setValue(L.LABEL_ADD_LOREBOOK, "Add Lorebook");
        setValue(L.LABEL_EDIT_LOREBOOK, "Edit Lorebook");
        setValue(L.LABEL_NEW_LOREBOOK, "New Lorebook");
        setValue(L.LABEL_DELETE_LOREBOOK, "Delete Lorebook");
        setValue(L.LABEL_SUB_LOREBOOKS, "Sub Lorebooks");
        setValue(L.LABEL_ADD_ENTRY, "Add Entry");
        setValue(L.LABEL_DELETE_ENTRY, "Delete Entry");
        setValue(L.LABEL_ENTRY_NAME, "Entry Name");
        setValue(L.LABEL_ORDER, "Order");
        setValue(L.LABEL_ENABLED, "Enabled");
        setValue(L.LABEL_CONTENT, "Content");
        setValue(L.LABEL_NOTE, "Note");
        setValue(L.LABEL_TAGS, "Tags");
        setValue(L.LABEL_LOREBOOK, "Lorebook");
        setValue(L.LABEL_DELETE, "Delete");
        setValue(L.LABEL_BOOKS, "Books");
        setValue(L.LABEL_BOOK, "Book");
        setValue(L.LABEL_ADD_BOOK, "Add Book");
        setValue(L.LABEL_EDIT_BOOK, "Edit Book");
        setValue(L.LABEL_NEW_BOOK, "New Book");
        setValue(L.LABEL_NEGATIVE_TAGS, "Negative Tags (Exclusion)");
        setValue(L.LABEL_INFO_PART, "About");
        setValue(L.LABEL_PROMPT_PART, "Prompts");
        setValue(L.LABEL_STORY_PART, "Story");
        setValue(L.LABEL_DESCRIPTION, "Description");
        setValue(L.LABEL_TOTAL_WORD_COUNT, "Total Word Count");
        setValue(L.LABEL_TOTAL_TOKEN_COUNT, "Total Token Count");
        setValue(L.LABEL_BRANCH_WORD_COUNT, "Active Branch Word Count");
        setValue(L.LABEL_BRANCH_TOKEN_COUNT, "Active Branch Token Count");
        setValue(L.LABEL_STORY_CONTROLS, "Story Controls");
        setValue(L.LABEL_GENERATE_NEXT, "Generate Next");
        setValue(L.LABEL_BRANCH_STORY, "Branch Story");
        setValue(L.LABEL_NODE_METADATA, "Node Metadata");
        setValue(L.LABEL_VIEW_REASONING, "View Reasoning");
        setValue(L.LABEL_TOKENS, "Tokens");
        setValue(L.LABEL_WORDS, "Words");
        setValue(L.LABEL_SCENE_SETTING, "Scene Setting");
        setValue(L.LABEL_POV_CHARACTER, "POV Character");
        setValue(L.LABEL_PRESENT_CHARACTERS, "Present Characters");
        setValue(L.LABEL_INSTRUCTIONS, "Instructions");
        setValue(L.LABEL_NEW_TURN_INSTRUCTIONS, "New Turn Instructions");
        setValue(L.LABEL_GENERATE, "Generate");
        setValue(L.LABEL_TURN_DETAILS, "Turn Details");
        setValue(L.LABEL_IMPORT_FROM_SILLYTAVERN, "Import from SillyTavern");
        setValue(L.LABEL_CHANGE_STYLES, "Change Styles For Text");
        setValue(L.LABEL_REGENERATE, "Regenerate");
        setValue(L.LABEL_SWIPE, "Swipe");

        setValue(L.ERROR_INTERNAL_SERVER_ERROR, "Internal Server Error");
        setValue(L.ERROR_AI_NOT_SET, "Book is missing model.");
        setValue(L.ERROR_PROTOCOL_NOT_SET, "Book is missing protocol.");
        setValue(L.ERROR_CONTEXT_INSUFFICIENT, "Contextual limit not sufficient.");

        setValue(L.MSG_VALIDATION_FAILED_CANT_SAVE, "Cannot save form.");
        setValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT, "Cannot save form. Invalid fields: ");
        setValue(L.MSG_AUTHENTICATION_FAILED_INFO, "Invalid credentials.");
        setValue(L.MSG_INVALID_JSON_OBJECT, "JSON Invalid.");
        setValue(L.MSG_FETCH_MODELS_FAILED, "Failed to download model list.");
        setValue(L.MSG_SETTINGS_SAVED, "Settings saved successfully.");
        setValue(L.MSG_CONFIRM_DELETE, "Arwhae you sure you want to delete this item?");
        setValue(L.MSG_CYCLE_DETECTED, "Cannot select sub lorebook: cycle detected.");
        setValue(L.MSG_NO_ACTIVE_BRANCH, "No active story branch. Add a message to begin.");
        setValue(L.MSG_SELECT_MESSAGE_METADATA, "Select a message to view metadata.");
        setValue(L.MSG_INTERRUPTED, "Generation interrupted on the backend.");

        setValue(L.ENUM_AI_TYPE_OPEN_AI_COMPATIBLE, "OpenAI Compatible");
        setValue(L.ENUM_PROTOCOL_TYPE_OPEN_CHAT_COMPLETION, "Chat Completion");
        setValue(L.ENUM_REASONING_NONE, "None");
        setValue(L.ENUM_REASONING_LOW, "Low");
        setValue(L.ENUM_REASONING_MEDIUM, "Medium");
        setValue(L.ENUM_REASONING_HIGH, "High");

        setValue(L.DESC_TEMPLATE_BACKGROUND_LORE, "Background lore and world context.");
        setValue(L.DESC_TEMPLATE_MANUSCRIPT, "The ongoing manuscript content.");
        setValue(L.DESC_TEMPLATE_NARRATIVE_POV, "Point of view (POV) for the narrative.");
        setValue(L.DESC_TEMPLATE_NARRATIVE_TENSE, "Tense used for the narrative.");
        setValue(L.DESC_TEMPLATE_STYLE, "Style and tone guidelines.");
        setValue(L.DESC_TEMPLATE_POV_CHARACTER, "Point of view character for the scene.");
        setValue(L.DESC_TEMPLATE_SCENE_SETTING, "Current location and scene setting.");
        setValue(L.DESC_TEMPLATE_PRESENT_CHARACTERS, "Characters currently present in the scene.");
        setValue(L.DESC_TEMPLATE_INSTRUCTIONS, "Plot specifications and instructions for continuation.");
    }

    @Override
    public Locale getLocale() {
        return Locale.US;
    }

    @Override
    public DateFormat getDateFormat() {
        return new SimpleDateFormat("MM.dd.yyyy");
    }

    @Override
    public DateFormat getHourFormat() {
        return new SimpleDateFormat("HH:mm:ss");
    }

    @Override
    public DateFormat getDateHourFormat() {
        return new SimpleDateFormat("MM.dd.yyyy HH:mm:ss");
    }
}