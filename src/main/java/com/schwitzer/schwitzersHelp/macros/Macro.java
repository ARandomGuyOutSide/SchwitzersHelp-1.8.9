package com.schwitzer.schwitzersHelp.macros;

public interface Macro {
    void onEnable();

    void onDisable();

    String getName();

    MacroController.MacroState getState();

    void setState(MacroController.MacroState state);
}