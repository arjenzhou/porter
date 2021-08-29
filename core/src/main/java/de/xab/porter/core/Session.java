package de.xab.porter.core;

import de.xab.porter.api.task.Context;

/**
 * a session represent a transmission action was created
 */
public class Session {

    public void start(Context context) {
        Task task = new Task(context);
        task.init();
        task.start();
    }
}
