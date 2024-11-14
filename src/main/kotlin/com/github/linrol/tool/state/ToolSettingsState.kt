package com.github.linrol.tool.state

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "com.github.linrol.tool.state.ToolSettingsStat", storages = [Storage("KectoolSettingsPlugin.xml")])
class ToolSettingsState : PersistentStateComponent<ToolSettingsState?> {
    // push后触发编译
    var buildAfterPush: Boolean = true

    var buildUrl: String = "http://ops.q7link.com:8000/qqdeploy/projectbuild/"

    var buildUser: String = "kectool"

    var shimoSid: String = "s%3A9e1d2ddd1970404b81e4fcf2b7182aed.gzbpB8BH75NkR7W87Tz1FKrR67A4L20vrkQgbcrGTHA"

    var translaterApi: String = "baidu"

    var chatgptKey: String = "sk-vbFFb1gpjDWO321CRryqxvnGflJKMJ4RfW6mQjNJtwiwlcld"

    var nThreads: String = "1"

    override fun getState(): ToolSettingsState {
        return this
    }

    override fun loadState(state: ToolSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val instance: ToolSettingsState = ApplicationManager.getApplication().getService(ToolSettingsState::class.java)
    }
}
