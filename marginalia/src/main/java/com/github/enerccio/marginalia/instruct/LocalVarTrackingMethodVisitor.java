package com.github.enerccio.marginalia.instruct;

import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;

public class LocalVarTrackingMethodVisitor extends MethodVisitor {

    private static final String SERVICE_TYPE = "com/github/enerccio/marginalia/domain/service/ExtensionService";
    private static final String SERVICE_DESC = "L" + SERVICE_TYPE + ";";
    private static final String CONTEXT_TYPE = "com/github/enerccio/marginalia/domain/service/ExtensionService$ExtendableMethodContext";
    private static final String CONTEXT_DESC = "L" + CONTEXT_TYPE + ";";

    private final String ownerClassName;
    private final String ownerName;
    private final int serviceSlot;
    private final int contextSlot;
    private final int thrownSlot;

    private final Label startLabel = new Label();
    private final Label endLabel = new Label();
    private final Label handlerLabel = new Label();

    public LocalVarTrackingMethodVisitor(int api, MethodVisitor methodVisitor, int initialLocalSlots, String ownerClassName, String ownerName) {
        super(api, methodVisitor);
        this.ownerClassName = ownerClassName;
        this.ownerName = ownerName;

        // Reserve 3 local variable slots at the end of the method's local variable table
        this.serviceSlot = initialLocalSlots;
        this.contextSlot = initialLocalSlots + 1;
        this.thrownSlot = initialLocalSlots + 2;
    }

    @Override
    public void visitCode() {
        super.visitCode();

        // 1. Register Try-Catch Block BEFORE emitting method instructions
        super.visitTryCatchBlock(startLabel, endLabel, handlerLabel, "java/lang/Throwable");

        // 2. Fetch 'this.$extensionService'
        super.visitVarInsn(Opcodes.ALOAD, 0);
        super.visitFieldInsn(Opcodes.GETFIELD, ownerClassName, "$extensionService", SERVICE_DESC);
        super.visitVarInsn(Opcodes.ASTORE, serviceSlot);

        // Initialize $extensionContext = null, $thrown = null
        super.visitInsn(Opcodes.ACONST_NULL);
        super.visitVarInsn(Opcodes.ASTORE, contextSlot);
        super.visitInsn(Opcodes.ACONST_NULL);
        super.visitVarInsn(Opcodes.ASTORE, thrownSlot);

        // if ($extensionService != null) { ... }
        Label skipEnter = new Label();
        super.visitVarInsn(Opcodes.ALOAD, serviceSlot);
        super.visitJumpInsn(Opcodes.IFNULL, skipEnter);

        // $extensionService.onExtendableMethodEnter(this.getClass(), this, methodName);
        super.visitVarInsn(Opcodes.ALOAD, serviceSlot);
        super.visitVarInsn(Opcodes.ALOAD, 0);
        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        super.visitVarInsn(Opcodes.ALOAD, 0);
        super.visitLdcInsn(ownerName);
        super.visitMethodInsn(Opcodes.INVOKEINTERFACE, SERVICE_TYPE, "onExtendableMethodEnter", "(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/String;)V", true);

        // $extensionContext = $extensionService.createContextHolder(this);
        super.visitVarInsn(Opcodes.ALOAD, serviceSlot);
        super.visitVarInsn(Opcodes.ALOAD, 0);
        super.visitMethodInsn(Opcodes.INVOKEINTERFACE, SERVICE_TYPE, "createContextHolder", "(Ljava/lang/Object;)" + CONTEXT_DESC, true);
        super.visitVarInsn(Opcodes.ASTORE, contextSlot);

        super.visitLabel(skipEnter);

        // --- TRY BLOCK START ---
        super.visitLabel(startLabel);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        // Execute original store (e.g., ASTORE layout)
        super.visitVarInsn(opcode, var);

        // Intercept ASTORE instructions for local object assignments
        if (opcode == Opcodes.ASTORE && var > 0 && var < serviceSlot) {
            Label skipRegister = new Label();
            super.visitVarInsn(Opcodes.ALOAD, contextSlot);
            super.visitJumpInsn(Opcodes.IFNULL, skipRegister);

            // $extensionContext.registerLocalVariable(var, varValue, varType);
            super.visitVarInsn(Opcodes.ALOAD, contextSlot);
            super.visitLdcInsn(var);
            super.visitVarInsn(Opcodes.ALOAD, var);

            // Pass object's runtime class or Object.class fallback
            Label nullClassLabel = new Label();
            Label afterClassLabel = new Label();
            super.visitVarInsn(Opcodes.ALOAD, var);
            super.visitJumpInsn(Opcodes.IFNULL, nullClassLabel);
            super.visitVarInsn(Opcodes.ALOAD, var);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
            super.visitJumpInsn(Opcodes.GOTO, afterClassLabel);

            super.visitLabel(nullClassLabel);
            super.visitLdcInsn(Type.getType("Ljava/lang/Object;"));

            super.visitLabel(afterClassLabel);

            super.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    CONTEXT_TYPE,
                    "registerLocalVariable",
                    "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Class;)V",
                    true
            );

            super.visitLabel(skipRegister);
        }
    }

    @Override
    public void visitInsn(int opcode) {
        // Intercept all RETURN statements (IRETURN, ARETURN, RETURN, etc.)
        if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
            emitLeaveCall();
        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        // --- TRY BLOCK END ---
        super.visitLabel(endLabel);

        Label skipHandler = new Label();
        super.visitJumpInsn(Opcodes.GOTO, skipHandler);

        // --- CATCH / FINALLY EXCEPTION HANDLER ---
        super.visitLabel(handlerLabel);
        super.visitVarInsn(Opcodes.ASTORE, thrownSlot);

        emitLeaveCall();

        // Re-throw
        super.visitVarInsn(Opcodes.ALOAD, thrownSlot);
        super.visitInsn(Opcodes.ATHROW);

        super.visitLabel(skipHandler);

        // Expand stack and local variable limits to fit injected instructions
        super.visitMaxs(maxStack + 5, maxLocals + 3);
    }

    private void emitLeaveCall() {
        Label skipLeave = new Label();
        super.visitVarInsn(Opcodes.ALOAD, serviceSlot);
        super.visitJumpInsn(Opcodes.IFNULL, skipLeave);

        super.visitVarInsn(Opcodes.ALOAD, serviceSlot);
        super.visitVarInsn(Opcodes.ALOAD, 0);
        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        super.visitVarInsn(Opcodes.ALOAD, 0);
        super.visitVarInsn(Opcodes.ALOAD, contextSlot);
        super.visitLdcInsn(ownerName);
        super.visitVarInsn(Opcodes.ALOAD, thrownSlot);

        super.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                SERVICE_TYPE,
                "onExtendableMethodLeave",
                "(Ljava/lang/Class;Ljava/lang/Object;" + CONTEXT_DESC + "Ljava/lang/String;Ljava/lang/Throwable;)V",
                true
        );

        super.visitLabel(skipLeave);
    }
}