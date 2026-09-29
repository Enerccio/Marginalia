package com.github.enerccio.marginalia.instruct;

import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class LocalVarTrackingMethodVisitor extends MethodVisitor {

    private static final String SERVICE_TYPE = "com/github/enerccio/marginalia/domain/service/ExtensionService";
    private static final String SERVICE_DESC = "L" + SERVICE_TYPE + ";";
    private static final String CONTEXT_TYPE = "com/github/enerccio/marginalia/domain/service/ExtensionService$ExtendableMethodContext";
    private static final String CONTEXT_DESC = "L" + CONTEXT_TYPE + ";";

    private final MethodVisitor targetVisitor;
    private final int argSlots;
    private final String ownerClassName;
    private final String ownerName;

    private final int serviceSlot;
    private final int contextSlot;
    private final int thrownSlot;

    private final Map<Integer, String> slotToNameMap = new HashMap<>();
    private final List<Consumer<MethodVisitor>> tryCatchBlocks = new ArrayList<>();
    private final List<Consumer<MethodVisitor>> instructions = new ArrayList<>();
    private final List<Consumer<MethodVisitor>> localVarsToReplay = new ArrayList<>();

    private final Label startLabel = new Label();
    private final Label endLabel = new Label();
    private final Label handlerLabel = new Label();

    public LocalVarTrackingMethodVisitor(int api, MethodVisitor targetVisitor, int argSlots, String ownerClassName, String ownerName) {
        super(api, null);
        this.targetVisitor = targetVisitor;
        this.argSlots = argSlots;
        this.ownerClassName = ownerClassName;
        this.ownerName = ownerName;

        this.serviceSlot = argSlots;
        this.contextSlot = argSlots + 1;
        this.thrownSlot = argSlots + 2;
    }

    // --- 1. Buffer Method Body Instructions ---

    @Override
    public void visitInsn(int opcode) {
        instructions.add(mv -> {
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                emitLeaveCall(mv);
            }
            mv.visitInsn(opcode);
        });
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        instructions.add(mv -> mv.visitIntInsn(opcode, operand));
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        instructions.add(mv -> {
            int remappedVar = (var >= argSlots) ? var + 3 : var;
            mv.visitVarInsn(opcode, remappedVar);

            if (opcode == Opcodes.ASTORE) {
                String varName = slotToNameMap.getOrDefault(var, "var" + var);

                Label skipRegister = new Label();
                mv.visitVarInsn(Opcodes.ALOAD, contextSlot);
                mv.visitJumpInsn(Opcodes.IFNULL, skipRegister);

                mv.visitVarInsn(Opcodes.ALOAD, contextSlot);
                mv.visitLdcInsn(varName);
                mv.visitVarInsn(Opcodes.ALOAD, remappedVar);

                Label nullClassLabel = new Label();
                Label afterClassLabel = new Label();
                mv.visitVarInsn(Opcodes.ALOAD, remappedVar);
                mv.visitJumpInsn(Opcodes.IFNULL, nullClassLabel);
                mv.visitVarInsn(Opcodes.ALOAD, remappedVar);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
                mv.visitJumpInsn(Opcodes.GOTO, afterClassLabel);

                mv.visitLabel(nullClassLabel);
                mv.visitLdcInsn(Type.getType("Ljava/lang/Object;"));

                mv.visitLabel(afterClassLabel);

                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE,
                        CONTEXT_TYPE,
                        "registerLocalVariable",
                        "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Class;)V",
                        true
                );

                mv.visitLabel(skipRegister);
            }
        });
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        instructions.add(mv -> mv.visitTypeInsn(opcode, type));
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        instructions.add(mv -> mv.visitFieldInsn(opcode, owner, name, descriptor));
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        instructions.add(mv -> mv.visitMethodInsn(opcode, owner, name, descriptor, isInterface));
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, net.bytebuddy.jar.asm.Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        instructions.add(mv -> mv.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments));
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        instructions.add(mv -> mv.visitJumpInsn(opcode, label));
    }

    @Override
    public void visitLabel(Label label) {
        instructions.add(mv -> mv.visitLabel(label));
    }

    @Override
    public void visitLdcInsn(Object value) {
        instructions.add(mv -> mv.visitLdcInsn(value));
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        instructions.add(mv -> {
            int remappedVar = (var >= argSlots) ? var + 3 : var;
            mv.visitIincInsn(remappedVar, increment);
        });
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        instructions.add(mv -> mv.visitTableSwitchInsn(min, max, dflt, labels));
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        instructions.add(mv -> mv.visitLookupSwitchInsn(dflt, keys, labels));
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        instructions.add(mv -> mv.visitMultiANewArrayInsn(descriptor, numDimensions));
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        // Collect try-catch definitions separately from bytecode instructions
        tryCatchBlocks.add(mv -> mv.visitTryCatchBlock(start, end, handler, type));
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        instructions.add(mv -> mv.visitLineNumber(line, start));
    }

    // --- 2. Record Variable Names from LocalVariableTable ---

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        if (name != null && !name.equals("this")) {
            slotToNameMap.put(index, name);
        }
        localVarsToReplay.add(mv -> {
            int remappedIndex = (index >= argSlots) ? index + 3 : index;
            mv.visitLocalVariable(name, descriptor, signature, start, end, remappedIndex);
        });
    }

    // --- 3. Replay Bytecode & Emit Calls at visitEnd ---

    @Override
    public void visitEnd() {
        targetVisitor.visitCode();

        for (Consumer<MethodVisitor> action : tryCatchBlocks) {
            action.accept(targetVisitor);
        }

        targetVisitor.visitTryCatchBlock(startLabel, endLabel, handlerLabel, "java/lang/Throwable");

        targetVisitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "com/github/enerccio/marginalia/instruct/ExtensionServiceHolder",
                "getInstance",
                "()Lcom/github/enerccio/marginalia/domain/service/ExtensionService;",
                false
        );
        targetVisitor.visitVarInsn(Opcodes.ASTORE, serviceSlot);

        targetVisitor.visitInsn(Opcodes.ACONST_NULL);
        targetVisitor.visitVarInsn(Opcodes.ASTORE, contextSlot);
        targetVisitor.visitInsn(Opcodes.ACONST_NULL);
        targetVisitor.visitVarInsn(Opcodes.ASTORE, thrownSlot);

        Label skipEnter = new Label();
        targetVisitor.visitVarInsn(Opcodes.ALOAD, serviceSlot);
        targetVisitor.visitJumpInsn(Opcodes.IFNULL, skipEnter);

        targetVisitor.visitVarInsn(Opcodes.ALOAD, serviceSlot);
        targetVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        targetVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        targetVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        targetVisitor.visitLdcInsn(ownerName);
        targetVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, SERVICE_TYPE, "onExtendableMethodEnter", "(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/String;)V", true);

        targetVisitor.visitVarInsn(Opcodes.ALOAD, serviceSlot);
        targetVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        targetVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, SERVICE_TYPE, "createContextHolder", "(Ljava/lang/Object;)" + CONTEXT_DESC, true);
        targetVisitor.visitVarInsn(Opcodes.ASTORE, contextSlot);

        targetVisitor.visitLabel(skipEnter);
        targetVisitor.visitLabel(startLabel);

        for (Consumer<MethodVisitor> action : instructions) {
            action.accept(targetVisitor);
        }

        targetVisitor.visitLabel(endLabel);
        Label skipHandler = new Label();
        targetVisitor.visitJumpInsn(Opcodes.GOTO, skipHandler);

        targetVisitor.visitLabel(handlerLabel);
        targetVisitor.visitVarInsn(Opcodes.ASTORE, thrownSlot);

        emitLeaveCall(targetVisitor);

        targetVisitor.visitVarInsn(Opcodes.ALOAD, thrownSlot);
        targetVisitor.visitInsn(Opcodes.ATHROW);

        targetVisitor.visitLabel(skipHandler);

        for (Consumer<MethodVisitor> action : localVarsToReplay) {
            action.accept(targetVisitor);
        }

        targetVisitor.visitMaxs(0, 0);
        targetVisitor.visitEnd();

        super.visitEnd();
    }

    private void emitLeaveCall(MethodVisitor mv) {
        Label skipLeave = new Label();
        mv.visitVarInsn(Opcodes.ALOAD, serviceSlot);
        mv.visitJumpInsn(Opcodes.IFNULL, skipLeave);

        mv.visitVarInsn(Opcodes.ALOAD, serviceSlot);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, contextSlot);
        mv.visitLdcInsn(ownerName);
        mv.visitVarInsn(Opcodes.ALOAD, thrownSlot);

        mv.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                SERVICE_TYPE,
                "onExtendableMethodLeave",
                "(Ljava/lang/Class;Ljava/lang/Object;" + CONTEXT_DESC + "Ljava/lang/String;Ljava/lang/Throwable;)V",
                true
        );

        mv.visitLabel(skipLeave);
    }
}