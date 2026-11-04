/**
 * Writes the requests of the designer ({@code DesignerPrompts}) for the real-model trial: <dir>/<name>.system.txt and <name>.user.txt for each action.
 * <p>
 * Usage (from the plugin folder, after {@code mvn package}; the classpath is the plugin's classes and its dependencies):
 * <pre>
 *   mvn -o -q dependency:build-classpath -Dmdep.outputFile=/tmp/cp.txt
 *   javac -cp "target/classes:$(cat /tmp/cp.txt)" -d /tmp/designgen trial/design/DesignGen.java
 *   java -cp "/tmp/designgen:target/classes:$(cat /tmp/cp.txt)" DesignGen LIBRARY.json trial/design vrakht.md
 * </pre>
 * LIBRARY.json is the library of languages as {@code ConlangStore} writes it (a JSON array of languages: export one from the editor and wrap it
 * in brackets); its first language is the test language, its rules are replaced by the text of the last argument (the whole of {@code vrakht.md}
 * as one section, as the translation trial did). Then {@code run.sh} sends every request once to the local model ({@code tools/LocalLlm.java}).
 */
import com.github.enerccio.marginalia.extensions.conlangtranslator.designer.*;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.*;
import com.github.enerccio.marginalia.extensions.conlangtranslator.store.ConlangStore;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.TranslationPrompt.Request;
import com.google.gson.*;
import java.nio.file.*;
import java.util.List;

public class DesignGen {
    static void write(String dir, String name, Request r) throws Exception {
        Files.writeString(Path.of(dir, name + ".system.txt"), r.system());
        Files.writeString(Path.of(dir, name + ".user.txt"), r.user());
    }
    public static void main(String[] a) throws Exception {
        JsonObject attrs = new JsonObject();
        attrs.add(ConlangStore.LIBRARY_KEY, JsonParser.parseString(Files.readString(Path.of(a[0]))));
        Language vrakht = ConlangStore.readLibrary(attrs).get(0);
        String dir = a[1];
        vrakht.getRules().clear();
        vrakht.getRules().add(new RuleSection("Vrakht (grammar, lexicon and instruction block)", Files.readString(Path.of(a[2]))));
        String lore = "Draconia Malfoy: the cold daughter of a legion commander.\nThe Black March: the frontier road where the legions camp in winter.\nThe Ember Gate: the fortress that closes the pass.";
        write(dir, "words", DesignerPrompts.words(vrakht, "Things", 8, "the kitchen and the supplies of a siege camp, hard short words", lore));
        write(dir, "section", DesignerPrompts.section(vrakht, "a numbering system: base 12, digits 0-11, the powers have names, how the numbers are said before a noun", lore));
        write(dir, "rewrite", DesignerPrompts.rewrite(vrakht, vrakht.getRules().get(0), "add how a question about a person is formed, without changing the mood particles"));
        write(dir, "review", DesignerPrompts.review(vrakht));
        write(dir, "ask", DesignerPrompts.ask(vrakht, "A character wants to say 'my brother died at the gate'. Which words are missing for that, and how would the sentence go?", List.of(), lore));
        Language fresh = new Language("Skarn");
        String brief = "The Skarn are a mountain people who herd goats above the snow line. Harsh and short-worded, like Old Norse mixed with Georgian; no sounds an English reader cannot say; their words sound like stone and wind.";
        write(dir, "stage-sounds", DesignerPrompts.stage(fresh, DesignerStage.SOUNDS, brief, "", "", ""));
        fresh.getRules().add(new RuleSection("Sounds", "Consonants: k g t d p b s z sh r l m n kh. Vowels: a e i o u. Syllables CV(C), stress on the first syllable. No vowel clusters. Written as pronounced; kh and sh are digraphs.", Audience.MODEL));
        write(dir, "stage-grammar", DesignerPrompts.stage(fresh, DesignerStage.GRAMMAR, brief, "", "", ""));
        write(dir, "samples", DesignerPrompts.samples(vrakht, List.of("The legion marches at dawn.", "Hold the line, and do not look back.", "My brother died at the gate.")));
    }
}
