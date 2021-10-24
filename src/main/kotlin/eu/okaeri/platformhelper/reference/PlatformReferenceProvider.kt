package eu.okaeri.platformhelper.reference

import com.intellij.openapi.roots.FileIndexFacade
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.util.parentOfTypes
import com.intellij.util.ProcessingContext
import eu.okaeri.platformhelper.completion.PlatformCompletion


class PlatformReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {

        val literalExpression = element as PsiLiteralExpression
        val value = if (literalExpression.value is String) literalExpression.value as String else null ?: return PsiReference.EMPTY_ARRAY

        val nameValuePair = element.parentOfTypes(PsiNameValuePair::class) ?: return PsiReference.EMPTY_ARRAY
        val annotation = element.parentOfTypes(PsiAnnotation::class) ?: return PsiReference.EMPTY_ARRAY

        val annotationName = annotation.qualifiedName
        val annotationParamName = nameValuePair.name
        val allowedNames = PlatformCompletion.ALL_ANNOTATION_STRINGS[annotation.qualifiedName] ?: return PsiReference.EMPTY_ARRAY

        if (annotationParamName != null && annotationParamName !in allowedNames) {
            return PsiReference.EMPTY_ARRAY
        }

        // okaeri-injector
        if (PlatformCompletion.OKAERI_INJECTOR_ANNOTATION_INJECT == annotationName && (annotationParamName == null || PlatformCompletion.OKAERI_INJECTOR_ANNOTATION_INJECT_VALUE == annotationParamName)) {
            // okaeri-injector
            return arrayOf(PsiReferenceBase.Immediate(element, this.findBean(element)))
        }

        return PsiReference.EMPTY_ARRAY
    }

    private fun findBean(inject: PsiElement): PsiElement? {

        val key = (inject as PsiLiteralExpression).value as String
        val project = inject.project
        val module = FileIndexFacade.getInstance(project).getModuleForFile(inject.containingFile.virtualFile) ?: return null
        val scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)

        val bean = JavaPsiFacade.getInstance(project).findClass(PlatformCompletion.OKAERI_PLATFORM_ANNOTATION_BEAN, scope) ?: return null
        val beans = AnnotatedElementsSearch.searchPsiMethods(bean, scope)

        return beans
            .map {
                val beanAnnotation = it.annotations.find { it.hasQualifiedName(PlatformCompletion.OKAERI_PLATFORM_ANNOTATION_BEAN) }!!
                val valueParam = beanAnnotation.parameterList.attributes.find { it.name == null || it.name == PlatformCompletion.OKAERI_PLATFORM_ANNOTATION_BEAN_VALUE }
                val name = when (val value = (valueParam?.value?.lastChild?.parent as PsiLiteralExpression).value as String?) {
                    null -> it.name
                    else -> value
                }
                Pair(beanAnnotation, name)
            }
            .find { it.second == key }
            ?.first
    }
}