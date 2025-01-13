package moe.ono.hooks._core

import moe.ono.config.ConfigManager
import moe.ono.hooks._base.ApiHookItem
import moe.ono.hooks._base.BaseSwitchFunctionHookItem
import moe.ono.hooks._core.factory.HookItemFactory
import moe.ono.util.Logger


class HookItemLoader {
    /**
     * 加载并判断哪些需要加载
     */
    fun loadHookItem(process: Int) {
        loadSwitchFunctionConfig()

        val allHookItems = HookItemFactory.getAllItemList()
        allHookItems.forEach { hookItem ->
            val path = hookItem.path
            if (hookItem is BaseSwitchFunctionHookItem && hookItem.isEnabled && process == hookItem.targetProcess) {
                Logger.i("[BaseSwitchFunctionHookItem] Initializing $path...")
                hookItem.startLoad()
            } else {
                if (hookItem is ApiHookItem && process == hookItem.targetProcess){
                    Logger.i("[API] Initializing $path...")
                    hookItem.startLoad()
                }
            }


        }
    }

    /**
     * 加载SwitchFunction的配置
     */
    private fun loadSwitchFunctionConfig() {
        val allHookItems = HookItemFactory.getAllSwitchFunctionItemList()
        allHookItems.forEach { hookItem ->
            hookItem.isEnabled = ConfigManager.getDefaultConfig().getBooleanOrFalse("setting_switch_value_${hookItem.path}")
        }
    }
}