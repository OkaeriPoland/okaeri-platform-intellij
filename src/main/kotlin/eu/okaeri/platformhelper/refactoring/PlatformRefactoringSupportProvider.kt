//package eu.okaeri.platformhelper.refactoring
//
//import com.intellij.lang.refactoring.RefactoringSupportProvider
//import com.intellij.psi.PsiElement
//import com.intellij.psi.PsiMethod
//import com.intellij.psi.util.parentOfTypes
//import eu.okaeri.platformhelper.util.getBeanNameFromMethod
//
//class PlatformRefactoringSupportProvider : RefactoringSupportProvider() {
//
//    override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
//        println(element.text)
//        println(element.context)
//        val method = element.parentOfTypes(PsiMethod::class) ?: return false
//        return getBeanNameFromMethod(method) != null
//    }
//}