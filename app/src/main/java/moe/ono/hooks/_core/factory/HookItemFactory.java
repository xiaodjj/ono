package moe.ono.hooks._core.factory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import moe.ono.hook.gen.HookItemEntryList;
import moe.ono.hooks._base.BaseHookItem;
import moe.ono.hooks._base.BaseSwitchFunctionHookItem;

public class HookItemFactory {
    private static final Map<Class<? extends BaseHookItem>, BaseHookItem> ITEM_MAP = new HashMap<>();

    static {
        List<BaseHookItem> items = HookItemEntryList.getAllHookItems();
        for (BaseHookItem item : items) {
            ITEM_MAP.put(item.getClass(), item);
        }
    }

    public static BaseSwitchFunctionHookItem findHookItemByPath(String path) {
        for (BaseHookItem item : ITEM_MAP.values()) {
            if (item.getPath().equals(path)) {
                return (BaseSwitchFunctionHookItem) item;
            }
        }
        return null;
    }

    public static List<BaseSwitchFunctionHookItem> getAllSwitchFunctionItemList() {
        ArrayList<BaseSwitchFunctionHookItem> result = new ArrayList<>();
        for (BaseHookItem item : ITEM_MAP.values()) {
            if (item instanceof BaseSwitchFunctionHookItem) {
                result.add((BaseSwitchFunctionHookItem) item);
            }
        }
        result.sort(Comparator.comparing(BaseHookItem::getSimpleName));
        return result;
    }

    public static List<BaseHookItem> getAllItemList() {
        return List.copyOf(ITEM_MAP.values());
    }

    public static <T extends BaseHookItem> T getItem(Class<T> clazz) {
        BaseHookItem item = ITEM_MAP.get(clazz);
        return clazz.cast(item);
    }
}
