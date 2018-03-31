package mod.sin.wyvern.mastercraft;

import javassist.bytecode.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;


public class BytecodeTools extends Bytecode {

    //private static final int EMPTY_INT = Integer.MAX_VALUE;
    
    BytecodeTools(ConstPool constPool){
        super(constPool);
    }

    /**
     * Look for a class in the constant pool.
     * Throws NoMatchingConstPoolIndexException if the references isn't found.
     *
     * @param className String Object type. Full class name using periods.
     * @return int primitive, index in constant pool table.
     */
    int findClassIndex(String className){
        int classReferenceIndex = getClassReferenceIndex(className);
        if (classReferenceIndex == -1)
            throw new RuntimeException("No matching class found.");
        this.add((classReferenceIndex >>> 8) & 0xFF, classReferenceIndex & 0xFF);
        return classReferenceIndex;
    }

    int addClassIndex(String className){
        int classReferenceIndex = getClassReferenceIndex(className);
        if (classReferenceIndex == -1)
            classReferenceIndex = this.getConstPool().addClassInfo(className);
        this.add((classReferenceIndex >>> 8) & 0xFF, classReferenceIndex & 0xFF);
        return classReferenceIndex;
    }

    int findMethodIndex(int opcode, String name, String descriptor, String className){
        int methodReferenceIndex;
        int classReferenceIndex = getClassReferenceIndex(className);
        if (classReferenceIndex == -1)
            throw new RuntimeException("No matching class found.");

        methodReferenceIndex = IntStream.range(1, this.getConstPool().getSize())
                .filter(value -> this.getConstPool().getTag(value) == ConstPool.CONST_Methodref)
                .filter(value -> this.getConstPool().eqMember(name, descriptor, value) != null)
                .filter(value -> Objects.equals(this.getConstPool().getMethodrefClass(value), classReferenceIndex))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No matching method found."));
        this.addOpcode(opcode);
        this.add((methodReferenceIndex >>> 8) & 0xFF, methodReferenceIndex & 0xFF);

        return methodReferenceIndex;
    }

    int addMethodIndex(int opcode, String name, String methodDescriptor, String className){
        int classReferenceIndex = getClassReferenceIndex(className);
        if (classReferenceIndex == -1)
            classReferenceIndex = this.getConstPool().addClassInfo(className);
        int indexReference = this.getConstPool().addMethodrefInfo(classReferenceIndex, name, methodDescriptor);
        this.addOpcode(opcode);
        this.add((indexReference >>> 8) & 0xFF, indexReference & 0xFF);
        return indexReference;
    }

    int findFieldIndex(int opcode, String name, String descriptor, String className){
        int fieldReferenceIndex;
        int classReferenceIndex = getClassReferenceIndex(className);
        if (classReferenceIndex == -1)
            throw new RuntimeException("No matching class found.");
        fieldReferenceIndex = IntStream.range(1, this.getConstPool().getSize())
                .filter(value -> this.getConstPool().getTag(value) == ConstPool.CONST_Fieldref)
                .filter(value -> this.getConstPool().eqMember(name, descriptor, value) != null)
                .filter(value -> this.getConstPool().getFieldrefClass(value) == classReferenceIndex)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No matching field found."));
        this.addOpcode(opcode);
        this.add((fieldReferenceIndex >>> 8) & 0xFF, fieldReferenceIndex & 0xFF);
        return fieldReferenceIndex;
    }

    int addFieldIndex(int opcode, String name, String descriptor, String className){
        int classReferenceIndex = getClassReferenceIndex(className);
        if (classReferenceIndex == -1)
            classReferenceIndex = this.getConstPool().addClassInfo(className);
        int fieldReferenceIndex = this.getConstPool().addFieldrefInfo(classReferenceIndex, name, descriptor);
        this.addOpcode(opcode);
        this.add((fieldReferenceIndex >>> 8) & 0xFF, fieldReferenceIndex & 0xFF);
        return fieldReferenceIndex;
    }

