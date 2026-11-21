package com.library.filter;

import com.library.dao.DatabaseConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/** On start-up: create the database, tables and demo data if missing. A failure here is only logged, never fatal. */
@WebListener
public class DatabaseInitListener implements ServletContextListener {
    private static final Logger LOG = Logger.getLogger(DatabaseInitListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            DatabaseConnection.initializeSchema();
        } catch (Throwable t) {
            LOG.log(Level.SEVERE, "Database setup failed: " + DatabaseConnection.describeError(t), t);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            // Stop MySQL's cleanup thread so Tomcat does not warn about leaks on redeploy.
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        } catch (Throwable ignored) {
        }
    }
}
