/**
 *     Copyright 2012 Couchbase, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.couchbase.mock.control;

import com.google.gson.JsonObject;
import java.util.List;
import org.couchbase.mock.CouchbaseMock;

import org.couchbase.mock.memcached.MemcachedServer;

/**
 * Hiccup will let all servers sleep after sending a specific amount of data.
 *
 * @author M. Nunberg
 */
public class HiccupCommandHandler extends ServersCommandHandler {

    private int milliSeconds;
    private int offset;

    @Override
    protected void handleJson(JsonObject payload) {
        milliSeconds = payload.get("msecs").getAsInt();
        offset = payload.get("offset").getAsInt();
    }

    @Override
    protected void handlePlain(List<String> tokens) {
        milliSeconds = Integer.parseInt(tokens.get(0));
        offset = Integer.parseInt(tokens.get(1));
    }

    @Override
    void doServerCommand(MemcachedServer server) {
        server.setHiccup(milliSeconds, offset);
    }

    public HiccupCommandHandler(CouchbaseMock m) {
        super(m);
    }
}
