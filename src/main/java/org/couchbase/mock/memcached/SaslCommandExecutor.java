/*
 *  Copyright 2011 Couchbase, Inc..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.couchbase.mock.memcached;

import org.couchbase.mock.Bucket;
import org.couchbase.mock.memcached.protocol.BinaryCommand;
import org.couchbase.mock.memcached.protocol.BinarySaslResponse;
import org.couchbase.mock.memcached.protocol.CommandCode;

/**
 * @author Sergey Avseyev <sergey.avseyev@gmail.com>
 */
public class SaslCommandExecutor implements CommandExecutor {

    @Override
    public void execute(BinaryCommand cmd, MemcachedServer server, MemcachedConnection client) {
        CommandCode cc = cmd.getComCode();

        switch (cc) {
            case SASL_LIST_MECHS:
                client.sendResponse(new BinarySaslResponse(cmd, "PLAIN"));
                break;
            case SASL_AUTH:
                String[] clientIn = new String(cmd.getValue()).split("\0");
                Bucket bucket = server.getBucket();
                if (bucket.getName().equals(clientIn[1]) && bucket.getPassword().equals(clientIn[2])) {
                    client.sendResponse(new BinarySaslResponse(cmd, "Authenticated"));
                    client.setAuthenticated();
                } else {
                    client.sendResponse(new BinarySaslResponse(cmd));
                }
                break;
            case SASL_STEP:
                client.sendResponse(new BinarySaslResponse(cmd));
                // This is only useful when the above returns SASL_CONTINUE.  In this
                // implementation, only PLAIN is supported, so it never will
                break;
            default:
                client.sendResponse(new BinarySaslResponse(cmd));
        }
    }
}
