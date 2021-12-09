package com.github.fenrir.xcommon.clients.xplanner.api.rest.entities;

public class ExecuteResponse {
    /**
     * 0: start running
     * 1: other plan is running
     * 2: plan not exist
     * 3: unknown error
     */
    public int status;
    public String msg;
}
