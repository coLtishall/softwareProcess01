package application;

import OriginSets.IntervalSet;
import OriginSets.MultiIntervalSet;
import concrete.ProcessIntervalSet;
import dimensions.NonlapException;
import dimensions.NonperiodicException;
import entity.Process;

import java.util.*;


public class ProcessScheduleApp {
    private static final Scanner input = new Scanner(System.in);

    private final static  List<Process> processes = new ArrayList<>();
    private final static Map<Process, Long> processTodoMap = new HashMap<>();
    private final static MultiIntervalSet<Process> processSchedule = new ProcessIntervalSet<>();
    //Abstraction function:
    //  AF(processes) = 进程列表
    //  AF(processTodoMap) = 还未执行完毕的进程及其剩余时间
    //  AF(processSchedule) = 调度系统时间轴

    //Representation invariant:
    //  同一进程执行时间段不能重叠，不能存储相同进程
    //  对每个进程，已执行时间大于0且小于最大执行时间

    //Safety from rep exposure:
    //  成员变量中，可变类型为private final，不可变类型为private
    //  防御性拷贝返回可变类型

    /**
     * 读取用户输入的数字，用于用户选择选项
     * @return 返回用户输入的数字
     */
    private static int readNum() {
        String str = input.next();
        while (!str.matches("[0-9]+")) {
            System.err.println("该输入不是数字，请重新输入");
            str = input.next();
        }
        return Integer.parseInt(str);
    }
    /**
     * 使用最短进程优先策略对进程进行调度。
     *
     * @return 如果没有进程可供调度或进程已被安排调度，返回false；否则返回true。
     *
     * <p>方法会检查是否有进程可供调度，如果没有，打印消息并返回false。
     * <p>方法会检查是否已经进行了调度，如果已经进行了调度，打印消息并返回false。
     * <p>如果上述两种情况均未发生，进行最短优先调度，打印消息表示最短进程优先策略调度已完成，并返回true。
     */
    private static int flag=0;

    private static boolean minFirstGet(){
        if (processes.isEmpty()) {
            System.out.println("调度系统中无进程储备！");
            return false;
        }
        if (!processSchedule.isEmpty()) {
            System.out.println("进程已被部分安排调度！");
            return false;
        }
        flag=1;
        Random rand = new Random();
        long timePoint = 0;
        List<Process> temp_processes = new ArrayList<>(processes); // 尚未完全执行的进程
        while (!temp_processes.isEmpty()) {
            // 间隔时间
            long blankTime = rand.nextInt(5);
            timePoint += blankTime;
            long remainTime = Long.MAX_VALUE;
            // 找到最短的剩余时间的进程
            for (Process temp : temp_processes) {
                long temp_remainTime = temp.getMaxEXT() - processTodoMap.get(temp);
                remainTime = Math.min(temp_remainTime, remainTime);
            }
            Process p = temp_processes.get(0);
            for (Process temp : temp_processes) {
                long temp_remainTime = temp.getMaxEXT() - processTodoMap.get(temp);
                if (temp_remainTime == remainTime) {
                    p = temp;
                }
            }
            long minEXT = p.getMinEXT();
            long maxEXT = p.getMaxEXT();
            // 计算下一个进程的执行情况
            long thisTime = (long) (rand.nextDouble() * maxEXT);
            long executed = processTodoMap.get(p);
            long totalTime = thisTime + executed;
            if (totalTime >= maxEXT) { //时间超出
                thisTime = maxEXT - executed;
                totalTime = maxEXT;
                temp_processes.remove(p);
            }
            if (totalTime >= minEXT) { // 进程执行完毕，无异常
                temp_processes.remove(p);
            }
            try {
                processSchedule.insert(timePoint, timePoint + thisTime, p);
            } catch (NonlapException e) {
                System.out.println("同种进程的时间重复");
            } catch (NonperiodicException e) {
                System.out.println("不符合非周期性");
            }
            processTodoMap.put(p, totalTime);
            timePoint += thisTime;
        }
        System.out.println("调度完毕");
        return true;
    }

