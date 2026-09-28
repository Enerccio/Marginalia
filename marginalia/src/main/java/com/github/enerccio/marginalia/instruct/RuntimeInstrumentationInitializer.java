package com.github.enerccio.marginalia.instruct;


import aj.org.objectweb.asm.Opcodes;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.SyntheticState;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.beans.factory.InitializingBean;

public class RuntimeInstrumentationInitializer implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        ByteBuddyAgent.install();

        new AgentBuilder.Default()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .type(ElementMatchers.isAnnotatedWith(Extendable.class))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) -> builder
                        .defineField("$extensionService", ExtensionService.class,
                                Visibility.PRIVATE,
                                Ownership.MEMBER,
                                FieldManifestation.PLAIN,
                                SyntheticState.SYNTHETIC)

                        .visit(Advice.to(ConstructorAdvice.class).on(ElementMatchers.isConstructor()))

                        .visit(new AsmVisitorWrapper.ForDeclaredMethods()
                                .method(
                                        ElementMatchers.not(ElementMatchers.isPrivate())
                                                .and(ElementMatchers.not(ElementMatchers.isConstructor()))
                                                .and(ElementMatchers.not(ElementMatchers.isStatic())),
                                        (instrumentedType, instrumentedMethod, methodVisitor, implementationContext, typePool, writerFlags, readerFlags) ->
                                                new LocalVarTrackingMethodVisitor(
                                                        Opcodes.ASM9,
                                                        methodVisitor,
                                                        instrumentedMethod.getStackSize(), // Reserved initial local variable slots
                                                        instrumentedType.getInternalName(),
                                                        instrumentedMethod.getName()
                                                )
                                )
                        ))
                .installOnByteBuddyAgent();
    }

}