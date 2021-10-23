package eu.okaeri.platformhelper.completion

import java.util.regex.Pattern

class PlatformCompletion {
    companion object {
        // okaeri-commands
        val OKAERI_COMMANDS_ANNOTATION_COMMAND = "eu.okaeri.commands.annotation.Command"
        val OKAERI_COMMANDS_ANNOTATION_COMMAND_STRINGS = setOf("label", "aliases", "description")
        val OKAERI_COMMANDS_ANNOTATION_EXECUTOR = "eu.okaeri.commands.annotation.Executor"
        val OKAERI_COMMANDS_ANNOTATION_EXECUTOR_STRINGS = setOf("pattern", "description", "usage")

        // okaeri-injector
        val OKAERI_INJECTOR_ANNOTATION_INJECT_VALUE = "value"
        val OKAERI_INJECTOR_ANNOTATION_INJECT = "eu.okaeri.injector.annotation.Inject"
        val OKAERI_INJECTOR_ANNOTATION_INJECT_STRINGS = setOf(OKAERI_INJECTOR_ANNOTATION_INJECT_VALUE)

        // okaeri-platform
        val OKAERI_PLATFORM_ANNOTATION_BEAN_VALUE = "value"
        val OKAERI_PLATFORM_ANNOTATION_BEAN = "eu.okaeri.platform.core.annotation.Bean"
        val OKAERI_PLATFORM_ANNOTATION_BEAN_STRINGS = setOf(OKAERI_PLATFORM_ANNOTATION_BEAN_VALUE)

        // okaeri-i18n
        val OKAERI_I18N_LOCALE_CONFIG = "eu.okaeri.i18n.configs.LocaleConfig"

        // okaeri-configs
        val OKAERI_CONFIGS_ANNOTATION_NAMES = "eu.okaeri.configs.annotation.Names";
        val OKAERI_CONFIGS_ANNOTATION_NAMES_STRATEGY = "strategy";
        val OKAERI_CONFIGS_ANNOTATION_NAMES_MODIFIER = "modifier";
        val OKAERI_CONFIGS_NAMESTRATEGY_SNAKE_CASE_PATTERN = Pattern.compile("(\\G(?!^)|\\b(?:[A-Z]{2}|[a-zA-Z][a-z]*))(?=[a-zA-Z]{2,}|\\d)([A-Z](?:[A-Z]|[a-z]*)|\\d+)")
        val OKAERI_CONFIGS_NAMESTRATEGY_SNAKE_CASE_REPLACEMENTS = "\$1_\$2"
        val OKAERI_CONFIGS_NAMESTRATEGY_HYPHEN_CASE_PATTERN = Pattern.compile("(\\G(?!^)|\\b(?:[A-Z]{2}|[a-zA-Z][a-z]*))(?=[a-zA-Z]{2,}|\\d)([A-Z](?:[A-Z]|[a-z]*)|\\d+)")
        val OKAERI_CONFIGS_NAMESTRATEGY_HYPHEN_CASE_REPLACEMENTS = "\$1-\$2"

        // collective
        val ALL_ANNOTATION_STRINGS = mapOf(
            // okaeri-commands
            OKAERI_COMMANDS_ANNOTATION_COMMAND to OKAERI_COMMANDS_ANNOTATION_COMMAND_STRINGS,
            OKAERI_COMMANDS_ANNOTATION_EXECUTOR to OKAERI_COMMANDS_ANNOTATION_EXECUTOR_STRINGS,
            // okaeri-injector
            OKAERI_INJECTOR_ANNOTATION_INJECT to OKAERI_INJECTOR_ANNOTATION_INJECT_STRINGS,
            // okaeri-platform
            OKAERI_PLATFORM_ANNOTATION_BEAN to OKAERI_PLATFORM_ANNOTATION_BEAN_STRINGS
        )
    }
}
