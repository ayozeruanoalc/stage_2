package com.guanchedata;

import com.guanchedata.infrastructure.ports.MainRunner;
import com.guanchedata.util.ControlRunner;

public class Main {
    public static void main(String[] args) throws Exception {
        MainRunner runner = new ControlRunner();
        runner.run(args);
    }
}
