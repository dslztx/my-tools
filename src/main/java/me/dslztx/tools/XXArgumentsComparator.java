package me.dslztx.tools;

import java.io.BufferedReader;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.dslztx.assist.util.CloseableAssist;
import me.dslztx.assist.util.IOAssist;
import me.dslztx.assist.util.ObjectAssist;
import me.dslztx.assist.util.StringAssist;

public class XXArgumentsComparator {

    File a;
    File b;

    Map<String, XXArgument> aMap;
    Map<String, XXArgument> bMap;

    public XXArgumentsComparator(File a, File b) {
        this.a = a;
        this.b = b;
    }

    public static void main(String[] args) {
        if (ObjectAssist.isNull(args) || args.length != 2) {
            System.out.println("need two files");
        }
        XXArgumentsComparator comparator = new XXArgumentsComparator(new File(args[0]), new File(args[1]));

        comparator.build();

        comparator.printDiff();
    }

    public void printDiff() {
        Set<String> commonArguments = obtainCommonArguments(aMap, bMap);

        printInANotInB(commonArguments);

        printInBNotInA(commonArguments);

        printCommonDiff(commonArguments);
    }

    private void printCommonDiff(Set<String> commonArguments) {
        System.out.println("## **三、CommonDiff**");
        System.out.println("|type|name|sign|value|desc|");
        System.out.println("|-|-|-|-|-|");
        for (String key : commonArguments) {
            if (aMap.get(key).equals(bMap.get(key))) {
                continue;
            }

            System.out.println(construct(aMap.get(key), bMap.get(key)));
        }
    }

    private String construct(XXArgument a, XXArgument b) {
        StringBuilder sb = new StringBuilder();
        sb.append("|");
        sb.append(a.getType()).append(" vs ").append(b.getType());
        sb.append("|");
        sb.append(a.getName());
        sb.append("|");
        sb.append(a.isDefaultIs() ? "=" : ":=").append(" vs ").append(b.isDefaultIs() ? "=" : ":=");
        sb.append("|");
        sb.append(a.getValue()).append(" vs ").append(b.getValue());
        sb.append("|");
        sb.append(a.getDesc()).append(" vs ").append(b.getDesc());
        sb.append("|");
        return sb.toString();
    }

    private void printInANotInB(Set<String> commonArguments) {
        System.out.println("## **一、InANotInB**");
        System.out.println("|type|name|sign|value|desc|");
        System.out.println("|-|-|-|-|-|");

        print0(aMap, commonArguments);

        System.out.println();
    }

    private void printInBNotInA(Set<String> commonArguments) {
        System.out.println("## **二、InBNotInA**");
        System.out.println("|type|name|sign|value|desc|");
        System.out.println("|-|-|-|-|-|");

        print0(bMap, commonArguments);

        System.out.println();
    }

    private void print0(Map<String, XXArgument> map, Set<String> commonArguments) {
        for (String key : map.keySet()) {
            if (commonArguments.contains(key)) {
                continue;
            }

            System.out.println(map.get(key).toString());
        }
    }

    private Set<String> obtainCommonArguments(Map<String, XXArgument> aMap, Map<String, XXArgument> bMap) {
        Set<String> result = new HashSet<>(aMap.keySet());

        result.retainAll(bMap.keySet());

        return result;
    }

    private void build() {
        aMap = build0(a);
        bMap = build0(b);
    }

    private Map<String, XXArgument> build0(File file) {
        Map<String, XXArgument> result = new HashMap<>();
        BufferedReader in = null;
        try {
            in = IOAssist.bufferedReader(file);
            String line = null;
            while ((line = in.readLine()) != null) {
                if (!line.contains("=")) {
                    continue;
                }

                String[] ss = StringAssist.split(line, ' ', true);
                if (ObjectAssist.isNull(ss) || ss.length != 5) {
                    continue;
                }

                result.put(ss[1], new XXArgument(ss));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseableAssist.closeQuietly(in);
        }

        return result;
    }
}

class XXArgument {

    String type;

    String name;

    boolean defaultIs;

    String value;

    String desc;

    public XXArgument(String[] ss) {
        type = ss[0];
        name = ss[1];
        if (ss[2].startsWith(":")) {
            defaultIs = false;
        } else {
            defaultIs = true;
        }
        value = ss[3];
        desc = ss[4];
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefaultIs() {
        return defaultIs;
    }

    public void setDefaultIs(boolean defaultIs) {
        this.defaultIs = defaultIs;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("|");
        sb.append(type);
        sb.append("|");
        sb.append(name);
        sb.append("|");
        sb.append((defaultIs ? "=" : ":="));
        sb.append("|");
        sb.append(value);
        sb.append("|");
        sb.append(desc);
        sb.append("|");
        return sb.toString();
    }

    @Override
    public boolean equals(Object b) {
        if (b == null || !(b instanceof XXArgument))
            return false;

        XXArgument bb = (XXArgument)b;

        return ObjectAssist.equals(type, bb.getType()) && ObjectAssist.equals(name, bb.getName())
            && ObjectAssist.equals(value, bb.getValue()) && ObjectAssist.equals(desc, bb.getDesc())
            && (defaultIs == bb.isDefaultIs());
    }
}
