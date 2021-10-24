package eu.okaeri.platformhelper.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.roots.FileIndexFacade
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiNameValuePair
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.util.parentOfTypes
import com.intellij.util.ProcessingContext
import eu.okaeri.platformhelper.PlatformIcons
import eu.okaeri.platformhelper.completion.PlatformCompletion.Companion.OKAERI_COMMANDS_ANNOTATION_COMMAND
import eu.okaeri.platformhelper.completion.PlatformCompletion.Companion.OKAERI_COMMANDS_ANNOTATION_EXECUTOR
import eu.okaeri.platformhelper.completion.PlatformCompletion.Companion.OKAERI_CONFIGS_ANNOTATION_NAMES
import eu.okaeri.platformhelper.completion.PlatformCompletion.Companion.OKAERI_CONFIGS_ANNOTATION_NAMES_MODIFIER
import eu.okaeri.platformhelper.completion.PlatformCompletion.Companion.OKAERI_CONFIGS_ANNOTATION_NAMES_STRATEGY
import eu.okaeri.platformhelper.completion.PlatformCompletion.Companion.OKAERI_CONFIGS_NAMESTRATEGY_HYPHEN_CASE_PATTERN
import eu.okaeri.platformhelper.completion.PlatformCompletion.Companion.OKAERI_CONFIGS_NAMESTRATEGY_HYPHEN_CASE_REPLACEMENTS
import eu.okaeri.platformhelper.completion.PlatformCompletion.Companion.OKAERI_CONFIGS_NAMESTRATEGY_SNAKE_CASE_PATTERN
import eu.okaeri.platformhelper.completion.PlatformCompletion.Companion.OKAERI_CONFIGS_NAMESTRATEGY_SNAKE_CASE_REPLACEMENTS
import eu.okaeri.platformhelper.completion.PlatformCompletion.Companion.OKAERI_I18N_LOCALE_CONFIG
import eu.okaeri.platformhelper.completion.PlatformCompletion.Companion.OKAERI_INJECTOR_ANNOTATION_INJECT
import eu.okaeri.platformhelper.completion.PlatformCompletion.Companion.OKAERI_INJECTOR_ANNOTATION_INJECT_VALUE
import eu.okaeri.platformhelper.completion.PlatformCompletion.Companion.OKAERI_PLATFORM_ANNOTATION_BEAN
import eu.okaeri.platformhelper.completion.PlatformCompletion.Companion.OKAERI_PLATFORM_ANNOTATION_BEAN_VALUE
import java.util.*

class PlatformCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {

        val nameValuePair = parameters.position.parentOfTypes(PsiNameValuePair::class) ?: return
        val annotation = parameters.position.parentOfTypes(PsiAnnotation::class) ?: return

        val annotationName = annotation.qualifiedName
        val annotationParamName = nameValuePair.name
        val allowedNames = PlatformCompletion.ALL_ANNOTATION_STRINGS[annotationName] ?: return

        if (annotationParamName != null && annotationParamName !in allowedNames) {
            return
        }

        val project = parameters.originalFile.project
        val module = FileIndexFacade.getInstance(project).getModuleForFile(parameters.originalFile.virtualFile) ?: return
        val scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)

        // okaeri-commands
        if (OKAERI_COMMANDS_ANNOTATION_EXECUTOR == annotationName || OKAERI_COMMANDS_ANNOTATION_COMMAND == annotationName) {

            val localeConfig = JavaPsiFacade.getInstance(project).findClass(OKAERI_I18N_LOCALE_CONFIG, scope) ?: return
            val results = ClassInheritorsSearch.search(localeConfig, false)

            return result.addAllElements(results
                .flatMap {
                    val names = it.annotations.find { it.hasQualifiedName(OKAERI_CONFIGS_ANNOTATION_NAMES) }?.parameterList?.attributes
                    val strategy = names?.find { it.name == OKAERI_CONFIGS_ANNOTATION_NAMES_STRATEGY }?.value?.lastChild?.text
                    val modifier = names?.find { it.name == OKAERI_CONFIGS_ANNOTATION_NAMES_MODIFIER }?.value?.lastChild?.text
                    it.fields.map {
                        PrioritizedLookupElement.withPriority(
                            LookupElementBuilder.create("\${${toKeyName(it.name, strategy, modifier)}}")
                                .withTailText(
                                    when (val text = it.initializer?.text) {
                                        null -> null
                                        else -> "=$text"
                                    }, true
                                )
                                .withTypeText(it.containingClass?.name, true)
                                .withBoldness(true)
                                .withIcon(PlatformIcons.Okaeri),
                            1000.0
                        )
                    }
                }
            )
        }

        // okaeri-injector
        if (OKAERI_INJECTOR_ANNOTATION_INJECT == annotationName && (annotationParamName == null || OKAERI_INJECTOR_ANNOTATION_INJECT_VALUE == annotationParamName)) {

            val bean = JavaPsiFacade.getInstance(project).findClass(OKAERI_PLATFORM_ANNOTATION_BEAN, scope) ?: return
            val beans = AnnotatedElementsSearch.searchPsiMethods(bean, scope)

            return result.addAllElements(beans
                .map {
                    val beanAnnotation = it.annotations.find { it.hasQualifiedName(OKAERI_PLATFORM_ANNOTATION_BEAN) }?.parameterList?.attributes
                    val valueParam = beanAnnotation?.find { it.name == null || it.name == OKAERI_PLATFORM_ANNOTATION_BEAN_VALUE }
                    val name = when (val value = (valueParam?.value?.lastChild?.parent as PsiLiteralExpression).value as String?) {
                        null -> it.name
                        else -> value
                    }
                    PrioritizedLookupElement.withPriority(
                        LookupElementBuilder.create(name)
                            .withTailText(
                                when (val text = it.returnType?.presentableText) {
                                    null -> null
                                    else -> " $text"
                                }, true
                            )
                            .withTypeText(it.containingClass?.name, true)
                            .withBoldness(true)
                            .withIcon(PlatformIcons.Okaeri),
                        1000.0
                    )
                }
            )
        }
    }

    private fun toKeyName(fieldName: String, strategy: String?, modifier: String?): String {
        var result = fieldName
        when (strategy) {
            "SNAKE_CASE" -> result = OKAERI_CONFIGS_NAMESTRATEGY_SNAKE_CASE_PATTERN.matcher(fieldName).replaceAll(OKAERI_CONFIGS_NAMESTRATEGY_SNAKE_CASE_REPLACEMENTS)
            "HYPHEN_CASE" -> result = OKAERI_CONFIGS_NAMESTRATEGY_HYPHEN_CASE_PATTERN.matcher(fieldName).replaceAll(OKAERI_CONFIGS_NAMESTRATEGY_HYPHEN_CASE_REPLACEMENTS)
        }
        when (modifier) {
            "TO_UPPER_CASE" -> result = result.toUpperCase(Locale.ROOT)
            "TO_LOWER_CASE" -> result = result.toLowerCase(Locale.ROOT)
        }
        return result
    }
}
