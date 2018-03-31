package mod.sin.wyvern.mastercraft;

import com.wurmonline.server.skills.SkillList;
import javassist.*;
import javassist.bytecode.*;

import org.gotti.wurmunlimited.modloader.classhooks.CodeReplacer;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.ArrayList;

class ExtendTitleEnum {

    private final String className;
    private final int valuesSizerIndex; // the bytecode index which puts a size specifying value on the stack for anewarray.
    private final int populateVALUESIndex; // the bytecode index where references to various enum instances are put in the $VALUES array.
    private final ConstPool constPool;

    private static ArrayList<EnumFields> toExtendEntries = new ArrayList<>();
    private static ExtendTitleEnum singletonInstance;
    
    private ExtendTitleEnum(String className, int valuesSizerIndex, int populateVALUESIndex, ConstPool constPool) {
        this.className = className;
        this.valuesSizerIndex = valuesSizerIndex;
        this.populateVALUESIndex = populateVALUESIndex;
        this.constPool = constPool;
    }

    /**
     * Goes through the enum class's initiator to find bytecode index positions.
     *
     * @throws BadBytecode forwarded, Javassist stuff.
     */
    static void builder(String className) throws BadBytecode, NotFoundException {
        int valuesSizerIndex = -1;
        //int indexANEWARRAY = -1;
        int populateVALUESIndex = -1;
        CtClass ctClassEnum = HookManager.getInstance().getClassPool().get(className);
        ConstPool constPool = ctClassEnum.getClassFile().getConstPool();
        CodeIterator codeIterator = ctClassEnum.getClassInitializer().getMethodInfo().getCodeAttribute().iterator();
        // Get the byte code instruction index for
        // 1) size value for ANEWARRAY,
        // 2) the VALUES array assignment or population.
        BytecodeTools b = new BytecodeTools(constPool);
        String valuesDescriptor = className.replace(".", "/");
        valuesDescriptor = "[L" + valuesDescriptor + ";";
        int constPoolValuesIndex = b.findFieldIndex(Opcode.PUTSTATIC, "$VALUES",
                valuesDescriptor, className);
        codeIterator.begin();
        int lastIndex = 0;
        while (codeIterator.hasNext()){
            int instructionIndex = codeIterator.next();
            int opCode = codeIterator.byteAt(instructionIndex);
            switch (opCode){
                case Opcode.ANEWARRAY :
                    valuesSizerIndex = lastIndex;
                    //indexANEWARRAY = instructionIndex;
                    break;
                case Opcode.PUTSTATIC :
                    int cpAddress = codeIterator.u16bitAt(instructionIndex+1);
                    if (cpAddress == constPoolValuesIndex){
                        populateVALUESIndex = instructionIndex;
                    }
                    break;
                default:
                    break;
            }
            lastIndex = instructionIndex;
        }

        synchronized (ExtendTitleEnum.class) {
            singletonInstance = new ExtendTitleEnum(className, valuesSizerIndex, populateVALUESIndex, constPool);
        }
    }


    static ExtendTitleEnum getSingletonInstance() {
        return singletonInstance;
    }

    /**
     * A method to create data structures and add record a reference for that object.
     *
     * @param fieldName the name for the enum entry.
     * @param titleId an ordinal for the Titles.Title enum.
     * @param maleName in-game title name for male toons.
     * @param femaleName in-game title name for femaleName toons.
     * @param skillId A id number for the skill associated with the title, see {@link SkillList}
     * @param titleTypes A string representation of entries in {@link com.wurmonline.server.players.Titles.TitleType}. In
     *                 order to avoid premature class initialization for Javassist's bytecode stages we use a string
     *                 instead of the WU object. The string must match one of the enum field names.
     */
    synchronized void addExtendEntry(String fieldName, int titleId, String maleName, String femaleName, int skillId, String titleTypes) {
        if (singletonInstance == null) {
            throw new RuntimeException("ExtendTitleEnum instance is null, build it before addExtendEntry");
        }
        EnumFields enumFields =  new EnumFields(fieldName, titleId, maleName, femaleName, skillId, titleTypes);
        toExtendEntries.add(enumFields);
    }

    class EnumFields {
        final String fieldName;
        final int titleId;
        final String maleName;
        final String femaleName;
        final int skillId;
        final String titleTypes;

        /**
         * @param fieldName the name for the enum entry.
         * @param titleId an ordinal for the Titles.Title enum.
         * @param maleName in-game title name for male toons.
         * @param femaleName in-game title name for femaleName toons.
         * @param skillId A id number for the skill associated with the title, see {@link SkillList}
         * @param titleTypes A string representation of entries in {@link com.wurmonline.server.players.Titles.TitleType}. In
         *                 order to avoid premature class initialization for Javassist's bytecode stages we use a string
         *                 instead of the WU object.
         **/
        EnumFields(String fieldName, int titleId, String maleName, String femaleName,
                   int skillId, String titleTypes){
            this.fieldName = fieldName;
            this.titleId = titleId;
            this.maleName = maleName;
            this.femaleName = femaleName;
            this.skillId = skillId;
            this.titleTypes = titleTypes;
        }
    }

