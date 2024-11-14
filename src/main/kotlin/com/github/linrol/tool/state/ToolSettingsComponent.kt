package com.github.linrol.tool.state

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.layout.selectedValueIs
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class ToolSettingsComponent {
    val panel: JPanel
    private val buildAfterPush = JBCheckBox("Enable ops build after successful push? ")
    private val buildUrl = JTextField("http://ops.q7link.com:8000/qqdeploy/projectbuild/")
    private val buildUser = JTextField("kectool")
    private val shimoSid = JTextField("s%3A9e1d2ddd1970404b81e4fcf2b7182aed.gzbpB8BH75NkR7W87Tz1FKrR67A4L20vrkQgbcrGTHA")
    private val translaterApi = ComboBox(arrayOf("baidu", "youdao", "google", "77hub"))
    private val chatgptKey = JTextField("sk-vbFFb1gpjDWO321CRryqxvnGflJKMJ4RfW6mQjNJtwiwlcld")
    private val nThreads = JTextField("1")

    init {
        panel = FormBuilder.createFormBuilder()
                .addComponent(buildAfterPush, 1)
                .addLabeledComponent(JLabel("编译地址"), buildUrl, 1)
                .addLabeledComponent(JLabel("编译人"), buildUser, 1)
                .addLabeledComponent(JLabel("石墨sid"), shimoSid, 1)
                .addLabeledComponent(JLabel("翻译api"), translaterApi, 1)
                .addLabeledComponent(JLabel("翻译线程数"), nThreads, 1)
                .addLabeledComponent(JLabel("chatgpt key"), chatgptKey, 1)
                .addComponentFillVertically(JPanel(), 0)
                .panel
    }

    val preferredFocusedComponent: JComponent get() = buildAfterPush

    fun getBuildAfterPush(): Boolean {
        return buildAfterPush.isSelected
    }

    fun getBuildUrl(): String {
        return buildUrl.text
    }

    fun getBuildUser(): String {
        return buildUser.text
    }

    fun getShimoSid(): String {
        return shimoSid.text
    }

    fun getTranslaterApi(): String {
        return translaterApi.selectedItem as String
    }

    fun getChatgptKey(): String {
        return chatgptKey.text
    }

    fun getNThreads(): String {
        return nThreads.text
    }

    fun setBuildAfterPush(newStatus: Boolean) {
        buildAfterPush.isSelected = newStatus
    }

    fun setBuildUrl(newUrl: String) {
        buildUrl.text = newUrl
    }

    fun setBuildUser(newUser: String) {
        buildUser.text = newUser
    }

    fun setShimoSid(newShimoSid: String) {
        shimoSid.text = newShimoSid
    }

    fun setTranslaterApi(newApi: String) {
        translaterApi.selectedItem = newApi
    }

    fun setChatgptKey(newChatgptKey: String) {
        chatgptKey.text = newChatgptKey
    }

    fun setNThreads(newNThreads: String) {
        nThreads.text = newNThreads
    }

    fun isModified(): Boolean {
        val enable = getBuildAfterPush() != ToolSettingsState.instance.buildAfterPush
        val urlModified = getBuildUrl() != ToolSettingsState.instance.buildUrl
        val userModified = getBuildUser() != ToolSettingsState.instance.buildUser
        val shimoSidModified = getShimoSid() != ToolSettingsState.instance.shimoSid
        val translaterApiModified = getTranslaterApi() != ToolSettingsState.instance.translaterApi
        val chatgptKeyModified = getChatgptKey() != ToolSettingsState.instance.chatgptKey
        val nThreadsModified = getNThreads() != ToolSettingsState.instance.nThreads
        return enable || urlModified || userModified || shimoSidModified || translaterApiModified || chatgptKeyModified || nThreadsModified
    }
}
