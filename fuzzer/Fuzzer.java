import java.io.*;
import java.util.*;

public class Fuzzer{
    private static final String OUTPUT_FILE = "fuzz.txt";
    private static final String PROPERTIES = "./state.properties";

    public static Random random = new Random();

    private static final Stack<String> operationStack = new Stack<>();
    private static final ArrayList<String> arrayList = new ArrayList<>();
    private static final ArrayList<String> instructionList = new ArrayList<>();

    private static final int MAX_LINE_LENGTH = (1022 - 1) + 1;
    private static final int MAX_INSTRUCTION_LENGTH = (1024 - 1) + 1;
    private static final int MAX_INSTRUCTIONS = (1024 - 1) + 1;
    private static final int MAX_INT_LENGTH = (11 - 1) + 1;
    private static final int MIN_INPUT = 1;
    private static final int PUSH_LOAD_SAVE_MAX_INPUT = (1017 - 1) + 1;
    private static final int STORE_MAX_INPUT = (1016 - 1) + 1;
    private static final int REMOVE_MAX_INPUT = (1015 - 1) + 1;
    private static final int STACK_CAPACITY = (512 - 1) + 1;
    private static final int CASES = 99;

    public enum STRING_TYPE{
        alphabets, numbers, special_Characters, alphanumeric, specialAlphanumeric
    }

    public enum INSTRUCTIONS{
        push, pop, load, list, remove, store, print, save
    }

    public enum OPERATORS{
        plus("+"),
        sub("-"),
        mul("*"),
        div("/");

        private final String opcode;

        OPERATORS(String opcode) {
            this.opcode = opcode;
        }

        public String getOpcode(){
            return opcode;
        }
    }

    public enum INSTRUCTION_TYPE{
        instructions, operators
    }