    /**
     * 使用随机进程策略对进程进行调度。
     * @return 如果没有进程可供调度或进程已被安排调度，返回false；否则返回true。
     *
     * <p>方法会检查是否有进程可供调度，如果没有，打印消息并返回false。
     * <p>方法会检查是否已经进行了调度，如果已经进行了调度，打印消息并返回false。
     * <p>如果上述两种情况均未发生，进行随机调度，打印消息表示随机进程策略调度已完成，并返回true。
     */
    private static boolean randomGet() {
        if (processes.isEmpty()) {
            System.out.println("调度系统中无进程储备");
            return false;
        }
        if (!processSchedule.isEmpty()) {
            System.out.println("进程已被部分安排调度");
            return false;
        }
        flag=1;
        Random rand = new Random();
        long timePoint = 0;
        List<Process> temp_processes = new ArrayList<>(processes); // 尚未完全执行的进程
        while (!temp_processes.isEmpty()) {
            // 间隔时间
            long blankTime = rand.nextInt(5);
            timePoint += blankTime;
            int size = temp_processes.size();
            int random;
            if (size > 1) // 随机选取
                random = rand.nextInt(size - 1);
            else
                random = 0;
            Process p = temp_processes.get(random);
            long minTime = p.getMinEXT();
            long maxTime = p.getMaxEXT();
            // 计算下一个进程的执行情况
            long thisTime = (long) (rand.nextDouble() * maxTime);
            long executed = processTodoMap.get(p);
            long totalTime = thisTime + executed;
            if (totalTime >= maxTime) { //时间超出
                thisTime = maxTime - executed;
                totalTime = maxTime;
                temp_processes.remove(p);
            }
            if (totalTime >= minTime) { // 进程执行完毕，无异常
                temp_processes.remove(p);
            }
            try {
                processSchedule.insert(timePoint,timePoint+thisTime,p);
            } catch (NonlapException e) {
                System.out.println("同种进程时间重叠");
            } catch (NonperiodicException e) {
                System.out.println("不符合非周期性");
            }

            processTodoMap.put(p, totalTime);
            timePoint += thisTime;
        }
        System.out.println("调度完毕");
        return true;
    }
    /**
     * 显示当前的进程调度结果。
     * <p>它会打印出所有进程的情况，包括每个进程的ID、名称、最小执行时间、最大执行时间和执行状态。
     * 如果进程已经执行，则执行状态为"已执行"，否则为"未调度"。
     * <p>如果还没有进行调度，它会打印"尚未进行调度"。
     * 如果已经进行了调度，它会打印出每个进程在时间线上的位置，包括每个进程的起始时间、结束时间、ID和名称。
     * <p>这些进程按照它们在时间线上的位置进行排序，从最早开始的进程开始打印。
     */
    public static void showProcess() {
        System.out.println("进程执行情况");
        if (processes.isEmpty())
            System.out.println("无进程");
        else {
            System.out.println("当前进程：");
            for (Process p : processes) {
                if (processTodoMap.get(p) >= p.getMinEXT())
                    System.out.printf("ID:%d    名称:%s    状态:已执行完毕\n",
                            p.getpID(), p.getpName(), p.getMinEXT(), p.getMaxEXT());
                else
                    System.out.printf("ID:%d    名称:%s    状态:未开始调度\n",
                            p.getpID(), p.getpName(), p.getMinEXT(), p.getMaxEXT());
            }
        }

        System.out.println("进程调度情况");
        if (processSchedule.isEmpty()&&flag==0) {
            System.out.println("未开始调度");
        } else {
            System.out.println("当前调度：");
            // 对进程进行排序
            List<List<long[]>> info = getInfo();
            while (!info.isEmpty()) {
                long start = Long.MAX_VALUE, end = 0;
                long ID = 0;
                String name = null;
                for (List<long[]> entry : info) { // 找到最小进程对应的起始与终点
                    if (entry.get(1)[0] < start) {
                        start = entry.get(1)[0];
                        end = entry.get(1)[1];
                        ID = entry.get(0)[0];
                    }
                }
                for (Process p : processes) {
                    if (p.getpID()==ID)
                        name = p.getpName();
                }
                System.out.printf("[%d ~ %d] ： ID:%d ， 进程名：%s\n", start, end, ID, name);
                // 删除list中的该元素
                Iterator<List<long[]>> it = info.iterator();
                while (it.hasNext()) {
                    List<long[]> entry = it.next();
                    if (entry.get(1)[0] == start && entry.get(1)[1] == end)
                        it.remove();
                }
            }
        }
    }

