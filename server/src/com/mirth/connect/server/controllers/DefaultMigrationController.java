/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 *
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.util.MigrationException;
import com.mirth.connect.server.ExtensionLoader;
import com.mirth.connect.server.migration.Migrator;
import com.mirth.connect.server.migration.ServerMigrator;
import com.mirth.connect.server.util.SqlConfig;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;

public class DefaultMigrationController extends MigrationController {
    private static MigrationController instance = null;

    public static MigrationController create() {
        synchronized (DefaultMigrationController.class) {
            if (instance == null) {
                instance = ExtensionLoader.getInstance().getControllerInstance(MigrationController.class);

                if (instance == null) {
                    instance = new DefaultMigrationController();
                }
            }

            return instance;
        }
    }

    /*
     * Don't create the ExtensionController here as a class variable, because its dependencies have
     * not all been initialized at this point. Specifically appDataDir in the
     * DefaultConfigurationController
     */
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private ServerMigrator serverMigrator;
    private Collection<Migrator> pluginMigrators;
    private Logger logger = Logger.getLogger(this.getClass());

    public DefaultMigrationController() {
        serverMigrator = new ServerMigrator();
    }

    @Override
    public void migrateConfiguration(PropertiesConfiguration configuration) throws MigrationException {
        serverMigrator.migrateConfiguration(configuration);
    }

    private void initPluginMigrators() {
        if (pluginMigrators == null) {
            pluginMigrators = new ArrayList<Migrator>();

            for (PluginMetaData pluginMetaData : ControllerFactory.getFactory().createExtensionController().getPluginMetaData().values()) {
                String migratorClassName = pluginMetaData.getMigratorClass();

                if (migratorClassName != null) {
                    try {
                        Migrator migrator = (Migrator) Class.forName(migratorClassName).newInstance();
                        migrator.setDefaultScriptPath("extensions/" + pluginMetaData.getPath());
                        pluginMigrators.add(migrator);
                    } catch (Exception e) {
                        logger.error("Failed to run migration for plugin: " + pluginMetaData.getName());
                    }
                }
            }
        }
    }

    @Override
    public void migrate() throws MigrationException {
        SqlConfig.getSqlSessionManager().startManagedSession();
        Connection connection = SqlConfig.getSqlSessionManager().getConnection();

        try {
            // ServerMigrator will set its own starting version
            serverMigrator.setConnection(connection);
            serverMigrator.setDatabaseType(configurationController.getConfigDatabaseType());
            serverMigrator.migrate();
        } finally {
            if (SqlConfig.getSqlSessionManager().isManagedSessionStarted()) {
                SqlConfig.getSqlSessionManager().close();
            }
        }
    }

    @Override
    public void migrateExtensions() {
        initPluginMigrators();

        SqlConfig.getSqlSessionManager().startManagedSession();
        Connection connection = SqlConfig.getSqlSessionManager().getConnection();

        try {
            for (Migrator migrator : pluginMigrators) {
                try {
                    migrator.setStartingVersion(serverMigrator.getStartingVersion());
                    migrator.setConnection(connection);
                    migrator.setDatabaseType(configurationController.getDatabaseType());
                    migrator.migrate();
                } catch (MigrationException e) {
                    logger.error("Failed to migrate extension", e);
                }
            }
        } finally {
            if (SqlConfig.getSqlSessionManager().isManagedSessionStarted()) {
                SqlConfig.getSqlSessionManager().close();
            }
        }
    }

    @Override
    public void migrateSerializedData() {
        SqlConfig.getSqlSessionManager().startManagedSession();
        Connection connection = SqlConfig.getSqlSessionManager().getConnection();

        try {
            serverMigrator.setConnection(connection);
            serverMigrator.setDatabaseType(configurationController.getDatabaseType());
            serverMigrator.migrateSerializedData();

            initPluginMigrators();

            for (Migrator migrator : pluginMigrators) {
                try {
                    migrator.setConnection(connection);
                    migrator.setDatabaseType(configurationController.getDatabaseType());
                    migrator.migrateSerializedData();
                } catch (MigrationException e) {
                    logger.error("Failed to migrate serialized data for plugin", e);
                }
            }
        } finally {
            if (SqlConfig.getSqlSessionManager().isManagedSessionStarted()) {
                SqlConfig.getSqlSessionManager().close();
            }
        }
    }
}
