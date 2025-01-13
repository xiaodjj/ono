package moe.ono.config

import moe.ono.hooks._base.BaseSwitchFunctionHookItem
import moe.ono.hooks._core.factory.HookItemFactory
import moe.ono.util.Logger

data class SettingItem(
    val key: String,
    val title: String,
    val path: String,
    val t: String,
    val iconResId: Int? = null,
    val summary: String? = null,
    val isSwitch: Boolean,
    val clazz: BaseSwitchFunctionHookItem
)

fun getDynamicSettings(): List<SettingItem> {

    return HookItemFactory.getAllSwitchFunctionItemList().map { hookItem ->
        val path = hookItem.path.split("/")
        SettingItem(
            key = path[path.size - 1],
            title = path[path.size - 1],
            summary = hookItem.desc,
            isSwitch = hookItem.isEnabled,
            t = path[0],
            path = hookItem.path,
            clazz =  hookItem
        )
    }
}
