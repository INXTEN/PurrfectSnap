package me.eternal.purrfectsnap.core.ui.menu.impl

import android.view.View
import android.widget.FrameLayout
import me.eternal.purrfectsnap.common.ui.OverlayType
import me.eternal.purrfectsnap.core.ui.menu.AbstractMenu
import me.eternal.purrfectsnap.core.util.hook.HookStage
import me.eternal.purrfectsnap.core.util.hook.hook
import me.eternal.purrfectsnap.core.util.ktx.getId

class SettingsMenu : AbstractMenu() {
    private val hovaHeaderSearchIconId by lazy {
        context.resources.getId("hova_header_search_icon")
    }

    override fun init() {
        if (context.config.userInterface.settingsMenu.get() != "default") return
        context.androidContext.classLoader.loadClass("com.snap.ui.view.SnapFontTextView").hook("setText", HookStage.BEFORE) { param ->
            val view = param.thisObject<View>()
            if ((view.parent as? FrameLayout)?.findViewById<View>(hovaHeaderSearchIconId) != null) {
                view.post {
                    view.setOnClickListener {
                        context.bridgeClient.openOverlay(OverlayType.SETTINGS)
                    }
                    view.setOnLongClickListener {
                        val holdKillConfig = context.config.userInterface.chatButtonHoldKill
                        if (!holdKillConfig.enabled.get()) {
                            return@setOnLongClickListener false
                        }

                        val targetApps = holdKillConfig.targetApps.get()
                        val shouldKillModule = targetApps.contains("kill_purrfectsnap")
                        val shouldKillSnapchat = targetApps.contains("kill_snapchat")

                        if (shouldKillModule) {
                            runCatching {
                                context.bridgeClient.terminateModuleProcess()
                            }.onFailure {
                                context.log.error("Failed to terminate PurrfectSnap module process", it, "SettingsMenu")
                            }
                        }
                        if (shouldKillSnapchat) {
                            context.forceCloseApp()
                        }

                        shouldKillModule || shouldKillSnapchat
                    }
                }
            }
        }
    }
}
