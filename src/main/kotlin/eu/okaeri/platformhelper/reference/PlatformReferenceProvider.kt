package eu.okaeri.platformhelper.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.parentOfTypes
import com.intellij.util.ProcessingContext
import eu.okaeri.platformhelper.util.*


class PlatformReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {

        val literalExpression = element as PsiLiteralExpression
        val value = if (literalExpression.value is String) literalExpression.value as String else null ?: return PsiReference.EMPTY_ARRAY

        val nameValuePair = element.parentOfTypes(PsiNameValuePair::class) ?: return PsiReference.EMPTY_ARRAY
        val annotation = element.parentOfTypes(PsiAnnotation::class) ?: return PsiReference.EMPTY_ARRAY

        val annotationName = annotation.qualifiedName
        val annotationParamName = nameValuePair.name
        val allowedNames = ALL_ANNOTATION_STRINGS[annotation.qualifiedName] ?: return PsiReference.EMPTY_ARRAY

        if (annotationParamName != null && annotationParamName !in allowedNames) {
            return PsiReference.EMPTY_ARRAY
        }

        // okaeri-commands (@Command, @Executor) -> LocaleConfig field
        if ((OKAERI_COMMANDS_ANNOTATION_EXECUTOR == annotationName || OKAERI_COMMANDS_ANNOTATION_COMMAND == annotationName) && (value.startsWith("\${") && value.endsWith("}"))) {
            return arrayOf(PlatformLocaleReference(nameValuePair.value!!, TextRange(3, nameValuePair.value!!.textLength - 2), value.substring(2, value.length - 1)))
        }

        // okaeri-injector (@Inject) -> @Bean
        if (OKAERI_INJECTOR_ANNOTATION_INJECT == annotationName && (annotationParamName == null || OKAERI_INJECTOR_ANNOTATION_INJECT_VALUE == annotationParamName)) {
            return arrayOf(PlatformBeanReference(element, TextRange(1, element.textLength - 1), element.value as String))
        }

        // okaeri-platform (@Bean) -> @Inject
        if (OKAERI_PLATFORM_ANNOTATION_BEAN == annotationName && (annotationParamName == null || OKAERI_PLATFORM_ANNOTATION_BEAN_VALUE == annotationParamName)) {
            return arrayOf(PlatformInjectReference(element, TextRange(1, element.textLength - 1), element.value as String))
        }

        return PsiReference.EMPTY_ARRAY
    }
}