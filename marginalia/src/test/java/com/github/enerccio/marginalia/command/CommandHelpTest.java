package com.github.enerccio.marginalia.command;

import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.command.CommandDefinition;
import com.github.enerccio.marginalia.domain.service.command.CommandHelp;
import com.github.enerccio.marginalia.domain.service.impl.CommandServiceImpl;
import com.github.enerccio.marginalia.domain.traits.Command;
import com.github.enerccio.marginalia.domain.traits.CommandParameter;
import com.github.enerccio.marginalia.domain.traits.CommandParameterSignature;
import com.github.enerccio.marginalia.loc.LocalizationKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link CommandHelp}: which commands and forms to show for the text the user typed.
 */
class CommandHelpTest {

    public static class Suite {
        @Command(commandName = "alpha", humanNameText = "First letter", descriptionText = "Does the first thing",
                signature = @CommandParameterSignature)
        public void none(CommandContext context) {
        }

        @Command(commandName = "alpha", humanNameText = "First letter",
                signature = @CommandParameterSignature({@CommandParameter(name = "x", descriptionText = "the x"),
                        @CommandParameter(name = "y")}))
        public void two(CommandContext context) {
        }

        @Command(commandName = "beta", humanNameText = "Second", descriptionText = "mentions alphabet")
        public void any(CommandContext context) {
        }

        @Command(commandName = "zeta", humanNameText = "Last")
        public void last(CommandContext context) {
        }
    }

    private final CommandServiceImpl service = new CommandServiceImpl();
    private List<CommandDefinition> all;

    @BeforeEach
    void register() {
        service.registerCommands(new Suite());
        all = service.getCommands();
    }

    @AfterEach
    void tearDown() {
        service.destroy();
    }

    @Test
    void namesAreFilteredByThePrefixFirstThenByWhatTheyContain() {
        assertThat(CommandHelp.matchingNames(all, "/", LocalizationKey::name)).containsExactly("alpha", "beta", "zeta");
        assertThat(CommandHelp.matchingNames(all, "/al", LocalizationKey::name)).containsExactly("alpha", "beta");
        assertThat(CommandHelp.matchingNames(all, "/ET", LocalizationKey::name)).containsExactly("alpha", "beta", "zeta"); // "letter", "beta", "zeta"
        assertThat(CommandHelp.matchingNames(all, "/zE", LocalizationKey::name)).containsExactly("zeta");
        assertThat(CommandHelp.matchingNames(all, "/second", LocalizationKey::name)).containsExactly("beta");
        assertThat(CommandHelp.matchingNames(all, "/nothing", LocalizationKey::name)).isEmpty();
    }

    @Test
    void theArgumentsDoNotChangeWhichNamesMatch() {
        assertThat(CommandHelp.matchingNames(all, "/zeta 1 2", LocalizationKey::name)).containsExactly("zeta");
        assertThat(CommandHelp.matchingNames(all, "/alpha 1 2", LocalizationKey::name)).first().isEqualTo("alpha");
    }

    @Test
    void whileTheNameIsTypedTheBestMatchIsDescribed() {
        CommandHelp help = CommandHelp.of(all, "/al", LocalizationKey::name);
        assertThat(help.typingName()).isTrue();
        assertThat(help.commandName()).isEqualTo("alpha");
        assertThat(help.overloads()).hasSize(2);
    }

    @Test
    void anExactNameWinsOverAPrefix() {
        assertThat(CommandHelp.of(all, "/beta", LocalizationKey::name).commandName()).isEqualTo("beta");
    }

    @Test
    void afterTheNameTheFormsThatTakeTheNextArgumentAreKept() {
        CommandHelp help = CommandHelp.of(all, "/alpha ", LocalizationKey::name);
        assertThat(help.typingName()).isFalse();
        assertThat(help.argumentIndex()).isEqualTo(0);
        assertThat(help.overloads()).hasSize(1);
        assertThat(help.overloads().getFirst().parameters()).hasSize(2);

        assertThat(CommandHelp.of(all, "/alpha 1 ", LocalizationKey::name).argumentIndex()).isEqualTo(1);
        assertThat(CommandHelp.of(all, "/alpha 1 ", LocalizationKey::name).overloads()).hasSize(1);
    }

    @Test
    void moreArgumentsThanAnyFormTakesIsReported() {
        CommandHelp help = CommandHelp.of(all, "/alpha 1 2 3", LocalizationKey::name);
        assertThat(help.tooManyArguments()).isTrue();
        assertThat(help.overloads()).hasSize(2);
    }

    @Test
    void aCommandThatTakesAnythingTakesTheNextArgument() {
        CommandHelp help = CommandHelp.of(all, "/beta 1 2 3 4 ", LocalizationKey::name);
        assertThat(help.tooManyArguments()).isFalse();
        assertThat(help.overloads().getFirst().anyArguments()).isTrue();
    }

    @Test
    void unknownCommandAndPlainTextHaveNothingToShow() {
        assertThat(CommandHelp.of(all, "/unknown x", LocalizationKey::name).commandName()).isNull();
        assertThat(CommandHelp.of(all, "/nothing", LocalizationKey::name).commandName()).isNull();
        assertThat(CommandHelp.of(all, "alpha", LocalizationKey::name).commandName()).isNull();
    }
}