    void findStringIndex(String string){
        int indexReference = IntStream.range(1, this.getConstPool().getSize())
                .filter(value -> this.getConstPool().getTag(value) == ConstPool.CONST_String)
                .filter(value -> Objects.equals(this.getConstPool().getStringInfo(value), string))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No matching string found."));
        this.add((indexReference >>> 8) & 0xFF, indexReference & 0xFF);
    }

    void addStringIndex(String string){
        int indexReference = this.getConstPool().addStringInfo(string);
        this.add((indexReference >>> 8) & 0xFF, indexReference & 0xFF);
    }

    void findLongIndex(long longValue){
        int indexReference = IntStream.range(1, this.getConstPool().getSize())
                .filter(value -> this.getConstPool().getTag(value) == ConstPool.CONST_Long)
                .filter(value -> this.getConstPool().getLongInfo(value) == longValue)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No matching long found."));
        this.add((indexReference >>> 8) & 0xFF, indexReference & 0xFF);
    }

    void addLongIndex(long longValue){
        int indexReference = this.getConstPool().addLongInfo(longValue);
        this.add((indexReference >>> 8) & 0xFF, indexReference & 0xFF);
    }

    void findFloatIndex(float floatValue){
        int indexReference = IntStream.range(1, this.getConstPool().getSize())
                .filter(value -> this.getConstPool().getTag(value) == ConstPool.CONST_Float)
                .filter(value -> this.getConstPool().getFloatInfo(value) == floatValue)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No matching float found."));
        this.add((indexReference >>> 8) & 0xFF, indexReference & 0xFF);
    }

    void addFloatIndex(float floatValue){
        int indexReference = this.getConstPool().addFloatInfo(floatValue);
        this.add((indexReference >>> 8) & 0xFF, indexReference & 0xFF);
    }

    /**
     * a double value stored in the constant pool is always a LDC2_W. This is different then a local variable holding a double.
     *
     * @param doubleValue primitive double.
     */
    void findDoubleIndex(double doubleValue){
        int indexReference = IntStream.range(1, this.getConstPool().getSize())
                .filter(value -> this.getConstPool().getTag(value) == ConstPool.CONST_Double)
                .filter(value -> this.getConstPool().getDoubleInfo(value) == doubleValue)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No matching double found."));
        this.addOpcode(Opcode.LDC2_W);
        this.add((indexReference >>> 8) & 0xFF, indexReference & 0xFF);
    }

    /**
     * a double value stored in the constant pool is always a LDC2_W. This is different then a local variable holding a double.
     *
     * @param doubleValue primitive double.
     */
    void addDoubleIndex(double doubleValue){
        int indexReference = this.getConstPool().addDoubleInfo(doubleValue);
        this.addOpcode(Opcode.LDC2_W);
        this.add((indexReference >>> 8) & 0xFF, indexReference & 0xFF);
    }

    private boolean hasAClassDeclaringTag(int tag){
        return tag == ConstPool.CONST_Methodref || tag == ConstPool.CONST_Fieldref || tag == ConstPool.CONST_InterfaceMethodref;
    }

