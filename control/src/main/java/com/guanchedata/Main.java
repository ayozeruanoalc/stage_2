package com.guanchedata;

import com.guanchedata.app.ControlApp;
import com.guanchedata.core.ArgsParser;

public class Main {
    public static void main(String[] args) throws Exception {
        ArgsParser parser = new ArgsParser(args);
        new ControlApp().run(parser);
    }
}
