package com.nexus.atp.utils;

import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;

public class Mock {
    public static <T> CustomAction captureArgument(final T[] capture, int argumentIndex) {
        return new CustomAction("capture arguments") {
            @Override
            public Object invoke(Invocation invocation) {
                capture[0] = (T) invocation.getParameter(argumentIndex);
                return null;
            }
        };
    }
}
