package com.github.tkawachi.psbridge;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;

import java.lang.instrument.Instrumentation;
import java.util.List;

public class PSBridge {
    public static void premain(String agentArgs, Instrumentation inst) {
        AWSSimpleSystemsManagement ssm = AWSSimpleSystemsManagementClientBuilder.defaultClient();
        String[] paths = (agentArgs == null ? "/" : agentArgs).split(",");
        for (String path: paths) {
            loadPath(ssm, path);
        }
        ssm.shutdown();
    }

    private static void loadPath(final AWSSimpleSystemsManagement ssm, final String path) {
        String nextToken = null;
        while (true) {
            final GetParametersByPathRequest request = new GetParametersByPathRequest().withPath(path).withNextToken(nextToken).withWithDecryption(true);
            final GetParametersByPathResult result = ssm.getParametersByPath(request);
            final List<Parameter> parameters = result.getParameters();
            for (Parameter param : parameters) {
                final String[] splitName = param.getName().split("/");
                final String key = splitName[splitName.length - 1];
                switch (param.getType()) {
                    default:
                        throw new RuntimeException("Unknown parameter type: " + param.getType());
                    case "String":
                    case "SecureString":
                        final String value = param.getValue();
                        System.setProperty(key, value);
                        break;
                    case "StringList":
                        // StringList is loaded in typesafe config compatible manner.
                        // For example, key=a,b,c is loaded as key.0=a key.1=b key.2=c.
                        final String[] values = param.getValue().split(",");
                        int i = 0;
                        // Set an each list element as a separated property.
                        for (; i < values.length; ++i) {
                            System.setProperty(getIndexedKey(key, i), values[i]);
                        }
                        // Remove properties at additional indices if exist.
                        for (; System.getProperty(getIndexedKey(key, i)) != null; ++i) {
                            System.clearProperty(getIndexedKey(key, i));
                        }
                        break;
                }
            }
            if (result.getNextToken() == null) {
                break;
            }
            nextToken = result.getNextToken();
        }
    }

    private static String getIndexedKey(String key, int i) {
        return key + "." + i;
    }
}

