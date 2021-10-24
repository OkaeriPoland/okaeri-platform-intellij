package eu.okaeri.platformhelper.reference

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import eu.okaeri.platformhelper.PlatformIcons
import eu.okaeri.platformhelper.util.OKAERI_PLATFORM_ANNOTATION_BEAN
import eu.okaeri.platformhelper.util.findAnnotatedMethods
import eu.okaeri.platformhelper.util.getBeanNameFromMethod

class PlatformBeanReference(element: PsiElement, range: TextRange, private val name: String) : PsiReferenceBase<PsiElement?>(element, range, true), PsiPolyVariantReference {

    override fun resolve(): PsiElement? {
        return multiResolve(false).map { it.element }.firstOrNull()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        return findAnnotatedMethods(OKAERI_PLATFORM_ANNOTATION_BEAN, myElement!!)
            ?.map { Pair(it, getBeanNameFromMethod(it)!!) }
            ?.filter { it.second == name }
            ?.map { PsiElementResolveResult(it.first) }
            ?.toTypedArray()
            ?: PsiElementResolveResult.EMPTY_ARRAY
    }

    override fun getVariants(): Array<Any> {
        return findAnnotatedMethods(OKAERI_PLATFORM_ANNOTATION_BEAN, myElement!!)
            ?.map { Pair(it, getBeanNameFromMethod(it)!!) }
            ?.map {
                PrioritizedLookupElement.withPriority(
                    LookupElementBuilder.create(it.second)
                        .withTailText(
                            when (val text = it.first.returnType?.presentableText) {
                                null -> ""
                                else -> " $text"
                            }, true
                        )
                        .withTypeText(it.first.containingClass?.name, true)
                        .withBoldness(true)
                        .withIcon(PlatformIcons.Okaeri),
                    1000.0
                )
            }
            ?.toTypedArray()
            ?: emptyArray()
    }
}