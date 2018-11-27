package me.dslztx.tools;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.dslztx.assist.util.ArrayAssist;
import me.dslztx.assist.util.CloseableAssist;
import me.dslztx.assist.util.IOAssist;
import me.dslztx.assist.util.ObjectAssist;
import me.dslztx.assist.util.StringAssist;

public class MachineApplicationMap {

    String machinePath;
    String[] moduleDeployMachinePaths;

    List<String> machines = new ArrayList<>();
    Map<String, List<String>> map = new HashMap<>();

    public MachineApplicationMap(String machinePath, String[] moduleDeployMachinePaths) {
        this.machinePath = machinePath;
        this.moduleDeployMachinePaths = moduleDeployMachinePaths;
    }

    public static void main(String[] args) {
        if (ArrayAssist.isEmpty(args) || args.length < 2) {
            System.err.println("need at least two arguments");
            System.exit(1);
        }

        String[] dst = new String[args.length - 1];
        System.arraycopy(args, 1, dst, 0, args.length - 1);
        MachineApplicationMap machineApplicationMap = new MachineApplicationMap(args[0], dst);
        machineApplicationMap.init();
        machineApplicationMap.join();
        machineApplicationMap.output();
    }

    public void init() {
        BufferedReader in = null;
        try {
            in = IOAssist.bufferedReader(new File(machinePath));
            String line;
            while ((line = in.readLine()) != null) {
                if (StringAssist.isBlank(line)) {
                    continue;
                }

                machines.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseableAssist.closeQuietly(in);
        }
    }

    public void join() {
        for (String moduleDeployMachinePath : moduleDeployMachinePaths) {
            joinOne(moduleDeployMachinePath);
        }
    }

    private void joinOne(String path) {
        File file = new File(path);
        BufferedReader in = null;
        try {
            in = IOAssist.bufferedReader(file);

            String moduleName = noSuffix(file.getName());

            String line;
            while ((line = in.readLine()) != null) {
                for (String machine : machines) {
                    if (line.contains(machine)) {
                        if (ObjectAssist.isNull(map.get(machine))) {
                            map.put(machine, new ArrayList<String>());
                        }

                        map.get(machine).add(moduleName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseableAssist.closeQuietly(in);
        }
    }

    private String noSuffix(String name) {
        int pos = name.lastIndexOf(".");
        if (pos == -1)
            return name;
        return name.substring(0, pos);
    }

    public void output() {
        StringBuilder sb = new StringBuilder();
        for (String machine : machines) {
            sb.append("|");
            sb.append(machine);
            sb.append("|");
            List<String> modules = map.get(machine);
            sb.append(StringAssist.joinUseSeparator(modules, ','));
            sb.append("|");
            sb.append("\n");
        }
        System.out.println(sb.toString());
    }
}