    /**
     * Often a class's name will appear twice in the constant pool. One of the occurrence is not used as a declaring class for anything.
     * I have no idea why it's present but it can break looking up constant pool references if the unassociated one is picked. JA has a
     * built in way of finding existent references but a underlying mechanic is that a hash map uses a string class name as a key
     * in a hashMap. Two equal strings will overwrite each other in this case. This is part the the tools library to look for matches
     * instead of relying on JA.
     *
     * 1. scan the constant pool and get the class references that match className.
     * 2. scan again through the constant pool looking for class associations that use the references found in #1. One of the options
     *      will have no references and illuminate that one to return the one that should be used.
     *
     * @param className String type object, uses full class name and periods.
     * @return int primitive, the address in constant pool for the class matching className.
     */
    private int getClassReferenceIndex(String className){
        return IntStream.range(1, this.getConstPool().getSize())
                .filter(value -> this.getConstPool().getTag(value) == ConstPool.CONST_Class)
                .filter(value -> Objects.equals(Descriptor.toClassName(this.getConstPool().getClassInfoByDescriptor(value)), className))
                .filter( verifyIndex ->
                        IntStream.range(1, this.getConstPool().getSize())
                                .filter(value -> hasAClassDeclaringTag(this.getConstPool().getTag(value)))
                                .filter(value -> {
                                    boolean result = false;
                                    switch (this.getConstPool().getTag(value)) {
                                        case ConstPool.CONST_Methodref:
                                            result = this.getConstPool().getMethodrefClass(value) == verifyIndex;
                                            break;
                                        case ConstPool.CONST_Fieldref:
                                            result = this.getConstPool().getFieldrefClass(value) == verifyIndex;
                                            break;
                                        case ConstPool.CONST_InterfaceMethodref:
                                            result = this.getConstPool().getInterfaceMethodrefClass(value) == verifyIndex;
                                            break;
                                    }
                                    return result;})
                                .count() > 0
                )
                .findFirst()
                .orElse(-1);
    }

    void findInterfaceMethodIndex(String name, String descriptor){
        if(IntStream.range(1, this.getConstPool().getSize())
                .filter(value -> this.getConstPool().getTag(value) == ConstPool.CONST_InterfaceMethodref)
                .filter(value -> this.getConstPool().eqMember(name, descriptor, value) != null)
                .count() != 1){
            throw new RuntimeException("No matching interface found.");
        }
        else {
            int indexReference = IntStream.range(1, this.getConstPool().getSize())
                    .filter(value -> this.getConstPool().getTag(value) == ConstPool.CONST_InterfaceMethodref)
                    .filter(value -> this.getConstPool().eqMember(name, descriptor, value) != null)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No matching interface found."));
            this.add((indexReference >>> 8) & 0xFF, indexReference & 0xFF);
        }
    }

    void addInterfaceMethodIndex(String name, String descriptor){
        int classIndexReference = IntStream.range(1, this.getConstPool().getSize())
                .filter(value -> this.getConstPool().getTag(value) == ConstPool.CONST_Class)
                .filter(value -> Objects.equals(this.getConstPool().getClassInfoByDescriptor(value), Descriptor.toClassName(descriptor)))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No matching class found."));
        int indexReference = this.getConstPool().addInterfaceMethodrefInfo(classIndexReference, name, descriptor);
        this.add((indexReference >>> 8) & 0xFF, indexReference & 0xFF);
    }

    void codeBranching(int opcode, int branchCount){
        this.addOpcode(opcode);
        this.add((branchCount >>> 8) & 0xFF, branchCount & 0xFF);
    }

    void localVariableIndex(int opcode, int slot){
        this.addOpcode(opcode);
        this.add(slot);
    }

    void integerIndex(int opcode, int value){
        switch (opcode) {
            case Opcode.BIPUSH :
                this.addOpcode(Opcode.BIPUSH);
                this.add((byte)value);
                break;
            case Opcode.SIPUSH :
                this.addOpcode(Opcode.SIPUSH);
                this.add((value >>> 8) & 0xFF, value & 0xFF);
        }
    }

    /**
     * Encode the value for arg "integer" into the appropriate byteCode opCode + operand  for the java-int. Add the
     * encoded information to the byte code object "bytecode".
     *
     * @param integer int value.
     */
    void addInteger(int integer) {
        switch (integer) {
            case -1:
                this.add(Opcode.ICONST_M1);
                break;
            case 0:
                this.add(Opcode.ICONST_0);
                break;
            case 1:
                this.add(Opcode.ICONST_1);
                break;
            case 2:
                this.add(Opcode.ICONST_2);
                break;
            case 3:
                this.add(Opcode.ICONST_3);
                break;
            case 4:
                this.add(Opcode.ICONST_4);
                break;
            case 5:
                this.add(Opcode.ICONST_5);
                break;
            default:
                if (integer >= Byte.MIN_VALUE && integer <= Byte.MAX_VALUE) {
                    this.add(Opcode.BIPUSH);
                    // integer bound to byte size.
                    this.add(integer);
                } else if (integer >= Short.MIN_VALUE && integer <= Short.MAX_VALUE) {
                    this.add(Opcode.SIPUSH);
                    // Since byte code requires byte sized blocks, break up integer with bitmask and shift.
                    this.add((integer & 0xff00) >>> 8, integer & 0x00ff);
                } else {
                    // Appends LDC or LDC_W depending on constant pool size.
                    this.addLdc(this.getConstPool().addIntegerInfo(integer));
                }
        }
    }