    public static void main(String[] args) throws IOException {
        System.out.println(Instruction.getBNF());
        FileOutputStream out = null;
        PrintWriter pw = null;
        try {
            out = new FileOutputStream(OUTPUT_FILE);
            pw = new PrintWriter(out);

            int state = getValue(PROPERTIES, "state");
            System.out.println(state);
            int round = state % CASES;
            String input_String = null;

            
            if(round <= 1){
                if(round == 1){
                    String variableName = characters(STRING_TYPE.specialAlphanumeric.toString(), random.nextInt(PUSH_LOAD_SAVE_MAX_INPUT - 1) + 1);

                    pw.println(INSTRUCTIONS.push.toString() + " " + random.nextInt(Integer.MAX_VALUE));
                    pw.println(INSTRUCTIONS.store.toString() + " " + variableName);
                    pw.println(INSTRUCTIONS.load.toString() + variableName);
                    pw.println(INSTRUCTIONS.remove.toString() + variableName);
                    pw.println(INSTRUCTIONS.list.toString());
                }
                String instruction;
                String value_Type;
                int value_Size;
                int total = random.nextInt(MAX_INSTRUCTIONS - 6);
                int list_Index = random.nextInt(total);
                for(int i = 0; i < total; i++){
                    int x = random.nextInt(2) + 1;
                    if(list_Index == i)
                        pw.println(INSTRUCTIONS.list.toString());
                    else {
                        if(x == 1 && round == 1){
                            instruction = INSTRUCTIONS.load.toString();
                            value_Size = random.nextInt(PUSH_LOAD_SAVE_MAX_INPUT);
                        } else {
                            instruction = INSTRUCTIONS.remove.toString();
                            value_Size = random.nextInt(REMOVE_MAX_INPUT);
                        }
                        value_Type = randomEnumValue("STRING_TYPE");
                        pw.println(generate_Instructions(instruction, value_Type, value_Size, false));
                    }
                }
                if(round == 0) pw.println(INSTRUCTIONS.load.toString());
            }
            else if(round == 2){ }//Empty File
            else if(round == 3){
                for(int i = 0; i < MAX_LINE_LENGTH - 8; i++){
                    pw.println(INSTRUCTIONS.print.toString());
                }
                pw.println(INSTRUCTIONS.push.toString() + " " + Integer.MAX_VALUE + "\n" + INSTRUCTIONS.push.toString() + " " + Integer.MIN_VALUE);
                pw.println(INSTRUCTIONS.store.toString() + " " + characters(STRING_TYPE.specialAlphanumeric.toString(), STORE_MAX_INPUT)
                        + "\n" + INSTRUCTIONS.store.toString() + " " + characters(randomEnumValue("STRING_TYPE"), 1));
                pw.println(INSTRUCTIONS.load.toString() + " " + characters(STRING_TYPE.specialAlphanumeric.toString(), PUSH_LOAD_SAVE_MAX_INPUT)
                        + "\n" + INSTRUCTIONS.load.toString() + " " + characters(randomEnumValue("STRING_TYPE"), 1));
                pw.println(INSTRUCTIONS.remove.toString() + " " + characters(STRING_TYPE.specialAlphanumeric.toString(), REMOVE_MAX_INPUT)
                        + "\n" + INSTRUCTIONS.remove.toString() + " " + characters(randomEnumValue("STRING_TYPE"), 1));
            }
            else if(round == 4){ //1 Line Instruction
                pw.println(INSTRUCTIONS.print.toString());
            }
            else if(round == 5 || round == 6 || round == 19){ //1024 Lines of Instruction
                for (int i = 0; i < MAX_INSTRUCTION_LENGTH; i++)
                    pw.println(INSTRUCTIONS.print.toString());
            }
            else if(round == 7){ // 1024 lines of Push + store
                int min = 0, max = Integer.MAX_VALUE;
                for(int i = 0; i < STACK_CAPACITY; i++){
                    pw.println(INSTRUCTIONS.push.toString() + " " + random.nextInt(max - min) + min);
                }
                for(int i = 0; i < STACK_CAPACITY; i++){
                    pw.println(generate_Instructions(INSTRUCTIONS.store.toString(), STRING_TYPE.specialAlphanumeric.toString(), random.nextInt(STORE_MAX_INPUT), true));
                }
            }
            else if(round == 8){ //1024 lines of push
                for(int i = 0; i < MAX_INSTRUCTIONS; i++) pw.println(generate_Instructions(INSTRUCTIONS.push.toString(), STRING_TYPE.numbers.toString(), random.nextInt(PUSH_LOAD_SAVE_MAX_INPUT), true));
            }
            else if(round == 9){ //1024 lines of pop
                for(int i = 0; i < MAX_INSTRUCTIONS; i++) pw.println(generate_Instructions(INSTRUCTIONS.pop.toString(), "", 0, true));
            }
            else if(round == 10){ //1024 lines of load
                for(int i = 0; i < MAX_INSTRUCTIONS; i++) pw.println(generate_Instructions(INSTRUCTIONS.load.toString(), STRING_TYPE.specialAlphanumeric.toString(), random.nextInt(PUSH_LOAD_SAVE_MAX_INPUT), true));
            }
            else if(round == 11){ //1024 lines of remove
                for(int i = 0; i < MAX_INSTRUCTIONS; i++) pw.println(generate_Instructions(INSTRUCTIONS.remove.toString(), STRING_TYPE.specialAlphanumeric.toString(), random.nextInt(REMOVE_MAX_INPUT), true));
            }
            else if(round == 12){ //1024 lines of store
                for(int i = 0; i < MAX_INSTRUCTIONS; i++) pw.println(generate_Instructions(INSTRUCTIONS.store.toString(), STRING_TYPE.specialAlphanumeric.toString(), random.nextInt(STORE_MAX_INPUT), true));
            }
            else if(round == 13){ //1024 lines of save
                for(int i = 0; i < MAX_INSTRUCTIONS; i++) pw.println(generate_Instructions(INSTRUCTIONS.save.toString(), STRING_TYPE.specialAlphanumeric.toString(), random.nextInt(PUSH_LOAD_SAVE_MAX_INPUT), true));
            }
            else if(round == 14){ //1024 lines of list
                for(int i = 0; i < MAX_INSTRUCTIONS; i++) pw.println(generate_Instructions(INSTRUCTIONS.list.toString(), "", 0, true));
            }
            else if(round == 15){ //1024 lines of print
                for(int i = 0; i < MAX_INSTRUCTIONS; i++) pw.println(generate_Instructions(INSTRUCTIONS.print.toString(), "", 0, true));
            }
            else if(round == 16){ //remove, load, store same times
                instructionList.clear();
                for(int i = 0; i < MAX_INSTRUCTION_LENGTH / 3; i++){
                    instructionList.add(generate_Instructions(INSTRUCTIONS.load.toString(), STRING_TYPE.specialAlphanumeric.toString(), random.nextInt(PUSH_LOAD_SAVE_MAX_INPUT), true));
                    instructionList.add(generate_Instructions(INSTRUCTIONS.store.toString(), STRING_TYPE.specialAlphanumeric.toString(), random.nextInt(STORE_MAX_INPUT), true));
                    instructionList.add(generate_Instructions(INSTRUCTIONS.remove.toString(), STRING_TYPE.specialAlphanumeric.toString(), random.nextInt(REMOVE_MAX_INPUT), true));
                }
                Collections.shuffle(instructionList);

                for(String s : instructionList) pw.println(s);

                //invalid remove with more than 1 argument
                String variableName = "";
                for(int i = 0; i < 600; i++){
                    variableName += " x";
                }
                pw.println(INSTRUCTIONS.remove.toString() + variableName);
            }
            else if(round == 17){ // > 1024 lines of random instructions
                for(int i = 0; i < MAX_INSTRUCTIONS * 2; i++){
                    pw.println(generate_Instructions(INSTRUCTIONS.print.toString(), "", 0, true));
                }
            }
            else if(round == 18){ // instructions with numbers only
                instructionList.clear();
                instructionList.add(generate_Instructions(INSTRUCTIONS.load.toString(), STRING_TYPE.numbers.toString(), PUSH_LOAD_SAVE_MAX_INPUT, true));
                instructionList.add(generate_Instructions(INSTRUCTIONS.store.toString(), STRING_TYPE.numbers.toString(), STORE_MAX_INPUT, true));
                instructionList.add(generate_Instructions(INSTRUCTIONS.remove.toString(), STRING_TYPE.numbers.toString(), REMOVE_MAX_INPUT, true));
                Collections.shuffle(instructionList);
                for(String s : instructionList) pw.println(s);
            }
            else if(round == 20){ //1 invalid Instruction
                pw.println("avbcds/asasas.asas a b c d");
            }
            else if(round == 21){ //load with 2+ arguments
                String str = INSTRUCTIONS.load.toString();
                for(int i = 0; i < 600; i++) str += " x";
                pw.println(str);
            }
            else if(round == 22){ //list multiple arguments
                String str = INSTRUCTIONS.list.toString() + " ";
                for(int i = 0; i < 600; i++) str += " x";
                pw.print(str);
            }
            else if(round == 23){ // invalid: save min
                String str = INSTRUCTIONS.save.toString() + " " + STRING_TYPE.specialAlphanumeric.toString();
                pw.print(str);
            }
            else if(round == 24){ // invalid: save max
                String str = INSTRUCTIONS.save.toString() + " " +
                        characters(randomEnumValue("STRING_TYPE"), PUSH_LOAD_SAVE_MAX_INPUT);
                pw.print(str);
            }
            else if(round == 25){ //blank space
                pw.print(" ");
            }
            else if(round == 26){ //push with random string
                pw.print(INSTRUCTIONS.push.toString() + " a*b");
            }
            else if(round == 27){ //push with random number string with max int length
                pw.print(generate_Instructions(INSTRUCTIONS.push.toString(),
                        STRING_TYPE.numbers.toString(), MAX_INT_LENGTH,
                        false));
            }
            else if(round == 28){ //store empty stack
                pw.print(generate_Instructions(INSTRUCTIONS.store.toString(),
                        randomEnumValue("STRING_TYPE"), 3, true));
            }
            else if(round == 29){ //push one element and do operation
                pw.println(generate_Instructions(INSTRUCTIONS.push.toString(),
                        STRING_TYPE.numbers.toString(), random.nextInt(2 - 1) + 1,
                        true));
                pw.println(generate_Instructions(randomEnumValue("OPERATORS"),
                        "", 0, false));
            }
            else if(round == 30){ //pop empty stack
                pw.print(INSTRUCTIONS.pop.toString() + " ");
            }
            else if(round == 31){ //store empty stack
                pw.println(generate_Instructions(INSTRUCTIONS.store.toString(),
                        randomEnumValue("STRING_TYPE"), MIN_INPUT, true));
            }
            else if(round == 32){ //push
                pw.print(INSTRUCTIONS.push.toString().toUpperCase() + " " + random.nextInt(3));
            }
            else if(round == 33){ //pop
                pw.print(INSTRUCTIONS.pop.toString().toUpperCase());
            }
            else if(round == 34){ //store
                pw.print(INSTRUCTIONS.store.toString().toUpperCase() + " " + characters(STRING_TYPE.numbers.toString(),
                        MIN_INPUT));
            }
            else if(round == 35) { //load
                pw.print(INSTRUCTIONS.load.toString().toUpperCase() + " " + characters(STRING_TYPE.specialAlphanumeric.toString(),
                        MIN_INPUT));
            }
            else if(round == 36){ //save
                pw.print(INSTRUCTIONS.save.toString().toUpperCase() + " " + characters(STRING_TYPE.numbers.toString(),
                        MIN_INPUT));
            }
            else if(round == 37){ //push 2 numbers and store them seperately and load the variables together
                pw.println(generate_Instructions(INSTRUCTIONS.push.toString(),
                        STRING_TYPE.numbers.toString(), random.nextInt(2 - 1) + 1, true));
                pw.println(INSTRUCTIONS.store.toString() + " a");
                pw.println(generate_Instructions(INSTRUCTIONS.push.toString(),
                        STRING_TYPE.numbers.toString(), random.nextInt(2 - 1) + 1, true));
                pw.println(INSTRUCTIONS.store.toString() + " b");
                pw.println(INSTRUCTIONS.load.toString() + " a b");
            }
            else if(round == 38){ // Store with invalid 1+ arguments
                String str = INSTRUCTIONS.store.toString();
                for(int i = 0; i < 300; i++) str += " x";
                pw.println(str);
            }
            else if(round == 39){ // + with invalid 0+ arguments
                String str = OPERATORS.plus.getOpcode();
                for(int i = 0; i < 300; i++)
                    str += " +";
                pw.println(str);
            }
            else if(round == 40){ // - with invalid 0+ arguments
                String str = OPERATORS.sub.getOpcode();
                for(int i = 0; i < 300; i++)
                    str += " -";
                pw.println(str);
            }
            else if(round == 41){ // * with invalid 0+ arguments
                String str = OPERATORS.mul.getOpcode();
                for(int i = 0; i < 300; i++)
                    str += " *";
                pw.println(str);
            }
            else if(round == 42){ // ? with invalid 0+ arguments
                String str = OPERATORS.div.getOpcode();
                for(int i = 0; i < 300; i++)
                    str += " /";
                pw.println(str);
            }
            else if(round == 43) { // remove with invalid 0+ arguments
                String str = INSTRUCTIONS.remove.toString();
                for(int i = 0; i < 300; i++) str += " x";
                pw.println(str);
            }
            else if(round == 44) { // Store and load from same variable
                String variableName = characters(STRING_TYPE.alphabets.toString(), random.nextInt(100));
                pw.println(INSTRUCTIONS.push.toString() + " " + random.nextInt(PUSH_LOAD_SAVE_MAX_INPUT));
                pw.println(INSTRUCTIONS.store.toString() + " " + variableName);
                pw.println(INSTRUCTIONS.load.toString() + " " + variableName);
            }
            else if(round == 45){ // Push store and list multiple times
                instructionList.clear();
                for(int i = 0; i < STACK_CAPACITY - 2; i++){
                    pw.println(generate_Instructions(INSTRUCTIONS.push.toString(),
                            STRING_TYPE.numbers.toString(), random.nextInt(PUSH_LOAD_SAVE_MAX_INPUT), true));
                }
                for(int i = 0; i < STACK_CAPACITY - 2; i++){
                    pw.println(generate_Instructions(INSTRUCTIONS.store.toString(), randomEnumValue("STRING_TYPE"), random.nextInt(STORE_MAX_INPUT), true));
                }
                pw.println(INSTRUCTIONS.list.toString());
            }
            else if(round == 46){ //Push with invalid 0+ arguments
                String str = INSTRUCTIONS.push.toString();
                String num = characters(STRING_TYPE.numbers.toString(), MIN_INPUT);
                for(int i = 0; i < 300; i++)
                    str += " " + num;
                pw.println(str);
            }
            else if(round == 47){ //list with invalid 0+ arguments
                String str = INSTRUCTIONS.list.toString();
                String num = characters(STRING_TYPE.alphabets.toString(), MIN_INPUT);
                for(int i = 0; i < 300; i++)
                    str += " " + num;
                pw.println(str);
            }
            else if(round == 48){ //print with invalid 0+ arguments
                String str = INSTRUCTIONS.print.toString();
                String num = characters(STRING_TYPE.alphabets.toString(), MIN_INPUT);
                for(int i = 0; i < 300; i++)
                    str += " " + num;
                pw.println(str);
            }
            else if(round == 49){ //Push store and list multiple times
                String variableName = characters(STRING_TYPE.alphabets.toString(), random.nextInt(100));
                instructionList.clear();
                for(int i = 0; i < STACK_CAPACITY - 2; i++){
                    pw.println(generate_Instructions(INSTRUCTIONS.push.toString(),
                            STRING_TYPE.numbers.toString(), random.nextInt(PUSH_LOAD_SAVE_MAX_INPUT), true));
                }
                for(int i = 0; i < STACK_CAPACITY - 2; i++){
                    pw.println(INSTRUCTIONS.store.toString() + " " + variableName);
                }
            }
            else if(round == 50){ //Push store and remove multiple times
                String variableName = characters(STRING_TYPE.alphabets.toString(), random.nextInt(100));
                instructionList.clear();
                for(int i = 0; i < STACK_CAPACITY - 2; i++){
                    pw.println(generate_Instructions(INSTRUCTIONS.push.toString(),
                            STRING_TYPE.numbers.toString(), random.nextInt(PUSH_LOAD_SAVE_MAX_INPUT), true));
                }
                for(int i = 0; i < STACK_CAPACITY - 2; i++){
                    pw.println(INSTRUCTIONS.store.toString() + " " + variableName);
                }
                pw.println(INSTRUCTIONS.remove.toString() + " " + variableName);
            }
            else if(round == 51){ //Push and print from stack randomly
                instructionList.clear();
                for(int i = 0; i < STACK_CAPACITY - 2 ; i++){
                    instructionList.add(generate_Instructions(INSTRUCTIONS.push.toString(), STRING_TYPE.numbers.toString(),
                            random.nextInt(PUSH_LOAD_SAVE_MAX_INPUT), true));
                    instructionList.add(generate_Instructions(INSTRUCTIONS.print.toString(),
                            "", 0, false));
                }
                Collections.shuffle(instructionList);
                for(String s : instructionList) pw.println(s);
            }
            else if(round == 52){ //Push to full stack
                for(int i = 0; i <= STACK_CAPACITY; i++) {
                    if(i == STACK_CAPACITY)
                        pw.println(generate_Instructions(INSTRUCTIONS.push.toString(),
                                STRING_TYPE.numbers.toString(), PUSH_LOAD_SAVE_MAX_INPUT, true));
                    else pw.println(generate_Instructions(INSTRUCTIONS.push.toString(),
                            STRING_TYPE.numbers.toString(), random.nextInt(PUSH_LOAD_SAVE_MAX_INPUT), true));
                }
            }
            else if(round == 53){ //perform addition randomly
                for(int i = 0; i < STACK_CAPACITY - 1; i++) {
                    pw.println(generate_Instructions(INSTRUCTIONS.push.toString(),
                            STRING_TYPE.numbers.toString(), random.nextInt(PUSH_LOAD_SAVE_MAX_INPUT), true));
                }
                for(int i = 0; i < (STACK_CAPACITY - 1) / 3; i++) {
                    pw.println(OPERATORS.plus.getOpcode());
                }
            }
            else if(round == 54){ //perform subtraction randomly
                for(int i = 0; i < STACK_CAPACITY - 1; i++) {
                    pw.println(generate_Instructions(INSTRUCTIONS.push.toString(),
                            STRING_TYPE.numbers.toString(), random.nextInt(PUSH_LOAD_SAVE_MAX_INPUT), true));
                }
                for(int i = 0; i < (STACK_CAPACITY - 1) / 3; i++) {
                    pw.println(OPERATORS.sub.getOpcode());
                }
            }
            else if(round == 55){ //perform multiply randomly
                for(int i = 0; i < STACK_CAPACITY - 1; i++) {
                    pw.println(generate_Instructions(INSTRUCTIONS.push.toString(),
                            STRING_TYPE.numbers.toString(), random.nextInt(PUSH_LOAD_SAVE_MAX_INPUT), true));
                }
                for(int i = 0; i < (STACK_CAPACITY - 1) / 3; i++) {
                    pw.println(OPERATORS.mul.getOpcode());
                }
            }
            else if(round == 56){ //perform divide randomly
                for(int i = 0; i < STACK_CAPACITY - 1; i++) {
                    pw.println(generate_Instructions(INSTRUCTIONS.push.toString(),
                            STRING_TYPE.numbers.toString(), random.nextInt(PUSH_LOAD_SAVE_MAX_INPUT), true));
                }
                for(int i = 0; i < (STACK_CAPACITY - 1) / 3; i++) {
                    pw.println(OPERATORS.div.getOpcode());
                }
            }
            else if(round == 57){ // + from insufficient stack
                for(int i = 0; i < MAX_LINE_LENGTH; i++)
                    pw.println(OPERATORS.plus.getOpcode());
            }
            else if(round == 58){ // - from insufficient stack
                for(int i = 0; i < MAX_LINE_LENGTH; i++)
                    pw.println(OPERATORS.sub.getOpcode());
            }
            else if(round == 59){ // * from insufficient stack
                for(int i = 0; i < MAX_LINE_LENGTH; i++)
                    pw.println(OPERATORS.mul.getOpcode());
            }
            else if(round == 60){ // / from insufficient stack
                for(int i = 0; i < MAX_LINE_LENGTH; i++)
                    pw.println(OPERATORS.div.getOpcode());
            }
            else if(round == 61){ //divide by 0
                pw.println(INSTRUCTIONS.push.toString() + " 0");
                pw.println(generate_Instructions(INSTRUCTIONS.push.toString(),
                        STRING_TYPE.numbers.toString(), random.nextInt(2 - 1) + 1, true));
                pw.println(OPERATORS.div.getOpcode());
            }
            else if(round > 61 && round <= 70){ //Random Valid Instructions with only numbers
                int run = random.nextInt(MAX_INSTRUCTIONS - 1) + 1;
                for(int i = 0; i < run; i++) {
                    input_String = generate_Random_Valid_Instruction();
                    if (input_String != null) {
                        pw.println(input_String);
                    }
                }
            }
            else if(round > 70 && round <= 80){ //Random Instructions with only alphabets
                String value_Type = STRING_TYPE.alphabets.toString();
                int run = random.nextInt(MAX_INSTRUCTIONS - 1) + 1;
                for(int i = 0; i < run; i++) {
                    input_String = generate_Random_Invalid_Instruction(value_Type);
                    if (input_String != null) {
                        pw.println(input_String);
                    }
                }
            }
            else if(round > 80 && round <= 90){ //Random Instructions with only alphabets and number
                String value_Type = STRING_TYPE.alphabets.toString();
                int run = random.nextInt(MAX_INSTRUCTIONS - 1) + 1;
                for(int i = 0; i < run; i++) {
                    input_String = generate_Random_Invalid_Instruction(value_Type);
                    if (input_String != null) {
                        pw.println(input_String);
                    }
                }
            }
            else if(round > 90){ //Random Instructions with alphabets, number, special characters
                String value_Type = STRING_TYPE.specialAlphanumeric.toString();
                int run = random.nextInt(MAX_INSTRUCTIONS - 1) + 1;
                for(int i = 0; i < run; i++) {
                    input_String = generate_Random_Invalid_Instruction(value_Type);
                    if (input_String != null) {
                        pw.println(input_String);
                    }
                }
            }

            writeProperty(PROPERTIES, "state", round + 1);
        }catch (Exception e){
            e.printStackTrace(System.err);
            System.exit(1);
        }finally{
            if (pw != null){
                pw.flush();
            }
            if (out != null){
                out.close();
            }
        }
    }

