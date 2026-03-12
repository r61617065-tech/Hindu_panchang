package com.example.hindupanchang;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ConverterEngine {

    private ConverterEngine() {
    }

    public static String javaToSmali(String javaCode) {
        String code = javaCode == null ? "" : javaCode;

        String pkg = findFirst(code, "package\\s+([\\w.]+)\\s*;");
        String cls = findFirst(code, "class\\s+(\\w+)");
        if (cls.isEmpty()) {
            cls = "GeneratedClass";
        }

        String classDescriptor = "L" + (pkg.isEmpty() ? "" : pkg.replace('.', '/') + "/") + cls + ";";

        StringBuilder out = new StringBuilder();
        out.append(".class public ").append(classDescriptor).append("\n");
        out.append(".super Ljava/lang/Object;\n\n");

        List<FieldInfo> fields = parseJavaFields(code);
        for (FieldInfo f : fields) {
            out.append(".field ").append(f.visibility).append(' ');
            if (f.isStatic) {
                out.append("static ");
            }
            out.append(f.name).append(':').append(toDescriptor(f.type)).append("\n");
        }
        if (!fields.isEmpty()) {
            out.append("\n");
        }

        out.append(".method public constructor <init>()V\n");
        out.append("    .locals 1\n");
        out.append("    invoke-direct {p0}, Ljava/lang/Object;-><init>()V\n");
        out.append("    return-void\n");
        out.append(".end method\n\n");

        for (MethodInfo method : parseJavaMethods(code, cls)) {
            out.append(".method ").append(method.visibility).append(' ');
            if (method.isStatic) {
                out.append("static ");
            }
            out.append(method.name)
                    .append('(').append(method.paramDescriptor).append(')')
                    .append(method.returnDescriptor).append("\n");
            out.append("    .locals 1\n");
            out.append(defaultSmaliReturn(method.returnDescriptor));
            out.append(".end method\n\n");
        }

        return out.toString().trim();
    }

    public static String smaliToJava(String smaliCode) {
        String code = smaliCode == null ? "" : smaliCode;

        String classDescriptor = findFirst(code, "\\.class\\s+[^\\n]*\\s(L[^;]+;)");
        if (classDescriptor.isEmpty()) {
            classDescriptor = "Lgenerated/GeneratedClass;";
        }

        String fqcn = descriptorToJavaType(classDescriptor);
        int dot = fqcn.lastIndexOf('.');
        String pkg = dot > 0 ? fqcn.substring(0, dot) : "";
        String cls = dot > 0 ? fqcn.substring(dot + 1) : fqcn;

        StringBuilder out = new StringBuilder();
        if (!pkg.isEmpty()) {
            out.append("package ").append(pkg).append(";\n\n");
        }

        out.append("public class ").append(cls).append(" {\n\n");

        for (FieldInfo f : parseSmaliFields(code)) {
            out.append("    ").append(f.visibility).append(' ');
            if (f.isStatic) {
                out.append("static ");
            }
            out.append(f.type).append(' ').append(f.name).append(";\n");
        }
        if (!parseSmaliFields(code).isEmpty()) {
            out.append("\n");
        }

        out.append("    public ").append(cls).append("() {\n");
        out.append("    }\n\n");

        for (MethodInfo m : parseSmaliMethods(code)) {
            out.append("    ").append(m.visibility).append(' ');
            if (m.isStatic) {
                out.append("static ");
            }
            out.append(m.returnType).append(' ').append(m.name).append('(').append(m.paramList).append(") {\n");
            if (!"void".equals(m.returnType)) {
                out.append("        return ").append(defaultJavaReturnValue(m.returnType)).append(";\n");
            }
            out.append("    }\n\n");
        }

        out.append('}');
        return out.toString();
    }

    private static List<FieldInfo> parseJavaFields(String code) {
        List<FieldInfo> fields = new ArrayList<>();
        Pattern p = Pattern.compile("(?m)^\\s*(public|private|protected)?\\s*(static\\s+)?(?:final\\s+)?([\\w$.<>\\[\\]]+)\\s+(\\w+)\\s*;");
        Matcher m = p.matcher(code);
        while (m.find()) {
            FieldInfo f = new FieldInfo();
            f.visibility = normalizeVisibility(m.group(1));
            f.isStatic = m.group(2) != null;
            f.type = normalizeType(m.group(3));
            f.name = m.group(4);
            fields.add(f);
        }
        return fields;
    }

    private static List<MethodInfo> parseJavaMethods(String code, String className) {
        List<MethodInfo> methods = new ArrayList<>();
        Pattern p = Pattern.compile("(?m)^\\s*(public|private|protected)?\\s*(static\\s+)?([\\w$.<>\\[\\]]+)\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*\\{");
        Matcher m = p.matcher(code);
        while (m.find()) {
            String methodName = m.group(4);
            if (methodName.equals(className)) {
                continue;
            }
            MethodInfo info = new MethodInfo();
            info.visibility = normalizeVisibility(m.group(1));
            info.isStatic = m.group(2) != null;
            info.name = methodName;
            info.returnType = normalizeType(m.group(3));
            info.returnDescriptor = toDescriptor(info.returnType);
            info.paramDescriptor = buildParamDescriptor(m.group(5));
            methods.add(info);
        }
        return methods;
    }

    private static List<FieldInfo> parseSmaliFields(String code) {
        List<FieldInfo> fields = new ArrayList<>();
        Pattern p = Pattern.compile("(?m)^\\.field\\s+(public|private|protected)?\\s*(static\\s+)?(\\w+):([^\\s]+)");
        Matcher m = p.matcher(code);
        while (m.find()) {
            FieldInfo f = new FieldInfo();
            f.visibility = normalizeVisibility(m.group(1));
            f.isStatic = m.group(2) != null;
            f.name = m.group(3);
            f.type = descriptorToJavaType(m.group(4));
            fields.add(f);
        }
        return fields;
    }

    private static List<MethodInfo> parseSmaliMethods(String code) {
        List<MethodInfo> methods = new ArrayList<>();
        Pattern p = Pattern.compile("(?m)^\\.method\\s+(public|private|protected)?\\s*(static\\s+)?(\\S+)\\(([^)]*)\\)(\\S+)");
        Matcher m = p.matcher(code);
        while (m.find()) {
            String methodName = m.group(3);
            if ("<init>".equals(methodName) || "<clinit>".equals(methodName)) {
                continue;
            }
            MethodInfo method = new MethodInfo();
            method.visibility = normalizeVisibility(m.group(1));
            method.isStatic = m.group(2) != null;
            method.name = methodName;
            method.returnType = descriptorToJavaType(m.group(5));
            method.paramList = descriptorListToParamList(m.group(4));
            methods.add(method);
        }
        return methods;
    }

    private static String descriptorListToParamList(String descriptorList) {
        List<String> descriptors = splitDescriptors(descriptorList);
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < descriptors.size(); i++) {
            if (i > 0) {
                out.append(", ");
            }
            out.append(descriptorToJavaType(descriptors.get(i))).append(" arg").append(i);
        }
        return out.toString();
    }

    private static List<String> splitDescriptors(String list) {
        List<String> descriptors = new ArrayList<>();
        int i = 0;
        while (i < list.length()) {
            char c = list.charAt(i);
            if (c == 'L') {
                int end = list.indexOf(';', i);
                if (end == -1) {
                    break;
                }
                descriptors.add(list.substring(i, end + 1));
                i = end + 1;
            } else if (c == '[') {
                int start = i;
                while (i < list.length() && list.charAt(i) == '[') {
                    i++;
                }
                if (i < list.length() && list.charAt(i) == 'L') {
                    int end = list.indexOf(';', i);
                    if (end == -1) {
                        break;
                    }
                    descriptors.add(list.substring(start, end + 1));
                    i = end + 1;
                } else if (i < list.length()) {
                    descriptors.add(list.substring(start, i + 1));
                    i++;
                }
            } else {
                descriptors.add(String.valueOf(c));
                i++;
            }
        }
        return descriptors;
    }

    private static String buildParamDescriptor(String params) {
        String trimmed = params == null ? "" : params.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        String[] pieces = trimmed.split(",");
        StringBuilder out = new StringBuilder();
        for (String piece : pieces) {
            String p = piece.trim();
            if (p.isEmpty()) {
                continue;
            }
            String[] parts = p.split("\\s+");
            String type = parts.length > 1 ? parts[0] : p;
            out.append(toDescriptor(normalizeType(type)));
        }
        return out.toString();
    }

    private static String defaultSmaliReturn(String returnDescriptor) {
        switch (returnDescriptor) {
            case "V":
                return "    return-void\n";
            case "J":
                return "    const-wide/16 v0, 0x0\n    return-wide v0\n";
            case "F":
                return "    const/4 v0, 0x0\n    return v0\n";
            case "D":
                return "    const-wide/16 v0, 0x0\n    return-wide v0\n";
            case "I":
            case "S":
            case "B":
            case "C":
            case "Z":
                return "    const/4 v0, 0x0\n    return v0\n";
            default:
                return "    const/4 v0, 0x0\n    return-object v0\n";
        }
    }

    private static String defaultJavaReturnValue(String javaType) {
        switch (javaType) {
            case "boolean":
                return "false";
            case "byte":
            case "short":
            case "int":
            case "long":
            case "float":
            case "double":
            case "char":
                return "0";
            default:
                return "null";
        }
    }

    private static String findFirst(String code, String pattern) {
        Matcher matcher = Pattern.compile(pattern).matcher(code);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String normalizeVisibility(String raw) {
        return raw == null || raw.trim().isEmpty() ? "public" : raw.trim();
    }

    private static String normalizeType(String type) {
        return type == null ? "Object" : type.replace("[]", "[]").trim();
    }

    private static String toDescriptor(String javaType) {
        String type = javaType.trim();
        if (type.endsWith("[]")) {
            return "[" + toDescriptor(type.substring(0, type.length() - 2));
        }
        switch (type) {
            case "void": return "V";
            case "boolean": return "Z";
            case "byte": return "B";
            case "char": return "C";
            case "short": return "S";
            case "int": return "I";
            case "long": return "J";
            case "float": return "F";
            case "double": return "D";
            case "String": return "Ljava/lang/String;";
            default:
                String cleaned = type.replace("<", "").replace(">", "");
                if (!cleaned.contains(".")) {
                    cleaned = "java.lang." + cleaned;
                }
                return "L" + cleaned.replace('.', '/') + ";";
        }
    }

    private static String descriptorToJavaType(String descriptor) {
        if (descriptor == null || descriptor.isEmpty()) {
            return "Object";
        }
        if (descriptor.startsWith("[")) {
            return descriptorToJavaType(descriptor.substring(1)) + "[]";
        }

        switch (descriptor) {
            case "V": return "void";
            case "Z": return "boolean";
            case "B": return "byte";
            case "C": return "char";
            case "S": return "short";
            case "I": return "int";
            case "J": return "long";
            case "F": return "float";
            case "D": return "double";
            default:
                if (descriptor.startsWith("L") && descriptor.endsWith(";")) {
                    String fqcn = descriptor.substring(1, descriptor.length() - 1).replace('/', '.');
                    if (fqcn.startsWith("java.lang.")) {
                        return fqcn.substring("java.lang.".length());
                    }
                    return fqcn;
                }
                return descriptor;
        }
    }

    private static final class FieldInfo {
        String visibility;
        boolean isStatic;
        String type;
        String name;
    }

    private static final class MethodInfo {
        String visibility;
        boolean isStatic;
        String name;
        String returnType;
        String returnDescriptor;
        String paramDescriptor;
        String paramList;
    }
}
