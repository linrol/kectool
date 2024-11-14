package com.github.linrol.tool.branch.merge.local

import com.github.linrol.tool.base.AbstractDumbAction
import com.github.linrol.tool.utils.GitLabUtil
import com.intellij.openapi.actionSystem.AnActionEvent

class CommonMergeAction : AbstractDumbAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        CommonMergeDialog(project, e).showAndGet()
    }

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return
        val repo = GitLabUtil.getRepository(project, "build")
        e.presentation.isEnabledAndVisible = repo != null
    }
}
