package eu.okaeri.platformhelper.reference

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import eu.okaeri.platformhelper.PlatformIcons
import eu.okaeri.platformhelper.util.OKAERI_INJECTOR_ANNOTATION_INJECT
import eu.okaeri.platformhelper.util.findAnnotatedFields
import eu.okaeri.platformhelper.util.getInjectNameFromField

class PlatformInjectReference(element: PsiElement, range: TextRange, private val name: String) : PsiReferenceBase<PsiElement?>(element, range, true), PsiPolyVariantReference {

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        return findAnnotatedFields(OKAERI_INJECTOR_ANNOTATION_INJECT, myElement!!)
            ?.map { Pair(it, getInjectNameFromField(it)!!) }
            ?.filter { it.second == name }
            ?.map { PsiElementResolveResult(it.first) }
            ?.toTypedArray()
            ?: PsiElementResolveResult.EMPTY_ARRAY
    }

    override fun getVariants(): Array<Any> {
        return findAnnotatedFields(OKAERI_INJECTOR_ANNOTATION_INJECT, myElement!!)
            ?.map { Pair(it, getInjectNameFromField(it)!!) }
            ?.map {
                PrioritizedLookupElement.withPriority(
                    LookupElementBuilder.create(it.second)
                        .withTailText(" ${it.first.type.presentableText}", true)
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