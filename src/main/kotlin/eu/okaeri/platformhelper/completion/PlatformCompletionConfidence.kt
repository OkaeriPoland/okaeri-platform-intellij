package eu.okaeri.platformhelper.completion

import com.intellij.codeInsight.completion.CompletionConfidence
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNameValuePair
import com.intellij.psi.util.parentOfTypes
import com.intellij.util.ThreeState
import eu.okaeri.platformhelper.util.ALL_ANNOTATION_STRINGS

class PlatformCompletionConfidence : CompletionConfidence() {

    override fun shouldSkipAutopopup(contextElement: PsiElement, psiFile: PsiFile, offset: Int): ThreeState {

        val nameValuePair = contextElement.parentOfTypes(PsiNameValuePair::class) ?: return ThreeState.UNSURE
        val annotation = contextElement.parentOfTypes(PsiAnnotation::class) ?: return ThreeState.UNSURE
        val allowedNames = ALL_ANNOTATION_STRINGS[annotation.qualifiedName] ?: return ThreeState.UNSURE

        return when (nameValuePair.name) {
            null -> ThreeState.NO
            !in allowedNames -> ThreeState.UNSURE
            else -> ThreeState.NO
        }
    }
}