/*
 * QAuxiliary - An Xposed module for QQ/TIM
 * Copyright (C) 2019-2024 QAuxiliary developers
 * https://github.com/cinit/QAuxiliary
 *
 * This software is an opensource software: you can redistribute it
 * and/or modify it under the terms of the General Public License
 * as published by the Free Software Foundation; either
 * version 3 of the License, or any later version as published
 * by QAuxiliary contributors.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the General Public License for more details.
 *
 * You should have received a copy of the General Public License
 * along with this software.
 * If not, see
 * <https://github.com/cinit/QAuxiliary/blob/master/LICENSE.md>.
 */
package moe.ono;

import androidx.annotation.Nullable;

import moe.ono.loader.hookapi.ILoaderService;
import moe.ono.startup.StartupInfo;

public class LoaderExtensionHelper {

    public static final String CMD_GET_XPOSED_BRIDGE_CLASS = "GetXposedBridgeClass";

    private LoaderExtensionHelper() {
    }

    @Nullable
    public static Class<?> getXposedBridgeClass() {
        ILoaderService loaderService = StartupInfo.getLoaderService();
        return (Class<?>) loaderService.queryExtension(CMD_GET_XPOSED_BRIDGE_CLASS);
    }



}
