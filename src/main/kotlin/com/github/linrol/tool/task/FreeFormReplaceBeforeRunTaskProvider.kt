package com.github.linrol.tool.task

import com.github.linrol.tool.lang.ClearLangAction
import com.github.linrol.tool.model.GitCmd
import com.github.linrol.tool.utils.GitLabUtil
import com.intellij.execution.BeforeRunTaskProvider
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.util.io.exists
import org.apache.commons.lang3.exception.ExceptionUtils
import org.jetbrains.annotations.Nls
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Paths
import javax.swing.Icon

class FreeFormReplaceBeforeRunTaskProvider : BeforeRunTaskProvider<FreeFormReplaceBeforeRunTask>() {

    companion object {
        private val logger = logger<ClearLangAction>()
    }

    override fun getId(): Key<FreeFormReplaceBeforeRunTask> {
        return FreeFormReplaceBeforeRunTask.TASK_KEY
    }

    override fun getName(): @Nls(capitalization = Nls.Capitalization.Title) String {
        return "Freeform Properties File Replacement Task"
    }

    override fun getIcon(): Icon? {
        return null // 可以提供自定义图标
    }

    override fun createTask(runConfiguration: RunConfiguration): FreeFormReplaceBeforeRunTask {
        return FreeFormReplaceBeforeRunTask()
    }

    override fun canExecuteTask(configuration: RunConfiguration, task: FreeFormReplaceBeforeRunTask): Boolean {
        return true // 可以根据需求决定任务是否可执行
    }

    override fun executeTask(
        dataContext: DataContext,
        runConfiguration: RunConfiguration,
        executionEnvironment: ExecutionEnvironment,
        freeFormReplaceBeforeRunTask: FreeFormReplaceBeforeRunTask
    ): Boolean {
        // 从 DataContext 获取 Project 对象
        val project = CommonDataKeys.PROJECT.getData(dataContext)
        if (project == null) {
            logger.error("No project found in DataContext.")
            return true
        }
        val repos = GitLabUtil.getRepositories(project)
        repos.forEach {
            val basePath = it.root.path
            val workPath = getWorkPath(basePath) ?: return@forEach
            val fileProperties = Paths.get(basePath, workPath, "target/classes/freeform.properties")
            if (fileProperties.exists()) {
                propertiesReplace(project, fileProperties.toFile())
            }
        }
        return true
    }

    private fun getWorkPath(basePath: String): String? {
        return if (basePath.contains("easy-rent-contract")) {
            "easy-rent-contract-start";
        } else if (basePath.contains("home-trusteeship-contract")) {
            "trusteeship-contract-web"
        } else {
            null
        }
    }

    private fun propertiesReplace(project: Project, file: File) {
        // 使用 Properties 读取和替换属性值
        val properties = java.util.Properties()
        try {
            FileInputStream(file).use { input ->
                properties.load(input)
            }
            // 替换属性值
            properties["freeform.domain"] = "test3-freeform.lianjian.com"
            properties["ak"] = "apjwywyujtexfngyaa0v"
            properties["sk"] = "lpfoffblrv8e03i21hcnez16"
            // 保存更改到文件
            FileOutputStream(file).use { output ->
                properties.store(output, "Updated properties")
            }
            GitCmd.log(project, "Freeform File【${file.path}】 Properties replacement completed successfully.")
            logger.info("Freeform File【${file.path}】 Properties replacement completed successfully.")
            return
        } catch (e: Exception) {
            logger.error("Failed to read or write the properties file during before run task.", e)
            GitCmd.log(project, e.stackTraceToString())
            GitCmd.log(project, ExceptionUtils.getRootCauseMessage(e))
            return
        }
    }
}