    /**
     * Decode the byte code represented by a opCode + operand(s) at the position in arg "instructionIndex". Return
     * decoded data as java-int.
     *
     * @param codeIterator JA CodeIterator object.
     * @param instructionIndex int value, it is the codeIterator index of an opCode.
     * @return int value.
     */
    int getInteger(CodeIterator codeIterator, int instructionIndex) {
        int opCode = codeIterator.byteAt(instructionIndex);
        switch (opCode) {
            case Opcode.ICONST_M1:
                return -1;
            case Opcode.ICONST_0:
                return 0;
            case Opcode.ICONST_1:
                return 1;
            case Opcode.ICONST_2:
                return 2;
            case Opcode.ICONST_3:
                return 3;
            case Opcode.ICONST_4:
                return 4;
            case Opcode.ICONST_5:
                return 5;
            case Opcode.BIPUSH:
                return codeIterator.byteAt(instructionIndex + 1);
            case Opcode.SIPUSH:
                return codeIterator.s16bitAt(instructionIndex + 1);
            case Opcode.LDC:
                return this.getConstPool().getIntegerInfo(codeIterator.byteAt(instructionIndex + 1));
            case Opcode.LDC_W:
                return this.getConstPool().getIntegerInfo(codeIterator.u16bitAt(instructionIndex + 1));
            default:
                throw new RuntimeException(String.format("Failed to decode integer. Pos = %d, Bytecode = %d", instructionIndex, opCode));
        }
    }

    static int findSlotInLocalVariableTable(CodeAttribute codeAttribute, String variableName){
        LocalVariableAttribute table = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        int tableOrdinal;
        tableOrdinal = IntStream.range(0,table.tableLength()).filter(value -> Objects.equals(table.variableName(value), variableName )).findFirst().orElse(-1);
        if (tableOrdinal == -1){
            return -1;
        }
        return table.index(tableOrdinal);
    }

    static int findLineNumberInLineNumberTable(CodeAttribute codeAttribute, String variableName){
        LocalVariableAttribute table = (LocalVariableAttribute) codeAttribute.getAttribute(LineNumberAttribute.tag);
        int tableOrdinal;
        tableOrdinal = IntStream.range(0,table.tableLength()).filter(value -> Objects.equals(table.variableName(value), variableName )).findFirst().orElse(-1);
        if (tableOrdinal == -1){
            return -1;
        }
        return table.index(tableOrdinal);
    }