    public static int getValue(String filePath, String key) {
        Properties p = new Properties();

        try {
            InputStream in = new BufferedInputStream(new FileInputStream(filePath));
            p.load(in);
            String value = p.getProperty(key);

            return Integer.parseInt(value);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static void writeProperty(String filePath, String key, int value) {
        Properties p = new Properties();

        try {
            OutputStream out = new FileOutputStream(filePath);
            p.setProperty(key, Integer.toString(value));
            p.store(out, "");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String characters(String INSTRUCTION_TYPE, long size){
        int alphabet_Begin = 65;
        int alphabet_End = 122;
        int number_Begin = 48;
        int number_End = 57;
        int special_Begin = 33;
        int special_End = 126;

        if(size > 0) {

            if (INSTRUCTION_TYPE.equals("alphabets")) return random.ints(alphabet_Begin, alphabet_End + 1)
                    .filter(i -> ((i >= 65 && i <= 90) || (i >= 97 && i <= 122)))
                    .limit(size)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();

            if (INSTRUCTION_TYPE.equals("numbers")) return random.ints(number_Begin, number_End + 1)
                    .limit(size)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();

            if (INSTRUCTION_TYPE.equals("special_Characters")) return random.ints(special_Begin, special_End + 1)
                    .filter(i -> ((i >= 33 && i <= 47) || (i >= 58 && i <= 64) || (i >= 91 && i <= 96) || (i >= 123 && i <= 126)))
                    .limit(size)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();

            if (INSTRUCTION_TYPE.equals("alphanumeric")) return random.ints(number_Begin, alphabet_End + 1)
                    .filter(i -> ((i >= 48 && i <= 57) || (i >= 65 && i <= 90) || (i >= 97 && i <= 122)))
                    .limit(size)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();

            if (INSTRUCTION_TYPE.equals("specialAlphanumeric")) return random.ints(special_Begin, special_End + 1)
                    .limit(size)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
        }
        return "";
    }

    public static String randomEnumValue(String enumName){
        if(enumName.equalsIgnoreCase("INSTRUCTIONS")) return INSTRUCTIONS.class.getEnumConstants()[random.nextInt(8)].toString();
        if(enumName.equalsIgnoreCase("STRING_TYPE")) return STRING_TYPE.class.getEnumConstants()[random.nextInt(5)].toString();
        if(enumName.equalsIgnoreCase("OPERATORS")) return OPERATORS.class.getEnumConstants()[random.nextInt(4)].getOpcode();
        if(enumName.equalsIgnoreCase("INSTRUCTION_TYPE")) return INSTRUCTION_TYPE.class.getEnumConstants()[random.nextInt(2)].toString();
        return "";
    }

    public static String generate_Instructions(String Instruction, String value_Type, int value_Size, boolean correctFormat) {
        if(!correctFormat) return Instruction + " " + characters(value_Type, value_Size);

        if(Instruction.equalsIgnoreCase(INSTRUCTIONS.push.toString())) return Instruction + " " + characters(value_Type, value_Size);
        if(Instruction.equalsIgnoreCase(INSTRUCTIONS.pop.toString())) return Instruction;
        if(Instruction.equalsIgnoreCase(INSTRUCTIONS.load.toString())) return Instruction + " " + characters(value_Type, value_Size);
        if(Instruction.equalsIgnoreCase(INSTRUCTIONS.store.toString())) return Instruction + " " + characters(value_Type, value_Size);
        if(Instruction.equalsIgnoreCase(INSTRUCTIONS.remove.toString())) return Instruction + " " + characters(value_Type, value_Size);
        if(Instruction.equalsIgnoreCase(INSTRUCTIONS.list.toString())) return Instruction;
        if(Instruction.equalsIgnoreCase(INSTRUCTIONS.print.toString())) return Instruction;
        if(Instruction.equalsIgnoreCase(INSTRUCTIONS.save.toString())) return Instruction + " " + characters(value_Type, value_Size);
        return "";
    }

    public static String generate_Random_Valid_Instruction() {
        String INSTRUCTION_TYPE = randomEnumValue("INSTRUCTION_TYPE");
        String instruction = randomEnumValue("INSTRUCTIONS");
        String input_String = null;
        if(INSTRUCTION_TYPE.equalsIgnoreCase("instructions")){
            if(instruction.equalsIgnoreCase("push")){
                String value = characters(STRING_TYPE.numbers.toString(), random.nextInt(MAX_INT_LENGTH - 1) + 1);
                input_String = instruction + " " + value;
                operationStack.push(value);
            } else if(instruction.equalsIgnoreCase("pop")){
                if(!operationStack.isEmpty()){
                    input_String = instruction;
                    operationStack.pop();
                } else generate_Random_Valid_Instruction();
            } else if(instruction.equalsIgnoreCase("store")){
                if(!operationStack.isEmpty()){
                    String variableName = characters(randomEnumValue("STRING_TYPE"), random.nextInt(STORE_MAX_INPUT - 1) + 1);
                    String str = variableName + " " + operationStack.pop();
                    input_String = instruction + " " + variableName;
                    arrayList.add(str);
                } else generate_Random_Valid_Instruction();
            } else if(instruction.equalsIgnoreCase("load")){
                if(!arrayList.isEmpty()){
                    int random_Index = random.nextInt(arrayList.size());
                    String[] loadString = arrayList.get(random_Index).split("\\s");
                    input_String = instruction + " " + loadString[0];
                    operationStack.push(loadString[1]);
                } else generate_Random_Valid_Instruction();
            } else if(instruction.equalsIgnoreCase("remove")){
                if(!arrayList.isEmpty()) {
                    int random_Index = random.nextInt(arrayList.size());
                    String[] removeString = arrayList.get(random_Index).split("\\s");
                    input_String = instruction + " " + removeString[0];
                    arrayList.remove(random_Index);
                } else generate_Random_Valid_Instruction();
            } else if(instruction.equalsIgnoreCase("list")) input_String = instruction;
            else if(instruction.equalsIgnoreCase("print")) input_String = instruction;
            else if(instruction.equalsIgnoreCase("save")) {
                String variableName = characters(randomEnumValue("STRING_TYPE"), random.nextInt(PUSH_LOAD_SAVE_MAX_INPUT));
                input_String = instruction + " " + variableName;
            }

        } else if(INSTRUCTION_TYPE.equalsIgnoreCase("operators")){
            if(operationStack.size() > 1){
                String operator = randomEnumValue("OPERATORS");
                if(operator.equalsIgnoreCase("+")){
                    long a = Long.parseLong(operationStack.pop());
                    long b = Long.parseLong(operationStack.pop());
                    operationStack.push(String.valueOf(a + b));
                    input_String = operator;
                } else if(operator.equalsIgnoreCase("-")){
                    long a = Long.parseLong(operationStack.pop());
                    long b = Long.parseLong(operationStack.pop());
                    operationStack.push(String.valueOf(a - b));
                    input_String = operator;
                } else if(operator.equalsIgnoreCase("*")){
                    long a = Long.parseLong(operationStack.pop());
                    long b = Long.parseLong(operationStack.pop());
                    operationStack.push(String.valueOf(a * b));
                    input_String = operator;
                } else if(operator.equalsIgnoreCase("/")){
                    long a = Long.parseLong(operationStack.pop());
                    long b = Long.parseLong(operationStack.pop());
                    if (b != 0) operationStack.push(String.valueOf(a / b));
                    input_String = operator;
                }
            }
        }
        return input_String;
    }

    public static String generate_Random_Invalid_Instruction(String value_Type) {
        String INSTRUCTION_TYPE = randomEnumValue("INSTRUCTION_TYPE");
        String instruction = randomEnumValue("INSTRUCTIONS");
        String input_String = null;
        if(INSTRUCTION_TYPE.equalsIgnoreCase("instructions")){
            if(instruction.equalsIgnoreCase("push")){
                String value = characters(STRING_TYPE.numbers.toString(), random.nextInt(MAX_INT_LENGTH - 1) + 1);
                input_String = instruction + " " + value;
                operationStack.push(value);
            } else if(instruction.equalsIgnoreCase("pop")){
                if(!operationStack.isEmpty()){
                    input_String = instruction;
                    operationStack.pop();
                } else generate_Random_Invalid_Instruction(value_Type);
            } else if(instruction.equalsIgnoreCase("store")){
                if(!operationStack.isEmpty()){
                    String variableName = characters(randomEnumValue("STRING_TYPE"), random.nextInt(STORE_MAX_INPUT - 1) + 1);
                    String str = variableName + " " + operationStack.pop();
                    input_String = instruction + " " + variableName;
                    arrayList.add(str);
                } else generate_Random_Invalid_Instruction(value_Type);
            } else if(instruction.equalsIgnoreCase("load")){
                if(!arrayList.isEmpty()){
                    int random_Index = random.nextInt(arrayList.size());
                    String[] loadString = arrayList.get(random_Index).split("\\s");
                    input_String = instruction + " " + loadString[0];
                    operationStack.push(loadString[1]);
                } else generate_Random_Invalid_Instruction(value_Type);
            } else if(instruction.equalsIgnoreCase("remove")){
                if(!arrayList.isEmpty()) {
                    int random_Index = random.nextInt(arrayList.size());
                    String[] removeString = arrayList.get(random_Index).split("\\s");
                    input_String = instruction + " " + removeString[0];
                    arrayList.remove(random_Index);
                } else generate_Random_Invalid_Instruction(value_Type);
            } else if(instruction.equalsIgnoreCase("list")) input_String = instruction;
            else if(instruction.equalsIgnoreCase("print")) input_String = instruction;
            else if(instruction.equalsIgnoreCase("save")) {
                String variableName = characters(randomEnumValue("STRING_TYPE"), random.nextInt(PUSH_LOAD_SAVE_MAX_INPUT));
                input_String = instruction + " " + variableName;
            }

        } else if(INSTRUCTION_TYPE.equalsIgnoreCase("operators")){
            if(operationStack.size() > 1){
                String operator = randomEnumValue("OPERATORS");
                if(operator.equalsIgnoreCase("+")){
                    input_String = operator;
                } else if(operator.equalsIgnoreCase("-")){
                    input_String = operator;
                } else if(operator.equalsIgnoreCase("*")){
                    input_String = operator;
                } else if(operator.equalsIgnoreCase("/")){
                    input_String = operator;
                }
            }
        }
        return input_String;
    }
}