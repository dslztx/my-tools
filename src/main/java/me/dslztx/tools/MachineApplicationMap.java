package me.dslztx.tools;

import java.io.BufferedReader;
import java.io.File;
import java.util.*;

import me.dslztx.assist.util.*;

public class MachineApplicationMap {

    String machinePath;
    String[] applicationDeployMachinePaths;

    List<String> machines = new ArrayList<>();
    Map<String, List<String>> map = new HashMap<>();

    public MachineApplicationMap(String machinePath, String[] applicationDeployMachinePaths) {
        this.machinePath = machinePath;
        this.applicationDeployMachinePaths = applicationDeployMachinePaths;
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
        for (String applicationDeployMachinePath : applicationDeployMachinePaths) {
            joinOne(applicationDeployMachinePath);
        }
    }

    private void joinOne(String path) {
        File file = new File(path);
        BufferedReader in = null;
        try {
            in = IOAssist.bufferedReader(file);

            String applicationName = noSuffix(file.getName());

            String line;
            while ((line = in.readLine()) != null) {
                for (String machine : machines) {
                    if (includeMachine(line, machine)) {
                        if (ObjectAssist.isNull(map.get(machine))) {
                            map.put(machine, new ArrayList<String>());
                        }

                        map.get(machine).add(applicationName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseableAssist.closeQuietly(in);
        }
    }

    private boolean includeMachine(String line, String machine) {
        int pos = line.indexOf(machine);
        if (pos == -1) {
            return false;
        }

        int end = pos + machine.length();
        if (line.length() == end) {
            return true;
        }

        char c = line.charAt(end);
        if (CharAssist.isDecimalChar(c) || CharAssist.isEnglishChar(c)) {
            return false;
        }

        return true;
    }

    private String noSuffix(String name) {
        int pos = name.lastIndexOf(".");
        if (pos == -1) {
            return name;
        }
        return name.substring(0, pos);
    }

    public void output() {
        StringBuilder sb = new StringBuilder();
        sb.append("The machine path is: ");
        sb.append(machinePath);
        sb.append("\n");
        sb.append("The application deploy machine paths are: ");
        sb.append(StringAssist.joinUseSeparator(Arrays.asList(applicationDeployMachinePaths), ','));
        sb.append("\n");
        sb.append("\n");

        sb.append("|machine|applications|\n");
        sb.append("|-|-|\n");
        for (String machine : machines) {
            sb.append("|");
            sb.append(machine);
            sb.append("|");
            List<String> applications = map.get(machine);
            sb.append(StringAssist.joinUseSeparator(applications, ','));
            sb.append("|");
            sb.append("\n");
        }
        System.out.println(sb.toString());
    }
}
