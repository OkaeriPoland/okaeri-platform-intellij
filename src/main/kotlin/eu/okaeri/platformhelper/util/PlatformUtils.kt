package eu.okaeri.platformhelper.util

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.FileIndexFacade
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.util.Query
import java.util.*

// okaeri-platform (@Bean)
fun getBeanNameFromMethod(method: PsiMethod): String? {

    val annotation = findAnnotation(method, OKAERI_PLATFORM_ANNOTATION_BEAN) ?: return null
    val valueParam = annotation.parameterList.attributes.find { it.name == null || it.name == OKAERI_PLATFORM_ANNOTATION_BEAN_VALUE }

    return when (val value = (valueParam?.value?.lastChild?.parent as PsiLiteralExpression?)?.value as String?) {
        null -> method.name
        else -> value
    }
}

// okaeri-injector (@Inject)
fun getInjectNameFromField(field: PsiField): String? {

    val annotation = findAnnotation(field, OKAERI_INJECTOR_ANNOTATION_INJECT) ?: return null
    val valueParam = annotation.parameterList.attributes.find { it.name == null || it.name == OKAERI_INJECTOR_ANNOTATION_INJECT_VALUE }

    return when (val value = (valueParam?.value?.lastChild?.parent as PsiLiteralExpression?)?.value as String?) {
        null -> field.name
        else -> value
    }
}

// okaeri-configs
fun toConfigKeyName(fieldName: String, strategy: String?, modifier: String?): String {
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

// general
fun findAnnotatedMethods(annotationClazz: String, project: Project, module: Module): Query<PsiMethod>? {
    val scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)
    val annotation = JavaPsiFacade.getInstance(project).findClass(annotationClazz, scope) ?: return null
    return AnnotatedElementsSearch.searchPsiMethods(annotation, scope)
}

fun findAnnotatedMethods(annotationClazz: String, element: PsiElement): Query<PsiMethod>? {
    return findAnnotatedMethods(annotationClazz, element.project, findModule(element)!!)
}

fun findAnnotatedFields(annotationClazz: String, project: Project, module: Module): Query<PsiField>? {
    val scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)
    val annotation = JavaPsiFacade.getInstance(project).findClass(annotationClazz, scope) ?: return null
    return AnnotatedElementsSearch.searchPsiFields(annotation, scope)
}

fun findAnnotatedFields(annotationClazz: String, element: PsiElement): Query<PsiField>? {
    return findAnnotatedFields(annotationClazz, element.project, findModule(element)!!)
}

fun findModule(element: PsiElement): Module? {
    return FileIndexFacade.getInstance(element.project).getModuleForFile(element.containingFile.originalFile.virtualFile)
}

fun findInheritors(inheritedClazz: String, project: Project, module: Module): Query<PsiClass>? {
    val scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)
    val clazz = JavaPsiFacade.getInstance(project).findClass(inheritedClazz, scope) ?: return null
    return ClassInheritorsSearch.search(clazz, false)
}

fun findInheritors(inheritedClazz: String, element: PsiElement): Query<PsiClass>? {
    return findInheritors(inheritedClazz, element.project, findModule(element)!!)
}

fun findAnnotation(method: PsiMethod, annotationClazz: String): PsiAnnotation? {
    return method.annotations.find { it.hasQualifiedName(annotationClazz) }
}

fun findAnnotation(clazz: PsiClass, annotationClazz: String): PsiAnnotation? {
    return clazz.annotations.find { it.hasQualifiedName(annotationClazz) }
}

fun findAnnotation(method: PsiField, annotationClazz: String): PsiAnnotation? {
    return method.annotations.find { it.hasQualifiedName(annotationClazz) }
}

fun findAnnotationValue(annotation: PsiAnnotation?, name: String): String? {
    return annotation?.parameterList?.attributes?.find { (it.name == name || (it.name == null && name == "value")) }?.value?.lastChild?.text
}
