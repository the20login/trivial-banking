package com.github.the20login.trivial.bankng;

import com.github.the20login.trivial.bankng.processing.Processing;
import com.github.the20login.trivial.bankng.processing.ProcessingImpl;
import com.github.the20login.trivial.bankng.web.AccountsRestController;
import com.github.the20login.trivial.bankng.web.TransferController;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class ProductionModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AccountsRestController.class).asEagerSingleton();
        bind(TransferController.class).asEagerSingleton();
    }

    @Provides
    Processing provideProcessing() {
        return new ProcessingImpl();
    }
}
