package moe.ono.hooks._core

import moe.ono.hooks._base.BaseFunctionHookItem
import moe.ono.hooks._base.BaseSwitchFunctionHookItem
import moe.ono.hooks._core.factory.HookItemFactory
import moe.ono.util.Logger


class HookItemLoader {
    /**
     * 加载并判断哪些需要加载
     */
    fun loadHookItem(process: Int) {
        val allHookItems = HookItemFactory.getAllItemList()
        allHookItems.forEach { hookItem ->
            val path = hookItem.path

            if (hookItem is BaseSwitchFunctionHookItem && hookItem.isEnabled && process == hookItem.targetProcess) {
                Logger.i("Initializing $path...")
                hookItem.startLoad()
            } else {
                if (hookItem is BaseFunctionHookItem && process == hookItem.targetProcess){
                    Logger.i("Initializing $path...")
                    hookItem.startLoad()
                }
            }


        }
    }
}