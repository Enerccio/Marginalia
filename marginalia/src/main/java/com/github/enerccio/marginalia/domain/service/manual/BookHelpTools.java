package com.github.enerccio.marginalia.domain.service.manual;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.traits.LLMFunction;
import com.github.enerccio.marginalia.domain.traits.LLMParam;

/**
 * What the help assistant's model may read of the books of the user: the folders, the titles and descriptions, a search
 * and the settings a book resolves to. Given to the model only when the user turned it on in the panel of the assistant
 * (what it reads is sent to the AI that answers). Read only; the text of a story is never returned.
 */
public interface BookHelpTools {

    @LLMFunction(name = "list_folder", description = "Shows a folder of the user's books: its subfolders (with how many books "
            + "are beneath each) and the books in it with their titles and descriptions. Without a path the top level. Use "
            + "it to find your way through the user's folders, going down one folder at a time.")
    void listFolder(FunctionContinuation<String> done,
                    @LLMParam(description = "Names of the folders from the top, separated by /, for example Sci-fi/Expanse; "
                            + "empty for the top level", required = false) String path);

    @LLMFunction(name = "search_books", description = "Finds the user's books by words: all of the words have to be in the title "
            + "or the description of a book, or in the text of its story. Says where each was found (title, description, story "
            + "text) and in which folder the book is; the text itself is not shown. Use it for \"where is my book about ...\".")
    void searchBooks(FunctionContinuation<String> done,
                     @LLMParam(description = "The words to look for") String query,
                     @LLMParam(description = "Look only in this folder and the ones below it (names separated by /); "
                             + "empty for all the books", required = false) String folder);

    @LLMFunction(name = "get_book_settings", description = "Tells which language, point of view, tense, style and prompts a "
            + "book writes with and where each one comes from: set on the book itself, inherited from a folder (which) or from "
            + "the user's settings, or the built-in default. Use it when the user asks why a book writes the way it does.")
    void getBookSettings(FunctionContinuation<String> done,
                         @LLMParam(description = "The id of the book as list_folder or search_books printed it, or its title, "
                                 + "or Folder/Sub/Title") String book);
}
