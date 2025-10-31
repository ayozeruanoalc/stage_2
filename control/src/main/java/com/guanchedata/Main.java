package com.guanchedata;

import com.guanchedata.application.usecases.control.ControlRunner;

public class Main {
    public static void main(String[] args) throws Exception {
        new ControlRunner().run(args);
    }
}
