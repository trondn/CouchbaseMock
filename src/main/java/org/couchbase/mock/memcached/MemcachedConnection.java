/**
 *     Copyright 2011 Membase, Inc.
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
package org.couchbase.mock.memcached;

import org.couchbase.mock.memcached.protocol.BinaryResponse;
import org.couchbase.mock.memcached.protocol.BinaryCommand;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedList;
import java.util.Queue;

import org.couchbase.mock.memcached.protocol.CommandFactory;

/**
 * @author Trond Norbye
 */
class MemcachedConnection {

    private final BinaryProtocolHandler protocolHandler;
    private final byte header[];
    private BinaryCommand command;
    private final ByteBuffer input;
    private final Queue<ByteBuffer> output;
    private boolean authenticated;
    private boolean closed;

    public MemcachedConnection(MemcachedServer server) {
        closed = false;
        authenticated = server.getBucket().getPassword().length() <= 0;
        header = new byte[24];
        input = ByteBuffer.wrap(header);
        protocolHandler = server.getProtocolHandler();
        output = new LinkedList<ByteBuffer>();
    }

    public void step() throws IOException {
        if (closed) {
            throw new ClosedChannelException();
        }
        if (input.position() == header.length) {
            if (command == null) {
                command = CommandFactory.create(input);
            }

            if (command.complete()) {
                command.process();
                protocolHandler.execute(command, this);
                command = null;
                input.rewind();
            }
        }
    }

    public void sendResponse(BinaryResponse response) {
        output.add(response.getBuffer());
    }

    boolean hasOutput() {
        return !output.isEmpty();
    }

    public ByteBuffer getInputBuffer() {
        if (command == null) {
            return input;
        } else {
            return command.getInputBuffer();
        }

    }

    public ByteBuffer getOutputBuffer() {
        if (output.isEmpty()) {
            return null;
        } else {
            return output.remove();
        }
    }

    void shutdown() {
        closed = true;
    }

    void setAuthenticated() {
        authenticated = true;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