    /**
     * Intended to be used in WurmServerMod-initiate section and it's for bytecode changes. This adds field objects to the enum class.
     *
     * @throws CannotCompileException forwarded, Javassist stuff.
     */
    private synchronized void createFieldsInEnum() throws CannotCompileException, NotFoundException {
        if (toExtendEntries.size() == 0){
            throw new RuntimeException("Can not extend an enum without values in toExtendEntries arrayList.");
        }

        CtClass enumCtClass = HookManager.getInstance().getClassPool().get(this.className);
        for (EnumFields enumData : toExtendEntries) {
            CtField field = new CtField(enumCtClass, enumData.fieldName, enumCtClass);
            field.setModifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL | Modifier.ENUM);
            enumCtClass.addField(field);
        }
    }

    /**
     * This method uses JA bytecode to inject into the Enum's class initiator in order to expand the enum's $VALUES field.
     *
     * @throws BadBytecode forwarded, Javassist stuff.
     */
    private void resizeEnumVALUES() throws BadBytecode, ClassNotFoundException, NotFoundException {
        int expansion = toExtendEntries.size();
        CtClass ctClassEnum = HookManager.getInstance().getClassPool().get(this.className);
        CodeIterator codeIterator = ctClassEnum.getClassInitializer().getMethodInfo().getCodeAttribute().iterator();

        BytecodeTools findBytecode = new BytecodeTools(this.constPool);
        int currentSize = findBytecode.getInteger(codeIterator, this.valuesSizerIndex);
        findBytecode.addInteger(currentSize);
        findBytecode.addOpcode(Opcode.ANEWARRAY);
        findBytecode.addClassIndex(this.className);

        BytecodeTools replaceBytecode = new BytecodeTools(this.constPool);
        replaceBytecode.addInteger(currentSize + expansion);
        replaceBytecode.addOpcode(Opcode.ANEWARRAY);
        replaceBytecode.addClassIndex(this.className);

        CodeReplacer codeReplacer = new CodeReplacer(ctClassEnum.getClassInitializer().getMethodInfo().getCodeAttribute());
        codeReplacer.replaceCode(findBytecode.get(), replaceBytecode.get());
    }

    /**
     * This method builds bytecode to inject into the enum's initiator. The injected code initializes new enum entries and adds
     * a reference of that new object to the $VALUES array.
     *
     * @throws BadBytecode forwarded, JA stuff.
     * @throws ClassNotFoundException forwarded, JA stuff.
     * @throws NotFoundException forwarded, JA stuff.
     */
    synchronized void ExtendEnumEntries() throws BadBytecode, ClassNotFoundException, NotFoundException, CannotCompileException {
        createFieldsInEnum();
        CtClass ctClassEnum = HookManager.getInstance().getClassPool().get(this.className);
        CodeIterator initiatorCodeIterator = ctClassEnum.getClassInitializer().getMethodInfo().getCodeAttribute().iterator();

        BytecodeTools enumInitiator = new BytecodeTools(ctClassEnum.getClassFile().getConstPool());
        BytecodeTools populateVALUES = new BytecodeTools(ctClassEnum.getClassFile().getConstPool());
        int extensionCounter = 0;
        int valuesSize = enumInitiator.getInteger(initiatorCodeIterator, this.valuesSizerIndex);
        // Construct the two bytecode objects to be inserted. The multiple enumData in toExtendEntries are combined into one
        // long bytecode sequence and inserted at the proper point.
        for (EnumFields enumData : toExtendEntries) {
            enumInitiator.addOpcode(Opcode.NEW);
            enumInitiator.findClassIndex(this.className);
            enumInitiator.addOpcode(Opcode.DUP);
            enumInitiator.addLdc(enumData.fieldName);
            enumInitiator.addInteger(valuesSize + extensionCounter);
            enumInitiator.addInteger(enumData.titleId);
            enumInitiator.addLdc(enumData.maleName);
            enumInitiator.addLdc(enumData.femaleName);
            enumInitiator.addInteger(enumData.skillId);
            enumInitiator.addFieldIndex(Opcode.GETSTATIC, enumData.titleTypes,
                    "Lcom/wurmonline/server/players/Titles$TitleType;",
                    "com/wurmonline/server/players/Titles$TitleType");
            enumInitiator.addMethodIndex(Opcode.INVOKESPECIAL, "<init>",
                    "(Ljava/lang/String;IILjava/lang/String;Ljava/lang/String;ILcom/wurmonline/server/players/Titles$TitleType;)V",
                    this.className);
            enumInitiator.addFieldIndex(Opcode.PUTSTATIC, enumData.fieldName, "Lcom/wurmonline/server/players/Titles$Title;",
                    this.className);

            populateVALUES.addOpcode(Opcode.DUP);
            populateVALUES.addInteger(valuesSize + extensionCounter);
            extensionCounter++;
            populateVALUES.findFieldIndex(Opcode.GETSTATIC, enumData.fieldName, "Lcom/wurmonline/server/players/Titles$Title;",
                    this.className);
            populateVALUES.addOpcode(Opcode.AASTORE);
        }
        // Do bytecode changes from the bottom up so bytecode indexes don't change after every insert.
        initiatorCodeIterator.insert(populateVALUESIndex, populateVALUES.get());
        resizeEnumVALUES();
        initiatorCodeIterator.insert(valuesSizerIndex, enumInitiator.get());
    }
}