package eu.okaeri.platformhelper.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiJavaPatterns

class PlatformCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(PsiJavaPatterns.literalExpression()),
            PlatformCompletionProvider()
        )
    }
}
