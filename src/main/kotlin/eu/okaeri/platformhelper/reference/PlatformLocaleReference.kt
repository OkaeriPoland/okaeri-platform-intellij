package eu.okaeri.platformhelper.reference

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import eu.okaeri.platformhelper.PlatformIcons
import eu.okaeri.platformhelper.util.*

class PlatformLocaleReference(element: PsiElement, range: TextRange, private val key: String) : PsiReferenceBase<PsiElement?>(element, range, true), PsiPolyVariantReference {

    override fun resolve(): PsiElement? {
        return multiResolve(false).map { it.element }.firstOrNull()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        return findInheritors(OKAERI_I18N_LOCALE_CONFIG, myElement!!)
            ?.flatMap {
                val names = findAnnotation(it, OKAERI_CONFIGS_ANNOTATION_NAMES)
                val strategy = findAnnotationValue(names, OKAERI_CONFIGS_ANNOTATION_NAMES_STRATEGY)
                val modifier = findAnnotationValue(names, OKAERI_CONFIGS_ANNOTATION_NAMES_MODIFIER)
                it.fields.map { Pair(it, toConfigKeyName(it.name, strategy, modifier)) }
            }
            ?.filter { it.second == key }
            ?.map { PsiElementResolveResult(it.first) }
            ?.toTypedArray()
            ?: PsiElementResolveResult.EMPTY_ARRAY
    }

    override fun getVariants(): Array<Any> {
        return findInheritors(OKAERI_I18N_LOCALE_CONFIG, myElement!!)
            ?.flatMap {
                val names = findAnnotation(it, OKAERI_CONFIGS_ANNOTATION_NAMES)
                val strategy = findAnnotationValue(names, OKAERI_CONFIGS_ANNOTATION_NAMES_STRATEGY)
                val modifier = findAnnotationValue(names, OKAERI_CONFIGS_ANNOTATION_NAMES_MODIFIER)
                it.fields.map {
                    PrioritizedLookupElement.withPriority(
                        LookupElementBuilder.create(toConfigKeyName(it.name, strategy, modifier))
                            .withTailText(
                                when (val text = it.initializer?.text) {
                                    null -> ""
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
            ?.toTypedArray()
            ?: emptyArray()
    }
}