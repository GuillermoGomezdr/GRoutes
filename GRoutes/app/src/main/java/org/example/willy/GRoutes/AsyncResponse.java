package org.example.willy.GRoutes;

import org.json.JSONArray;
import org.json.JSONException;

public interface AsyncResponse {
    void processFinish(JSONArray output) throws JSONException;
}
