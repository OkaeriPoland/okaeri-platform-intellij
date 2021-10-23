package com.github.pinklolicorn.okaeriplatformintellij.services

import com.intellij.openapi.project.Project
import com.github.pinklolicorn.okaeriplatformintellij.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
