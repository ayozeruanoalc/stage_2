package com.guanchedata;

import com.guanchedata.config.AppConfig;
import com.guanchedata.core.ArgsParser;
import com.guanchedata.core.BookProcessorRunner;

public class Main {
    public static void main(String[] args) throws Exception {
        AppConfig config = AppConfig.loadDefaults();
        ArgsParser parser = new ArgsParser(args);
        BookProcessorRunner runner = new BookProcessorRunner(config, parser);
        runner.run();
    }
}
