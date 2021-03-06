/*
 * Copyright 2013 Couchbase, Inc.
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
package org.couchbase.mock.harakiri;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.couchbase.mock.CouchbaseMock;
import org.couchbase.mock.control.*;

/**
 *
 * @author Mark Nunberg
 */
public class HarakiriDispatcher {
    public enum PayloadFormat { JSON, PLAIN }

    public static final Map<String,Class> commandMap = new HashMap<String, Class>();
    private static final Map<HarakiriCommand.Command, Class> classMap
            = new EnumMap<HarakiriCommand.Command, Class>(HarakiriCommand.Command.class);

    private static void registerClass(HarakiriCommand.Command cmd, Class cls) {
        if (!HarakiriCommand.class.isAssignableFrom(cls)) {
            throw new RuntimeException("Can process only HarakiriMonitor objects");
        }

        String commandName = cmd.toString().toUpperCase();

        commandMap.put(commandName, cls);
        classMap.put(cmd, cls);
    }

    static {
        registerClass(HarakiriCommand.Command.HICCUP, HiccupCommandHandler.class);
        registerClass(HarakiriCommand.Command.FAILOVER, FailoverCommandHandler.class);
        registerClass(HarakiriCommand.Command.TRUNCATE, TruncateCommandHandler.class);
        registerClass(HarakiriCommand.Command.RESPAWN, RespawnCommandHandler.class);
        registerClass(HarakiriCommand.Command.MOCKINFO, MockInfoCommandHandler.class);
        registerClass(HarakiriCommand.Command.CACHE, PersistenceCommandHandler.class);
        registerClass(HarakiriCommand.Command.UNCACHE, PersistenceCommandHandler.class);
        registerClass(HarakiriCommand.Command.PERSIST, PersistenceCommandHandler.class);
        registerClass(HarakiriCommand.Command.UNPERSIST, PersistenceCommandHandler.class);
        registerClass(HarakiriCommand.Command.ENDURE, PersistenceCommandHandler.class);
        registerClass(HarakiriCommand.Command.PURGE, PersistenceCommandHandler.class);
        registerClass(HarakiriCommand.Command.KEYINFO, KeyInfoCommandHandler.class);
        registerClass(HarakiriCommand.Command.HELP, HarakiriHelpCommand.class);
    }


    // Instance members
    private final CouchbaseMock mock;

    public HarakiriCommand getCommand(PayloadFormat fmt, String commandString, Object payloadObj) {
        HarakiriCommand obj;

        if (!commandMap.containsKey(commandString.toUpperCase())) {
            throw new CommandNotFoundException("Unknown command: " + commandString);
        }

        HarakiriCommand.Command cmd;
        Class cls;

        try {
            cmd = HarakiriCommand.Command.valueOf(commandString.toUpperCase());

        } catch (IllegalArgumentException e) {
            throw new CommandNotFoundException("No such command: " + commandString, e);
        }

        cls = classMap.get(cmd);
        if (cls == null) {
            throw new RuntimeException("Can't find class for " + cmd);
        }

        try {
            obj = (HarakiriCommand)cls.getConstructor
                    (CouchbaseMock.class).newInstance(mock);
            obj.command = cmd;

        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        switch (fmt) {
            case JSON:
                obj.payload = (JsonObject)payloadObj;
                obj.handleJson(obj.payload);
                break;

            case PLAIN:
                obj.handlePlain((List<String>)payloadObj);
        }

        obj.execute();

        return obj;
    }

    public HarakiriDispatcher(CouchbaseMock mock) {
        this.mock = mock;
    }

    public CouchbaseMock getMock() {
        return mock;
    }
}