    /**
     * 向进程调度系统中添加指定数量的进程。
     *
     * @param num 要添加的进程数量。如果 num 为0，程序会打印一条消息并退出。
     *
     * <p>对于每一个要添加的进程，方法会提示用户输入进程的相关信息，包括ID、名称、最短执行时间和最长执行时间。
     * <p>如果用户输入的最短执行时间大于或等于最长执行时间，
     *            方法会提示用户重新输入最长执行时间，直到最短执行时间小于最长执行时间为止。
     * <p>如果进程是否已经存在于 processTodo 映射中，方法会打印一条消息并跳过当前循环，不会添加这个进程。
     * <p>如果进程不存在于 processTodo 映射中，方法会将新的 Process 对象添加到 processTodo 映射中，
     *            并将其执行时间初始化为0。
     */
    private static void addProcess(int num){
        if(num == 0) {
            System.out.print("无可执行的进程，程序退出\n");
            System.exit(0);
        }
        for(int i = 0; i < num; i++) {
            System.out.printf("请输入第 %d个进程相关信息:\n", i + 1);
            System.out.print("ID: ");
            long id = readNum();
            System.out.print("Name: ");
            String name = input.next();
            System.out.print("最短执行时间: ");
            int min = readNum();
            System.out.print("最长执行时间: ");
            int max = readNum();
           if(min>=max){
               System.out.println("最短执行时间大于或等于最长执行时间，请重新输入");
                i--;
                continue;
           }
            Process process = new Process(id, name, (long)min, (long)max);
            if(processTodoMap.containsKey(process)){
                System.out.println("该进程已存在，添加失败");
               i--;
                continue;
            }
            processes.add(process);
            processTodoMap.put(process, 0L);
            System.out.println("添加进程成功");
        }
    }

    /**
     * 返回调度时间轴中的信息
     * @return 一个包含每个标签的调度时间的List<List<long[]>
     * <p>外部List表示所有进程，
     * 内部List表示单个进程的信息，long数组中存储该进程执行的ID以及开始、结束是按
     */
    private static List<List<long[]>> getInfo() {
        List<List<long[]>> info = new ArrayList<>();
        for (Process p : processSchedule.labels()) {
            IntervalSet<Integer> intervalSet = processSchedule.intervals(p);
            Set<Integer> set = intervalSet.labels();

            for (int i : set) {
                List<long[]> subInfo = new ArrayList<>();
                long start = intervalSet.start(i);
                long end = intervalSet.end(i);
                long[] ID = {p.getpID()};
                long[] time = {start, end};
                subInfo.add(ID);
                subInfo.add(time);
                info.add(subInfo);
            }
        }
        return info;
    }

    /**
     * 展示用户选择菜单，读取用户选择，进行对应进程操作
     * <p>选择1->添加进程
     * <p>选择2->进行随机调度
     * <p>选择3->进行最短进程优先调度
     * <p>选择4->展示调度情况
     * <p>选择0->退出调度系统
     */
    private static void menu(){
        System.out.println("——————菜单——————");
        System.out.println("1.添加进程");
        System.out.println("2.随机调度");
        System.out.println("3.最短进程优先原则调度");
        System.out.println("4.调度情况展示");
        System.out.println("0.退出调度系统");
        System.out.println();
        System.out.print("请输入要使用的功能(0~4):");
        int choice =readNum();
        switch (choice){
            case 0:
                System.out.println("调度程序退出");
                System.exit(0);
                break;
            case 1:
                System.out.println("请输入需要添加的进程数量");
                int num=readNum();
                addProcess(num);
                break;
            case 2:
                randomGet();
                break;
            case 3:
                minFirstGet();
                break;
            case 4:
                showProcess();
                break;
            default:
                System.out.println("输入非法，请输入0~4的数字");
        }
    }
    public static void main(String[] args) {
        while(true){
            menu();
        }
    }
}