    private static int[] getInstruction(int size, int index, CodeIterator codeIterator) {
        int[] toReturn = null;
        int bitLine;
        int bitLine2;
        switch (size) {
            case 1:
                bitLine = codeIterator.byteAt(index);
                toReturn = new int[]{
                        bitLine
                };
                break;
            case 2:
                bitLine = codeIterator.s16bitAt(index);
                toReturn = new int[]{
                        (bitLine & 0xff00) >>> 8,
                        bitLine & 0x00ff
                };
                break;
            case 3:
                bitLine = codeIterator.s32bitAt(index);
                toReturn = new int[]{
                        (bitLine & 0xff000000) >>> 24,
                        (bitLine & 0x00ff0000) >>> 16,
                        (bitLine & 0x0000ff00) >>> 8
                        // not using the last byte
                };
                break;
            case 4:
                bitLine = codeIterator.s32bitAt(index);
                toReturn = new int[]{
                        (bitLine & 0xff000000) >>> 24,
                        (bitLine & 0x00ff0000) >>> 16,
                        (bitLine & 0x0000ff00) >>> 8,
                        (bitLine & 0x000000ff)
                };
                break;
            case 5:
                bitLine = codeIterator.s32bitAt(index);
                bitLine2 = codeIterator.byteAt(index + 4);
                toReturn = new int[]{
                        (bitLine & 0xff000000) >>> 24,
                        (bitLine & 0x00ff0000) >>> 16,
                        (bitLine & 0x0000ff00) >>> 8,
                        (bitLine & 0x000000ff),
                        bitLine2
                };
                break;
            case 6:
                bitLine = codeIterator.s32bitAt(index);
                bitLine2 = codeIterator.s16bitAt(index + 4);
                toReturn = new int[]{
                        (bitLine & 0xff000000) >>> 24,
                        (bitLine & 0x00ff0000) >>> 16,
                        (bitLine & 0x0000ff00) >>> 8,
                        (bitLine & 0x000000ff),
                        (bitLine2 & 0xff00) >>> 8,
                        (bitLine2 & 0x00ff)
                };
                break;
            case 7:
                bitLine = codeIterator.s32bitAt(index);
                bitLine2 = codeIterator.s32bitAt(index + 4);
                toReturn = new int[]{
                        (bitLine & 0xff000000) >>> 24,
                        (bitLine & 0x00ff0000) >>> 16,
                        (bitLine & 0x0000ff00) >>> 8,
                        (bitLine & 0x000000ff),
                        (bitLine2 & 0xff000000) >>> 24,
                        (bitLine2 & 0x00ff0000) >>> 16,
                        (bitLine2 & 0x0000ff00) >>> 8
                        // not using the last byte
                };
                break;
            case 8:
                bitLine = codeIterator.s32bitAt(index);
                bitLine2 = codeIterator.s32bitAt(index + 4);
                toReturn = new int[]{
                        (bitLine & 0xff000000) >>> 24,
                        (bitLine & 0x00ff0000) >>> 16,
                        (bitLine & 0x0000ff00) >>> 8,
                        (bitLine & 0x000000ff),
                        (bitLine2 & 0xff000000) >>> 24,
                        (bitLine2 & 0x00ff0000) >>> 16,
                        (bitLine2 & 0x0000ff00) >>> 8,
                        (bitLine2 & 0x000000ff)
                };
                break;
        }
        return toReturn;
    }

    public static void byteCodePrint(String destinationPath, CodeIterator codeIterator) throws FileNotFoundException, BadBytecode {
        Path printPath = Paths.get(destinationPath);
        PrintWriter out = new PrintWriter(printPath.toFile());
        final String[] instructionOut = {""};
        codeIterator.begin();
        while (codeIterator.hasNext()) {
            int index = codeIterator.next();
            int[] instruction = getInstruction(codeIterator.lookAhead() - index, index, codeIterator);
            instructionOut[0] += Integer.toString(index);
            instructionOut[0] += " ";
            instructionOut[0] += Mnemonic.OPCODE[instruction[0]];
            if (instruction.length > 1) {
                instructionOut[0] += " ";
                IntStream.range(1, instruction.length)
                        .forEach(value -> {
                            instructionOut[0] += Integer.toString(instruction[value]);
                            instructionOut[0] += " ";
                        });
            }
            out.println(instructionOut[0]);
            instructionOut[0] = "";
        }
        out.close();
    }

    static void printArrayToHex(Object[] obj, String name, Logger logger){
        int length = obj.length;
        int[] c = new int[length];
        for (int i=0;i<length;i++){
            c[i]=(int)obj[i];
        }
        String[] a = new String[length];
        for (int i=0;i<length;i++){
            a[i]=String.format("%02X", c[i] & 0xff);
        }
        logger.log(Level.INFO,name + " : " + Arrays.toString(a));
    }
}