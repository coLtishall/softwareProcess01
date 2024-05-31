package application;

import API.APIs;
import concrete.DutyIntervalSet;
import dimensions.NonlapException;
import entity.Employee;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DutyRosterApp {
    private final static long[][] dayOfMonth = { { 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 },
            { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 } };

    private final static Scanner input = new Scanner(System.in);
    private final static Map<Employee, Boolean> PeopleWithStatus = new HashMap<>();
    private final static APIs<Employee> api=new APIs<>();

    private static DutyIntervalSet<Employee> dutySet = null;

    // Abstract function:
    // 一个值班排班应用，它管理和调度员工的值班。它有一个值班时间表 dutySet，
    // 它是一个 DutyIntervalSet 对象，表示整个值班时间段。
    // 它有一个员工状态表 PeopleWithStatus，它是一个映射，键是 Employee 对象，值是布尔值，
    // 表示该员工是否已被安排值班bel存储在L中的MultiIntervalSet组成的集合，
    // 一个label可以对应多个时间段

    // Representation invariant:
    // PeopleWithStatus 中的每个 Employee 对象都不为 null。
    //PeopleWithStatus 中的每个布尔值都不为 null，并满足所有成员变量的RI。

    // Safety from rep exposure:
    // 所有的字段都是 private 的。
    // PeopleWithStatus 的修改方法都是 private 的，客户端无法修改它。
    // dutySet 不可变。
    // 所有返回可变对象的方法都返回防御性拷贝的对象

    /**
     * 用于从用户输入创建值班总时间表
     * @param start 值班总时间段开始时间
     * @param end 值班总时间段结束时间
     */
    private static void makeWholeTimeLine(long start,long end){
        dutySet=new DutyIntervalSet<>(start,end);
    }

    /**
     * 向用户展示菜单并记录用户选择
     * @return 用户选择的选项序号
     */
    private static int menu() {
        System.out.println("—————————菜单—————————");
        System.out.printf("%-15s %-15s %-15s\n", "1.自动排班", "2.手动排班", "3.检查排班进度");
        System.out.printf("%-15s %-15s %-15s\n", "4.添加新员工", "5.删除员工", "6.删除员工排班");
        System.out.printf("%-15s %-15s %-15s\n", "7.展示排班", "8.已排班员工", "9.未排班员工");
        System.out.println("0.退出");
        System.out.println("**若排班表已从文件读入，则无法自动生成排班表**");
        return readNum();
    }

    /**
     * 读取并限定用户输入数字
     * @return 用户输入的数字
     */
    private static int readNum() {
        String str = input.next();
        while (!str.matches("[0-9]+")) {
            System.err.println("请输入正确选项");
            str = input.next();
        }
        return Integer.parseInt(str);
    }

    /**
     * 将用户输入的有效部分转换为long形式
     * @param year 对应有效年
     * @param month 对应有效月
     * @param day 对应有效日
     * @return 一种表示时间的long
     */
    private static long transTime(int year, int month, int day) {
        long sum = 0;
        sum += year * 365L + (year + 3) / 4;
        for (int i = 0; i < month - 1; i++) {
            if (year % 4 == 0)
                sum += dayOfMonth[0][i];
            else
                sum += dayOfMonth[1][i];
        }
        sum += day;
        return sum;
    }

    /**
     * 用户输入时间，并将其转换为long形式
     * @return 时间的long形式
     */
    private static long getTime() {
        while (true) {
//            System.out.println("请输入日期（格式为：年-月-日）：");
            String dateStr = input.next();

            String[] parts = dateStr.split("-");
            if (parts.length != 3) {
                System.err.println("日期格式错误，正确的格式为：年-月-日，请重新输入该日期");
                continue;
            }

            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);

            if (month < 0 || month > 12) {
                System.err.println("月份错误，应在1-12之间，请重新输入该日期");
                continue;
            }

            if (year % 4 == 0) {
                if (day <= 0 || day > dayOfMonth[0][month - 1]) {
                    System.err.println("天数错误（与月份不匹配）,请重新输入该日期");
                    continue;
                }
            } else {
                if (day <= 0 || day > dayOfMonth[1][month - 1]) {
                    System.err.println("天数错误（与月份不匹配）");
                    continue;
                }
            }

            return transTime(year, month, day);
        }
    }

    /**
     * 从long转化为年月日
     * @param sum 年月日的long表示
     * @return List<Integer>，0、1、2的位置分别为年、月、日
     */
    public static List<Integer> longTrans(long sum){
        List<Integer> time=new ArrayList<>();
        int year = yearFromLong(sum);
        int month = monthFromLong(sum);
        int day = dayFromLong(sum);
        time.add(year);
        time.add(month);
        time.add(day);
        return time;
    }
    /**
     * 从long形式的时间中读取年
     * @param sum long形式时间
     * @return 对应时间的年份
     */
    private static int yearFromLong(long sum) {
        if (sum % (365 * 3 + 366) == 0)
            return (int) (sum / (365 * 3 + 366)) * 4 - 1;
        int year = (int) (sum / (365 * 3 + 366)) * 4;
        int dayCount = (int) (sum % (365 * 3 + 366));
        if (dayCount > 731 && dayCount <= 1096) {
            year += 2;
        } else if (dayCount > 366 && dayCount <= 731) {
            year += 1;
        } else if (dayCount > 366 + 365 * 2) {
            year += 3;
        }
        return year;
    }
    /**
     * 从long形式的时间中读取月
     * @param sum long形式时间
     * @return 对应时间的月份
     */
    private static int monthFromLong(long sum) {

        int monthCount = 0;
        int month = 0;
        int year = yearFromLong(sum);
        int sum1 = year * 365 + (year + 3) / 4;
        int minus = (int) (sum - sum1);

        for (int i = 0; i < 12; i++) {
            if (year % 4 == 0)
                monthCount += dayOfMonth[0][i];
            else
                monthCount += dayOfMonth[1][i];
            month += 1;
            if (monthCount >= minus) {
                break;
            }
        }
        return month;
    }

    /**
     * 从long形式的时间中读取日
     * @param sum long形式时间
     * @return 对应时间的日期
     */
    private static int dayFromLong(long sum) {
        int year = yearFromLong(sum);
        int month = monthFromLong(sum);
        int s = 0;
        s += year * 365 + (year + 3) / 4;
        for (int i = 0; i < month - 1; i++) {
            if (year % 4 == 0)
                s += dayOfMonth[0][i];
            else
                s += dayOfMonth[1][i];
        }
        return (int) (sum - s);
    }

    /**
     * 显示已做安排员工表或未做安排员工表，
     * 其中，未做安排员工可以被安排进排班表，已做安排员工若满足安排进排班表后，值班时间不出现间断，也可以被安排进排班表。
     * @param peopleStatus
     * @param choice
     */
    private static void showPeople(Map<Employee, Boolean> peopleStatus, int choice) {
        if (choice == 0)
            System.out.print("未做安排员工：\n");
        else {
            System.out.print("已做安排员工：\n");
        }
        boolean status=choice==0? false:true;
        for (Employee E : peopleStatus.keySet()) {
            if (peopleStatus.get(E).equals(status)) {
                String s = E.getName() + "[" + E.getPosition() + "]" + "\n";
                System.out.print(s);
            }
        }
    }
    /**
     * 向员工表中添加一个新的员工。
     * 创建一个新的员工对象，并将其添加到员工表中。
     * <p>如果给定的名字包含数字，抛出 {@link IllegalArgumentException}。
     * <p>如果给定的电话号码包含非数字字符，抛出{@link IllegalArgumentException}。
     * <p>如果员工表中已经存在一个名字与给定名字相同的员工，抛出 {@link confictName}。
     *
     * @param name 新员工的名字，不能包含数字
     * @param position 新员工的职位
     * @param phoneNumber 新员工的电话号码，必须全为数字
     * @throws IllegalArgumentException 如果名字包含数字，或电话号码包含非数字字符
     * @throws confictName 员工表中已存在同名员工
     */
    private static void addEmployee(String name, String position, String phoneNumber) {
        if (!name.matches("[a-zA-Z]+")) {
            throw new IllegalArgumentException("姓名不能含有数字，请重新输入");
        }
        if (!position.matches("[a-zA-Z]+")) {
            throw new IllegalArgumentException("职务不能含有数字，请重新输入");
        }
        if (!phoneNumber.matches("[0-9]+")) {
            throw new IllegalArgumentException("电话号码必须全为数字，请重新输入");
        }
        for (Employee existingEmployee : PeopleWithStatus.keySet()) {
            if (existingEmployee.getName().equals(name)) {
                throw new confictName("已存在名为 " + name + " 的员工，请重新输入");
            }
        }
        Employee newEmployee = new Employee(name, position, phoneNumber);
        PeopleWithStatus.put(newEmployee, false);
    }

    /**
     * 从排班表删除指定名字的员工的所有排班,
     * 如果此员工不存在或未被安排进排班表中，表示删除失败
     */
    private static void removeEmployeeDuty() {
        showPeople(PeopleWithStatus, 1);
        System.out.println("输入要删除排班的员工姓名:");
        String dPName = input.next();
        Employee employee1 = null;
        boolean exist1 = false;
        for (Employee e : PeopleWithStatus.keySet()) {
            if (e.getName().equals(dPName) ) {
                exist1 = true;
                employee1 = e;
                break;
            }
        }
        if (!exist1) {
            System.err.print("此人不存在或未被安排进排班表中\n");
            return;
        }
        dutySet.remove(employee1);
        PeopleWithStatus.put(employee1, false);
        System.out.println("删除排班完成");
    }
    /**
     * 从员工表中删除指定名字的员工。
     * <p>员工表中查找名字为给定参数的员工，并将其从员工表中删除。如果找到的员工已经被编排进排班表，
     * 抛出一个 {@link wrongStatusException}，表示必须先删除员工的排班信息才能删除员工。
     *
     * <p>如果在员工表中没有找到名字为给定参数的员工，会抛出一个 {@link noExsistedException}。
     *
     * @param name 要删除的员工的名字
     * @throws wrongStatusException 如果员工已经被编排进排班表
     * @throws noExsistedException 如果没有找到名为给定参数的员工
     */
    private static void removeEmployee(String name) {
        Employee toRemove = null;
        for (Employee employee : PeopleWithStatus.keySet()) {
            if (employee.getName().equals(name)) {
                if (PeopleWithStatus.get(employee)) {
                    throw new wrongStatusException("员工已经被编排进排班表，必须先删除其排班信息才能删除员工");
                }
                toRemove = employee;
                break;
            }
        }
        if (toRemove != null) {
            PeopleWithStatus.remove(toRemove);
        } else {
            throw new noExsistedException("没有找到名为 " + name + " 的员工");
        }
    }

    /**
     * 安排一个员工到特定时间段进行值班。这个时间段的开始和结束时间必须以天为单位。
     * 可以给同一个员工通过多次追加值班时间段增扩时间，但是同一个员工的值班时间必须连续。
     * <p>没有找到这个员工，会抛出
     * {@link IllegalArgumentException}。
     * <p>检查新的值班时间段是否与这个员工已有的值班时间段连续。如果不连续，抛出
     * {@link IllegalArgumentException}。
     * <p>将新的值班时间段添加到值班表中。如果新的值班时间段与已有的值班时间段重叠，抛出
     * {@link NonlapException}。
     *
     * @param name 员工的名字
     * @param start 值班时间段的开始时间，以天为单位
     * @param end 值班时间段的结束时间，以天为单位
     * @throws IllegalArgumentException 如果没有找到名为给定参数的员工，或者新的值班时间段与这个员工已有的值班时间段不连续
     */
    private static void addDuty(String name, long start, long end) {
        Employee employee = null;
        //根据姓名找到对应人员
        for (Employee e : PeopleWithStatus.keySet()) {
            if (e.getName().equals(name)) {
                employee = e;
                break;
            }
        }
        if (employee == null) {
            throw new IllegalArgumentException("没有找到名为 " + name + " 的员工");
        }
        // 确定排班连续
        for (Employee e : dutySet.labels()) {
            if (e.equals(employee)) {
                long lastEnd = dutySet.end(e)+1;
                long lastStart= dutySet.start(e)-1;
                // 可以加到已有值班时间的连续前端或后端
                if (lastEnd != start&&lastStart!=end&&(!dutySet.labels().contains(e))) {
                    throw new IllegalArgumentException("员工 " + name + " 的排班不连续");
                }
            }
        }
        try {
            PeopleWithStatus.put(employee, true);
            dutySet.Insert(start,end,employee);
        } catch (NonlapException e) {
            System.err.println("新建的排班记录与已有重叠冲突" + e.getMessage());
        }
    }
    /**
     * 返回未排班时间段的list
     * @return 未排班时间段的List，每个元素为一个long[2]数组，表示一个时间段
     */
    private static List<long[]> getFreePeriod() {
        List<Employee> intervals = dutySet.sort();
        List<long[]> freePeriod=new ArrayList<>();
        double period=0;
        for(int i=0;i<=intervals.size();i++){
            if(i==0){
                //处理开始处的空隙
                if(dutySet.start(intervals.get(0))>dutySet.getStart()){
                    freePeriod.add(new long[]{dutySet.getStart(),dutySet.start(intervals.get(0))});
                    period+=dutySet.start(intervals.get(0))-dutySet.getStart();
                }
            } else if (i==intervals.size()) {
                //处理结束的空隙
                if(dutySet.getEnd()>dutySet.end(intervals.get(i-1))){
                    freePeriod.add(new long[]{dutySet.end(intervals.get(i-1)),dutySet.getEnd()});
                    period+=dutySet.getEnd()-dutySet.end(intervals.get(i-1));
                }
                break;
            }else{
                //处理中间的空隙
                if(dutySet.start(intervals.get(i))-1>dutySet.end(intervals.get(i-1))){
                    freePeriod.add(new long[]{dutySet.end(intervals.get(i-1)),dutySet.start(intervals.get(i))});
                    period+=dutySet.start(intervals.get(i))-1-dutySet.end(intervals.get(i-1));
                }
            }
        }
        String format=String.format("%.3f",period/(double) (dutySet.getEnd()- dutySet.getStart()+1));
        System.out.println("未排班时间占比为："+format);
        return freePeriod;
    }
    /**
     * 展示未排班时间段,用字符串形式输出
     */
    private static void showFreePeriod(){
        List<long[]> freePeriods=getFreePeriod();
        if(freePeriods.size()==0){
            System.out.println("排版完成，无未排班时间段\n");
            return;
        }
        System.out.println("未排班时间段还有:\n");
        for (long[] period:freePeriods){
            List<Integer> timeStart= longTrans(period[0]+1);
            List<Integer> timeEnd= longTrans(period[1]);
            System.out.println("["+timeStart.get(0)+"-"
                    +timeStart.get(1)+"-"+timeStart.get(2)+"] ~ ["+
                    timeEnd.get(0)+"-"
                    +timeEnd.get(1)+"-"+timeEnd.get(2)+"]\n");
        }
        System.out.println("请继续进行手动排班");
    }
    /**
     * 自动随机生成一个排班表；
     * 如果排班表不为空，则无法进行该操作；
     * 自动生成表后，无法再进行排班表的修改
     */
    private static void autoGenerateDutyset(){
        long start= dutySet.getStart();
        long end= dutySet.getEnd();
        Random rand = new Random();
        long length=end-start+1;
        int emplSize=PeopleWithStatus.size();
        long done=start-1;
        Employee now=null;
        for(Employee e:PeopleWithStatus.keySet()){
            now=e;
            int randomNum = rand.nextInt((int) (length/emplSize)*2+1);
            if(done-start+1+randomNum<=length){
                addDuty(e.getName(),done+1,done+randomNum);
                done+=randomNum;
                if(done==length)
                    return;
            }else{
                addDuty(e.getName(),done+1,end);
                return;
            }
        }
        addDuty(now.getName(),done+1,end);
    }

    /**
     * 进行手动排班
     */
    private static void manualDutyAssignment( ) {
        showPeople(PeopleWithStatus, 0);
        System.out.print("请输入人名：");
        String name = input.next();
        Employee employee = null;
        boolean exist = false;
        for (Employee e : PeopleWithStatus.keySet()) {
            if (e.getName().equals(name)) {
                exist = true;
                employee = e;
                break;
            }
        }
        if (!exist) {
            System.err.print("此人不存在或未被安排进排班表中\n");
            return;
        }
        System.out.println("输入该员工排班开始时间");
        long start1 = getTime();
        while (start1< dutySet.getStart()){
            System.err.print("开始时间不能早于排班表开始时间\n");
            System.out.println("请重新输入该员工排班开始时间");
            start1=getTime();
        }
        System.out.println("输入该员工排班结束时间");
        long end1 = getTime();
        if(end1> dutySet.getEnd()){
            System.err.print("结束时间不能晚于排班表结束时间\n");
            System.out.println("请重新输入该员工排班结束时间");
            end1 = getTime();
        }
        if (start1 > end1) {
            System.err.print("开始时间不能晚于结束时间\n");
            return;
        }
        addDuty(name, start1, end1);
        System.out.println("该次手动排班完成\n");
    }
    /**
     * 用于检查文件中时间信息是否符合格式,。若符合，将日期信息转换为long形式
     * 如果该时间格式不为yyyy-mm-dd，或信息不正确，程序直接退出
     * @param str checkfileparser中的一个字符子串，表示一个时间，需要为yyyy-mm-dd形式
     * @return {@link #transTime},用这个函数将日期信息转换为long形式
     */
    private static long checkLineParser(String str){
        String regex = "\\d{4}-\\d{2}-\\d{2}";
        if (!str.matches(regex)) {
            System.err.print("时间不为对应形式 yyyy-mm-dd\n");
            System.exit(0);
        }
        int month = Integer.parseInt(str.substring(5, 7));
        int day = Integer.parseInt(str.substring(8, 10));
        int year = Integer.parseInt(str.substring(0, 4));
        if (month <= 0 || month > 12) {
            System.err.print("不存在"+month+"月\n");
            System.exit(0);
        }
        if (year % 4 == 0) {
            if (day > dayOfMonth[0][month - 1] || day <= 0) {
                System.err.print(month+"月不能为"+day+"天\n");
                System.exit(0);
            }
        } else {
            if (day > dayOfMonth[1][month - 1] || day <= 0) {
                System.err.print(month+"月不能为"+day+"天\n");
                System.exit(0);
            }
        }
        return transTime(year, month, day);
    }

    /**
     * 根据指定文件生成排班表
     * @param index 文件序号
     */
    private static void readFile(int index) {
        String indexStr = String.valueOf(index);

        try {
            FileReader reader = new FileReader("src/text/text"+indexStr+".txt");
            BufferedReader bufferedReader = new BufferedReader(reader);

            String line="";
            while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains("Period{")) {
                        getLineInfo(line);
                        break;
                    }
            }
            reader.close();
            FileReader readerNew1 = new FileReader("src/text/text"+indexStr+".txt");
            BufferedReader bufferedReaderNew1 = new BufferedReader(readerNew1);
            String newLine1="";
            while ((newLine1 = bufferedReaderNew1.readLine()) != null) {
                        if(newLine1.contains("Employee{")){
                            while(!newLine1.matches("[\\s}]*")||newLine1.matches("\\s*")){
                                getLineInfo(newLine1);
                                newLine1=bufferedReaderNew1.readLine();
                            }
                            break;
                        }
            }
            reader.close();

            FileReader readerNew2 = new FileReader("src/text/text"+indexStr+".txt");
            BufferedReader bufferedReaderNew2 = new BufferedReader(readerNew2);
            String newLine2="";
            while((newLine2=bufferedReaderNew2.readLine())!=null){
                if(newLine2.contains("Roster{")){
                    while(!newLine2.matches("[\\s}]*")||newLine2.matches("\\s*")){
                        getLineInfo(newLine2);
                        newLine2=bufferedReaderNew2.readLine();
                    }
                    break;
                }
            }
            reader.close();

        } catch (IOException e) {
            System.err.print("文件打开失败\n");
            System.exit(0);
        }
    }

    /**
     * 根据文件中信息生成排班表，若文件格式错误或信息错误，进程直接结束；
     * <p>
     *     格式错误包括：1.时间不为yyyy-mm-dd 2.员工姓名中含有非字母符号或数字 3.员工电话号码不为数字
     * </p>
     * 信息错误包括：1.员工姓名重复 2.员工姓名在员工表中不存在，但试图将其添加到排班表
     * @param str 文件名
     */
    private static int  ifHave =0;
    private static void getLineInfo(String originStr) {
        String str=originStr.replaceAll("\\s","");
        long start;
        long end;
        if (str == null || str.trim().isEmpty()) {
            return;
        }
        // 取得时间信息
        if (str.startsWith("Period{")) {
            String time = str.substring(7, 17);
            start = checkLineParser(time);
            String next = str.substring(str.length() - 11, str.length() - 1);
            end = checkLineParser(next);
            if(ifHave==0) {
                dutySet = new DutyIntervalSet<>(start, end);
                ifHave=1;
            }
        } else if (str.startsWith("Employee{")) {

        } else if (str.startsWith("Roster{")) {

        } else if (str.startsWith("}")) {

        } else {
            // 设置日期区间正则规则
            String reg1 = "\\d{4}-\\d{2}-\\d{2},\\d{4}-\\d{2}-\\d{2}";
            Pattern pattern1 = Pattern.compile(reg1);
            Matcher matcher1 = pattern1.matcher(str);

            // 设置号码正则规则
            String reg2 = "\\d{3}-\\d{4}-\\d{4}";
            Pattern pattern2 = Pattern.compile(reg2);
            Matcher matcher2 = pattern2.matcher(str);
            // 匹配日期
            if (matcher1.find()) {
                //找日期区间格式
                String msg = matcher1.group();
                long startTime = checkLineParser(msg.substring(0, 10));
                long endTime = checkLineParser(msg.substring(11, 21));
                String reg3 = "[a-zA-Z]+";

                Pattern pattern3 = Pattern.compile(reg3);
                Matcher matcher3 = pattern3.matcher(str);
                if(matcher3.find()) {
                    String name = matcher3.group();
                    boolean flag = false;
                    for (Employee E : PeopleWithStatus.keySet()) {
                        // 在员工表中已经定义
                        if (E.getName().equals(name)) {
                            flag = true;
                            try {
                                dutySet.Insert(startTime, endTime, E);
                            } catch (NonlapException e) {
                                System.err.print("排班在" + E.getName() + "时出现重复\n");
                                System.exit(0);
                            }
                            PeopleWithStatus.put(E, true);
                            break;
                        }
                    }
                    if (!flag) {
                        System.err.print("没有名为“" + name + "”的员工\n");
                        System.exit(0);
                    }
                }
            } else if (matcher2.find()) {// 电话号码
                String msg1 = matcher2.group();
                String reg4 = "[a-zA-Z ]+";
                Pattern pattern4 = Pattern.compile(reg4);
                Matcher matcher4 = pattern4.matcher(str);
                String name = null, job = null;
                if (matcher4.find()) {// 员工姓名
                    name = matcher4.group();
                    if (str.charAt(1 + name.length()) != '{') {
                        System.err.print("人名中含有非字母的符号或数字\n");
                        System.exit(0);
                    }
                    for (Employee E : PeopleWithStatus.keySet()) {
                        if (E.getName().equals(name)) {
                            System.err.print("有重复人名\n");
                            System.exit(0);
                        }
                    }
                }
                if (matcher4.find()) {//姓名之后的职位
                    job = matcher4.group();
                }
                Employee newEmployee = new Employee(name, job, msg1);
                PeopleWithStatus.put(newEmployee, false);
            } else {
                System.err.print("非法的手机号\n");
                System.exit(0);
            }
        }
    }
    /**
     * 打印值班表
     */
    private static void printDutyRoster() {
        System.out.println(String.format("%-20s %-15s %-15s %-20s", "日期", "值班人姓名", "职位", "手机号码"));
        List<Employee> sortedList = dutySet.sort();
        for (Employee employee : sortedList) {
            long start = dutySet.start(employee);
            long end = dutySet.end(employee);
            for (long day = start; day <= end; day++) {
                List<Integer> dayList = longTrans(day);
                String date = String.format("%04d-%02d-%02d", dayList.get(0), dayList.get(1), dayList.get(2));
                System.out.println(String.format("%-20s %-15s %-15s %-20s", date, employee.getName(), employee.getPosition(), employee.getPhoneNum()));
            }
        }
    }

    /**
     * 用户选择获取数据方式为手动输入时，初次读入员工，初始化排班表，并调用running进行运行
     */
    private static void initialAndRun() {
        long start = 0;
        long end = 0;
        boolean flag = true;
        while (flag) {
            System.out.println("输入值班总开始时间(yyyy-mm-dd)");
            start = getTime();
            System.out.println("输入值班总结束时间(yyyy-mm-dd)");
            end = getTime();
            if (start <= end) {
                flag = false;
                makeWholeTimeLine(start, end); //初始化值班表时间轴
            } else
                System.err.println("值班开始时间应不大于结束时间，请重新输入\n");
        }
        int emplFlag = 0;
        while (emplFlag == 0) {
            try {
                System.out.print("请输入人名：");
                String name = input.next();
                System.out.print("请输入职务：");
                String position = input.next();
                System.out.print("请输入电话号码：");
                String phone = input.next();
                addEmployee(name, position, phone);
            } catch (confictName e) {
                System.err.println(e.getMessage());
                continue;
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
                continue;
            }
            System.out.println("是否结束？(Y/任意输入)");
            String choose = input.next();
            emplFlag = choose.equals("Y") ? 1 : 0;
        }
        running(start, end);
    }

    /**
     * 在用户选择数据来源、输入初始工作人员数据后，按照用户在菜单上选择的操作选项进行操作；
     * <p>
     * 操作选项包括：1.自动排班 2.手动排班 3.检查排班进度 4.添加新员工 5.删除员工 6.删除员工排班 7.展示排班 8.已排班员工 9.未排班员工
     * </p>
     * @param start  值班总时间段开始时间
     * @param end 值班总时间段结束时间
     */
    private static void running(long start,long end){
        do {
            int choice=menu();
            switch (choice) {
                case 1://自动排班
                    if (PeopleWithStatus.isEmpty()) {
                        System.out.println("无所需人员信息");
                        break;
                    }
                    if (!dutySet.labels().isEmpty()) {
                        System.err.println("已有排班，无法自动排班");
                        break;
                    }
                    autoGenerateDutyset();
                    System.out.println("自动排班完成\n");
                    break;
                case 2://手动排班
                    manualDutyAssignment();
                    break;
                case 3://检查排班进度
                    boolean check = dutySet.checkIfNonblank();
                    if (check) {
                        System.out.println("排班表已排满");
                    } else if (dutySet.labels().isEmpty()) {
                        System.out.println("还未开始排班");
                    } else {
                        showFreePeriod();
                    }
                    break;
                case 4://排班过程中添加新员工
                    int emplFlag1 = 0;
                    while (emplFlag1 == 0) {
                        try{
                            System.out.print("请输入人名：");
                            String name1 = input.next();
                            System.out.print("请输入职务：");
                            String position1 = input.next();
                            System.out.print("请输入电话号码：");
                            String phone1 = input.next();
                            addEmployee(name1,position1, phone1);
                        } catch (confictName e){
                            System.err.println(e.getMessage());
                            continue;
                        } catch (IllegalArgumentException e){
                            System.err.println(e.getMessage());
                            continue;
                        }
                        System.out.println("是否结束？(Y/任意输入)");
                        String choose = input.next();
                        emplFlag1 = choose.equals("Y") ? 1 : 0;
                    }
                    break;
                case 5://从员工表中删除员工
                    showPeople(PeopleWithStatus, 0);
                    System.out.println("输入要删除的员工姓名:");
                    String dName = input.next();
                    try {
                        removeEmployee(dName);
                    } catch (noExsistedException e) {
                        System.out.println("该员工不在员工库中");
                    } catch (wrongStatusException e) {
                        System.out.println("该员工在排班表中，无法删除");
                    }
                    break;
                case 6://删除员工排班
                    removeEmployeeDuty();
                    break;
                case 7://展示排班表
                    printDutyRoster();
                    break;
                case 8://已排班员工
                    showPeople(PeopleWithStatus, 1);
                    break;
                case 9://未排班员工
                    showPeople(PeopleWithStatus, 0);
                    break;
                default:
                    System.out.println("退出排班系统");
                    System.exit(0);
            }
        } while (true);
    }
    public static void main(String[] args) {

        System.out.println("****选择构建值班表的方式****");
        System.out.print("  1.手动录入\t2.文件读入\n   ");
        int ch = readNum();
        while (ch <= 0 || ch >= 3) {
            System.out.print("非法选择，请重新输入");
            ch = readNum();
        }
        if (ch == 2) {
            long start=0;
            long end=0;
            System.out.print("请输入文件名序号：");
            int text = readNum();
            while (text <= 0 || text >7) {//提供了7个文件
                System.out.print("不存在该文件，请重新输入");
                text = readNum();
            }
            readFile(text);
            System.out.println("读入文件成功！");
            running(start,end);//实际按照内类参数运行

        } else {
            initialAndRun();
        }
    }
}
